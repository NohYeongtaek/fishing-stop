package com.rocketdan24.fishingstop.feature.notice.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import com.rocketdan24.fishingstop.core.util.IoDispatcher
import com.rocketdan24.fishingstop.feature.notice.domain.AdminGateRepository
import com.rocketdan24.fishingstop.feature.notice.domain.PinResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.security.MessageDigest
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * 관리자 PIN 게이트 구현체.
 *
 * - PIN 해시(SHA-256)는 Firestore config/adminGate 문서에서 읽어 비교한다(콘솔에서 교체 가능).
 * - 5회 연속 실패 시 30분 기기 로컬 잠금(DataStore, 재시작해도 유지).
 * - USE_ANON_AUTH = true 이면 PIN 통과 시 익명 로그인을 수행해 Firestore 규칙(request.auth != null)과
 *   맞물리게 할 수 있다(권장 옵션). 기본은 false(순수 PIN 게이트).
 *
 * ⚠️ 4자리 PIN + 공개 read 해시는 오프라인 브루트포스에 취약하다. 이 게이트는 암호학적 방어가
 *    아니라 우발적/일반 사용자 진입 차단 목적이다.
 */
class AdminGateRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val dataStore: DataStore<Preferences>,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : AdminGateRepository {

    override suspend fun verifyPin(pin: String): PinResult = withContext(ioDispatcher) {
        val now = System.currentTimeMillis()

        // 1) 잠금 중이면 즉시 반환
        val prefs = dataStore.data.first()
        val lockUntil = prefs[KEY_LOCK_UNTIL] ?: 0L
        if (now < lockUntil) {
            return@withContext PinResult.Locked(lockUntil - now)
        }

        // 2) PIN 해시 로드
        val pinHash = runCatching { loadPinHash() }.getOrElse {
            return@withContext PinResult.Error("비밀번호 확인에 실패했어요. 네트워크 상태를 확인해 주세요.")
        }
        if (pinHash.isNullOrBlank()) {
            return@withContext PinResult.Error("관리자 설정이 준비되지 않았어요. (config/adminGate 없음)")
        }

        // 3) 비교
        val matched = sha256Hex(pin).equals(pinHash, ignoreCase = true)
        if (matched) {
            dataStore.edit {
                it[KEY_FAILED] = 0
                it[KEY_LOCK_UNTIL] = 0L
            }
            if (USE_ANON_AUTH && auth.currentUser == null) {
                runCatching { signInAnonymously() }
                    .onFailure { return@withContext PinResult.Error("인증에 실패했어요. 다시 시도해 주세요.") }
            }
            PinResult.Success
        } else {
            val failed = (prefs[KEY_FAILED] ?: 0) + 1
            if (failed >= MAX_ATTEMPTS) {
                val until = now + LOCK_DURATION_MS
                dataStore.edit {
                    it[KEY_FAILED] = 0
                    it[KEY_LOCK_UNTIL] = until
                }
                PinResult.Locked(LOCK_DURATION_MS)
            } else {
                dataStore.edit { it[KEY_FAILED] = failed }
                PinResult.Failed(remaining = MAX_ATTEMPTS - failed)
            }
        }
    }

    private suspend fun loadPinHash(): String? = withTimeoutOrNull(TIMEOUT_MS) {
        suspendCancellableCoroutine { cont ->
            firestore.collection(CONFIG_COLLECTION).document(ADMIN_GATE_DOC)
                .get()
                .addOnSuccessListener { cont.resume(it.getString(FIELD_PIN_HASH)) }
                .addOnFailureListener { e -> cont.resumeWithException(e) }
        }
    } ?: throw IllegalStateException("timeout")

    private suspend fun signInAnonymously() = suspendCancellableCoroutine { cont ->
        auth.signInAnonymously()
            .addOnSuccessListener { cont.resume(Unit) }
            .addOnFailureListener { e -> cont.resumeWithException(e) }
    }

    private fun sha256Hex(input: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(input.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }

    companion object {
        /** true 로 바꾸면 PIN 통과 시 익명 로그인 수행(Firestore 규칙 request.auth != null 과 연동). */
        private const val USE_ANON_AUTH = false

        private const val MAX_ATTEMPTS = 5
        private const val LOCK_DURATION_MS = 30 * 60 * 1000L // 30분
        private const val TIMEOUT_MS = 10_000L

        private const val CONFIG_COLLECTION = "config"
        private const val ADMIN_GATE_DOC = "adminGate"
        private const val FIELD_PIN_HASH = "pinHash"

        private val KEY_FAILED = intPreferencesKey("admin_failed_attempts")
        private val KEY_LOCK_UNTIL = longPreferencesKey("admin_lock_until")
    }
}
