# HakPlace 🍱

<p align="center">
    <img width="400" alt="Splash Content Container" src="https://github.com/user-attachments/assets/b5992045-be97-4a71-829d-60eea86a8d17" />
</p>

> **"오늘 학식 뭐 나오지? 맛은 어떨까?"**  
> 대학 생활의 중심인 학식을 스마트하게 즐기는 방법, HakPlace입니다.

HakPlace는 대학생들을 위한 맞춤형 **학식 조회 및 리뷰 공유 Android 애플리케이션**입니다. 
단순한 메뉴 조회를 넘어, 학생들 간의 솔직한 평가와 AI 기반 식단 분석, 학식 알림을 통해 더 나은 학식 경험을 제공합니다.


## 📸 스크린샷

| 오늘 학식 | 이번주 학식 | 리뷰 목록 | 평점 통계 |
| :---: | :---: | :---: | :---: |
| <img width="200" alt="image" src="https://github.com/user-attachments/assets/f985764f-24d3-4294-93d9-1838c960bb8b" /> | <img width="200" alt="Screenshot_20260516_135629" src="https://github.com/user-attachments/assets/76603b81-add4-4ddb-9b6b-8bd93292b570" /> | <img width="200" alt="Screenshot_20260516_135719" src="https://github.com/user-attachments/assets/6f8426f3-e57f-4890-8340-c0aa8c514072" /> | <img width="200" alt="Screenshot_20260516_135647" src="https://github.com/user-attachments/assets/2039e6ca-f83b-4014-ba1a-6a4c45eb546b" /> |


## 🚀 주요 기능

### 1. 식단 조회 (Today & Weekly)
- **금일 메뉴**: 오늘 제공되는 조식, 중식, 석식 메뉴를 사진과 함께 실시간으로 확인합니다.
- **금주 메뉴**: 요일별/식사 종류별로 일주일간의 식단 계획을 미리 파악할 수 있습니다.

### 2. 정시 학식 알림 시스템 (AlarmManager)
- **평일 맞춤 알림**: 매주 평일(월~금) 지정된 시간대에 필요한 식단 정보를 팝업으로 전달합니다.
  - **08:00**: 오늘의 조식/중식/석식 전체 요약 카드
  - **11:30**: 오늘의 중식 단독 메뉴 알림
  - **17:30**: 오늘의 석식 단독 메뉴 알림
- **개별 알림 설정**: 마이페이지에서 각 시간대별 알림을 개별 ON/OFF 할 수 있습니다.

### 3. 리뷰 및 평점 커뮤니티
- **리뷰 작성**: 메뉴별 별점과 리뷰, 음식 사진을 함께 등록합니다.
- **실시간 평점 순위**: 학생들의 누적 평가를 바탕으로 오늘의 베스트 메뉴를 한눈에 확인합니다.
- **AI 기반 식단 분석**: 주간/월간 단위의 평점 추이 차트 및 AI가 요약한 식단 평가 인사이트를 제공합니다.

### 4. 사용자 관리
- **회원가입 및 로그인**: JWT 기반 인증을 통해 안전하게 계정을 관리합니다.
- **마이페이지**: 내가 쓴 리뷰 목록을 관리하고 프로필 정보를 수정할 수 있습니다.


## 🛠 기술 스택

- **Platform**: Android Native
- **Language**: Java 21 (Android SDK / compileSdk 36, minSdk 24)
- **Architecture**: MVVM (ViewModel, LiveData, Repository 패턴)
- **Networking**: Retrofit2, OkHttp3, Gson
- **Image Loading**: Glide (이미지 캐싱 및 로딩 최적화)
- **UI/UX**: Material Design, Custom Progress Bars, Multi-part Image Upload
- **Security**: EncryptedSharedPreferences (토큰 보안 저장)  


## 📂 프로젝트 구조

```text
app/src/main
├── AndroidManifest.xml      # 앱 매니페스트 (권한, 리시버, 액티비티 설정)
├── java/kr/ac/waltdev29/hakplace
│   ├── api/                 # 네트워크 통신 계층 (Retrofit, API 서비스, DTO 모델)
│   ├── receiver/            # 백그라운드 리시버 (정시 알림 트리거, 기기 부팅 복원)
│   ├── utils/               # 공통 유틸리티 (알림 스케줄러, 암호화 토큰 관리, 다이얼로그 등)
│   └── (Activities)/        # 주요 화면 액티비티 (식단 조회, 리뷰, 마이페이지 등)
└── res/
    ├── layout/              # 화면 및 다이얼로그 XML 레이아웃
    ├── values/              # 언어별 다국어(ko/ja), 색상, 테마 리소스
    └── drawable/            # 이미지 및 그래픽 리소스
```


## ⚙️ 시작하기

### 사전 요구사항
- Android Studio Ladybug / Koala 이상 권장
- JDK 21
- Android SDK API Level 24 (Nougat) 이상 지원

### 설치 방법
최신 버전을 다운로드하세요. 👉 [Release](https://github.com/WaltDev29/HakPlace/releases)

## 🤝 기여하기
버그 리포트나 개선 아이디어는 [GitHub Issues](https://github.com/WaltDev29/HakPlace/issues)를 통해 언제든지 등록해 주세요!
