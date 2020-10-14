package com.example.plante.Base_module;

public class ModelAddTreatment {
	
	String treatmentname, description;
	
	public ModelAddTreatment(String treatmentname, String description) {
		this.treatmentname = treatmentname;
		this.description = description;
	}
	
	public String getTreatmentname() {
		return treatmentname;
	}
	
	public void setTreatmentname(String treatmentname) {
		this.treatmentname = treatmentname;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
}
