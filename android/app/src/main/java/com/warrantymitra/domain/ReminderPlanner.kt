package com.warrantymitra.domain

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class ReminderSubject(
    val itemId: Long,
    val product: String,
    val purchase: LocalDate,
    val warrantyMonths: Int,
    val plan: PlanTerms?,
    val remindDays: List<Int>,
    val amcDate: LocalDate?,
)

data class DueReminder(val itemId: Long, val title: String, val text: String)

/** Decides which reminders fire on a given day. Runs once a day from the reminder worker. */
object ReminderPlanner {
    val AMC_DAYS = listOf(7, 1, 0)

    fun due(subjects: List<ReminderSubject>, today: LocalDate, fmt: (LocalDate) -> String): List<DueReminder> {
        val out = mutableListOf<DueReminder>()
        for (s in subjects) {
            val c = CoverageCalculator.compute(s.purchase, s.warrantyMonths, s.plan, today)
            if (c.daysLeft >= 0 && s.remindDays.any { it.toLong() == c.daysLeft }) {
                val title = if (c.daysLeft == 0L) "${s.product}: cover ends today" else
                    "${s.product}: cover ends in ${c.daysLeft} day${if (c.daysLeft == 1L) "" else "s"}"
                out += DueReminder(s.itemId, title, "Covered till ${fmt(c.end)}. Get any problems checked before then.")
            }
            s.amcDate?.let { amc ->
                val d = ChronoUnit.DAYS.between(today, amc)
                if (d in AMC_DAYS.map { it.toLong() }) {
                    out += DueReminder(s.itemId, "${s.product}: AMC or service due ${if (d == 0L) "today" else "on ${fmt(amc)}"}", "Book the service visit or renew the AMC.")
                }
            }
        }
        return out
    }
}
