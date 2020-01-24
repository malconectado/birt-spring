package com.malconectado.dto;

public enum ParameterType {
    INTEGER, STRING;
    @Override
    public String toString() {
    	return super.toString().charAt(0) + super.toString().substring(1).toLowerCase();
    }
}