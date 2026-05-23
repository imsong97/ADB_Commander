package com.ch0pp4.adbcommander.presentation.model

import com.ch0pp4.adbcommander.data.model.BroadcastExtra as DataBroadcastExtra

data class BroadcastExtraUiModel(
    val id: Long = System.nanoTime(),
    val type: ExtraTypeList = ExtraTypeList.STRING,
    val extra: String = "",
    val value: String = "",
)

fun BroadcastExtraUiModel.toData() = DataBroadcastExtra(
    id = id,
    type = type.toData(),
    extra = extra,
    value = value,
)

fun DataBroadcastExtra.toPresentation() = BroadcastExtraUiModel(
    id = id,
    type = type.toPresentation(),
    extra = extra,
    value = value,
)
