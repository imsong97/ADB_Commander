package com.ch0pp4.adbcommander.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ch0pp4.adbcommander.data.CommandRepository
import com.ch0pp4.adbcommander.presentation.model.ExportResult
import com.ch0pp4.adbcommander.presentation.model.UserCollectionUiModel
import com.ch0pp4.adbcommander.presentation.model.toPresentation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class CollectionViewModel(private val repository: CommandRepository) : ViewModel() {

    private val _collections = MutableStateFlow(listOf<UserCollectionUiModel>())
    val collections: StateFlow<List<UserCollectionUiModel>> = _collections

    private val _exportResult = MutableSharedFlow<ExportResult>()
    val exportResult: SharedFlow<ExportResult> = _exportResult

    init {
        loadCollections()
    }

    private fun loadCollections() {
        viewModelScope.launch {
            _collections.value = repository.getAllCollections().map { it.toPresentation() }
        }
    }

    fun createCollection(name: String) {
        viewModelScope.launch {
            repository.saveCollection(name)
            loadCollections()
        }
    }

    fun deleteCollection(id: Int) {
        viewModelScope.launch {
            repository.deleteCollection(id)
            loadCollections()
        }
    }

    fun reorderCollections(orderedIds: List<Int>) {
        viewModelScope.launch {
            repository.reorderCollections(orderedIds)
            loadCollections()
        }
    }

    fun renameCollection(id: Int, newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            repository.renameCollection(id, newName)
            loadCollections()
        }
    }

    fun exportToFile(file: File) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    file.writeText(buildExportText())
                    ExportResult.Success
                } catch (e: Exception) {
                    e.printStackTrace()
                    ExportResult.Failure(e.message ?: "")
                }

            }.also {
                _exportResult.emit(it)
            }
        }
    }

    private suspend fun buildExportText(): String = buildString {
        repository.getAllCollections().forEach { collection ->
            append("[${collection.name}]\n")
            val commands = repository.getByCollection(collection.id)
            commands.forEachIndexed { i, command ->
                append("${command.title} : ${command.command}\n")
                if (i == commands.lastIndex) {
                    append("\n")
                }
            }
        }
    }
}
