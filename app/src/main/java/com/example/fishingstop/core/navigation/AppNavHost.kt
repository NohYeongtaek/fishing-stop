package com.example.fishingstop.core.navigation

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fishingstop.core.utils.Routes
import com.example.fishingstop.features.inspect.presentation.screens.InspectResultScreen
import com.example.fishingstop.features.inspect.presentation.viewmodels.InspectResultViewModel
import com.example.fishingstop.features.qr_scan.presentation.screens.QrScanScreen

/**
 * 앱 전체 네비게이션 그래프.
 *
 * QR 검사, 메시지 검사, 이미지 검사, URL 검사 등 입력 경로가 달라도 검사가 끝나면
 * 모두 같은 결과 화면(INSPECT_RESULT)으로 합류한다.
 *
 * InspectResultViewModel을 Activity 범위(hiltViewModel(viewModelStoreOwner = activity))로 가져와서
 * NavHost 내 모든 화면이 같은 인스턴스를 공유하게 만든다. 그래서 QR 검사 화면이 결과를 담아두면
 * 결과 화면이 그대로 읽어 보여줄 수 있고, 결과 객체를 Navigation 인자로 직렬화해 넘길 필요가 없다.
 */
@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    val activity = LocalContext.current as ComponentActivity
    val inspectResultViewModel: InspectResultViewModel = hiltViewModel(viewModelStoreOwner = activity)

    NavHost(navController = navController, startDestination = Routes.QR_SCAN) {
        composable(Routes.QR_SCAN) {
            QrScanScreen(
                onInspectionComplete = { result ->
                    inspectResultViewModel.setResult(result)
                    navController.navigate(Routes.INSPECT_RESULT)
                },
            )
        }

        composable(Routes.INSPECT_RESULT) {
            InspectResultScreen(
                viewModel = inspectResultViewModel,
                onBackToHome = {
                    navController.popBackStack(Routes.QR_SCAN, inclusive = false)
                },
                onReportClick = {
                    // 신고 화면이 아직 없어서 임시로 비워둠. 신고 화면이 만들어지면
                    // navController.navigate(Routes.REPORT) 형태로 여기만 연결하면 된다.
                },
            )
        }
    }
}
