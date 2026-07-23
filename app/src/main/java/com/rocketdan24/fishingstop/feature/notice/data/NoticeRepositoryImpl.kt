package com.rocketdan24.fishingstop.feature.notice.data

import com.rocketdan24.fishingstop.core.util.IoDispatcher
import com.rocketdan24.fishingstop.feature.notice.domain.NoticeRepository
import com.rocketdan24.fishingstop.feature.notice.domain.model.Notice
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * 공지사항을 Firestore "notices" 컬렉션에서 읽고 쓰는 구현체.
 * ReportRepositoryImpl 과 동일하게 Task(콜백)를 코루틴으로 감싸고, 오프라인 무한대기를
 * 막기 위해 withTimeoutOrNull 을 둔다.
 */
class NoticeRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : NoticeRepository {

    override suspend fun getNotices(): List<Notice> = withContext(ioDispatcher) {
        withTimeoutOrNull(TIMEOUT_MS) {
            suspendCancellableCoroutine { cont ->
                firestore.collection(COLLECTION)
                    .orderBy(FIELD_CREATED_AT, Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener { snap ->
                        val list = snap.documents.map { doc ->
                            Notice(
                                id = doc.id,
                                title = doc.getString(FIELD_TITLE).orEmpty(),
                                body = doc.getString(FIELD_BODY).orEmpty(),
                                // 서버 반영 전(pending write)이면 timestamp 가 null → 0
                                createdAtMillis = doc.getTimestamp(FIELD_CREATED_AT)?.toDate()?.time ?: 0L
                            )
                        }
                        cont.resume(list)
                    }
                    .addOnFailureListener { e -> cont.resumeWithException(e) }
            }
        } ?: throw IllegalStateException(
            "공지사항을 불러오지 못했어요. 네트워크 상태를 확인한 뒤 다시 시도해 주세요."
        )
    }

    override suspend fun addNotice(title: String, body: String): Unit = withContext(ioDispatcher) {
        val data = mapOf(
            FIELD_TITLE to title,
            FIELD_BODY to body,
            FIELD_CREATED_AT to FieldValue.serverTimestamp()
        )
        withTimeoutOrNull(TIMEOUT_MS) {
            suspendCancellableCoroutine { cont ->
                firestore.collection(COLLECTION)
                    .add(data)
                    .addOnSuccessListener { cont.resume(Unit) }
                    .addOnFailureListener { e -> cont.resumeWithException(e) }
            }
        } ?: throw IllegalStateException(
            "공지 등록이 지연되고 있어요. 네트워크 상태를 확인한 뒤 다시 시도해 주세요."
        )
    }

    override suspend fun getNotice(id: String): Notice? = withContext(ioDispatcher) {
        withTimeoutOrNull(TIMEOUT_MS) {
            suspendCancellableCoroutine { cont ->
                firestore.collection(COLLECTION).document(id)
                    .get()
                    .addOnSuccessListener { doc ->
                        val notice = if (doc.exists()) Notice(
                            id = doc.id,
                            title = doc.getString(FIELD_TITLE).orEmpty(),
                            body = doc.getString(FIELD_BODY).orEmpty(),
                            createdAtMillis = doc.getTimestamp(FIELD_CREATED_AT)?.toDate()?.time ?: 0L
                        ) else null
                        cont.resume(notice)
                    }
                    .addOnFailureListener { e -> cont.resumeWithException(e) }
            }
        } ?: throw IllegalStateException(
            "공지를 불러오지 못했어요. 네트워크 상태를 확인한 뒤 다시 시도해 주세요."
        )
    }

    override suspend fun updateNotice(id: String, title: String, body: String): Unit =
        withContext(ioDispatcher) {
            // createdAt 은 건드리지 않고 제목/본문만 갱신한다.
            val data = mapOf(FIELD_TITLE to title, FIELD_BODY to body)
            withTimeoutOrNull(TIMEOUT_MS) {
                suspendCancellableCoroutine { cont ->
                    firestore.collection(COLLECTION).document(id)
                        .update(data)
                        .addOnSuccessListener { cont.resume(Unit) }
                        .addOnFailureListener { e -> cont.resumeWithException(e) }
                }
            } ?: throw IllegalStateException(
                "공지 수정이 지연되고 있어요. 네트워크 상태를 확인한 뒤 다시 시도해 주세요."
            )
        }

    override suspend fun deleteNotice(id: String): Unit = withContext(ioDispatcher) {
        withTimeoutOrNull(TIMEOUT_MS) {
            suspendCancellableCoroutine { cont ->
                firestore.collection(COLLECTION).document(id)
                    .delete()
                    .addOnSuccessListener { cont.resume(Unit) }
                    .addOnFailureListener { e -> cont.resumeWithException(e) }
            }
        } ?: throw IllegalStateException(
            "공지 삭제가 지연되고 있어요. 네트워크 상태를 확인한 뒤 다시 시도해 주세요."
        )
    }

    companion object {
        private const val COLLECTION = "notices"
        private const val FIELD_TITLE = "title"
        private const val FIELD_BODY = "body"
        private const val FIELD_CREATED_AT = "createdAt"
        private const val TIMEOUT_MS = 10_000L
    }
}
