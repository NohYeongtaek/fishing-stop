package com.example.fishingstop.features.home.data.datasources

import android.util.Log
import com.newBie.new_bie.core.managers.SupabaseManager
import com.newBie.new_bie.core.utils.Constants.TAG
import com.newBie.new_bie.features.profile.data.dto.GuestbooksDto
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject

class GuestbooksDatasource @Inject constructor(){

    private val _supabase = SupabaseManager.supabase

    suspend fun getGuestbooksList(receiverId: String, range: String): List<GuestbooksDto>{

        return try {
            // "0-5" 문자열을 각각 0과 5이라는 숫자로 쪼개는 작업
            val parts = range.split("-")
            val fromIndex = parts.getOrNull(0)?.toLongOrNull() ?: 0L
            val toIndex = parts.getOrNull(1)?.toLongOrNull() ?: 5L

            _supabase.from("guestbooks")
                .select(
                    columns = Columns.raw(
                        """
                            id,
                            title,
                            content,
                            image_url,
                            updated_at,
                            deleted_at,
                            created_at,
                            comments_count,
                            receiver_id:users!guestbooks_receiver_id_fkey(*),
                            sender_id:users!guestbooks_sender_id_fkey(*)
                        """.trimIndent()
                    )
                ){
                    order(
                        column = "created_at", order = Order.DESCENDING
                    )
                    range(from = fromIndex, to = toIndex)
                    filter {
                        eq("receiver_id", receiverId)
                    }
                }
                .decodeList<GuestbooksDto>()
        } catch (e: Exception){
            Log.e(TAG, "fetchGuestbooksList: ${e.message}" )
            emptyList()
        }
    }

    // 방명록 상세 조회
    suspend fun getGuestbook(guestbookId: Int): GuestbooksDto? {
        return try {
            _supabase.from("guestbooks")
                .select(
                    columns = Columns.raw(
                        """
                            id,
                            title,
                            content,
                            image_url,
                            updated_at,
                            deleted_at,
                            created_at,
                            comments_count,
                            receiver_id:users!guestbooks_receiver_id_fkey(*),
                            sender_id:users!guestbooks_sender_id_fkey(*)
                        """.trimIndent()
                    )
                ){
                    filter {
                        eq("id", guestbookId)
                    }
                }
                .decodeSingle<GuestbooksDto>()
        } catch (e: Exception){
            Log.e(TAG, "fetchGuestbooksList: ${e.message}" )
            null
        }
    }

    // 방명록 저장
    suspend fun insertGuestbook(receiverId: String, senderId: String, content: String, image: String?){
        _supabase.from("guestbooks")
            .insert(
                buildJsonObject {
                    put("receiver_id", receiverId)
                    put("sender_id", senderId)
                    put("content", content)
                    // 이미지가 null이 아닐 때 DB에 넣는다.
                    if (image != null){
                        put("image_url", image)
                    }
                }
            )
    }

    // 방명록 삭제
    suspend fun deleteGuestbook(guestbookId: Int){
        _supabase.from("guestbooks")
            .delete {
                filter {
                    eq("id", guestbookId)
                }
            }
    }
}