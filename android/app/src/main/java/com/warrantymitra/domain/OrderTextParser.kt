package com.warrantymitra.domain

import java.time.LocalDate

data class ParsedOrder(
    val product: String? = null,
    val price: Long? = null,
    val invoiceNo: String? = null,
    val purchaseDate: LocalDate? = null,
    val platform: String? = null,
    val pin: String? = null,
    val brand: String? = null,
    val warrantyMonths: Int? = null,
) {
    val fieldCount: Int get() = listOf(product, price, invoiceNo, purchaseDate, platform, pin, brand, warrantyMonths).count { it != null }
}

/**
 * Picks purchase details out of an order SMS, an order email, an invoice PDF or the text read from a bill photo.
 * Built for how Indian shops and apps write them: long product titles, several amounts, seller and buyer
 * addresses, OTPs, two-digit years and OCR slips like "5OOO16".
 */
object OrderTextParser {
    val PLATFORMS = listOf(
        "Amazon", "Flipkart", "Croma", "Reliance Digital", "Vijay Sales", "JioMart", "Tata CLiQ", "Myntra", "Meesho", "Nykaa",
    )
    // Links, seller names and order number formats that give the shop away when its name is not written out.
    private val PLATFORM_HINTS = listOf(
        "Amazon" to Regex("""amzn\.in|amazon|appario|cloudtail|\b\d{3}-\d{7}-\d{7}\b""", RegexOption.IGNORE_CASE),
        "Flipkart" to Regex("""fkrt\.it|flipkart|\bOD\d{12,}\b""", RegexOption.IGNORE_CASE),
    )

    val BRANDS = listOf(
        "Samsung", "Apple", "OnePlus", "Xiaomi", "Redmi", "Mi", "Realme", "Oppo", "Vivo", "iQOO", "Motorola", "Nokia", "Poco", "Nothing",
        "Google", "Honor", "Tecno", "Infinix", "Lava", "LG", "Sony", "Panasonic", "Philips", "TCL", "Hisense", "Vu", "Thomson", "Onida",
        "Bajaj", "Havells", "Crompton", "Orient", "Usha", "Atomberg", "Polycab", "V-Guard", "Voltas", "Daikin", "Blue Star", "Lloyd",
        "Hitachi", "Carrier", "Haier", "Whirlpool", "Godrej", "IFB", "Bosch", "Siemens", "Kelvinator", "Kent", "Aquaguard",
        "Eureka Forbes", "Pureit", "AO Smith", "Racold", "Prestige", "Pigeon", "Preethi", "Butterfly", "Morphy Richards", "Kenstar",
        "Sujata", "Dyson", "Faber", "Elica", "Hindware", "Wipro", "Syska", "boAt", "Noise", "JBL", "Bose", "Sennheiser", "Fire-Boltt",
        "Mivi", "Zebronics", "Portronics", "Amazfit", "Titan", "Fastrack", "Lenovo", "HP", "Dell", "Asus", "Acer", "MSI", "Canon",
        "Nikon", "Epson", "Brother", "Luminous", "Exide", "Microtek", "Hero", "Honda", "TVS", "Ather", "Ola",
    )
    // Brands that are also everyday words or letters, so they only count inside a product name.
    private val AMBIGUOUS_BRANDS = setOf("Mi", "Nothing", "Google", "Vu", "HP", "Orient", "Carrier", "Brother", "Titan", "Hero", "Ola", "Honor", "Lava", "Usha")

    private val MONTHS = listOf("jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec")
    private const val M = "jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec"
    private const val AMOUNT = """(\d{1,3}(?:,\d{2,3})+(?:\.\d{1,2})?|\d+(?:\.\d{1,2})?)"""

    private val currencyAmountRe = Regex("""(?:₹|\bRs\.?|\bINR)\s?$AMOUNT""", RegexOption.IGNORE_CASE)
    private val bareAmountRe = Regex("""(?<![\d.])$AMOUNT(?:/-)?(?![\d.])""")
    // Strongest first: what was actually paid beats a plain "total", which can be a tax total.
    private val totalLabels = listOf(
        """grand\s*t[o0]tal|amount\s*paid|total\s*amount\s*payable|amount\s*payable|total\s*payable|net\s*amount|net\s*amt|order\s*total""",
        """(?<!sub)(?<!sub\s)t[o0]tal(?:\s*amount)?""",
    ).map { Regex("""(?:$it)(?!\s*qty)""", RegexOption.IGNORE_CASE) }
    private val notThePriceRe = Regex("""cashback|discount|\boff\b|\bsave\b|up\s*to|upto|reward|coupon|emi\b""", RegexOption.IGNORE_CASE)

    private val orderFormats = listOf(Regex("""\b\d{3}-\d{7}-\d{7}\b"""), Regex("""\bOD\d{12,}\b"""))
    private val orderLabelRe = Regex(
        """\b(?:order[ \t]*(?:id|no\.?|number|#)?|inv[o0]ice[ \t]*(?:n[o0]\.?|number|#)?|inv\.?[ \t]*n[o0]\.?|bill[ \t]*n[o0]\.?)[ \t]*[:#.\-]?[ \t]*([A-Z0-9][A-Z0-9\-/]{2,})""",
        RegexOption.IGNORE_CASE,
    )

    private val numericDateRe = Regex("""(?<![\d-])(\d{1,2})([/\-.])(\d{1,2})\2(\d{4}|\d{2})(?![\d/\-])""")
    private val dayMonthRe = Regex("""\b(\d{1,2})(?:st|nd|rd|th)?[\s\-]+($M)[a-z]*,?[\s\-]+(\d{4}|\d{2})(?!\d)""", RegexOption.IGNORE_CASE)
    private val monthDayRe = Regex("""\b($M)[a-z]*\s+(\d{1,2}),?\s+(\d{4})""", RegexOption.IGNORE_CASE)
    private val futureDateRe = Regex("""(?:deliver(?:y|ed)?\s+by|arriving|expect\w*|estimated|due)\W+(?:\w+\W+){0,2}$""", RegexOption.IGNORE_CASE)
    private val invoiceDateRe = Regex("""inv[o0]ice\s*(?:date|dt)\W*$""", RegexOption.IGNORE_CASE)

    private val pinRe = Regex("""(?<![\d\-/])([1-9]\d{5})(?![\d\-/])""")
    private val notAPinRe = Regex("""(?:otp|code|ref|hsn|sac|a/c|article|txn|utr)\W*$""", RegexOption.IGNORE_CASE)
    private val buyerRe = Regex("""bill(?:ing)?\s*to|billing\s*address|ship\s*to|shipping\s*address|deliver(?:y|ed)?\s*(?:address|to)""", RegexOption.IGNORE_CASE)
    private val sellerRe = Regex("""sold\s*by|ship-?\s*from|seller|dispatch(?:ed)?\s*from""", RegexOption.IGNORE_CASE)

    private val quotedRe = Regex("""['‘“]([^'‘’“”\n]{3,90})['’”]""")
    private val labeledProductRe = Regex("""\b(?:product(?:\s*name|\s*title)?|item(?:\s*name)?|description|particulars)\s*[:\-]\s*(.+)""", RegexOption.IGNORE_CASE)
    private val phraseRe = Regex(
        """(?:order(?:\s+[A-Z0-9\-]{6,})?\s+(?:for|of)|you ordered|package (?:with|containing)|delivered:|shipped:|आपका ऑर्डर|ऑर्डर)\s+(?:your\s+)?(?:(?:[A-Za-z]+\s+)?package\s+(?:with|containing)\s+)?(.{3,120}?)(?=\s+(?:has|have|is|was|were|will|worth|amounting|with\s+(?:order|tracking))\b|\.\.\.|…|\.\s|[\u0900-\u0DFF]|\n|$)""",
        RegexOption.IGNORE_CASE,
    )
    // Where an item line on a bill stops: column gaps, qty and price columns, ASIN or HSN codes.
    private val itemEndRe = Regex("""\s{2,}|\s+\|\s+|\s+HSN|\s+\d+\s+(?:x\s+)?-?[\d,]+(?:\.\d{1,2})?(?:/-)?(?:\s|$)|\s+[\d,]+\.\d{2}\b""", RegexOption.IGNORE_CASE)

    private val itemAmountRe = Regex("""\d{1,3}(?:,\d{2,3})+|\d+\.\d{2}\b|\d+/-""")

    private val warrantyRe = Regex(
        """(?:warranty|guarantee|గ్యారంటీ|वारंटी|गारंटी)[^\n\d]{0,30}?(\d{1,2})\s*(years?|yrs?|months?|mons?)\b|(\d{1,2})\s*(years?|yrs?|months?)\s+(?:of\s+)?(?:\w+\s+){0,2}(?:warranty|guarantee)""",
        RegexOption.IGNORE_CASE,
    )

    fun parse(raw: String): ParsedOrder {
        val text = fixOcrDigits(raw)
        val invoice = findOrderNo(text)
        val price = findPrice(text)
        val product = findProduct(text)
        return ParsedOrder(
            product = product,
            price = price,
            invoiceNo = invoice,
            purchaseDate = findDate(text),
            platform = findPlatform(text),
            pin = findPin(text, invoice, price),
            brand = product?.let(::brandIn),
            warrantyMonths = findWarranty(text),
        )
    }

    /** Bill photos often come back with O for 0 inside numbers ("5OOO16", "2O/O8/2025"). */
    internal fun fixOcrDigits(text: String): String =
        Regex("""(?<![A-Za-z])[0-9Oo][0-9Oo,./\-]*[0-9Oo](?![A-Za-z])""").replace(text) { m ->
            if (m.value.count(Char::isDigit) >= 2) m.value.replace('O', '0').replace('o', '0') else m.value
        }

    private fun findPlatform(text: String): String? =
        PLATFORMS.firstOrNull { p -> Regex("""\b${p.replace(" ", "\\s*")}\b""", RegexOption.IGNORE_CASE).containsMatchIn(text) }
            ?: PLATFORM_HINTS.firstOrNull { (_, re) -> re.containsMatchIn(text) }?.first

    private fun findOrderNo(text: String): String? {
        orderFormats.forEach { re -> re.find(text)?.let { return it.value } }
        return orderLabelRe.findAll(text).map { it.groupValues[1] }.firstOrNull { v -> v.any(Char::isDigit) && !looksLikeDate(v) }
    }

    private fun looksLikeDate(s: String) = numericDateRe.matches(s) || dayMonthRe.matches(s)

    private fun amountOf(s: String) = s.replace(",", "").toDoubleOrNull()?.let { Math.round(it) }

    private fun findPrice(text: String): Long? {
        for (label in totalLabels) {
            // The last amount on the label's line is the one that was paid: "TOTAL: ₹228.66 ₹1,499.00".
            val best = label.findAll(text).mapNotNull { m ->
                val rest = text.substring(m.range.last + 1).substringBefore('\n')
                bareAmountRe.findAll(rest).lastOrNull()?.let { amountOf(it.groupValues[1]) }
            }.lastOrNull { it > 0 }
            if (best != null) return best
        }
        return currencyAmountRe.findAll(text).firstOrNull { m ->
            !notThePriceRe.containsMatchIn(text.substring(maxOf(0, m.range.first - 30), m.range.first))
        }?.let { amountOf(it.groupValues[1]) }?.takeIf { it > 0 }
    }

    private fun findDate(text: String): LocalDate? {
        data class Found(val at: Int, val date: LocalDate)
        val found = mutableListOf<Found>()
        numericDateRe.findAll(text).forEach { m ->
            val (d, _, mo, y) = m.destructured
            val year = if (y.length == 2) 2000 + y.toInt() else y.toInt()
            safeDate(year, mo.toInt(), d.toInt())?.let { found += Found(m.range.first, it) }
        }
        dayMonthRe.findAll(text).forEach { m ->
            val (d, mon, y) = m.destructured
            safeDate(if (y.length == 2) 2000 + y.toInt() else y.toInt(), MONTHS.indexOf(mon.take(3).lowercase()) + 1, d.toInt())?.let { found += Found(m.range.first, it) }
        }
        monthDayRe.findAll(text).forEach { m ->
            val (mon, d, y) = m.destructured
            safeDate(y.toInt(), MONTHS.indexOf(mon.take(3).lowercase()) + 1, d.toInt())?.let { found += Found(m.range.first, it) }
        }
        val before = { f: Found -> text.substring(maxOf(0, f.at - 30), f.at) }
        val past = found.sortedBy { it.at }.filterNot { futureDateRe.containsMatchIn(before(it)) }
        // Warranty runs from the invoice date, which can be a day or two after the order.
        return (past.firstOrNull { invoiceDateRe.containsMatchIn(before(it)) } ?: past.firstOrNull())?.date
    }

    private fun findPin(text: String, invoice: String?, price: Long?): String? {
        val candidates = pinRe.findAll(text).filter { m ->
            val v = m.groupValues[1]
            invoice?.contains(v) != true && price?.toString() != v &&
                !notAPinRe.containsMatchIn(text.substring(maxOf(0, m.range.first - 15), m.range.first))
        }.toList()
        if (candidates.isEmpty()) return null
        // The buyer's PIN is what service centre search needs; on a shop bill the shop's own PIN is the only one.
        val labelBefore = { m: MatchResult, re: Regex -> re.findAll(text.substring(maxOf(0, m.range.first - 200), m.range.first)).lastOrNull() }
        candidates.firstOrNull { m ->
            val buyer = labelBefore(m, buyerRe) ?: return@firstOrNull false
            val seller = labelBefore(m, sellerRe)
            seller == null || seller.range.first < buyer.range.first
        }?.let { return it.groupValues[1] }
        return (candidates.firstOrNull { labelBefore(it, sellerRe) == null } ?: candidates.first()).groupValues[1]
    }

    private fun findProduct(text: String): String? {
        val fromQuotes = quotedRe.findAll(text).map { it.groupValues[1] }.firstOrNull { q -> q.any(Char::isLetter) && q.contains(' ') }
        val fromLabel = labeledProductRe.find(text)?.groupValues?.get(1)?.let(::cutItemLine)
        val fromPhrase = phraseRe.find(text)?.groupValues?.get(1)
        // A brand on a line with an amount is an item; a brand on a line without one is often the shop's own name.
        val brandLines = text.lines().mapNotNull { line ->
            BRANDS.filterNot { it in AMBIGUOUS_BRANDS }.mapNotNull { brandRegex(it).find(line) }.minByOrNull { it.range.first }
                ?.let { line to cutItemLine(line.substring(it.range.first)) }
        }
        val fromBrand = (brandLines.firstOrNull { (line, _) -> itemAmountRe.containsMatchIn(line) } ?: brandLines.firstOrNull())?.second
        return listOf(fromQuotes, fromLabel, fromPhrase, fromBrand).firstNotNullOfOrNull { it?.let(::cleanProduct) }
    }

    private fun cutItemLine(s: String): String = itemEndRe.find(s.trim())?.let { s.trim().substring(0, it.range.first) } ?: s.trim()

    private fun cleanProduct(s: String): String? {
        // Keep "(Mint, 128 GB)" but drop ", Upto 15 Hours Playback (Luscious Black)" marketing tails.
        var depth = 0
        var end = s.length
        for ((i, c) in s.withIndex()) {
            when (c) {
                '(' -> depth++
                ')' -> depth--
                ',' -> if (depth == 0) { end = i; break }
            }
        }
        val out = s.substring(0, end).trim().trimEnd('.', ',', ':', '-').trim()
        return out.takeIf { it.length >= 3 && it.count(Char::isLetter) >= 3 && !Regex("""^(on|to|at|by|from)\b""", RegexOption.IGNORE_CASE).containsMatchIn(it) }
    }

    private fun brandRegex(brand: String) =
        Regex("""(?<![A-Za-z])${brand.replace("-", "\\-").replace(" ", "\\s*")}(?![A-Za-z])""", RegexOption.IGNORE_CASE)

    private fun brandIn(product: String): String? =
        BRANDS.mapNotNull { b -> brandRegex(b).find(product)?.let { b to it.range.first } }.minByOrNull { it.second }?.first

    private fun findWarranty(text: String): Int? = warrantyRe.find(text)?.let { m ->
        val n = (m.groupValues[1].ifEmpty { m.groupValues[3] }).toInt()
        val unit = m.groupValues[2].ifEmpty { m.groupValues[4] }.lowercase()
        if (unit.startsWith("y")) n * 12 else n
    }?.takeIf { it in 1..240 }

    private fun safeDate(y: Int, m: Int, d: Int): LocalDate? = runCatching { LocalDate.of(y, m, d) }.getOrNull()
}
