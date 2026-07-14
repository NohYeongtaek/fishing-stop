package com.example.fishingstop.feature.inspect.presentation.check

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.fishingstop.core.ui.components.PrimaryButton

/**
 * 링크 검사 화면(FO_02_02).
 * 복사해 둔 URL을 붙여넣으면 휴리스틱 + AI 병합 검사로 넘어간다.
 * 링크를 자동으로 열지 않는다(안전 원칙).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkCheckScreen(
    onSubmit: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var url by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("링크 검사") },
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "의심 가는 링크(URL)를 복사한 뒤 붙여넣어 주세요.",
                style = MaterialTheme.typography.bodyLarge
            )
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("링크(URL)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            PrimaryButton(
                text = "링크 검사하기",
                onClick = {
                    val text = url.trim()
                    if (text.isEmpty()) {
                        Toast.makeText(context, "검사할 링크를 붙여넣어 주세요.", Toast.LENGTH_SHORT).show()
                    } else {
                        onSubmit(text)
                    }
                }
            )
        }
    }
}
