package com.rocketdan24.fishingstop.feature.home.presentation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.rocketdan24.fishingstop.core.ui.components.AppBottomBar
import com.rocketdan24.fishingstop.core.ui.components.AppScaffold
import com.rocketdan24.fishingstop.core.ui.components.BottomBarItem
import com.rocketdan24.fishingstop.feature.education.presentation.EducationScreen
import com.rocketdan24.fishingstop.feature.inspect.presentation.history.HistoryScreen
import com.rocketdan24.fishingstop.feature.inspect.presentation.select.InspectSelectScreen
import com.rocketdan24.fishingstop.feature.settings.presentation.SettingsScreen
import com.rocketdan24.fishingstop.ui.theme.AppTheme

/**
 * 홈 화면(하단 탭 컨테이너) — 와이어프레임 확정 5탭 구조.
 *
 * 탭: 홈 / 직접검사 / 검사기록 / 피싱예방 / 설정
 *  - 홈: 중앙 큰 원형 "검사 시작" 버튼(메시지 앱 열기) + 공유 방법 가이드
 *  - 직접검사: QR·링크·이미지·문자 4종 선택(FO_02)
 *
 * 선택 탭 표시는 (알약 인디케이터가 아니라) 아이콘+글씨 색 변화 방식을 유지한다(AppBottomBar).
 *
 * @param onQr/onLink/onImage/onText 직접검사 방식별 화면으로 이동
 * @param onOpenResult 검사기록 항목 → 결과 화면
 * @param onOpenEducation 예방교육 카테고리 → 상세
 */
@Composable
fun HomeScreen(
    onQr: () -> Unit,
    onLink: () -> Unit,
    onImage: () -> Unit,
    onText: () -> Unit,
    onOpenResult: (Long) -> Unit,
    onOpenEducation: (String) -> Unit,
    onOpenNotice: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenAdmin: () -> Unit,
    coachMarkViewModel: HomeCoachMarkViewModel = hiltViewModel()
) {
    // 선택된 하단 탭. (0:홈, 1:직접검사, 2:검사기록, 3:피싱예방, 4:설정)
    // rememberSaveable: 상세 화면에 갔다가 "뒤로"로 돌아와도 보던 탭이 유지되도록
    // 백스택 저장 상태에 함께 보존한다(remember 는 백스택 이탈 시 초기화됨).
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    // 공지사항 FCM 알림을 보여주려면 API 33+에서 런타임 권한이 필요하다.
    // 온보딩을 마치고 처음 홈에 들어왔을 때 한 번 요청한다(이후 거부/허용은 시스템이 알아서 처리).
    val context = LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {}
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // 하단 탭 코치마크: 최초 1회만, 하단 탭 5개(0~4)를 순서대로 하이라이트하면서
    // 실제 그 탭 화면으로 이동시켜 보여주고, 마지막(설정) 탭에서는 이어서 어르신 모드
    // 행까지 하나 더 짚어준다. 뚫린 영역을 탭하면 다음으로 넘어가고,
    // 전부 끝나면 열람 여부가 저장된다.
    // "홈" 탭(인덱스 0) 다음에는 탭이 아니라 ① 중앙 "검사 시작" 버튼(인덱스 1),
    // ② 사진 가이드(PhotoGuideOverlay, 인덱스 2)를 순서대로 끼워 넣는다.
    // 그 뒤 탭들의 순번은 두 칸씩 밀린다.
    val showCoachMark by coachMarkViewModel.showCoachMark.collectAsState()
    val startButtonStepIndex = 1
    val photoGuideStepIndex = 2
    val coachMarkState = rememberCoachMarkState(stepCount = HomeTab.entries.size + 3)

    LaunchedEffect(coachMarkState.currentIndex) {
        val index = coachMarkState.currentIndex
        when {
            index == 0 -> selectedTab = 0
            index == startButtonStepIndex -> Unit
            index == photoGuideStepIndex -> Unit
            index - 2 in HomeTab.entries.indices -> selectedTab = index - 2
        }
    }

    Box(Modifier.fillMaxSize()) {
        AppScaffold(
            bottomBar = {
                AppBottomBar(
                    items = HomeTab.entries.mapIndexed { index, tab ->
                        // 홈(0)은 코치마크 인덱스 0, 그 다음 탭들은 검사 시작 버튼·사진 가이드
                        // 두 단계만큼 밀려 인덱스 3부터 이어진다.
                        val coachMarkIndex = if (index == 0) 0 else index + 2
                        BottomBarItem(
                            label = tab.label,
                            icon = tab.icon,
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            iconModifier = Modifier.coachMarkTarget(
                                state = coachMarkState,
                                index = coachMarkIndex,
                                shape = CoachMarkShape.CIRCLE,
                                tooltip = { CoachMarkTooltip(tab.description) }
                            )
                        )
                    }
                )
            }
        ) { innerPadding ->
            Box(Modifier.fillMaxSize().padding(innerPadding)) {
                when (HomeTab.entries[selectedTab]) {
                    HomeTab.HOME -> HomeTabContent(
                        startButtonCoachMarkModifier = Modifier.coachMarkTarget(
                            state = coachMarkState,
                            index = startButtonStepIndex,
                            shape = CoachMarkShape.CIRCLE,
                            tooltip = { CoachMarkTooltip("눌러서 메시지 앱을 바로 열 수 있어요") }
                        )
                    )
                    HomeTab.DIRECT_INSPECT -> InspectSelectScreen(
                        onQr = onQr, onLink = onLink, onImage = onImage, onText = onText
                    )
                    HomeTab.HISTORY -> HistoryScreen(onOpenResult = onOpenResult)
                    HomeTab.EDUCATION -> EducationScreen(onOpenCategory = onOpenEducation)
                    HomeTab.SETTINGS -> SettingsScreen(
                        onOpenNotice = onOpenNotice,
                        onOpenPrivacy = onOpenPrivacy,
                        onOpenAdmin = onOpenAdmin,
                        elderModeCoachMarkModifier = Modifier.coachMarkTarget(
                            state = coachMarkState,
                            index = HomeTab.entries.size + 2,
                            shape = CoachMarkShape.RECT,
                            tooltip = { CoachMarkTooltip("글씨를 크게, 화면을 또렷하게 보여드려요") }
                        )
                    )
                }
            }
        }

        if (showCoachMark) {
            if (coachMarkState.currentIndex == photoGuideStepIndex) {
                PhotoGuideOverlay(
                    onCompleted = {
                        if (coachMarkState.advance()) coachMarkViewModel.markSeen()
                    }
                )
            } else {
                CoachMarkOverlay(
                    state = coachMarkState,
                    onCompleted = { coachMarkViewModel.markSeen() }
                )
            }
        }
    }
}

/** 코치마크 하이라이트 옆에 뜨는 말풍선 텍스트. */
@Composable
private fun CoachMarkTooltip(text: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = AppTheme.colors.cardBg,
        shadowElevation = 4.dp,
        modifier = Modifier.widthIn(max = 200.dp)
    ) {
        Text(
            text = text,
            style = AppTheme.type.body,
            color = AppTheme.colors.textPrimary,
            modifier = Modifier.padding(12.dp)
        )
    }
}

/** 홈 탭: 중앙 원형 "검사 시작" 버튼 + 메시지 공유 가이드(FO_01). */
@Composable
private fun HomeTabContent(startButtonCoachMarkModifier: Modifier = Modifier) {
    val context = LocalContext.current
    val colors = AppTheme.colors
    val circle = AppTheme.sizes.homeCircle
    val elder = AppTheme.elder

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppTheme.spacing.screenX),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 중앙 큰 원형 버튼(와이어프레임): 누르면 기본 메시지 앱을 연다.
        // 물결 애니메이션(RippleWaves)으로 주목도를 높인다 — 기존 인터랙션 유지.
        Box(
            modifier = Modifier.size(circle + 96.dp),
            contentAlignment = Alignment.Center
        ) {
            RippleWaves(color = colors.greenPrimary)

            // 어르신 모드: 원 둘레에 4dp 흰 테두리(스펙)로 대비를 높인다.
            val buttonModifier = Modifier
                .size(circle)
                .then(
                    if (elder) Modifier.border(4.dp, Color.White, CircleShape) else Modifier
                )
                .then(startButtonCoachMarkModifier)

            Button(
                onClick = { openMessagingApp(context) },
                shape = CircleShape,
                modifier = buttonModifier,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = colors.onGreen
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.radialGradient(colors.greenGradient),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("검사 시작", style = AppTheme.type.h1, color = colors.onGreen, textAlign = TextAlign.Center)
                }
            }
        }

        Text(
            text = "메시지 앱에서 의심 문자를 길게 눌러\n[공유] → [피싱멈춰!] 를 선택하면\n바로 검사할 수 있어요.",
            style = AppTheme.type.body,
            textAlign = TextAlign.Center,
            color = colors.textSecondary,
            modifier = Modifier.padding(top = 32.dp)
        )
    }
}

/** 버튼 주변으로 퍼지며 사라지는 물결 3개를 위상차를 두고 반복 재생한다. 크기는 Box에 비례. */
@Composable
private fun RippleWaves(color: Color) {
    val waveCount = 3
    val durationMillis = 3000
    val infiniteTransition = rememberInfiniteTransition(label = "ripple")
    val progresses = List(waveCount) { i ->
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis, easing = LinearEasing),
                initialStartOffset = StartOffset(durationMillis / waveCount * i)
            ),
            label = "wave$i"
        )
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val maxR = size.minDimension / 2f
        val baseRadius = maxR * 0.72f
        val extra = maxR * 0.28f
        progresses.forEach { progress ->
            val value = progress.value
            drawCircle(
                color = color,
                radius = baseRadius + extra * value,
                alpha = (1f - value) * 0.4f
            )
        }
    }
}

/** 기본 메시지 앱을 연다. 없으면 안내 토스트. */
private fun openMessagingApp(context: android.content.Context) {
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_MESSAGING)
    runCatching { context.startActivity(intent) }
        .onFailure {
            Toast.makeText(context, "메시지 앱을 열 수 없습니다. '직접검사' 탭을 이용해 주세요.", Toast.LENGTH_LONG).show()
        }
}

/** 홈 하단 탭 정의(와이어프레임 5탭). description은 코치마크 안내 문구로 쓰인다. */
private enum class HomeTab(val label: String, val icon: ImageVector, val description: String) {
    HOME("홈", Icons.Filled.Home, "메시지 앱에서 공유하면 바로 검사할 수 있어요"),
    DIRECT_INSPECT("직접검사", Icons.Filled.Search, "문자·링크·이미지·QR을 직접 검사해보세요"),
    HISTORY("검사기록", Icons.Filled.DateRange, "지난 검사 결과를 여기서 확인하세요"),
    EDUCATION("피싱예방", Icons.Filled.Info, "피싱 사기 예방 정보를 알려드려요"),
    SETTINGS("설정", Icons.Filled.Settings, "화면 테마 등 여러 설정을 바꿀 수 있어요")
}
