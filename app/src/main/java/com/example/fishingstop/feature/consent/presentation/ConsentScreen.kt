package com.example.fishingstop.feature.consent.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.fishingstop.core.ui.components.PrimaryButton

/**
 * 최초 실행 동의 화면. (동의 필수)
 *
 * 스펙 요구사항 반영:
 *  - 메시지/이미지 내용을 AI 분석을 위해 외부로 전송한다는 안내를 명시
 *  - 개인정보 처리방침 전문 링크(인앱 페이지) 제공
 *  - 명시적 동의 버튼. 동의해야만 다음 화면으로 진입하며, 거부하면 앱을 종료한다.
 *
 * @param onAgreed 동의 저장 완료 후 다음 화면(온보딩/홈)으로 이동
 * @param onDecline 동의하지 않음 → 앱 종료
 * @param onOpenPrivacyPolicy 개인정보 처리방침 전문 화면으로 이동
 */
@Composable
fun ConsentScreen(
    onAgreed: () -> Unit,
    onDecline: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    viewModel: ConsentViewModel = hiltViewModel()
) {
    // 실수 종료를 막기 위한 확인 다이얼로그
    var showDeclineDialog by remember { mutableStateOf(false) }
    if (showDeclineDialog) {
        AlertDialog(
            onDismissRequest = { showDeclineDialog = false },
            title = { Text("동의하지 않으시겠어요?") },
            text = { Text("피싱멈춰!는 동의하셔야 이용할 수 있어요.\n동의하지 않으면 앱이 종료됩니다.") },
            confirmButton = {
                TextButton(onClick = onDecline) {
                    Text("종료", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeclineDialog = false }) { Text("계속 보기") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "피싱멈춰! 이용을 위한 동의",
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = "이 앱은 사용자가 직접 공유하거나 업로드한 메시지·이미지의 내용을 " +
                "피싱 위험도 분석을 위해 외부 AI 서비스로 전송합니다. " +
                "문자 읽기 권한(SMS)이나 화면 캡처 권한은 사용하지 않으며, " +
                "분석 후 이미지는 즉시 메모리에서 삭제됩니다.",
            style = MaterialTheme.typography.bodyLarge
        )

        Text(
            text = "• 개인정보는 분석에 필요한 최소한만 전송됩니다.\n" +
                "• 사용자 동의 없이 문자 내용을 서버에 저장하지 않습니다.\n" +
                "• 신고 시 개인정보는 익명화되어 저장됩니다.\n" +
                "• AI 분석 결과는 참고 정보이며 법적 증거가 아닙니다.",
            style = MaterialTheme.typography.bodyMedium
        )

        TextButton(onClick = onOpenPrivacyPolicy) {
            Text(text = "개인정보 처리방침 전문 보기")
        }

        Spacer(modifier = Modifier.height(8.dp))

        PrimaryButton(
            text = "위 내용에 동의하고 시작하기",
            onClick = { viewModel.agree(onCompleted = onAgreed) }
        )

        // 동의는 필수: 거부하면 앱을 종료한다.
        OutlinedButton(
            onClick = { showDeclineDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("동의하지 않음")
        }

        Text(
            text = "동의하지 않으면 앱을 이용할 수 없어 종료됩니다.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
