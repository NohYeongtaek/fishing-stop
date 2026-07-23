package com.rocketdan24.fishingstop.feature.notice.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.rocketdan24.fishingstop.core.ui.components.AppTextField
import com.rocketdan24.fishingstop.core.ui.components.WarnBox
import com.rocketdan24.fishingstop.ui.theme.AppTheme

/**
 * 관리자 PIN 입력 다이얼로그.
 * 설정의 "app version" 10탭으로 열린다. 4자리 숫자 검증, 5회 실패 시 30분 잠금 안내.
 *
 * @param onSuccess PIN 통과 → 작성 화면으로 이동
 * @param onDismiss 닫기
 */
@Composable
fun AdminPinDialog(
    onSuccess: () -> Unit,
    onDismiss: () -> Unit,
    viewModel: AdminGateViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val colors = AppTheme.colors
    var pin by remember { mutableStateOf("") }

    // 다이얼로그가 뜰 때 이전 상태 초기화(잠금 여부는 verify 시점에 재확인)
    LaunchedEffect(Unit) { viewModel.reset() }

    // 통과 시 이동
    LaunchedEffect(state) {
        if (state is AdminGateUiState.Success) onSuccess()
    }

    val locked = state as? AdminGateUiState.Locked
    val verifying = state is AdminGateUiState.Verifying

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.cardBg,
        title = { Text("관리자 확인", style = AppTheme.type.cardLabel, color = colors.textPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (locked != null) {
                    WarnBox {
                        Text(
                            "비밀번호를 너무 많이 틀렸어요.\n약 ${locked.remainingMinutes}분 후 다시 시도해 주세요.",
                            style = AppTheme.type.body,
                            color = colors.warnText
                        )
                    }
                } else {
                    Text(
                        "비밀번호 4자리를 입력해 주세요.",
                        style = AppTheme.type.body,
                        color = colors.textSecondary
                    )
                    AppTextField(
                        value = pin,
                        onValueChange = { new -> if (new.length <= 4 && new.all { it.isDigit() }) pin = new },
                        label = "비밀번호",
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    when (val s = state) {
                        is AdminGateUiState.Failed -> Text(
                            "비밀번호가 올바르지 않아요. (남은 시도 ${s.remaining}회)",
                            style = AppTheme.type.caption,
                            color = colors.dangerPrimary
                        )
                        is AdminGateUiState.Error -> Text(
                            s.message,
                            style = AppTheme.type.caption,
                            color = colors.dangerPrimary
                        )
                        else -> Unit
                    }
                }
            }
        },
        confirmButton = {
            if (locked == null) {
                TextButton(
                    onClick = { viewModel.verify(pin) },
                    enabled = pin.length == 4 && !verifying
                ) {
                    if (verifying) {
                        CircularProgressIndicator(
                            color = colors.greenPrimary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.padding(end = 8.dp).size(18.dp)
                        )
                    }
                    Text("확인", color = colors.greenPrimary, style = AppTheme.type.button)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소", color = colors.textSecondary, style = AppTheme.type.button)
            }
        }
    )
}
