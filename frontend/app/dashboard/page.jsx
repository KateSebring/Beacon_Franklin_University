"use client";

import { useRouter } from "next/navigation";
import { useEffect } from "react";
import Link from "next/link";
import useAuth from "../components/useAuth";

export default function DashboardPage() {
  const router = useRouter();
  const { loggedIn, loading } = useAuth();

  useEffect(() => {
    if (!loading && !loggedIn) {
      router.replace("/auth/login");
    }
  }, [loggedIn, loading, router]);

  if (loading || !loggedIn) return null;

  const buttonStyle = {
    padding: "10px 14px",
    border: "1px solid #ddd",
    borderRadius: 8,
    textDecoration: "none",
    display: "inline-flex",
    alignItems: "center",
    justifyContent: "center",
    background: "white",
    color: "inherit",
    cursor: "pointer",
    minWidth: 120,
    fontWeight: 500,
  };

  const cardStyle = {
    border: "1px solid #eee",
    borderRadius: 14,
    padding: 20,
    background: "white",
    boxShadow: "0 2px 8px rgba(0,0,0,0.04)",
  };

  return (
    <main style={{ padding: 24, maxWidth: 1100, margin: "0 auto" }}>
      <div>
        <h1 style={{ margin: 0 }}>Dashboard</h1>
        <p style={{ marginTop: 8, color: "#666" }}>Welcome to Beacon</p>
      </div>

      <section
        style={{
          marginTop: 24,
          display: "grid",
          gridTemplateColumns: "repeat(auto-fit, minmax(240px, 1fr))",
          gap: 16,
        }}
      >
        <div style={cardStyle}>
          <div style={{ fontSize: 28, marginBottom: 10 }}>👤</div>
          <div style={{ fontWeight: 600, marginBottom: 6 }}>Profiles</div>
          <div style={{ color: "#666", marginBottom: 14 }}>
            View, edit, create, or delete profiles.
          </div>
          <Link href="/profiles" style={buttonStyle}>
            Open Profiles
          </Link>
        </div>

        <div style={cardStyle}>
          <div style={{ fontSize: 28, marginBottom: 10 }}>✉️</div>
          <div style={{ fontWeight: 600, marginBottom: 6 }}>Messages</div>
          <div style={{ color: "#666", marginBottom: 14 }}>
            Check incoming messages and updates.
          </div>
          <Link href="/messages" style={buttonStyle}>
            Open Messages
          </Link>
        </div>

        <div style={cardStyle}>
          <div
            style={{
              fontSize: 32,
              marginBottom: 10,
              color: "#198754",
              fontWeight: "bold",
            }}
          >
            +
          </div>
          <div style={{ fontWeight: 600, marginBottom: 6 }}>New Profile</div>
          <div style={{ color: "#666", marginBottom: 14 }}>
            Add a new profile and emergency contact details.
          </div>
          <Link href="/profiles/settings" style={buttonStyle}>
            Create New
          </Link>
        </div>
      </section>
    </main>
  );
}