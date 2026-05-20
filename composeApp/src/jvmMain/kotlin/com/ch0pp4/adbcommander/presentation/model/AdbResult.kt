package com.ch0pp4.adbcommander.presentation.model

sealed class AdbResult {
    data object Empty : AdbResult()
    data class Success(val output: String) : AdbResult()
    data class Error(val message: String) : AdbResult()
}

fun AdbResult.toDisplayString(): String = when (this) {
    is AdbResult.Success -> output
    is AdbResult.Error -> "Error: $message"
    AdbResult.Empty -> ""
}