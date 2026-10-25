@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.warrantymitra.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.warrantymitra.data.Item
import com.warrantymitra.data.amcDate
import com.warrantymitra.data.coverage
import com.warrantymitra.data.plan
import com.warrantymitra.data.remindDayList
import com.warrantymitra.domain.Coverage
import com.warrantymitra.domain.DamageCheck
import com.warrantymitra.domain.Problem
import com.warrantymitra.domain.Verdict
import com.warrantymitra.domain.VerdictKind
import com.warrantymitra.domain.formatDate
import com.warrantymitra.domain.formatRupees
import com.warrantymitra.domain.status
import com.warrantymitra.domain.statusLabel
import com.warrantymitra.ui.theme.LocalStatusColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.temporal.ChronoUnit

private val CLAIM_STATUSES = listOf(
    "No claim", "Complaint registered", "Technician visit booked", "Pickup done", "Under repair", "Waiting for parts", "Rejected", "Closed",
)

@Composable
fun DetailScreen(vm: ItemsViewModel, id: Long, onBack: () -> Unit, onEdit: (Long) -> Unit) {
    val item by remember(id) { vm.observe(id) }.collectAsState(initial = null)
    var confirmDelete by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(item?.product ?: "", maxLines = 1) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                actions = {
                    TextButton(onClick = { onEdit(id) }, enabled = item != null) { Text("Edit") }
                    TextButton(onClick = { confirmDelete = true }, enabled = item != null) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                },
            )
        },
    ) { padding ->
        val it = item
        if (it == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            val c = it.coverage()
            Column(
                Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Header(it, c)
                CoverageCard(it, c)
                StoreCard(it)
                RemindersCard(it, c)
                DamageCard(it, c)
                ClaimCard(it) { updated -> vm.save(updated) {} }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete ${item?.product}?") },
            text = { Text("The item, its bill photo and claim notes are removed from this phone.") },
            confirmButton = { TextButton(onClick = { item?.let(vm::delete); confirmDelete = false; onBack() }) { Text("Delete", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Keep") } },
        )
    }
}

@Composable
private fun Header(it: Item, c: Coverage) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(it.product, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        val sub = listOf(it.brand, it.model, it.category).filter(String::isNotBlank).joinToString(" · ") +
            if (it.owner != "Me") " · ${it.owner}'s" else ""
        Text(sub, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            StatusPill(c.status(), c.statusLabel())
            if (it.isSample) SampleTag()
        }
    }
}

@Composable
private fun CoverageCard(it: Item, c: Coverage) {
    SectionCard("Coverage") {
        Text(
            if (c.daysLeft >= 0) "Covered for ${c.daysLeft} more day${if (c.daysLeft == 1L) "" else "s"}, until ${formatDate(c.end)}."
            else "Cover ended on ${formatDate(c.end)}, ${-c.daysLeft} days ago.",
            fontWeight = FontWeight.Medium,
        )
        Bar("Maker's warranty", "${formatDate(c.purchase)} to ${formatDate(c.makerEnd)}", fraction(c.purchase, c.makerEnd), MaterialTheme.colorScheme.primary)
        if (c.planStart != null && c.planEnd != null) {
            Bar(it.planProvider.ifBlank { "Plan" }, "${formatDate(c.planStart)} to ${formatDate(c.planEnd)}", fraction(c.planStart, c.planEnd), MaterialTheme.colorScheme.secondary)
        }
        it.amcDate()?.let { amc ->
            val d = ChronoUnit.DAYS.between(LocalDate.now(), amc)
            Text(if (d >= 0) "Next AMC or service due on ${formatDate(amc)} ($d days)." else "AMC or service was due on ${formatDate(amc)}.")
        }
    }
}

private fun fraction(start: LocalDate, end: LocalDate): Float {
    val span = maxOf(1L, ChronoUnit.DAYS.between(start, end))
    return (ChronoUnit.DAYS.between(start, LocalDate.now()).toFloat() / span).coerceIn(0f, 1f)
}

@Composable
private fun Bar(label: String, range: String, used: Float, color: androidx.compose.ui.graphics.Color) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        LinearProgressIndicator(progress = { used }, color = color, trackColor = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth())
        Text(range, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun KV(k: String, v: String) {
    if (v.isBlank()) return
    Row {
        Text(k, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(120.dp))
        Text(v, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StoreCard(it: Item) {
    val context = LocalContext.current
    SectionCard("Purchase and store") {
        KV("Bought on", formatDate(LocalDate.ofEpochDay(it.purchaseEpochDay)))
        KV("Price", it.price?.let(::formatRupees) ?: "")
        KV("Paid by", it.payment)
        KV("Invoice / order", it.invoiceNo)
        KV("Serial / IMEI", it.serial)
        if (it.boughtOnline) {
            KV("Website", it.platform.ifBlank { "Online" })
            KV("Seller", it.storeName)
            KV("Delivered to", listOf(it.city, it.pin).filter(String::isNotBlank).joinToString(" "))
        } else {
            KV("Shop", it.storeName)
            KV("Location", listOf(it.storeAddress, it.city, it.pin).filter(String::isNotBlank).joinToString(", "))
            KV("Shop phone", it.storePhone)
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (!it.boughtOnline) {
                val query = listOf(it.storeName, it.storeAddress, it.city, it.pin).filter(String::isNotBlank).joinToString(", ")
                if (it.storeLat != null && it.storeLng != null) OutlinedButton(onClick = { context.openMapAt(it.storeLat, it.storeLng, it.storeName.ifBlank { "Shop" }) }) { Text("Open shop in Maps") }
                else if (query.isNotBlank()) OutlinedButton(onClick = { context.mapsSearch(query) }) { Text("Find shop in Maps") }
                if (it.storePhone.isNotBlank()) OutlinedButton(onClick = { context.dial(it.storePhone) }) { Text("Call shop") }
            } else {
                ORDER_LINKS[it.platform]?.let { url -> OutlinedButton(onClick = { context.openUrl(url) }) { Text("Open ${it.platform} orders") } }
            }
        }
        BillPhoto(it.billPhotoPath)
    }
}

@Composable
private fun BillPhoto(path: String?) {
    if (path == null) {
        Text("No bill photo saved yet. Tap Edit to add one.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }
    val bitmap by produceState<ImageBitmap?>(null, path) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeFile(path, bounds)
                var sample = 1
                while (bounds.outWidth / sample > 1400) sample *= 2
                BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample })?.asImageBitmap()
            }.getOrNull()
        }
    }
    bitmap?.let {
        Image(
            it, contentDescription = "Bill photo", contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxWidth().heightIn(max = 320.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
        )
    }
}

@Composable
private fun RemindersCard(it: Item, c: Coverage) {
    val context = LocalContext.current
    SectionCard("Reminders") {
        val days = it.remindDayList().sortedDescending()
        if (days.isEmpty()) Text("No reminders set. Tap Edit to choose some.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        days.forEach { n ->
            val d = c.end.minusDays(n.toLong())
            Text("${formatDate(d)} · $n day${if (n > 1) "s" else ""} before it ends${if (d.isBefore(LocalDate.now())) " (passed)" else ""}")
        }
        OutlinedButton(onClick = {
            val where = if (it.boughtOnline) it.platform.ifBlank { "online" } else it.storeName.ifBlank { "a shop" }
            context.shareText(
                "Reminder: the warranty for ${it.product}${if (it.brand.isNotBlank()) " (${it.brand})" else ""} ends on ${formatDate(c.end)}. " +
                    "Bought from $where${if (it.invoiceNo.isNotBlank()) ", invoice ${it.invoiceNo}" else ""}.",
            )
        }) { Text("Share reminder (WhatsApp, SMS)") }
    }
}

@Composable
private fun DamageCard(it: Item, c: Coverage) {
    var problem by rememberSaveable { mutableStateOf<String?>(null) }
    SectionCard("Something broke? Check if it's covered") {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Problem.entries.forEach { p -> FilterChip(selected = problem == p.name, onClick = { problem = p.name }, label = { Text(p.label) }) }
        }
        problem?.let { name ->
            val v = DamageCheck.check(Problem.valueOf(name), c, it.plan(), ::formatDate)
            VerdictBox(v, it)
            NextSteps(it, c, v)
        }
    }
}

@Composable
private fun VerdictBox(v: Verdict, it: Item) {
    val s = LocalStatusColors.current
    val (fg, bg) = when (v.kind) {
        VerdictKind.LIKELY_COVERED -> s.ok to s.okBg
        VerdictKind.UNCLEAR -> s.warn to s.warnBg
        VerdictKind.LIKELY_NOT_COVERED -> s.bad to s.badBg
    }
    Column(
        Modifier.fillMaxWidth().background(bg, RoundedCornerShape(10.dp)).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(v.title, color = fg, fontWeight = FontWeight.Bold)
        Text(v.reason, style = MaterialTheme.typography.bodyMedium)
        if (it.terms.isNotBlank()) {
            Text("Your saved terms", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(it.terms, style = MaterialTheme.typography.bodySmall)
        }
        Text(
            "This is guidance, not a guarantee. The brand or service centre makes the final decision after inspection.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun NextSteps(it: Item, c: Coverage, v: Verdict) {
    val context = LocalContext.current
    val brand = it.brand.ifBlank { it.product }
    val near = it.pin.ifBlank { it.city }
    val daysSince = ChronoUnit.DAYS.between(c.purchase, LocalDate.now())
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("What to do next", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        if (it.boughtOnline && daysSince <= 10) {
            Step("You bought this $daysSince days ago. ${it.platform.ifBlank { "The site" }} may still accept a return or replacement, which is faster than a repair.")
            ORDER_LINKS[it.platform]?.let { url -> OutlinedButton(onClick = { context.openUrl(url) }) { Text("Open your orders") } }
        }
        if (v.claimWithPlan) Step("Raise the claim with ${it.planProvider.ifBlank { "your plan provider" }}, since the maker's warranty has ended.")
        else {
            Step("Register a complaint with $brand and note the complaint number in the tracker below.")
            OutlinedButton(onClick = { context.openUrl("https://www.google.com/search?q=" + android.net.Uri.encode("$brand official customer care India register complaint")) }) {
                Text("Find $brand customer care")
            }
        }
        Step("Go to an authorised service centre. Unauthorised repairs can void the warranty.")
        OutlinedButton(onClick = { context.mapsSearch("$brand authorised service centre $near".trim()) }) {
            Text(if (near.isNotBlank()) "Service centres near $near" else "Find service centres")
        }
        Step(
            "Carry the bill${if (it.invoiceNo.isNotBlank()) " (${it.invoiceNo})" else ""}, the warranty card, " +
                (if (it.serial.isNotBlank()) "serial / IMEI ${it.serial}, " else "the serial number or IMEI, ") +
                (if (it.planProvider.isNotBlank()) "your plan certificate, " else "") + "and photos of the problem.",
        )
        if (!it.boughtOnline && it.storeName.isNotBlank()) {
            Step("${it.storeName} can often book the repair for you.")
            if (it.storePhone.isNotBlank()) OutlinedButton(onClick = { context.dial(it.storePhone) }) { Text("Call ${it.storeName}") }
        }
        Step("If a covered repair is refused or delayed, call the National Consumer Helpline on 1915 or file a complaint online. A formal case can be filed on e-Daakhil.")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            OutlinedButton(onClick = { context.dial("1915") }) { Text("Call 1915") }
            OutlinedButton(onClick = { context.openUrl("https://consumerhelpline.gov.in") }) { Text("consumerhelpline.gov.in") }
            OutlinedButton(onClick = { context.openUrl("https://edaakhil.nic.in") }) { Text("e-Daakhil") }
        }
    }
}

@Composable
private fun Step(text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("•", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
        Text(text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ClaimCard(it: Item, onSave: (Item) -> Unit) {
    var status by remember(it.id) { mutableStateOf(it.claimStatus.ifBlank { "No claim" }) }
    var ticket by remember(it.id) { mutableStateOf(it.claimTicket) }
    var centre by remember(it.id) { mutableStateOf(it.claimCentre) }
    var notes by remember(it.id) { mutableStateOf(it.claimNotes) }
    var saved by remember { mutableStateOf(false) }
    LaunchedEffect(status, ticket, centre, notes) { saved = false }
    SectionCard("Repair or claim tracker") {
        PickerField("Status", status, CLAIM_STATUSES, { status = it }, Modifier.fillMaxWidth())
        OutlinedTextField(ticket, { ticket = it }, label = { Text("Complaint / ticket no.") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(centre, { centre = it }, label = { Text("Service centre") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(notes, { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = {
                onSave(
                    it.copy(
                        claimStatus = if (status == "No claim") "" else status,
                        claimTicket = ticket.trim(), claimCentre = centre.trim(), claimNotes = notes.trim(),
                        claimUpdatedAt = System.currentTimeMillis(),
                    ),
                )
                saved = true
            }) { Text("Save claim") }
            if (saved) Text("Saved", color = LocalStatusColors.current.ok)
            else it.claimUpdatedAt?.let { t ->
                Text(
                    "Updated ${formatDate(java.time.Instant.ofEpochMilli(t).atZone(java.time.ZoneId.systemDefault()).toLocalDate())}",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
