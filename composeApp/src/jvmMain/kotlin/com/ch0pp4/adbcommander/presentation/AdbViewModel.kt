package com.ch0pp4.adbcommander.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ch0pp4.adbcommander.data.CommandRepository
import com.ch0pp4.adbcommander.executor.AdbExecutor
import com.ch0pp4.adbcommander.presentation.model.AdbUiState
import com.ch0pp4.adbcommander.presentation.model.SavedCommandUiModel
import com.ch0pp4.adbcommander.presentation.model.toDisplayString
import com.ch0pp4.adbcommander.presentation.model.toPresentation
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AdbViewModel(
    private val commandRepository: CommandRepository,
    private val executor: AdbExecutor,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdbUiState())
    val uiState: StateFlow<AdbUiState> = _uiState.asStateFlow()

    init {
        loadSavedItems()
    }

    fun onCommandChange(value: String) {
        _uiState.update { it.copy(command = value) }
    }

    fun onRun() {
        val command = _uiState.value.command
        if (command.isBlank()) return
        _uiState.update { it.copy(executionResult = "") }
        viewModelScope.launch {
            val result = executor.execute(command).toDisplayString()
            _uiState.update { it.copy(executionResult = result) }
        }
    }

    fun onReset() {
        _uiState.update { it.copy(command = "", executionResult = "") }
    }

    fun saveCommand(title: String) {
        val command = _uiState.value.command
        if (title.isBlank() || command.isBlank()) return
        viewModelScope.launch {
            try {
                commandRepository.saveADBCommand(title = title, command = command)
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
        _uiState.update { it.copy(command = item.command, executionResult = "") }
    }

    private fun loadSavedItems() {
        viewModelScope.launch {
            val items = commandRepository.getByTab("COMMAND").map { it.toPresentation() }
            _uiState.update { it.copy(savedItems = items) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }
}
