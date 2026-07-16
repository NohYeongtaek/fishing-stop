package com.example.fishingstop.feature.home.presentation

import android.content.Intent
import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.fishingstop.core.ui.components.AppBottomBar
import com.example.fishingstop.core.ui.components.AppScaffold
import com.example.fishingstop.core.ui.components.BottomBarItem
import com.example.fishingstop.feature.education.presentation.EducationScreen
import com.example.fishingstop.feature.inspect.presentation.history.HistoryScreen
import com.example.fishingstop.feature.inspect.presentation.select.InspectSelectScreen
import com.example.fishingstop.feature.settings.presentation.SettingsScreen
import com.example.fishingstop.ui.theme.AppTheme

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
    onOpenPrivacy: () -> Unit
) {
    // 선택된 하단 탭. (0:홈, 1:직접검사, 2:검사기록, 3:피싱예방, 4:설정)
    // rememberSaveable: 상세 화면에 갔다가 "뒤로"로 돌아와도 보던 탭이 유지되도록
    // 백스택 저장 상태에 함께 보존한다(remember 는 백스택 이탈 시 초기화됨).
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    AppScaffold(
        bottomBar = {
            AppBottomBar(
                items = HomeTab.entries.mapIndexed { index, tab ->
                    BottomBarItem(
                        label = tab.label,
                        icon = tab.icon,
                        selected = selectedTab == index,
                        onClick = { selectedTab = index }
                    )
                }
            )
        }
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            when (HomeTab.entries[selectedTab]) {
                HomeTab.HOME -> HomeTabContent()
                HomeTab.DIRECT_INSPECT -> InspectSelectScreen(
                    onQr = onQr, onLink = onLink, onImage = onImage, onText = onText
                )
                HomeTab.HISTORY -> HistoryScreen(onOpenResult = onOpenResult)
                HomeTab.EDUCATION -> EducationScreen(onOpenCategory = onOpenEducation)
                HomeTab.SETTINGS -> SettingsScreen(
                    onOpenNotice = onOpenNotice,
                    onOpenPrivacy = onOpenPrivacy
                )
            }
        }
    }
}

/** 홈 탭: 중앙 원형 "검사 시작" 버튼 + 메시지 공유 가이드(FO_01). */
@Composable
private fun HomeTabContent() {
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

/** 홈 하단 탭 정의(와이어프레임 5탭). */
private enum class HomeTab(val label: String, val icon: ImageVector) {
    HOME("홈", Icons.Filled.Home),
    DIRECT_INSPECT("직접검사", Icons.Filled.Search),
    HISTORY("검사기록", Icons.Filled.DateRange),
    EDUCATION("피싱예방", Icons.Filled.Info),
    SETTINGS("설정", Icons.Filled.Settings)
}
