package com.example.fishingstop.features.risk_engine.domain.entities

/**
 * 위험도 판정 최종 결과 (UseCase가 만들어서 결과 화면에 넘기는 도메인 모델).
 *
 * 메시지 검사·이미지(OCR) 검사·URL 검사 등 어떤 입력 경로로 들어왔든 이 모델 하나로 결과를 받는다.
 *
 * @param level 등급(안전/경고/위험) — 결과 화면에서 가장 크게 보여줄 값
 * @param score 0~100 AI 참고 점수. 결과 화면에서는 작게 "AI 참고 점수" 라벨과 함께 보조적으로만 노출한다.
 * @param reasons 판정 근거 목록. 결과 화면의 체크리스트/경고 문구로 그대로 사용된다.
 * @param advice 사용자가 지금 무엇을 하면 좋을지 알려주는 행동 조언(예: "발신 번호로 다시 걸지 말고 공식 대표번호로 확인하세요").
 *   등급별 정형 문구가 아니라 이 메시지 내용에 맞춘 구체적인 조언이어야 한다.
 * @param disclaimer 법적 증거가 아니라는 안내 문구. 결과 화면 하단에 항상 노출한다.
 */
data class RiskAnalysisResult(
    val level: RiskLevel,
    val score: Int,
    val reasons: List<String>,
    val advice: String,
    val disclaimer: String = DEFAULT_DISCLAIMER,
) {
    companion object {
        const val DEFAULT_DISCLAIMER =
            "이 결과는 AI 분석에 따른 참고 정보이며 법적 증거로 사용할 수 없습니다."
    }
}
