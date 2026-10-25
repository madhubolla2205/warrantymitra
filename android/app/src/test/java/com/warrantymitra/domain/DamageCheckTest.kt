package com.warrantymitra.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DamageCheckTest {
    private val bought = LocalDate.of(2025, 11, 13)
    private val today = LocalDate.of(2026, 3, 1)
    private val fmt: (LocalDate) -> String = ::formatDate

    @Test fun defectInsideWarrantyIsCovered() {
        val v = DamageCheck.check(Problem.DEFECT, CoverageCalculator.compute(bought, 12, null, today), null, fmt)
        assertEquals(VerdictKind.LIKELY_COVERED, v.kind)
    }

    @Test fun dropWithoutPlanIsNotCovered() {
        val v = DamageCheck.check(Problem.DROP, CoverageCalculator.compute(bought, 12, null, today), null, fmt)
        assertEquals(VerdictKind.LIKELY_NOT_COVERED, v.kind)
    }

    @Test fun liquidDamageCoveredByPlanWithLiquidCover() {
        val plan = PlanTerms("Complete Mobile Protection", 12, startsAtPurchase = true, accidental = true, liquid = true)
        val v = DamageCheck.check(Problem.LIQUID, CoverageCalculator.compute(bought, 12, plan, today), plan, fmt)
        assertEquals(VerdictKind.LIKELY_COVERED, v.kind)
        assertTrue(v.claimWithPlan)
    }

    @Test fun defectDuringExtendedPlanGoesToPlan() {
        val plan = PlanTerms("Onsitego", 12, startsAtPurchase = false, accidental = false, liquid = false)
        val v = DamageCheck.check(Problem.DEFECT, CoverageCalculator.compute(bought, 12, plan, LocalDate.of(2027, 1, 1)), plan, fmt)
        assertEquals(VerdictKind.LIKELY_COVERED, v.kind)
        assertTrue(v.claimWithPlan)
    }

    @Test fun anythingAfterCoverEndsIsNotCovered() {
        val v = DamageCheck.check(Problem.DEFECT, CoverageCalculator.compute(bought, 12, null, LocalDate.of(2027, 1, 1)), null, fmt)
        assertEquals(VerdictKind.LIKELY_NOT_COVERED, v.kind)
    }

    @Test fun surgeIsUnclear() {
        val v = DamageCheck.check(Problem.SURGE, CoverageCalculator.compute(bought, 12, null, today), null, fmt)
        assertEquals(VerdictKind.UNCLEAR, v.kind)
    }
}
