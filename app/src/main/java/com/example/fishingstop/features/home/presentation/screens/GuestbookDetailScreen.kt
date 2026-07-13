package com.example.fishingstop.features.home.presentation.screens

import android.annotation.SuppressLint
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.newBie.new_bie.core.components.BaseAsyncImage
import com.newBie.new_bie.core.components.LinkifyText
import com.newBie.new_bie.core.components.TopBarLayout
import com.newBie.new_bie.core.managers.SupabaseManager
import com.newBie.new_bie.core.utils.Routes
import com.newBie.new_bie.core.utils.toKoreaLocalDateTime
import com.newBie.new_bie.core.utils.toTimeAgo
import com.newBie.new_bie.features.notification.presentation.viewModels.NotificationViewModel
import com.newBie.new_bie.features.post.domain.entities.PostImageEntity
import com.newBie.new_bie.features.post.presentation.components.SmallProfileComponent
import com.newBie.new_bie.features.post.presentation.components.buttons.PostMoreVertButton
import com.newBie.new_bie.features.profile.presentation.components.GuestBooksCommentsBottomSheet
import com.newBie.new_bie.features.profile.presentation.viewModels.GuestbooksCommentsBottomSheetViewModel
import com.newBie.new_bie.features.profile.presentation.viewModels.GuestbooksDetailViewModel
import com.newBie.new_bie.ui.theme.AppTextStyle
import io.github.jan.supabase.auth.auth
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable

@SuppressLint("ConfigurationScreenWidthHeight")
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuestbookDetailScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel : GuestbooksDetailViewModel = hiltViewModel(),
    guestbooksCommentsViewModel: GuestbooksCommentsBottomSheetViewModel = hiltViewModel(),
    notificationViewModel: NotificationViewModel) {


    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSheet by remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    // 꽉 차는 값 flag 값
    var isExpanded by remember { mutableStateOf(false) }
    // 사진 한장 한장가져올 값
    var selectedImage by remember { mutableStateOf<PostImageEntity?>(null) }

    val focusManager = LocalFocusManager.current
    val isRead by notificationViewModel.isRead.collectAsState()

    val guestbook by viewModel.guestbooks.collectAsState()


//    LaunchedEffect(post) {
//        viewModel.fetchComments()
//    }


    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopBarLayout(
                title = "방명록",
                focusManager = focusManager,
                navController = navController,
                isRead = isRead
            )
        },
    ) { innerPadding ->
        SharedTransitionLayout(
            modifier = Modifier
                .padding(top = innerPadding.calculateTopPadding())
        ){
            AnimatedContent(
                targetState = isExpanded,
                label = "ImageTransition"
            ) { targetExpended ->
                if (!targetExpended){
                    Column(
                        modifier = modifier.fillMaxSize()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 10.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                SmallProfileComponent(
                                    modifier = Modifier.weight(1f),
                                    imageUrl = guestbook?.senderId?.profileImage ?: "",
                                    nickName = guestbook?.senderId?.nickName ?: "",
                                    introduce = guestbook?.createdAt?.toKoreaLocalDateTime()?.toTimeAgo(),
                                    userId = guestbook?.senderId?.id ?: "",
                                    {
                                        navController.navigate("${Routes.MY_PROFILE}/${guestbook?.senderId?.id}")
                                    }
                                )
                                val currentId = SupabaseManager.supabase.auth.currentUserOrNull()?.id ?:""

                                PostMoreVertButton(
                                    targetId = guestbook?.senderId?.id ?:"",
                                    targetIdNickname = guestbook?.senderId?.nickName ?:"",
                                    currentId = currentId,
                                    updateClick = {},
                                    deletedClick = {
                                        viewModel.deleteGuestbook(onSuccess = {
                                            // 프로필 화면이 기다리고 있는 "profile_need_refresh"를 true로 변경!
                                            navController.previousBackStackEntry?.savedStateHandle?.set("profile_need_refresh", true)
                                            // 그 후 화면을 닫음
                                            navController.popBackStack()
                                        })
                                                   },
                                    navController = navController,
                                    guestbooksMode = true,
                                    hostId = guestbook?.receiverId?.id
                                )
                            }
                            Text(guestbook?.title ?: "", style = AppTextStyle.Title, )

                            Spacer(modifier = Modifier.height(20.dp))

                            if (guestbook?.imageUrl?.isNotEmpty() == true){

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(500.dp)
                                ) {
                                        BaseAsyncImage(
                                            model = guestbook?.imageUrl,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop, // BoxFit.cover
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clickable(
                                                    onClick = {
                                                        isExpanded = true
                                                    }
                                                )
                                        )

                                }
                            }
                            Spacer(modifier = Modifier.height(20.dp))

                            SelectionContainer {
                                LinkifyText(
                                    text = guestbook?.content ?: "",
                                    style = AppTextStyle.Content
                                )
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.End) {
                            Row(
                                modifier = Modifier.clickable(onClick = {
                                    guestbooksCommentsViewModel.getGuestbooksComments()
                                    showSheet = true
                                }),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChatBubble,
                                    contentDescription = null,
                                    tint = Color.White
                                )

                                Spacer(modifier = Modifier.width(4.dp))

                                Text(
                                    text = "${guestbook?.commentsCount ?: 0}",
                                    color = Color.White
                                )
                            }
                        }
                        if (showSheet) {
                            GuestBooksCommentsBottomSheet(
                                viewModel= guestbooksCommentsViewModel,
                                screenHeight = screenHeight,
                                sheetState = sheetState,
                                onDismiss = {showSheet = false},
                                navController = navController
                            )
                        }

                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black)
                    ) { ->
                            val currentUri = guestbook?.imageUrl
                            val zoomState = rememberZoomState()

                            Box(
                                modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                            ) {
                                BaseAsyncImage(
                                    model = currentUri,
                                    contentDescription = "확대 이미지",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .sharedElement(
                                            rememberSharedContentState(key = "image_$currentUri"),
                                            animatedVisibilityScope = this@AnimatedContent,
                                        )
                                        .zoomable(zoomState)
                                )
                            }

                        // 닫기 버튼
                        IconButton(
                            onClick = { isExpanded = false },
                            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "닫기", tint = Color.White)
                        }
                    }
                    // 안드로이드 뒤로가기 키 설정
                    BackHandler() {
                        if (isExpanded){
                            isExpanded = false
                        }
                    }
                }
            }
        }

    }

}