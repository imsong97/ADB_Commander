package com.ch0pp4.adbcommander.executor

import com.ch0pp4.adbcommander.presentation.model.AdbResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class JvmAdbExecutor(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : AdbExecutor {

    override suspend fun execute(rawCommand: String): AdbResult = withContext(context = dispatcher) {
        if (rawCommand.isBlank()) return@withContext AdbResult.Empty

        var process: Process? = null
        try {
            val parts = rawCommand.trim().split(regex = "\\s+".toRegex())
            process = ProcessBuilder(parts)
                .redirectErrorStream(true)
                .start()

            // prevent deadlock
            val outputDeferred = async {
                process.inputStream.bufferedReader().readText()
            }
            val completed = process.waitFor(30L, TimeUnit.SECONDS)

            if (!completed) {
                process.destroyForcibly()
                outputDeferred.cancel()
                return@withContext AdbResult.Error("Command timed out (30s)")
            }

            val output = outputDeferred.await()
            if (output.isBlank()) AdbResult.Empty else AdbResult.Success(output)
        } catch (e: Exception) {
            process?.destroyForcibly()
            AdbResult.Error(message = e.message ?: "JvmAdbExecutor - Unknown error")
        }
    }
}
