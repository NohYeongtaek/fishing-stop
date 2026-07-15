package com.example.fishingstop.feature.inspect.data

import android.net.Uri
import com.example.fishingstop.core.util.IoDispatcher
import com.example.fishingstop.feature.inspect.data.ocr.MlKitOcrDataSource
import com.example.fishingstop.feature.inspect.domain.OcrRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * OCR 저장소 구현체. ML Kit 데이터소스를 IO 디스패처에서 호출한다.
 */
class OcrRepositoryImpl @Inject constructor(
    private val ocrDataSource: MlKitOcrDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : OcrRepository {

    override suspend fun extractText(imageUri: Uri): String =
        withContext(ioDispatcher) { ocrDataSource.recognize(imageUri) }
}
