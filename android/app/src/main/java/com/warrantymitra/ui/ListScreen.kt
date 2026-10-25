@file:OptIn(ExperimentalMaterial3Api::class)

package com.warrantymitra.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.warrantymitra.data.Item
import com.warrantymitra.data.amcDate
import com.warrantymitra.data.coverage
import com.warrantymitra.data.hasOpenClaim
import com.warrantymitra.data.whereBought
import com.warrantymitra.domain.CoverageStatus
import com.warrantymitra.domain.formatDate
import com.warrantymitra.domain.formatRupees
import com.warrantymitra.domain.status
import com.warrantymitra.domain.statusLabel
import com.warrantymitra.ui.theme.LocalStatusColors
import java.time.LocalDate
import java.time.temporal.ChronoUnit

private enum class Filter(val label: String) { ALL("All"), SOON("Ending soon"), EXPIRED("Expired"), AMC("AMC"), CLAIMS("Claims") }

@Composable
fun ListScreen(vm: ItemsViewModel, onOpen: (Long) -> Unit, onAdd: () -> Unit) {
    val items by vm.items.collectAsState()
    var filter by rememberSaveable { mutableStateOf("ALL") }
    var menu by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val backup = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) vm.exportBackup(uri) { ok -> Toast.makeText(context, if (ok) "Backup saved" else "Could not save the backup", Toast.LENGTH_SHORT).show() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("WarrantyMitra", fontWeight = FontWeight.Bold)
                        Text("Is it still covered?", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { menu = true }) { Icon(Icons.Filled.MoreVert, contentDescription = "More") }
                        DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                            DropdownMenuItem(text = { Text("Back up to a file") }, onClick = {
                                menu = false; backup.launch("warrantymitra-backup-${LocalDate.now()}.json")
                            })
                            DropdownMenuItem(text = { Text("Add sample items") }, onClick = { menu = false; vm.addSamples() })
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            if (!items.isNullOrEmpty()) ExtendedFloatingActionButton(onClick = onAdd, icon = { Icon(Icons.Filled.Add, null) }, text = { Text("Add item") })
        },
    ) { padding ->
        val list = items
        when {
            list == null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            list.isEmpty() -> EmptyState(Modifier.padding(padding), onAdd, vm::addSamples)
            else -> ItemList(list, Filter.valueOf(filter), { filter = it.name }, onOpen, padding)
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier, onAdd: () -> Unit, onSamples: () -> Unit) {
    Column(modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Your warranty folder is empty", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            "Add a product with its bill, purchase date, the shop you bought it from, and how long the warranty runs. " +
                "WarrantyMitra counts down to the end date, reminds you before it lapses, and tells you whether a repair should be free.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            "Tip: share an order SMS or email from Messages or Gmail to WarrantyMitra and the form fills itself.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onAdd) { Text("Add your first item") }
            OutlinedButton(onClick = onSamples) { Text("Try 4 sample items") }
        }
    }
}

@Composable
private fun ItemList(all: List<Item>, filter: Filter, onFilter: (Filter) -> Unit, onOpen: (Long) -> Unit, padding: PaddingValues) {
    val today = LocalDate.now()
    val withCov = all.map { it to it.coverage(today) }
        .sortedBy { (_, c) -> if (c.daysLeft < 0) 1_000_000 - c.daysLeft else c.daysLeft }
    val active = withCov.filter { it.second.daysLeft >= 0 }
    val soon = active.filter { it.second.status() == CoverageStatus.ENDING_SOON }
    val value = active.sumOf { it.first.price ?: 0L }
    val claims = all.count { it.hasOpenClaim() }
    val shown = withCov.filter { (it, c) ->
        when (filter) {
            Filter.ALL -> true
            Filter.SOON -> c.status() == CoverageStatus.ENDING_SOON
            Filter.EXPIRED -> c.daysLeft < 0
            Filter.AMC -> it.amcEpochDay != null
            Filter.CLAIMS -> it.hasOpenClaim()
        }
    }
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = padding.calculateTopPadding() + 4.dp, bottom = padding.calculateBottomPadding() + 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Stat("Under warranty", formatRupees(value), "${active.size} of ${all.size} items", Modifier.weight(1f))
                Stat("Ending in 30 days", soon.size.toString(), soon.firstOrNull()?.first?.product ?: "Nothing urgent", Modifier.weight(1f))
                Stat("Open claims", claims.toString(), if (claims > 0) "In progress" else "None", Modifier.weight(1f))
            }
        }
        item {
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Filter.entries.forEach { f -> FilterChip(selected = f == filter, onClick = { onFilter(f) }, label = { Text(f.label) }) }
            }
        }
        if (shown.isEmpty()) item { Text("No items match this filter.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        items(shown, key = { it.first.id }) { (it, c) -> ItemCard(it, c, today) { onOpen(it.id) } }
    }
}

@Composable
private fun Stat(label: String, value: String, sub: String, modifier: Modifier) {
    Card(
        modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(Modifier.padding(10.dp)) {
            Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
        }
    }
}

@Composable
private fun ItemCard(it: Item, c: com.warrantymitra.domain.Coverage, today: LocalDate, onClick: () -> Unit) {
    val s = LocalStatusColors.current
    val st = c.status()
    Card(
        onClick = onClick, modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(it.product.ifBlank { "Untitled item" }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    val sub = listOf(it.brand, it.model).filter(String::isNotBlank).joinToString(" · ")
                    val where = it.whereBought()
                    Text(
                        listOf(sub, if (where.isNotBlank()) "Bought at $where" else "").filter(String::isNotBlank).joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    StatusPill(st, c.statusLabel())
                    it.amcDate()?.let { amc ->
                        val d = ChronoUnit.DAYS.between(today, amc)
                        if (d in 0..30) com.warrantymitra.ui.Pill("AMC ${formatDate(amc)}", s.warn, s.warnBg)
                    }
                    if (it.hasOpenClaim()) TagPill("Claim: ${it.claimStatus}")
                    if (it.owner != "Me") TagPill(it.owner)
                    if (it.isSample) SampleTag()
                }
            }
            Text(
                "Bought ${formatDate(c.purchase)} · Covered till ${formatDate(c.end)}${if (c.planEnd != null) " (incl. plan)" else ""}",
                style = MaterialTheme.typography.bodySmall,
            )
            LinearProgressIndicator(
                progress = { c.usedFraction },
                color = when (st) { CoverageStatus.COVERED -> MaterialTheme.colorScheme.primary; CoverageStatus.ENDING_SOON -> s.warn; CoverageStatus.EXPIRED -> s.bad },
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth().height(6.dp).padding(top = 2.dp),
            )
        }
    }
}
