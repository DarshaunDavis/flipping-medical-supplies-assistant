package com.tundynamcorp.flippingmedicalsuppliesassistant.ui.invoice

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Environment
import androidx.core.graphics.toColorInt
import androidx.core.graphics.scale
import com.tundynamcorp.flippingmedicalsuppliesassistant.R
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object InvoicePdfGenerator {

    fun generate(
        context: Context,
        seller: SellerInfo,
        meta: InvoiceMeta,
        lines: List<InvoiceLine>,
        invoiceDate: Date = Date()
    ): File {
        // 1️⃣ Create PDF and page
        val pdf = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdf.startPage(pageInfo)
        val c = page.canvas

        // 2️⃣ Paints
        val titleP  = Paint().apply { typeface = Typeface.DEFAULT_BOLD; textSize = 28f }
        val subP    = Paint().apply { typeface = Typeface.DEFAULT; textSize = 12f; color = Color.DKGRAY }
        val headerP = Paint().apply { typeface = Typeface.DEFAULT_BOLD; textSize = 14f }
        val bodyP   = Paint().apply { typeface = Typeface.DEFAULT; textSize = 12f }
        val totalP  = Paint().apply { typeface = Typeface.DEFAULT_BOLD; textSize = 16f }
        val lineP   = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }
        val stripeP = Paint().apply { color = "#F7F7F7".toColorInt() }

        // Helper to format phone
        fun formatPhone(raw: String): String {
            val d = raw.filter(Char::isDigit)
            return if (d.length == 10) "(${d.substring(0,3)}) ${d.substring(3,6)}-${d.substring(6)}" else raw
        }

        // 3️⃣ Seller block, start lower to give breathing room
        val topMargin = 80f
        var y = topMargin

        c.drawText(seller.name, 40f, y, titleP)
        y += titleP.textSize + 8f

        seller.dba?.takeIf(String::isNotBlank)?.let {
            c.drawText(it, 40f, y, bodyP)
            y += bodyP.textSize + 4f
        }

        val addrLine = seller.address1 + seller.address2?.let { ", $it" }.orEmpty()
        c.drawText(addrLine, 40f, y, bodyP)
        y += bodyP.textSize + 4f

        c.drawText("${seller.city}, ${seller.state} ${seller.zip}", 40f, y, bodyP)
        y += bodyP.textSize + 4f

        c.drawText(formatPhone(seller.phone), 40f, y, bodyP)
        y += bodyP.textSize + 8f

        // 4️⃣ Determine invoice title position just below seller block
        val invoiceY = y + 16f

        // 5️⃣ Draw logo using direct resource reference and scale extension
        val bmp = BitmapFactory.decodeResource(context.resources, R.drawable.fmsalogo)
        val scaledBmp = bmp.scale(80, 80)
        val logoHeight = scaledBmp.height.toFloat()
        val logoY = topMargin + ((invoiceY - topMargin) - logoHeight) / 2f
        c.drawBitmap(scaledBmp, 495f, logoY, null)

        // 6️⃣ Invoice title & date
        c.drawText("Invoice", 40f, invoiceY, titleP)
        c.drawText(
            "Submitted on ${SimpleDateFormat("MM/dd/yyyy", Locale.US).format(invoiceDate)}",
            40f, invoiceY + titleP.textSize + 4f, subP
        )

        // 7️⃣ Client / Payable / Invoice # grid
        val gridTop = invoiceY + titleP.textSize + subP.textSize + 24f
        val colX = listOf(40f, 220f, 400f)
        c.drawText("Invoice for", colX[0], gridTop, headerP)
        c.drawText(meta.clientName, colX[0], gridTop + 16f, bodyP)
        c.drawText(
            meta.clientAddress1 + meta.clientAddress2?.let { ", $it" }.orEmpty(),
            colX[0], gridTop + 32f, bodyP
        )
        c.drawText(
            "${meta.clientCity}, ${meta.clientState} ${meta.clientZip}",
            colX[0], gridTop + 48f, bodyP
        )
        c.drawText("Payable to", colX[1], gridTop, headerP)
        c.drawText(meta.payableTo, colX[1], gridTop + 16f, bodyP)
        c.drawText("Invoice #", colX[2], gridTop, headerP)
        meta.invoiceNumber?.let {
            c.drawText(it, colX[2], gridTop + 16f, bodyP)
        }

        // 8️⃣ Table separator
        val tableTop = gridTop + 80f
        c.drawLine(40f, tableTop, 555f, tableTop, lineP)

        // 9️⃣ Table header
        val headerY = tableTop + 24f
        c.drawText("Description", 40f, headerY, headerP)
        c.drawText("Expiration", 260f, headerY, headerP)
        listOf("Qty" to 380f, "Unit" to 430f, "Total" to 520f).forEach { (txt, x) ->
            c.drawText(txt, x - headerP.measureText(txt), headerY, headerP)
        }
        val headerSepY = headerY + headerP.textSize + 8f
        c.drawLine(40f, headerSepY, 555f, headerSepY, lineP.apply { strokeWidth = 2f })

        // 🔟 Line items
        var itemY = headerSepY + 16f
        lines.forEachIndexed { idx, line ->
            if (idx % 2 == 1) {
                c.drawRect(40f, itemY - 16f, 555f, itemY + 4f, stripeP)
            }
            c.drawText(line.description, 40f, itemY, bodyP)
            c.drawText(line.expiration, 260f, itemY, bodyP)
            listOf(
                line.quantity.toString() to 380f,
                "$${"%.2f".format(line.unitPrice)}" to 430f,
                "$${"%.2f".format(line.lineTotal)}" to 520f
            ).forEach { (txt, x) ->
                c.drawText(txt, x - bodyP.measureText(txt), itemY, bodyP)
            }
            itemY += bodyP.textSize + 8f
        }

        // 1️⃣1️⃣ Subtotal & Adjustments
        val subtotal = lines.sumOf { it.lineTotal.toDouble() }
        val subY = itemY + 8f
        c.drawText("Subtotal", 360f, subY, headerP)
        val subTxt = "$${"%.2f".format(subtotal)}"
        c.drawText(subTxt, 520f - bodyP.measureText(subTxt), subY, bodyP)

        val adjY = subY + bodyP.textSize + 8f
        c.drawText("Adjustments", 360f, adjY, headerP)
        val adjTxt = "$0.00"
        c.drawText(adjTxt, 520f - bodyP.measureText(adjTxt), adjY, bodyP)

        // 1️⃣2️⃣ Grand total
        val totalY = adjY + bodyP.textSize + 16f
        val totalSepY = totalY - 12f
        c.drawLine(360f, totalSepY, 555f, totalSepY, lineP.apply { strokeWidth = 2f })
        c.drawText("Total Due", 360f, totalY, totalP)
        val totalTxt = "$${"%.2f".format(subtotal)}"
        c.drawText(totalTxt, 520f - totalP.measureText(totalTxt), totalY, totalP)

        // 1️⃣3️⃣ Finish
        pdf.finishPage(page)

        // 1️⃣4️⃣ Safe filename + ensure directory
        val safeDate = SimpleDateFormat("yyyyMMdd", Locale.US).format(invoiceDate)
        val filename = "invoice_${meta.invoiceNumber ?: safeDate}.pdf"
        val outDir = context
            .getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            ?.also { it.mkdirs() }
            ?: context.filesDir
        val outFile = File(outDir, filename)

        // 1️⃣5️⃣ Write file
        FileOutputStream(outFile).use { pdf.writeTo(it) }
        pdf.close()

        return outFile
    }
}