package com.korugan.tflitemodel.util

import android.graphics.Bitmap
import java.nio.ByteBuffer
import java.nio.ByteOrder

fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
    val byteBuffer = ByteBuffer.allocateDirect(4 * 128 * 128 * 3)
    byteBuffer.order(ByteOrder.nativeOrder())
    val intValues = IntArray(128 * 128)
    bitmap.getPixels(intValues, 0, 128, 0, 0, 128, 128)
    var pixel = 0
    for (i in 0 until 128) {
        for (j in 0 until 128) {
            val value = intValues[pixel++]
            byteBuffer.putFloat(((value shr 16 and 0xFF) - 127.5f) / 127.5f)
            byteBuffer.putFloat(((value shr 8 and 0xFF) - 127.5f) / 127.5f)
            byteBuffer.putFloat(((value and 0xFF) - 127.5f) / 127.5f)
        }
    }
    return byteBuffer
}