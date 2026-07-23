package com.rocketdan24.fishingstop.core.di

import javax.inject.Qualifier

/**
 * 용도가 다른 [com.google.firebase.ai.GenerativeModel] 인스턴스를 Hilt로 구분해 주입하기 위한 한정자.
 *
 * URL Context 도구(웹페이지를 직접 가져와 근거로 삼음)를 쓰는 모델은 시스템 지침/도구 구성이
 * 메시지 텍스트 분류용 모델과 다르므로 별도 인스턴스로 관리한다.
 */
@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class UrlContextModel
