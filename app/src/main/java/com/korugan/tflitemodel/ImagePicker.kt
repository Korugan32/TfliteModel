package com.korugan.tflitemodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.korugan.tflitemodel.ml.ConvertedModel
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

@Composable
fun ImagePickerWithConvertedModel() {
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var predictionResult by remember { mutableStateOf("Henüz tahmin yapılmadı") }
    var predictionResult1 by remember { mutableStateOf("Henüz tahmin yapılmadı") }
    var predictionRealResult by remember { mutableStateOf(0.0) }
    var predictionFakeResult by remember { mutableStateOf(0.0) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Button(onClick = { launcher.launch("image/*") }) {
            Text("Galeriden Resim Seç")
        }

        Spacer(modifier = Modifier.height(16.dp))

        imageUri?.let { uri ->
            // Resmi göster
            Image(
                painter = rememberImagePainter(uri),
                contentDescription = null,
                modifier = Modifier.size(200.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tahmin yap butonu
            Button(onClick = {
                val bitmap = uriToBitmap(context, uri)
                bitmap?.let {
                    // Modeli oluştur
                    val model = ConvertedModel.newInstance(context)

                    // Resmi uygun boyutlara ölçeklendir
                    val scaledBitmap = Bitmap.createScaledBitmap(it, 128, 128, true) // Modelin giriş boyutuna göre ayarlayın
                    val byteBuffer = convertBitmapToByteBuffer(scaledBitmap)

                    // Input tensor oluştur
                    val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 128, 128, 3), DataType.FLOAT32)
                    inputFeature0.loadBuffer(byteBuffer)

                    // Modeli çalıştır ve sonucu al
                    val outputs = model.process(inputFeature0)
                    val outputFeature0 = outputs.outputFeature0AsTensorBuffer


                    // Tahmin sonucunu ayarla
                    predictionResult = "Tahmin Sonucu: ${(outputFeature0.floatArray).joinToString()}"
                     if(outputFeature0.floatArray[0]>0.5){
                         predictionResult1 = "Gerçek"
                     }else{
                         predictionResult1 = "Sahte"
                     }
                    predictionRealResult = outputFeature0.floatArray[0].toDouble()*100
                    predictionFakeResult = (1-outputFeature0.floatArray[0].toDouble())*100
                    // Model kaynaklarını serbest bırak
                    model.close()
                }
            }) {
                Text("Tahmin Yap")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(predictionResult)
            Text(predictionResult1)
            Text("Real : %"+predictionRealResult.toString())
            Text("Fake : %"+predictionFakeResult.toString())
        } ?: Text("Henüz bir resim seçilmedi")
    }
}

// Seçilen URI'yi Bitmap'e çevir
fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
    return try {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        // Bitmap'i ARGB_8888 formatında oluştur
        ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
            decoder.setTargetSampleSize(1)
            decoder.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// Bitmap'i ByteBuffer'a dönüştür
fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
    val byteBuffer = ByteBuffer.allocateDirect(4 * 128 * 128 * 3) // 128x128 boyutları için
    byteBuffer.order(ByteOrder.nativeOrder())
    val intValues = IntArray(128 * 128)
    bitmap.getPixels(intValues, 0, 128, 0, 0, 128, 128)
    var pixel = 0
    for (i in 0 until 128) {
        for (j in 0 until 128) {
            val value = intValues[pixel++]
            byteBuffer.putFloat(((value shr 16 and 0xFF) - 127.5f) / 127.5f) // Kırmızı
            byteBuffer.putFloat(((value shr 8 and 0xFF) - 127.5f) / 127.5f)  // Yeşil
            byteBuffer.putFloat(((value and 0xFF) - 127.5f) / 127.5f)      // Mavi
        }
    }
    return byteBuffer
}
