package com.example.plante.Base_module;

public class ModelFertilizerShop {
	
	String id, name, contact, city, image;
	
	public ModelFertilizerShop(String id, String name, String contact, String city, String image) {
		this.id = id;
		this.name = name;
		this.contact = contact;
		this.city = city;
		this.image = image;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getContact() {
		return contact;
	}
	
	public void setContact(String contact) {
		this.contact = contact;
	}
	
	public String getCity() {
		return city;
	}
	
	public void setCity(String city) {
		this.city = city;
	}
	
	public String getImage() {
		return image;
	}
	
	public void setImage(String image) {
		this.image = image;
	}
}
