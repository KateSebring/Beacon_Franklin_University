"use client";

import { useContext } from "react";
import { useRouter } from "next/navigation";
import { AuthContext } from "../../components/authProvider";
import { ToastContext } from "../../components/toastProvider";

export default function Home() {
  const router = useRouter();
  const { login } = useContext(AuthContext);
  const { showToast } = useContext(ToastContext);

  async function register(e) {
    e.preventDefault();
    const formData = new FormData(e.currentTarget);
    const formValues = {
      firstName: formData.get("firstName"),
      lastName: formData.get("lastName"),
      dob: formData.get("dateOfBirth"),
      username: formData.get("username"),
      email: formData.get("email"),
      password: formData.get("password"),
    };

    const myHeaders = new Headers();
    myHeaders.append("Content-Type", "application/json");
    const jsonData = JSON.stringify(formValues);

    try {
      const response = await fetch("http://localhost:8080/api/auth/register", {
        method: "POST",
        headers: myHeaders,
        body: jsonData,
      });

      const message = await response.json();

      if (response.status === 201) {
        const loginResponse = await fetch("http://localhost:8080/api/auth/login", {
          method: "POST",
          headers: myHeaders,
          body: JSON.stringify({
            username: formValues.username,
            password: formValues.password,
          }),
        });

        if (loginResponse.status === 200) {
          const loginData = await loginResponse.json();
          const token = loginData.token;
          login(token);
          showToast("Welcome to Beacon!", "success");
          router.push("/dashboard");
          return;
        }

        showToast("Registration worked, but automatic login failed.", "error");
      } else if (response.status === 409) {
        showToast(message.title, "error");
      } else {
        showToast("Registration failed.", "error");
      }
    } catch (err) {
      showToast("An unexpected error occurred. Please try again.", "error");
    }
  }

  return (
    <>
      <div className="p-4">
        <h1>Register for Beacon</h1>
        <form id="regForm" style={{ maxWidth: 400 }} onSubmit={register}>
          <div className="mb-3">
            <input
              id="fname"
              type="text"
              name="firstName"
              className="form-control"
              placeholder="First Name"
              required
            />
          </div>
          <div className="mb-3">
            <input
              id="lname"
              type="text"
              name="lastName"
              className="form-control"
              placeholder="Last Name"
              required
            />
          </div>
          <div className="mb-3">
            <input
              id="dob"
              type="date"
              name="dateOfBirth"
              className="form-control"
              placeholder="Date of Birth"
              required
            />
          </div>
          <div className="mb-3">
            <input
              id="username"
              type="text"
              name="username"
              className="form-control"
              placeholder="Username"
              required
            />
          </div>
          <div className="mb-3">
            <input
              id="email"
              type="email"
              name="email"
              className="form-control"
              placeholder="Email Address"
              required
            />
          </div>
          <div className="mb-3">
            <input
              id="pass"
              type="password"
              name="password"
              className="form-control"
              placeholder="Password"
              required
            />
          </div>
          <button type="submit" className="btn btn-primary">
            Register
          </button>
        </form>
      </div>
    </>
  );
}