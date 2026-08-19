package com.example

import android.bluetooth.BluetoothDevice
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.midi.MidiDeviceInfo
import android.media.midi.MidiManager
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class KorgMidiController(
    private val midiManager: MidiManager,
    private val context: Context? = null
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var midiService: KorgMidiService? = null
    private var isBound = false

    private val _devices = MutableStateFlow<List<MidiDeviceInfo>>(emptyList())
    val devices: StateFlow<List<MidiDeviceInfo>> = _devices.asStateFlow()

    private val _selectedInputDevice = MutableStateFlow<MidiDeviceInfo?>(null)
    val selectedInputDevice: StateFlow<MidiDeviceInfo?> = _selectedInputDevice.asStateFlow()

    private val _selectedOutputDevice = MutableStateFlow<MidiDeviceInfo?>(null)
    val selectedOutputDevice: StateFlow<MidiDeviceInfo?> = _selectedOutputDevice.asStateFlow()

    private val _selectedDevice = MutableStateFlow<MidiDeviceInfo?>(null)
    val selectedDevice: StateFlow<MidiDeviceInfo?> = _selectedDevice.asStateFlow()

    private val _currentDevicePatch = MutableStateFlow<KorgPatchInfo?>(null)
    val currentDevicePatch: StateFlow<KorgPatchInfo?> = _currentDevicePatch.asStateFlow()

    private val _connectionStatus = MutableStateFlow(KorgConnectionStatus.DISCONNECTED)
    val connectionStatus: StateFlow<KorgConnectionStatus> = _connectionStatus.asStateFlow()

    private val _statusMessage = MutableStateFlow("Initializing MIDI Service...")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    private val _trafficLogs = MutableStateFlow<List<MidiTrafficLog>>(emptyList())
    val trafficLogs: StateFlow<List<MidiTrafficLog>> = _trafficLogs.asStateFlow()

    private val _incomingMidiEvent = MutableStateFlow<IncomingMidiInputEvent?>(null)
    val incomingMidiEvent: StateFlow<IncomingMidiInputEvent?> = _incomingMidiEvent.asStateFlow()

    private val _scannedBleDevices = MutableStateFlow<List<BleMidiDevice>>(emptyList())
    val scannedBleDevices: StateFlow<List<BleMidiDevice>> = _scannedBleDevices.asStateFlow()

    private val _isScanningBle = MutableStateFlow(false)
    val isScanningBle: StateFlow<Boolean> = _isScanningBle.asStateFlow()

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val korgBinder = binder as? KorgMidiService.KorgMidiBinder
            if (korgBinder != null) {
                midiService = korgBinder.getService()
                isBound = true
                Log.d("KorgMidiController", "Connected to KorgMidiService instance")

                scope.launch {
                    midiService?.availableDevices?.collect { _devices.value = it }
                }
                scope.launch {
                    midiService?.selectedInputDeviceInfo?.collect { _selectedInputDevice.value = it }
                }
                scope.launch {
                    midiService?.selectedOutputDeviceInfo?.collect { _selectedOutputDevice.value = it }
                }
                scope.launch {
                    midiService?.selectedDeviceInfo?.collect { _selectedDevice.value = it }
                }
                scope.launch {
                    midiService?.currentPatchInfo?.collect { _currentDevicePatch.value = it }
                }
                scope.launch {
                    midiService?.connectionStatus?.collect { _connectionStatus.value = it }
                }
                scope.launch {
                    midiService?.statusMessage?.collect { _statusMessage.value = it }
                }
                scope.launch {
                    midiService?.trafficLogs?.collect { _trafficLogs.value = it }
                }
                scope.launch {
                    midiService?.incomingMidiEvent?.collect { _incomingMidiEvent.value = it }
                }
                scope.launch {
                    midiService?.scannedBleDevices?.collect { _scannedBleDevices.value = it }
                }
                scope.launch {
                    midiService?.isScanningBle?.collect { _isScanningBle.value = it }
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            midiService = null
            isBound = false
            _connectionStatus.value = KorgConnectionStatus.DISCONNECTED
            _statusMessage.value = "MIDI Service Disconnected"
            Log.d("KorgMidiController", "Disconnected from KorgMidiService")
        }
    }

    init {
        if (context != null) {
            val intent = Intent(context, KorgMidiService::class.java)
            try {
                context.startService(intent)
                context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            } catch (e: Exception) {
                Log.e("KorgMidiController", "Failed to bind KorgMidiService", e)
            }
        }
    }

    fun startBleScan() {
        midiService?.startBleScan()
    }

    fun stopBleScan() {
        midiService?.stopBleScan()
    }

    fun connectBleDevice(device: BluetoothDevice, asInput: Boolean = true, asOutput: Boolean = true) {
        midiService?.connectBleDevice(device, asInput, asOutput)
    }

    fun disconnectBleDevice(device: BluetoothDevice) {
        midiService?.disconnectBleDevice(device)
    }

    fun selectInputDevice(deviceInfo: MidiDeviceInfo?) {
        midiService?.connectInputDevice(deviceInfo)
    }

    fun selectOutputDevice(deviceInfo: MidiDeviceInfo?) {
        midiService?.connectOutputDevice(deviceInfo)
    }

    fun selectDevice(deviceInfo: MidiDeviceInfo) {
        midiService?.connectDevice(deviceInfo)
    }

    fun refreshDevices() {
        midiService?.refreshDevices()
    }

    fun updateCurrentPatch(msb: Int, lsb: Int, program: Int, mode: String, customName: String? = null) {
        _currentDevicePatch.value = KorgPatchInfo(msb, lsb, program, mode, customName)
        midiService?.updatePatchStateLocally(msb, lsb, program, mode, customName)
    }

    fun requestCurrentSoundInfo(channel: Int = 0) {
        midiService?.requestCurrentSoundInfo(channel)
    }

    fun sendPitchBend(channel: Int, value: Int) {
        midiService?.sendPitchBend(channel, value)
    }

    fun sendProgramChange(channel: Int, msb: Int, lsb: Int, program: Int) {
        midiService?.sendProgramChange(channel, msb, lsb, program)
    }

    fun sendNoteOn(channel: Int, note: Int, velocity: Int) {
        midiService?.sendNoteOn(channel, note, velocity)
    }

    fun sendNoteOff(channel: Int, note: Int) {
        midiService?.sendNoteOff(channel, note)
    }

    fun sendMasterCoarseTune(channel: Int, transpose: Int) {
        midiService?.sendMasterCoarseTune(channel, transpose)
    }

    fun sendModeChange(channel: Int, mode: Int) {
        midiService?.sendModeChange(channel, mode)
    }

    fun sendSysexHex(hexString: String) {
        midiService?.sendSysexHex(hexString)
    }

    fun clearTrafficLogs() {
        midiService?.clearTrafficLogs()
    }

    fun close() {
        if (isBound && context != null) {
            try {
                context.unbindService(serviceConnection)
            } catch (e: Exception) {
                Log.e("KorgMidiController", "Error unbinding service", e)
            }
            isBound = false
        }
    }
}
