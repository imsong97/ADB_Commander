package com.ch0pp4.adbcommander.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ch0pp4.adbcommander.data.CommandRepository
import com.ch0pp4.adbcommander.executor.AdbExecutor
import com.ch0pp4.adbcommander.presentation.model.BroadcastExtraUiModel
import com.ch0pp4.adbcommander.presentation.model.IntentCommandType
import com.ch0pp4.adbcommander.presentation.model.SavedCommandUiModel
import com.ch0pp4.adbcommander.presentation.model.SendBroadcastUiState
import com.ch0pp4.adbcommander.presentation.model.toData
import com.ch0pp4.adbcommander.presentation.model.toDisplayString
import com.ch0pp4.adbcommander.presentation.model.toPresentation
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

class SendBroadcastViewModel(
    private val commandRepository: CommandRepository,
    private val executor: AdbExecutor,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SendBroadcastUiState())
    val uiState: StateFlow<SendBroadcastUiState> = _uiState.asStateFlow()

    init {
        loadSavedItems()
    }

    fun onCommandTypeChange(type: IntentCommandType) {
        _uiState.update { state ->
            val new = state.copy(commandType = type, primaryValue = "", extras = emptyList(), executionResult = "")
            new.copy(completedCommand = buildCommand(new))
        }
    }

    fun onPrimaryValueChange(value: String) {
        _uiState.update { state ->
            val new = state.copy(primaryValue = value)
            new.copy(completedCommand = buildCommand(new))
        }
    }

    fun onExtraAdd() {
        _uiState.update { state ->
            val new = state.copy(extras = state.extras + BroadcastExtraUiModel())
            new.copy(completedCommand = buildCommand(new))
        }
    }

    fun onExtrasUpdate(index: Int, extra: BroadcastExtraUiModel) {
        _uiState.update { state ->
            val newExtras = state.extras.toMutableList().also { it[index] = extra }
            val new = state.copy(extras = newExtras)
            new.copy(completedCommand = buildCommand(new))
        }
    }

    fun onExtraDelete(index: Int) {
        _uiState.update { state ->
            val newExtras = state.extras.toMutableList().also { it.removeAt(index) }
            val new = state.copy(extras = newExtras)
            new.copy(completedCommand = buildCommand(new))
        }
    }

    fun onRun() {
        val command = _uiState.value.completedCommand
        if (command.isBlank()) return
        viewModelScope.launch {
            val result = executor.execute(command).toDisplayString()
            _uiState.update { it.copy(executionResult = result) }
        }
    }

    fun onReset() {
        _uiState.update { state ->
            val new = state.copy(primaryValue = "", extras = emptyList(), executionResult = "")
            new.copy(completedCommand = buildCommand(new))
        }
    }

    fun onCopy() {
        Toolkit.getDefaultToolkit().systemClipboard
            .setContents(StringSelection(_uiState.value.completedCommand), null)
    }

    fun saveCommand(title: String) {
        val state = _uiState.value
        if (title.isBlank()) return
        viewModelScope.launch {
            try {
                commandRepository.saveBroadcastCommand(
                    title = title,
                    command = state.completedCommand,
                    intentType = state.commandType.toData(),
                    extras = state.extras.map { it.toData() },
                )
                loadSavedItems()
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(executionResult = "[Save Error] ${e::class.simpleName}: ${e.message}") }
            }
        }
    }

    fun deleteItem(item: SavedCommandUiModel) {
        viewModelScope.launch {
            commandRepository.deleteById(item.id)
            loadSavedItems()
        }
    }

    fun selectItem(item: SavedCommandUiModel) {
        val prefix = "adb shell ${item.intentType.adbCommand} ${item.intentType.actionFlag} "
        val primaryValue = item.command.removePrefix(prefix).substringBefore(" --").trim()
        _uiState.update { state ->
            val new = state.copy(
                commandType = item.intentType,
                primaryValue = primaryValue,
                extras = item.extras,
                executionResult = "",
            )
            new.copy(completedCommand = buildCommand(new))
        }
    }

    private fun loadSavedItems() {
        viewModelScope.launch {
            val items = commandRepository.getByTab("BROADCAST").map { it.toPresentation() }
            _uiState.update { it.copy(savedItems = items) }
        }
    }

    private fun buildCommand(state: SendBroadcastUiState): String {
        if (state.primaryValue.isBlank()) return ""
        val sb = StringBuilder("adb shell ${state.commandType.adbCommand} ${state.commandType.actionFlag} ${state.primaryValue}")
        state.extras.forEach { extra ->
            if (extra.extra.isNotBlank()) {
                sb.append(" ${extra.type.flag} ${extra.extra} ${extra.value}")
            }
        }
        return sb.toString()
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }
}
