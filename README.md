# HakPlace 🍱

<p align="center">
    <img width="400" alt="Splash Content Container" src="https://github.com/user-attachments/assets/b5992045-be97-4a71-829d-60eea86a8d17" />
</p>

> **"오늘 학식 뭐 나오지? 맛은 어떨까?"**  
> 대학 생활의 중심인 학식을 스마트하게 즐기는 방법, HakPlace입니다.

HakPlace는 대학생들을 위한 맞춤형 **학식 조회 및 리뷰 공유 Android 애플리케이션**입니다. 
단순한 메뉴 조회를 넘어, 학생들 간의 솔직한 평가와 AI 기반 식단 분석을 통해 더 나은 학식 경험을 제공합니다.


## 📸 스크린샷

| 오늘 학식 | 이번주 학식 | 리뷰 목록 | 평점 통계 |
| :---: | :---: | :---: | :---: |
| <img width="200" alt="image" src="https://github.com/user-attachments/assets/f985764f-24d3-4294-93d9-1838c960bb8b" /> | <img width="200" alt="Screenshot_20260516_135629" src="https://github.com/user-attachments/assets/76603b81-add4-4ddb-9b6b-8bd93292b570" /> | <img width="200" alt="Screenshot_20260516_135719" src="https://github.com/user-attachments/assets/6f8426f3-e57f-4890-8340-c0aa8c514072" /> | <img width="200" alt="Screenshot_20260516_135647" src="https://github.com/user-attachments/assets/2039e6ca-f83b-4014-ba1a-6a4c45eb546b" /> |


## 🚀 주요 기능

### 1. 식단 조회 (Today & Weekly)
- **금일 메뉴**: 오늘 제공되는 조식, 중식, 석식 메뉴를 사진과 함께 실시간으로 확인합니다.
- **금주 메뉴**: 요일별/식사 종류별로 일주일간의 식단 계획을 미리 파악할 수 있습니다.

### 2. 리뷰 및 평점 시스템
- **솔직한 리뷰**: 메뉴별로 별점과 텍스트, 사진을 포함한 상세 리뷰를 작성하고 공유합니다.
- **실시간 평점**: 메뉴의 평균 평점을 통해 어떤 메뉴가 인기 있는지 한눈에 알 수 있습니다.

### 3. AI 기반 식단 분석
- **데이터 통계**: 주간/월간 단위의 평점 추이를 차트로 시각화합니다.
- **AI 요약**: 서버의 AI가 리뷰를 분석하여 식단의 강점과 보완점을 요약해서 알려줍니다.

### 4. 사용자 관리
- **회원가입 및 로그인**: JWT 기반 인증을 통해 안전하게 계정을 관리합니다.
- **마이페이지**: 내가 쓴 리뷰 목록을 관리하고 프로필 정보를 수정할 수 있습니다.


## 🛠 기술 스택

- **Platform**: Android Native
- **Language**: Java (Android SDK)
- **Architecture**: MVVM (ViewModel, LiveData, Repository 패턴)
- **Networking**: Retrofit2, OkHttp3, Gson
- **Image Loading**: Glide (이미지 캐싱 및 로딩 최적화)
- **UI/UX**: Material Design, Custom Progress Bars, Multi-part Image Upload
- **Security**: EncryptedSharedPreferences (토큰 보안 저장)  


## 📂 프로젝트 구조

```text
app/src/main
├── AndroidManifest.xml      # 앱 매니페스트 (권한, 액티비티 설정)
├── java/kr/ac/waltdev29/hakplace
│   ├── api/                 # API 통신 관련 (Retrofit, DTO)
│   │   ├── ApiClient.java
│   │   ├── ApiInterface.java
│   │   └── models/          # 데이터 모델 (DailyMeals, ReviewList 등)
│   ├── utils/               # 공통 유틸리티 (DialogHelper 등)
│   ├── fragments/           # UI 프래그먼트
│   └── (Activities)/        # 주요 화면 액티비티
│       ├── TitleActivity.java
│       ├── LoginActivity.java
│       ├── MenuTodayActivity.java
│       ├── MenuWeekActivity.java
│       ├── ReviewWriteActivity.java
│       └── ... (그 외 화면들)
└── res/
    ├── layout/              # XML 레이아웃 파일
    │   ├── activity_*.xml   # 각 화면 레이아웃
    │   ├── dialog_*.xml     # 팝업/모달 레이아웃
    │   └── item_*.xml       # 리스트 아이템 레이아웃
    ├── values/              # 리소스 값 (strings, colors, themes)
    └── drawable/            # 이미지 및 그래픽 리소스
```


## ⚙️ 시작하기

### 사전 요구사항
- Android Studio Iguana 이상 권장
- Android SDK API Level 26 (Oreo) 이상

### 설치 방법
최신 버전을 다운로드하세요. 👉 [Release](https://github.com/WaltDev29/HakPlace/releases)

## 🤝 기여하기
버그 리포트나 기능 제안은 Issue 탭을 통해 언제든지 환영합니다!  
