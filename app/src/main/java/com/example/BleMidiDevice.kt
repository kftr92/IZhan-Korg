package com.example

import android.bluetooth.BluetoothDevice

data class BleMidiDevice(
    val device: BluetoothDevice,
    val name: String,
    val address: String,
    val rssi: Int = 0,
    val isBonded: Boolean = false,
    val isConnected: Boolean = false
)
