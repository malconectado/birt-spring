package com.malconectado.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Report DTO class for report´s definitions.
 */
public class Report {
	
    private String title;
    private String name;
    private List<Parameter> parameters = new ArrayList<Parameter>();

    public Report(String title, String name) {
        this.title = title;
        this.name = name;
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

	public List<Parameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<Parameter> parameters) {
		this.parameters = parameters;
	}
}