package com.example.plante;

public class Post {
	String pid, ptitle, pcontent, pLikes, pComments, pImage, ptime, uid, uemail, uprofile, uname;
	
	public Post() {
	
	}
	
	public Post(String pid, String ptitle, String pcontent, String pLikes, String pComments, String pImage, String ptime, String uid, String uemail, String uprofile, String uname) {
		this.pid = pid;
		this.ptitle = ptitle;
		this.pcontent = pcontent;
		this.pLikes = pLikes;
		this.pComments = pComments;
		this.pImage = pImage;
		this.ptime = ptime;
		this.uid = uid;
		this.uemail = uemail;
		this.uprofile = uprofile;
		this.uname = uname;
	}
	
	public String getPid() {
		return pid;
	}
	
	public void setPid(String pid) {
		this.pid = pid;
	}
	
	public String getPtitle() {
		return ptitle;
	}
	
	public void setPtitle(String ptitle) {
		this.ptitle = ptitle;
	}
	
	public String getPcontent() {
		return pcontent;
	}
	
	public void setPcontent(String pcontent) {
		this.pcontent = pcontent;
	}
	
	public String getpLikes() {
		return pLikes;
	}
	
	public void setpLikes(String pLikes) {
		this.pLikes = pLikes;
	}
	
	public String getpComments() {
		return pComments;
	}
	
	public void setpComments(String pComments) {
		this.pComments = pComments;
	}
	
	public String getpImage() {
		return pImage;
	}
	
	public void setpImage(String pImage) {
		this.pImage = pImage;
	}
	
	public String getPtime() {
		return ptime;
	}
	
	public void setPtime(String ptime) {
		this.ptime = ptime;
	}
	
	public String getUid() {
		return uid;
	}
	
	public void setUid(String uid) {
		this.uid = uid;
	}
	
	public String getUemail() {
		return uemail;
	}
	
	public void setUemail(String uemail) {
		this.uemail = uemail;
	}
	
	public String getUprofile() {
		return uprofile;
	}
	
	public void setUprofile(String userImage) {
		this.uprofile = userImage;
	}
	
	public String getUname() {
		return uname;
	}
	
	public void setUname(String uname) {
		this.uname = uname;
	}
}