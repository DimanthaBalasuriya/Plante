package com.example.plante.Base_module;

public class ModelShop {
	
	String id, name, city, number, image;
	
	public ModelShop(String id, String name, String city, String number, String image) {
		this.id = id;
		this.name = name;
		this.city = city;
		this.number = number;
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
	
	public String getCity() {
		return city;
	}
	
	public void setCity(String city) {
		this.city = city;
	}
	
	public String getNumber() {
		return number;
	}
	
	public void setNumber(String number) {
		this.number = number;
	}
	
	public String getImage() {
		return image;
	}
	
	public void setImage(String image) {
		this.image = image;
	}
}
