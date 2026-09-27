package dev.xykal.nabungin.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.xykal.nabungin.core.AppContainer
import dev.xykal.nabungin.domain.model.Snapshot
import java.time.LocalDate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(private val container: AppContainer) : ViewModel() {

    val snapshot: StateFlow<Snapshot> = container.repository.snapshot
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Snapshot())

    fun addDeposit(goalId: Long, amount: Long, note: String, day: LocalDate) {
        viewModelScope.launch {
            container.repository.addDeposit(goalId = goalId, amount = amount, day = day, note = note)
        }
    }
}

class StatsViewModel(private val container: AppContainer) : ViewModel() {

    val snapshot: StateFlow<Snapshot> = container.repository.snapshot
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Snapshot())
}
