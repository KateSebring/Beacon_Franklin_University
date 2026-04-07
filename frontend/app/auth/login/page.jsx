"use client";

import { useEffect, useState, useContext } from "react";
import { useRouter } from "next/navigation";
import { AuthContext } from "../../components/authProvider";
import { ToastContext } from "../../components/toastProvider";

export default function LogIn() {
  const { login } = useContext(AuthContext);
  const { showToast } = useContext(ToastContext);
  const router = useRouter();

  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem("token");
    if (token) {
      router.replace("/dashboard");
    } else {
      setLoading(false);
    }
  }, [router]);

  if (loading) return null;

  async function loginSubmit(e) {
    e.preventDefault();
    const formData = new FormData(e.currentTarget);
    const formValues = {
      username: formData.get("username"),
      password: formData.get("password"),
    };

    const myHeaders = new Headers();
    myHeaders.append("Content-Type", "application/json");
    const jsonData = JSON.stringify(formValues);

    try {
      const response = await fetch("http://localhost:8080/api/auth/login", {
        method: "POST",
        headers: myHeaders,
        body: jsonData,
      });

      if (response.status === 200) {
        const responseToken = await response.json();
        const token = responseToken.token;
        login(token);
        showToast("Welcome to Beacon!", "success");
        router.push("/dashboard");
      } else {
        showToast("Login failed. Please check your credentials.", "error");
      }
    } catch (err) {
      showToast("An error occurred during login. Please try again.", "error");
    }
  }

  return (
    <>
      <div className="p-4">
        <h1>Login</h1>
        <form id="regForm" style={{ maxWidth: 400 }} onSubmit={loginSubmit}>
          <div className="mb-3">
            <input
              id="username"
              name="username"
              type="text"
              className="form-control"
              placeholder="Username"
              required
            />
          </div>
          <div className="mb-3">
            <input
              id="password"
              name="password"
              type="password"
              className="form-control"
              placeholder="Password"
              required
            />
          </div>
          <button type="submit" className="btn btn-primary">
            Log In
          </button>
        </form>
      </div>
    </>
  );
}