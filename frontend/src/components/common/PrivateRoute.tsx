import { useSelector } from "react-redux";
import { Navigate, Outlet } from "react-router-dom";
import { type RootState } from "../../store";

/**
 * 인증된 사용자만 접근할 수 있는 라우트를 위한 Wrapper 컴포넌트
 * 사용자가 인증되지 않은 경우 로그인 페이지로 리디렉션합니다.
 */
const PrivateRoute = () => {
  const { isAuthenticated } = useSelector((state: RootState) => state.auth);

  // 인증 상태를 확인하여 조건부 렌더링 또는 리디렉션
  return isAuthenticated ? <Outlet /> : <Navigate to="/login" replace />;
};

export default PrivateRoute;
