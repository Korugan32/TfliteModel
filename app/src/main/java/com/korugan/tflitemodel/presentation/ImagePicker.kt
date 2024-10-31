package com.korugan.tflitemodel.presentation

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
import com.korugan.tflitemodel.util.predictionWithModel

@Composable
fun ImagePickerWithConvertedModel() {
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var predictionRealResult by remember { mutableStateOf(0.0) }
    var predictionFakeResult by remember { mutableStateOf(0.0) }
    var predictionResult by remember { mutableStateOf("Henüz Bir Tahmin Yapılmadı") }

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
        imageUri?.let { uri ->
            Image(
                painter = rememberImagePainter(uri),
                contentDescription = null,
                modifier = Modifier.size(200.dp)
            )
            Button(onClick = {
                predictionRealResult = predictionWithModel(context, uri) * 100
                predictionFakeResult = (100 - predictionRealResult)
                if (predictionRealResult < 0.5) {
                    predictionResult = "Tahmin : Sahte"
                } else {
                    predictionResult = "Tahmin : Gerçek"
                }
            }) {
                Text("Tahmin Yap")
            }
            Text("Real : %" + String.format("%.2f", (predictionRealResult)))
            Text("Fake : %" + String.format("%.2f", (predictionFakeResult)))
            Text(predictionResult)
        } ?: Text("Henüz Bir Resim Seçilmedi")
        Button(onClick = { launcher.launch("image/*") }) {
            Text("Galeriden Resim Seç")
        }
    }
}


