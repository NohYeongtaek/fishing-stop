package com.example.fishingstop.feature.onboarding.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.fishingstop.core.ui.components.AppScaffold
import com.example.fishingstop.core.ui.components.PrimaryButton
import com.example.fishingstop.ui.theme.AppTheme
import kotlinx.coroutines.launch

/** 온보딩 페이지 데이터. */
private data class OnboardPage(val emoji: String, val title: String, val body: String)

private val PAGES = listOf(
    OnboardPage(
        emoji = "🔎",
        title = "의심스러운 문자, 바로 확인하세요",
        body = "문자·이미지·링크·QR 코드까지, 피싱이 의심되면 간편하게 검사할 수 있어요."
    ),
    OnboardPage(
        emoji = "🛡️",
        title = "AI가 위험도를 알려드려요",
        body = "안전·주의·위험 등급과 판단 근거를 쉽게 보여드립니다. (참고용 정보예요)"
    ),
    OnboardPage(
        emoji = "🚨",
        title = "위험하면 바로 신고까지",
        body = "의심 문자는 신고하고, 사기 유형별 예방법도 익혀 피해를 막으세요."
    )
)

/**
 * 온보딩(사용설명) 화면.
 * 최초 1회만 노출되며, "시작하기"를 누르면 열람 기록 후 홈으로 이동한다.
 *
 * @param onFinish 온보딩 완료 → 홈으로
 */
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    // 이미 본 적 있으면(재진입 방어) 화면을 그리지 않고 즉시 통과시킨다.
    val alreadySeen by viewModel.alreadySeen.collectAsState()
    if (alreadySeen == true) {
        androidx.compose.runtime.LaunchedEffect(Unit) { onFinish() }
        return
    }

    val pagerState = rememberPagerState(pageCount = { PAGES.size })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == PAGES.lastIndex
    val colors = AppTheme.colors

    AppScaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(AppTheme.spacing.screenX)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                val item = PAGES[page]
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(item.emoji, fontSize = 72.sp)
                    Spacer(Modifier.size(24.dp))
                    Text(
                        item.title,
                        style = AppTheme.type.h1,
                        color = colors.textPrimary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.size(12.dp))
                    Text(
                        item.body,
                        style = AppTheme.type.body,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // 페이지 인디케이터(점)
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(PAGES.size) { index ->
                    val selected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (selected) 10.dp else 8.dp)
                            .background(
                                color = if (selected) colors.greenPrimary else colors.borderInput,
                                shape = CircleShape
                            )
                    )
                }
            }

            PrimaryButton(
                text = if (isLastPage) "시작하기" else "다음",
                onClick = {
                    if (isLastPage) {
                        viewModel.complete(onDone = onFinish)
                    } else {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    }
                }
            )
        }
    }
}
