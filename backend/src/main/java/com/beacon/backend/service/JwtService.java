package com.beacon.backend.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import com.beacon.backend.security.AccountUserDetails;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

@Service
public class JwtService {
	private static final String SECRET = "this-is-a-very-long-secret-key-that-is-at-least-32-chars";
	
	public String generateToken(int userId, String role) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("roles", role);
		long oneHour = 1000L * 60 * 60;
		// JWT token expires in 1 hour
		return Jwts.builder()
				.claims()
				.add(claims)
				.subject(String.valueOf(userId))
				.issuedAt(new Date(System.currentTimeMillis()))
				.expiration(new Date(System.currentTimeMillis() + oneHour))
				.and()
				.signWith(getKey())
				.compact();
	}

	private SecretKey getKey() {
		return Keys.hmacShaKeyFor(SECRET.getBytes());
	}
	
	public int extractUserId(String token) {
		String subject = extractClaim(token, Claims::getSubject);
		try {
			return Integer.parseInt(subject);
		} catch (NumberFormatException ex) {
			throw new MalformedJwtException("JWT subject must be a numeric user id.");
		}
	}
	
	public String extractRole(String token) {
	    return (String) extractAllClaims(token).get("roles");
	}
	
	private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
		final Claims claims = extractAllClaims(token);
		return claimResolver.apply(claims);
	}
	
	// fetches all the claims
	private Claims extractAllClaims(String token) {
		return Jwts.parser()
				.verifyWith(getKey())
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}
	
	public boolean validateToken(String token, UserDetails userDetails) {
		if (!(userDetails instanceof AccountUserDetails accountUserDetails)) {
			return false;
		}

		final int userId = extractUserId(token);
		return (userId == accountUserDetails.getUserId() && !isTokenExpired(token));
	}
	
	private boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}
	
	private Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}
}
