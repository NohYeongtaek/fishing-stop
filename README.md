# fishing-stop
보이스피싱, 문자 간편 확인 앱 서비스

## 최초 빌드 전 준비

`local.properties`는 사람마다 값이 달라서 git에 커밋하지 않는다. 이 리포를 새로 받았다면(클론/풀)
아래처럼 직접 만들어야 한다(빠뜨리면 `SDK location not found` 에러로 빌드가 실패한다).

```bash
cp local.properties.sample local.properties
```

그 다음 `local.properties`를 열어 `sdk.dir`(Android Studio에서 프로젝트를 한 번 열면 자동으로
채워지기도 함)와, 필요하면 `SAFE_BROWSING_API_KEY`(링크 검사 1차 관문 — 없어도 빌드/실행은 됨,
발급 방법은 파일 안 주석 참고)를 채운다.
