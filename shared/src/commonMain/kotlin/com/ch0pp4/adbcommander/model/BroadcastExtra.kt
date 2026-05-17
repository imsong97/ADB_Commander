package com.ch0pp4.adbcommander.model

enum class ExtraType(val flag: String, val displayName: String) {
    STRING("--es", "String (--es)"),
    INT("--ei", "Int (--ei)"),
    LONG("--el", "Long (--el)"),
    BOOLEAN("--ez", "Boolean (--ez)"),
    FLOAT("--ef", "Float (--ef)"),
    URI("--eu", "URI (--eu)"),
}

data class BroadcastExtra(
    val id: Long = System.nanoTime(),
    val type: ExtraType = ExtraType.STRING,
    val extra: String = "",
    val value: String = "",
)
