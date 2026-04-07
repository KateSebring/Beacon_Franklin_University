package com.beacon.backend.model;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.Period;

@Entity
public class AccountUser {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int userId;
	private String username;
	private String passwordHash;
	private String firstName;
	private String lastName;
	private LocalDate dob;
	private String email;
	private String role;
	
	@ManyToOne
	@JoinColumn(name="AddressID")
	private Address address;
	
	public AccountUser() {}
	
	// constructor
	public AccountUser(String username, String passwordHash, String firstName, String lastName, LocalDate dob, String email) {
		this.username = username;
		this.passwordHash = passwordHash;
		this.firstName = firstName;
		this.lastName = lastName;
		this.dob = dob;
		this.email = email;
	}
	
	// getters
	public int getId() {
		return userId;
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getPasswordHash() {
		return passwordHash;
	}
	
	public String getFirstName() {
		return firstName;
	}
	
	public String getLastName() {
		return lastName;
	}
	
	public LocalDate getDOB() {
		return dob;
	}
	
	public String getEmail() {
		return email;
	}
	
	public Period getAge() {
		return Period.between(dob, LocalDate.now());
	}
	
	public String getRole() {
		return role;
	}
	
	// setters
	public void setUsername(String newUsername) {
		this.username = newUsername;
	}
	
	public void setPasswordHash(String newPasswordHash) {
		this.passwordHash = newPasswordHash;
	}
	
	public void setFirstName(String newFirstName) {
		this.firstName = newFirstName;
	}
	
	public void setLastName(String newLastName) {
		this.lastName = newLastName;
	}
	
	public void setDOB(LocalDate newDOB) {
		this.dob = newDOB;
	}
	
	public void setEmail(String newEmail) {
		this.email = newEmail;
	}
	
	public void setRole(String role) {
		this.role = role;
	}
}