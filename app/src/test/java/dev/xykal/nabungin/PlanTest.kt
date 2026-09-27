package dev.xykal.nabungin

import dev.xykal.nabungin.domain.SavingsMath
import dev.xykal.nabungin.domain.model.Goal
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlanTest {
    private val today = LocalDate.of(2026, 9, 27)

    @Test fun dailyRequiredRoundsUp() {
        assertEquals(334L, SavingsMath.requiredPerDay(1_000, today.plusDays(3), today))
        assertEquals(0L, SavingsMath.requiredPerDay(1_000, null, today))
        assertEquals(1_000L, SavingsMath.requiredPerDay(1_000, today.minusDays(1), today))
    }

    @Test fun planForecastNeverCountsAsSavedMoney() {
        val goal = Goal(name = "Laptop", targetAmount = 1_000_000, saved = 100_000, dailyPlan = 50_000)
        assertEquals(100_000L, goal.saved)
        assertEquals(18L, SavingsMath.pace(goal).etaDays)
        assertEquals(today.plusDays(18), SavingsMath.projectedDate(900_000, 50_000, today))
    }

    @Test fun noPlanHasNoForecast() {
        assertNull(SavingsMath.projectedDate(10_000, 0, today))
        assertNull(SavingsMath.projectedDate(0, 10_000, today))
    }

    @Test fun deadlineAndDailyPlanReportOnTrackCorrectly() {
        val onTrack = Goal(name="A", targetAmount=100_000, saved=0, dailyPlan=10_000,
            deadline=LocalDate.now().plusDays(15))
        assertTrue(SavingsMath.pace(onTrack).onTrack)
        assertFalse(SavingsMath.pace(onTrack.copy(deadline=LocalDate.now().plusDays(5))).onTrack)
    }
}
