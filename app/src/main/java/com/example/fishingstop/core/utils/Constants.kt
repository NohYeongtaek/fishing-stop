package com.newBie.new_bie.core.utils

import com.newBie.new_bie.BuildConfig

object Constants {
    val TAG : String = "로그"
}

object SupabaseInitial {
    val URL : String = BuildConfig.URL
    val ANON_KEY : String = BuildConfig.ANON_KEY
    val GOOGLE_WEB_CLIENT_ID : String = BuildConfig.GOOGLE_WEB_CLIENT_ID
}

object API {
    val SUPABASE_BASE_URL : String = "${BuildConfig.URL}/functions/v1/post-function/"

    val AUTHORIZATION : String = "Bearer ${BuildConfig.ANON_KEY}"
    val CONTENT_TYPE : String = "application/json"
}

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val DELETED_USER = "deleted_user"
    const val BLOCKED = "blocked"
    const val UNREGISTER = "unregister"
    const val SET_PROFILE = "set_profile"

    const val HOME = "home"
    const val SEARCH = "search"
    const val ADD = "add"
    const val JOURNAL = "journal"
    const val MY_PROFILE = "my_profile"

    const val POST = "post"
    const val POST_EDIT = "edit"

    const val USER_PROFILE = "user_profile"
    const val FOLLOW = "follow"

    const val SETTING = "setting"
    const val QUESTION = "question"
    const val NOTICE = "notice"
    const val NOTICE_DETAIL = "notice_detail"
    const val BLOCKED_USERS = "blocked_users"
    const val UPDATE_PROFILE = "updateProfile"
    const val TEAM_PROJECT = "teamProject"
    const val CHATTING = "chatting"

    const val NOTIFICATION = "notification"
    const val GUESTBOOKS = "guestbooks"
    const val NOTIFICATION_SETTING = "notification_setting"
    const val TERMS = "terms"
}


enum class PageSet {
    HOME, PROFILE, ADD_POST
}

enum class PageType{
    HOME, POST_DETAIL, SEARCH
}

enum class OrderByType{
    NEW_FIRST, OLD_FIRST, LIKES_FIRST
}
 enum class BottomSheetType{
     COMMENT, LIKES
 }
enum class PostingDialogType{
    SAVE, CANCEL
}
