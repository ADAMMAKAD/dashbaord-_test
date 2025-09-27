import React from 'react';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import Dashboard from './components/Dashboard';
import LoginPage from './components/LoginPage';
import './styles.css';

// Main app content - handles routing based on auth state
function AppContent() {
  const { user, loading } = useAuth();

  // Show loading spinner while checking auth
  if (loading) {
    return (
      <div className="loading-container">
        <div className="loading-spinner">Loading...</div>
      </div>
    );
  }

  // Simple routing - show dashboard if logged in, login page if not
  return user ? <Dashboard /> : <LoginPage />;
}

// Root app component
function App() {
  return (
    <AuthProvider>
      <div className="App">
        <AppContent />
      </div>
    </AuthProvider>
  );
}

export default App;
