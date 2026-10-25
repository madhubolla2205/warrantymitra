package com.warrantymitra.data

import java.time.LocalDate

/** Example items for trying the app. Each one is marked as a sample and can be deleted. */
fun sampleItems(today: LocalDate = LocalDate.now()): List<Item> {
    fun ago(months: Long, days: Long = 0) = today.minusMonths(months).minusDays(days).toEpochDay()
    return listOf(
        Item(
            product = "Galaxy S24 FE", brand = "Samsung", category = "Mobile phone", model = "SM-S721B", serial = "35••••••••4821",
            purchaseEpochDay = ago(11, 12), warrantyMonths = 12, price = 35999, invoiceNo = "FAAB2K24001738", payment = "Credit card",
            planProvider = "Flipkart Complete Mobile Protection", planMonths = 12, planStartsAtPurchase = true, planAccidental = true, planLiquid = true,
            boughtOnline = true, platform = "Flipkart", storeName = "RetailNet", city = "Hyderabad", pin = "500081",
            terms = "The warranty covers manufacturing defects for 12 months from the date of purchase. Physical damage, liquid damage, and damage due to unauthorised repair are not covered. Battery is covered for 6 months.",
            isSample = true,
        ),
        Item(
            product = "Front-load washing machine 7 kg", brand = "LG", category = "Washing machine", model = "FHM1207SDW", serial = "304KWXX0Q912",
            owner = "Amma", purchaseEpochDay = ago(20), warrantyMonths = 24, price = 31490, invoiceNo = "CR/HYD/24-25/08817", payment = "EMI",
            planProvider = "Onsitego extended warranty", planMonths = 24, remindDays = "30,10",
            storeName = "Croma", storeAddress = "Inorbit Mall, Madhapur", city = "Hyderabad", pin = "500081",
            terms = "Motor carries 10 years warranty. Damage caused by voltage fluctuation, rodents, or improper installation is excluded.",
            isSample = true,
        ),
        Item(
            product = "RO + UV water purifier", brand = "Kent", category = "Water purifier", model = "Grand Plus", owner = "Home",
            purchaseEpochDay = ago(14), warrantyMonths = 12, amcEpochDay = today.plusDays(18).toEpochDay(), price = 15500, invoiceNo = "SLE-2291",
            payment = "UPI", storeName = "Sri Lakshmi Electronics", storeAddress = "Main Road, Ameerpet", city = "Hyderabad", pin = "500016",
            remindDays = "10", terms = "Filters, membrane and consumables are not covered under warranty.", isSample = true,
        ),
        Item(
            product = "Ceiling fan 1200 mm", brand = "Atomberg", category = "Fan / cooler", model = "Renesa", owner = "Nanna",
            purchaseEpochDay = ago(22), warrantyMonths = 24, price = 3799, invoiceNo = "402-1188221-7712345", payment = "Debit card",
            boughtOnline = true, platform = "Amazon", storeName = "Appario Retail", city = "Vijayawada", pin = "520010", remindDays = "30,10",
            isSample = true,
        ),
    )
}
