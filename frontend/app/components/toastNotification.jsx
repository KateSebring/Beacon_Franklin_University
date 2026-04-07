"use client";

import { useEffect, useRef } from "react";

export default function ToastNotification({ type = "info", message }) {

    const toastRef = useRef(null);
    const variants = {
        success: "text-bg-success",
        error: "text-bg-danger",
        info: "text-bg-primary",
        warning: "text-bg-warning"
    };

    const cls = variants[type] || variants.info;

    useEffect(() => {
        import("bootstrap").then(({ Toast }) => {
            const toast = new Toast(toastRef.current);
            toast.show();
        });
    }, []);

    return (
        <div className="position-fixed bottom-0 end-0 p-3" style={{ zIndex: 1080 }}>
            <div 
                ref={toastRef}
                className={`toast fade ${cls}`} 
                role="alert"
                data-bs-autohide="true"
                data-bs-delay="5000"
            >

                <div className="toast-header">
                    <strong className="me-auto">Notification</strong>
                </div>

                <div className="toast-body">
                    {message}
                </div>

            </div>
        </div>
    );
}