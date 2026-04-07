import React from 'react';
import sampleProfiles from '../dashboard/sample-profiles.json';


export default function Cards() {
    
  return (
    <>
    {sampleProfiles.map((profile) => (
        <div className="" style={{width:500}}  key={profile.id}>
            <div className="card m-3">
                <img src="/profile.png" className="card-img-top img-fluid" style={{maxWidth:100}}/>
                <div className="card-body">
                    <p className="ms-2">
                        Name: {profile.profileName}
                    </p>
                    <p className="ms-2">Emergency Contact Name: {profile.emergencyName}</p>
                    <p className="ms-2">Emergency Contact Phone: {profile.emergencyPhone}       </p>
                    <p>
                        <button type="submit" className="btn btn-primary m-2" value="">Generate QR Code</button>
                        <button type="submit" className="btn btn-primary m-2" value ="">Edit Profile</button>
                        <button type="submit" className="btn btn-danger m-2" value="">Delete</button>
                    </p>
                </div>
            </div>
        </div>
    ))}
    </>
  )
}