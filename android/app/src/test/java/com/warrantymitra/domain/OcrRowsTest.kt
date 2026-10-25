package com.warrantymitra.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class OcrRowsTest {
    @Test fun columnsReadBackAsRows() {
        // The reader returns the description column, then the amount column.
        val lines = listOf(
            OcrLine("SRI VENKATESWARA ELECTRONICS", 40, 10, 600, 40),
            OcrLine("Bajaj Rex 500W Mixer Grinder", 40, 200, 420, 228),
            OcrLine("Grand Total", 300, 300, 450, 330),
            OcrLine("2711.86", 700, 203, 800, 230),
            OcrLine("3200.00", 700, 298, 800, 327),
        )
        assertEquals(
            "SRI VENKATESWARA ELECTRONICS\nBajaj Rex 500W Mixer Grinder   2711.86\nGrand Total   3200.00",
            ocrRows(lines),
        )
        assertEquals(3200L, OrderTextParser.parse(ocrRows(lines)).price)
    }

    @Test fun nothingRead() = assertEquals("", ocrRows(emptyList()))
}
