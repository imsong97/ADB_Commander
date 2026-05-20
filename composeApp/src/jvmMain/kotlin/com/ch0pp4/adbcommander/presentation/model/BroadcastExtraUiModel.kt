package com.ch0pp4.adbcommander.presentation.model

import com.ch0pp4.adbcommander.model.BroadcastExtra as DataBroadcastExtra
import com.ch0pp4.adbcommander.model.ExtraType as DataExtraType

typealias ExtraType = DataExtraType

data class BroadcastExtraUiModel(
    val id: Long = System.nanoTime(),
    val type: ExtraType = ExtraType.STRING,
    val extra: String = "",
    val value: String = "",
)

fun BroadcastExtraUiModel.toData() = DataBroadcastExtra(
    id = id,
    type = type,
    extra = extra,
    value = value,
)

fun DataBroadcastExtra.toPresentation() = BroadcastExtraUiModel(
    id = id,
    type = type,
    extra = extra,
    value = value,
)
