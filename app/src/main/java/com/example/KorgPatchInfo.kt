package com.example

data class KorgPatchInfo(
    val msb: Int,
    val lsb: Int,
    val program: Int,
    val mode: String = "Prog",
    val customName: String? = null
)
