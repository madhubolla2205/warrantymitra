package com.warrantymitra.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class FormatsTest {
    @Test fun indianGrouping() {
        assertEquals("₹999", formatRupees(999))
        assertEquals("₹8,499", formatRupees(8499))
        assertEquals("₹1,24,999", formatRupees(124999))
        assertEquals("₹1,24,99,900", formatRupees(12499900))
    }

    @Test fun dateFormat() = assertEquals("05 Aug 2025", formatDate(LocalDate.of(2025, 8, 5)))
}
