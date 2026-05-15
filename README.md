# 🍱 HakPlace Android App

대학생들을 위한 지능형 학식 리뷰 및 분석 플랫폼 **학플(HakPlace)**의 안드로이드 클라이언트 앱입니다.

## ⚙️ 프로젝트 설정 (Setup)

이 프로젝트는 서버 API 주소와 같은 환경 변수를 `local.properties` 파일을 통해 관리합니다. 프로젝트를 처음 클론 받으신 후 아래 절차를 따라주세요.

### 1. 환경 변수 설정
프로젝트 루트에 있는 `local.properties.template` 파일을 복사하여 `local.properties` 파일을 생성합니다.

```bash
cp local.properties.template local.properties
```

### 2. API URL 수정
생성한 `local.properties` 파일을 열고 `api.url` 항목에 실제 서버 주소를 입력합니다.
* **주의**: URL 마지막은 반드시 슬래시(`/`)로 끝나야 합니다. (예: `https://example.com/hakplace/`)

```properties
api.url=https://your-api-domain.com/hakplace/
```

### 3. 빌드 및 실행
Android Studio에서 프로젝트를 열고 Sync를 진행한 뒤 빌드합니다. `BuildConfig.API_URL`을 통해 설정한 주소가 자동으로 반영됩니다.

---
© 2026 HakPlace Team. All rights reserved.
