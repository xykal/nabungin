package dev.xykal.nabungin.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.xykal.nabungin.core.AppContainer
import dev.xykal.nabungin.domain.model.Deposit
import dev.xykal.nabungin.domain.model.Goal
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(private val container: AppContainer) : ViewModel() {

    val deposits: StateFlow<List<Deposit>> = container.repository.allDeposits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val goals: StateFlow<List<Goal>> = container.repository.goals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun delete(id: Long) {
        viewModelScope.launch { container.repository.deleteDeposit(id) }
    }
}
