package com.example.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.ReminderStatus
import com.example.domain.model.AdherenceStats
import com.example.domain.model.Medicine
import com.example.domain.model.ScheduledReminderItem
import com.example.domain.model.UserProfile
import com.example.domain.repository.MedicationRepository
import com.example.domain.repository.UserRepository
import com.example.domain.usecase.ProcessDoseStateTransitionUseCase
import com.example.notifications.NotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val activeProfile: UserProfile? = null,
    val todayReminders: List<ScheduledReminderItem> = emptyList(),
    val lowStockMedicines: List<Medicine> = emptyList(),
    val adherenceStats: AdherenceStats = AdherenceStats(0, 0, 0, 100)
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    userRepository: UserRepository,
    private val medicationRepository: MedicationRepository,
    private val processDoseUseCase: ProcessDoseStateTransitionUseCase,
    private val notificationHelper: NotificationHelper
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<DashboardUiState> = userRepository.observeActiveProfile()
        .flatMapLatest { profile ->
            if (profile == null) {
                flowOf(DashboardUiState())
            } else {
                combine(
                    medicationRepository.observeTodayReminders(profile.id),
                    medicationRepository.observeLowInventoryMedicines(profile.id)
                ) { reminders, lowStock ->
                    val completedTaken = reminders.count {
                        it.status == ReminderStatus.COMPLETED && it.responseType == ReminderStatus.TAKEN
                    }
                    val skipped = reminders.count {
                        it.status == ReminderStatus.COMPLETED && it.responseType == ReminderStatus.SKIPPED
                    }
                    val totalResolved = completedTaken + skipped
                    val adherence = if (totalResolved == 0) 100 else ((completedTaken * 100) / totalResolved)
                    DashboardUiState(
                        activeProfile = profile,
                        todayReminders = reminders,
                        lowStockMedicines = lowStock,
                        adherenceStats = AdherenceStats(
                            totalScheduledToday = reminders.size,
                            completedTakenToday = completedTaken,
                            skippedToday = skipped,
                            adherencePercent = adherence
                        )
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardUiState()
        )

    fun markDoseTaken(reminderId: Long) {
        viewModelScope.launch {
            processDoseUseCase.markTaken(reminderId)
            notificationHelper.dismissNotification(reminderId)
        }
    }

    fun markDoseSkipped(reminderId: Long) {
        viewModelScope.launch {
            processDoseUseCase.markSkipped(reminderId)
            notificationHelper.dismissNotification(reminderId)
        }
    }

    fun triggerReminderNow(item: ScheduledReminderItem) {
        viewModelScope.launch {
            processDoseUseCase.triggerReminderSent(item.reminderId)
            notificationHelper.showDoseReminderNotification(
                reminderId = item.reminderId,
                medicineName = item.medicineName,
                dosage = item.dosage
            )
        }
    }
}
