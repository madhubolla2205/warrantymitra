package com.warrantymitra.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "items")
data class Item(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val product: String = "",
    val brand: String = "",
    val category: String = "Other",
    val model: String = "",
    val serial: String = "",
    /** Whose item it is in the household: Me, Amma, Nanna and so on. */
    val owner: String = "Me",

    val purchaseEpochDay: Long = LocalDate.now().toEpochDay(),
    val price: Long? = null,
    val invoiceNo: String = "",
    val payment: String = "",

    val warrantyMonths: Int = 12,
    val planProvider: String = "",
    val planMonths: Int = 0,
    val planStartsAtPurchase: Boolean = false,
    val planAccidental: Boolean = false,
    val planLiquid: Boolean = false,
    val amcEpochDay: Long? = null,
    val terms: String = "",

    // Where it was bought
    val boughtOnline: Boolean = false,
    val platform: String = "",
    val storeName: String = "",
    val storeAddress: String = "",
    val city: String = "",
    val pin: String = "",
    val storePhone: String = "",
    val storeLat: Double? = null,
    val storeLng: Double? = null,

    /** Comma-separated days before the end of cover, e.g. "30,10,1". */
    val remindDays: String = "30,10,1",
    val billPhotoPath: String? = null,

    val claimStatus: String = "",
    val claimTicket: String = "",
    val claimCentre: String = "",
    val claimNotes: String = "",
    val claimUpdatedAt: Long? = null,

    val isSample: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)
