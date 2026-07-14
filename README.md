# PIXCEL

# 클라우드 기반 프로젝트 관리 시스템
클라우드 기반 프로젝트 관리 시스템은 **프로젝트 관리자, 팀원, 협업 참여자**​가 프로젝트의 생성부터 업무 관리, 문서 공유, 개발 협업까지 하나의 환경에서 수행할 수 있도록 구현한 **통합 프로젝트 협업 관리 웹 플랫폼**입니다.

---


---
## 프로젝트 미리보기

### 메인 화면


### 시스템 구성도


### 자료 흐름도


### ERD

---

## 프로젝트 개요

### 개발 동기
기존 Redmine을 사용하며 겪었던 오래된 UI, 번거로운 프로젝트 설정 과정, 부족한 실시간 협업 기능 등의 불편함을 해결하기 위해 시작된 프로젝트입니다.

### 프로젝트 개요
PIXCEL은 프로젝트 개발의 생명주기 전반을 지원하는 프로젝트 관리 플랫폼입니다. Spring Boot와 Oracle DB를 기반으로 설계되었으며, 일감 관리부터 Git 저장소 연동, 일정 시각화, 실시간 위키, 테스트 관리까지 핵심 개발 모듈을 제공합니다. 프로젝트마다 필요한 모듈을 선택하여 사용할 수 있습니다.

### 기대 효과
1. 프로젝트 생성 및 설정 과정을 간소화하여 초기 설정 시간 단축
2. 사용자 중심 UI/UX를 통해 누구나 쉽게 사용 가능
3. 실시간 협업 기능을 제공하여 팀원 간 의사소통과 업무 공유 효율성 향상

### 서비스 주소
http://pixcel.cloud/

### 프로젝트 기간
2026.06.08 ~ 2026.07.14
### 개발 절차

| 단계 | 기간 | 주요 활동 |
|------|------|-----------|
| 사전 기획 | 06.08 - 06.09 | 요구사항 도출, 아이디어 구상 |
| 화면 설계 | 06.10 - 06.11 | 화면 UX/UI 체계화, 개발 방향 수립 |
| DB 설계 | 06.12 - 06.15 | 테이블정의서 작성, ERD 모델링, 공통코드 표준화, DB 인프라 구축 |
| 개발 | 06.16 - 07.03 | 간트차트 작성, 화면 구현, 기능 구현, 코드 리뷰 |
| 테스트 | 07.06 - 07.08 | 단위 테스트, 시스템 통합 테스트, 이슈 트래킹 및 결함 작성 |
| 배포 | 07.08 - 07.13 | 배포 프로세스 최적화, 통합 시나리오 작성 |

---
## 주요 기능

### 관리자
- 로그인
- 구독 인증
- 코드값 생성 / 관리
- 일감 유형 / 상태 생성 / 관리
- 역할 생성 / 관리
- 업무흐름 관리

### 프로젝트
- 프로젝트 생성 / 관리
- 구성원 등록 / 관리
- 그룹 생성 / 관리
- 로드맵 생성 / 관리

### 일감
- 일감 생성 / 관리
- 작업내역
- 소요시간 생성 / 관리
- 하위일감 생성 / 관리
- 마일스톤 생성 / 관리

### 현황 보고
- 일감보고서
- 달력
- 간트차트

### 보조 기능
- 위키
- 문서
- 저장소 동기화
- 게시판
- 테스트 / 버그
- 자료실

---

## 업무 흐름

### 관리자
1. 관리자가 로그인 후 구독 인증을 진행합니다.
2. 관리 메뉴를 생성 및 관리합니다.
3. 프로젝트를 생성 및 관리합니다.
4. 로드맵을 생성 및 관리합니다.

### 팀장
1. 팀장이 로그인 후 일감을 생성합니다.
2. 마일스톤 적용 여부에 따라 분기됩니다.
   - **미적용** 시 바로 일감 관리로 이동합니다.
   - **적용** 시 마일스톤을 생성 및 관리한 후 일감 관리로 이동합니다.

### 팀원
1. 팀원이 로그인 후 일감 보고서를 확인합니다.
2. 나의 일감을 관리합니다.
3. 소요시간을 등록 및 관리합니다.

---

## 기술 스택

### 기획 & 디자인
- Google Sheets / Google Docs / Google Drive
- Figma
- ERD Cloud

### IDE
- Eclipse
- DBeaver

### 형상관리
- GitHub

### 배포
- Jenkins
- Docker
- AWS

### DB
- Oracle

### 프론트엔드
- HTML / CSS / JavaScript
- Bootstrap
- SweetAlert2

### 백엔드
- Java
- Spring
- JPA
- MyBatis

### 외부 라이브러리
- FullCalendar
- Tiptap
- Websockets
- Y.js
- DHX Gantt
- Webpack
---

## 기술 스택 시각화

| 구분 | 사용 기술 |
|------|----------|
| Collaboration | ![Figma](https://img.shields.io/badge/figma-%23F24E1E.svg?style=for-the-badge&logo=figma&logoColor=white),![Google Sheets](https://img.shields.io/badge/Google%20Sheets-%2334A853?style=for-the-badge&logo=googlesheets&logoColor=white)|
| Frontend |![HTML5](https://img.shields.io/badge/html5-%23E34F26.svg?style=for-the-badge&logo=html5&logoColor=white),![CSS](https://img.shields.io/badge/css-%23663399.svg?style=for-the-badge&logo=css&logoColor=white),![JavaScript](https://img.shields.io/badge/javascript-%23323330.svg?style=for-the-badge&logo=javascript&logoColor=%23F7DF1E),![Bootstrap](https://img.shields.io/badge/bootstrap-%238511FA.svg?style=for-the-badge&logo=bootstrap&logoColor=white) |
| Backend | ![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white),![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white) |
| Database | ![Oracle](https://img.shields.io/badge/Oracle-F80000?style=for-the-badge&logo=oracle&logoColor=white) |
| Dev Tools | ![Eclipse](https://img.shields.io/badge/Eclipse-FE7A16.svg?style=for-the-badge&logo=Eclipse&logoColor=white) |
| Collaboration | ![Git](https://img.shields.io/badge/Git-F05032?style=for-the-badge&logo=git&logoColor=white),![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white)|
| Deployment | ![AWS](https://img.shields.io/badge/AWS-%23FF9900.svg?style=for-the-badge&logo=amazon-aws&logoColor=white),![Jenkins](https://img.shields.io/badge/jenkins-%232C5263.svg?style=for-the-badge&logo=jenkins&logoColor=white),![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white),![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white) |


---

## 프로젝트 구조

```bash
finalproject/
├── src/main/java/com/pixcel/app/
│   ├── bug/
│   │   ├── mapper/
│   │   ├── service/
│   │   ├── service/impl/
│   │   └── web/
│   ├── calendar/
│   │   ├── mapper/
│   │   ├── service/
│   │   ├── service/impl/
│   │   └── web/
│   ├── categories/
│   │   └── web/
│   ├── codevalue/
│   │   ├── mapper/
│   │   ├── service/
│   │   ├── service/impl/
│   │   └── web/
│   ├── common/
│   │   └── web/
│   ├── config/
│   ├── dashboard/
│   │   └── web/
│   ├── document/
│   │   ├── mapper/
│   │   ├── service/
│   │   ├── service/impl/
│   │   └── web/
│   ├── file/
│   │   ├── config/
│   │   ├── mapper/
│   │   ├── service/
│   │   └── service/impl/
│   ├── gantt/
│   │   ├── service/
│   │   ├── service/impl/
│   │   └── web/
│   ├── group/
│   │   └── web/
│   ├── issues/
│   │   ├── mapper/
│   │   ├── service/
│   │   ├── service/impl/
│   │   └── web/
│   ├── issuestatus/
│   │   ├── mapper/
│   │   ├── service/
│   │   ├── service/impl/
│   │   └── web/
│   ├── issuetype/
│   │   ├── mapper/
│   │   ├── service/
│   │   ├── service/impl/
│   │   └── web/
│   ├── kanban/
│   │   └── web/
│   ├── manage/
│   │   ├── mapper/
│   │   ├── service/
│   │   ├── service/impl/
│   │   └── web/
│   ├── members/
│   │   └── web/
│   ├── milestones/
│   │   ├── mapper/
│   │   ├── service/
│   │   ├── service/impl/
│   │   └── web/
│   ├── notice/
│   │   ├── entity/
│   │   ├── mapper/
│   │   ├── repository/
│   │   ├── service/
│   │   ├── service/impl/
│   │   └── web/
│   ├── permissions/
│   │   └── web/
│   ├── priorities/
│   │   └── web/
│   ├── project/
│   │   ├── mapper/
│   │   ├── service/
│   │   ├── service/impl/
│   │   └── web/
│   ├── repository/
│   │   ├── mapper/
│   │   ├── service/
│   │   ├── service/impl/
│   │   └── web/
│   ├── roadmap/
│   │   ├── mapper/
│   │   ├── service/
│   │   ├── service/impl/
│   │   └── web/
│   ├── roles/
│   │   ├── mapper/
│   │   ├── service/
│   │   ├── service/impl/
│   │   └── web/
│   ├── sourcerepository/
│   │   ├── mapper/
│   │   ├── service/
│   │   ├── service/impl/
│   │   └── web/
│   ├── team/
│   │   ├── service/
│   │   └── web/
│   ├── test/
│   │   ├── mapper/
│   │   ├── service/
│   │   ├── service/impl/
│   │   └── web/
│   ├── testcase/
│   │   ├── mapper/
│   │   ├── service/
│   │   ├── service/impl/
│   │   └── web/
│   ├── testexecution/
│   │   ├── mapper/
│   │   ├── service/
│   │   ├── service/impl/
│   │   └── web/
│   ├── timelog/
│   │   ├── mapper/
│   │   ├── service/
│   │   ├── service/impl/
│   │   └── web/
│   ├── user/
│   │   └── security/
│   ├── users/
│   │   ├── mapper/
│   │   ├── service/
│   │   ├── service/impl/
│   │   └── web/
│   ├── version/
│   │   └── web/
│   ├── websocket/
│   ├── wiki/
│   │   ├── mapper/
│   │   ├── service/
│   │   ├── service/impl/
│   │   └── web/
│   ├── workflow/
│   │   ├── mapper/
│   │   ├── service/
│   │   ├── service/impl/
│   │   └── web/
│   └── workhistory/
│       ├── mapper/
│       ├── service/
│       ├── service/impl/
│       └── web/
│
├── src/main/resources/
│   ├── mapper/
│   │   ├── bug/
│   │   ├── calendar/
│   │   ├── categories/
│   │   ├── codevalue/
│   │   ├── dashboard/
│   │   ├── document/
│   │   ├── file/
│   │   ├── gantt/
│   │   ├── group/
│   │   ├── issues/
│   │   ├── issuestatus/
│   │   ├── issuetype/
│   │   ├── kanban/
│   │   ├── manage/
│   │   ├── members/
│   │   ├── milestones/
│   │   ├── notice/
│   │   ├── permissions/
│   │   ├── priorities/
│   │   ├── project/
│   │   ├── repository/
│   │   ├── roadmap/
│   │   ├── roles/
│   │   ├── sourcerepository/
│   │   ├── team/
│   │   ├── test/
│   │   ├── testcases/
│   │   ├── testexecution/
│   │   ├── timelog/
│   │   ├── timetracking/
│   │   ├── users/
│   │   ├── version/
│   │   ├── wiki/
│   │   ├── workflow/
│   │   ├── workflows/
│   │   └── workhistory/
│   ├── static/
│   │   ├── css/
│   │   ├── fonts/
│   │   ├── img/
│   │   ├── js/
│   │   └── pdf/
│   ├── templates/
│   │   ├── bug/
│   │   ├── calendar/
│   │   ├── categories/
│   │   ├── codevalue/
│   │   ├── common/
│   │   │   ├── configs/
│   │   │   ├── fragments/
│   │   │   └── layouts/
│   │   ├── dashboard/
│   │   ├── document/
│   │   ├── gantt/
│   │   ├── group/
│   │   ├── issues/
│   │   ├── issuestatus/
│   │   ├── issuetype/
│   │   ├── kanban/
│   │   ├── manage/
│   │   ├── members/
│   │   ├── milestones/
│   │   ├── notice/
│   │   ├── permissions/
│   │   ├── priorities/
│   │   ├── project/
│   │   ├── repository/
│   │   ├── roadmap/
│   │   ├── roles/
│   │   ├── sourcerepository/
│   │   ├── team/
│   │   ├── test/
│   │   ├── testcase/
│   │   ├── timelog/
│   │   ├── timetracking/
│   │   ├── users/
│   │   ├── version/
│   │   ├── wiki/
│   │   ├── workflow/
│   │   ├── workflows/
│   │   └── workhistory/
│   └── application.properties
│
├── Dockerfile
├── pom.xml
├── webpack.config.js
└── README.md
