package com.example.fishingstop.core.managers
import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import io.github.jan.supabase.storage.storage
object PhotoPickerManager {

    // 여러 장 업로드 할 때
    suspend fun uploadImages(
        context: Context,
        uriList: List<Uri>,
        userId: String,
        pathName: String
    ): List<String> {
        // core에 있는 uriToByteArray 함수를 사용해서 ByteArray로 자료형을 바꿔준다.
        // 갤러리에 접근하려면 ContentResolver라는 권한이 필요한데 이 때 context(신분증 같은 역할)가 필요하다네요
        val byteArrayList = uriList.mapNotNull { uri ->
            uriToByteArray(context, uri)
        }

        // 우리가 알고 있는 http:// .... 로 path를 바꿔주는 작업
        // index를 붙이는 이유는 중복방지와 사진을 여러 장 올릴 경우에 순서가 필요하니까
        return byteArrayList.mapIndexed { index, bytes ->

            //  파일의 확장자를 가져오는 변수
            val extension = getExtensionFromUri(context, uriList[index])

            val fileName = "private/${pathName}_${userId}_${System.currentTimeMillis()}_$index.$extension"

            // supabase storage의 "images"에 http://로 바꿨던 주소를 업로드하는 작업
            SupabaseManager.supabase.storage.from("images").upload(
                path = fileName,
                data = bytes,
                options = {
                    upsert = false //이미 같은 이름의 파일이 있으면 false = 실행시켜라 true = 덮어쓰기"
                }
            )
            // 업로드했던 이미지의 주소를 가져오는 작업 -> 이제 밑에 보면 images에 넣어줘야지 사진이 올라가니까
            SupabaseManager.supabase.storage.from("images").publicUrl(fileName)

        }
    }

    // 한 장만 업로드할 때(프로필 사진)
    suspend fun uploadSingleImage(
        context: Context,
        uri: Uri?,
        userId: String,
        pathName: String
    ): String? {

        if (uri == null) return null

        // 여기서 여러장 코드를 참고해서 사용하고 있어서 경로나 uriToByteArray 등을 사용하고 있음
        val url = uploadImages(
            context = context,
            uriList = listOf(uri),
            userId = userId,
            pathName = pathName
        )
        // 그리고 첫번재 것만 사용하는거니까 한 장으로 됨.
        return url.firstOrNull()
    }

    // 확장자 추출 함수
    private fun getExtensionFromUri(context: Context, uri: Uri): String {
        val mimeType = context.contentResolver.getType(uri)
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "png"
    }
}

// Content URI를 ByteArray로 변환하는 함수
fun uriToByteArray(context: Context, uri: Uri): ByteArray? {
    return context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
}