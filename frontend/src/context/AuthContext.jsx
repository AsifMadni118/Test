import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { authService, profileService } from '../services/api';

const AuthContext = createContext();

const INACTIVITY_TIMEOUT_MS = 30 * 60 * 1000; // 30 minutes

export const AuthProvider = ({ children }) => {
  const [token, setToken] = useState(() => localStorage.getItem('proteinpro_token') || null);
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('proteinpro_user');
    return saved ? JSON.parse(saved) : null;
  });
  const [loading, setLoading] = useState(false);

  const clearSession = useCallback(() => {
    setToken(null);
    setUser(null);
    localStorage.removeItem('proteinpro_token');
    localStorage.removeItem('proteinpro_user');
  }, []);

  const logout = useCallback(async () => {
    if (token) {
      await authService.logout(token);
    }
    clearSession();
  }, [token, clearSession]);

  // Fetch current user profile when token is set
  const refreshProfile = useCallback(async (authToken) => {
    if (!authToken) return;
    try {
      setLoading(true);
      const profileData = await profileService.getProfile(authToken);
      setUser(profileData);
      localStorage.setItem('proteinpro_user', JSON.stringify(profileData));
    } catch (err) {
      console.error('Failed to fetch user profile:', err);
      // If token is invalid or expired, log out
      if (err.status === 401) {
        clearSession();
      }
      throw err;
    } finally {
      setLoading(false);
    }
  }, [clearSession]);

  const login = async (authToken) => {
    setToken(authToken);
    localStorage.setItem('proteinpro_token', authToken);
    try {
      await refreshProfile(authToken);
    } catch (error) {
      clearSession();
      throw error;
    }
  };

  const updateUserProfile = (updatedData) => {
    setUser(prev => {
      const next = { ...prev, ...updatedData };
      localStorage.setItem('proteinpro_user', JSON.stringify(next));
      return next;
    });
  };

  // Inactivity timeout handling
  useEffect(() => {
    if (!token) return;

    let timer;
    const resetTimer = () => {
      clearTimeout(timer);
      timer = setTimeout(() => {
        console.log('Inactivity timeout reached. Logging out user.');
        logout();
      }, INACTIVITY_TIMEOUT_MS);
    };

    const events = ['mousedown', 'mousemove', 'keypress', 'scroll', 'touchstart'];
    events.forEach(event => window.addEventListener(event, resetTimer));
    resetTimer();

    return () => {
      clearTimeout(timer);
      events.forEach(event => window.removeEventListener(event, resetTimer));
    };
  }, [token, logout]);

  useEffect(() => {
    const handleUnauthorized = () => clearSession();
    window.addEventListener('proteinpro:unauthorized', handleUnauthorized);
    return () => window.removeEventListener('proteinpro:unauthorized', handleUnauthorized);
  }, [clearSession]);

  return (
    <AuthContext.Provider
      value={{
        token,
        user,
        isAuthenticated: !!token,
        isGuest: !token,
        loading,
        login,
        logout,
        refreshProfile,
        updateUserProfile
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
