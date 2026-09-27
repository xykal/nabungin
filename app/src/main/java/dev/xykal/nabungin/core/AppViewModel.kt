package dev.xykal.nabungin.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.xykal.nabungin.NabunginApp

/** Bikin ViewModel tanpa framework DI berat: container diambil dari Application. */
@Composable
inline fun <reified VM : ViewModel> appViewModel(noinline create: (AppContainer) -> VM): VM {
    val container = (LocalContext.current.applicationContext as NabunginApp).container
    val factory = remember(container) {
        viewModelFactory { initializer<VM> { create(container) } }
    }
    return viewModel(factory = factory)
}
