package com.bandbbs.ebook.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import kotlin.math.roundToInt


fun UritoFile(uri: Uri?, context: Context): File? {
    var file: File? = null
    if (uri == null) return file
    //android10以上转换
    if (uri.scheme == ContentResolver.SCHEME_FILE) {
        file = File(uri.path!!)
    } else if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
        //把文件复制到沙盒目录
        val contentResolver = context.contentResolver
        val cursor = contentResolver.query(uri, null, null, null, null)
        val displayName = cursor?.use {
            if (it.moveToFirst()) {
                it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
            } else {
                null
            }
        } ?: ((System.currentTimeMillis() + ((Math.random() + 1) * 1000).roundToInt()
                ).toString() + "." + MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri)))

        try {
            val `is`: InputStream = contentResolver.openInputStream(uri)!!
            val cache = File(context.cacheDir.absolutePath, displayName)
            val fos = FileOutputStream(cache)
            // ▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼
            // 修改部分：
            // 移除了只在API 29+才执行的 if 判断，
            // 并调用 GetFilePathFromUri 中公开的 copyStream 方法来复制文件内容
            GetFilePathFromUri.copyStream(`is`, fos)
            // ▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲

            file = cache
            fos.close()
            `is`.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    return file
}
