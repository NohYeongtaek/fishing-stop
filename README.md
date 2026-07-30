# 피싱 멈춰! (fishing-stop)

보이스피싱·스미싱 문자를 AI로 분석해 위험도를 알려주는 안드로이드 앱입니다.
링크·이미지·QR·문자, 어떤 형태로 오더라도 메시지 앱에서 공유 한 번으로 바로 검사할 수 있습니다.

- **개발 기간**: 2026.07.07 ~ 2026.08.06 (멋쟁이사자처럼 안드로이드 인턴십)
- **팀 구성**: 3인 팀 (기획·디자인·개발 전반 참여)
- **담당**: 팀장 · 앱 기획 및 로고·디자인 톤앤매너 총괄 · 핵심 기능 개발

## 시연 영상

[시연영상.mp4 보기](<피싱멈춰! 화면 캡쳐/시연영상.mp4>)

## 왜 만들었는가

보이스피싱·스미싱은 문자, 링크, 이미지, QR 코드 등 점점 더 다양한 형태로 진화하고 있는데,
정작 의심스러운 메시지를 받았을 때 일반 사용자가 스스로 확인할 방법은 마땅치 않다는 문제의식에서
출발했습니다. 메시지 앱에서 공유 한 번으로 바로 검사하고, 어떤 형태로 오더라도 AI가 위험도와
판단 근거까지 함께 제시하는 서비스를 목표로 삼았습니다.

## 주요 기능

- **검사 시작 · 공유 연동**: 메시지 앱에서 의심 문자를 길게 눌러 [공유] → [피싱멈춰!]를 선택하면 바로 검사
- **직접 검사**: QR 코드, 이미지(OCR 텍스트 추출), 링크, 문자 4가지 방식 중 원하는 방법으로 직접 검사
- **링크 피싱 탐지 2단계 검증**: Google Safe Browsing 1차 조회 → 매치가 없을 때만 Gemini의 URL Context 도구로 실제 페이지를 방문·분석
- **검사 결과**: 위험도를 점수·등급(안전·주의·위험)으로 보여주고, 검사한 내용·확인 체크리스트·권고 사항까지 함께 제시
- **신고하기**: 검사 결과를 바탕으로 원하는 항목만 선택해 익명으로 신고
- **검사 기록**: 지금까지 검사한 내역을 시간순으로 모아보고, 즐겨찾기로 다시 확인하고 싶은 결과만 모아보기
- **피싱 예방 교육**: 기관·택배·지인 사칭, 몸캠 피싱, 큐싱(QR 피싱) 등 사기 유형별 특징과 대처법 안내
- **공지사항 · 긴급공지**: 관리자 모드에서 공지를 작성·관리하면 전체 사용자에게 FCM 푸시로 즉시 발송
- **접근성**: 라이트·다크·시스템 테마, 글씨를 크게·또렷하게 보여주는 어르신 모드, 코치마크 기반 온보딩

## 화면

| | | | |
| --- | --- | --- | --- |
| ![검사 중](<피싱멈춰! 화면 캡쳐/readme_thumbs/검사 중.png>) | ![검사 결과](<피싱멈춰! 화면 캡쳐/readme_thumbs/검사 결과.png>) | ![공유로 검사하기](<피싱멈춰! 화면 캡쳐/readme_thumbs/공유로 검사하기.png>) | ![QR 코드 검사](<피싱멈춰! 화면 캡쳐/readme_thumbs/직접검사 - QR 코드 검사.png>) |
| 검사 중 (진행률 표시) | 검사 결과 | 공유로 검사하기 | QR 코드 검사 |
| ![검사 기록](<피싱멈춰! 화면 캡쳐/readme_thumbs/검사 기록 - 즐겨찾기.png>) | ![신고하기](<피싱멈춰! 화면 캡쳐/readme_thumbs/신고하기.png>) | ![피싱 예방 교육](<피싱멈춰! 화면 캡쳐/readme_thumbs/피싱 예방.png>) | ![코치마크 온보딩](<피싱멈춰! 화면 캡쳐/readme_thumbs/코치마크 - 검사 시작.png>) |
| 검사 기록 · 즐겨찾기 | 신고하기 | 피싱 예방 교육 | 코치마크 온보딩 |
| ![관리자 모드](<피싱멈춰! 화면 캡쳐/readme_thumbs/관리자 모드 - 공지 작성.png>) | ![다크 모드](<피싱멈춰! 화면 캡쳐/readme_thumbs/다크모드 - 홈.png>) | ![어르신 모드](<피싱멈춰! 화면 캡쳐/readme_thumbs/어르신 모드 - 홈.png>) | ![텍스트 추출](<피싱멈춰! 화면 캡쳐/readme_thumbs/텍스트 추출.png>) |
| 관리자 모드 · 공지 작성 | 다크 모드 | 어르신 모드 | 이미지 텍스트 추출(OCR) |

## 링크 피싱 탐지 2단계 검증 시스템

기존 링크 검사는 URL 패턴 점수화와 텍스트 기반 Gemini 판단만으로 이뤄져 있어, 신종 피싱 사이트나
이미 알려진 악성 URL을 확실히 걸러내기 어려웠습니다. "판정 실패를 안전으로 오인하지 않는다"는
원칙을 지키기 위해 다음과 같은 2단계 구조로 개선했습니다.

1. **1차 — Google Safe Browsing** `threatMatches:find` API로 조회해 매치되면 즉시 위험을 확정 (2차 생략)
2. **2차 — Gemini URL Context** 매치가 없을 때만 Gemini의 URL Context 도구로 실제 페이지를 방문·분석
3. 두 결과 중 더 위험한 점수를 최종 점수로 채택하고, 판단 근거(signals)는 중복 제거 후 합침
4. 단축 URL·QR 코드는 중간 리다이렉트 지점이 아닌 최종 목적지까지 추적해 검사

별도 서버(헤드리스 브라우저) 없이 무료로 구현했으며, 실기기 테스트에서 로컬 휴리스틱만으로는
40점(주의)으로 오판정되던 정상 사이트(naver.com)가 실제 방문 근거와 함께 0점(안전)으로
정확하게 개선됨을 확인했습니다.

## 기술 스택

| 분류 | 기술 |
| --- | --- |
| Android · UI | Kotlin, Jetpack Compose, Navigation |
| Architecture | MVVM, Clean Architecture, Hilt |
| AI · Security | Gemini API(URL Context), Google Safe Browsing API, ML Kit(OCR, Barcode) |
| Network · Data | Retrofit2, OkHttp3, Room, DataStore |
| Backend · Media | Firebase(Auth · Firestore · FCM), CameraX, Coil3 |
| 개발 도구 | Claude Code |

## 아키텍처

MVVM · Clean Architecture 기반으로 Presentation · Domain · Data 3계층으로 책임을 나누고,
계층 간 의존성은 Hilt로 주입해 각 레이어가 서로의 구현체가 아닌 인터페이스만 알도록 강제했습니다.
다만 단순 조회성 화면은 예외를 둬 구조적 엄격함과 개발 속도의 균형을 맞췄습니다.

- **Presentation**: Compose UI + ViewModel · 상태 구독과 액션 위임
- **Domain**: 순수 Kotlin · UseCase, Repository 인터페이스
- **Data**: Repository 구현체 · Firebase AI, Safe Browsing, Room 등 실제 데이터 소스

**Feature 모듈**: `core` `home` `inspect` `education` `notice` `onboarding` `settings` `splash`

## 시작하기

### 최초 빌드 전 준비

`local.properties`는 사람마다 값이 달라서 git에 커밋하지 않는다. 이 리포를 새로 받았다면(클론/풀)
아래처럼 직접 만들어야 한다(빠뜨리면 `SDK location not found` 에러로 빌드가 실패한다).

```bash
cp local.properties.sample local.properties
```

그 다음 `local.properties`를 열어 `sdk.dir`(Android Studio에서 프로젝트를 한 번 열면 자동으로
채워지기도 함)와, 필요하면 `SAFE_BROWSING_API_KEY`(링크 검사 1차 관문 — 없어도 빌드/실행은 됨,
발급 방법은 파일 안 주석 참고)를 채운다.

## 관련 링크

- GitHub: [NohYeongtaek/fishing-stop](https://github.com/NohYeongtaek/fishing-stop)
- 시연 영상(짧은 버전): [youtube.com/shorts/1qpkGDDMsLI](https://youtube.com/shorts/1qpkGDDMsLI)
