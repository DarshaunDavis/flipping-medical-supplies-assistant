package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.invoice

import android.content.Context
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class PdfDocumentAdapter(
    private val context: Context,
    private val pdfPath: String
) : PrintDocumentAdapter() {

    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes,
        cancellationSignal: CancellationSignal,
        callback: LayoutResultCallback,
        extras: Bundle?
    ) {
        if (cancellationSignal.isCanceled) {
            callback.onLayoutCancelled()
            return
        }
        val pdi = PrintDocumentInfo
            .Builder(File(pdfPath).name)
            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
            .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
            .build()
        callback.onLayoutFinished(pdi, true)
    }

    override fun onWrite(
        pages: Array<PageRange>,
        destination: ParcelFileDescriptor,
        cancellationSignal: CancellationSignal,
        callback: WriteResultCallback
    ) {
        if (cancellationSignal.isCanceled) {
            callback.onWriteCancelled()
            return
        }
        FileInputStream(pdfPath).use { input ->
            FileOutputStream(destination.fileDescriptor).use { output ->
                input.copyTo(output)
            }
        }
        callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
    }
}
