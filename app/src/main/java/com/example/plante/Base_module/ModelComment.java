package com.example.plante.Base_module;

public class ModelComment {
	
	String cId, comment, timestamp, uId, uEmail, uDp, uName;
	
	public ModelComment() {
	
	}
	
	public ModelComment(String cId, String comment, String timestamp, String uId, String uEmail, String uDp, String uName) {
		this.cId = cId;
		this.comment = comment;
		this.timestamp = timestamp;
		this.uId = uId;
		this.uEmail = uEmail;
		this.uDp = uDp;
		this.uName = uName;
	}
	
	public String getcId() {
		return cId;
	}
	
	public void setcId(String cId) {
		this.cId = cId;
	}
	
	public String getComment() {
		return comment;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public String getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	
	public String getuId() {
		return uId;
	}
	
	public void setuId(String uId) {
		this.uId = uId;
	}
	
	public String getuEmail() {
		return uEmail;
	}
	
	public void setuEmail(String uEmail) {
		this.uEmail = uEmail;
	}
	
	public String getuDp() {
		return uDp;
	}
	
	public void setuDp(String uDp) {
		this.uDp = uDp;
	}
	
	public String getuName() {
		return uName;
	}
	
	public void setuName(String uName) {
		this.uName = uName;
	}
}
