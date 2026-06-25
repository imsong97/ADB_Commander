package com.ch0pp4.adbcommander.presentation.model

sealed class ExportResult {
    data object Success : ExportResult()
    data class Failure(val message: String) : ExportResult()
}