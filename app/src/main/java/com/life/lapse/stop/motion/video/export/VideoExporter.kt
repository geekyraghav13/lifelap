package com.life.lapse.stop.motion.video.export

import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import android.util.Log
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.life.lapse.stop.motion.video.data.model.Project
import java.io.File
import java.io.FileOutputStream

class VideoExporter(private val context: Context) {

    companion object {
        private const val TAG = "VideoExporter"
    }

    fun export(project: Project, onResult: (isSuccess: Boolean, message: String) -> Unit) {
        if (project.frameUris.isEmpty()) {
            onResult(false, "Cannot export an empty project.")
            return
        }

        // --- Create the output file using MediaStore ---
        val fileName = "LifeLapse_${System.currentTimeMillis()}.mp4"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Movies/LifeLapse")
        }
        val resolver = context.contentResolver
        val finalVideoUri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)

        if (finalVideoUri == null) {
            onResult(false, "Failed to create output file.")
            return
        }

        // FFmpeg needs a real file path to write to, so we use a temporary file in the cache
        val tempOutputFile = File(context.cacheDir, "temp_export.mp4")

        // --- Create a temporary text file listing all image frames for FFmpeg ---
        val fileListContent = project.frameUris.joinToString(separator = "\n") { frameUri ->
            // FFmpeg requires the content URI to be resolved to a real file path
            val realPath = getPathFromContentUri(frameUri)
            "file '$realPath'"
        }
        val listFile = File(context.cacheDir, "filelist.txt")
        listFile.writeText(fileListContent)

        // --- Construct the FFmpeg command ---
        // -r       : Frame rate (from project speed)
        // -f concat: Use the concat format to read from our text file
        // -safe 0  : Allow file paths outside the current directory
        // -i       : The input file list
        // -c:v     : The video codec to use
        // -pix_fmt : Pixel format for compatibility
        val command = "-r ${project.speed} -f concat -safe 0 -i ${listFile.absolutePath} -c:v libx264 -pix_fmt yuv420p -y ${tempOutputFile.absolutePath}"

        onResult(false, "Export started...")

        // --- Execute the command asynchronously ---
        FFmpegKit.executeAsync(command) { session ->
            if (ReturnCode.isSuccess(session.returnCode)) {
                // Success! Now copy the temp file to its final MediaStore location
                try {
                    resolver.openFileDescriptor(finalVideoUri, "w")?.use { pfd ->
                        FileOutputStream(pfd.fileDescriptor).use { outStream ->
                            tempOutputFile.inputStream().use { inStream ->
                                inStream.copyTo(outStream)
                            }
                        }
                    }
                    onResult(true, "Export successful! Saved to Movies/LifeLapse.")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to copy file to final destination", e)
                    onResult(false, "Export failed: Could not save file.")
                }

            } else {
                // Failure
                Log.e(TAG, "FFmpeg failed with logs: ${session.allLogsAsString}")
                onResult(false, "Export failed. Please check logs.")
            }

            // Clean up the temporary files
            listFile.delete()
            tempOutputFile.delete()
        }
    }

    // Helper function to get a real file path from a content URI
    private fun getPathFromContentUri(contentUri: String): String? {
        val uri = android.net.Uri.parse(contentUri)
        var realPath: String? = null
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
                realPath = cursor.getString(index)
            }
        }
        return realPath
    }
}