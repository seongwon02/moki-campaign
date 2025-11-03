import { Routes, Route } from "react-router-dom";
import LoginPage from "./pages/Login.tsx";
import Dashboard from "./pages/Dashboard.tsx";
import TestPage from "./pages/TestPage.tsx";
import PrivateRoute from "./components/common/PrivateRoute.tsx";

const App = () => {
  return (
    <div className="App">
      <Routes>
        {/* 로그인 페이지 */}
        <Route path="/login" element={<LoginPage />} />

        {/* 테스트 페이지 */}
        <Route path="/test" element={<TestPage />} />

        {/* 보호된 경로 */}
        <Route element={<PrivateRoute />}>
          <Route path="/" element={<Dashboard />} />
          {/* 여기에 다른 보호된 페이지들을 추가할 수 있습니다. */}
          {/* 예: <Route path="/customers" element={<CustomerPage />} /> */}
        </Route>

        {/* 일치하는 경로가 없을 때 처리 (선택 사항) */}
        {/* <Route path="*" element={<NotFoundPage />} /> */}
      </Routes>
    </div>
  );
};

export default App;