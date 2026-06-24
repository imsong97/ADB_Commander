package com.ch0pp4.adbcommander.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ch0pp4.adbcommander.data.CommandRepository
import com.ch0pp4.adbcommander.presentation.model.UserCollectionUiModel
import com.ch0pp4.adbcommander.presentation.model.toPresentation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class CollectionViewModel(private val repository: CommandRepository) : ViewModel() {

    private val _collections = MutableStateFlow(listOf<UserCollectionUiModel>())
    val collections: StateFlow<List<UserCollectionUiModel>> = _collections

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

    fun renameCollection(id: Int, newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            repository.renameCollection(id, newName)
            loadCollections()
        }
    }

    fun exportToFile(file: File, onComplete: (success: Boolean) -> Unit) {
        viewModelScope.launch {
            val success = try {
                val text = buildExportText()
                withContext(Dispatchers.IO) { file.writeText(text) }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
            onComplete(success)
        }
    }

    private suspend fun buildExportText(): String = buildString {
        repository.getAllCollections().forEach { collection ->
            append("[${collection.name}]\n")
            repository.getByCollection(collection.id).forEach { command ->
                append("${command.title} : ${command.command}\n")
            }
        }
    }
}
