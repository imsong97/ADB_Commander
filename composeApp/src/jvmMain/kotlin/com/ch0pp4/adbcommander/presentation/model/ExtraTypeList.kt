package com.ch0pp4.adbcommander.presentation.model

import com.ch0pp4.adbcommander.data.model.ExtraType

enum class ExtraTypeList(val flag: String, val displayName: String) {
    STRING("--es", "String (--es)"),
    INT("--ei", "Int (--ei)"),
    LONG("--el", "Long (--el)"),
    BOOLEAN("--ez", "Boolean (--ez)"),
    FLOAT("--ef", "Float (--ef)"),
    URI("--eu", "URI (--eu)"),
}

fun ExtraTypeList.toData(): ExtraType = ExtraType.valueOf(this.name)
fun ExtraType.toPresentation(): ExtraTypeList = ExtraTypeList.valueOf(this.name)
