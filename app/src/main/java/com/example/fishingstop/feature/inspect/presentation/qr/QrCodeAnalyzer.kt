package com.example.fishingstop.feature.inspect.presentation.qr

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.atomic.AtomicBoolean

/**
 * CameraX 프레임에서 QR 코드를 찾는 분석기.
 *
 * - 온디바이스(ML Kit)로 처리하며 프레임을 외부로 전송하지 않는다.
 * - 첫 인식 즉시 [onDetected] 를 "한 번만" 호출한다(중복 콜백 방지).
 * - QR 속 URL을 자동으로 열지 않는다 — 값을 검사 플로우로 넘길 뿐이다(안전 원칙).
 */
class QrCodeAnalyzer(
    private val onDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
    )

    // 인식 성공 후 추가 프레임에서 콜백이 또 오지 않도록 막는다.
    private val consumed = AtomicBoolean(false)

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null || consumed.get()) {
            imageProxy.close()
            return
        }

        val input = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(input)
            .addOnSuccessListener { barcodes ->
                val value = barcodes.firstOrNull()?.rawValue
                if (!value.isNullOrBlank() && consumed.compareAndSet(false, true)) {
                    onDetected(value)
                }
            }
            // 반드시 close 해야 다음 프레임이 들어온다.
            .addOnCompleteListener { imageProxy.close() }
    }
}
