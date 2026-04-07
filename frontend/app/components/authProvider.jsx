"use client";

import { createContext, useEffect, useState } from "react";
import { jwtDecode } from "jwt-decode";

export const AuthContext = createContext();

export function AuthProvider({ children }) {
  const [loggedIn, setLoggedIn] = useState(false);
  const [loading, setLoading] = useState(true); 

  useEffect(() => {
    const token = localStorage.getItem("token");

    if (!token) {
      setLoggedIn(false);
      setLoading(false);
      return;
    }

    try {
      const decoded = jwtDecode(token);
      const currentTime = Date.now() / 1000;

      if (!decoded.exp || decoded.exp < currentTime) {
        localStorage.removeItem("token");
        setLoggedIn(false);
      } else {
        setLoggedIn(true);
      }
    } catch (error) {
      localStorage.removeItem("token");
      setLoggedIn(false);
    }

    setLoading(false);
  }, []);

  function login(token) {
    localStorage.setItem("token", token);
    setLoggedIn(true);
  }

  function logout() {
    localStorage.removeItem("token");
    setLoggedIn(false);
  }

  return (
    <AuthContext.Provider value={{ loggedIn, login, logout, loading }}>
      {children}
    </AuthContext.Provider>
  );
}