<<<<<<< HEAD
import { useState } from 'react'
import reactLogo from './assets/react.svg'
import viteLogo from '/vite.svg'
import './App.css'

function App() {
  const [count, setCount] = useState(0)

  return (
    <>
      <div>
        <a href="https://vite.dev" target="_blank">
          <img src={viteLogo} className="logo" alt="Vite logo" />
        </a>
        <a href="https://react.dev" target="_blank">
          <img src={reactLogo} className="logo react" alt="React logo" />
        </a>
      </div>
      <h1>Vite + React</h1>
      <div className="card">
        <button onClick={() => setCount((count) => count + 1)}>
          count is {count}
        </button>
        <p>
          Edit <code>src/App.jsx</code> and save to test HMR
        </p>
      </div>
      <p className="read-the-docs">
        Click on the Vite and React logos to learn more
      </p>
    </>
  )
}

export default App
=======
import React, { useState, useEffect, Suspense, lazy } from 'react';
import { getMe, logout as doLogout } from './utils/services/authService';
import { getAccessToken } from './utils/tokenStorage';
import { Toaster } from 'react-hot-toast';

const LoginPage = lazy(() => import('./pages/LoginPage'));
const RegisterPage = lazy(() => import('./pages/RegisterPage'));
const HomePage = lazy(() => import('./pages/HomePage'));
const AdminHomePage = lazy(() => import('./pages/AdminHomePage'));
const NotFoundPage = lazy(() => import('./pages/NotFoundPage'));

function App() {
  const getInitialPage = () => {
    const path = window.location.pathname;
    // Luôn hiển thị trang đăng nhập khi vào / hoặc /login
    if (path === '/' || path === '/login') return 'login';
    if (path === '/register') return 'register';
    // Protected pages require a token
    const token = getAccessToken();
    if (!token) return 'login';
    if (path.startsWith('/admin')) return 'admin-home';
    if (path.startsWith('/home')) return 'user-home';
    return 'not-found';
  };

  const [currentPage, setCurrentPage] = useState(getInitialPage);
  const [userRole, setUserRole] = useState('user');
  const [isBootstrapping, setIsBootstrapping] = useState(true);

  useEffect(() => {
    const bootstrap = async () => {
      try {
        const token = getAccessToken();
        if (!token) {
          // No token → always go to login (unless on register)
          if (currentPage !== 'register') setCurrentPage('login');
          return;
        }

        // Nếu đang ở trang login hoặc register → KHÔNG tự redirect
        // Chỉ validate token cho các protected pages (admin-home, user-home)
        if (currentPage === 'login' || currentPage === 'register') {
          return;
        }

        const principal = await getMe();
        const roles = principal?.roles ?? principal?.authorities ?? [];
        const rolesStr = Array.isArray(roles)
          ? roles.map(r => (typeof r === 'string' ? r : r?.authority ?? r?.role ?? '')).join(',')
          : String(roles);
        const isAdmin = /ADMIN/i.test(rolesStr);
        setUserRole(isAdmin ? 'admin' : 'user');

        // Nếu user thường đang ở trang admin → đẩy về trang chủ user
        if (currentPage === 'admin-home' && !isAdmin) {
          setCurrentPage('user-home');
          window.history.replaceState(null, '', '/home');
        }
        // Cho phép admin xem giao diện user bình thường, không tự động force sang admin-home.
      } catch {
        doLogout();
        setCurrentPage('login');
      } finally {
        setIsBootstrapping(false);
      }
    };
    bootstrap();
  }, []);

  useEffect(() => {
    const handlePopState = () => {
      setCurrentPage(getInitialPage());
    };
    window.addEventListener('popstate', handlePopState);
    return () => window.removeEventListener('popstate', handlePopState);
  }, []);

  useEffect(() => {
    if (currentPage === 'login') {
      if (window.location.pathname !== '/login' && window.location.pathname !== '/') {
        window.history.pushState(null, '', '/login');
      }
      document.title = 'Đăng nhập - EngLearn';
    } else if (currentPage === 'register') {
      if (window.location.pathname !== '/register') {
        window.history.pushState(null, '', '/register');
      }
      document.title = 'Đăng ký - EngLearn';
    } else if (currentPage === 'not-found') {
      document.title = 'Không tìm thấy trang - EngLearn';
    }
  }, [currentPage]);

  const renderPage = () => {
    if (currentPage === 'login') {
      return (
        <LoginPage
          onNavigateToRegister={() => setCurrentPage('register')}
          onLoginSuccess={(role) => {
            setUserRole(role);
            setCurrentPage(role === 'admin' ? 'admin-home' : 'user-home');
          }}
        />
      );
    }
    if (currentPage === 'register') {
      return <RegisterPage onNavigateToLogin={() => setCurrentPage('login')} />;
    }
    if (currentPage === 'user-home') {
      return <HomePage onLogout={() => { doLogout(); setCurrentPage('login'); }} />;
    }
    if (currentPage === 'admin-home' && userRole === 'admin') {
      return <AdminHomePage onLogout={() => { doLogout(); setCurrentPage('login'); }} />;
    }
    if (currentPage === 'not-found' || (currentPage === 'admin-home' && userRole === 'user')) {
      return <NotFoundPage onNavigateHome={() => setCurrentPage(userRole === 'admin' ? 'admin-home' : 'user-home')} />;
    }
    return null;
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Toaster position="top-right" toastOptions={{ duration: 3000 }} />
      {isBootstrapping && currentPage !== 'login' && currentPage !== 'register' ? (
        <div className="min-h-screen flex items-center justify-center bg-slate-50">
          <div className="flex flex-col items-center gap-4">
            <div className="w-12 h-12 border-4 border-cyan-200 border-t-cyan-600 rounded-full animate-spin"></div>
            <div className="text-gray-500 font-bold">Đang tải...</div>
          </div>
        </div>
      ) : (
        <Suspense fallback={
          <div className="min-h-screen flex items-center justify-center bg-slate-50">
            <div className="w-10 h-10 border-4 border-gray-200 border-t-gray-600 rounded-full animate-spin"></div>
          </div>
        }>
          {renderPage()}
        </Suspense>
      )}
    </div>
  );
}

export default App;
>>>>>>> trithuan
