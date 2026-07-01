package com.ch0pp4.adbcommander.di

import com.ch0pp4.adbcommander.data.CommandRepository
import com.ch0pp4.adbcommander.data.CommandRepositoryImpl
import com.ch0pp4.adbcommander.local.LocalDataSourceImpl
import com.ch0pp4.adbcommander.data.datasource.LocalDataSource
import com.ch0pp4.adbcommander.executor.AdbExecutor
import com.ch0pp4.adbcommander.executor.JvmAdbExecutor
import com.ch0pp4.adbcommander.preference.AppPreferences
import com.ch0pp4.adbcommander.presentation.CollectionItemViewModel
import com.ch0pp4.adbcommander.presentation.CollectionViewModel

class AppContainer {

    val appPreferences: AppPreferences = AppPreferences()

    val adbExecutor: AdbExecutor = JvmAdbExecutor()

    val localDataSource: LocalDataSource = LocalDataSourceImpl()

    val commandRepository: CommandRepository = CommandRepositoryImpl(localDataSource)

    val collectionViewModel: CollectionViewModel = CollectionViewModel(commandRepository)

    val collectionCommandViewModel: CollectionItemViewModel = CollectionItemViewModel(commandRepository, adbExecutor)
}
