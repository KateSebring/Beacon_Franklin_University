"use client";

import ProfileForm from '../../../components/profileForm'
import { useRouter } from "next/navigation";
import { useParams } from "next/navigation";
import { useContext } from "react";
import { ToastContext } from "../../../components/toastProvider";

export default function EditProfilePage() {
  const router = useRouter();
  const { id } = useParams();
  const { showToast } = useContext(ToastContext);

  async function handleSave(form) {
    const token = localStorage.getItem("token");
    const myHeaders = new Headers();
    myHeaders.append("Content-Type", "application/json");
    myHeaders.append("Authorization", "Bearer " + token);
    const jsonData = JSON.stringify(form);

    try {
        const response = await fetch(`${process.env.NEXT_PUBLIC_BACKEND_URL}/api/profile/${id}`, {
            method: "PUT",
            headers: myHeaders,
            body: jsonData
        });

        const message = await response.json();

        if(response.ok) {
            showToast("Profile successfully updated!", "success");
            router.push("/profiles");
        } else {
            showToast("Something went wrong with updating the profile.", "error");
        }

    } catch (err) {
        showToast("Something went wrong with updating the profile.", "error")
    }
  }

  return <ProfileForm profileId={id} onSubmit={handleSave}/>
}
