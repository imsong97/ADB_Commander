package com.ch0pp4.adbcommander.presentation.model

data class AdbUiState(
    val command: String = "",
    val executionResult: String = "",
    val isLoading: Boolean = false,
    val savedItems: List<SavedCommandUiModel> = emptyList(),
    val selectedItemId: Int? = null,
    val selectedTitle: String? = null,
    val originalCommand: String? = null,
    val isModified: Boolean = false,
)
