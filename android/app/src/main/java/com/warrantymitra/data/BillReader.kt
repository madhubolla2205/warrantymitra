package com.warrantymitra.data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.warrantymitra.BuildConfig
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.warrantymitra.domain.OcrLine
import com.warrantymitra.domain.ocrRows
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume

/** Reads the printed text on a bill photo, on the phone itself, so the photo never leaves the device. Null if nothing could be read. */
suspend fun readBillText(context: Context, photo: File): String? {
    val image = runCatching { InputImage.fromFilePath(context, Uri.fromFile(photo)) }.getOrNull() ?: return null
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    val result: Text? = suspendCancellableCoroutine { cont ->
        recognizer.process(image)
            .addOnSuccessListener { cont.resume(it) }
            .addOnFailureListener { cont.resume(null) }
    }
    recognizer.close()
    val lines = result?.textBlocks.orEmpty().flatMap { it.lines }.mapNotNull { line ->
        line.boundingBox?.let { OcrLine(line.text, it.left, it.top, it.right, it.bottom) }
    }
    return ocrRows(lines).ifBlank { null }.also { if (BuildConfig.DEBUG) Log.d("BillReader", it ?: "(nothing read)") }
}
