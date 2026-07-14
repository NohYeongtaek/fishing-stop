package com.example.fishingstop.feature.inspect.presentation.qr

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.fishingstop.core.ui.components.PrimaryButton
import java.util.concurrent.Executors

/**
 * QR 검사 화면(FO_02_01).
 *
 * - CameraX 프리뷰 + ML Kit 바코드 스캔(온디바이스).
 * - QR을 화면에 비추면 자동 인식되어 곧바로 검사 플로우로 넘어간다.
 *   (별도 촬영 버튼보다 실수·조작이 적어 어르신 사용성에 유리)
 * - CAMERA 런타임 권한: 거부 시 안내, "다시 묻지 않음"까지 거부되면 설정 이동 버튼 제공.
 * - QR 속 URL은 절대 자동으로 열지 않는다 — 검사로만 전달(안전 원칙).
 *
 * @param onDetected QR 값 인식 → 분석 화면으로 이동
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScanScreen(
    onDetected: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // 최초 권한 상태
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    // 권한 요청을 한 번이라도 거부당했는지(설정 이동 안내용)
    var denied by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (!granted) denied = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("QR 코드 검사") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            if (hasPermission) {
                QrCameraPreview(onDetected = onDetected)
                Text(
                    "QR코드를 화면 중앙에 맞춰 주세요",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 24.dp)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "QR 검사를 하려면 카메라 권한이 필요해요.",
                        style = MaterialTheme.typography.titleMedium
                    )
                    PrimaryButton(
                        text = "카메라 권한 허용하기",
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        modifier = Modifier.padding(top = 24.dp)
                    )
                    if (denied) {
                        // "다시 묻지 않음" 거부 시 시스템 창이 뜨지 않으므로 설정으로 안내한다.
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts("package", context.packageName, null)
                                )
                                runCatching { context.startActivity(intent) }
                            },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) { Text("설정에서 권한 허용하기") }
                    }
                }
            }
        }
    }
}

/** CameraX 프리뷰 + QR 분석 파이프라인. 화면을 떠나면 카메라/스레드를 정리한다. */
@Composable
private fun QrCameraPreview(onDetected: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnDetected by rememberUpdatedState(onDetected)

    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose {
            // 화면 이탈 시 카메라 바인딩 해제 + 분석 스레드 종료
            runCatching { ProcessCameraProvider.getInstance(context).get().unbindAll() }
            analysisExecutor.shutdown()
        }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val providerFuture = ProcessCameraProvider.getInstance(ctx)
            providerFuture.addListener({
                val provider = providerFuture.get()

                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }
                val analysis = ImageAnalysis.Builder()
                    // 최신 프레임만 분석(지연 누적 방지)
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(
                            analysisExecutor,
                            QrCodeAnalyzer { value ->
                                // 콜백은 분석 스레드에서 오므로 메인 스레드로 넘긴다.
                                previewView.post { currentOnDetected(value) }
                            }
                        )
                    }

                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analysis
                )
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        }
    )
}
