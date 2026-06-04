package com.ch0pp4.adbcommander.presentation.model

data class SendBroadcastUiState(
    val commandType: IntentCommandType = IntentCommandType.BROADCAST,
    val primaryValue: String = "",
    val extras: List<BroadcastExtraUiModel> = emptyList(),
    val completedCommand: String = "",
    val executionResult: String = "",
    val isLoading: Boolean = false,
    val savedItems: List<SavedCommandUiModel> = emptyList(),
)
