"use client";

import { useContext, useEffect, useMemo, useState } from "react";
import { useParams } from "next/navigation";
import { ToastContext } from "../../components/toastProvider";

export default function PublicProfilePage() {
  const params = useParams();
  const rawUuid = params?.uuid;
  const uuid = Array.isArray(rawUuid) ? rawUuid[0] : rawUuid;

  const { showToast } = useContext(ToastContext);

  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState("");
  const [submitStatus, setSubmitStatus] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const [formData, setFormData] = useState({
    sender: "",
    messageContent: "",
  });

  const backendUrl =
    process.env.NEXT_PUBLIC_BACKEND_URL || "http://localhost:8080";

  useEffect(() => {
    const fetchPublicProfile = async () => {
      if (!uuid) {
        setLoadError("Missing profile UUID.");
        setLoading(false);
        return;
      }

      setLoading(true);
      setLoadError("");
      setProfile(null);

      try {
        const response = await fetch(
          `${backendUrl}/api/public/profiles/${uuid}`,
          {
            method: "GET",
            headers: {
              "Content-Type": "application/json",
            },
            cache: "no-store",
          }
        );

        const responseText = await response.text();

        if (!response.ok) {
          throw new Error(`Failed to load public profile (${response.status})`);
        }

        const data = responseText ? JSON.parse(responseText) : null;

        const isEmptyObject =
          data &&
          typeof data === "object" &&
          !Array.isArray(data) &&
          Object.keys(data).length === 0;

        if (!data || isEmptyObject) {
          throw new Error("Public profile returned no data.");
        }

        setProfile(data);
      } catch (error) {
        console.error("Error loading public profile:", error);
        setLoadError("Unable to load this public profile.");
        showToast("Unable to load public profile.", "error");
      } finally {
        setLoading(false);
      }
    };

    fetchPublicProfile();
  }, [uuid, backendUrl, showToast]);

  const displayName = useMemo(() => {
    if (!profile) return "";

    if (profile.displayName) return profile.displayName;
    if (profile.name) return profile.name;
    if (profile.fullName) return profile.fullName;

    const firstName = profile.firstName || "";
    const lastName = profile.lastName || "";
    const fullName = `${firstName} ${lastName}`.trim();

    return fullName || "Profile";
  }, [profile]);

  const receiver = useMemo(() => {
    if (!profile) return "Emergency Contact";

    const emergencyName = `${profile.emergencyFirstName || ""} ${
      profile.emergencyLastName || ""
    }`.trim();

    return (
      profile.receiver ||
      profile.receiverName ||
      profile.contactName ||
      profile.emergencyContact?.name ||
      emergencyName ||
      "Emergency Contact"
    );
  }, [profile]);

  const handleChange = (event) => {
    const { name, value } = event.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSubmitStatus("");
    setSubmitting(true);

    if (!formData.sender.trim() || !formData.messageContent.trim()) {
      const msg = "Please fill out your name and message.";
      setSubmitStatus(msg);
      showToast(msg, "warning");
      setSubmitting(false);
      return;
    }

    const payload = {
  messageContent: formData.messageContent.trim(),
  displayName,
  profileUUID: uuid,
  sender: formData.sender.trim(),
  receiver: receiver || "Emergency Contact",
  dateSent: new Date().toISOString(),
};

    try {
      const response = await fetch(`${backendUrl}/api/contact`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(payload),
      });

      const responseText = await response.text().catch(() => "");

      if (!response.ok) {
        throw new Error(
          responseText || `Failed to send message (${response.status})`
        );
      }

      const successMsg = "Message sent successfully.";
      setSubmitStatus(successMsg);
      showToast(successMsg, "success");
      setFormData({
        sender: "",
        messageContent: "",
      });
    } catch (error) {
      console.error("Error sending message:", error);
      const errorMsg = error?.message || "Unable to send message right now.";
      setSubmitStatus(errorMsg);
      showToast(errorMsg, "error");
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="container py-5">
        <div className="text-center">Loading public profile...</div>
      </div>
    );
  }

  if (loadError) {
    return (
      <div className="container py-5">
        <div className="alert alert-danger" role="alert">
          {loadError}
        </div>
      </div>
    );
  }

  if (!profile) {
    return (
      <div className="container py-5">
        <div className="alert alert-warning" role="alert">
          Public profile not found.
        </div>
      </div>
    );
  }

  return (
    <div className="container py-5">
      <div className="row justify-content-center">
        <div className="col-lg-8">
          <div className="card shadow-sm mb-4">
            <div className="card-body">
              <h1 className="card-title mb-3">{displayName}</h1>

              {profile.bio && <p className="card-text">{profile.bio}</p>}

              <div className="row">
                {profile.phoneNumber && (
                  <div className="col-md-6 mb-2">
                    <strong>Phone:</strong> {profile.phoneNumber}
                  </div>
                )}

                {profile.email && (
                  <div className="col-md-6 mb-2">
                    <strong>Email:</strong> {profile.email}
                  </div>
                )}

                {profile.address && (
                  <div className="col-12 mb-2">
                    <strong>Address:</strong> {profile.address}
                  </div>
                )}

                {profile.notes && (
                  <div className="col-12 mb-2">
                    <strong>Notes:</strong> {profile.notes}
                  </div>
                )}
              </div>
            </div>
          </div>

          <div className="card shadow-sm">
            <div className="card-body">
              <h2 className="h4 mb-3">Send a Message</h2>

              <form onSubmit={handleSubmit}>
                <div className="mb-3">
                  <label htmlFor="sender" className="form-label">
                    Your Name
                  </label>
                  <input
                    type="text"
                    id="sender"
                    name="sender"
                    className="form-control"
                    value={formData.sender}
                    onChange={handleChange}
                    required
                  />
                </div>

                <div className="mb-3">
                  <label htmlFor="messageContent" className="form-label">
                    Message
                  </label>
                  <textarea
                    id="messageContent"
                    name="messageContent"
                    className="form-control"
                    rows="5"
                    value={formData.messageContent}
                    onChange={handleChange}
                    required
                  />
                </div>

                <button
                  type="submit"
                  className="btn btn-primary"
                  disabled={submitting}
                >
                  {submitting ? "Sending..." : "Send Message"}
                </button>
              </form>

              {submitStatus && (
                <div className="mt-3 alert alert-info" role="alert">
                  {submitStatus}
                </div>
              )}

              <div className="mt-3 text-muted small">
                Resolved receiver: {receiver || "none returned"}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}