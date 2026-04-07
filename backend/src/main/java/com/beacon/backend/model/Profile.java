package com.beacon.backend.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
public class Profile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int profileId;

	@Column(nullable = false, unique = true, length = 36)
	private String uuid;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "owner_user_id", nullable = false)
	private AccountUser owner;

	@Column(nullable = false)
	private String firstName;

	@Column(nullable = false)
	private String lastName;

	@Column(nullable = false)
	private String emergencyFirstName;

	@Column(nullable = false)
	private String emergencyLastName;

	@Column(nullable = false)
	private String emergencyEmail;


	public Profile() {}

	public Profile(AccountUser owner, String firstName, String lastName, String emergencyFirstName,
				   String emergencyLastName, String emergencyEmail) {
		this.uuid = UUID.randomUUID().toString();
		this.owner = owner;
		this.firstName = firstName;
		this.lastName = lastName;
		this.emergencyFirstName = emergencyFirstName;
		this.emergencyLastName = emergencyLastName;
		this.emergencyEmail = emergencyEmail;
	}

	// Forces a UUID if JPA uses the empty constructor for some reason
	@PrePersist
	private void alwaysHaveUuid() {
		if (uuid == null || uuid.isBlank()) {
			uuid = UUID.randomUUID().toString();
		}
	}

	// Getters
	public int getId() {
		return profileId;
	}

	public String getUuid() {
		return uuid;
	}

	public AccountUser getOwner() {
		return owner;
	}

	public String getFirstName() { return firstName; }

	public String getLastName() { return lastName; }

	public String getEmergencyFirstName() { return emergencyFirstName; }

	public String getEmergencyLastName() { return emergencyLastName; }

	public String getEmergencyEmail() { return emergencyEmail; }

	// Setters
	public void setOwner(AccountUser owner) {
		this.owner = owner;
	}

	public void setFirstName(String firstName) { this.firstName = firstName; }

	public void setLastName(String lastName) { this.lastName = lastName; }

	public void setEmergencyFirstName(String emergencyFirstName) { this.emergencyFirstName = emergencyFirstName; }

	public void setEmergencyLastName(String emergencyLastName) { this.emergencyLastName = emergencyLastName; }

	public void setEmergencyEmail(String emergencyEmail) { this.emergencyEmail = emergencyEmail; }
}
