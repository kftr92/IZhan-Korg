package com.example

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.midi.MidiDevice
import android.media.midi.MidiDeviceInfo
import android.media.midi.MidiInputPort
import android.media.midi.MidiManager
import android.media.midi.MidiOutputPort
import android.media.midi.MidiReceiver
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class KorgConnectionStatus {
    DISCONNECTED,
    SCANNING,
    CONNECTING,
    CONNECTED,
    ERROR
}

data class MidiTrafficLog(
    val id: Long = System.currentTimeMillis() + (0..999).random(),
    val timestamp: String,
    val direction: Direction,
    val summary: String,
    val hexDump: String
) {
    enum class Direction { IN, OUT, SYSTEM }
}

class KorgMidiService : Service() {

    private val binder = KorgMidiBinder()

    inner class KorgMidiBinder : Binder() {
        fun getService(): KorgMidiService = this@KorgMidiService
    }

    private lateinit var midiManager: MidiManager

    private val _connectionStatus = MutableStateFlow(KorgConnectionStatus.DISCONNECTED)
    val connectionStatus: StateFlow<KorgConnectionStatus> = _connectionStatus.asStateFlow()

    private val _statusMessage = MutableStateFlow("MIDI Manager initialized")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    private val _availableDevices = MutableStateFlow<List<MidiDeviceInfo>>(emptyList())
    val availableDevices: StateFlow<List<MidiDeviceInfo>> = _availableDevices.asStateFlow()

    private val _selectedInputDeviceInfo = MutableStateFlow<MidiDeviceInfo?>(null)
    val selectedInputDeviceInfo: StateFlow<MidiDeviceInfo?> = _selectedInputDeviceInfo.asStateFlow()

    private val _selectedOutputDeviceInfo = MutableStateFlow<MidiDeviceInfo?>(null)
    val selectedOutputDeviceInfo: StateFlow<MidiDeviceInfo?> = _selectedOutputDeviceInfo.asStateFlow()

    private val _selectedDeviceInfo = MutableStateFlow<MidiDeviceInfo?>(null)
    val selectedDeviceInfo: StateFlow<MidiDeviceInfo?> = _selectedDeviceInfo.asStateFlow()

    private val _currentPatchInfo = MutableStateFlow<KorgPatchInfo?>(null)
    val currentPatchInfo: StateFlow<KorgPatchInfo?> = _currentPatchInfo.asStateFlow()

    private val _trafficLogs = MutableStateFlow<List<MidiTrafficLog>>(emptyList())
    val trafficLogs: StateFlow<List<MidiTrafficLog>> = _trafficLogs.asStateFlow()

    private val _incomingMidiEvent = MutableStateFlow<IncomingMidiInputEvent?>(null)
    val incomingMidiEvent: StateFlow<IncomingMidiInputEvent?> = _incomingMidiEvent.asStateFlow()

    private var activeInputMidiDevice: MidiDevice? = null
    private var activeOutputMidiDevice: MidiDevice? = null
    private var inputPort: MidiInputPort? = null
    private var outputPort: MidiOutputPort? = null

    private var currentMsb = 0
    private var currentLsb = 0
    private var currentMode = "Prog"

    private val sysexAccumulator = ByteArrayOutputStream()
    private var inSysex = false

    private val timeFormatter = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    private val deviceCallback = object : MidiManager.DeviceCallback() {
        override fun onDeviceAdded(device: MidiDeviceInfo) {
            logTraffic(MidiTrafficLog.Direction.SYSTEM, "Device Added: ${getDeviceDisplayName(device)}", "")
            refreshDevices()
        }

        override fun onDeviceRemoved(device: MidiDeviceInfo) {
            logTraffic(MidiTrafficLog.Direction.SYSTEM, "Device Removed: ${getDeviceDisplayName(device)}", "")
            if (_selectedDeviceInfo.value?.id == device.id) {
                disconnectDevice("Selected MIDI device disconnected")
            }
            refreshDevices()
        }
    }

    private val midiReceiver = object : MidiReceiver() {
        override fun onSend(msg: ByteArray?, offset: Int, count: Int, timestamp: Long) {
            if (msg == null || count <= 0) return
            val rawBytes = msg.copyOfRange(offset, offset + count)
            logTraffic(MidiTrafficLog.Direction.IN, "RX (${count} bytes)", bytesToHex(rawBytes))
            parseIncomingMidiBytes(msg, offset, count)
        }
    }

    override fun onCreate() {
        super.onCreate()
        midiManager = getSystemService(Context.MIDI_SERVICE) as MidiManager
        midiManager.registerDeviceCallback(deviceCallback, Handler(Looper.getMainLooper()))
        refreshDevices()
        logTraffic(MidiTrafficLog.Direction.SYSTEM, "Korg MIDI Manager Service Started", "")
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    fun refreshDevices() {
        _connectionStatus.value = KorgConnectionStatus.SCANNING
        val devices = midiManager.devices.filter { it.inputPortCount > 0 || it.outputPortCount > 0 }
        _availableDevices.value = devices

        if (devices.isEmpty()) {
            _connectionStatus.value = KorgConnectionStatus.DISCONNECTED
            _statusMessage.value = "No MIDI hardware connected"
            return
        }

        val autoSelectInput = devices.firstOrNull { it.outputPortCount > 0 && isKorgDevice(it) }
            ?: devices.firstOrNull { it.outputPortCount > 0 }

        val autoSelectOutput = devices.firstOrNull { it.inputPortCount > 0 && isKorgDevice(it) }
            ?: devices.firstOrNull { it.inputPortCount > 0 }

        if (_selectedInputDeviceInfo.value == null && autoSelectInput != null) {
            connectInputDevice(autoSelectInput)
        }

        if (_selectedOutputDeviceInfo.value == null && autoSelectOutput != null) {
            connectOutputDevice(autoSelectOutput)
        }

        if (activeOutputMidiDevice != null || activeInputMidiDevice != null) {
            _connectionStatus.value = KorgConnectionStatus.CONNECTED
        } else {
            _connectionStatus.value = KorgConnectionStatus.DISCONNECTED
        }
    }

    fun isKorgDevice(device: MidiDeviceInfo): Boolean {
        val properties = device.properties
        val manufacturer = properties.getString(MidiDeviceInfo.PROPERTY_MANUFACTURER) ?: ""
        val name = properties.getString(MidiDeviceInfo.PROPERTY_NAME) ?: ""
        val product = properties.getString(MidiDeviceInfo.PROPERTY_PRODUCT) ?: ""
        val combined = "$manufacturer $name $product".uppercase(Locale.US)
        return combined.contains("KORG") || combined.contains("KROME") || combined.contains("KRONOS") ||
                combined.contains("MICROKORG") || combined.contains("MINILOGUE") || combined.contains("TRITON")
    }

    fun getDeviceDisplayName(device: MidiDeviceInfo): String {
        val properties = device.properties
        val product = properties.getString(MidiDeviceInfo.PROPERTY_PRODUCT)
        val name = properties.getString(MidiDeviceInfo.PROPERTY_NAME)
        val manufacturer = properties.getString(MidiDeviceInfo.PROPERTY_MANUFACTURER)
        return product ?: name ?: manufacturer ?: "MIDI Device #${device.id}"
    }

    fun connectInputDevice(deviceInfo: MidiDeviceInfo?) {
        if (deviceInfo == null) {
            outputPort?.disconnect(midiReceiver)
            outputPort?.close()
            outputPort = null
            if (activeInputMidiDevice != null && activeInputMidiDevice != activeOutputMidiDevice) {
                activeInputMidiDevice?.close()
            }
            activeInputMidiDevice = null
            _selectedInputDeviceInfo.value = null
            logTraffic(MidiTrafficLog.Direction.SYSTEM, "Input MIDI Device Disconnected", "")
            return
        }

        if (_selectedInputDeviceInfo.value?.id == deviceInfo.id && activeInputMidiDevice != null && outputPort != null) {
            return
        }

        outputPort?.disconnect(midiReceiver)
        outputPort?.close()
        outputPort = null

        if (activeInputMidiDevice != null && activeInputMidiDevice != activeOutputMidiDevice) {
            activeInputMidiDevice?.close()
        }
        activeInputMidiDevice = null

        _selectedInputDeviceInfo.value = deviceInfo
        val name = getDeviceDisplayName(deviceInfo)
        logTraffic(MidiTrafficLog.Direction.SYSTEM, "Connecting Input MIDI: $name", "ID: ${deviceInfo.id}")

        if (activeOutputMidiDevice != null && _selectedOutputDeviceInfo.value?.id == deviceInfo.id) {
            activeInputMidiDevice = activeOutputMidiDevice
            if (deviceInfo.outputPortCount > 0) {
                outputPort = activeInputMidiDevice?.openOutputPort(0)
                outputPort?.connect(midiReceiver)
                logTraffic(MidiTrafficLog.Direction.SYSTEM, "Input MIDI Connected (Shared device): $name", "")
            }
        } else {
            midiManager.openDevice(deviceInfo, { device ->
                if (device != null) {
                    activeInputMidiDevice = device
                    if (deviceInfo.outputPortCount > 0) {
                        outputPort = device.openOutputPort(0)
                        outputPort?.connect(midiReceiver)
                        logTraffic(MidiTrafficLog.Direction.SYSTEM, "Input MIDI Connected: $name", "")
                    }
                } else {
                    logTraffic(MidiTrafficLog.Direction.SYSTEM, "Failed to open Input MIDI: $name", "")
                }
            }, Handler(Looper.getMainLooper()))
        }
    }

    fun connectOutputDevice(deviceInfo: MidiDeviceInfo?) {
        if (deviceInfo == null) {
            inputPort?.close()
            inputPort = null
            if (activeOutputMidiDevice != null && activeOutputMidiDevice != activeInputMidiDevice) {
                activeOutputMidiDevice?.close()
            }
            activeOutputMidiDevice = null
            _selectedOutputDeviceInfo.value = null
            _selectedDeviceInfo.value = null
            _connectionStatus.value = KorgConnectionStatus.DISCONNECTED
            logTraffic(MidiTrafficLog.Direction.SYSTEM, "Output MIDI Device Disconnected", "")
            return
        }

        if (_selectedOutputDeviceInfo.value?.id == deviceInfo.id && activeOutputMidiDevice != null && inputPort != null) {
            return
        }

        inputPort?.close()
        inputPort = null

        if (activeOutputMidiDevice != null && activeOutputMidiDevice != activeInputMidiDevice) {
            activeOutputMidiDevice?.close()
        }
        activeOutputMidiDevice = null

        _selectedOutputDeviceInfo.value = deviceInfo
        _selectedDeviceInfo.value = deviceInfo
        _connectionStatus.value = KorgConnectionStatus.CONNECTING
        val name = getDeviceDisplayName(deviceInfo)
        _statusMessage.value = "Opening $name..."
        logTraffic(MidiTrafficLog.Direction.SYSTEM, "Connecting Output MIDI: $name", "ID: ${deviceInfo.id}")

        if (activeInputMidiDevice != null && _selectedInputDeviceInfo.value?.id == deviceInfo.id) {
            activeOutputMidiDevice = activeInputMidiDevice
            if (deviceInfo.inputPortCount > 0) {
                inputPort = activeOutputMidiDevice?.openInputPort(0)
                _connectionStatus.value = KorgConnectionStatus.CONNECTED
                _statusMessage.value = "Connected to $name"
                logTraffic(MidiTrafficLog.Direction.SYSTEM, "Output MIDI Connected (Shared device): $name", "")
                Handler(Looper.getMainLooper()).postDelayed({
                    requestCurrentSoundInfo(0)
                }, 300)
            }
        } else {
            midiManager.openDevice(deviceInfo, { device ->
                if (device != null) {
                    activeOutputMidiDevice = device
                    if (deviceInfo.inputPortCount > 0) {
                        inputPort = device.openInputPort(0)
                        _connectionStatus.value = KorgConnectionStatus.CONNECTED
                        _statusMessage.value = "Connected to $name"
                        logTraffic(MidiTrafficLog.Direction.SYSTEM, "Output MIDI Connected: $name", "")
                        Handler(Looper.getMainLooper()).postDelayed({
                            requestCurrentSoundInfo(0)
                        }, 300)
                    }
                } else {
                    _connectionStatus.value = KorgConnectionStatus.ERROR
                    _statusMessage.value = "Failed to open $name"
                    logTraffic(MidiTrafficLog.Direction.SYSTEM, "Failed to open Output MIDI: $name", "")
                }
            }, Handler(Looper.getMainLooper()))
        }
    }

    fun connectDevice(deviceInfo: MidiDeviceInfo) {
        connectInputDevice(deviceInfo)
        connectOutputDevice(deviceInfo)
    }

    fun disconnectDevice(reason: String = "User requested disconnect") {
        inputPort?.close()
        inputPort = null

        outputPort?.disconnect(midiReceiver)
        outputPort?.close()
        outputPort = null

        activeInputMidiDevice?.close()
        activeInputMidiDevice = null

        activeOutputMidiDevice?.close()
        activeOutputMidiDevice = null

        _selectedInputDeviceInfo.value = null
        _selectedOutputDeviceInfo.value = null
        _selectedDeviceInfo.value = null
        _connectionStatus.value = KorgConnectionStatus.DISCONNECTED
        _statusMessage.value = reason
        logTraffic(MidiTrafficLog.Direction.SYSTEM, "Disconnected", reason)
    }

    // --- MIDI TRANSMISSION COMMANDS ---

    fun sendProgramChange(channel: Int, msb: Int, lsb: Int, program: Int) {
        val port = inputPort ?: run {
            logTraffic(MidiTrafficLog.Direction.SYSTEM, "TX Skipped: Port Closed", "Attempted PC $program")
            return
        }
        try {
            val ch = channel.coerceIn(0, 15)

            // Bank Select MSB (CC 0)
            val msbBuffer = byteArrayOf((0xB0 or ch).toByte(), 0x00, msb.toByte())
            port.send(msbBuffer, 0, 3)

            // Bank Select LSB (CC 32)
            val lsbBuffer = byteArrayOf((0xB0 or ch).toByte(), 0x20, lsb.toByte())
            port.send(lsbBuffer, 0, 3)

            // Program Change
            val pcBuffer = byteArrayOf((0xC0 or ch).toByte(), program.toByte())
            port.send(pcBuffer, 0, 2)

            logTraffic(
                MidiTrafficLog.Direction.OUT,
                "TX: Bank MSB $msb LSB $lsb, PC $program (Ch ${ch + 1})",
                "${bytesToHex(msbBuffer)} ${bytesToHex(lsbBuffer)} ${bytesToHex(pcBuffer)}"
            )
        } catch (e: Exception) {
            Log.e("KorgMidiService", "Error sending Program Change", e)
            logTraffic(MidiTrafficLog.Direction.SYSTEM, "TX Error: Program Change", e.localizedMessage ?: "Unknown error")
        }
    }

    fun sendNoteOn(channel: Int, note: Int, velocity: Int) {
        val port = inputPort ?: return
        try {
            val ch = channel.coerceIn(0, 15)
            val buffer = byteArrayOf((0x90 or ch).toByte(), note.coerceIn(0, 127).toByte(), velocity.coerceIn(0, 127).toByte())
            port.send(buffer, 0, 3)
            logTraffic(MidiTrafficLog.Direction.OUT, "TX: Note On $note Vel $velocity (Ch ${ch + 1})", bytesToHex(buffer))
        } catch (e: Exception) {
            Log.e("KorgMidiService", "Error sending Note On", e)
        }
    }

    fun sendNoteOff(channel: Int, note: Int) {
        val port = inputPort ?: return
        try {
            val ch = channel.coerceIn(0, 15)
            val buffer = byteArrayOf((0x80 or ch).toByte(), note.coerceIn(0, 127).toByte(), 0x00)
            port.send(buffer, 0, 3)
            logTraffic(MidiTrafficLog.Direction.OUT, "TX: Note Off $note (Ch ${ch + 1})", bytesToHex(buffer))
        } catch (e: Exception) {
            Log.e("KorgMidiService", "Error sending Note Off", e)
        }
    }

    fun sendPitchBend(channel: Int, value: Int) {
        val port = inputPort ?: return
        try {
            val ch = channel.coerceIn(0, 15)
            val clamped = value.coerceIn(0, 16383)
            val lsb = (clamped and 0x7F).toByte()
            val msb = ((clamped shr 7) and 0x7F).toByte()
            val buffer = byteArrayOf((0xE0 or ch).toByte(), lsb, msb)
            port.send(buffer, 0, 3)
            logTraffic(MidiTrafficLog.Direction.OUT, "TX: Pitch Bend $clamped (Ch ${ch + 1})", bytesToHex(buffer))
        } catch (e: Exception) {
            Log.e("KorgMidiService", "Error sending Pitch Bend", e)
        }
    }

    fun sendMasterCoarseTune(channel: Int, transpose: Int) {
        val port = inputPort ?: return
        try {
            val ch = channel.coerceIn(0, 15)
            val mm = (64 + transpose.coerceIn(-12, 12)).toByte()
            val sysex = byteArrayOf(
                0xF0.toByte(), 0x7F.toByte(), ch.toByte(), 0x04.toByte(),
                0x04.toByte(), 0x00.toByte(), mm, 0xF7.toByte()
            )
            port.send(sysex, 0, sysex.size)
            logTraffic(MidiTrafficLog.Direction.OUT, "TX SysEx: Master Transpose ${if (transpose >= 0) "+$transpose" else "$transpose"} semitones", bytesToHex(sysex))
        } catch (e: Exception) {
            Log.e("KorgMidiService", "Error sending Transpose SysEx", e)
        }
    }

    fun sendModeChange(channel: Int, mode: Int) {
        val port = inputPort ?: return
        try {
            val ch = channel.coerceIn(0, 15)
            val modeName = if (mode == 0) "Combi" else if (mode == 2) "Prog" else "Seq"
            val sysex = byteArrayOf(
                0xF0.toByte(), 0x42.toByte(), (0x30 or ch).toByte(), 0x00.toByte(),
                0x01.toByte(), 0x15.toByte(), 0x4E.toByte(), mode.toByte(), 0xF7.toByte()
            )
            port.send(sysex, 0, sysex.size)
            logTraffic(MidiTrafficLog.Direction.OUT, "TX SysEx: Korg Mode Switch to $modeName", bytesToHex(sysex))
        } catch (e: Exception) {
            Log.e("KorgMidiService", "Error sending Mode Change SysEx", e)
        }
    }

    fun requestCurrentSoundInfo(channel: Int = 0) {
        val port = inputPort ?: return
        try {
            val ch = (0x30 or (channel and 0x0F)).toByte()

            val modeReq = byteArrayOf(0xF0.toByte(), 0x42.toByte(), ch, 0x00, 0x01, 0x15, 0x12.toByte(), 0xF7.toByte())
            port.send(modeReq, 0, modeReq.size)

            val progReq = byteArrayOf(0xF0.toByte(), 0x42.toByte(), ch, 0x00, 0x01, 0x15, 0x10.toByte(), 0xF7.toByte())
            port.send(progReq, 0, progReq.size)

            val currentReq = byteArrayOf(0xF0.toByte(), 0x42.toByte(), ch, 0x00, 0x01, 0x15, 0x1C.toByte(), 0xF7.toByte())
            port.send(currentReq, 0, currentReq.size)

            val idReq = byteArrayOf(0xF0.toByte(), 0x7E.toByte(), 0x7F.toByte(), 0x06.toByte(), 0x01.toByte(), 0xF7.toByte())
            port.send(idReq, 0, idReq.size)

            logTraffic(MidiTrafficLog.Direction.OUT, "TX SysEx: Sound & Parameter Info Requests Sent to Korg Hardware", bytesToHex(modeReq))
        } catch (e: Exception) {
            Log.e("KorgMidiService", "Error requesting sound info SysEx", e)
        }
    }

    fun sendSysexHex(hexString: String) {
        val port = inputPort ?: return
        try {
            val cleanHex = hexString.replace(" ", "").replace("0x", "", ignoreCase = true)
            if (cleanHex.length % 2 != 0 || cleanHex.isEmpty()) return
            val byteArray = ByteArray(cleanHex.length / 2)
            for (i in byteArray.indices) {
                val index = i * 2
                byteArray[i] = cleanHex.substring(index, index + 2).toInt(16).toByte()
            }
            port.send(byteArray, 0, byteArray.size)
            logTraffic(MidiTrafficLog.Direction.OUT, "TX Custom SysEx (${byteArray.size} bytes)", bytesToHex(byteArray))
        } catch (e: Exception) {
            Log.e("KorgMidiService", "Error sending custom SysEx", e)
        }
    }

    fun updatePatchStateLocally(msb: Int, lsb: Int, program: Int, mode: String, customName: String? = null) {
        currentMsb = msb
        currentLsb = lsb
        currentMode = mode
        _currentPatchInfo.value = KorgPatchInfo(msb, lsb, program, mode, customName)
    }

    // --- INCOMING MIDI PARSER ---

    private fun parseIncomingMidiBytes(msg: ByteArray, offset: Int, count: Int) {
        var i = offset
        val end = offset + count

        while (i < end) {
            val b = msg[i].toInt() and 0xFF

            if (inSysex) {
                sysexAccumulator.write(b)
                if (b == 0xF7) {
                    inSysex = false
                    val sysexBytes = sysexAccumulator.toByteArray()
                    processReceivedSysex(sysexBytes)
                    sysexAccumulator.reset()
                }
                i++
                continue
            }

            if (b == 0xF0) {
                inSysex = true
                sysexAccumulator.reset()
                sysexAccumulator.write(b)
                i++
                continue
            }

            if (b >= 0x80) { // Status byte
                val type = b and 0xF0
                val ch = b and 0x0F
                if (type == 0xB0) { // Control Change
                    if (i + 2 < end) {
                        val cc = msg[i + 1].toInt() and 0xFF
                        val value = msg[i + 2].toInt() and 0xFF
                        if (cc == 0) currentMsb = value
                        else if (cc == 32) currentLsb = value
                        logTraffic(MidiTrafficLog.Direction.IN, "RX: CC $cc = $value (Ch ${ch + 1})", "")
                        _incomingMidiEvent.value = IncomingMidiInputEvent(ch, MidiEventType.CONTROL_CHANGE, cc, value)

                        // Direct forward CC / Modulation to MIDI Output
                        val port = inputPort
                        if (port != null) {
                            try {
                                val ccBuffer = byteArrayOf((0xB0 or ch).toByte(), cc.toByte(), value.toByte())
                                port.send(ccBuffer, 0, 3)
                                if (cc == 1) {
                                    logTraffic(MidiTrafficLog.Direction.OUT, "Direct Forward Modulation (CC 1=$value, Ch ${ch + 1})", bytesToHex(ccBuffer))
                                }
                            } catch (e: Exception) {
                                Log.e("KorgMidiService", "Error forwarding CC", e)
                            }
                        }

                        i += 3
                    } else break
                } else if (type == 0xC0) { // Program Change
                    if (i + 1 < end) {
                        val pc = msg[i + 1].toInt() and 0xFF
                        val prevName = _currentPatchInfo.value?.customName
                        _currentPatchInfo.value = KorgPatchInfo(currentMsb, currentLsb, pc, currentMode, prevName)
                        logTraffic(MidiTrafficLog.Direction.IN, "RX: Program Change $pc (MSB $currentMsb, LSB $currentLsb)", "")
                        _incomingMidiEvent.value = IncomingMidiInputEvent(ch, MidiEventType.PROGRAM_CHANGE, pc, 0)
                        i += 2
                        requestCurrentSoundInfo(0)
                    } else break
                } else if (type == 0x80 || type == 0x90) {
                    val note = msg.getOrNull(i + 1)?.toInt()?.and(0xFF) ?: 0
                    val vel = msg.getOrNull(i + 2)?.toInt()?.and(0xFF) ?: 0
                    val isNoteOn = (type == 0x90 && vel > 0)
                    val eventType = if (isNoteOn) MidiEventType.NOTE_ON else MidiEventType.NOTE_OFF
                    val action = if (isNoteOn) "Note On" else "Note Off"
                    logTraffic(MidiTrafficLog.Direction.IN, "RX: $action Note $note Vel $vel (Ch ${ch + 1})", "")
                    _incomingMidiEvent.value = IncomingMidiInputEvent(ch, eventType, note, vel)
                    i += 3
                } else if (type == 0xA0 || type == 0xE0) {
                    if (type == 0xE0 && i + 2 < end) {
                        val lsb = msg[i + 1].toInt() and 0xFF
                        val msb = msg[i + 2].toInt() and 0xFF
                        val pbValue = (msb shl 7) or lsb
                        logTraffic(MidiTrafficLog.Direction.IN, "RX: Pitch Bend $pbValue (Ch ${ch + 1})", "")
                        _incomingMidiEvent.value = IncomingMidiInputEvent(ch, MidiEventType.PITCH_BEND, pbValue, 0)

                        // Direct forward Pitch Bend to MIDI Output
                        val port = inputPort
                        if (port != null) {
                            try {
                                val pbBuffer = byteArrayOf((0xE0 or ch).toByte(), lsb.toByte(), msb.toByte())
                                port.send(pbBuffer, 0, 3)
                                logTraffic(MidiTrafficLog.Direction.OUT, "Direct Forward Pitch Bend $pbValue (Ch ${ch + 1})", bytesToHex(pbBuffer))
                            } catch (e: Exception) {
                                Log.e("KorgMidiService", "Error forwarding Pitch Bend", e)
                            }
                        }

                        i += 3
                    } else {
                        i += 3
                    }
                } else if (type == 0xD0) {
                    i += 2
                } else {
                    i++
                }
            } else {
                i++
            }
        }
    }

    private fun processReceivedSysex(sysexBytes: ByteArray) {
        if (sysexBytes.size < 5) return

        if ((sysexBytes[0].toInt() and 0xFF) == 0xF0 && (sysexBytes[1].toInt() and 0xFF) == 0x42) {
            var funcId = 0
            if (sysexBytes.size > 6) {
                funcId = sysexBytes[6].toInt() and 0xFF
            }

            if (funcId == 0x42 || funcId == 0x4E || funcId == 0x40 || funcId == 0x4C || funcId == 0x68) {
                if (sysexBytes.size > 7) {
                    val mVal = sysexBytes[7].toInt() and 0xFF
                    currentMode = if (mVal == 0) "Combi" else "Prog"
                }
            }

            val extractedName = extractAsciiNameFromSysex(sysexBytes)
            if (!extractedName.isNullOrBlank()) {
                val current = _currentPatchInfo.value
                _currentPatchInfo.value = KorgPatchInfo(
                    msb = current?.msb ?: currentMsb,
                    lsb = current?.lsb ?: currentLsb,
                    program = current?.program ?: 0,
                    mode = currentMode,
                    customName = extractedName
                )
                logTraffic(MidiTrafficLog.Direction.IN, "RX Korg SysEx: Extracted Sound Name '$extractedName' [$currentMode]", bytesToHex(sysexBytes))
            }
        }
    }

    private fun extractAsciiNameFromSysex(raw: ByteArray): String? {
        if (raw.size < 8) return null

        for (offset in listOf(7, 6, 5, 4)) {
            if (raw.size > offset + 4) {
                val unpacked = unpackKorg7BitPayload(raw, startOffset = offset, endOffset = raw.size - 1)
                if (unpacked.size >= 16) {
                    val nameBytes = unpacked.copyOfRange(0, 16)
                    val candidate = String(nameBytes, Charsets.US_ASCII).trim()
                    if (isValidSoundName(candidate) && candidate.length >= 2) {
                        return candidate
                    }
                }
                val scanned = findPrintableAsciiString(unpacked)
                if (!scanned.isNullOrBlank()) return scanned
            }
        }

        return findPrintableAsciiString(raw)
    }

    private fun findPrintableAsciiString(bytes: ByteArray): String? {
        var start = -1
        var len = 0
        var bestString: String? = null

        for (idx in bytes.indices) {
            val b = bytes[idx].toInt() and 0xFF
            if (b in 32..126) {
                if (start == -1) start = idx
                len++
            } else {
                if (len >= 2 && start != -1) {
                    val str = String(bytes, start, len, Charsets.US_ASCII).trim()
                    if (isValidSoundName(str)) {
                        bestString = str
                        break
                    }
                }
                start = -1
                len = 0
            }
        }
        if (bestString == null && len >= 2 && start != -1) {
            val str = String(bytes, start, len, Charsets.US_ASCII).trim()
            if (isValidSoundName(str)) {
                bestString = str
            }
        }
        return bestString
    }

    private fun isValidSoundName(s: String): Boolean {
        if (s.length < 2) return false
        if (s.all { !it.isLetterOrDigit() && it != ' ' && it != '-' && it != '+' && it != '/' && it != '.' }) return false
        val uppercase = s.uppercase(Locale.US)
        if (uppercase.contains("KORG") || uppercase.contains("SYSEX") || uppercase.contains("HEADER")) return false
        return true
    }

    private fun unpackKorg7BitPayload(raw: ByteArray, startOffset: Int, endOffset: Int): ByteArray {
        val out = ByteArrayOutputStream()
        var i = startOffset
        while (i < endOffset) {
            val controlByte = raw[i].toInt() and 0xFF
            i++
            for (bit in 0..6) {
                if (i >= endOffset) break
                val dataByte = raw[i].toInt() and 0xFF
                i++
                val msb = (controlByte shr bit) and 0x01
                val fullByte = (msb shl 7) or dataByte
                out.write(fullByte)
            }
        }
        return out.toByteArray()
    }

    private fun logTraffic(direction: MidiTrafficLog.Direction, summary: String, hexDump: String) {
        val timestamp = timeFormatter.format(Date())
        val newLog = MidiTrafficLog(timestamp = timestamp, direction = direction, summary = summary, hexDump = hexDump)

        val currentList = _trafficLogs.value.toMutableList()
        if (currentList.size >= 100) {
            currentList.removeAt(0)
        }
        currentList.add(newLog)
        _trafficLogs.value = currentList
    }

    fun clearTrafficLogs() {
        _trafficLogs.value = emptyList()
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val sb = StringBuilder()
        for (b in bytes) {
            sb.append(String.format("%02X ", b))
        }
        return sb.toString().trim()
    }

    override fun onDestroy() {
        super.onDestroy()
        disconnectDevice("Service destroyed")
        try {
            midiManager.unregisterDeviceCallback(deviceCallback)
        } catch (e: Exception) {
            // Ignore
        }
    }
}
