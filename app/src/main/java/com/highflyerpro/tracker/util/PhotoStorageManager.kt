package com.highflyerpro.tracker.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

data class ProcessedPhotoResult(
    val filePath: String,
    val thumbnailPath: String
)

object PhotoStorageManager {

    fun createTempCameraUri(context: Context, tag: String = "pigeon"): Uri {
        val photosDir = File(context.filesDir, "pigeon_photos").apply { if (!exists()) mkdirs() }
        val tempFile = File(photosDir, "temp_${tag}_${System.currentTimeMillis()}.jpg")
        val authority = "${context.packageName}.fileprovider"
        return FileProvider.getUriForFile(context, authority, tempFile)
    }

    fun createImageUri(context: Context): Uri = createTempCameraUri(context)

    fun savePigeonPhoto(
        context: Context,
        sourceUri: Uri,
        pigeonId: String,
        isProfile: Boolean = false
    ): ProcessedPhotoResult {
        val (path, thumb) = savePhotoFromUri(context, sourceUri)
        return ProcessedPhotoResult(filePath = path, thumbnailPath = thumb)
    }

    fun savePhotoFromUri(context: Context, sourceUri: Uri): Pair<String, String> {
        return try {
            val photosDir = File(context.filesDir, "pigeon_photos").apply { if (!exists()) mkdirs() }
            val thumbsDir = File(context.filesDir, "pigeon_thumbs").apply { if (!exists()) mkdirs() }

            val fileId = UUID.randomUUID().toString()
            val destFile = File(photosDir, "photo_$fileId.jpg")
            val thumbFile = File(thumbsDir, "thumb_$fileId.jpg")

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }

            // Create thumbnail
            val bitmap = BitmapFactory.decodeFile(destFile.absolutePath)
            if (bitmap != null) {
                val scaled = Bitmap.createScaledBitmap(bitmap, 300, 300, true)
                FileOutputStream(thumbFile).use { out ->
                    scaled.compress(Bitmap.CompressFormat.JPEG, 80, out)
                }
            } else {
                destFile.copyTo(thumbFile, overwrite = true)
            }

            Pair(destFile.absolutePath, thumbFile.absolutePath)
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(sourceUri.toString(), sourceUri.toString())
        }
    }

    fun deletePhotoFiles(effectivePath: String?, thumbnailPath: String?) {
        try {
            if (!effectivePath.isNullOrEmpty() && effectivePath.startsWith("/")) {
                val f = File(effectivePath)
                if (f.exists()) f.delete()
            }
            if (!thumbnailPath.isNullOrEmpty() && thumbnailPath.startsWith("/")) {
                val f = File(thumbnailPath)
                if (f.exists()) f.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
