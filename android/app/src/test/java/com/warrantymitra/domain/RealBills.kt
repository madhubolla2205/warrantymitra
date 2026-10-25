package com.warrantymitra.domain

import java.time.LocalDate

/**
 * Bills and order messages written the way Indian shops, apps and banks actually send them:
 * long product titles, several amounts, seller and buyer addresses, OTPs, OCR slips and Hindi.
 * Names, numbers and addresses are made up.
 */
data class Bill(
    val name: String,
    val text: String,
    val product: String? = null,
    val price: Long? = null,
    val invoiceNo: String? = null,
    val date: LocalDate? = null,
    val platform: String? = null,
    val pin: String? = null,
    val brand: String? = null,
    val warrantyMonths: Int? = null,
)

object RealBills {
    val all = listOf(
        Bill(
            "Amazon delivered SMS",
            """Delivered: Your package with boAt Rockerz 450 Bluetooth On Ear Headphones with Mic... has been delivered. Track: https://amzn.in/d/8kQ2xLm""",
            product = "boAt Rockerz 450 Bluetooth On Ear Headphones with Mic", platform = "Amazon", brand = "boAt",
        ),
        Bill(
            "Amazon order confirmation email",
            """
            Hello Ravi,
            Thank you for your order. We'll send a confirmation when your items ship.
            Details
            Order #407-8843210-5521946
            Arriving:
            Tuesday, October 21
            Ship to:
            Ravi Kumar
            HYDERABAD, TELANGANA
            Order Total: ₹1,499.00
            Placed on Saturday, October 18, 2025
            boAt Rockerz 450 Bluetooth On Ear Headphones with Mic, Upto 15 Hours Playback (Luscious Black)
            Sold by: Appario Retail Private Ltd
            ₹1,499.00
            Quantity: 1
            """.trimIndent(),
            product = "boAt Rockerz 450 Bluetooth On Ear Headphones with Mic", price = 1499, invoiceNo = "407-8843210-5521946",
            date = LocalDate.of(2025, 10, 18), platform = "Amazon", brand = "boAt",
        ),
        Bill(
            "Amazon tax invoice (PDF text)",
            """
            Tax Invoice/Bill of Supply/Cash Memo
            (Original for Recipient)
            Sold By :
            Appario Retail Private Ltd
            *Building No.3, Sy No. 33 to 37, Mamidipally Village, Shamshabad Mandal
            Hyderabad, TELANGANA, 500108
            IN
            PAN No: AALCA0171E
            GST Registration No: 36AALCA0171E1ZK
            Order Number: 407-8843210-5521946
            Order Date: 18.10.2025
            Billing Address :
            Ravi Kumar
            Flat 302, Sai Residency, Madhapur
            HYDERABAD, TELANGANA, 500081
            IN
            Invoice Number : HYD8-1129845
            Invoice Date : 18.10.2025
            Sl. No Description Unit Price Qty Net Amount Tax Rate Tax Type Tax Amount Total Amount
            1 boAt Rockerz 450 Bluetooth On Ear Headphones | B07PR1CL3S ( B07PR1CL3S ) HSN:85183000 ₹1,270.34 1 ₹1,270.34 9% CGST ₹114.33 ₹1,499.00
            9% SGST ₹114.33
            TOTAL: ₹228.66 ₹1,499.00
            Amount in Words:
            One Thousand Four Hundred Ninety-nine only
            """.trimIndent(),
            product = "boAt Rockerz 450 Bluetooth On Ear Headphones", price = 1499, invoiceNo = "407-8843210-5521946",
            date = LocalDate.of(2025, 10, 18), platform = "Amazon", pin = "500081", brand = "boAt",
        ),
        Bill(
            "Flipkart order placed SMS",
            """Order Placed: Your order for Samsung Galaxy S24 FE 5G (Mint, 128 GB) with order id OD432918877165451100 amounting to Rs.32490 has been received. You can expect delivery by Mon Oct 27, 2025. Manage your order at http://fkrt.it/kD2Tq7NNNN""",
            product = "Samsung Galaxy S24 FE 5G (Mint, 128 GB)", price = 32490, invoiceNo = "OD432918877165451100",
            platform = "Flipkart", brand = "Samsung",
        ),
        Bill(
            "Flipkart tax invoice (PDF text)",
            """
            Tax Invoice
            Order ID: OD432918877165451100
            Order Date: 23-10-2025, 09:41 PM
            Invoice Date: 25-10-2025, 10:12 AM
            PAN: AABCF8078M
            Sold By: TBL Online Services Private Limited ,
            Ship-from Address: Survey No 228, Medchal Village, Hyderabad, Telangana, India - 501401
            GSTIN - 36AABCF8078M1ZC
            Invoice Number # FAMDDS2600123456
            Bill To
            Ravi Kumar
            Plot 14, Kondapur, Hyderabad 500084, Telangana
            Product Title Qty Gross Amount ₹ Discount ₹ Taxable Value ₹ IGST ₹ Total ₹
            Samsung Galaxy S24 FE 5G (Mint, 128 GB) 1 35999.00 -3509.00 27533.90 4956.10 32490.00
            FSN: MOBH4DQFQ2Y8KZHX
            HSN/SAC: 85171300
            Warranty: 1 Year Manufacturer Warranty for Phone and 6 Months for In-Box Accessories
            Grand Total ₹ 32490.00
            """.trimIndent(),
            product = "Samsung Galaxy S24 FE 5G (Mint, 128 GB)", price = 32490, invoiceNo = "OD432918877165451100",
            date = LocalDate.of(2025, 10, 25), platform = "Flipkart", pin = "500084", brand = "Samsung", warrantyMonths = 12,
        ),
        Bill(
            "Flipkart email, discount and amount paid",
            """
            Hi Priya, your order has been placed.
            Order ID OD432811090123344200 | Ordered on 04 Oct 2025
            Item: Noise ColorFit Pro 5 Smart Watch
            Price ₹4,999
            Discount -₹1,500
            Delivery Charges FREE
            Amount Paid ₹3,499
            Delivery Address: Priya Sharma, 21 MG Road, Indiranagar, Bengaluru 560038
            """.trimIndent(),
            product = "Noise ColorFit Pro 5 Smart Watch", price = 3499, invoiceNo = "OD432811090123344200",
            date = LocalDate.of(2025, 10, 4), platform = "Flipkart", pin = "560038", brand = "Noise",
        ),
        Bill(
            "Croma online order email",
            """
            Thank you for shopping with Croma!
            Your order 30098812345 has been confirmed.
            Order Date: 12 Oct 2025
            Product: LG 7 kg 5 Star Inverter Fully Automatic Front Load Washing Machine (FHM1207SDW)
            Qty: 1
            Price: ₹31,990.00
            Payment mode: UPI
            """.trimIndent(),
            product = "LG 7 kg 5 Star Inverter Fully Automatic Front Load Washing Machine (FHM1207SDW)", price = 31990,
            invoiceNo = "30098812345", date = LocalDate.of(2025, 10, 12), platform = "Croma", brand = "LG",
        ),
        Bill(
            "Croma out-for-delivery SMS with OTP",
            """Hi, your Croma order 30098812399 for Apple iPhone 16 (128GB, Black) is out for delivery. Share OTP 482913 with the delivery executive. Track: croma.com/t/Q7xK""",
            product = "Apple iPhone 16 (128GB, Black)", invoiceNo = "30098812399", platform = "Croma", brand = "Apple",
        ),
        Bill(
            "Croma store receipt",
            """
            CROMA
            Infiniti Retail Limited
            Croma - Inorbit Mall, Madhapur
            Hyderabad - 500081
            GSTIN: 36AACCV1726H1ZJ
            TAX INVOICE
            Bill No: C412/2526/018834   Date: 05/10/2025 18:42
            Cust: RAVI  Mob: 98xxxxxx21
            Item Code  Description            Qty   Amount
            234567     SONY WH-1000XM5 BLK      1   29990.00
            Total Qty: 1
            Net Amount :   29,990.00
            Paid by: HDFC CREDIT CARD  29,990.00
            Thank you for shopping at Croma
            """.trimIndent(),
            product = "SONY WH-1000XM5 BLK", price = 29990, invoiceNo = "C412/2526/018834", date = LocalDate.of(2025, 10, 5),
            platform = "Croma", pin = "500081", brand = "Sony",
        ),
        Bill(
            "Reliance Digital store invoice",
            """
            Reliance Digital
            Reliance Retail Ltd
            Store: RD Kukatpally, Hyderabad 500072
            Tax Invoice
            Invoice No : 2510RD4471000129
            Invoice Dt : 14-Sep-2025
            Article    Description                                              Qty  MRP       Net Amt
            491234567  Samsung 253L 3 Star Frost Free Refrigerator RT28C3053S8   1    28990.00  24490.00
            Total Amount Payable : Rs 24,490.00
            Warranty: 1 Year on product, 20 Years on compressor
            """.trimIndent(),
            product = "Samsung 253L 3 Star Frost Free Refrigerator RT28C3053S8", price = 24490, invoiceNo = "2510RD4471000129",
            date = LocalDate.of(2025, 9, 14), platform = "Reliance Digital", pin = "500072", brand = "Samsung", warrantyMonths = 12,
        ),
        Bill(
            "Local shop GST bill",
            """
            SRI VENKATESWARA ELECTRONICS & HOME APPLIANCES
            D.No 6-3-249, Main Road, Ameerpet, Hyderabad - 500016
            Ph: 040-23741234  GSTIN: 36ABCPV1234K1ZQ
            TAX INVOICE
            Inv No. SVE/1087     Date: 02/09/25
            To: Mr. Suresh
            S.No Particulars                     HSN   Qty  Rate     Amount
            1    Bajaj Rex 500W Mixer Grinder     8509  1    2711.86  2711.86
                 CGST 9%                                          244.07
                 SGST 9%                                          244.07
                                         Grand Total            3200.00
            Rupees Three Thousand Two Hundred Only
            E.&O.E.  Goods once sold will not be taken back.
            """.trimIndent(),
            product = "Bajaj Rex 500W Mixer Grinder", price = 3200, invoiceNo = "SVE/1087", date = LocalDate.of(2025, 9, 2),
            pin = "500016", brand = "Bajaj",
        ),
        Bill(
            "Local shop bill, rupee slash style",
            """
            NEW LAXMI TV CENTRE
            Station Road, Vijayawada 520003  Cell 9848012345
            Bill No. 4512      Date 15.07.2025
            LG 43" 4K Smart TV 43UR7500          1     27,500/-
            Total Rs. 27,500/-
            Warranty: 1 yr comprehensive + 1 yr on panel
            """.trimIndent(),
            product = "LG 43\" 4K Smart TV 43UR7500", price = 27500, invoiceNo = "4512", date = LocalDate.of(2025, 7, 15),
            pin = "520003", brand = "LG", warrantyMonths = 12,
        ),
        Bill(
            "Vijay Sales receipt read from a faded photo (OCR errors)",
            """
            VIJAY SALES
            Ameerpet Hyd 5OOO16
            INV0ICE N0: VS-AMP-25-77812
            DT:2O/O8/2025
            HAIER 1.5T 3S INV AC HSU18C   1   36,490.0O
            T0TAL      36,49O.00
            """.trimIndent(),
            product = "HAIER 1.5T 3S INV AC HSU18C", price = 36490, invoiceNo = "VS-AMP-25-77812", date = LocalDate.of(2025, 8, 20),
            platform = "Vijay Sales", pin = "500016", brand = "Haier",
        ),
        Bill(
            "Hindi delivery SMS",
            """प्रिय ग्राहक, आपका ऑर्डर Redmi Note 13 5G (Arctic White, 8GB RAM) डिलीवर हो गया है। ऑर्डर आईडी 171-5582910-2231457, राशि ₹17,999. - Amazon""",
            product = "Redmi Note 13 5G (Arctic White, 8GB RAM)", price = 17999, invoiceNo = "171-5582910-2231457",
            platform = "Amazon", brand = "Redmi",
        ),
        Bill(
            "Hinglish shipped SMS with cashback clutter",
            """Shipped: 'Mi Power Bank 3i 20000mAh' will be delivered by Tomorrow. Track: amzn.in/d/2kQx. Get 5% cashback up to Rs 100 with Amazon Pay ICICI card.""",
            product = "Mi Power Bank 3i 20000mAh", platform = "Amazon", brand = "Mi",
        ),
        Bill(
            "Bank debit SMS",
            """Rs.8,499.00 debited from A/c XX1234 on 12-09-25 to AMAZON PAY INDIA. UPI Ref 525512345678. Not you? Call 18002586161 -HDFC Bank""",
            price = 8499, date = LocalDate.of(2025, 9, 12), platform = "Amazon",
        ),
        Bill(
            "JioMart delivered email",
            """Your JioMart order OM12345678901 for Philips GC1905 Steam Iron worth ₹1,795 was delivered on 03 Oct 2025 to 400076.""",
            product = "Philips GC1905 Steam Iron", price = 1795, invoiceNo = "OM12345678901", date = LocalDate.of(2025, 10, 3),
            platform = "JioMart", pin = "400076", brand = "Philips",
        ),
        Bill(
            "Amazon email with ship-to address",
            """
            Ship to: Priya Sharma, 21 MG Road, Indiranagar, Bengaluru, Karnataka 560038, Phone: 9876543210
            Order #171-2283311-0091726 placed on 2 October 2025
            Dyson V8 Absolute Cordless Vacuum Cleaner (Nickel/Yellow)
            Item subtotal: ₹29,900.00
            Grand Total: ₹29,900.00
            """.trimIndent(),
            product = "Dyson V8 Absolute Cordless Vacuum Cleaner (Nickel/Yellow)", price = 29900, invoiceNo = "171-2283311-0091726",
            date = LocalDate.of(2025, 10, 2), platform = "Amazon", pin = "560038", brand = "Dyson",
        ),
        Bill(
            "Tata CLiQ email with brand warranty line",
            """
            Thank you for shopping on Tata CLiQ.
            Order No: 1251004556677 | Order placed on Oct 11, 2025
            Product: Bosch 13 Place Settings Dishwasher SMS66GI01I
            Total payable: ₹44,990
            Includes 2 Years brand warranty.
            """.trimIndent(),
            product = "Bosch 13 Place Settings Dishwasher SMS66GI01I", price = 44990, invoiceNo = "1251004556677",
            date = LocalDate.of(2025, 10, 11), platform = "Tata CLiQ", brand = "Bosch", warrantyMonths = 24,
        ),
        Bill(
            "Telugu-English shop bill",
            """
            శ్రీ సాయి ఎలక్ట్రికల్స్ / SRI SAI ELECTRICALS
            Gandhi Nagar, Guntur - 522006
            Bill No: 2231   Date: 18-08-2025
            Crompton HS Plus 1200mm Ceiling Fan  2 x 1,850 = 3,700
            మొత్తం / Total: ₹3,700
            గ్యారంటీ / Guarantee: 2 years
            """.trimIndent(),
            product = "Crompton HS Plus 1200mm Ceiling Fan", price = 3700, invoiceNo = "2231", date = LocalDate.of(2025, 8, 18),
            pin = "522006", brand = "Crompton", warrantyMonths = 24,
        ),
    )
}
