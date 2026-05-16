package com.ch0pp4.adbcommander.executor

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class JvmAdbExecutor(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : AdbExecutor {

    override suspend fun execute(command: AdbCommand): AdbResult = execute(rawCommand = command.command)

    override suspend fun execute(rawCommand: String): AdbResult = withContext(context = dispatcher) {
        if (rawCommand.isBlank()) {
            AdbResult.Empty
        } else {
            try {
                val parts = rawCommand.trim().split(regex = "\\s+".toRegex())
                val process = ProcessBuilder(parts)
                    .redirectErrorStream(true)
                    .start()
                val output = process.inputStream.bufferedReader().readText()
                process.waitFor()

                if (output.isBlank()) AdbResult.Empty else AdbResult.Success(output)
            } catch (e: Exception) {
                AdbResult.Error(message = e.message ?: "JvmAdbExecutor - Unknown error")
            }
        }
    }
}
