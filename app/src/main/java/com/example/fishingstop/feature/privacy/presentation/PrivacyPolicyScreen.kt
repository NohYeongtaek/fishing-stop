package com.example.fishingstop.feature.privacy.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 개인정보 처리방침 전문(인앱 정적 페이지).
 *
 * Play 스토어 등록 요건상 동일 내용을 외부에서 접근 가능한 URL로도 준비해야 하며,
 * 확정되면 웹뷰로 그 URL을 로드하거나 이 인앱 텍스트와 동기화한다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("개인정보 처리방침") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Text(
                text = PRIVACY_POLICY_TEXT,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// 실제 배포 전 법무 검토를 거쳐 확정할 초안. 외부 공개 URL과 동일하게 유지한다.
private const val PRIVACY_POLICY_TEXT = """
[피싱멈춰! 개인정보 처리방침 (초안)]

1. 수집 항목 및 목적
피싱멈춰!는 사용자가 직접 공유·업로드한 메시지 텍스트 및 이미지를, 피싱 위험도 분석 목적에 한해 처리합니다. 문자 읽기 권한(SMS), 화면 캡처 권한은 사용하지 않습니다.

2. AI 분석을 위한 외부 전송
분석을 위해 입력 내용이 외부 AI 서비스로 전송될 수 있습니다. 전송은 분석에 필요한 최소한으로 제한합니다.

3. 저장 및 보관
사용자 동의 없이 문자 내용을 서버에 저장하지 않습니다. 검사 기록은 사용자의 기기(로컬)에만 저장됩니다. 분석에 사용된 이미지는 처리 직후 메모리에서 삭제합니다.

4. 신고 데이터
신고 기능 이용 시 개인정보는 제거·익명화되어 저장됩니다.

5. 결과의 성격
AI 분석 결과는 참고 정보이며, 법적 효력이나 확정 판정이 아닙니다.

6. 문의
개인정보 관련 문의: (담당자 이메일 기입 예정)
"""
