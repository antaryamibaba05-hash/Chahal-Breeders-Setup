package com.highflyerpro.tracker.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import androidx.core.content.FileProvider
import com.highflyerpro.tracker.data.local.entities.*
import com.highflyerpro.tracker.domain.calculation.PerformanceEngine
import com.highflyerpro.tracker.domain.model.LeaderboardEntry
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    private const val BRAND_HEADER = "HIGH FLYER PRO TRACKER"
    private const val BRAND_SUBTITLE = "Professional Pigeon Loft Management System"
    private const val BRAND_FOOTER = "Official Document - High Flyer Pro Tracker System"

    private fun createPageHeader(canvas: Canvas, paint: Paint, title: String, subtitle: String): Float {
        canvas.drawColor(Color.WHITE)

        // Header Background Bar
        paint.color = Color.parseColor("#1E3A8A") // Dark Navy
        canvas.drawRect(0f, 0f, 595f, 90f, paint)

        // Header Text
        paint.color = Color.WHITE
        paint.textSize = 20f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(BRAND_HEADER, 30f, 38f, paint)

        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(BRAND_SUBTITLE, 30f, 56f, paint)

        paint.textSize = 9f
        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        canvas.drawText("Generated: ${dateFormat.format(Date())}", 30f, 74f, paint)

        // Title text below header bar
        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 15f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(title, 30f, 120f, paint)

        paint.color = Color.parseColor("#475569")
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(subtitle, 30f, 136f, paint)

        return 160f // Y start for page content
    }

    private fun drawFooter(canvas: Canvas, paint: Paint, pageNumber: Int) {
        paint.color = Color.GRAY
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("$BRAND_FOOTER | Page $pageNumber", 30f, 825f, paint)
    }

    // 1. Complete Loft Master Roster Report
    fun generateMasterRosterPdf(context: Context, pigeons: List<PigeonEntity>): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        var y = createPageHeader(canvas, paint, "LOFT MASTER ROSTER REPORT", "Total Registered Pigeons: ${pigeons.size}")

        // Table Header
        paint.color = Color.parseColor("#E2E8F0")
        canvas.drawRect(30f, y, 565f, y + 22f, paint)

        paint.color = Color.BLACK
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("#", 35f, y + 15f, paint)
        canvas.drawText("Ring #", 60f, y + 15f, paint)
        canvas.drawText("Name / Title", 160f, y + 15f, paint)
        canvas.drawText("Gender", 300f, y + 15f, paint)
        canvas.drawText("Breed", 360f, y + 15f, paint)
        canvas.drawText("Color", 450f, y + 15f, paint)
        canvas.drawText("Status", 515f, y + 15f, paint)

        y += 22f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 9f

        for ((idx, p) in pigeons.take(30).withIndex()) {
            if (idx % 2 == 1) {
                paint.color = Color.parseColor("#F8FAFC")
                canvas.drawRect(30f, y, 565f, y + 20f, paint)
            }
            paint.color = Color.BLACK
            canvas.drawText("${idx + 1}", 35f, y + 14f, paint)
            canvas.drawText(p.ringNumber.ifBlank { "Unbanded" }, 60f, y + 14f, paint)
            canvas.drawText(p.name, 160f, y + 14f, paint)
            canvas.drawText(p.gender, 300f, y + 14f, paint)
            canvas.drawText(p.breed, 360f, y + 14f, paint)
            canvas.drawText(p.color.ifBlank { "Standard" }, 450f, y + 14f, paint)
            canvas.drawText(p.status, 515f, y + 14f, paint)
            y += 20f
            if (y > 800f) break
        }

        drawFooter(canvas, paint, 1)
        pdfDocument.finishPage(page)
        return savePdfToFile(context, pdfDocument, "Master_Roster_${System.currentTimeMillis()}.pdf")
    }

    // 2. Individual Pigeon Profile PDF
    fun generatePigeonProfilePdf(
        context: Context,
        pigeon: PigeonEntity,
        father: PigeonEntity?,
        mother: PigeonEntity?,
        flightHistory: List<FlightRecordEntity>
    ): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        var y = createPageHeader(canvas, paint, "PIGEON DIGITAL LIFETIME PROFILE", "Ring Number: ${pigeon.ringNumber}")

        // Identity Card Box
        paint.color = Color.parseColor("#F1F5F9")
        canvas.drawRect(30f, y, 565f, y + 110f, paint)

        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 16f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("${pigeon.name} (${pigeon.ringNumber})", 45f, y + 30f, paint)

        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Gender: ${pigeon.gender}   |   Breed: ${pigeon.breed}   |   Color: ${pigeon.color.ifBlank { "Standard" }}", 45f, y + 50f, paint)
        canvas.drawText("Status: ${pigeon.status}   |   Nickname: ${pigeon.nickname.ifBlank { "None" }}", 45f, y + 68f, paint)
        canvas.drawText("Sire (Father): ${father?.name ?: pigeon.fatherId ?: "Unknown"}", 45f, y + 86f, paint)
        canvas.drawText("Dam (Mother): ${mother?.name ?: pigeon.motherId ?: "Unknown"}", 300f, y + 86f, paint)

        y += 130f

        // Flight Records Table
        paint.color = Color.BLACK
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("FLIGHT HISTORICAL PERFORMANCE LOG (${flightHistory.size})", 30f, y, paint)

        y += 15f
        paint.color = Color.parseColor("#E2E8F0")
        canvas.drawRect(30f, y, 565f, y + 22f, paint)

        paint.color = Color.BLACK
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Release Date", 40f, y + 15f, paint)
        canvas.drawText("Flight Duration", 200f, y + 15f, paint)
        canvas.drawText("Landing Rank", 350f, y + 15f, paint)
        canvas.drawText("Status", 450f, y + 15f, paint)

        y += 22f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        for (rec in flightHistory.take(20)) {
            val dateStr = dateFormat.format(Date(rec.releaseTimestamp))
            val durStr = rec.durationMillis?.let { PerformanceEngine.formatDurationHMS(it) } ?: "-"
            val posStr = if (rec.landingPosition != null && rec.landingPosition > 0) "#${rec.landingPosition}" else "-"

            canvas.drawText(dateStr, 40f, y + 15f, paint)
            canvas.drawText(durStr, 200f, y + 15f, paint)
            canvas.drawText(posStr, 350f, y + 15f, paint)
            canvas.drawText(rec.status, 450f, y + 15f, paint)
            y += 20f
            if (y > 800f) break
        }

        drawFooter(canvas, paint, 1)
        pdfDocument.finishPage(page)
        return savePdfToFile(context, pdfDocument, "Profile_${pigeon.ringNumber.ifBlank { pigeon.pigeonId }}.pdf")
    }

    // 3. Flight Session Report PDF
    fun generateFlightSessionPdf(
        context: Context,
        eventName: String,
        dateMillis: Long,
        records: List<FlightRecordEntity>,
        pigeonsMap: Map<String, PigeonEntity>
    ): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        var y = createPageHeader(canvas, paint, "FLIGHT SESSION PERFORMANCE REPORT", "Event: $eventName | Total Birds: ${records.size}")

        // Table Header
        paint.color = Color.parseColor("#E2E8F0")
        canvas.drawRect(30f, y, 565f, y + 22f, paint)

        paint.color = Color.BLACK
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        canvas.drawText("Pos", 40f, y + 15f, paint)
        canvas.drawText("Ring #", 80f, y + 15f, paint)
        canvas.drawText("Pigeon Name", 180f, y + 15f, paint)
        canvas.drawText("Release", 330f, y + 15f, paint)
        canvas.drawText("Landing", 410f, y + 15f, paint)
        canvas.drawText("Duration", 490f, y + 15f, paint)

        y += 22f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 9f

        val sortedRecords = records.sortedWith(compareByDescending { it.durationMillis ?: 0L })

        for ((idx, rec) in sortedRecords.withIndex()) {
            val pigeon = pigeonsMap[rec.pigeonId]
            if (idx % 2 == 1) {
                paint.color = Color.parseColor("#F8FAFC")
                canvas.drawRect(30f, y, 565f, y + 20f, paint)
            }

            paint.color = Color.BLACK
            val posStr = if (rec.landingPosition != null && rec.landingPosition > 0) "#${rec.landingPosition}" else "${idx + 1}"
            canvas.drawText(posStr, 40f, y + 14f, paint)
            canvas.drawText(pigeon?.ringNumber ?: "N/A", 80f, y + 14f, paint)
            canvas.drawText(pigeon?.name ?: rec.pigeonId, 180f, y + 14f, paint)

            val relTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(rec.releaseTimestamp))
            val landTime = rec.landingTimestamp?.let { SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(it)) } ?: "-"
            val durationStr = rec.durationMillis?.let { PerformanceEngine.formatDurationHMS(it) } ?: rec.status

            canvas.drawText(relTime, 330f, y + 14f, paint)
            canvas.drawText(landTime, 410f, y + 14f, paint)
            canvas.drawText(durationStr, 490f, y + 14f, paint)

            y += 20f
            if (y > 800f) break
        }

        drawFooter(canvas, paint, 1)
        pdfDocument.finishPage(page)
        return savePdfToFile(context, pdfDocument, "Flight_Report_${System.currentTimeMillis()}.pdf")
    }

    // 4. Leaderboard & Event Standings PDF
    fun generateLeaderboardPdf(context: Context, entries: List<LeaderboardEntry>): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        var y = createPageHeader(canvas, paint, "CUMULATIVE EVENT & LOFT STANDINGS", "Official Lifetime Leaderboard & Endurance Rankings")

        paint.color = Color.parseColor("#E2E8F0")
        canvas.drawRect(30f, y, 565f, y + 22f, paint)

        paint.color = Color.BLACK
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        canvas.drawText("Rank", 40f, y + 15f, paint)
        canvas.drawText("Ring #", 90f, y + 15f, paint)
        canvas.drawText("Name", 190f, y + 15f, paint)
        canvas.drawText("Flights", 330f, y + 15f, paint)
        canvas.drawText("Total Hours", 400f, y + 15f, paint)
        canvas.drawText("Best Flight", 490f, y + 15f, paint)

        y += 22f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 9f

        for (e in entries.take(30)) {
            if (e.rank % 2 == 0) {
                paint.color = Color.parseColor("#F8FAFC")
                canvas.drawRect(30f, y, 565f, y + 20f, paint)
            }
            paint.color = Color.BLACK
            canvas.drawText("#${e.rank}", 40f, y + 14f, paint)
            canvas.drawText(e.ringNumber.ifBlank { "Unbanded" }, 90f, y + 14f, paint)
            canvas.drawText(e.name, 190f, y + 14f, paint)
            canvas.drawText("${e.flightsCount}", 330f, y + 14f, paint)
            canvas.drawText(PerformanceEngine.formatDurationLong(e.totalDurationMillis), 400f, y + 14f, paint)
            canvas.drawText(PerformanceEngine.formatDurationHMS(e.bestDurationMillis), 490f, y + 14f, paint)

            y += 20f
            if (y > 800f) break
        }

        drawFooter(canvas, paint, 1)
        pdfDocument.finishPage(page)
        return savePdfToFile(context, pdfDocument, "Leaderboard_${System.currentTimeMillis()}.pdf")
    }

    // 5. Generic Category PDF Generator
    fun generateGenericReportPdf(context: Context, title: String, subtitle: String, headers: List<String>, rows: List<List<String>>): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        var y = createPageHeader(canvas, paint, title.uppercase(), subtitle)

        paint.color = Color.parseColor("#E2E8F0")
        canvas.drawRect(30f, y, 565f, y + 22f, paint)

        paint.color = Color.BLACK
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        val colWidth = 535f / headers.size.coerceAtLeast(1)
        for ((idx, h) in headers.withIndex()) {
            canvas.drawText(h, 35f + (idx * colWidth), y + 15f, paint)
        }

        y += 22f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 9f

        for ((rIdx, row) in rows.take(30).withIndex()) {
            if (rIdx % 2 == 1) {
                paint.color = Color.parseColor("#F8FAFC")
                canvas.drawRect(30f, y, 565f, y + 20f, paint)
            }
            paint.color = Color.BLACK
            for ((cIdx, cell) in row.withIndex()) {
                canvas.drawText(cell, 35f + (cIdx * colWidth), y + 14f, paint)
            }
            y += 20f
            if (y > 800f) break
        }

        drawFooter(canvas, paint, 1)
        pdfDocument.finishPage(page)
        return savePdfToFile(context, pdfDocument, "${title.replace(" ", "_")}_${System.currentTimeMillis()}.pdf")
    }

    private fun savePdfToFile(context: Context, pdfDocument: PdfDocument, filename: String): File? {
        return try {
            val publicDocs = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                "HighFlyerProReports"
            )
            if (!publicDocs.exists()) publicDocs.mkdirs()

            val file = if (publicDocs.exists() && publicDocs.canWrite()) {
                File(publicDocs, filename)
            } else {
                val appDocs = context.getExternalFilesDir("documents") ?: context.filesDir
                if (!appDocs.exists()) appDocs.mkdirs()
                File(appDocs, filename)
            }

            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.close()
            pdfDocument.close()
            
            if (file.exists() && file.length() > 0) file else null
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    fun sharePdf(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "Share PDF Report")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
