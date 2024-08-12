package com.renalize.android.ui.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

@Throws(IllegalStateException::class)
fun Context.getFileMetaInfo(
    uri: Uri
): FileMetaInfo {
    contentResolver.query(
        uri,
        null,
        null,
        null,
        null
    )?.use {
        val sizeIndex = it.getColumnIndexOrThrow(OpenableColumns.SIZE)
        val nameIndex = it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
        it.moveToFirst()
        return FileMetaInfo(
            size = it.getLong(sizeIndex),
            name = it.getString(nameIndex)
        )
    }
    throw IllegalStateException("Unable to get file meta info")
}

data class FileMetaInfo(
    val size: Long = 0,
    val name: String = ""
)


