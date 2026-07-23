package com.rocketdan24.fishingstop.feature.notice.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.rocketdan24.fishingstop.core.ui.components.AppScaffold
import com.rocketdan24.fishingstop.core.ui.components.AppTopBar
import com.rocketdan24.fishingstop.core.ui.components.PrimaryButton
import com.rocketdan24.fishingstop.core.ui.components.SecondaryButton
import com.rocketdan24.fishingstop.ui.theme.AppTheme

/**
 * 관리자 화면. 설정의 숨은 PIN 게이트를 통과하면 진입한다.
 * 공지 작성 / 공지 관리(수정·삭제) 두 갈래로 나눈다.
 *
 * @param onWrite  공지 작성 화면으로
 * @param onManage 공지 관리 화면으로
 * @param onBack   뒤로
 */
@Composable
fun AdminHomeScreen(
    onWrite: () -> Unit,
    onManage: () -> Unit,
    onBack: () -> Unit
) {
    val colors = AppTheme.colors

    AppScaffold(
        topBar = { AppTopBar(title = "관리자", onBack = onBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = AppTheme.spacing.screenX, vertical = AppTheme.spacing.cardPad),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stackGap)
        ) {
            Text(
                "공지사항을 작성하거나 관리합니다.",
                style = AppTheme.type.subtitle,
                color = colors.textSecondary
            )
            PrimaryButton(text = "공지 작성하기", onClick = onWrite)
            SecondaryButton(text = "공지 관리하기", onClick = onManage)
        }
    }
}
