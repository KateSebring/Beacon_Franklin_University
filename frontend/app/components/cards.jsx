"use client";
import React, { useEffect } from "react";

export default function Cards() {

  useEffect(() => {
    // load bootstrap JS
    const bootstrap = require("bootstrap/dist/js/bootstrap.bundle.min.js");

    const carousel = document.querySelector("#cardsCarousel");

    if (carousel) {
      new bootstrap.Carousel(carousel, {
        interval: 3000,
        ride: "carousel",
      });
    }
  }, []);

  return (
    <div
      className="shadow p-3 mb-5 bg-body-tertiary rounded d-flex align-items-center justify-content-center"
      style={{ minHeight: 150 }}
    >
      <div
        id="cardsCarousel"
        className="carousel slide"
        style={{ width: "50%" }}
      >
        <div className="carousel-inner text-center">
          <div className="carousel-item active">
            <h5>Peace of Mind</h5>
            <p>
              Create multiple profiles and manage them all from one secure location
            </p>
          </div>

          <div className="carousel-item">
            <h5>Safe and secure</h5>
            <p>
              Others can scan your QR code to directly and securely send you a
              message without them getting your contact information
            </p>
          </div>

          <div className="carousel-item">
            <h5>Privacy Protection</h5>
            <p>
              The QR code only links to a secure messaging system, so no
              personal information is exposed
            </p>
          </div>
        </div>

        <button
          className="carousel-control-prev"
          type="button"
          data-bs-target="#cardsCarousel"
          data-bs-slide="prev"
        >
          <span className="carousel-control-prev-icon"></span>
        </button>

        <button
          className="carousel-control-next"
          type="button"
          data-bs-target="#cardsCarousel"
          data-bs-slide="next"
        >
          <span className="carousel-control-next-icon"></span>
        </button>
      </div>
    </div>
  );
}