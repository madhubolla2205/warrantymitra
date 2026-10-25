package com.warrantymitra.domain

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

private val dateFormat = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)

fun formatDate(d: LocalDate): String = d.format(dateFormat)

/** Indian digit grouping: 1,24,999 rather than 124,999. */
fun formatRupees(amount: Long): String {
    val digits = abs(amount).toString()
    val grouped = if (digits.length <= 3) digits else {
        val head = digits.dropLast(3).reversed().chunked(2).joinToString(",").reversed()
        "$head,${digits.takeLast(3)}"
    }
    return (if (amount < 0) "-₹" else "₹") + grouped
}
