package com.warrantymitra.data

import com.warrantymitra.domain.Coverage
import com.warrantymitra.domain.CoverageCalculator
import com.warrantymitra.domain.PlanTerms
import com.warrantymitra.domain.ReminderSubject
import java.time.LocalDate

fun Item.purchaseDate(): LocalDate = LocalDate.ofEpochDay(purchaseEpochDay)

fun Item.amcDate(): LocalDate? = amcEpochDay?.let(LocalDate::ofEpochDay)

fun Item.plan(): PlanTerms? =
    if (planMonths > 0 || planProvider.isNotBlank()) PlanTerms(planProvider, planMonths, planStartsAtPurchase, planAccidental, planLiquid) else null

fun Item.coverage(today: LocalDate = LocalDate.now()): Coverage =
    CoverageCalculator.compute(purchaseDate(), warrantyMonths, plan(), today)

fun Item.remindDayList(): List<Int> = remindDays.split(',').mapNotNull { it.trim().toIntOrNull() }

fun Item.hasOpenClaim(): Boolean = claimStatus.isNotBlank() && claimStatus != "Closed"

fun Item.toReminderSubject() = ReminderSubject(id, product, purchaseDate(), warrantyMonths, plan(), remindDayList(), amcDate())

/** Where it was bought, in one line: "Croma, Hyderabad" or "Flipkart · RetailNet". */
fun Item.whereBought(): String =
    if (boughtOnline) listOf(platform.ifBlank { "Online" }, storeName).filter { it.isNotBlank() }.joinToString(" · ")
    else listOf(storeName, city).filter { it.isNotBlank() }.joinToString(", ")
