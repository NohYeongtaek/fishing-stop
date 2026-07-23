package com.rocketdan24.fishingstop.core.util

import javax.inject.Qualifier

/**
 * 코루틴 디스패처를 Hilt로 주입받기 위한 한정자(Qualifier)들.
 *
 * ViewModel/UseCase에서 Dispatchers.IO 를 직접 참조하지 않고 이 한정자로 주입받으면,
 * 단위 테스트에서 TestDispatcher 로 손쉽게 교체할 수 있어 테스트가 쉬워진다.
 */

/** 네트워크/DB/파일 등 블로킹 I/O 작업용 디스패처 */
@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class IoDispatcher

/** CPU 집약 연산(파싱, 휴리스틱 계산 등)용 디스패처 */
@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class DefaultDispatcher

/** UI 갱신용 메인 디스패처 */
@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class MainDispatcher
