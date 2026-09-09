package com.highflyerpro.tracker.util

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.documentfile.provider.DocumentFile
import com.highflyerpro.tracker.data.local.AppDatabase
import com.highflyerpro.tracker.data.local.entities.PigeonEntity
import com.highflyerpro.tracker.data.repository.HighFlyerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

data class BackupMetadata(
    val appName: String = "High Flyer Pro Tracker",
    val appVersion: String = "1.0",
    val dbVersion: Int = AppDatabase.DATABASE_VERSION,
    val backupDate: String = "",
    val backupTimestamp: Long = 0L,
    val totalPigeons: Int = 0,
    val totalActivePigeons: Int = 0,
    val totalRetiredPigeons: Int = 0,
    val totalArchivedPigeons: Int = 0,
    val totalFlights: Int = 0,
    val totalEvents: Int = 0,
    val totalBreedingPairs: Int = 0,
    val totalOffspring: Int = 0,
    val totalHealthRecords: Int = 0,
    val totalMedications: Int = 0,
    val totalDewormingRecords: Int = 0,
    val totalAchievements: Int = 0,
    val totalPhotos: Int = 0,
    val backupSizeBytes: Long = 0L,
    val fileName: String = ""
)

data class BackupValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null,
    val metadata: BackupMetadata? = null,
    val zipUri: Uri? = null
)

data class RestoreSummary(
    val pigeonsRestored: Int,
    val flightsRestored: Int,
    val photosRestored: Int,
    val breedingRecordsRestored: Int,
    val healthRecordsRestored: Int,
    val achievementsRestored: Int
)

sealed class BackupOperationResult {
    data class Success(val metadata: BackupMetadata, val zipUri: Uri, val destinationPath: String) : BackupOperationResult()
    data class Error(val message: String) : BackupOperationResult()
}

sealed class RestoreOperationResult {
    data class Success(val summary: RestoreSummary) : RestoreOperationResult()
    data class Error(val message: String) : RestoreOperationResult()
}

object BackupRestoreManager {

    private const val DB_NAME = AppDatabase.DATABASE_NAME

    suspend fun createBackup(
        context: Context,
        database: AppDatabase,
        repository: HighFlyerRepository,
        folderUri: Uri? = null
    ): BackupOperationResult = withContext(Dispatchers.IO) {
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault())
            val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val timestamp = System.currentTimeMillis()
            val fileName = "High_Flyer_Pro_Backup_${dateFormat.format(Date(timestamp))}.zip"

            // 1. Gather stats & entities
            val pigeons = repository.allPigeonsFlow.first()
            val sessions = repository.allSessionsFlow.first()
            val records = repository.allFlightRecordsFlow.first()
            val achievements = repository.allAchievementsFlow.first()
            val healthRecords = repository.allHealthRecordsFlow.first()
            val breedingPairs = repository.allBreedingRecordsFlow.first()
            val photos = repository.allPigeonPhotosFlow.first()

            val activePigeons = pigeons.count { !it.isArchived && it.status == "Active" }
            val retiredPigeons = pigeons.count { !it.isArchived && it.status == "Retired" }
            val archivedPigeons = pigeons.count { it.isArchived }

            val tempZipFile = File(context.cacheDir, fileName)
            if (tempZipFile.exists()) tempZipFile.delete()

            // Flush Room WAL checkpoint
            try {
                database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").close()
            } catch (e: Exception) {
                android.util.Log.w("BackupRestoreManager", "WAL checkpoint failed prior to backup: ${e.message}", e)
            }

            val dbFile = context.getDatabasePath(DB_NAME)
            if (!dbFile.exists()) {
                return@withContext BackupOperationResult.Error("Database file ${DB_NAME} does not exist on device.")
            }

            ZipOutputStream(BufferedOutputStream(FileOutputStream(tempZipFile))).use { zipOut ->
                // A. Database
                addFileToZip(zipOut, dbFile, "database/${DB_NAME}")

                // B. Metadata JSON
                val metadataObj = JSONObject().apply {
                    put("appName", "High Flyer Pro Tracker")
                    put("appVersion", "1.0")
                    put("dbVersion", AppDatabase.DATABASE_VERSION)
                    put("backupDate", dateStr)
                    put("backupTimestamp", timestamp)
                    put("totalPigeons", pigeons.size)
                    put("totalActivePigeons", activePigeons)
                    put("totalRetiredPigeons", retiredPigeons)
                    put("totalArchivedPigeons", archivedPigeons)
                    put("totalFlights", records.size)
                    put("totalEvents", sessions.size)
                    put("totalBreedingPairs", breedingPairs.size)
                    put("totalHealthRecords", healthRecords.size)
                    put("totalAchievements", achievements.size)
                    put("totalPhotos", photos.size)
                    put("fileName", fileName)
                }
                addStringToZip(zipOut, metadataObj.toString(2), "metadata.json")

                // C. Human-readable README.txt
                val readmeText = """
                    High Flyer Pro Tracker - Backup Archive
                    =======================================
                    Application Name: High Flyer Pro Tracker
                    Backup Date: $dateStr
                    Database Name: ${AppDatabase.DATABASE_NAME}
                    Database Version: ${AppDatabase.DATABASE_VERSION}
                    
                    RECORD SUMMARY:
                    - Total Pigeons: ${pigeons.size} (Active: $activePigeons, Retired: $retiredPigeons, Archived: $archivedPigeons)
                    - Flight Records: ${records.size}
                    - Flight Sessions: ${sessions.size}
                    - Breeding Records: ${breedingPairs.size}
                    - Health Records: ${healthRecords.size}
                    - Achievements: ${achievements.size}
                    - Photos Exported: ${photos.size}
                    
                    RESTORE INSTRUCTIONS:
                    1. Install High Flyer Pro Tracker on your Android device.
                    2. Open the application.
                    3. On first launch, tap "RESTORE DATA" (or go to Settings -> Data & Backup -> Restore Data).
                    4. Select this ZIP backup file ($fileName).
                    5. Confirm restore when prompted.
                """.trimIndent()
                addStringToZip(zipOut, readmeText, "README.txt")

                // D. Human-readable JSON exports
                val pigeonsArr = JSONArray()
                pigeons.forEach { p ->
                    pigeonsArr.put(JSONObject().apply {
                        put("pigeonId", p.pigeonId)
                        put("ringNumber", p.ringNumber)
                        put("name", p.name)
                        put("gender", p.gender)
                        put("color", p.color)
                        put("breed", p.breed)
                        put("status", p.status)
                        put("fatherId", p.fatherId ?: "")
                        put("motherId", p.motherId ?: "")
                    })
                }
                addStringToZip(zipOut, pigeonsArr.toString(2), "readable_data/pigeons.json")

                val flightsArr = JSONArray()
                records.forEach { r ->
                    flightsArr.put(JSONObject().apply {
                        put("recordId", r.id)
                        put("sessionId", r.sessionId)
                        put("pigeonId", r.pigeonId)
                        put("releaseTimestamp", r.releaseTimestamp)
                        put("landingTimestamp", r.landingTimestamp ?: 0L)
                        put("durationMillis", r.durationMillis ?: 0L)
                        put("landingPosition", r.landingPosition ?: 0)
                        put("status", r.status)
                    })
                }
                addStringToZip(zipOut, flightsArr.toString(2), "readable_data/flights.json")

                // E. Export internal photos
                val photoDir = File(context.filesDir, "pigeon_photos")
                if (photoDir.exists() && photoDir.isDirectory) {
                    photoDir.listFiles()?.forEach { photoFile ->
                        if (photoFile.isFile) {
                            addFileToZip(zipOut, photoFile, "photos/pigeon_photos/${photoFile.name}")
                        }
                    }
                }
            }

            // Copy to external folder if SAF Uri supplied
            var finalDestination = tempZipFile.absolutePath
            var targetUri: Uri = Uri.fromFile(tempZipFile)

            if (folderUri != null) {
                try {
                    val treeDoc = DocumentFile.fromTreeUri(context, folderUri)
                    if (treeDoc != null && treeDoc.exists()) {
                        val backupFolder = treeDoc.findFile("High Flyer Pro Backups")
                            ?: treeDoc.createDirectory("High Flyer Pro Backups")
                            ?: treeDoc

                        val createdDoc = backupFolder.createFile("application/zip", fileName)
                        if (createdDoc != null) {
                            context.contentResolver.openOutputStream(createdDoc.uri)?.use { outStream ->
                                FileInputStream(tempZipFile).use { inStream ->
                                    inStream.copyTo(outStream)
                                }
                            }
                            targetUri = createdDoc.uri
                            finalDestination = createdDoc.uri.toString()
                        }
                    }
                } catch (e: Exception) {
                    // Fallback to local cache file if SAF copy failed
                }
            }

            val size = tempZipFile.length()
            if (!tempZipFile.exists() || size == 0L) {
                return@withContext BackupOperationResult.Error("Generated backup ZIP archive is empty or invalid.")
            }

            val metadata = BackupMetadata(
                appName = "High Flyer Pro Tracker",
                dbVersion = AppDatabase.DATABASE_VERSION,
                backupDate = dateStr,
                backupTimestamp = timestamp,
                totalPigeons = pigeons.size,
                totalActivePigeons = activePigeons,
                totalRetiredPigeons = retiredPigeons,
                totalArchivedPigeons = archivedPigeons,
                totalFlights = records.size,
                totalEvents = sessions.size,
                totalBreedingPairs = breedingPairs.size,
                totalHealthRecords = healthRecords.size,
                totalAchievements = achievements.size,
                totalPhotos = photos.size,
                backupSizeBytes = size,
                fileName = fileName
            )

            BackupOperationResult.Success(metadata, targetUri, finalDestination)
        } catch (e: Exception) {
            BackupOperationResult.Error(e.message ?: "Failed to generate backup archive.")
        }
    }

    suspend fun validateBackup(context: Context, zipUri: Uri): BackupValidationResult = withContext(Dispatchers.IO) {
        try {
            var metadataObj: JSONObject? = null
            var hasDb = false

            val inputStream: InputStream? = context.contentResolver.openInputStream(zipUri)
                ?: if (zipUri.path != null && File(zipUri.path!!).exists()) FileInputStream(File(zipUri.path!!)) else null

            if (inputStream == null) {
                return@withContext BackupValidationResult(false, "Cannot open selected backup file.")
            }

            ZipInputStream(BufferedInputStream(inputStream)).use { zipIn ->
                var entry = zipIn.nextEntry
                while (entry != null) {
                    val name = entry.name
                    if (name.contains(DB_NAME) || name.endsWith(".db")) {
                        hasDb = true
                    } else if (name == "metadata.json") {
                        val content = zipIn.bufferedReader().readText()
                        try {
                            metadataObj = JSONObject(content)
                        } catch (e: Exception) {
                            android.util.Log.e("BackupRestoreManager", "Failed to parse metadata JSON from ZIP: ${e.message}", e)
                        }
                    }
                    zipIn.closeEntry()
                    entry = zipIn.nextEntry
                }
            }

            if (!hasDb) {
                return@withContext BackupValidationResult(
                    isValid = false,
                    errorMessage = "This file is not a valid High Flyer Pro Tracker backup (Missing Database File)."
                )
            }

            val meta = metadataObj?.let { obj ->
                BackupMetadata(
                    appName = obj.optString("appName", "High Flyer Pro Tracker"),
                    appVersion = obj.optString("appVersion", "1.0"),
                    dbVersion = obj.optInt("dbVersion", AppDatabase.DATABASE_VERSION),
                    backupDate = obj.optString("backupDate", "Unknown"),
                    backupTimestamp = obj.optLong("backupTimestamp", 0L),
                    totalPigeons = obj.optInt("totalPigeons", 0),
                    totalActivePigeons = obj.optInt("totalActivePigeons", 0),
                    totalRetiredPigeons = obj.optInt("totalRetiredPigeons", 0),
                    totalArchivedPigeons = obj.optInt("totalArchivedPigeons", 0),
                    totalFlights = obj.optInt("totalFlights", 0),
                    totalEvents = obj.optInt("totalEvents", 0),
                    totalBreedingPairs = obj.optInt("totalBreedingPairs", 0),
                    totalHealthRecords = obj.optInt("totalHealthRecords", 0),
                    totalAchievements = obj.optInt("totalAchievements", 0),
                    totalPhotos = obj.optInt("totalPhotos", 0),
                    fileName = obj.optString("fileName", "Backup.zip")
                )
            } ?: BackupMetadata(backupDate = "Valid Backup")

            BackupValidationResult(isValid = true, metadata = meta, zipUri = zipUri)
        } catch (e: Exception) {
            BackupValidationResult(
                isValid = false,
                errorMessage = "Selected file is corrupted or not a valid ZIP archive: ${e.message}"
            )
        }
    }

    suspend fun restoreBackup(
        context: Context,
        database: AppDatabase,
        zipUri: Uri,
        onProgress: (stepName: String, progress: Float) -> Unit
    ): RestoreOperationResult = withContext(Dispatchers.IO) {
        val safetyDir = File(context.cacheDir, "safety_backup")
        val activeDbFile = context.getDatabasePath(DB_NAME)

        try {
            // Step 1: Validate (10%)
            onProgress("Validating Backup Archive...", 0.1f)
            val validation = validateBackup(context, zipUri)
            if (!validation.isValid) {
                return@withContext RestoreOperationResult.Error(
                    validation.errorMessage ?: "This file is not a valid High Flyer Pro Tracker backup."
                )
            }

            // Step 2: Safety Backup of Current DB (20%)
            onProgress("Creating Safety Checkpoint...", 0.2f)
            if (activeDbFile.exists()) {
                safetyDir.mkdirs()
                val safetyDbFile = File(safetyDir, "high_flyer_safety.db")
                activeDbFile.copyTo(safetyDbFile, overwrite = true)
            }

            // Step 3: Extract Database (40%)
            onProgress("Restoring Database...", 0.4f)
            val tempExtractedDb = File(context.cacheDir, "restored_temp.db")
            if (tempExtractedDb.exists()) tempExtractedDb.delete()

            val photoTargetDir = File(context.filesDir, "pigeon_photos")
            photoTargetDir.mkdirs()

            var extractedPhotosCount = 0

            val inputStream = context.contentResolver.openInputStream(zipUri)
                ?: FileInputStream(File(zipUri.path!!))

            ZipInputStream(BufferedInputStream(inputStream)).use { zipIn ->
                var entry = zipIn.nextEntry
                while (entry != null) {
                    val name = entry.name
                    if (name.contains(DB_NAME) || name.endsWith(".db")) {
                        FileOutputStream(tempExtractedDb).use { out ->
                            zipIn.copyTo(out)
                        }
                    } else if (name.startsWith("photos/pigeon_photos/")) {
                        val photoName = name.removePrefix("photos/pigeon_photos/")
                        if (photoName.isNotBlank()) {
                            val photoOutFile = File(photoTargetDir, photoName)
                            FileOutputStream(photoOutFile).use { out ->
                                zipIn.copyTo(out)
                            }
                            extractedPhotosCount++
                        }
                    }
                    zipIn.closeEntry()
                    entry = zipIn.nextEntry
                }
            }

            if (!tempExtractedDb.exists()) {
                return@withContext RestoreOperationResult.Error("Database file not found in backup archive.")
            }

            // Step 4: Close current DB connections, delete WAL/SHM sidecars, and swap DB file (60%)
            onProgress("Applying Restored Records...", 0.6f)
            AppDatabase.closeInstance()

            val walFile = File("${activeDbFile.path}-wal")
            val shmFile = File("${activeDbFile.path}-shm")
            if (walFile.exists()) walFile.delete()
            if (shmFile.exists()) shmFile.delete()

            tempExtractedDb.copyTo(activeDbFile, overwrite = true)

            // Step 5: Verify Restored Data (90%)
            onProgress("Verifying Restored Data...", 0.9f)
            val newDb = AppDatabase.getDatabase(context)
            val restoredPigeons = newDb.pigeonDao().getAllPigeonsList()
            val restoredFlights = newDb.flightDao().getAllRecords()
            val restoredHealth = newDb.healthDao().getAllHealthRecords()
            val restoredBreeding = newDb.breedingDao().getAllBreedingRecords()
            val restoredAchievements = newDb.achievementDao().getAllAchievements()

            onProgress("Restore Completed!", 1.0f)

            val summary = RestoreSummary(
                pigeonsRestored = restoredPigeons.size,
                flightsRestored = restoredFlights.size,
                photosRestored = extractedPhotosCount,
                breedingRecordsRestored = restoredBreeding.size,
                healthRecordsRestored = restoredHealth.size,
                achievementsRestored = restoredAchievements.size
            )

            RestoreOperationResult.Success(summary)
        } catch (e: Exception) {
            // Attempt rollback to safety backup if exists
            try {
                val safetyDbFile = File(safetyDir, "high_flyer_safety.db")
                if (safetyDbFile.exists()) {
                    safetyDbFile.copyTo(activeDbFile, overwrite = true)
                }
            } catch (rollbackEx: Exception) {
                android.util.Log.e("BackupRestoreManager", "Safety rollback failed: ${rollbackEx.message}", rollbackEx)
            }

            RestoreOperationResult.Error("Restore failed: ${e.message}")
        }
    }

    private fun addFileToZip(zipOut: ZipOutputStream, file: File, entryPath: String) {
        if (!file.exists()) return
        val entry = ZipEntry(entryPath)
        zipOut.putNextEntry(entry)
        FileInputStream(file).use { inStream ->
            inStream.copyTo(zipOut)
        }
        zipOut.closeEntry()
    }

    private fun addStringToZip(zipOut: ZipOutputStream, content: String, entryPath: String) {
        val entry = ZipEntry(entryPath)
        zipOut.putNextEntry(entry)
        zipOut.write(content.toByteArray(Charsets.UTF_8))
        zipOut.closeEntry()
    }
}
