package com.beacon.backend.model;
import java.util.List;

import jakarta.persistence.*;

@Entity
public class Address {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="AddressID")
	private int addressId;
	
	@OneToMany(mappedBy="address", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	private List<AccountUser> accountUsers;
	
	@Column(name="PrimaryStreet")
	private String primaryStreet;
	
	// optional?
	@Column(name="SecondaryStreet")
	private String secondaryStreet;
	
	@Column(name="City")
	private String city;
	
	@Column(name="State")
	private String state;
	
	@Column(name="ZipCode")
	private String zipCode;
	
	@Column(name="Country")
	private String country;
	
	public Address() {}
	
	public Address(String primaryStreet, String secondaryStreet, String city, String state, String zipCode, String country) {
		this.primaryStreet = primaryStreet;
		this.secondaryStreet = secondaryStreet;
		this.city = city;
		this.state = state;
		this.zipCode = zipCode;
		this.country = country;
	}
	
	// getters
	public int getAddressId() {
		return addressId;
	}
	
	public String getPrimaryStreet() {
		return primaryStreet;
	}
	
	public String getSecondaryStreet() {
		return secondaryStreet;
	}
	
	public String getCity() {
		return city;
	}
	
	public String getState() {
		return state;
	}
	
	public String getZipCode() {
		return zipCode;
	}
	
	public String getCountry() {
		return country;
	}
	
	// setters
	public void setPrimaryStreet(String newPrimaryStreet) {
		this.primaryStreet = newPrimaryStreet;
	}
	
	public void setSecondaryStreet(String newSecondaryStreet) {
		this.secondaryStreet = newSecondaryStreet;
	}
	
	public void setCity(String newCity) {
		this.city = newCity;
	}
	
	public void setState(String newState) {
		this.state = newState;
	}
	
	public void setZipCode(String newZipCode) {
		this.zipCode = newZipCode;
	}
	
	public void setCountry(String newCountry) {
		this.country = newCountry;
	}
}