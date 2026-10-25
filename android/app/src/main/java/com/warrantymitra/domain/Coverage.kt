package com.warrantymitra.domain

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** An extended warranty or protection plan bought on top of the maker's warranty. */
data class PlanTerms(
    val provider: String,
    val months: Int,
    val startsAtPurchase: Boolean,
    val accidental: Boolean,
    val liquid: Boolean,
)

data class Coverage(
    val purchase: LocalDate,
    val makerEnd: LocalDate,
    val planStart: LocalDate?,
    val planEnd: LocalDate?,
    /** The last day anything still covers the product. */
    val end: LocalDate,
    val daysLeft: Long,
    /** Share of the whole cover period already used, 0..1. */
    val usedFraction: Float,
    val inMaker: Boolean,
    val inPlan: Boolean,
)

enum class CoverageStatus { COVERED, ENDING_SOON, EXPIRED }

object CoverageCalculator {
    const val SOON_DAYS = 30

    fun compute(purchase: LocalDate, warrantyMonths: Int, plan: PlanTerms?, today: LocalDate): Coverage {
        // A 12-month warranty bought on 13 Nov 2025 runs through 12 Nov 2026.
        val makerEnd = purchase.plusMonths(warrantyMonths.toLong()).minusDays(1)
        var planStart: LocalDate? = null
        var planEnd: LocalDate? = null
        if (plan != null && plan.months > 0) {
            val start = if (plan.startsAtPurchase) purchase else makerEnd.plusDays(1)
            planStart = start
            planEnd = start.plusMonths(plan.months.toLong()).minusDays(1)
        }
        val end = if (planEnd != null && planEnd.isAfter(makerEnd)) planEnd else makerEnd
        val daysLeft = ChronoUnit.DAYS.between(today, end)
        val span = maxOf(1L, ChronoUnit.DAYS.between(purchase, end))
        val used = (ChronoUnit.DAYS.between(purchase, today).toFloat() / span).coerceIn(0f, 1f)
        val inMaker = !today.isBefore(purchase) && !today.isAfter(makerEnd)
        val inPlan = planStart != null && planEnd != null && !today.isBefore(planStart) && !today.isAfter(planEnd)
        return Coverage(purchase, makerEnd, planStart, planEnd, end, daysLeft, used, inMaker, inPlan)
    }
}

fun Coverage.status(): CoverageStatus = when {
    daysLeft < 0 -> CoverageStatus.EXPIRED
    daysLeft <= CoverageCalculator.SOON_DAYS -> CoverageStatus.ENDING_SOON
    else -> CoverageStatus.COVERED
}

fun Coverage.statusLabel(): String = when (status()) {
    CoverageStatus.EXPIRED -> "Expired"
    CoverageStatus.ENDING_SOON -> if (daysLeft == 0L) "Ends today" else "Ends in $daysLeft day${if (daysLeft == 1L) "" else "s"}"
    CoverageStatus.COVERED -> "Covered"
}
