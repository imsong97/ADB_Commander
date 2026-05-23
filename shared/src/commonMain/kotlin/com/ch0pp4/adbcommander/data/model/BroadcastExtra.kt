package com.ch0pp4.adbcommander.data.model

enum class ExtraType {
    STRING, INT, LONG, BOOLEAN, FLOAT, URI,
}

data class BroadcastExtra(
    val id: Long = System.nanoTime(),
    val type: ExtraType = ExtraType.STRING,
    val extra: String = "",
    val value: String = "",
)
