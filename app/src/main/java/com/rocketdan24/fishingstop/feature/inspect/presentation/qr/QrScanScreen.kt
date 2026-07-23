package com.rocketdan24.fishingstop.feature.inspect.presentation.qr

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.draw.clipToBounds
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
import com.rocketdan24.fishingstop.core.ui.components.AppScaffold
import com.rocketdan24.fishingstop.core.ui.components.AppTopBar
import com.rocketdan24.fishingstop.core.ui.components.PrimaryButton
import com.rocketdan24.fishingstop.core.ui.components.SecondaryButton
import com.rocketdan24.fishingstop.ui.theme.AppTheme
import java.util.concurrent.Executors

/**
 * QR 검사 화면(FO_02_01).
 *
 * - CameraX 프리뷰(고정 크기) + ML Kit 바코드 스캔(온디바이스), 항상 프레임을 분석해
 *   화면에 QR이 보이는지 여부를 실시간으로 안내한다.
 * - "QR코드 검사하기" 버튼을 눌렀을 때 QR이 보이면 그 값으로 검사 플로우로 넘어가고,
 *   보이지 않으면 검사할 수 없다는 안내만 하고 화면에 머문다.
 * - CAMERA 런타임 권한: 거부 시 안내, "다시 묻지 않음"까지 거부되면 설정 이동 버튼 제공.
 * - QR 속 URL은 절대 자동으로 열지 않는다 — 검사로만 전달(안전 원칙).
 *
 * @param onDetected QR 값 인식 → 분석 화면으로 이동
 */
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

    AppScaffold(
        topBar = { AppTopBar(title = "QR 코드 검사", onBack = onBack) }
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            if (hasPermission) {
                // 카메라가 매 프레임 분석한 결과. null이면 화면에 QR이 안 보이는 상태.
                var qrValue by remember { mutableStateOf<String?>(null) }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(AppTheme.spacing.screenX),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (qrValue != null) "QR코드가 감지되었어요" else "QR코드가 보이지 않아요",
                        style = AppTheme.type.subtitle,
                        color = if (qrValue != null) AppTheme.colors.greenPrimary else AppTheme.colors.textSecondary,
                        modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)
                    )
                    QrCameraPreview(
                        onResult = { qrValue = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clipToBounds()
                    )
                    PrimaryButton(
                        text = "QR코드 검사하기",
                        onClick = {
                            val value = qrValue
                            if (value != null) {
                                onDetected(value)
                            } else {
                                Toast.makeText(
                                    context,
                                    "QR코드가 보이지 않아 검사할 수 없어요.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 48.dp)
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(AppTheme.spacing.screenX),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "QR 검사를 하려면 카메라 권한이 필요해요.",
                        style = AppTheme.type.subtitle,
                        color = AppTheme.colors.textPrimary
                    )
                    PrimaryButton(
                        text = "카메라 권한 허용하기",
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        modifier = Modifier.padding(top = 24.dp)
                    )
                    if (denied) {
                        // "다시 묻지 않음" 거부 시 시스템 창이 뜨지 않으므로 설정으로 안내한다.
                        SecondaryButton(
                            text = "설정에서 권한 허용하기",
                            onClick = {
                                val intent = Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts("package", context.packageName, null)
                                )
                                runCatching { context.startActivity(intent) }
                            },
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * CameraX 프리뷰 + QR 분석 파이프라인. 화면을 떠나면 카메라/스레드를 정리한다.
 *
 * @param onResult 매 프레임 분석 결과(값 또는 null = 화면에 QR 없음)를 알린다.
 */
@Composable
private fun QrCameraPreview(
    onResult: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnResult by rememberUpdatedState(onResult)

    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose {
            // 화면 이탈 시 카메라 바인딩 해제 + 분석 스레드 종료
            runCatching { ProcessCameraProvider.getInstance(context).get().unbindAll() }
            analysisExecutor.shutdown()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                // SurfaceView(기본 PERFORMANCE 모드)는 다른 컴포저블과 겹칠 때
                // Compose 레이아웃 경계를 무시하고 위로 그려지는 문제가 있어
                // 전체화면이 아닌 이 화면에서는 TextureView 기반으로 강제한다.
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }
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
                            QrCodeAnalyzer(
                                onResult = { value ->
                                    // 콜백은 분석 스레드에서 오므로 메인 스레드로 넘긴다.
                                    previewView.post { currentOnResult(value) }
                                }
                            )
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
