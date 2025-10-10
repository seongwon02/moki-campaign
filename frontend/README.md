# 미방문 고객 관리 프로젝트 (Non-Visiting Customer Management Project)

본 문서는 **Vite + React + TypeScript** 기반의 **미방문 고객 관리 프론트엔드 애플리케이션**에 대한 개요, 설치 및 실행 방법, 기술 스택, 코드 품질 관리, 그리고 디렉토리 구조를 설명합니다.

---

## 1. 프로젝트 개요

이 프로젝트는 **지정된 기간 동안 매장 방문 빈도가 줄어든 고객**을 효율적으로 식별하고 관리하기 위한 프론트엔드 시스템입니다. 고객 데이터를 시각적으로 제공하고, 맞춤형 마케팅 전략 수립을 지원하는 것을 목표로 하고 있습니다.

### 주요 기능

- **고객 리스트 조회**: 기간별·조건별 필터링을 통한 고객 목록 확인
- **고객 상세 정보**: 개별 고객의 과거 구매 이력, 방문 기록 등 상세 정보 확인
- **통계 및 대시보드**: 고객 방문 현황, 재방문률 등 핵심 지표 시각화
- **캠페인 관리 (선택적)**: 특정 고객 그룹을 대상으로 하는 마케팅 캠페인 생성 및 추적

---

## 2. 기술 스택 및 개발 환경

| 분류              | 기술          | 버전 | 설명                                     |
| :---------------- | :------------ | :--- | :--------------------------------------- |
| **프레임워크**    | React         | 18.x | 사용자 인터페이스 구축 핵심 라이브러리   |
| **타입 스크립트** | TypeScript    | 5.x  | 정적 타입 검사를 통한 안정성 확보        |
| **빌드 도구**     | Vite          | 5.x  | 빠르고 경량화된 개발 환경 및 번들링 제공 |
| **상태 관리**     | Redux Toolkit | 최신 | 전역 상태 관리 및 비동기 로직 처리       |
| **스타일링**      | Tailwind CSS  | 최신 | 유틸리티 기반의 효율적인 스타일링 적용   |
| **API 통신**      | Axios         | 최신 | 백엔드 API와의 통신 담당                 |

---

## 3. 개발 환경 설정 및 설치

### 3.1 필수 요구사항

- **Node.js**: v18 이상
- **npm**: 최신 버전 (본 프로젝트에서는 npm을 기본 패키지 매니저로 사용)

### 3.2 설치

프로젝트 루트 디렉토리에서 다음 명령어를 실행하여 종속성을 설치합니다:

```bash
npm install
```

### 3.3 환경변수 설정

프로젝트 루트에 `.env` 파일을 생성하고 다음과 같이 환경 변수를 설정합니다:

```env
VITE_API_BASE_URL="http://localhost:8080/api/v1"
```

## 4. 프로젝트 실행 및 빌드

### 4.1 개발 모드 실행

개발 서버를 시작하여 로컬 환경에서 애플리케이션을 확인합니다.

```bash
npm run dev
```

### 4.2 빌드 및 배포

배포용 정적 파일을 빌드합니다. 결과물은 dist/ 디렉토리에 생성됩니다.

```bash
npm run build
```

## 5. 코드 품질 관리 (ESLint Configuration)

본 프로젝트는 TypeScript의 장점을 극대화하고 코드 품질을 유지하기 위해 `typescript-eslint` 기반의 타입 안전 린트 규칙을 사용합니다.

### 5.1 타입 기반 린트 규칙

`esling.config.js` 에는 `typescript-eslint`의 `strictTypeChecked` 규칙이 활성화되어 있습니다.

### 5.2 React 전용 린트 규칙

React 컴포넌트의 모범 사례 및 잠재적 오류를 탐지하기 위해 다음 플러그인을 적용합니다:

- `eslint-plugin-react-x`
- `eslint-plugin-react-dom`

## 6. 디렉토리 구조

```
frontend/
├── node_modules/             # 프로젝트 종속성
├── public/
│   └── index.html            # HTML 진입점
├── src/
│   ├── assets/               # 이미지, 폰트 등 정적 리소스
│   ├── components/           # 재사용 가능한 UI 컴포넌트 집합
│   │   ├── common/           # Button, Input 등 범용 (Atomic) 컴포넌트
│   │   ├── layout/           # Header, Sidebar, Footer 등 앱 구조 컴포넌트
│   │   └── modules/          # 페이지별 복합 컴포넌트
│   ├── hooks/                # 커스텀 훅
│   ├── pages/                # 라우팅되는 페이지 컴포넌트
│   ├── services/             # API 통신 로직 및 정의
│   ├── store/                # 상태 관리 (Redux slice 등)
│   ├── types/                # TypeScript 타입 정의 파일
│   ├── utils/                # 유틸리티 함수
│   ├── App.css               # 앱 전역 스타일
│   ├── App.tsx               # 메인 앱 컴포넌트 및 라우팅 설정
│   ├── index.css             # 루트 인덱스 스타일
│   └── main.tsx              # 애플리케이션 진입점 (DOM 렌더링)
├── .gitignore                # Git 추적 제외 파일 목록
├── eslint.config.js          # ESLint 설정 파일
├── package-lock.json         # NPM 종속성 잠금 파일
├── package.json              # 프로젝트 종속성 및 스크립트 정의
├── postcss.config.js         # PostCSS 설정 (TailwindCSS 전처리)
├── README.md                 # 현재 문서
├── tailwind.config.js        # Tailwind CSS 설정 파일
├── tsconfig.app.json         # 애플리케이션 코드용 TypeScript 설정
├── tsconfig.json             # 기본 TypeScript 설정
├── tsconfig.node.json        # Node 환경용 TypeScript 설정
└── vite.config.ts            # Vite 빌드 도구 설정 파일
```
