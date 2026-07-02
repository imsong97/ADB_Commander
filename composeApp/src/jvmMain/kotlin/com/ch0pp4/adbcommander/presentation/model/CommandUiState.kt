package com.ch0pp4.adbcommander.presentation.model

data class CommandUiState(
    val commandType: IntentCommandType = IntentCommandType.BROADCAST,
    val primaryValue: String = "",
    val extras: List<BroadcastExtraModel> = emptyList(),
    val completedCommand: String = "",
    val executionResult: String = "",
    val isLoading: Boolean = false,
    val savedItems: List<SavedCommandUiModel> = emptyList(),
    val selectedItemId: Int? = null,
    val selectedTitle: String? = null,
    val originalCompletedCommand: String? = null,
    val isModified: Boolean = false,
)
