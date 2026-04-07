"use client";

import { createContext, useState } from "react";
import ToastNotification from "./toastNotification";

export const ToastContext = createContext();

export function ToastProvider({ children }) {
    const [toast, setToast] = useState(null);

    function showToast(message, type = "info") {
        setToast({ message, type, id: Date.now() });
    }

    return (
        <ToastContext.Provider value={{ showToast }}>
            {children}

            {toast && (
                <ToastNotification
                    key={toast.id}
                    message={toast.message}
                    type={toast.type}
                />
            )}
        </ToastContext.Provider>
    )
}