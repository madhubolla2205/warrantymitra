package com.warrantymitra.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class OrderTextParserTest {
    @Test fun amazonEmail() {
        val p = OrderTextParser.parse(
            "Your order of Philips Air Fryer HD9200 has been delivered. Order #402-1234567-1234567 delivered on 12/09/2026. Total Rs. 8,499 Amazon.in",
        )
        assertEquals("Philips Air Fryer HD9200", p.product)
        assertEquals(8499L, p.price)
        assertEquals("402-1234567-1234567", p.invoiceNo)
        assertEquals(LocalDate.of(2026, 9, 12), p.purchaseDate)
        assertEquals("Amazon", p.platform)
    }

    @Test fun flipkartSmsWithWrittenDate() {
        val p = OrderTextParser.parse("Delivered: Samsung Galaxy S24 FE is delivered on 13 Nov 2025. Order ID OD330128872110 worth ₹35,999. Flipkart")
        assertEquals(35999L, p.price)
        assertEquals("OD330128872110", p.invoiceNo)
        assertEquals(LocalDate.of(2025, 11, 13), p.purchaseDate)
        assertEquals("Flipkart", p.platform)
    }

    @Test fun shopBillWithPin() {
        val p = OrderTextParser.parse("Sri Lakshmi Electronics, Ameerpet, Hyderabad 500016. Invoice No: SLE-2291 Date 05-08-2025 Amount INR 15500")
        assertEquals("500016", p.pin)
        assertEquals("SLE-2291", p.invoiceNo)
        assertEquals(LocalDate.of(2025, 8, 5), p.purchaseDate)
        assertEquals(15500L, p.price)
        assertNull(p.platform)
    }

    @Test fun invalidDateIgnored() {
        assertNull(OrderTextParser.parse("on 31/02/2026").purchaseDate)
    }
}
