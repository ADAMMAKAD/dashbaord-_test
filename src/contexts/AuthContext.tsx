import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { login as apiLogin, logout as apiLogout, getAuthToken, setAuthToken, LoginRequest, User } from '../services/api';

// Auth context interface - keeping it simple for now
interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  loading: boolean;
  login: (credentials: LoginRequest) => Promise<void>;
  logout: () => void;
  error: string | null;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

// Custom hook for using auth context
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

interface AuthProviderProps {
  children: ReactNode;
}

// Main auth provider component
export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true); // start with loading true
  const [error, setError] = useState<string | null>(null);

  // Check if user is already logged in when app starts
  useEffect(() => {
    const token = getAuthToken();
    if (token) {
      // T should probably validate token with server instead of just trusting it
      // but for now this works fine
      try {
        // decode JWT payload - this is a bit hacky but works
        const payload = JSON.parse(atob(token.split('.')[1]));
        const userData = {
          id: payload.sub === 'admin' ? 1 : 2, // hardcoded IDs for now
          username: payload.sub,
          role: payload.sub === 'admin' ? 'ADMIN' : 'USER'
        };
        setUser(userData);
      } catch (err) {
        apiLogout();
      }
    }
    setIsLoading(false);
  }, []);

  const login = async (credentials: LoginRequest) => {
    try {
      setIsLoading(true);
      setError(null);
      const response = await apiLogin(credentials);
      setAuthToken(response.token);
      
      const payload = JSON.parse(atob(response.token.split('.')[1]));
      const userData: User = {
        id: payload.sub === 'admin' ? 1 : 2,
        username: payload.sub,
        role: payload.sub === 'admin' ? 'ADMIN' : 'USER'
      };
      
      setUser(userData);
    } catch (err) {
      const errorMsg = err instanceof Error ? err.message : 'Login failed';
      setError(errorMsg);
      throw err;
    } finally {
      setIsLoading(false);
    }
  };

  const logout = () => {
    apiLogout();
    setUser(null);
    setError(null);
  };

  // Context value object
  const contextValue: AuthContextType = {
    user,
    isAuthenticated: !!user, // convert to boolean
    loading: isLoading,
    login,
    logout,
    error
  };

  return (
    <AuthContext.Provider value={contextValue}>
      {children}
    </AuthContext.Provider>
  );
};