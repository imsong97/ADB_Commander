package com.ch0pp4.adbcommander.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import com.ch0pp4.adbcommander.data.CommandRepository
import com.ch0pp4.adbcommander.data.CommandRepositoryImpl
import com.ch0pp4.adbcommander.local.LocalDataSourceImpl
import com.ch0pp4.adbcommander.data.datasource.LocalDataSource
import com.ch0pp4.adbcommander.executor.AdbExecutor
import com.ch0pp4.adbcommander.executor.JvmAdbExecutor
import com.ch0pp4.adbcommander.presentation.AdbViewModel
import com.ch0pp4.adbcommander.presentation.SendBroadcastViewModel
import kotlin.reflect.KClass

class AppContainer {

    val adbExecutor: AdbExecutor by lazy { JvmAdbExecutor() }

    val localDataSource: LocalDataSource by lazy {
        LocalDataSourceImpl()
    }

    val commandRepository: CommandRepository by lazy {
        CommandRepositoryImpl(localDataSource)
    }

    val adbViewModel: AdbViewModel by lazy {
        AdbViewModel(commandRepository, adbExecutor)
    }

    val sendBroadcastViewModel: SendBroadcastViewModel by lazy {
        SendBroadcastViewModel(commandRepository, adbExecutor)
    }
}