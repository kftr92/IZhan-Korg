package com.example

enum class MidiEventType {
    NOTE_ON,
    NOTE_OFF,
    PROGRAM_CHANGE,
    CONTROL_CHANGE,
    PITCH_BEND
}

data class IncomingMidiInputEvent(
    val channel: Int,
    val type: MidiEventType,
    val noteOrPc: Int,
    val velocityOrVal: Int,
    val timestamp: Long = System.currentTimeMillis()
)
