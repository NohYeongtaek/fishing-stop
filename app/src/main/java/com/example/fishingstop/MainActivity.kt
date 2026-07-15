package com.example.fishingstop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.fishingstop.core.navigation.AppNavHost
import com.example.fishingstop.ui.theme.FishingstopTheme
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.initialize
import dagger.hilt.android.AndroidEntryPoint

// Compose 화면 트리 안에서 hiltViewModel()로 ViewModel을 주입받으려면
// Activity에 @AndroidEntryPoint가 반드시 붙어 있어야 한다. (없으면 런타임에 크래시)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Firebase.initialize(this)

        Firebase.appCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance()
        )
        enableEdgeToEdge()
        setContent {
            FishingstopTheme {
                AppNavHost()
            }
        }
    }
}