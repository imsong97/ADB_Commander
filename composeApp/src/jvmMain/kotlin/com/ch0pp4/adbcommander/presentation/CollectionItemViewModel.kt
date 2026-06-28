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
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CollectionItemViewModel(
    private val commandRepository: CommandRepository,
    private val executor: AdbExecutor,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SendBroadcastUiState(commandType = IntentCommandType.ADB))
    val uiState: StateFlow<SendBroadcastUiState> = _uiState.asStateFlow()

    private val _collectionItems = MutableStateFlow<Map<Int, List<SavedCommandUiModel>>>(emptyMap())
    val collectionItems: StateFlow<Map<Int, List<SavedCommandUiModel>>> = _collectionItems.asStateFlow()

    private var currentCollectionId: Int? = null

    fun setCollection(collectionId: Int) {
        if (currentCollectionId == collectionId) return
        currentCollectionId = collectionId
        _uiState.update { SendBroadcastUiState(commandType = IntentCommandType.ADB) }
        loadSavedItems()
    }

    fun loadCollectionItems(collectionId: Int) {
        viewModelScope.launch {
            val items = commandRepository.getByCollection(collectionId).map { it.toPresentation() }
            _collectionItems.update { it + (collectionId to items) }
        }
    }

    fun removeCollectionItems(collectionId: Int) {
        _collectionItems.update { it - collectionId }
    }

    fun onCommandTypeChange(type: IntentCommandType) {
        _uiState.update { state ->
            val new = state.copy(commandType = type, primaryValue = "", extras = emptyList(), executionResult = "")
            val withCommand = new.copy(completedCommand = buildCommand(new))
            withCommand.copy(
                isModified = if (state.selectedTitle != null) withCommand.completedCommand != state.originalCompletedCommand else false,
            )
        }
    }

    fun onPrimaryValueChange(value: String) {
        _uiState.update { state ->
            val new = state.copy(primaryValue = value)
            val withCommand = new.copy(completedCommand = buildCommand(new))
            withCommand.copy(
                isModified = if (state.selectedTitle != null) withCommand.completedCommand != state.originalCompletedCommand else value.isNotBlank() || state.extras.isNotEmpty(),
            )
        }
    }

    fun onRun() {
        val command = _uiState.value.completedCommand
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

    fun saveCommand(title: String) {
        val collectionId = currentCollectionId ?: return
        val state = _uiState.value
        if (title.isBlank()) return
        viewModelScope.launch {
            try {
                val newId = commandRepository.saveCollectionCommand(
                    collectionId = collectionId,
                    title = title,
                    command = state.completedCommand,
                    intentType = state.commandType.toData(),
                    extras = state.extras.map { it.toData() },
                )
                loadSavedItems()
                _uiState.update { it.copy(selectedItemId = newId, selectedTitle = title, originalCompletedCommand = state.completedCommand, isModified = false) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateCommand(id: Int, title: String) {
        val state = _uiState.value
        viewModelScope.launch {
            try {
                commandRepository.updateCollectionCommand(
                    id = id,
                    title = title,
                    command = state.completedCommand,
                    intentType = state.commandType.toData(),
                    extras = state.extras.map { it.toData() },
                )
                loadSavedItems()
                _uiState.update { it.copy(selectedTitle = title, originalCompletedCommand = state.completedCommand, isModified = false) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun selectItem(item: SavedCommandUiModel) {
        val (commandType, primaryValue) = if (item.intentType == IntentCommandType.ADB) {
            IntentCommandType.ADB to item.command
        } else {
            val prefix = "adb shell ${item.intentType.adbCommand} ${item.intentType.actionFlag} "
            item.intentType to item.command.removePrefix(prefix).substringBefore(" --").trim()
        }
        _uiState.update { state ->
            val new = state.copy(
                commandType = commandType,
                primaryValue = primaryValue,
                extras = item.extras,
                executionResult = "",
                selectedItemId = item.id,
                selectedTitle = item.title,
                originalCompletedCommand = item.command,
                isModified = false,
            )
            new.copy(completedCommand = buildCommand(new))
        }
    }

    fun deleteItem(item: SavedCommandUiModel) {
        viewModelScope.launch {
            commandRepository.deleteCollectionCommand(item.id)
            if (_uiState.value.selectedItemId == item.id) {
                onReset()
            }
            loadSavedItems()
        }
    }

    fun renameItem(item: SavedCommandUiModel, newTitle: String) {
        if (newTitle.isBlank()) return
        viewModelScope.launch {
            commandRepository.renameCollectionCommand(item.id, newTitle)
            if (_uiState.value.selectedItemId == item.id) {
                _uiState.update { it.copy(selectedTitle = newTitle) }
            }
            loadSavedItems()
        }
    }

    fun onExtraAdd() {
        _uiState.update { state ->
            val new = state.copy(extras = state.extras + BroadcastExtraUiModel())
            val withCommand = new.copy(completedCommand = buildCommand(new))
            withCommand.copy(
                isModified = if (state.selectedTitle != null) withCommand.completedCommand != state.originalCompletedCommand else true,
            )
        }
    }

    fun onExtrasUpdate(index: Int, extra: BroadcastExtraUiModel) {
        _uiState.update { state ->
            val newExtras = state.extras.toMutableList().also { it[index] = extra }
            val new = state.copy(extras = newExtras)
            val withCommand = new.copy(completedCommand = buildCommand(new))
            withCommand.copy(
                isModified = if (state.selectedTitle != null) withCommand.completedCommand != state.originalCompletedCommand else true,
            )
        }
    }

    fun onExtraDelete(index: Int) {
        _uiState.update { state ->
            val newExtras = state.extras.toMutableList().also { it.removeAt(index) }
            val new = state.copy(extras = newExtras)
            val withCommand = new.copy(completedCommand = buildCommand(new))
            withCommand.copy(
                isModified = if (state.selectedTitle != null) withCommand.completedCommand != state.originalCompletedCommand else state.primaryValue.isNotBlank() || newExtras.isNotEmpty(),
            )
        }
    }

    fun onReset() {
        _uiState.update { state ->
            val new = state.copy(
                primaryValue = "",
                extras = emptyList(),
                executionResult = "",
                selectedItemId = null,
                selectedTitle = null,
                originalCompletedCommand = null,
                isModified = false,
            )
            new.copy(completedCommand = buildCommand(new))
        }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedItemId = null, selectedTitle = null, isModified = false) }
    }

    private fun loadSavedItems() {
        val collectionId = currentCollectionId ?: return
        viewModelScope.launch {
            val items = commandRepository.getByCollection(collectionId).map { it.toPresentation() }
            _uiState.update { it.copy(savedItems = items) }
            _collectionItems.update { it + (collectionId to items) }
        }
    }

    private fun buildCommand(state: SendBroadcastUiState): String {
        if (state.primaryValue.isBlank()) return ""
        return when (state.commandType) {
            IntentCommandType.ADB -> state.primaryValue
            else -> {
                val sb = StringBuilder("adb shell ${state.commandType.adbCommand} ${state.commandType.actionFlag} ${state.primaryValue}")
                state.extras.forEach { extra ->
                    if (extra.extra.isNotBlank()) {
                        sb.append(" ${extra.type.flag} ${extra.extra} ${extra.value}")
                    }
                }
                sb.toString()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}
