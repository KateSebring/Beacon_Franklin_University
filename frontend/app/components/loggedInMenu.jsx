"use client";

import React from "react";
import { useContext } from "react";
import { useRouter } from "next/navigation";
import { AuthContext } from "./authProvider";
import { ToastContext} from "./toastProvider";

export default function LoggedInMenu() {
  const router = useRouter();
  const { logout } = useContext(AuthContext)
  const { showToast } = useContext(ToastContext);
  
  //check for token and logout if not found
  function handleLogout(e) {
    e.preventDefault();
    logout();
    showToast("Logged out successfully.", "info");
    router.push("/auth/login");
  }

  return (
    <ul className="navbar-nav ms-auto">
      <li className="nav-item dropdown" style={{ position: "relative" }}>
        <a
          className="nav-link dropdown-toggle"
          href="#"
          role="button"
          data-bs-toggle="dropdown"
          aria-expanded="false"
        >
          Welcome, User
        </a>

        <ul className="dropdown-menu dropdown-menu-end" style={{ position: "absolute" }}>
          <li>
            <a className="dropdown-item" href="/dashboard">
              My Dashboard
            </a>
          </li>
          <li>
            <a className="dropdown-item" href="/profiles">
              Profiles
            </a>
          </li>
          <li>
            <a className="dropdown-item" href="/messages">
              Messages
            </a>
          </li>
          <li>
            <a className="dropdown-item" href="/user/settings">
              Settings
            </a>
          </li>

          <li>
            <hr className="dropdown-divider" />
          </li>

          <li>
            <a className="dropdown-item" href="/auth/login" onClick={handleLogout}>
              Logout
            </a>
          </li>
        </ul>
      </li>
    </ul>
  );
}