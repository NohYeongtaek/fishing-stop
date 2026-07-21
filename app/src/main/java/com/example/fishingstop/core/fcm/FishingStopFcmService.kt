package com.example.fishingstop.core.fcm

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.fishingstop.MainActivity
import com.example.fishingstop.R
import com.example.fishingstop.core.utils.Constants.TAG
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * 로그인/기기별 토큰 저장 없이 전체 공지를 브로드캐스트하는 방식(FCM 토픽 구독).
 * 관리자가 Firebase 콘솔(또는 서버)에서 [NOTICE_TOPIC] 토픽으로 발송하면
 * 이 토픽을 구독한 모든 기기가 동일한 공지 알림을 받는다.
 */
class FishingStopFcmService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: getString(R.string.app_name)
        val body = message.notification?.body ?: message.data["body"] ?: return
        showNotification(this, title, body)
    }

    // 토픽 구독 방식은 기기별 토큰을 서버에 저장하지 않으므로 갱신 시 별도 처리가 필요 없다.
    override fun onNewToken(token: String) = Unit

    companion object {
        /** 전체 공지 브로드캐스트용 토픽 이름. 서버/콘솔에서 발송 시 동일한 이름을 사용해야 한다. */
        const val NOTICE_TOPIC = "notice_all"

        /** 앱 시작 시 한 번 호출해 알림 채널을 준비한다(API 26+, 이미 있으면 아무 일도 하지 않음). */
        fun ensureNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
            val channel = NotificationChannel(
                context.getString(R.string.fcm_notice_channel_id),
                context.getString(R.string.fcm_notice_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.fcm_notice_channel_desc)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        /** 공지 알림 켜기: [NOTICE_TOPIC] 토픽을 구독한다. */
        fun subscribeNotice() {
            FirebaseMessaging.getInstance().subscribeToTopic(NOTICE_TOPIC)
                .addOnCompleteListener { task ->
                    Log.d(TAG, "subscribeNotice: 구독 요청 성공 여부=${task.isSuccessful}", task.exception)
                }
        }

        /** 공지 알림 끄기: [NOTICE_TOPIC] 토픽 구독을 해제한다. */
        fun unsubscribeNotice() {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(NOTICE_TOPIC)
                .addOnCompleteListener { task ->
                    Log.d(TAG, "unsubscribeNotice: 해제 요청 성공 여부=${task.isSuccessful}", task.exception)
                }
        }

        private fun showNotification(context: Context, title: String, body: String) {
            // 알림 권한이 없으면(거부/미요청) 조용히 무시한다 — 크래시 방지.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                return
            }

            val tapIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                tapIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val notification = NotificationCompat.Builder(context, context.getString(R.string.fcm_notice_channel_id))
                .setSmallIcon(R.drawable.ic_stat_notify)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            NotificationManagerCompat.from(context)
                .notify(System.currentTimeMillis().toInt(), notification)
        }
    }
}
