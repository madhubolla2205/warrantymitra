package com.warrantymitra.domain

/** One line of text the on-phone reader found on a bill photo, with its box in image pixels. */
data class OcrLine(val text: String, val left: Int, val top: Int, val right: Int, val bottom: Int) {
    val centerY: Int get() = (top + bottom) / 2
    val height: Int get() = bottom - top
}

/**
 * Puts the reader's lines back into the bill's rows. The reader returns columns as separate blocks, so
 * "Grand Total" and "3200.00" come back far apart; joined by row they read "Grand Total   3200.00" again,
 * with wide gaps kept between columns so the parser can tell the item name from the qty and price.
 */
fun ocrRows(lines: List<OcrLine>): String {
    if (lines.isEmpty()) return ""
    val typical = lines.map { it.height }.sorted()[lines.size / 2].coerceAtLeast(1)
    val rows = mutableListOf<MutableList<OcrLine>>()
    for (line in lines.sortedBy { it.centerY }) {
        val row = rows.lastOrNull()
        if (row != null && line.centerY - row.map { it.centerY }.average() < typical * 0.6) row += line else rows += mutableListOf(line)
    }
    return rows.joinToString("\n") { row -> row.sortedBy { it.left }.joinToString("   ") { it.text.trim() } }
}
