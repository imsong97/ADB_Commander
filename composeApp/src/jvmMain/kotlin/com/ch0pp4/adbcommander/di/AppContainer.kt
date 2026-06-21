package com.ch0pp4.adbcommander.di

import com.ch0pp4.adbcommander.data.CommandRepository
import com.ch0pp4.adbcommander.data.CommandRepositoryImpl
import com.ch0pp4.adbcommander.local.LocalDataSourceImpl
import com.ch0pp4.adbcommander.data.datasource.LocalDataSource
import com.ch0pp4.adbcommander.executor.AdbExecutor
import com.ch0pp4.adbcommander.executor.JvmAdbExecutor
import com.ch0pp4.adbcommander.presentation.CollectionItemViewModel
import com.ch0pp4.adbcommander.presentation.CollectionViewModel
import com.ch0pp4.adbcommander.presentation.viewmodel.DesktopViewModel

class AppContainer {

    private val viewModels = mutableListOf<DesktopViewModel>()
    private fun <T : DesktopViewModel> T.register(): T = this.also { viewModels.add(it) }

    val adbExecutor: AdbExecutor by lazy { JvmAdbExecutor() }

    val localDataSource: LocalDataSource by lazy { LocalDataSourceImpl() }

    val commandRepository: CommandRepository by lazy { CommandRepositoryImpl(localDataSource) }

    val collectionViewModel: CollectionViewModel by lazy {
        CollectionViewModel(commandRepository).register()
    }

    val collectionItemViewModel: CollectionItemViewModel by lazy {
        CollectionItemViewModel(commandRepository, adbExecutor).register()
    }

    fun clear() = viewModels.forEach { it.cancel() }
}
