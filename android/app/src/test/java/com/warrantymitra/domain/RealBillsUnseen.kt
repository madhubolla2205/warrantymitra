package com.warrantymitra.domain

import java.time.LocalDate

/** A second set, written after the parser was tuned on [RealBills], to check it was not just fitted to those. */
object RealBillsUnseen {
    val all = listOf(
        Bill(
            "Amazon shipped SMS",
            """Shipped: Your Amazon package with Prestige PKGSS 3L Svachh Pressure Cooker will be delivered Thu, 9 Oct. Track at amzn.in/d/aB3kP""",
            product = "Prestige PKGSS 3L Svachh Pressure Cooker", platform = "Amazon", brand = "Prestige",
        ),
        Bill(
            "Flipkart delivered SMS",
            """Delivered: Your Whirlpool 184 L Direct Cool Single Door 3 Star Refrigerator with tracking ID FMPC3329948812 was delivered on 12-08-2025. Rate your experience: http://fkrt.it/aQ8tTuNNNN""",
            product = "Whirlpool 184 L Direct Cool Single Door 3 Star Refrigerator", date = LocalDate.of(2025, 8, 12), platform = "Flipkart",
            brand = "Whirlpool",
        ),
        Bill(
            "Vijay Sales online invoice",
            """
            VIJAY SALES (INDIA) PVT. LTD.
            Tax Invoice
            Invoice No.: VSO/2526/0034412      Invoice Date: 21/06/2025
            Shipping Address: Anil Rao, 4-1-12, Begum Bazar, Hyderabad - 500012
            Sr  Item Description                       Qty  Rate       Amount
            1   Daikin 1.5 Ton 3 Star Inverter Split AC FTKL50UV16  1    38,990.00  38,990.00
            Installation                               1    0.00       0.00
            Invoice Total (Rs.)                                        38,990.00
            Warranty: 1 Year comprehensive, 5 years on PCB, 10 years on compressor
            """.trimIndent(),
            product = "Daikin 1.5 Ton 3 Star Inverter Split AC FTKL50UV16", price = 38990, invoiceNo = "VSO/2526/0034412",
            date = LocalDate.of(2025, 6, 21), platform = "Vijay Sales", pin = "500012", brand = "Daikin", warrantyMonths = 12,
        ),
        Bill(
            "Local mobile shop bill",
            """
            SAI MOBILES
            Shop 12, Abids Road, Hyderabad 500001
            Cash Bill   No. 0789   Dt. 3/9/2025
            Oppo A79 5G 8/128 Glowing Green  IMEI 86xxxxxxxxx4410   1  17,999
            Tempered glass                                           1     199
            Total                                                       18,198
            """.trimIndent(),
            product = "Oppo A79 5G 8/128 Glowing Green", price = 18198, invoiceNo = "0789", date = LocalDate.of(2025, 9, 3), pin = "500001",
            brand = "Oppo",
        ),
        Bill(
            "Hindi bill with amount label",
            """
            गुप्ता इलेक्ट्रॉनिक्स / GUPTA ELECTRONICS
            Lalbagh, Lucknow 226001
            बिल नं / Bill No: 1442   दिनांक / Date: 07-09-2025
            Havells Stealth Air 1200mm Fan   1   4,650
            कुल / Total: ₹4,650
            वारंटी / Warranty: 5 years
            """.trimIndent(),
            product = "Havells Stealth Air 1200mm Fan", price = 4650, invoiceNo = "1442", date = LocalDate.of(2025, 9, 7), pin = "226001",
            brand = "Havells", warrantyMonths = 60,
        ),
        Bill(
            "Reliance Digital online email",
            """
            Thanks for your order at Reliance Digital!
            Order Number: RDO0012345678
            Order Date: 29 Aug 2025
            Item: OnePlus Nord CE4 Lite 5G (Super Silver, 8GB RAM, 128GB)
            You paid: ₹19,999.00
            Deliver to: Kavya, 12 Park Street, Kolkata 700016
            """.trimIndent(),
            product = "OnePlus Nord CE4 Lite 5G (Super Silver, 8GB RAM, 128GB)", price = 19999, invoiceNo = "RDO0012345678",
            date = LocalDate.of(2025, 8, 29), platform = "Reliance Digital", pin = "700016", brand = "OnePlus",
        ),
        Bill(
            "Credit card SMS for a store purchase",
            """INR 52,490.00 spent on ICICI Bank Card XX9012 on 01-Sep-25 at CROMA KONDAPUR. Avl Limit: INR 1,23,456.78. If not you, call 1800 2662.""",
            price = 52490, date = LocalDate.of(2025, 9, 1), platform = "Croma",
        ),
        Bill(
            "Faded thermal receipt OCR",
            """
            KENT RO SYSTEMS - AUTH. DEALER
            AQUA CARE, Kothrud, Pune 411038
            Bill N0 : AC/245   Date: 11/O7/2025
            KENT GRAND PLUS RO+UV+UF  1  18,5OO.OO
            NET AMT   18,5OO.OO
            1 Yr warranty, free service x3
            """.trimIndent(),
            product = "KENT GRAND PLUS RO+UV+UF", price = 18500, invoiceNo = "AC/245", date = LocalDate.of(2025, 7, 11), pin = "411038",
            brand = "Kent", warrantyMonths = 12,
        ),
    )
}
