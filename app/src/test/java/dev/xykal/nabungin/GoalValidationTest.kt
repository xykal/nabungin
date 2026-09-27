package dev.xykal.nabungin

import dev.xykal.nabungin.domain.GoalValidation
import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test

class GoalValidationTest {
    private val today = LocalDate.of(2026, 9, 27)
    @Test fun errorsAreSpecificToFields() {
        val errors = GoalValidation.validate("  ", 0, today, true, 0, today)
        assertFalse(errors.isValid)
        assertNotNull(errors.name)
        assertNotNull(errors.target)
        assertNotNull(errors.deadline)
        assertNotNull(errors.ruleAmount)
    }
    @Test fun optionalDeadlineAndAutoSaveAreReallyOptional() {
        assertTrue(GoalValidation.validate("PC", 4_000_000, null, false, 0, today).isValid)
        assertTrue(GoalValidation.validate("PC", 4_000_000, today.plusDays(1), true, 10_000, today).isValid)
    }
}
