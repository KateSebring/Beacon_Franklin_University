"use client";

import { useEffect, useMemo, useState, useContext } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import useAuth from "../components/useAuth";
import { ToastContext } from "../components/toastProvider";

export default function MessagesPage() {
  const router = useRouter();
  const { loggedIn, loading } = useAuth();
  const { showToast } = useContext(ToastContext);

  const [mounted, setMounted] = useState(false);
  const [search, setSearch] = useState("");
  const [messages, setMessages] = useState([]);
  const [messagesLoading, setMessagesLoading] = useState(true);
  const [messagesError, setMessagesError] = useState("");

  useEffect(() => {
    setMounted(true);
  }, []);

  useEffect(() => {
    if (!loading && !loggedIn) {
      router.replace("/auth/login");
    }
  }, [loggedIn, loading, router]);

  function getBackendIsRead(message) {
    if (typeof message?.isRead === "boolean") {
      return message.isRead;
    }

    if (typeof message?.read === "boolean") {
      return message.read;
    }

    if (typeof message?.status === "string") {
      return message.status.trim().toUpperCase() === "READ";
    }

    if (typeof message?.messageStatus === "string") {
      return message.messageStatus.trim().toUpperCase() === "READ";
    }

    return false;
  }

  useEffect(() => {
    async function fetchMessages() {
      const token = localStorage.getItem("token");

      if (!token) {
        setMessagesLoading(false);
        setMessagesError("You must be logged in to view messages.");
        return;
      }

      try {
        setMessagesLoading(true);
        setMessagesError("");

        const response = await fetch("http://localhost:8080/api/messages", {
          method: "GET",
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        });

        if (!response.ok) {
          const errorText = await response.text();
          console.error("Messages response error:", response.status, errorText);

          if (response.status === 401) {
            throw new Error("Your session expired. Please log in again.");
          }

          if (response.status === 403) {
            throw new Error("You do not have permission to view messages.");
          }

          throw new Error(`Failed to load messages. Status: ${response.status}`);
        }

        const data = await response.json();

        const normalizedMessages = Array.isArray(data)
          ? data.map((message, index) => {
              const id = message.id ?? index;
              const isRead = getBackendIsRead(message);

              return {
                id,
                senderName:
                  message.sender || message.displayName || "Unknown Sender",
                relationship: message.receiver || "Contact",
                body:
                  message.messageContent ||
                  message.body ||
                  message.message ||
                  "",
                createdAt:
                  message.dateSent ||
                  message.createdAt ||
                  message.sentAt ||
                  message.timestamp ||
                  "",
                isNew: !isRead,
              };
            })
          : [];

        setMessages(normalizedMessages);
      } catch (err) {
        console.error("Message load error:", err);
        const errorMessage = err.message || "Could not load messages.";
        setMessagesError(errorMessage);
        showToast(errorMessage, "error");
      } finally {
        setMessagesLoading(false);
      }
    }

    if (!loading && loggedIn) {
      fetchMessages();
    }
  }, [loading, loggedIn]);

  function formatTime(dateValue) {
    if (!dateValue) return "No timestamp";

    if (
      typeof dateValue === "string" &&
      /^\d{4}-\d{2}-\d{2}$/.test(dateValue)
    ) {
      const [year, month, day] = dateValue.split("-").map(Number);
      const localDate = new Date(year, month - 1, day);

      return localDate.toLocaleDateString(undefined, {
        month: "short",
        day: "2-digit",
        year: "numeric",
      });
    }

    const d = new Date(dateValue);

    if (Number.isNaN(d.getTime())) {
      return String(dateValue);
    }

    return d.toLocaleString(undefined, {
      month: "short",
      day: "2-digit",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  }

  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase();

    if (!q) {
      return messages;
    }

    return messages.filter((m) => {
      return (
        (m.senderName || "").toLowerCase().includes(q) ||
        (m.relationship || "").toLowerCase().includes(q) ||
        (m.body || "").toLowerCase().includes(q)
      );
    });
  }, [messages, search]);

  function markAsRead(id) {
    setMessages((prev) =>
      prev.map((m) =>
        String(m.id) === String(id) ? { ...m, isNew: false } : m
      )
    );

    showToast("Message marked as read", "success");
  }

  if (loading || !loggedIn) return null;

  return (
    <main style={{ padding: 24, maxWidth: 1100, margin: "0 auto" }}>
      <Link href="/dashboard" style={{ textDecoration: "none" }}>
        ← Back to Dashboard
      </Link>

      <h1 style={{ marginTop: 12 }}>Messages</h1>

      <section style={{ minWidth: 0 }}>
        <div
          style={{
            display: "flex",
            alignItems: "center",
            justifyContent: "space-between",
            gap: 12,
            flexWrap: "wrap",
          }}
        >
          <h2 style={{ margin: 0 }}>Inbox</h2>

          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search messages..."
            style={{ padding: 10, width: 260, maxWidth: "100%" }}
          />
        </div>

        {messagesLoading && (
          <div style={{ marginTop: 12, color: "#666" }}>
            Loading messages...
          </div>
        )}

        {messagesError && (
          <div style={{ marginTop: 12, color: "#b00020" }}>
            {messagesError}
          </div>
        )}

        {!messagesLoading && !messagesError && (
          <div style={{ marginTop: 12, display: "grid", gap: 12 }}>
            {filtered.map((m) => (
              <div
                key={m.id}
                style={{
                  border: "1px solid #e5e5e5",
                  borderRadius: 10,
                  padding: 14,
                  boxShadow: "0 1px 2px rgba(0,0,0,0.04)",
                }}
              >
                <div
                  style={{
                    display: "flex",
                    alignItems: "flex-start",
                    justifyContent: "space-between",
                    gap: 12,
                  }}
                >
                  <div>
                    <div style={{ fontWeight: 700 }}>
                      {m.senderName}{" "}
                      <span style={{ fontWeight: 400, color: "#666" }}>
                        • {m.relationship}
                      </span>
                    </div>

                    <div
                      style={{ color: "#666", fontSize: 12 }}
                      suppressHydrationWarning
                    >
                      {mounted ? formatTime(m.createdAt) : ""}
                    </div>
                  </div>

                  <span
                    style={{
                      padding: "2px 8px",
                      borderRadius: 999,
                      fontSize: 12,
                      fontWeight: 700,
                      background: m.isNew ? "#198754" : "#6c757d",
                      color: "white",
                      height: "fit-content",
                    }}
                  >
                    {m.isNew ? "New" : "Read"}
                  </span>
                </div>

                <div style={{ marginTop: 10, color: "#333" }}>{m.body}</div>

                {m.isNew && (
                  <button
                    type="button"
                    onClick={() => markAsRead(m.id)}
                    style={{
                      marginTop: 12,
                      padding: "8px 12px",
                      cursor: "pointer",
                      borderRadius: 6,
                      border: "1px solid #ddd",
                      background: "white",
                    }}
                  >
                    Mark as Read
                  </button>
                )}
              </div>
            ))}

            {filtered.length === 0 && (
              <div style={{ color: "#666", marginTop: 12 }}>
                No messages found.
              </div>
            )}
          </div>
        )}
      </section>
    </main>
  );
}