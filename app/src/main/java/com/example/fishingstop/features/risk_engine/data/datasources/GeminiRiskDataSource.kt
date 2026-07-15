package com.example.fishingstop.features.risk_engine.data.datasources

import com.example.fishingstop.features.risk_engine.data.dto.RiskAnalysisDto
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.generationConfig
import org.json.JSONObject
import javax.inject.Inject

/**
 * Gemini(Firebase AI Logic) 호출부.
 *
 * 프롬프트 원문(RISK_ANALYSIS_PROMPT)을 이 파일에 상수로 모아둔다.
 * 1주차에 수집한 사기/정상 문자 샘플로 임계값·문구를 튜닝할 때는 이 파일만 고치면 된다.
 * 위험도 로직 리스크 분산 원칙에 따라 프롬프트를 수정할 때는 반드시 페어 설계 또는 코드 리뷰를 거친다.
 */
class GeminiRiskDataSource @Inject constructor() {

    private val model: GenerativeModel by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
            modelName = "gemini-2.5-flash",
            generationConfig = generationConfig {
                // 응답을 JSON 하나로 강제해서 파싱을 단순하게 만든다.
                responseMimeType = "application/json"
            },
        )
    }

    suspend fun fetchRiskAnalysis(text: String, urls: List<String>): RiskAnalysisDto {
        val prompt = buildPrompt(text = text, urls = urls)
        val response = model.generateContent(prompt)
        val raw = response.text ?: throw IllegalStateException("Gemini 응답이 비어 있습니다.")
        return parseResponse(raw)
    }

    private fun buildPrompt(text: String, urls: List<String>): String {
        val urlSection = if (urls.isEmpty()) "없음" else urls.joinToString("\n")
        return RISK_ANALYSIS_PROMPT
            .replace("{{MESSAGE}}", text)
            .replace("{{URLS}}", urlSection)
    }

    private fun parseResponse(raw: String): RiskAnalysisDto {
        val json = JSONObject(raw)
        val reasonsArray = json.optJSONArray("reasons")
        val reasons = buildList {
            if (reasonsArray != null) {
                for (i in 0 until reasonsArray.length()) {
                    add(reasonsArray.getString(i))
                }
            }
        }
        return RiskAnalysisDto(
            score = json.optInt("score", 0),
            level = json.optString("level", "SAFE"),
            reasons = reasons,
            advice = json.optString("advice", DEFAULT_ADVICE),
        )
    }

    companion object {
        // {{MESSAGE}}, {{URLS}} 를 실제 값으로 치환해서 사용한다.
        // 판정 기준(안전/경고/위험)은 기획서 기준을 그대로 반영했다. 샘플 데이터로 튜닝할 때 이 문구를 조정한다.
        private const val RISK_ANALYSIS_PROMPT = """
당신은 보이스피싱·스미싱 탐지를 돕는 분석기입니다. 아래 메시지를 분석해서 위험도를 판정하세요.

[분석 대상 메시지]
{{MESSAGE}}

[메시지에서 추출된 URL]
{{URLS}}

[판정 기준]
- 안전(0~30점): 피싱 징후 없는 일상 대화, 인증번호, 단순 택배 안내
- 경고(30~75점): 출처 불명 URL, 금융/수사기관 언급 시작, 유도성 문구, 판단할 데이터가 부족한 경우
- 위험(75~100점): 신분증·계좌번호 요구, 대출 권유, 기관 사칭, "즉시", "금일 마감" 등 압박 문구

[출력 형식]
반드시 아래 JSON 형식으로만 응답하세요. 다른 설명은 절대 포함하지 마세요.
{
  "score": 0부터 100 사이의 정수,
  "level": "SAFE" 또는 "WARNING" 또는 "DANGER",
  "reasons": ["판정 근거를 사용자가 이해할 수 있는 한국어 문장으로 나열"],
  "advice": "이 메시지에 맞춰 사용자가 지금 취해야 할 행동을 한국어 1~3문장으로 구체적으로 제시. 예: 발신 번호로 다시 전화하지 말고 기관 대표번호로 직접 확인, 링크를 절대 누르지 말 것, 가족에게 사실관계를 먼저 확인할 것 등. 안전 등급이면 계속 주의하라는 가벼운 조언으로 작성."
}

주의: 이 판정은 참고 정보일 뿐이며 확정된 사실이 아닙니다. 애매한 경우 과감하게 안전으로 단정하지 말고 경고로 분류하세요.
"""

        // Gemini가 advice를 비워서 반환하는 예외 상황에 대비한 최소 안전장치. 실제 조언은 항상 AI가 메시지 맥락에 맞게 생성한다.
        private const val DEFAULT_ADVICE =
            "판단이 애매한 메시지입니다. 발신자에게 먼저 연락하지 말고, 공식 대표번호로 직접 확인해보세요."
    }
}
