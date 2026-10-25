package com.warrantymitra.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.warrantymitra.domain.CoverageStatus
import com.warrantymitra.domain.formatDate
import com.warrantymitra.ui.theme.LocalStatusColors
import java.time.LocalDate

@Composable
fun Pill(text: String, fg: Color, bg: Color, modifier: Modifier = Modifier) {
    Text(
        text, color = fg, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium,
        modifier = modifier.background(bg, RoundedCornerShape(50)).padding(horizontal = 10.dp, vertical = 3.dp),
    )
}

@Composable
fun StatusPill(status: CoverageStatus, label: String) {
    val s = LocalStatusColors.current
    when (status) {
        CoverageStatus.COVERED -> Pill(label, s.ok, s.okBg)
        CoverageStatus.ENDING_SOON -> Pill(label, s.warn, s.warnBg)
        CoverageStatus.EXPIRED -> Pill(label, s.bad, s.badBg)
    }
}

@Composable
fun TagPill(text: String) = Pill(text, MaterialTheme.colorScheme.onSurfaceVariant, MaterialTheme.colorScheme.surfaceVariant)

@Composable
fun SampleTag() = Pill("Sample", MaterialTheme.colorScheme.onSecondaryContainer, MaterialTheme.colorScheme.secondaryContainer)

@Composable
fun SectionCard(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            content()
        }
    }
}

/** A read-only field that opens a menu of choices. */
@Composable
fun PickerField(label: String, value: String, options: List<String>, onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    var open by remember { mutableStateOf(false) }
    Box(modifier) {
        OutlinedTextField(value = value, onValueChange = {}, readOnly = true, label = { Text(label) }, modifier = Modifier.fillMaxWidth())
        Box(Modifier.matchParentSize().clickable { open = true })
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            options.forEach { o -> DropdownMenuItem(text = { Text(o) }, onClick = { onSelect(o); open = false }) }
        }
    }
}

/** A date field that opens the system date picker. */
@Composable
fun DateField(label: String, date: LocalDate?, onPick: (LocalDate) -> Unit, modifier: Modifier = Modifier, placeholder: String = "Choose a date") {
    val context = LocalContext.current
    Box(modifier) {
        OutlinedTextField(
            value = date?.let(::formatDate) ?: "", onValueChange = {}, readOnly = true,
            label = { Text(label) }, placeholder = { Text(placeholder) }, modifier = Modifier.fillMaxWidth(),
        )
        // The tap target sits over the field, so it has to say what the field holds or screen readers announce nothing.
        val shown = date?.let(::formatDate) ?: placeholder
        Box(Modifier.matchParentSize().semantics { contentDescription = "$label: $shown" }.clickable(onClickLabel = "Change date") {
            val d = date ?: LocalDate.now()
            DatePickerDialog(context, { _, y, m, day -> onPick(LocalDate.of(y, m + 1, day)) }, d.year, d.monthValue - 1, d.dayOfMonth).show()
        })
    }
}
