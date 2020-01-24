package com.malconectado.dto;

public class Parameter {
    private String title;
    private String name;
    private ParameterType type;       
    
    
	public Parameter(String title, String name, ParameterType type) {
		super();
		this.title = title;
		this.name = name;
		this.type = type;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ParameterType getType() {
		return type;
	}
	public void setType(ParameterType type) {
		this.type = type;
	}
    

}