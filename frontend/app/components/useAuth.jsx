"use client";

import { useContext } from "react";
import { AuthContext } from "../components/authProvider";

export default function useAuth() {
  return useContext(AuthContext);
}