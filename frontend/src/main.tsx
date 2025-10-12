import React from "react";
import ReactDOM from "react-dom/client";
import { Provider } from "react-redux";
import App from "./App.tsx";
import { store } from "./store/index.ts";
// import './index.css'; // Vite 프로젝트 기본 CSS (Tailwind CSS 설정 파일에서 처리되는 경우가 많습니다.)

ReactDOM.createRoot(document.getElementById("root")!).render(
  // strict mode는 개발 중 잠재적인 문제를 감지하는 데 도움을 줍니다.
  <React.StrictMode>
    {/* Redux Store를 전체 앱에 제공합니다. */}
    <Provider store={store}>
      <App />
    </Provider>
  </React.StrictMode>
);
