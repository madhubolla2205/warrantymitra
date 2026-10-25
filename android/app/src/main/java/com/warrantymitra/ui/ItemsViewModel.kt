package com.warrantymitra.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.warrantymitra.data.AppDatabase
import com.warrantymitra.data.Item
import com.warrantymitra.data.sampleItems
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class ItemsViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.get(app).items()

    /** null until the database has answered, so the list can tell "loading" from "empty". */
    val items: StateFlow<List<Item>?> = dao.observeAll().map<List<Item>, List<Item>?> { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun observe(id: Long): Flow<Item?> = dao.observe(id)

    suspend fun load(id: Long): Item? = dao.get(id)

    fun save(item: Item, onSaved: (Long) -> Unit) = viewModelScope.launch {
        val rowId = dao.upsert(item)
        onSaved(if (item.id != 0L) item.id else rowId)
    }

    fun delete(item: Item) = viewModelScope.launch {
        dao.delete(item)
        item.billPhotoPath?.let { runCatching { File(it).delete() } }
    }

    fun addSamples() = viewModelScope.launch { sampleItems().forEach { dao.upsert(it) } }

    /** Copies a picked or shared bill photo into app storage so it survives the source being deleted. */
    suspend fun importBillPhoto(uri: Uri): String? = withContext(Dispatchers.IO) {
        val ctx = getApplication<Application>()
        runCatching {
            val dir = File(ctx.filesDir, "bills").apply { mkdirs() }
            val out = File(dir, "bill-${System.currentTimeMillis()}.jpg")
            ctx.contentResolver.openInputStream(uri)!!.use { input -> out.outputStream().use { input.copyTo(it) } }
            out.absolutePath
        }.getOrNull()
    }

    /** Writes every item as JSON to a file the person chose, for backup. */
    fun exportBackup(target: Uri, onDone: (Boolean) -> Unit) = viewModelScope.launch {
        val ok = withContext(Dispatchers.IO) {
            runCatching {
                val arr = JSONArray()
                dao.getAll().forEach { arr.put(it.toJson()) }
                getApplication<Application>().contentResolver.openOutputStream(target)!!.use { it.write(arr.toString(2).toByteArray()) }
            }.isSuccess
        }
        onDone(ok)
    }
}

private fun Item.toJson() = JSONObject().apply {
    put("product", product); put("brand", brand); put("category", category); put("model", model); put("serial", serial); put("owner", owner)
    put("purchaseDate", java.time.LocalDate.ofEpochDay(purchaseEpochDay).toString()); put("price", price ?: JSONObject.NULL)
    put("invoiceNo", invoiceNo); put("payment", payment); put("warrantyMonths", warrantyMonths)
    put("planProvider", planProvider); put("planMonths", planMonths); put("planStartsAtPurchase", planStartsAtPurchase)
    put("planAccidental", planAccidental); put("planLiquid", planLiquid)
    put("amcDate", amcEpochDay?.let { java.time.LocalDate.ofEpochDay(it).toString() } ?: JSONObject.NULL)
    put("boughtOnline", boughtOnline); put("platform", platform); put("storeName", storeName); put("storeAddress", storeAddress)
    put("city", city); put("pin", pin); put("storePhone", storePhone)
    put("storeLat", storeLat ?: JSONObject.NULL); put("storeLng", storeLng ?: JSONObject.NULL)
    put("remindDays", remindDays); put("terms", terms)
    put("claimStatus", claimStatus); put("claimTicket", claimTicket); put("claimCentre", claimCentre); put("claimNotes", claimNotes)
}
