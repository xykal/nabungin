package dev.xykal.nabungin.ui.goal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.xykal.nabungin.core.AppContainer
import dev.xykal.nabungin.domain.model.AutoRule
import dev.xykal.nabungin.domain.model.Deposit
import dev.xykal.nabungin.domain.model.Goal
import java.time.LocalDate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GoalDetailViewModel(
    private val container: AppContainer,
    private val goalId: Long,
) : ViewModel() {

    val goal: StateFlow<Goal?> = container.repository.goal(goalId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val deposits: StateFlow<List<Deposit>> = container.repository.depositsOf(goalId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val rule: StateFlow<AutoRule?> = container.repository.ruleOf(goalId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun addDeposit(amount: Long, note: String, day: LocalDate) {
        viewModelScope.launch {
            container.repository.addDeposit(goalId = goalId, amount = amount, day = day, note = note)
        }
    }

    fun deleteDeposit(id: Long) {
        viewModelScope.launch { container.repository.deleteDeposit(id) }
    }

    fun saveRule(rule: AutoRule) {
        viewModelScope.launch { container.repository.upsertRule(rule) }
    }

    fun deleteRule() {
        viewModelScope.launch { container.repository.deleteRule(goalId) }
    }

    fun deleteGoal(onDone: () -> Unit) {
        viewModelScope.launch {
            container.repository.deleteGoal(goalId)
            onDone()
        }
    }
}

class GoalEditViewModel(private val container: AppContainer) : ViewModel() {

    suspend fun loadGoal(id: Long): Goal? = container.repository.goal(id).first()

    suspend fun loadRule(id: Long): AutoRule? = container.repository.ruleOf(id).first()

    fun save(goal: Goal, rule: AutoRule?, onDone: (Long) -> Unit) {
        viewModelScope.launch {
            val id = container.repository.upsertGoal(goal)
            if (rule != null && rule.amount > 0L) {
                container.repository.upsertRule(rule.copy(goalId = id))
            } else if (rule == null) {
                container.repository.deleteRule(id)
            }
            onDone(id)
        }
    }

    fun delete(id: Long, onDone: () -> Unit) {
        viewModelScope.launch {
            container.repository.deleteGoal(id)
            onDone()
        }
    }
}
