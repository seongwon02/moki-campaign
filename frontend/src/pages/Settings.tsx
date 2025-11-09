import React from "react";
import { useDispatch } from "react-redux";
import { useNavigate } from "react-router-dom";
import Button from "../components/common/Button";
import { logout } from "../store/authSlice";
import { type AppDispatch } from "../store";

interface SettingsPageProps {
  onClose: () => void;
}

const SettingsPage: React.FC<SettingsPageProps> = ({ onClose }) => {
  const dispatch = useDispatch<AppDispatch>();
  const navigate = useNavigate();

  const handleLogout = () => {
    dispatch(logout());
    navigate("/login");
  };

  const handleQAClick = () => {
    // Q&A page navigation or modal logic will be here
    console.log("Q&A button clicked");
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-80 backdrop-blur-md flex items-center justify-center" onClick={onClose}>
      <div className="w-full max-w-xs p-8 space-y-4 bg-white rounded-xl shadow-lg" onClick={(e) => e.stopPropagation()}>
        <Button onClick={handleLogout} variant="primary" className="w-full">
          로그아웃
        </Button>
        <Button onClick={handleQAClick} variant="secondary" className="w-full">
          Q&A
        </Button>
      </div>
    </div>
  );
};

export default SettingsPage;
