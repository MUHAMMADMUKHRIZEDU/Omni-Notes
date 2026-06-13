package it.feio.android.omninotes.utils

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

object OCRHelper {

    interface OCRCallback {
        fun onTextExtracted(text: String)
        fun onFailure(e: Exception)
    }

    @JvmStatic
    fun extractText(context: Context, imageUri: Uri, callback: OCRCallback) {
        try {
            val image = InputImage.fromFilePath(context, imageUri)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    callback.onTextExtracted(visionText.text)
                }
                .addOnFailureListener { e ->
                    callback.onFailure(e)
                }
        } catch (e: Exception) {
            callback.onFailure(e)
        }
    }
}
