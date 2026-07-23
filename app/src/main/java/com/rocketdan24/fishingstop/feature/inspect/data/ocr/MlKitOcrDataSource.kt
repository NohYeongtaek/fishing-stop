package com.rocketdan24.fishingstop.feature.inspect.data.ocr

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognizer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * ML Kit 온디바이스 텍스트 인식(OCR) 데이터소스.
 *
 * - 이미지를 서버로 보내지 않고 기기 내에서 처리한다(개인정보 원칙).
 * - InputImage.fromFilePath 는 내부적으로 이미지를 로드/해제하므로,
 *   우리가 Bitmap 참조를 들고 있지 않아 "분석 후 이미지 즉시 삭제" 원칙에 부합한다.
 * - ML Kit Task(콜백 API)를 코루틴 suspend 함수로 감싼다(추가 의존성 없이).
 */
class MlKitOcrDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val recognizer: TextRecognizer
) {
    suspend fun recognize(uri: Uri): String = suspendCancellableCoroutine { cont ->
        val image = try {
            InputImage.fromFilePath(context, uri)
        } catch (e: Exception) {
            cont.resumeWithException(e)
            return@suspendCancellableCoroutine
        }

        recognizer.process(image)
            .addOnSuccessListener { visionText -> cont.resume(visionText.text) }
            .addOnFailureListener { e -> cont.resumeWithException(e) }
    }
}
