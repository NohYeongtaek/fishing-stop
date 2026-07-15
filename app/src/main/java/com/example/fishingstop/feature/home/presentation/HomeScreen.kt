package com.example.fishingstop.feature.home.presentation

import android.content.Intent
import android.widget.Toast
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fishingstop.feature.education.presentation.EducationScreen
import com.example.fishingstop.feature.inspect.presentation.history.HistoryScreen
import com.example.fishingstop.feature.inspect.presentation.select.InspectSelectScreen
import com.example.fishingstop.feature.settings.presentation.SettingsScreen

/**
 * 홈 화면(하단 탭 컨테이너) — 와이어프레임 확정 5탭 구조.
 *
 * 탭: 홈 / 직접검사 / 검사기록 / 피싱예방 / 설정
 *  - 홈: 중앙 큰 원형 "검사 시작" 버튼(메시지 앱 열기) + 공유 방법 가이드
 *  - 직접검사: QR·링크·이미지·문자 4종 선택(FO_02)
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

    Scaffold(
        bottomBar = {
            NavigationBar {
                HomeTab.entries.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 중앙 큰 원형 버튼(와이어프레임): 누르면 기본 메시지 앱을 연다.
        Button(
            onClick = { openMessagingApp(context) },
            shape = CircleShape,
            modifier = Modifier.size(200.dp)
        ) {
            Text("검사 시작", fontSize = 24.sp, textAlign = TextAlign.Center)
        }

        Text(
            text = "메시지 앱에서 의심 문자를 길게 눌러\n[공유] → [피싱멈춰!] 를 선택하면\n바로 검사할 수 있어요.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 32.dp)
        )
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
