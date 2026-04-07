"use client";

import { useRouter } from "next/navigation";
import { useEffect, useState, useContext } from "react";
import useAuth from "../../components/useAuth";
import { ToastContext } from "../../components/toastProvider";

export default function Home() {
  const router = useRouter();
  const { loggedIn, loading } = useAuth();
  const { showToast } = useContext(ToastContext);

  const [userInfo, setUserInfo] = useState(null);
  const [formValues, setFormValues] = useState({
    username: "",
    firstName: "",
    lastName: "",
    dob: "",
    email: "",
  });

  const [passwordValues, setPasswordValues] = useState({
    currentPassword: "",
    newPassword: "",
  });

  const [isEditing, setIsEditing] = useState(false);
  const [showPasswordForm, setShowPasswordForm] = useState(false);

  useEffect(() => {
    if (!loading && !loggedIn) {
      router.replace("/auth/login");
    }
  }, [loggedIn, loading, router]);

  useEffect(() => {
    if (!loggedIn) return;

    const token = localStorage.getItem("token");

    fetch("http://localhost:8080/api/users/me", {
      headers: { Authorization: `Bearer ${token}` },
    })
      .then(async (res) => {
        const text = await res.text();
        if (!res.ok) throw new Error(text);
        return JSON.parse(text);
      })
      .then((data) => {
        setUserInfo(data);
        setFormValues(data);
      })
      .catch((err) => console.error(err));
  }, [loggedIn]);

  function handleChange(e) {
    const { name, value } = e.target;
    setFormValues((prev) => ({ ...prev, [name]: value }));
  }
  
  function handlePasswordChange(e) {
    const { name, value } = e.target;
    setPasswordValues((prev) => ({ ...prev, [name]: value }));
  }

  async function handleSave(e) {
    e.preventDefault();
    const token = localStorage.getItem("token");

    try {
      const res = await fetch("http://localhost:8080/api/users/me", {
        method: "PUT",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify(formValues),
      });

      const text = await res.text();
      if (!res.ok) {
        let errorMessage = "Update failed";

        try {
          const errorData = JSON.parse(text);
          errorMessage = errorData.detail || errorData.title || errorMessage;
        } catch {
          errorMessage = text || errorMessage;
        }

        showToast(errorMessage, "error");
        return;
      }


      const updated = JSON.parse(text);
      setUserInfo(updated);
      setIsEditing(false);
      showToast("Profile updated successfully!", "success");
    } catch (err) {
      console.error(err);
      showToast("Update failed", "error");
    }
  }

  async function handlePasswordSave(e) {
    e.preventDefault();
    const token = localStorage.getItem("token");

    try {
      const res = await fetch("http://localhost:8080/api/users/me/password", {
        method: "PUT",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify(passwordValues),
      });

      const text = await res.text();
      if (!res.ok) throw new Error(text);

      setPasswordValues({ currentPassword: "", newPassword: "" });
      showToast("Password updated!", "success");
      setShowPasswordForm(false);
    } catch (err) {
      console.error(err);
      showToast("Password update failed", "error");
    }
  }

  if (loading || !loggedIn) return null;

  return (
    <div className="m-3  mt-4">
      
      {/* INFO / EDIT SECTION */}
      {!isEditing && userInfo && (
        
        <div>
          <h3>User Settings</h3>
          <div className="container m-2 mt-3">
            <div className="row mb-2">
              <label className="col-sm-2 fw-bold">Username:</label>
              <div className="col-sm-3">
                {userInfo.username}
              </div>
            </div>
            <div className="row mb-2">
              <label className="col-sm-2 fw-bold">First Name:</label>
              <div className="col-sm-3">
                {userInfo.firstName}
              </div>
            </div>
            <div className="row mb-2">
              <label className="col-sm-2 fw-bold">Last Name:</label>
              <div className="col-sm-3">
                {userInfo.lastName}
              </div>
            </div>
            <div className="row mb-2">
              <label className="col-sm-2 fw-bold">DOB:</label>
              <div className="col-sm-3">
                {userInfo.dob}
              </div>
            </div>
            <div className="row mb-2">
              <label className="col-sm-2 fw-bold">Email:</label>
              <div className="col-sm-3">
                {userInfo.email}
              </div>
            </div>
          </div>
          <div>
            <button className="btn btn-primary m-2 ms-3" onClick={() => setIsEditing(true)}>
            Edit
          </button>
          </div>
          <div className="form-group m-3 mt-2">
            <button className="btn btn-secondary" onClick={() => setShowPasswordForm((v) => !v)}>
            Change Password
          </button>
          </div>
          

        </div>
        
      )}

      {isEditing && (
       <> 
       <h3>Edit Settings</h3>
        <form onSubmit={handleSave}>
          <div className="row mb-3" style={{maxWidth:500}}>
            <label className="col-form-label col-sm-3">Username:</label>
            <div className="col-sm-7">
              <input name="username" className="form-control"value={formValues.username} onChange={handleChange} /> 
            </div>  
          </div>
          <div className="row mb-3" style={{maxWidth:500}}>
            <label className="col-form-label col-sm-3">First Name:</label>
            <div className="col-sm-7">
              <input name="firstName" className="form-control" value={formValues.firstName} onChange={handleChange} />
            </div>
          </div>
          <div className="row mb-3" style={{maxWidth:500}}>
            <label className="col-form-label col-sm-3">Last Name:</label>
            <div className="col-sm-7">
              <input name="lastName" className="form-control" value={formValues.lastName} onChange={handleChange} />
            </div>
          </div>
          <div className="row mb-3" style={{maxWidth:500}}>
            <label className="col-form-label col-sm-3">DOB:</label>
            <div className="col-sm-7">
              <input type="date" name="dob" className="form-control" value={formValues.dob} onChange={handleChange} />
            </div>
          </div>
          <div className="row mb-3" style={{maxWidth:500}}>
            <label className="col-form-label col-sm-3">Email:</label>
            <div className="col-sm-7">
              <input name="email" className="form-control" value={formValues.email} onChange={handleChange} />
            </div>
          </div>

          <button type="submit" className="btn btn-primary me-2">
            Save
          </button>
          <button type="button" className="btn btn-secondary " onClick={() => setIsEditing(false)}>
            Cancel
          </button>
        </form>
        </>
      )}

      {/* 👇 PASSWORD FORM NOW ALWAYS UNDERNEATH */}
      {showPasswordForm && (
        <div className="m-3">
          <p>Input your old password, then your new password:</p>
          <form className="m-2" style={{maxWidth: 200}} onSubmit={handlePasswordSave}>
            <div className="row mb-2">
              <input
                type="password"
                className="form-control"
                name="currentPassword"
                placeholder="Current Password"
                value={passwordValues.currentPassword}
                onChange={handlePasswordChange}
              />
            </div>
            <div className="row mb-2">
            <input
              type="password"
              name="newPassword"
              className="form-control"
              placeholder="New Password"
              value={passwordValues.newPassword}
              onChange={handlePasswordChange}
            />
            </div>
            <div className="row">
              <button type="submit" className="btn btn-primary">Update Password</button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
}