package com.ch0pp4.adbcommander.presentation.model

import com.ch0pp4.adbcommander.data.model.BroadcastExtra as DataBroadcastExtra

data class BroadcastExtraModel(
    val id: Long = System.nanoTime(),
    val type: ExtraTypeList = ExtraTypeList.STRING,
    val extra: String = "",
    val value: String = "",
)

fun BroadcastExtraModel.toData() = DataBroadcastExtra(
    id = id,
    type = type.toData(),
    extra = extra,
    value = value,
)

fun DataBroadcastExtra.toPresentation() = BroadcastExtraModel(
    id = id,
    type = type.toPresentation(),
    extra = extra,
    value = value,
)
