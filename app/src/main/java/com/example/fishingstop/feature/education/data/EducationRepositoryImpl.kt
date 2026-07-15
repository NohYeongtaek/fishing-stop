package com.example.fishingstop.feature.education.data

import com.example.fishingstop.feature.education.domain.EducationCategory
import com.example.fishingstop.feature.education.domain.EducationRepository
import javax.inject.Inject

/**
 * 앱 내장 정적 예방 교육 콘텐츠.
 * 오프라인에서도 열람 가능하도록 코드에 내장한다. 추후 서버/원격 콘텐츠로 확장할 수 있다.
 */
class EducationRepositoryImpl @Inject constructor() : EducationRepository {

    private val categories: List<EducationCategory> = listOf(
        EducationCategory(
            id = "impersonation",
            title = "기관 사칭",
            summary = "검찰·경찰·금감원·국세청 등을 사칭해 겁을 주고 돈을 요구합니다.",
            warningSigns = listOf(
                "\"당신 명의 계좌가 범죄에 연루됐다\"며 압박한다.",
                "안전계좌로 돈을 옮기라고 요구한다.",
                "통화를 끊지 말라고 하거나 비밀 유지를 강요한다."
            ),
            tips = listOf(
                "공공기관은 전화로 계좌이체·현금 전달을 요구하지 않습니다.",
                "전화를 끊고 해당 기관 대표번호로 직접 확인하세요.",
                "112(경찰) 또는 1332(금감원)로 문의하세요."
            )
        ),
        EducationCategory(
            id = "delivery",
            title = "택배 사칭",
            summary = "배송 오류·주소 확인을 빙자한 문자로 악성 링크를 클릭하게 만듭니다.",
            warningSigns = listOf(
                "\"주소 불명으로 배송 불가\" 같은 문구와 링크가 함께 온다.",
                "택배사 앱이 아닌 낯선 링크로 유도한다.",
                "앱(APK) 설치를 요구한다."
            ),
            tips = listOf(
                "문자 속 링크 대신 공식 택배사 앱/홈페이지에서 조회하세요.",
                "출처를 알 수 없는 APK는 절대 설치하지 마세요."
            )
        ),
        EducationCategory(
            id = "acquaintance",
            title = "지인 사칭",
            summary = "가족·친구를 사칭해 급하게 돈이나 상품권을 요구합니다.",
            warningSigns = listOf(
                "\"폰이 고장 나 새 번호\"라며 대화를 시작한다.",
                "직접 통화는 피하고 문자로만 대화한다.",
                "문화상품권 구매·핀번호 전송을 요구한다."
            ),
            tips = listOf(
                "반드시 기존에 알던 번호로 전화해 본인 확인을 하세요.",
                "송금 전 영상통화 등으로 확인하세요."
            )
        ),
        EducationCategory(
            id = "sextortion",
            title = "몸캠 피싱",
            summary = "영상통화 유도 후 녹화 영상으로 협박해 금전을 갈취합니다.",
            warningSigns = listOf(
                "처음 보는 상대가 빠르게 영상통화를 유도한다.",
                "화면 공유나 특정 앱 설치를 요구한다.",
                "지인 연락처 유출을 빌미로 협박한다."
            ),
            tips = listOf(
                "낯선 상대의 영상통화·앱 설치 요구는 응하지 마세요.",
                "협박에 응하지 말고 즉시 캡처해 112·118에 신고하세요."
            )
        ),
        EducationCategory(
            id = "qshing",
            title = "큐싱(QR 피싱)",
            summary = "가짜 QR코드로 악성 사이트·앱 설치로 유도합니다.",
            warningSigns = listOf(
                "공공장소에 덧붙여진 QR코드.",
                "QR 스캔 후 로그인·결제 정보를 요구한다.",
                "QR이 단축 URL이나 낯선 도메인으로 연결된다."
            ),
            tips = listOf(
                "출처가 불분명한 QR은 스캔하지 마세요.",
                "스캔 후 연결되는 주소를 반드시 확인하세요."
            )
        ),
        EducationCategory(
            id = "call_forwarding",
            title = "전화 가로채기",
            summary = "악성 앱이 내가 거는 전화를 사기범에게 연결합니다.",
            warningSigns = listOf(
                "출처 불명 앱 설치 후 은행에 전화하면 이상한 상담원이 받는다.",
                "정상 번호로 걸어도 사기범이 응답한다."
            ),
            tips = listOf(
                "의심 앱을 삭제하고, 다른 기기로 은행 대표번호에 확인하세요.",
                "출처 불명 앱은 설치하지 마세요."
            )
        ),
        EducationCategory(
            id = "investment",
            title = "투자 사기",
            summary = "고수익 보장·리딩방으로 유혹해 투자금을 가로챕니다.",
            warningSigns = listOf(
                "\"원금 보장, 확정 고수익\"을 약속한다.",
                "단체 채팅방(리딩방)으로 유도한다.",
                "출금하려 하면 수수료·세금을 추가로 요구한다."
            ),
            tips = listOf(
                "원금 보장 고수익은 사기일 가능성이 큽니다.",
                "제도권 금융회사인지 금융소비자정보포털 '파인'에서 확인하세요."
            )
        ),
        EducationCategory(
            id = "phishing_site",
            title = "피싱 사이트",
            summary = "진짜와 똑같이 생긴 가짜 사이트로 로그인·결제 정보를 훔칩니다.",
            warningSigns = listOf(
                "주소(도메인)가 공식과 미묘하게 다르다.",
                "문자·메일 링크를 통해 로그인 페이지로 유도한다.",
                "https가 아니거나 인증서 경고가 뜬다."
            ),
            tips = listOf(
                "링크 대신 공식 앱이나 즐겨찾기로 접속하세요.",
                "주소창의 도메인을 한 글자씩 확인하세요."
            )
        ),
        EducationCategory(
            id = "romance",
            title = "로맨스 스캠",
            summary = "SNS로 접근해 호감을 산 뒤 각종 명목으로 돈을 요구합니다.",
            warningSigns = listOf(
                "만난 적 없는 상대가 빠르게 결혼·사랑을 언급한다.",
                "세관·수수료·통관비 등 명목으로 송금을 요구한다.",
                "직접 만남·영상통화를 계속 회피한다."
            ),
            tips = listOf(
                "만난 적 없는 상대에게는 절대 송금하지 마세요.",
                "상대 사진을 역이미지 검색으로 확인해 보세요."
            )
        ),
        EducationCategory(
            id = "fake_payment",
            title = "허위 결제 문자",
            summary = "\"○○원 결제 완료\" 문자로 놀라게 해 상담 전화·링크로 유도합니다.",
            warningSigns = listOf(
                "결제한 적 없는 내역과 고객센터 번호가 함께 온다.",
                "그 번호로 전화하면 개인정보·앱 설치를 요구한다."
            ),
            tips = listOf(
                "문자 속 번호로 전화하지 말고 카드사 공식 번호로 확인하세요.",
                "결제 취소를 빌미로 한 원격 제어 앱 설치에 응하지 마세요."
            )
        )
    )

    override fun getCategories(): List<EducationCategory> = categories

    override fun getCategory(id: String): EducationCategory? =
        categories.firstOrNull { it.id == id }
}
