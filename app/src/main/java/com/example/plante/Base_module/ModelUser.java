package com.example.plante.Base_module;

public class ModelUser {
	
	String name, position, search, image, uid, onlineStatus, typingTo;
	
	public ModelUser() {
	}
	
	public ModelUser(String name, String position, String search, String image, String uid, String onlineStatus, String typingTo) {
		this.name = name;
		this.position = position;
		this.search = search;
		this.image = image;
		this.uid = uid;
		this.onlineStatus = onlineStatus;
		this.typingTo = typingTo;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getPosition() {
		return position;
	}
	
	public void setPosition(String position) {
		this.position = position;
	}
	
	public String getSearch() {
		return search;
	}
	
	public void setSearch(String search) {
		this.search = search;
	}
	
	public String getImage() {
		return image;
	}
	
	public void setImage(String image) {
		this.image = image;
	}
	
	public String getUid() {
		return uid;
	}
	
	public void setUid(String uid) {
		this.uid = uid;
	}
	
	public String getOnlineStatus() {
		return onlineStatus;
	}
	
	public void setOnlineStatus(String onlineStatus) {
		this.onlineStatus = onlineStatus;
	}
	
	public String getTypingTo() {
		return typingTo;
	}
	
	public void setTypingTo(String typingTo) {
		this.typingTo = typingTo;
	}
}
