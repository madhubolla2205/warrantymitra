@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.warrantymitra.ui

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.warrantymitra.data.Item
import com.warrantymitra.data.amcDate
import com.warrantymitra.data.purchaseDate
import com.warrantymitra.data.remindDayList
import com.warrantymitra.domain.OrderTextParser
import com.warrantymitra.domain.ParsedOrder
import kotlinx.coroutines.launch
import java.time.LocalDate

val CATEGORIES = listOf(
    "Mobile phone", "Laptop / tablet", "TV", "Refrigerator", "Washing machine", "Air conditioner", "Water purifier", "Kitchen appliance",
    "Fan / cooler", "Furniture", "Mattress", "Two-wheeler", "Car", "Inverter / battery", "Watch / wearable", "Camera", "Other",
)
val PLATFORMS = listOf("Amazon", "Flipkart", "Croma", "Reliance Digital", "Vijay Sales", "JioMart", "Tata CLiQ", "Myntra", "Brand website", "Other online")
val PAYMENTS = listOf("UPI", "Credit card", "Debit card", "EMI", "Cash", "Net banking")
val OWNERS = listOf("Me", "Amma", "Nanna", "Spouse", "Kids", "Home")
private val REMIND_CHOICES = listOf(30, 10, 7, 1)

@Composable
fun EditScreen(vm: ItemsViewModel, id: Long?, onDone: (Long) -> Unit, onCancel: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var f by remember { mutableStateOf(Item()) }
    var priceText by remember { mutableStateOf("") }
    var monthsText by remember { mutableStateOf("12") }
    var planMonthsText by remember { mutableStateOf("") }
    var pasted by remember { mutableStateOf("") }
    var note by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var locating by remember { mutableStateOf(false) }

    // A bill photo picked after typing only fills the blanks, so it never overwrites what the person entered.
    fun applyParsed(p: ParsedOrder, onlyBlanks: Boolean = false): Int {
        fun <T> take(found: T?, current: T, blank: Boolean): T = if (found != null && (!onlyBlanks || blank)) found else current
        val online = p.platform != null && f.platform.isBlank()
        f = f.copy(
            product = take(p.product, f.product, f.product.isBlank()),
            brand = take(p.brand, f.brand, f.brand.isBlank()),
            invoiceNo = take(p.invoiceNo, f.invoiceNo, f.invoiceNo.isBlank()),
            purchaseEpochDay = take(p.purchaseDate?.toEpochDay(), f.purchaseEpochDay, f.purchaseEpochDay == LocalDate.now().toEpochDay()),
            boughtOnline = if (online) true else f.boughtOnline,
            platform = take(p.platform?.takeIf { it in PLATFORMS }, f.platform, f.platform.isBlank()),
            pin = take(p.pin, f.pin, f.pin.isBlank()),
        )
        priceText = take(p.price?.toString(), priceText, priceText.isBlank())
        monthsText = take(p.warrantyMonths?.toString(), monthsText, monthsText == "12")
        return p.fieldCount
    }

    fun readPhoto(path: String, onlyBlanks: Boolean) = scope.launch {
        note = "Bill photo added. Reading it\u2026"
        val n = vm.readBill(path)?.let { applyParsed(it, onlyBlanks) } ?: 0
        note = if (n > 0) "Read $n detail${if (n > 1) "s" else ""} from the bill photo. Check them and add the rest."
        else "Bill photo added. Couldn't read details from it, so fill them in below."
    }

    LaunchedEffect(id) {
        if (id != null) {
            vm.load(id)?.let {
                f = it
                priceText = it.price?.toString() ?: ""
                monthsText = it.warrantyMonths.toString()
                planMonthsText = if (it.planMonths > 0) it.planMonths.toString() else ""
            }
        } else {
            ShareInbox.text?.let { shared ->
                ShareInbox.text = null
                pasted = shared
                val n = applyParsed(OrderTextParser.parse(shared))
                note = if (n > 0) "Filled $n field${if (n > 1) "s" else ""} from the shared message. Check them and add the rest." else "Could not find order details in the shared text."
            }
            ShareInbox.imageUri?.let { uri ->
                ShareInbox.imageUri = null
                val path = vm.importBillPhoto(uri)
                if (path != null) {
                    f = f.copy(billPhotoPath = path)
                    readPhoto(path, onlyBlanks = false)
                } else {
                    note = "Couldn't open the shared photo. Use \"Add bill photo\" below to pick it from your gallery."
                }
            }
        }
    }

    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) scope.launch {
            val path = vm.importBillPhoto(uri)
            if (path != null) {
                f = f.copy(billPhotoPath = path)
                readPhoto(path, onlyBlanks = true)
            } else note = "Couldn't save that photo. Try another one."
        }
    }
    val askLocation = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
        if (grants.values.any { it }) {
            locating = true
            fetchCurrentPlace(context) { place ->
                locating = false
                if (place == null) note = "Could not get your location. Turn on location and try again, or type the address."
                else {
                    f = f.copy(
                        storeLat = place.lat, storeLng = place.lng,
                        storeAddress = place.address ?: f.storeAddress, city = place.city ?: f.city, pin = place.pin ?: f.pin,
                    )
                    note = "Shop location saved. Check the address."
                }
            }
        } else note = "Location was not allowed. You can type the shop's address instead."
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (id == null) "Add an item" else "Edit item") },
                navigationIcon = { IconButton(onClick = onCancel) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
            )
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).imePadding().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            note?.let { Text(it, color = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.fillMaxWidth().padding(4.dp)) }

            if (id == null) SectionCard("Fill it in for me") {
                Text(
                    "Paste an order SMS or email from Amazon, Flipkart, Croma or a shop, or share it straight from Messages or Gmail. A bill photo is read too.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(pasted, { pasted = it }, label = { Text("Order SMS or email") }, minLines = 3, modifier = Modifier.fillMaxWidth())
                OutlinedButton(onClick = {
                    val n = applyParsed(OrderTextParser.parse(pasted))
                    note = if (n > 0) "Filled $n field${if (n > 1) "s" else ""}. Check them and add the rest." else "Could not find order details in this text."
                }, enabled = pasted.isNotBlank()) { Text("Fill the form from this text") }
            }

            SectionCard("Product") {
                OutlinedTextField(f.product, { f = f.copy(product = it) }, label = { Text("Product name *") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(f.brand, { f = f.copy(brand = it) }, label = { Text("Brand") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(f.model, { f = f.copy(model = it) }, label = { Text("Model") }, modifier = Modifier.weight(1f))
                }
                PickerField("Category", f.category, CATEGORIES, { f = f.copy(category = it) }, Modifier.fillMaxWidth())
                OutlinedTextField(f.serial, { f = f.copy(serial = it) }, label = { Text("Serial no. / IMEI") }, modifier = Modifier.fillMaxWidth())
                PickerField("Whose is it?", f.owner, OWNERS, { f = f.copy(owner = it) }, Modifier.fillMaxWidth())
            }

            SectionCard("Purchase and bill") {
                DateField("Purchase date *", f.purchaseDate(), { f = f.copy(purchaseEpochDay = it.toEpochDay()) }, Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        priceText, { priceText = it.filter(Char::isDigit) }, label = { Text("Price paid (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f),
                    )
                    PickerField("Paid by", f.payment, PAYMENTS, { f = f.copy(payment = it) }, Modifier.weight(1f))
                }
                OutlinedTextField(f.invoiceNo, { f = f.copy(invoiceNo = it) }, label = { Text("Invoice / order no.") }, modifier = Modifier.fillMaxWidth())
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                        Text(if (f.billPhotoPath == null) "Add bill photo" else "Change bill photo")
                    }
                    if (f.billPhotoPath != null) Text("Photo saved", style = MaterialTheme.typography.bodySmall)
                }
            }

            SectionCard("Where you bought it") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = !f.boughtOnline, onClick = { f = f.copy(boughtOnline = false) }, label = { Text("In a shop") })
                    FilterChip(selected = f.boughtOnline, onClick = { f = f.copy(boughtOnline = true) }, label = { Text("Online") })
                }
                if (f.boughtOnline) PickerField("Website or app", f.platform, PLATFORMS, { f = f.copy(platform = it) }, Modifier.fillMaxWidth())
                OutlinedTextField(f.storeName, { f = f.copy(storeName = it) }, label = { Text(if (f.boughtOnline) "Seller name" else "Shop name") }, modifier = Modifier.fillMaxWidth())
                if (!f.boughtOnline) {
                    OutlinedButton(
                        onClick = { askLocation.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) },
                        enabled = !locating,
                    ) { Text(if (locating) "Finding you…" else if (f.storeLat != null) "Update shop location (I'm at the shop)" else "I'm at the shop: save its location") }
                    if (f.storeLat != null) Text(
                        "Map pin saved: %.5f, %.5f".format(f.storeLat, f.storeLng),
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    OutlinedTextField(f.storeAddress, { f = f.copy(storeAddress = it) }, label = { Text("Shop address or area") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(
                        f.storePhone, { f = f.copy(storePhone = it) }, label = { Text("Shop phone") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth(),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(f.city, { f = f.copy(city = it) }, label = { Text(if (f.boughtOnline) "Delivered to (city)" else "City / town") }, modifier = Modifier.weight(1.4f))
                    OutlinedTextField(
                        f.pin, { f = f.copy(pin = it.filter(Char::isDigit).take(6)) }, label = { Text("PIN code") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f),
                    )
                }
                Text(
                    "The PIN code is also used to find the nearest authorised service centre.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            SectionCard("Warranty") {
                OutlinedTextField(
                    monthsText, { monthsText = it.filter(Char::isDigit).take(3) }, label = { Text("Maker's warranty (months)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(f.planProvider, { f = f.copy(planProvider = it) }, label = { Text("Extended warranty or protection plan") },
                    placeholder = { Text("Onsitego, Amazon or Flipkart protection, brand plan") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    planMonthsText, { planMonthsText = it.filter(Char::isDigit).take(3) }, label = { Text("Plan length (months)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(),
                )
                Text("Plan starts", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = !f.planStartsAtPurchase, onClick = { f = f.copy(planStartsAtPurchase = false) }, label = { Text("After maker's warranty") })
                    FilterChip(selected = f.planStartsAtPurchase, onClick = { f = f.copy(planStartsAtPurchase = true) }, label = { Text("From purchase date") })
                }
                CheckRow("Plan covers accidental or screen damage", f.planAccidental) { f = f.copy(planAccidental = it) }
                CheckRow("Plan covers liquid damage", f.planLiquid) { f = f.copy(planLiquid = it) }
                DateField("Next AMC or service due (optional)", f.amcDate(), { f = f.copy(amcEpochDay = it.toEpochDay()) }, Modifier.fillMaxWidth(), placeholder = "None")
                OutlinedTextField(
                    f.terms, { f = f.copy(terms = it) }, label = { Text("Warranty terms (from the card or website)") }, minLines = 3, modifier = Modifier.fillMaxWidth(),
                )
            }

            SectionCard("Remind me") {
                val chosen = f.remindDayList().toSet()
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    REMIND_CHOICES.forEach { n ->
                        FilterChip(
                            selected = n in chosen,
                            onClick = {
                                val next = if (n in chosen) chosen - n else chosen + n
                                f = f.copy(remindDays = next.sortedDescending().joinToString(","))
                            },
                            label = { Text("$n day${if (n > 1) "s" else ""} before") },
                        )
                    }
                }
                Text("You'll get a phone notification at 9 AM on each reminder day.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    when {
                        f.product.isBlank() -> error = "Add the product name to save."
                        f.pin.isNotEmpty() && f.pin.length != 6 -> error = "A PIN code has 6 digits."
                        f.purchaseDate().isAfter(LocalDate.now()) -> error = "The purchase date is in the future."
                        else -> {
                            val pm = planMonthsText.toIntOrNull() ?: 0
                            vm.save(
                                f.copy(
                                    product = f.product.trim(),
                                    price = priceText.toLongOrNull(),
                                    warrantyMonths = monthsText.toIntOrNull() ?: 0,
                                    planMonths = pm,
                                    platform = if (f.boughtOnline) f.platform else "",
                                ),
                                onDone,
                            )
                        }
                    }
                }) { Text(if (id == null) "Save item" else "Save changes", fontWeight = FontWeight.Medium) }
                OutlinedButton(onClick = onCancel) { Text("Cancel") }
            }
        }
    }
}

@Composable
private fun CheckRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    // The whole row toggles, so tapping the words works as well as tapping the box.
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().toggleable(value = checked, onValueChange = onChange, role = Role.Checkbox),
    ) {
        Checkbox(checked = checked, onCheckedChange = null)
        Text(label)
    }
}
