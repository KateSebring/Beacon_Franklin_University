"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { useParams } from 'next/navigation'
import Link from "next/link";
import useAuth from "./useAuth";

export default function ProfileForm({ profileId, onSubmit }) {
    const router = useRouter();
    const { loggedIn, loading } = useAuth();

    useEffect(() => {
        if (!loading && !loggedIn) {
        router.replace("/auth/login");
        }
    }, [loggedIn, loading, router]);

    const [form, setForm] = useState({
        firstName: "",
        lastName: "",
        emergencyFirstName: "",
        emergencyLastName: "",
        emergencyEmail: "",
    });

    useEffect(() => {
        if(!profileId) {
            return;
        }

        const fetchExistingProfile = async () => {
            const token = localStorage.getItem("token");
            const myHeaders = new Headers();
            myHeaders.append("Authorization", "Bearer " + token);
            try {
              const response = await fetch(`${process.env.NEXT_PUBLIC_BACKEND_URL}/api/profile/${profileId}`, {
                method: "GET",
                headers: myHeaders
              });
            
              if(!response.ok) {
                  alert("Something went wrong! " + response.status);
              }

              const data = await response.json();

              setForm({
                  firstName: data.firstName || "",
                  lastName: data.lastName || "",
                  emergencyFirstName: data.emergencyFirstName || "",
                  emergencyLastName: data.emergencyLastName || "",
                  emergencyEmail: data.emergencyEmail || "",
              })

            } catch (err) {
                alert("Something went wrong! " + err);
            }
        }

        fetchExistingProfile();
    }, [profileId])

  function handleChange(e) {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  }

  const handleSave = (e) => {
    e.preventDefault()
    if (onSubmit) {
      onSubmit(form);
    }
  }

  if (loading || !loggedIn) return null;

  return (
    <main style={{ padding: 24, maxWidth: 700, margin: "0 auto" }}>
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          gap: 12,
        }}
      >
        <h1 style={{ margin: 0 }}>Profile Settings</h1>

        <Link
          href="/profiles"
          style={{
            padding: "10px 14px",
            border: "1px solid #ddd",
            borderRadius: 6,
            textDecoration: "none",
            color: "inherit",
          }}
        >
          Back to Profiles
        </Link>
      </div>

      <form
        onSubmit={handleSave}
        style={{
          marginTop: 20,
          padding: 16,
          border: "1px solid #ddd",
          borderRadius: 8,
          display: "grid",
          gap: 12,
        }}
      >
        <h2 style={{ margin: 0 }}>Profile</h2>

        <div
          style={{
            display: "grid",
            gridTemplateColumns: "1fr 1fr",
            gap: 12,
          }}
        >
          <label>
            First Name
            <input
              name="firstName"
              value={form.firstName}
              onChange={handleChange}
              style={{ width: "100%", padding: 10, marginTop: 4 }}
              required
            />
          </label>

          <label>
            Last Name
            <input
              name="lastName"
              value={form.lastName}
              onChange={handleChange}
              style={{ width: "100%", padding: 10, marginTop: 4 }}
              required
            />
          </label>
        </div>

        <h2 style={{ margin: 0, marginTop: 8 }}>Emergency Contact</h2>

        <div
          style={{
            display: "grid",
            gridTemplateColumns: "1fr 1fr",
            gap: 12,
          }}
        >
          <label>
            First Name
            <input
              name="emergencyFirstName"
              value={form.emergencyFirstName}
              onChange={handleChange}
              style={{ width: "100%", padding: 10, marginTop: 4 }}
              required
            />
          </label>

          <label>
            Last Name
            <input
              name="emergencyLastName"
              value={form.emergencyLastName}
              onChange={handleChange}
              style={{ width: "100%", padding: 10, marginTop: 4 }}
              required
            />
          </label>
        </div>

        <label>
          Email
          <input
            type="email"
            name="emergencyEmail"
            value={form.emergencyEmail}
            onChange={handleChange}
            style={{ width: "100%", padding: 10, marginTop: 4 }}
            required
          />
        </label>

        <div style={{ display: "flex", gap: 10, marginTop: 6 }}>
          <button
            type="submit"
            style={{
              padding: "10px 14px",
              cursor: "pointer",
              border: "1px solid #ddd",
              borderRadius: 6,
              background: "white",
            }}
          >
            Save
          </button>

          <button
            type="button"
            onClick={() => router.push("/profiles")}
            style={{
              padding: "10px 14px",
              cursor: "pointer",
              border: "1px solid #ddd",
              borderRadius: 6,
              background: "white",
            }}
          >
            Cancel
          </button>
        </div>
      </form>
    </main>
  );
}
