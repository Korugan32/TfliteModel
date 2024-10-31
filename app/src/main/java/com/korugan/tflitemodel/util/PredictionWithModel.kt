package com.korugan.tflitemodel.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.korugan.tflitemodel.ml.ConvertedModel
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

fun predictionWithModel(context: Context, uri: Uri): Double {
    val bitmap = uriToBitmap(context, uri)
    var predictionResult = 0.0
    bitmap?.let {
        val model = ConvertedModel.newInstance(context)

        val scaledBitmap = Bitmap.createScaledBitmap(it, 128, 128, true) // Modelin giriş boyutuna göre ayarlayın
        val byteBuffer = convertBitmapToByteBuffer(scaledBitmap)

        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 128, 128, 3), DataType.FLOAT32)
        inputFeature0.loadBuffer(byteBuffer)

        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer

        predictionResult = outputFeature0.floatArray[0].toDouble()
        model.close()
    }
    return predictionResult
}
