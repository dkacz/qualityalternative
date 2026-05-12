package com.qualityalternative.app.data

import android.content.Context
import android.content.ContentUris
import android.content.ContentValues
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import com.qualityalternative.app.domain.service.AccountLightProfileBackupReader
import com.qualityalternative.app.domain.service.AccountLightProfileAutosaveWriter
import java.io.File
import java.io.OutputStreamWriter

class AndroidAccountLightProfileAutosaveWriter(
    private val context: Context,
) : AccountLightProfileAutosaveWriter, AccountLightProfileBackupReader {
    override suspend fun writeProfileJson(uri: String, fileName: String, json: String) {
        if (uri == DEFAULT_PROFILE_BACKUP_URI) {
            writeDefaultProfileJson(fileName = fileName, json = json)
            return
        }
        val parsedUri = Uri.parse(uri)
        if (parsedUri.scheme == "file") {
            val path = parsedUri.path?.takeIf(String::isNotBlank)
                ?: error("Could not open portable profile autosave destination.")
            val selected = File(path)
            val outputFile = if (selected.isDirectory) File(selected, fileName) else selected
            outputFile.parentFile?.mkdirs()
            outputFile.writeText(json, Charsets.UTF_8)
            return
        }
        if (DocumentsContract.isTreeUri(parsedUri)) {
            writeProfileJsonToTree(treeUri = parsedUri, fileName = fileName, json = json)
            return
        }
        context.contentResolver.openOutputStream(parsedUri, "wt")
            ?.use { stream ->
                OutputStreamWriter(stream, Charsets.UTF_8).use { writer ->
                    writer.write(json)
                }
            }
            ?: error("Could not write portable profile.")
    }

    override suspend fun readProfileJson(uri: String, fileName: String): String? {
        if (uri == DEFAULT_PROFILE_BACKUP_URI) {
            return defaultProfileDocumentUri(fileName)
                ?.let { documentUri -> runCatching { readTextFromUri(documentUri) }.getOrNull() }
                ?: readDefaultProfileJsonFromPublicDownloads(fileName)
        }
        val parsedUri = Uri.parse(uri)
        if (parsedUri.scheme == "file") {
            val path = parsedUri.path?.takeIf(String::isNotBlank) ?: return null
            val selected = File(path)
            val inputFile = if (selected.isDirectory) File(selected, fileName) else selected
            return inputFile.takeIf { it.isFile }?.readText(Charsets.UTF_8)
        }
        return readTextFromUri(parsedUri)
    }

    private fun writeDefaultProfileJson(fileName: String, json: String) {
        var hadExistingDefaultProfileDocument = false
        defaultProfileDocumentUri(fileName)?.let { profileDocumentUri ->
            hadExistingDefaultProfileDocument = true
            if (writeTextToUri(profileDocumentUri, json)) {
                return
            }
        }
        val profileDocumentUri = createDefaultProfileDocument(
            fileName = fileName,
            preferCollisionName = hadExistingDefaultProfileDocument || hasDefaultProfileFileInPublicDownloads(fileName),
        )
        if (!writeTextToUri(profileDocumentUri, json)) {
            error("Could not write default portable profile backup.")
        }
    }

    private fun writeTextToUri(uri: Uri, json: String): Boolean {
        return runCatching {
            context.contentResolver.openOutputStream(uri, "wt")
                ?.use { stream ->
                    OutputStreamWriter(stream, Charsets.UTF_8).use { writer ->
                        writer.write(json)
                    }
                } != null
        }.getOrDefault(false)
    }

    private fun createDefaultProfileDocument(fileName: String, preferCollisionName: Boolean): Uri {
        if (!preferCollisionName) {
            insertDefaultProfileDocument(displayName = fileName)?.let { return it }
        }
        insertDefaultProfileDocument(displayName = defaultProfileCollisionDisplayName(fileName))?.let { return it }
        if (preferCollisionName) {
            insertDefaultProfileDocument(displayName = fileName)?.let { return it }
        }
        error("Could not create default portable profile backup.")
    }

    private fun insertDefaultProfileDocument(displayName: String): Uri? {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
            put(MediaStore.MediaColumns.RELATIVE_PATH, DEFAULT_PROFILE_BACKUP_RELATIVE_PATH)
        }
        return runCatching {
            context.contentResolver.insert(DEFAULT_DOWNLOADS_COLLECTION, values)
        }.getOrNull()
    }

    private fun defaultProfileCollisionDisplayName(fileName: String): String {
        val dotIndex = fileName.lastIndexOf('.')
        val collisionToken = System.currentTimeMillis().toString()
        return if (dotIndex > 0) {
            "${fileName.substring(0, dotIndex)} ($collisionToken)${fileName.substring(dotIndex)}"
        } else {
            "$fileName ($collisionToken)"
        }
    }

    private fun defaultProfileDocumentUri(fileName: String): Uri? {
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.RELATIVE_PATH,
            MediaStore.MediaColumns.DATE_ADDED,
            MediaStore.MediaColumns.DATE_MODIFIED,
        )
        val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} LIKE ? AND ${MediaStore.MediaColumns.RELATIVE_PATH}=?"
        val selectionArgs = arrayOf(defaultProfileDisplayNameLike(fileName), DEFAULT_PROFILE_BACKUP_RELATIVE_PATH)
        context.contentResolver.query(
            DEFAULT_DOWNLOADS_COLLECTION,
            projection,
            selection,
            selectionArgs,
            "${MediaStore.MediaColumns.DATE_ADDED} DESC, ${MediaStore.MediaColumns._ID} DESC",
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndex(MediaStore.MediaColumns._ID)
            val nameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
            while (idIndex >= 0 && nameIndex >= 0 && cursor.moveToNext()) {
                if (isDefaultProfileFileName(fileName = fileName, displayName = cursor.getString(nameIndex))) {
                    return ContentUris.withAppendedId(DEFAULT_DOWNLOADS_COLLECTION, cursor.getLong(idIndex))
                }
            }
        }
        return null
    }

    private fun readTextFromUri(uri: Uri): String? {
        return context.contentResolver.openInputStream(uri)
            ?.bufferedReader(Charsets.UTF_8)
            ?.use { reader -> reader.readText() }
    }

    @Suppress("DEPRECATION")
    private fun readDefaultProfileJsonFromPublicDownloads(fileName: String): String? {
        val backupDirectory = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "Quality Alternative",
        )
        return backupDirectory.listFiles()
            ?.filter { file -> file.isFile && isDefaultProfileFileName(fileName = fileName, displayName = file.name) }
            ?.maxByOrNull(File::lastModified)
            ?.let { file -> runCatching { file.readText(Charsets.UTF_8) }.getOrNull() }
    }

    @Suppress("DEPRECATION")
    private fun hasDefaultProfileFileInPublicDownloads(fileName: String): Boolean {
        val backupDirectory = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "Quality Alternative",
        )
        return backupDirectory.listFiles()
            ?.any { file -> file.isFile && isDefaultProfileFileName(fileName = fileName, displayName = file.name) }
            ?: false
    }

    private fun writeProfileJsonToTree(treeUri: Uri, fileName: String, json: String) {
        val resolver = context.contentResolver
        val parentDocumentId = DocumentsContract.getTreeDocumentId(treeUri)
        val parentDocumentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, parentDocumentId)
        val existingDocumentUri = findChildDocumentUri(treeUri = treeUri, fileName = fileName)
        val profileDocumentUri = existingDocumentUri ?: DocumentsContract.createDocument(
            resolver,
            parentDocumentUri,
            "application/json",
            fileName,
        ) ?: error("Could not create portable profile file.")

        resolver.openOutputStream(profileDocumentUri, "wt")
            ?.use { stream ->
                OutputStreamWriter(stream, Charsets.UTF_8).use { writer ->
                    writer.write(json)
                }
            }
            ?: error("Could not write portable profile file.")
    }

    private fun findChildDocumentUri(treeUri: Uri, fileName: String): Uri? {
        val resolver = context.contentResolver
        val parentDocumentId = DocumentsContract.getTreeDocumentId(treeUri)
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentDocumentId)
        resolver.query(
            childrenUri,
            arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            ),
            null,
            null,
            null,
        )?.use { cursor ->
            val documentIdIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            val nameIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            while (cursor.moveToNext()) {
                if (documentIdIndex >= 0 && nameIndex >= 0 && cursor.getString(nameIndex) == fileName) {
                    return DocumentsContract.buildDocumentUriUsingTree(
                        treeUri,
                        cursor.getString(documentIdIndex),
                    )
                }
            }
        }
        return null
    }

    private fun defaultProfileDisplayNameLike(fileName: String): String {
        val extensionIndex = fileName.lastIndexOf('.')
        if (extensionIndex <= 0) {
            return "$fileName%"
        }
        val stem = fileName.substring(0, extensionIndex)
        val extension = fileName.substring(extensionIndex)
        return "$stem%$extension"
    }

    private fun isDefaultProfileFileName(fileName: String, displayName: String?): Boolean {
        val safeDisplayName = displayName ?: return false
        if (safeDisplayName == fileName) {
            return true
        }
        val extensionIndex = fileName.lastIndexOf('.')
        if (extensionIndex <= 0) {
            return false
        }
        val stem = Regex.escape(fileName.substring(0, extensionIndex))
        val extension = Regex.escape(fileName.substring(extensionIndex))
        return Regex("$stem \\(\\d+\\)$extension").matches(safeDisplayName)
    }

    companion object {
        const val DEFAULT_PROFILE_BACKUP_URI = "qualityalternative://profile-backup/default"
        const val DEFAULT_PROFILE_BACKUP_DISPLAY_NAME =
            "Downloads/Quality Alternative/quality-alternative-profile.json"
        private val DEFAULT_DOWNLOADS_COLLECTION: Uri =
            MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        private val DEFAULT_PROFILE_BACKUP_RELATIVE_PATH =
            "${Environment.DIRECTORY_DOWNLOADS}/Quality Alternative/"
    }
}
