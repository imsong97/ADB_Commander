package com.ch0pp4.adbcommander.executor

import com.ch0pp4.adbcommander.presentation.model.AdbResult

interface AdbExecutor {
//    suspend fun execute(command: AdbCommand): AdbResult
    suspend fun execute(rawCommand: String): AdbResult
}