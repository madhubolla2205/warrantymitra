package com.warrantymitra.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class CoverageCalculatorTest {
    private val bought = LocalDate.of(2025, 11, 13)

    @Test fun makerWarrantyEndsTheDayBeforeTheAnniversary() {
        val c = CoverageCalculator.compute(bought, 12, null, LocalDate.of(2026, 10, 25))
        assertEquals(LocalDate.of(2026, 11, 12), c.makerEnd)
        assertEquals(c.makerEnd, c.end)
        assertEquals(18, c.daysLeft)
        assertEquals(CoverageStatus.ENDING_SOON, c.status())
        assertTrue(c.inMaker)
    }

    @Test fun extendedPlanStartsAfterMakerWarranty() {
        val plan = PlanTerms("Onsitego", 24, startsAtPurchase = false, accidental = false, liquid = false)
        val c = CoverageCalculator.compute(bought, 12, plan, LocalDate.of(2027, 1, 1))
        assertEquals(LocalDate.of(2026, 11, 13), c.planStart)
        assertEquals(LocalDate.of(2028, 11, 12), c.planEnd)
        assertFalse(c.inMaker)
        assertTrue(c.inPlan)
        assertEquals(CoverageStatus.COVERED, c.status())
    }

    @Test fun protectionPlanFromPurchaseDoesNotExtendEnd() {
        val plan = PlanTerms("Flipkart protection", 12, startsAtPurchase = true, accidental = true, liquid = true)
        val c = CoverageCalculator.compute(bought, 12, plan, LocalDate.of(2026, 1, 1))
        assertEquals(c.makerEnd, c.end)
        assertTrue(c.inMaker && c.inPlan)
    }

    @Test fun expiredAfterEnd() {
        val c = CoverageCalculator.compute(bought, 12, null, LocalDate.of(2026, 11, 13))
        assertEquals(-1, c.daysLeft)
        assertEquals(CoverageStatus.EXPIRED, c.status())
        assertEquals("Expired", c.statusLabel())
    }

    @Test fun monthEndPurchaseIsClamped() {
        val c = CoverageCalculator.compute(LocalDate.of(2024, 1, 31), 1, null, LocalDate.of(2024, 2, 1))
        assertEquals(LocalDate.of(2024, 2, 28), c.makerEnd)
    }
}
