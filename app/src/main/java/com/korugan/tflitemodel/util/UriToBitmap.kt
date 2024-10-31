package com.korugan.tflitemodel.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri

fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
    return try {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
            decoder.setTargetSampleSize(1)
            decoder.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}