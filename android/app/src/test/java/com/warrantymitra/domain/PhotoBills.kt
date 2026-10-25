package com.warrantymitra.domain

import java.time.LocalDate

/**
 * Text exactly as the on-phone reader (ML Kit) returned it for the bill photos in .github/scripts/bills,
 * captured from the emulator run, slips and all: "Hyde rabad", "0l8834", "VS - AMP -25-77812", "20/08/2 025".
 */
object PhotoBills {
    val all = listOf(
        Bill(
            "Local shop GST bill, tilted, with stamp",
            """
            SRI VENKATESWARA ELECTRONICS
            D.No 6-3-249, Main Road, Ameerpet, Hyderabad - 500016
            Ph: 040-23741234 GSTIN: 36ABCPV1234K1ZQ
            TAX INVOICE
            Inv No. SVE/1087
            To: Mr. Suresh   Date: 02/09/25
            S.NO Particulars
            1   Bajaj Rex 500W Mixer Grinder   HSN   Qty Rate   Amount
            8509   1 2711.86 2711.86
            CGST 9%
            SGST 9%   |244.07
            |244.07
            Grand Total   3200.00
            Rupees Three Thousand Two Hundred Only
            Warranty: 1 Year from date of bill
            E.&0.E. Goods once sold will not be taken back.
            PAID
            Customer Signature
            For Sri Venkateswara Electronics
            """.trimIndent(),
            product = "Bajaj Rex 500W Mixer Grinder", price = 3200, invoiceNo = "SVE/1087", date = LocalDate.of(2025, 9, 2), pin = "500016", brand = "Bajaj", warrantyMonths = 12,
        ),
        Bill(
            "Croma thermal receipt, faded and creased",
            """
            CROMA
            Infiniti Retail Limited
            Croma - Inorbit Mall, Madhapur
            Hyde rabad 500081
            GSTIN: 36AACCV1726H 1ZJ
            TAX INVOICE
            Bill No: C412/2526/0l8834
            Date: 05/10/2025 18:42
            Cust: RAVI Mob: 98xxXXXX21
            Description   Qty   Amount
            SONY WH-1000XM5 BLK   1   29990. 00
            HSN 85183000
            Total Qty: 1
            Net Amount :   29,990.00
            Paid by: HDFC CREDIT CARD
            29,990.00
            Warranty: 1 Year from invoice date
            Thank you for shopping at Croma
            WwW.Croma. Com   1800 572 7662
            """.trimIndent(),
            product = "SONY WH-1000XM5 BLK", price = 29990, invoiceNo = "C412/2526/018834", date = LocalDate.of(2025, 10, 5), platform = "Croma", pin = "500081", brand = "Sony", warrantyMonths = 12,
        ),
        Bill(
            "Reliance Digital invoice, dim light",
            """
            Reliance Digital
            Reliance Retail Ltd
            Store: RD Kukatpally, Hyderabad 500072
            Tax Invoice
            Invoice No : 251ORD4471000129
            Invoice Dt : 14-Sep-2025
            Description   Qty   Net Amt
            Samsung 253L 3 Star Frost Free Refrigerator RT28C3053S8   1   24490.00
            Total Amount Payable : Rs 24,490.00
            Warranty: 1 Year on product, 20 Years on compressor
            """.trimIndent(),
            product = "Samsung 253L 3 Star Frost Free Refrigerator RT28C3053S8", price = 24490,
            // The printed number is 2510RD4471000129; the reader saw "0R" as "OR", and an O next to letters can't be safely changed back.
            invoiceNo = "251ORD4471000129", date = LocalDate.of(2025, 9, 14), platform = "Reliance Digital", pin = "500072", brand = "Samsung", warrantyMonths = 12,
        ),
        Bill(
            "Vijay Sales slip, crumpled, low contrast",
            """
            VIJAY SALES
            Amee rpet Hyd 500016
            INVOICE NO: VS - AMP -25-77812
            DT:20/08/2 025
            HAIER 1.5T 3S INV AC HSU18C
            36,490. 00
            TOTAL   36,490.00
            Thank you. Visit again
            1 Yr warranty, 10 Yr on compressor
            """.trimIndent(),
            product = "HAIER 1.5T 3S INV AC HSU18C", price = 36490, invoiceNo = "VS-AMP-25-77812", date = LocalDate.of(2025, 8, 20), platform = "Vijay Sales", pin = "500016", brand = "Haier", warrantyMonths = 12,
        ),
    )
}
