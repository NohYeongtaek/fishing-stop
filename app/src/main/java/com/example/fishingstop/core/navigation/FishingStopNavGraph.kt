package com.example.fishingstop.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.fishingstop.core.util.InspectMethod
import com.example.fishingstop.feature.consent.presentation.ConsentScreen
import com.example.fishingstop.feature.education.presentation.EducationDetailScreen
import com.example.fishingstop.feature.home.presentation.HomeScreen
import com.example.fishingstop.feature.inspect.presentation.analyze.AnalyzeScreen
import com.example.fishingstop.feature.inspect.presentation.check.ImageCheckScreen
import com.example.fishingstop.feature.inspect.presentation.check.LinkCheckScreen
import com.example.fishingstop.feature.inspect.presentation.check.TextCheckScreen
import com.example.fishingstop.feature.inspect.presentation.qr.QrScanScreen
import com.example.fishingstop.feature.inspect.presentation.result.InspectResultScreen
import com.example.fishingstop.feature.privacy.presentation.PrivacyPolicyScreen
import com.example.fishingstop.feature.report.presentation.ReportScreen
import com.example.fishingstop.feature.settings.presentation.NoticeScreen
import com.example.fishingstop.feature.splash.presentation.SplashScreen

/**
 * 앱 전체 네비게이션 그래프.
 *
 * 화면(Composable)들은 NavController 를 직접 알지 못하고, "다음에 무엇을 할지"를
 * 람다 콜백으로만 전달받는다. 이렇게 하면 화면이 네비게이션 구현에 결합되지 않아
 * 미리보기(Preview)/테스트가 쉬워지고 재사용성이 올라간다.
 *
 * @param sharedText 공유(ACTION_SEND)로 앱이 시작된 경우의 원문(없으면 null).
 */
@Composable
fun FishingStopNavGraph(
    sharedText: String? = null,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.Splash
    ) {
        // 스플래시 → 동의 노출 여부/공유 텍스트에 따라 분기
        composable<Routes.Splash> {
            SplashScreen(
                sharedText = sharedText,
                onNavigateToConsent = {
                    navController.navigate(Routes.Consent()) {
                        popUpTo(Routes.Splash) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Routes.Home) {
                        popUpTo(Routes.Splash) { inclusive = true }
                    }
                },
                // 공유 텍스트는 문자 검사 화면에 자동 입력해 사용자가 확인 후 검사한다(기획 확정).
                onNavigateToAnalyze = { text ->
                    navController.navigate(Routes.Home) {
                        popUpTo(Routes.Splash) { inclusive = true }
                    }
                    navController.navigate(Routes.TextCheck(prefill = text))
                }
            )
        }

        // 동의 화면 (최초 실행 또는 검사 게이트에서 진입)
        composable<Routes.Consent> { backStackEntry ->
            val route = backStackEntry.toRoute<Routes.Consent>()
            ConsentScreen(
                onAgreed = {
                    if (route.fromGate) {
                        // 검사 게이트에서 온 경우: 검사 화면으로 복귀(자동으로 분석 재개)
                        navController.popBackStack()
                    } else {
                        navController.navigate(Routes.Home) {
                            popUpTo<Routes.Consent> { inclusive = true }
                        }
                    }
                },
                onSkip = {
                    if (route.fromGate) {
                        navController.popBackStack()
                    } else {
                        navController.navigate(Routes.Home) {
                            popUpTo<Routes.Consent> { inclusive = true }
                        }
                    }
                },
                onOpenPrivacyPolicy = { navController.navigate(Routes.PrivacyPolicy) }
            )
        }

        // 개인정보 처리방침 전문
        composable<Routes.PrivacyPolicy> {
            PrivacyPolicyScreen(onBack = { navController.popBackStack() })
        }

        // 홈(하단 5탭: 홈/직접검사/검사기록/피싱예방/설정)
        composable<Routes.Home> {
            HomeScreen(
                onQr = { navController.navigate(Routes.QrScan) },
                onLink = { navController.navigate(Routes.LinkCheck) },
                onImage = { navController.navigate(Routes.ImageCheck) },
                onText = { navController.navigate(Routes.TextCheck()) },
                onOpenResult = { id -> navController.navigate(Routes.InspectResult(id)) },
                onOpenEducation = { categoryId -> navController.navigate(Routes.EducationDetail(categoryId)) },
                onOpenNotice = { navController.navigate(Routes.NoticeList) },
                onOpenPrivacy = { navController.navigate(Routes.PrivacyPolicy) }
            )
        }

        // ── 직접검사 방식별 화면 (모두 Analyze 로 합류) ──

        composable<Routes.QrScan> {
            QrScanScreen(
                onDetected = { value ->
                    navController.navigate(Routes.Analyze(text = value, method = InspectMethod.QR.name)) {
                        // 결과에서 뒤로 갈 때 카메라로 되돌아가지 않도록 제거
                        popUpTo<Routes.QrScan> { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable<Routes.LinkCheck> {
            LinkCheckScreen(
                onSubmit = { text ->
                    navController.navigate(Routes.Analyze(text = text, method = InspectMethod.LINK.name))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable<Routes.ImageCheck> {
            ImageCheckScreen(
                onSubmit = { text ->
                    navController.navigate(Routes.Analyze(text = text, method = InspectMethod.IMAGE.name))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable<Routes.TextCheck> { backStackEntry ->
            val route = backStackEntry.toRoute<Routes.TextCheck>()
            TextCheckScreen(
                prefill = route.prefill,
                onSubmit = { text ->
                    val method = if (route.prefill != null) InspectMethod.SHARE else InspectMethod.TEXT
                    navController.navigate(Routes.Analyze(text = text, method = method.name))
                },
                onBack = { navController.popBackStack() }
            )
        }

        // 분석 진행(로딩) → 완료 시 결과 화면으로 교체
        composable<Routes.Analyze> {
            AnalyzeScreen(
                onResult = { id ->
                    navController.navigate(Routes.InspectResult(id)) {
                        // 인자와 무관하게 Analyze 목적지 자체를 백스택에서 제거
                        popUpTo<Routes.Analyze> { inclusive = true }
                    }
                },
                onCancel = { navController.popBackStack() },
                onRequestConsent = {
                    // 검사 직전 동의 게이트: 동의 후 이 화면(Analyze)으로 복귀한다.
                    navController.navigate(Routes.Consent(fromGate = true))
                }
            )
        }

        // 검사 결과
        composable<Routes.InspectResult> {
            InspectResultScreen(
                onReport = { id -> navController.navigate(Routes.Report(id)) },
                // 홈을 재생성(navigate+popUpTo)하면 탭 상태가 날아가므로,
                // 백스택의 기존 홈으로 "되돌아간다" — 보던 탭이 그대로 유지된다.
                onHome = { navController.popBackStack(Routes.Home, inclusive = false) }
            )
        }

        // 신고
        composable<Routes.Report> {
            ReportScreen(
                onBack = { navController.popBackStack() },
                onHome = { navController.popBackStack(Routes.Home, inclusive = false) }
            )
        }

        // 예방 교육 상세
        composable<Routes.EducationDetail> {
            EducationDetailScreen(onBack = { navController.popBackStack() })
        }

        // 공지사항(월별 아코디언)
        composable<Routes.NoticeList> {
            NoticeScreen(onBack = { navController.popBackStack() })
        }
    }
}
