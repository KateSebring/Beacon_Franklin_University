"use client";

import React, { useEffect, useContext } from 'react'
import LoggedInMenu from './loggedInMenu';
import LoggedOutMenu from './loggedOutMenu';
import {AuthContext} from './authProvider'



export default function Header() {
  const { loggedIn, loading } = useContext(AuthContext);

  useEffect(() => {
    require("bootstrap/dist/js/bootstrap.bundle.min.js");
  }, []);

    return (
    <div id="header" className="border-bottom">
      <nav className='navbar'>
          <a href='/'className="h1 align-middle mb-0">
            <img src="/BeaconLogoWide.jpg" alt="Beacon Logo" className="img-fluid" style={{height: '50px'}} />
          </a>
          <div className='text-end font-size-sm'>
            {!loading && (loggedIn ? <LoggedInMenu /> : <LoggedOutMenu />)}
          </div>
      </nav>
    </div>
  )
}
