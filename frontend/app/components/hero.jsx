import React from 'react'

const sloganStyle = {
  fontFamily: "Lucida Handwriting",
  color: "#1A3C4A"
}

export default function Hero() {
  return (
    <div id="hero" className="m-4 text-center">
      <div className="m-2">
      <img src="/BeaconLogo.jpg" alt="Beacon Logo" className="img-fluid" style={{height: '150px'}} />
      </div>
      <div>
        <h4 style={sloganStyle }>FOR YOUR CHILD'S SAFETY</h4>
        <h4 style={sloganStyle  }>FOR YOUR PEACE OF MIND</h4>
      </div>
    </div>

  )
}
