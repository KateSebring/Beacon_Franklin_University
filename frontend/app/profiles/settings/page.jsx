"use client";

import { Suspense, useEffect, useState, useContext } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import Link from "next/link";
import useAuth from "../../components/useAuth";
import { ToastContext } from "../../components/toastProvider";

function ProfileSettingsContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const { loggedIn, loading } = useAuth();
  const { showToast } = useContext(ToastContext);

  const profileId = searchParams.get("id");
  const isEditMode = Boolean(profileId);

  const [form, setForm] = useState({
    firstName: "",
    lastName: "",
    emergencyFirstName: "",
    emergencyLastName: "",
    emergencyEmail: "",
  });

  const [formLoading, setFormLoading] = useState(false);
  const [saving, setSaving] = useState(false);

  const backendUrl =
    process.env.NEXT_PUBLIC_BACKEND_URL || "http://localhost:8080";

  useEffect(() => {
    if (!loading && !loggedIn) {
      router.replace("/auth/login");
    }
  }, [loggedIn, loading, router]);

  useEffect(() => {
    async function loadProfile() {
      const token = localStorage.getItem("token");

      if (!token || !profileId) {
        return;
      }

      try {
        setFormLoading(true);

        const myHeaders = new Headers();
        myHeaders.append("Authorization", "Bearer " + token);

        const response = await fetch(`${backendUrl}/api/profile/${profileId}`, {
          method: "GET",
          headers: myHeaders,
          cache: "no-store",
        });

        if (!response.ok) {
          const errorText = await response.text();
          console.error("Load profile error:", response.status, errorText);

          if (response.status === 401) {
            throw new Error("Your session expired. Please log in again.");
          }

          if (response.status === 403) {
            throw new Error("You do not have permission to view this profile.");
          }

          if (response.status === 404) {
            throw new Error("Profile not found.");
          }

          throw new Error(`Failed to load profile. Status: ${response.status}`);
        }

        const data = await response.json();

        setForm({
          firstName: data.firstName || "",
          lastName: data.lastName || "",
          emergencyFirstName: data.emergencyFirstName || "",
          emergencyLastName: data.emergencyLastName || "",
          emergencyEmail: data.emergencyEmail || "",
        });
      } catch (err) {
        console.error("Profile load exception:", err);
        showToast(err.message || "Could not load profile.", "error");
        router.push("/profiles");
      } finally {
        setFormLoading(false);
      }
    }

    if (!loading && loggedIn && profileId) {
      loadProfile();
    }
  }, [backendUrl, loading, loggedIn, profileId, router, showToast]);

  function handleChange(e) {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  }

  async function handleSave(e) {
    e.preventDefault();

    const token = localStorage.getItem("token");

    if (!token) {
      showToast("You are not logged in.", "error");
      return;
    }

    const myHeaders = new Headers();
    myHeaders.append("Content-Type", "application/json");
    myHeaders.append("Authorization", "Bearer " + token);

    const jsonData = JSON.stringify(form);
    const url = isEditMode
      ? `${backendUrl}/api/profile/${profileId}`
      : `${backendUrl}/api/profile`;
    const method = isEditMode ? "PUT" : "POST";

    try {
      setSaving(true);

      const response = await fetch(url, {
        method,
        headers: myHeaders,
        body: jsonData,
      });

      if (response.ok) {
        showToast(
          isEditMode
            ? "Profile successfully updated."
            : "Profile successfully added!",
          "success"
        );
        router.push("/profiles");
        return;
      }

      const errorText = await response.text();
      console.error("Save profile error:", response.status, errorText);

      if (response.status === 401) {
        showToast("Your session expired. Please log in again.", "error");
      } else if (response.status === 403) {
        showToast("You do not have permission to save this profile.", "error");
      } else {
        showToast("Something went wrong with the profile save.", "error");
      }
    } catch (err) {
      console.error("Save profile exception:", err);
      showToast("Something went wrong while saving the profile.", "error");
    } finally {
      setSaving(false);
    }
  }

  if (loading) {
    return (
      <main style={{ padding: 24, maxWidth: 700, margin: "0 auto" }}>
        <div>Loading profile settings...</div>
      </main>
    );
  }

  if (!loggedIn) return null;

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
        <h1 style={{ margin: 0 }}>
          {isEditMode ? "Edit Profile" : "Profile Settings"}
        </h1>

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

      {formLoading ? (
        <div style={{ marginTop: 20, color: "#666" }}>Loading profile...</div>
      ) : (
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
              disabled={saving}
              style={{
                padding: "10px 14px",
                cursor: "pointer",
                border: "1px solid #ddd",
                borderRadius: 6,
                background: "white",
              }}
            >
              {saving ? "Saving..." : isEditMode ? "Update" : "Save"}
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
      )}
    </main>
  );
}

export default function ProfileSettingsPage() {
  return (
    <Suspense
      fallback={
        <main style={{ padding: 24, maxWidth: 700, margin: "0 auto" }}>
          <div>Loading profile settings...</div>
        </main>
      }
    >
      <ProfileSettingsContent />
    </Suspense>
  );
}