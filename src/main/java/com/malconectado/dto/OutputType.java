package com.malconectado.dto;

import org.eclipse.birt.report.engine.api.IRenderOption;
/**
 * Birt output types
 * @author jcastillo
 *
 */
public enum OutputType {
	/** Birt HTML output */
	HTML(IRenderOption.OUTPUT_FORMAT_HTML),
	/** Birt PDF output */
    PDF(IRenderOption.OUTPUT_FORMAT_PDF),
    /** Birt Excel output */
    XLSX("xlsx"),
    /** Birt Default definied in {@link com.alphas.projects.service.BirtReportService#DEFAULT_FORMAT} output */
    DEFAULT("default"),
    /** Used for invalid format definition */
    INVALID("invalid");

	/** Name of format */
    private String name;
    
    private OutputType(String name) {
        this.name = name;
    }

    /** Get enum definition form string value  or invalid if not exists*/
    public static OutputType from(String text) {
        for (OutputType output : values()) {
            if(output.name.equalsIgnoreCase(text)) return output;
        }
        return INVALID;
    }
}
