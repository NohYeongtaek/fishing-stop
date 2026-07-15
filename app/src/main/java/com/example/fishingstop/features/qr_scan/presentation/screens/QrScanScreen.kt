package com.example.fishingstop.features.qr_scan.presentation.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fishingstop.features.qr_scan.data.datasources.QrCodeAnalyzer
import com.example.fishingstop.features.qr_scan.presentation.viewmodels.QrScanUiState
import com.example.fishingstop.features.qr_scan.presentation.viewmodels.QrScanViewModel
import com.example.fishingstop.features.risk_engine.domain.entities.RiskAnalysisResult
import java.util.concurrent.Executors

/**
 * QR 코드 촬영 화면.
 *
 * 안내 문구 아래에 카메라 미리보기를 화면 전체가 아닌 중앙의 직사각형 박스 안에만 표시한다.
 * 하단의 "검사하기" 버튼을 누르면 그 시점까지 인식된 QR 값을 URL로 만들어
 * 공통 검사 파이프라인(InspectTextUseCase → risk_engine)으로 넘긴다.
 *
 * @param onInspectionComplete 검사 완료 시 결과 화면으로 이동시키기 위한 콜백. 실제 네비게이션은 NavHost 쪽에서 처리한다.
 */
@Composable
fun QrScanScreen(
    onInspectionComplete: (RiskAnalysisResult) -> Unit,
    viewModel: QrScanViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED,
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // 검사가 끝나면(state가 Completed) 결과 화면으로 이동하도록 호출부에 알려준다.
    LaunchedEffect(uiState) {
        val state = uiState
        if (state is QrScanUiState.Completed) {
            onInspectionComplete(state.result)
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                if (hasCameraPermission) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "QR코드가 화면 중앙에 위치하게 찍어주세요",
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                        )

                        // 카메라 미리보기를 화면 전체가 아니라 이 직사각형 박스 안에만 담는다.
                        CameraPreview(
                            onQrDetected = viewModel::onQrDetected,
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .aspectRatio(3f / 4f)
                                .clip(RoundedCornerShape(20.dp))
                                .border(
                                    width = 3.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(20.dp),
                                ),
                        )
                    }
                } else {
                    Text(
                        text = "QR 코드를 촬영하려면 카메라 권한이 필요합니다.",
                        modifier = Modifier.padding(24.dp),
                    )
                }
            }

            val failedMessage = (uiState as? QrScanUiState.Failed)?.message
            if (failedMessage != null) {
                Text(
                    text = failedMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }

            Button(
                onClick = viewModel::onInspectClick,
                enabled = hasCameraPermission && uiState !is QrScanUiState.Analyzing,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                if (uiState is QrScanUiState.Analyzing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text("검사하기")
                }
            }
        }
    }
}

@Composable
private fun CameraPreview(
    onQrDetected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    // Analyzer는 별도 스레드에서 돌아가야 카메라 프리뷰(메인 스레드)가 끊기지 않는다.
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { analysis ->
                        analysis.setAnalyzer(cameraExecutor, QrCodeAnalyzer(onQrDetected = onQrDetected))
                    }

                runCatching {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis,
                    )
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
    )
}
