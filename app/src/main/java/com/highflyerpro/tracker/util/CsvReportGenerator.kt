package com.highflyerpro.tracker.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.highflyerpro.tracker.data.local.entities.PigeonEntity
import java.io.File
import java.io.FileWriter

object CsvReportGenerator {

    fun exportPigeonsCsv(context: Context, pigeons: List<PigeonEntity>): File? {
        return try {
            val docsDir = context.getExternalFilesDir("documents") ?: context.filesDir
            if (!docsDir.exists()) docsDir.mkdirs()
            val file = File(docsDir, "High_Flyer_Pigeons_${System.currentTimeMillis()}.csv")
            val writer = FileWriter(file)

            // Header
            writer.append("PigeonID,RingNumber,Name,Nickname,Gender,Breed,Color,Status,SireID,DamID,Breeder,Source,Notes\n")

            for (p in pigeons) {
                writer.append("\"${p.pigeonId}\",")
                writer.append("\"${p.ringNumber}\",")
                writer.append("\"${p.name}\",")
                writer.append("\"${p.nickname}\",")
                writer.append("\"${p.gender}\",")
                writer.append("\"${p.breed}\",")
                writer.append("\"${p.color}\",")
                writer.append("\"${p.status}\",")
                writer.append("\"${p.fatherId ?: ""}\",")
                writer.append("\"${p.motherId ?: ""}\",")
                writer.append("\"${p.breeder}\",")
                writer.append("\"${p.source}\",")
                writer.append("\"${p.notes.replace("\n", " ")}\"\n")
            }

            writer.flush()
            writer.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun shareCsv(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "Share CSV Data Export")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
