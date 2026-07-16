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
 * - 매 프레임 감지 결과를 [onResult] 로 알린다(값을 찾으면 그 값, 못 찾으면 null).
 *   화면에 "QR 감지됨/안 보임" 상태를 실시간으로 보여주기 위함이다.
 * - QR 속 URL을 자동으로 열지 않는다 — 값을 검사 플로우로 넘길 뿐이다(안전 원칙).
 */
class QrCodeAnalyzer(
    private val onResult: (String?) -> Unit
) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
    )

    // 이전 프레임 분석이 끝나기 전까지는 다음 프레임을 건너뛴다(ML Kit 호출 누적 방지).
    private val busy = AtomicBoolean(false)

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null || !busy.compareAndSet(false, true)) {
            imageProxy.close()
            return
        }

        val input = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(input)
            .addOnSuccessListener { barcodes ->
                onResult(barcodes.firstOrNull()?.rawValue?.takeIf { it.isNotBlank() })
            }
            .addOnFailureListener { onResult(null) }
            // 반드시 close 해야 다음 프레임이 들어온다.
            .addOnCompleteListener {
                busy.set(false)
                imageProxy.close()
            }
    }
}
