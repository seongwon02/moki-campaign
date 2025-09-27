# moki-campaign

### 작업 방식

**[최초 사용]**

1.  본인의 컴퓨터에서 "Git Bash"를 실행합니다.
2.  `git clone https://github.com/seongwon02/moki-campaign.git`을 입력하여 저장소를 복제합니다.
3.  복제가 완료되면 본인이 맡은 폴더를 찾아 들어갑니다.
4.  맡은 폴더에서 작업을 실시합니다.

---

### 🚀 작업 흐름 (Git Flow)

우리 팀은 이슈를 생성하고, 기능별 브랜치를 만들어 작업한 뒤 Pull Request를 통해 코드 리뷰를 거쳐 `main` 브랜치에 병합하는 전략을 따릅니다.

**1. 이슈 생성 (Create Issue)**
   - 작업을 시작하기 전, GitHub Issues에 새로운 이슈를 생성합니다.
   - 이슈에는 작업의 목표, 상세 기능, 참고 자료 등을 명확하게 기재합니다.
   - 본인에게 이슈를 할당(Assignees)하고, 관련된 라벨(Labels)을 추가합니다.

**2. 브랜치 생성 (Create Branch)**
   - 생성한 이슈를 기반으로 `main` 브랜치에서 새로운 기능 브랜치(feature branch)를 생성합니다.
   - 브랜치 이름은 아래 규칙을 따릅니다.
     ```bash
     git checkout -b <브랜치_이름> main
     ```

   **브랜치 이름 규칙**
   `<type>/<scope>/#<issue-number>-<description>`<br>
    - **type**: `feat`, `fix`, `docs` 등 [커밋 타입](#-allowed-type)과 동일<br>
    - **scope**: `fe`, `be`, `ai` 등 작업 영역<br>
    - **issue-number**: 관련된 이슈 번호<br>
    - **description**: 브랜치에 대한 간단한 설명 (영문 소문자, 띄어쓰기는 `-`로 연결)

   **예시)**
    `feat/fe/#1-user-login`
    `fix/be/#3-token-error`
    `refactor/ai/#5-model-data-preprocessing`

**3. 작업 및 커밋 (Work & Commit)**
   - 기능 개발을 완료한 후, 아래 [커밋 메시지 형식](#-커밋-메시지-형식)에 맞게 커밋합니다.
   - 관련된 변경 사항들을 하나의 논리적인 단위로 묶어 커밋하는 것을 권장합니다.

**4. 원격 저장소에 Push (Push to Remote)**
   - 작업이 완료된 브랜치를 원격 저장소(GitHub)에 push합니다.
     ```bash
     git push origin <브랜치_이름>
     ```

**5. Pull Request (PR) 생성**
   - GitHub에서 `main` 브랜치로 향하는 Pull Request를 생성합니다.
   - PR 제목은 **`[<scope>] <type>: #(이슈번호) <subject>`** 형식으로 작성합니다.
     - 예: `[FE] feat: #1 로그인 페이지 UI 구현`
   - PR 본문에는 관련된 이슈를 `Closes #이슈번호` 형식으로 반드시 포함시켜, PR이 머지될 때 해당 이슈가 자동으로 닫히도록 합니다.
   - 변경 사항, 테스트 방법 등을 상세히 기재 후 팀원들에게 자신이 작업한 내용을 알립니다.
   - 팀원들이 확인하였다면 main으로 merge합니다.

**6. 코드 리뷰 및 병합 (Code Review & Merge)**
   - 리뷰어는 코드를 리뷰하고 피드백을 제공합니다.
   - 리뷰가 완료되고 모든 테스트가 통과되면, PR을 `main` 브랜치로 병합합니다.
   - **Squash and merge** 방식을 사용하여 커밋 히스토리를 깔끔하게 유지합니다.

**7. 브랜치 삭제 (Clean up)**
   - 병합이 완료된 브랜치는 로컬과 원격 저장소에서 모두 삭제하여 저장소를 깨끗하게 관리합니다.

---

### 📝 커밋 메시지 형식

`<type>(<scope>): <subject>`

-   **라인 길이**: 100자 이내
-   **Subject**: 간결히, **명령형·현재형** 사용 (“add” not “added”)
-   첫 글자 대문자 ❌, 마침표 ❌

---

### ✅ Allowed `<type>`

-   **feat**: 새로운 기능
-   **fix**: 버그 수정
-   **docs**: 문서 수정
-   **design**: CSS 등 사용자 UI/UX 디자인 변경
-   **style**: 포맷 변경(세미콜론, 공백 등)
-   **refactor**: 코드 리팩터링 (기능 변화 없음)
-   **test**: 테스트 관련 작업
-   **chore**: 유지보수, 빌드 관련
-   **추가됨**: build, ci, perf

---

### ✅ Allowed `<scope>`

-   변경된 부분의 범위를 구체적으로 명시
-   예: `fe`, `be`, `ai`
