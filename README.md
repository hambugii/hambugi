# 햄부기 (Hambugi)

> 대학생의 시간표·출결 관리를 한 곳에서 돕는 Android 애플리케이션

<img width="400" height="850" alt="image04" src="https://github.com/user-attachments/assets/ac29dca0-fbba-4ed8-9651-19f6738471ae" />

## 프로젝트 소개

수업 시간표를 등록하고, 날짜별 출결을 기록하며, 과목별 출석률을 확인할 수 있는 출결 관리 앱입니다. 매번 수업 시간과 출결 현황을 따로 확인해야 하는 불편을 줄이는 데 초점을 맞췄습니다. 학기별로 정보를 분리하고 수업 시작 알림을 제공해 학기 단위의 일정 관리 흐름을 지원합니다.

| 구분 | 내용 |
| --- | --- |
| 프로젝트명 | 햄부기 (Hambugi) |
| 플랫폼 | Android |
| 개발 언어 | Java |
| 최소 지원 버전 | Android 7.0 (API 24) |
| 데이터 저장 | SQLite |
| 개발 형태 | 팀 프로젝트 |

## 핵심 기능

### 1. 회원가입 및 로그인

- 영문과 숫자 조합의 아이디·비밀번호 유효성 검증
- 아이디 중복 확인 및 로컬 사용자 정보 저장
- `SharedPreferences`를 활용한 로그인 세션 유지와 로그아웃 처리

<img width="400" height="850" alt="image01" src="https://github.com/user-attachments/assets/999628bb-1136-4247-b157-b04f945e6d3f" />
<img width="400" height="850" alt="image02" src="https://github.com/user-attachments/assets/5afb3457-5d78-47b7-82af-a7840f26e853" />


### 2. 학기별 시간표 관리

- 학기를 선택해 시간표와 출결 데이터를 분리하여 관리
- 과목명, 교수명, 강의실, 요일, 시작·종료 시간을 등록
- 등록된 강의 수정 및 삭제 지원
- 같은 요일의 시간대가 겹치는 강의를 사전에 검증

<img width="400" height="850" alt="image05" src="https://github.com/user-attachments/assets/8e016502-c4f7-4891-afae-56d46810a36d" />

### 3. 출결 기록 및 출석률 확인

- 날짜와 요일에 맞는 강의를 불러와 교시별 출결 상태 입력
- 출석·지각·결석 상태를 저장하고, 같은 항목을 다시 입력하면 최신 상태로 갱신
- 과목별 출석률을 계산해 메인 화면에서 진행 바로 제공
- 현재 학기에는 당일 수업 중심으로, 다른 학기에는 해당 학기의 전체 과목 현황을 표시

<img width="400" height="850" alt="image06" src="https://github.com/user-attachments/assets/a8b0e0eb-1364-4e76-8ce6-32a048a00eb3" />

### 4. 캘린더 기반 출결 조회

- 연도·월 선택이 가능한 월간 캘린더 제공
- 날짜를 선택해 해당 일자의 출결 입력 화면으로 이동
- 출결 상태에 따라 캘린더 날짜에 시각적 상태를 표시

<img width="400" height="850" alt="image07" src="https://github.com/user-attachments/assets/e496e7b9-e138-4f91-a16c-62d70e19d8c7" />

### 5. 수업 알림

- 시간표에 등록한 수업을 기준으로 수업 5분 전과 정시에 알림 예약
- Android 알림 권한 및 정확한 알람 권한 처리
- 알림 토글로 학기 내 등록 수업의 알림을 일괄 설정·해제

<img width="320" height="98" alt="스크린샷 2026-08-22 202050" src="https://github.com/user-attachments/assets/7b15c3e6-8926-421f-ae50-f6bdc616627b" />
<img width="322" height="98" alt="스크린샷 2026-08-22 202122" src="https://github.com/user-attachments/assets/c539dad4-33fe-4d24-aa3b-9707086a16ee" />


## 기술적 구현 포인트

| 주제 | 구현 내용 |
| --- | --- |
| 로컬 데이터 모델링 | `users`, `timetable`, `attend` 테이블을 SQLite로 구성하고 사용자·학기·과목 단위로 데이터를 조회했습니다. |
| 데이터 정합성 | 출결 데이터에 복합 기본 키를 적용하고 `CONFLICT_REPLACE`로 중복 입력 시 최신 기록이 반영되도록 했습니다. |
| 시간표 충돌 방지 | 새 강의 등록 전 같은 학기·요일의 시작/종료 시간을 비교해 겹치는 수업을 차단했습니다. |
| 데이터 삭제 처리 | 시간표 삭제 시 관련 출결 기록도 함께 삭제하도록 트랜잭션으로 묶어 고아 데이터를 방지했습니다. |
| Android 시스템 연동 | `AlarmManager`, `BroadcastReceiver`, `PendingIntent`를 사용해 수업 알림을 예약했습니다. |
| 사용자 상태 관리 | `SharedPreferences`로 로그인 세션, 선택 학기, 알림 토글 상태를 유지했습니다. |

## 화면 흐름

```text
로그인 / 회원가입
        ↓
메인 화면 (학기 선택 · 시간표 · 출석률)
   ├── 강의 등록 / 수정 / 삭제
   └── 캘린더
          ↓
     날짜별 출결 입력
```

## 프로젝트 구조

```text
app/src/main/
├── java/com/example/hambugi_am/
│   ├── StartActivity.java          # 로그인
│   ├── SignUpActivity.java         # 회원가입
│   ├── MainActivity.java           # 시간표·출석률 메인 화면
│   ├── AddscheduleActivity.java    # 강의 등록·수정·삭제
│   ├── CalenderActivity.java       # 월간 캘린더
│   ├── AttendCheckActivity.java    # 날짜별 출결 입력
│   ├── AttendCalculActivity.java   # 출결·출석률 계산
│   ├── DBHelper.java               # SQLite 데이터 접근
│   ├── AlarmHelper.java            # 수업 알림 예약·해제
│   └── AlarmReceiver.java          # 알림 수신
└── res/                            # 레이아웃·색상·이미지 리소스
```

## 실행 방법

1. Android Studio에서 프로젝트를 엽니다.
2. Gradle 동기화가 완료될 때까지 기다립니다.
3. Android 7.0(API 24) 이상 에뮬레이터 또는 실기기를 선택합니다.
4. 앱을 실행합니다.
5. 알림 기능 사용 시 알림 권한과 정확한 알람 권한을 허용합니다.

## 사용 기술

- Java 11
- Android SDK (compileSdk 35 / targetSdk 35)
- AndroidX AppCompat, ConstraintLayout, Material Components
- SQLite / SQLiteOpenHelper
- SharedPreferences
- AlarmManager / BroadcastReceiver

## 문서

- [요구사항 분석서](docs/유한출결보조앱_요구사항%20분석서.hwpx)
- [기능 명세서](docs/유한출결보조앱_기능%20명세서.hwpx)
- [UI 설계 PDF](docs/모바일%20UI.pdf)
- [UI 설계 Figma 파일](docs/모바일%20UI.fig)

## 팀

**햄부기 (Hambugi)**

- 팀장: 김은비
- 팀원: 김려원, 김원정, 박한별, 윤나래

