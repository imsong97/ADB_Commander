package com.ch0pp4.adbcommander.executor

interface AdbExecutor {
    suspend fun execute(command: AdbCommand): AdbResult
    suspend fun execute(rawCommand: String): AdbResult
}