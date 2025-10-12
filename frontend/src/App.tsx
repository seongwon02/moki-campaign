import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
// 확장자를 명시하여 모듈 확인 오류를 해결합니다.
import TestPage from "./pages/TestPage.tsx";
import LoginPage from "./pages/Login.tsx";

// Redux를 위한 Provider와 store를 가정합니다.
// 실제 프로젝트에서는 Redux Store를 구성하고 main.tsx에서 감싸주어야 합니다.
const MockStoreProvider = ({ children }: { children: React.ReactNode }) => {
  // Redux를 설정하기 전까지는 단순 Wrapper로 사용합니다.
  return <>{children}</>;
};

const App = () => {
  return (
    // 라우팅을 위해 BrowserRouter를 사용합니다.
    <MockStoreProvider>
      <Router>
        <div className="App">
          <Routes>
            {/* 테스트를 위해 초기 경로('/')에 TestPage를 연결합니다. */}
            <Route path="/" element={<TestPage />} />

            {/* 실제 로그인 페이지 경로는 /login으로 가정합니다. */}
            <Route path="/login" element={<LoginPage />} />

            {/* 나머지 페이지 경로는 추후 추가됩니다. */}
            {/* <Route path="/dashboard" element={<MainDashboardPage />} /> */}
          </Routes>
        </div>
      </Router>
    </MockStoreProvider>
  );
};

export default App;
