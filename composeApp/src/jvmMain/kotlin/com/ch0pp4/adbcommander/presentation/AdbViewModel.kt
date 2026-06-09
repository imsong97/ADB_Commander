package com.ch0pp4.adbcommander.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ch0pp4.adbcommander.data.CommandRepository
import com.ch0pp4.adbcommander.executor.AdbExecutor
import com.ch0pp4.adbcommander.presentation.model.AdbUiState
import com.ch0pp4.adbcommander.presentation.model.SavedCommandUiModel
import com.ch0pp4.adbcommander.presentation.model.toDisplayString
import com.ch0pp4.adbcommander.presentation.model.toPresentation
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
        _uiState.update { it.copy(
            command = value,
            isModified = if (it.selectedTitle != null) value != it.originalCommand else value.isNotBlank(),
        ) }
    }

    fun onRun() {
        val command = _uiState.value.command
        if (command.isBlank()) return
        _uiState.update { it.copy(executionResult = "", isLoading = true) }
        viewModelScope.launch {
            try {
                val result = executor.execute(command).toDisplayString()
                _uiState.update { it.copy(executionResult = result) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onReset() {
        _uiState.update { it.copy(command = "", executionResult = "", selectedItemId = null, selectedTitle = null, originalCommand = null, isModified = false) }
    }

    fun updateCommand(id: Int, title: String) {
        val command = _uiState.value.command
        viewModelScope.launch {
            try {
                commandRepository.updateCommand(id = id, title = title, command = command)
                loadSavedItems()
                _uiState.update { it.copy(selectedTitle = title, originalCommand = command, isModified = false) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
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

    fun renameItem(item: SavedCommandUiModel, newTitle: String) {
        if (newTitle.isBlank()) return
        viewModelScope.launch {
            commandRepository.renameCommand(item.id, newTitle)
            loadSavedItems()
        }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedItemId = null) }
    }

    fun selectItem(item: SavedCommandUiModel) {
        _uiState.update { it.copy(command = item.command, executionResult = "", selectedItemId = item.id, selectedTitle = item.title, originalCommand = item.command, isModified = false) }
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
