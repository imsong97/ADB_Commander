package com.ch0pp4.adbcommander.presentation.model

import com.ch0pp4.adbcommander.data.model.Collection

data class CollectionUiModel(val id: Int, val name: String)

fun Collection.toPresentation() = CollectionUiModel(id = id, name = name)
