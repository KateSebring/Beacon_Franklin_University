"use client";

import { useEffect, useMemo, useState } from "react";
import { QRCodeCanvas } from "qrcode.react";
import { useParams } from "next/navigation";

export default function PublicProfilePrintPage() {
  const params = useParams();
  const rawUuid = params?.uuid;
  const uuid = Array.isArray(rawUuid) ? rawUuid[0] : rawUuid;

  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [previewMode, setPreviewMode] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  const frontendUrl =
    process.env.NEXT_PUBLIC_FRONTEND_URL || "http://localhost:3000";
  const backendUrl =
    process.env.NEXT_PUBLIC_BACKEND_URL || "http://localhost:8080";

  const qrValue = useMemo(() => {
    if (!uuid) return frontendUrl;
    return `${frontendUrl}/p/${uuid}`;
  }, [frontendUrl, uuid]);

  useEffect(() => {
    async function fetchProfile() {
      if (!uuid) {
        setErrorMessage("Profile UUID is missing.");
        setPreviewMode(true);
        setLoading(false);
        return;
      }

      try {
        const res = await fetch(`${backendUrl}/api/public/profiles/${uuid}`);

        if (!res.ok) {
          const errorText = await res.text();
          console.error("Public profile fetch failed:", res.status, errorText);
          setErrorMessage("Public profile data unavailable.");
          setPreviewMode(true);
          return;
        }

        const data = await res.json();
        setProfile(data);
      } catch (error) {
        console.error("Public profile fetch exception:", error);
        setErrorMessage("Could not load public profile data.");
        setPreviewMode(true);
      } finally {
        setLoading(false);
      }
    }

    fetchProfile();
  }, [uuid, backendUrl]);

  if (loading) {
    return <p className="p-4">Loading...</p>;
  }

  const displayName =
    profile?.displayName ||
    `${profile?.firstName || ""} ${profile?.lastName || ""}`.trim() ||
    "Demo User";

  return (
    <div className="container py-4 text-center">
      <div className="d-print-none mb-4">
        <button
          type="button"
          className="btn btn-primary"
          onClick={() => window.print()}
        >
          Print
        </button>
      </div>

      <h1 className="mb-3">Profile QR Code</h1>

      {previewMode && (
        <p className="text-muted mb-3">
          Preview mode: {errorMessage || "backend profile data unavailable."}
        </p>
      )}

      <p className="mb-2">
        <strong>Person:</strong> {displayName}
      </p>

      <p className="mb-4">
        <strong>UUID:</strong> {uuid || "Unavailable"}
      </p>

      <div className="d-flex justify-content-center mb-4">
        <QRCodeCanvas
          value={qrValue}
          size={220}
          level="H"
          marginSize={2}
        />
      </div>

      <p className="mb-0">
        Scan this QR code to open the public profile page.
      </p>
    </div>
  );
}