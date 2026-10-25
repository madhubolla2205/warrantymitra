package com.warrantymitra.domain

import java.time.LocalDate

data class ParsedOrder(
    val product: String? = null,
    val price: Long? = null,
    val invoiceNo: String? = null,
    val purchaseDate: LocalDate? = null,
    val platform: String? = null,
    val pin: String? = null,
) {
    val fieldCount: Int get() = listOf(product, price, invoiceNo, purchaseDate, platform, pin).count { it != null }
}

/** Picks purchase details out of an order SMS or email from Indian shops and shopping apps. */
object OrderTextParser {
    val PLATFORMS = listOf(
        "Amazon", "Flipkart", "Croma", "Reliance Digital", "Vijay Sales", "JioMart", "Tata CLiQ", "Myntra", "Meesho", "Nykaa",
    )
    private val MONTHS = listOf("jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec")
    private const val M = "jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec"

    private val priceRe = Regex("""(?:₹|Rs\.?|INR)\s?([\d,]+(?:\.\d{1,2})?)""", RegexOption.IGNORE_CASE)
    private val orderRe = Regex(
        """(?:order\s*(?:id|no\.?|number|#)?|invoice\s*(?:no\.?|number)?)\s*[:#-]?\s*([A-Z0-9][A-Z0-9\-/]{6,})""",
        RegexOption.IGNORE_CASE,
    )
    private val numericDateRe = Regex("""\b(\d{1,2})[/\-.](\d{1,2})[/\-.](\d{4})\b""")
    private val dayMonthRe = Regex("""\b(\d{1,2})(?:st|nd|rd|th)?\s+($M)[a-z]*,?\s+(\d{4})""", RegexOption.IGNORE_CASE)
    private val monthDayRe = Regex("""\b($M)[a-z]*\s+(\d{1,2}),?\s+(\d{4})""", RegexOption.IGNORE_CASE)
    private val pinRe = Regex("""\b([1-9]\d{5})\b""")
    private val productRe = Regex(
        """(?:order (?:for|of)|you ordered|item:?|delivered:?|shipped:?)\s+(?:your\s+)?(.{3,70}?)(?=\s+(?:has|have|is|was|will|worth|for ₹|for Rs)\b|[,.\n]|$)""",
        RegexOption.IGNORE_CASE,
    )

    fun parse(text: String): ParsedOrder {
        val price = priceRe.find(text)?.groupValues?.get(1)?.replace(",", "")?.toDoubleOrNull()?.let { Math.round(it) }
        val invoice = orderRe.find(text)?.groupValues?.get(1)?.takeIf { it.any(Char::isDigit) }
        val date = parseDate(text)
        val platform = PLATFORMS.firstOrNull { p -> Regex(p.replace(" ", "\\s*"), RegexOption.IGNORE_CASE).containsMatchIn(text) }
        // Leave out the order number itself and amounts so they are not mistaken for a PIN code.
        val pin = pinRe.findAll(text).map { it.groupValues[1] }.firstOrNull { candidate ->
            invoice?.contains(candidate) != true && price?.toString() != candidate
        }
        val product = productRe.find(text)?.groupValues?.get(1)?.trim()?.trimEnd('.', ',')?.takeIf { it.length >= 3 }
        return ParsedOrder(product, price, invoice, date, platform, pin)
    }

    private fun parseDate(text: String): LocalDate? {
        numericDateRe.find(text)?.let { m ->
            val (d, mo, y) = m.destructured
            safeDate(y.toInt(), mo.toInt(), d.toInt())?.let { return it }
        }
        dayMonthRe.find(text)?.let { m ->
            val (d, mon, y) = m.destructured
            safeDate(y.toInt(), MONTHS.indexOf(mon.take(3).lowercase()) + 1, d.toInt())?.let { return it }
        }
        monthDayRe.find(text)?.let { m ->
            val (mon, d, y) = m.destructured
            safeDate(y.toInt(), MONTHS.indexOf(mon.take(3).lowercase()) + 1, d.toInt())?.let { return it }
        }
        return null
    }

    private fun safeDate(y: Int, m: Int, d: Int): LocalDate? = runCatching { LocalDate.of(y, m, d) }.getOrNull()
}
