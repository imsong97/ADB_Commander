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

    fun getAdbViewModel(viewModelStore: ViewModelStore): AdbViewModel = VMProvider(
        store = viewModelStore,
        instance = AdbViewModel(commandRepository, adbExecutor)
    )

    fun getSendBroadcastViewModel(viewModelStore: ViewModelStore): SendBroadcastViewModel = VMProvider(
        store = viewModelStore,
        instance = SendBroadcastViewModel(commandRepository, adbExecutor)
    )
}

@Suppress("UNCHECKED_CAST")
private fun <T : ViewModel> VMProvider(
    store: ViewModelStore,
    instance: ViewModel
): T {
    val owner = object : ViewModelStoreOwner {
        override val viewModelStore: ViewModelStore = store
    }
    val factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            return if (modelClass.isInstance(instance)) {
                instance as T
            } else {
                throw IllegalArgumentException("Unknown ViewModel: ${modelClass.qualifiedName}")
            }
        }
    }
    return ViewModelProvider.create(owner, factory)[(instance as T)::class]
}