package com.warrantymitra.domain

import java.time.LocalDate

enum class Problem(val label: String) {
    DEFECT("Stopped working on its own"),
    DROP("Dropped, cracked or broken"),
    LIQUID("Water or liquid damage"),
    SURGE("Power cut or voltage fluctuation"),
    WEAR("Battery, remote or consumable part"),
    THEFT("Lost or stolen"),
}

enum class VerdictKind { LIKELY_COVERED, UNCLEAR, LIKELY_NOT_COVERED }

data class Verdict(
    val kind: VerdictKind,
    val title: String,
    val reason: String,
    /** True when the claim should go to the plan provider rather than the brand. */
    val claimWithPlan: Boolean = false,
)

/**
 * Quick rule-based answer to "is this repair free?", using common Indian warranty norms.
 * It is guidance only: the brand or service centre decides after inspection.
 */
object DamageCheck {
    fun check(problem: Problem, coverage: Coverage, plan: PlanTerms?, fmt: (LocalDate) -> String): Verdict {
        val planName = plan?.provider?.takeIf { it.isNotBlank() }?.let { " ($it)" } ?: ""
        if (problem == Problem.THEFT) {
            return Verdict(
                VerdictKind.LIKELY_NOT_COVERED, "Not covered by a warranty",
                "Warranties and most extended plans do not cover loss or theft. Check for a separate gadget or home insurance policy " +
                    "and file a police complaint. For a lost phone, block it on the government CEIR portal (ceir.gov.in).",
            )
        }
        if (coverage.daysLeft < 0) {
            return Verdict(
                VerdictKind.LIKELY_NOT_COVERED, "Not covered: cover ended on ${fmt(coverage.end)}",
                "You will likely pay for this repair. Ask the authorised service centre for a quote first and compare it with a trusted local repair shop.",
            )
        }
        return when (problem) {
            Problem.DEFECT -> when {
                coverage.inMaker -> Verdict(
                    VerdictKind.LIKELY_COVERED, "Likely covered by the maker's warranty",
                    "Faults that appear on their own during the warranty period are what a maker's warranty is for. It runs till ${fmt(coverage.makerEnd)}.",
                )
                coverage.inPlan -> Verdict(
                    VerdictKind.LIKELY_COVERED, "Likely covered by your plan$planName",
                    "The maker's warranty has ended, but your plan runs till ${fmt(coverage.planEnd!!)}. Claim through the plan provider, not the brand.",
                    claimWithPlan = true,
                )
                else -> Verdict(VerdictKind.UNCLEAR, "Unclear", "Your plan has not started yet or has a gap. Check the plan certificate.")
            }
            Problem.DROP, Problem.LIQUID -> {
                val drop = problem == Problem.DROP
                val planCovers = plan != null && (if (drop) plan.accidental else plan.liquid)
                if (planCovers && coverage.inPlan) {
                    Verdict(
                        VerdictKind.LIKELY_COVERED, "Likely covered by your plan$planName",
                        "Your plan includes ${if (drop) "accidental damage" else "liquid damage"} and runs till ${fmt(coverage.planEnd!!)}. " +
                            "Plans often charge a small processing fee per claim.",
                        claimWithPlan = true,
                    )
                } else {
                    Verdict(
                        VerdictKind.LIKELY_NOT_COVERED, "Likely not covered",
                        "Standard warranties in India exclude ${if (drop) "physical damage such as drops and cracks" else "liquid damage"}. " +
                            if (plan != null) "Your saved plan does not list this cover, so check its terms." else "Only an accidental or liquid damage plan would cover this.",
                    )
                }
            }
            Problem.SURGE -> Verdict(
                VerdictKind.UNCLEAR, "Unclear: often excluded",
                "Many brands exclude damage from voltage fluctuation or power surges, especially without a stabiliser. Some do cover it. The service centre will inspect and decide.",
            )
            Problem.WEAR -> Verdict(
                VerdictKind.UNCLEAR, "Usually not covered, sometimes partly",
                "Consumables such as filters, remotes and bulbs are usually excluded. Batteries often have a shorter warranty (for phones, typically 6 months). Check the terms for this part.",
            )
            Problem.THEFT -> error("handled above")
        }
    }
}
