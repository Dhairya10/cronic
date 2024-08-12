package com.renalize.android.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.renalize.android.R
import java.io.File


class FileProvider: FileProvider(R.xml.file_path){

    companion object{
        fun getImageUri(context: Context): Uri {
            val directory = File(context.filesDir, "images")
            directory.mkdirs()
            val file = File.createTempFile("${System.currentTimeMillis()}", ".jpg", directory)
            val authority = "${context.packageName}.fileprovider"
            return getUriForFile(context, authority, file)
        }
    }
}