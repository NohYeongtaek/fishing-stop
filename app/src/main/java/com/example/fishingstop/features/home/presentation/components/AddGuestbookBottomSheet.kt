package com.example.fishingstop.features.home.presentation.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.newBie.new_bie.App
import com.newBie.new_bie.core.components.BaseAsyncImage
import com.newBie.new_bie.core.components.BottomSheetTopBatTitle
import com.newBie.new_bie.core.components.rememberPhotoPicker
import com.newBie.new_bie.core.components.rememberSinglePhotoPicker
import com.newBie.new_bie.core.utils.toKoreaLocalDateTime
import com.newBie.new_bie.core.utils.toTimeAgo
import com.newBie.new_bie.features.post.presentation.components.buttons.ImageDeleteButton
import com.newBie.new_bie.features.post.presentation.components.likesAndComments.CommentBottomSheetTextField
import com.newBie.new_bie.features.profile.presentation.viewModels.AddGuestbooksBottomSheetViewModel
import com.newBie.new_bie.features.profile.presentation.viewModels.GuestbooksCommentsBottomSheetViewModel
import com.newBie.new_bie.ui.theme.BlackColor
import com.newBie.new_bie.ui.theme.OrangeColor

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGuestbookBottomSheet(viewModel : AddGuestbooksBottomSheetViewModel, screenHeight: Dp, sheetState: SheetState, onDismiss: () -> Unit){

    // 포커스를 잡을때 필요한 상태정보
    val addContentFocusRequester = remember { FocusRequester() }
    // 포커스 매니저는 포커스를 해제할 때 사용됨(여기서는)
    val focusManager = LocalFocusManager.current

    val imageInput by viewModel.imageInput.collectAsState()
    val pickSingleMedia = rememberSinglePhotoPicker{
        viewModel.getImage(it)
    }

    // 저장 중 인디케이터
    val isSaving by viewModel.isSaving.collectAsState()

    // 안드로이드 Jetpack Compose 정석대로 현재 화면의 Context를 안전하게 가져옴
    val currentContext = LocalContext.current

    ModalBottomSheet(
        containerColor = BlackColor,
        contentColor = BlackColor,
        onDismissRequest = {
            onDismiss()
        },
        sheetState = sheetState
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = screenHeight * 0.7f, max = screenHeight * 0.7f),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BottomSheetTopBatTitle("방명록 작성")

                if (imageInput != null){
                    Box(contentAlignment = Alignment.TopEnd){
                        BaseAsyncImage(
                            model = imageInput,
                            contentDescription = "방명록 사용자 입력 사진",
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .size(200.dp)
                        )
                        ImageDeleteButton{viewModel.deleteImage()}
                    }
                }

                Box(
                    modifier = Modifier.weight(1f)
                ){
                    AddGuestbookBottomSheetTextField(
                        state = viewModel.userContentInput,
                        focusRequester = addContentFocusRequester,
                        focusManager = focusManager
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button (
                        onClick = {pickSingleMedia()},
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Unspecified),
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "사진 추가 버튼",
                            tint = Color.White,
                            modifier = Modifier
                                .size(30.dp)
                        )
                    }
                    Button (
                        onClick = {viewModel.saveGuestbook(context = currentContext)},
                        enabled = viewModel.userContentInput.text.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeColor),
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SaveAlt,
                            contentDescription = "저장 버튼",
                            tint = Color.White,
                            modifier = Modifier
                                .size(30.dp)
                        )
                    }
                }

            }
            if(isSaving){
                Box(modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(enabled = false){},
                    contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = OrangeColor)
                }
            }
        }
    }
}