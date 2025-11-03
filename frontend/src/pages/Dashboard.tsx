import React from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import Button from '../components/common/Button';
import { logout } from '../store/authSlice';
import { type RootState, type AppDispatch } from '../store';

const Dashboard: React.FC = () => {
  const dispatch = useDispatch<AppDispatch>();
  const navigate = useNavigate();
  const { token } = useSelector((state: RootState) => state.auth);

  const handleLogout = () => {
    dispatch(logout());
    navigate('/login');
  };

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col items-center justify-center p-8">
      <div className="w-full max-w-4xl bg-white p-8 rounded-xl shadow-2xl text-center">
        <h1 className="text-3xl font-extrabold text-indigo-700 mb-4">
          로그인 성공! 환영합니다.
        </h1>
        <p className="text-gray-600 mb-6">
          성공적으로 대시보드에 접근했습니다.
        </p>
        <div className="mt-4 p-4 bg-gray-100 rounded-lg overflow-x-auto">
          <p className="text-sm text-left text-gray-700 font-mono break-all">
            <b>Auth Token:</b> {token ? token.substring(0, 100) + '...' : '토큰이 없습니다.'}
          </p>
        </div>
        <Button onClick={handleLogout} variant="primary" className="mt-8">
          로그아웃
        </Button>
      </div>
    </div>
  );
};

export default Dashboard;
