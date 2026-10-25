package com.warrantymitra.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class RealBillsTest {
    private fun check(bills: List<Bill>) {
        val misses = bills.flatMap { b ->
            val p = OrderTextParser.parse(b.text)
            listOf(
                "product" to (b.product to p.product), "price" to (b.price to p.price), "invoiceNo" to (b.invoiceNo to p.invoiceNo),
                "date" to (b.date to p.purchaseDate), "platform" to (b.platform to p.platform), "pin" to (b.pin to p.pin),
                "brand" to (b.brand to p.brand), "warrantyMonths" to (b.warrantyMonths to p.warrantyMonths),
            ).filter { (_, v) -> v.first != v.second }.map { (field, v) -> "${b.name}: $field expected ${v.first}, got ${v.second}" }
        }
        assertEquals(misses.joinToString("\n"), 0, misses.size)
    }

    @Test fun billsTheParserWasTunedOn() = check(RealBills.all)

    @Test fun billsWrittenAfterTuning() = check(RealBillsUnseen.all)

    @Test fun ocrDigitSlipsAreRepaired() {
        assertEquals("Hyd 500016 on 20/08/2025 for 36,490.00", OrderTextParser.fixOcrDigits("Hyd 5OOO16 on 2O/O8/2025 for 36,49O.0O"))
        assertEquals("ORDER NO: OD", OrderTextParser.fixOcrDigits("ORDER NO: OD"))
    }
}
