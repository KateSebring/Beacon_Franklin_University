"use client";

import { useState, useEffect, useContext } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import useAuth from "../components/useAuth";
import { ToastContext } from "../components/toastProvider";

export default function ProfilesPage() {
  const router = useRouter();
  const { loggedIn, loading } = useAuth();
  const { showToast } = useContext(ToastContext);

  const [sortBy, setSortBy] = useState("firstName");
  const [searchTerm, setSearchTerm] = useState("");
  const [sortDirection, setSortDirection] = useState("asc");
  const [profiles, setProfiles] = useState([]);
  const [profilesLoading, setProfilesLoading] = useState(true);
  const [profilesError, setProfilesError] = useState("");

  useEffect(() => {
    if (!loading && !loggedIn) {
      router.replace("/auth/login");
    }
  }, [loggedIn, loading, router]);

  useEffect(() => {
    async function fetchProfiles() {
      const token = localStorage.getItem("token");

      if (!token) {
        setProfilesLoading(false);
        setProfilesError("You must be logged in to view profiles.");
        return;
      }

      try {
        setProfilesLoading(true);
        setProfilesError("");

        const myHeaders = new Headers();
        myHeaders.append("Authorization", "Bearer " + token);

        const response = await fetch("http://localhost:8080/api/profile", {
          method: "GET",
          headers: myHeaders,
        });

        if (!response.ok) {
          const errorText = await response.text();
          console.error("Profiles response error:", response.status, errorText);

          if (response.status === 401) {
            throw new Error("Your session expired. Please log in again.");
          }

          if (response.status === 403) {
            throw new Error("You do not have permission to view profiles.");
          }

          throw new Error(`Failed to load profiles. Status: ${response.status}`);
        }

        const data = await response.json();
        setProfiles(Array.isArray(data) ? data : []);
      } catch (e) {
        console.error("Profile load error:", e);
        const errorMessage = e.message || "Could not load profiles.";
        setProfilesError(errorMessage);
        showToast(errorMessage, "error");
      } finally {
        setProfilesLoading(false);
      }
    }

    if (!loading && loggedIn) {
      fetchProfiles();
    }
  }, [loading, loggedIn, showToast]);

  async function handleDeleteProfile(id) {
    const confirmDelete = window.confirm("Are you sure you want to delete this profile?");

    if (!confirmDelete) {
      return;
    }

    const token = localStorage.getItem("token");
    const myHeaders = new Headers();
    myHeaders.append("Authorization", "Bearer " + token);

    try {
      const response = await fetch(`http://localhost:8080/api/profile/${id}`, {
        method: "DELETE",
        headers: myHeaders,
      });

      if (response.status === 204) {
        setProfiles((prevProfiles) => prevProfiles.filter((p) => p.id !== id));
        showToast("Profile successfully deleted.", "success");
      } else {
        const errorText = await response.text();
        console.error("Delete profile error:", response.status, errorText);
        showToast("Failed to delete profile.", "error");
      }
    } catch (e) {
      console.error("Delete profile exception:", e);
      showToast("Something went wrong while deleting the profile.", "error");
    }
  }

  function handleGenerateQr(profile) {
    const profileUuid = profile.uuid || profile.profileUUID || profile.profileUuid;

    if (!profileUuid) {
      showToast("This profile does not have a UUID yet.", "error");
      return;
    }

    window.open(`/p/${profileUuid}/print`, "_blank", "noopener,noreferrer");
  }

  function handleEditProfile(profile) {
    if (!profile?.id) {
      showToast("Profile ID is missing.", "error");
      return;
    }

    router.push(`/profiles/settings?id=${profile.id}`);
  }

  if (loading || !loggedIn) return null;

  const filteredProfiles = profiles.filter((p) => {
    const search = searchTerm.toLowerCase();

    return (
      (p.firstName || "").toLowerCase().includes(search) ||
      (p.lastName || "").toLowerCase().includes(search) ||
      (p.emergencyFirstName || "").toLowerCase().includes(search) ||
      (p.emergencyLastName || "").toLowerCase().includes(search) ||
      (p.emergencyEmail || "").toLowerCase().includes(search)
    );
  });

  const displayProfiles = [...filteredProfiles].sort((a, b) => {
    const aValue = (a[sortBy] || "").toString().toLowerCase();
    const bValue = (b[sortBy] || "").toString().toLowerCase();
    const result = aValue.localeCompare(bValue);
    return sortDirection === "desc" ? -result : result;
  });

  return (
    <main style={{ padding: 24, maxWidth: 1100, margin: "0 auto" }}>
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          gap: 12,
        }}
      >
        <h1 style={{ margin: 0 }}>Profiles</h1>

        <Link
          href="/dashboard"
          style={{
            padding: "10px 14px",
            border: "1px solid #ddd",
            borderRadius: 6,
            textDecoration: "none",
            color: "inherit",
          }}
        >
          Back to Dashboard
        </Link>
      </div>

      <p style={{ marginTop: 8 }}>
        Current profiles are listed here. Use the green + to access Profile Settings.
      </p>

      <div className="d-flex gap-2 mt-3 mb-3" style={{ maxWidth: 400 }}>
        <div className="input-group">
          <input
            className="form-control"
            type="text"
            placeholder="Search profiles..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
          <button
            className="btn btn-outline-secondary"
            type="button"
            onClick={() => setSearchTerm("")}
          >
            Clear
          </button>
        </div>
      </div>

      <div className="d-flex gap-2 mt-3 mb-3" style={{ maxWidth: 400 }}>
        <select
          className="form-select"
          value={sortBy}
          onChange={(e) => setSortBy(e.target.value)}
        >
          <option value="firstName">Sort by First Name</option>
          <option value="lastName">Sort by Last Name</option>
          <option value="emergencyFirstName">Sort by Emergency Contact First Name</option>
          <option value="emergencyLastName">Sort by Emergency Contact Last Name</option>
          <option value="emergencyEmail">Sort by Emergency Contact Email</option>
        </select>

        <button
          className="btn btn-outline-secondary"
          onClick={() => setSortDirection(sortDirection === "asc" ? "desc" : "asc")}
        >
          {sortDirection === "asc" ? " ▲" : " ▼"}
        </button>
      </div>

      {profilesLoading && (
        <div style={{ marginTop: 12, color: "#666" }}>Loading profiles...</div>
      )}

      {profilesError && (
        <div style={{ marginTop: 12, color: "#b00020" }}>{profilesError}</div>
      )}

      {!profilesLoading && !profilesError && (
        <section style={{ marginTop: 20 }}>
          <div
            style={{
              display: "flex",
              gap: 16,
              flexWrap: "wrap",
              marginTop: 12,
              alignItems: "flex-start",
            }}
          >
            {displayProfiles.map((p) => (
              <div
                key={p.id}
                style={{
                  border: "1px solid #ddd",
                  borderRadius: 12,
                  padding: 16,
                  width: 280,
                }}
              >
                <div style={{ fontWeight: 600 }}>
                  Name: {p.firstName} {p.lastName}
                </div>

                <div style={{ marginTop: 8 }}>
                  Emergency Contact: {p.emergencyFirstName} {p.emergencyLastName}
                </div>

                <div style={{ marginTop: 8 }}>
                  Emergency Email: {p.emergencyEmail}
                </div>

                <div style={{ display: "flex", gap: 10, marginTop: 14, flexWrap: "wrap" }}>
                  <button
                    type="button"
                    onClick={() => handleGenerateQr(p)}
                    style={{
                      padding: "8px 10px",
                      border: "1px solid #ddd",
                      borderRadius: 6,
                      background: "#0d6efd",
                      color: "white",
                      cursor: "pointer",
                    }}
                  >
                    Generate QR Code
                  </button>

                  <button
                    type="button"
                    onClick={() => handleEditProfile(p)}
                    style={{
                      padding: "8px 10px",
                      border: "1px solid #ddd",
                      borderRadius: 6,
                      background: "#0d6efd",
                      color: "white",
                      cursor: "pointer",
                    }}
                  >
                    Edit Profile
                  </button>

                  <button
                    type="button"
                    onClick={() => handleDeleteProfile(p.id)}
                    style={{
                      padding: "8px 10px",
                      border: "1px solid #ddd",
                      borderRadius: 6,
                      background: "#dc3545",
                      color: "white",
                      cursor: "pointer",
                    }}
                  >
                    Delete
                  </button>
                </div>
              </div>
            ))}

            {displayProfiles.length === 0 && (
              <div style={{ color: "#666", marginTop: 12 }}>
                No profiles found.
              </div>
            )}

            <Link
              href="/profiles/settings"
              style={{
                width: 60,
                height: 60,
                border: "2px dashed #198754",
                borderRadius: 12,
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                fontSize: 28,
                fontWeight: "bold",
                color: "#198754",
                textDecoration: "none",
                cursor: "pointer",
              }}
              aria-label="Profile settings"
              title="Profile settings"
            >
              +
            </Link>
          </div>
        </section>
      )}
    </main>
  );
}