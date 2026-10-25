package com.warrantymitra.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ReminderPlannerTest {
    private val subject = ReminderSubject(
        itemId = 7, product = "LG washing machine", purchase = LocalDate.of(2025, 1, 10), warrantyMonths = 24, plan = null,
        remindDays = listOf(30, 10, 1), amcDate = LocalDate.of(2026, 12, 8),
    )

    @Test fun firesOnReminderDays() {
        // Cover ends 9 Jan 2027, so 30 days before is 10 Dec 2026.
        val due = ReminderPlanner.due(listOf(subject), LocalDate.of(2026, 12, 10), ::formatDate)
        assertEquals(1, due.size)
        assertTrue(due[0].title.contains("30 days"))
    }

    @Test fun quietOnOtherDays() {
        assertTrue(ReminderPlanner.due(listOf(subject), LocalDate.of(2026, 12, 11), ::formatDate).isEmpty())
    }

    @Test fun amcReminderAWeekBefore() {
        val due = ReminderPlanner.due(listOf(subject), LocalDate.of(2026, 12, 1), ::formatDate)
        assertEquals(1, due.size)
        assertTrue(due[0].title.contains("AMC"))
    }
}
