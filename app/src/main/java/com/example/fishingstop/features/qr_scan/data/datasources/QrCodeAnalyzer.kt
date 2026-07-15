package com.example.fishingstop.features.qr_scan.data.datasources

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

/**
 * CameraX 프레임에서 QR 코드를 온디바이스로 인식하는 Analyzer.
 * 인식에 성공할 때마다 onQrDetected로 원문 문자열을 콜백한다.
 * 실제로 "검사"를 실행할지는 화면의 검사하기 버튼(QrScanViewModel.onInspectClick)에서 결정하고,
 * 이 클래스는 최신 인식 값을 계속 갱신해서 넘겨주는 역할만 한다.
 */
class QrCodeAnalyzer(
    private val onQrDetected: (String) -> Unit,
) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient()

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                barcodes.firstOrNull { !it.rawValue.isNullOrBlank() }
                    ?.rawValue
                    ?.let(onQrDetected)
            }
            .addOnCompleteListener {
                // 다음 프레임을 계속 받기 위해 반드시 close() 해야 한다.
                imageProxy.close()
            }
    }
}
