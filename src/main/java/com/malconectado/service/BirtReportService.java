package com.malconectado.service;

import com.malconectado.dto.OutputType;
import com.malconectado.dto.Parameter;
import com.malconectado.dto.ParameterType;
import com.malconectado.dto.Report;

import org.apache.log4j.Logger;
import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.*;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.*;


/**
 * Service for manage birt engine
 * @author jcastillo
 *
 */
@Service
public class BirtReportService implements ApplicationContextAware, DisposableBean {
	
	private static final Logger log = Logger.getLogger(BirtReportService.class);
	
    @Value("${birt.reports.path}")
    private String reportsPath;
    
    @Value("${birt.images.path}")
    private String imagesPath;

    private HTMLServerImageHandler htmlImageHandler = new HTMLServerImageHandler();    
    
    @Autowired
    private ResourceLoader resourceLoader;
    //@Autowired
    //private ServletContext servletContext;

    private IReportEngine birtEngine;
    
    private ApplicationContext context;
    
    private String imageFolder;

    private String reportFolder;
    
    private Map<String, IReportRunnable> reports;
    
    public static OutputType DEFAULT_FORMAT = OutputType.HTML;

    @SuppressWarnings("unchecked")
    @PostConstruct
    protected void initialize() throws BirtException {
        EngineConfig config = new EngineConfig();
        config.getAppContext().put("spring", context);
        Platform.startup(config);
        IReportEngineFactory factory = (IReportEngineFactory) Platform
          .createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
        birtEngine = factory.createReportEngine(config);
        
        Resource resource = resourceLoader.getResource("classpath:");
        
        File folder;
		try {
			folder = resource.getFile();
			imageFolder = folder.getAbsolutePath() + File.separatorChar + imagesPath;
			reportFolder = folder.getAbsolutePath() + File.separatorChar + reportsPath;
		} catch (IOException e) {
			return;
		}
		
		//creates a cache of 20 values only
		reports = new LinkedHashMap<String, IReportRunnable>() {
			/***/
			private static final long serialVersionUID = 7698703702107208597L;
			@Override
			protected boolean removeEldestEntry(Map.Entry<String, IReportRunnable> eldest) {
		        return size() > 20;
		     }
		};
		
		
    }

    @Override
    public void setApplicationContext(ApplicationContext context) {
        this.context = context;
    }


    /**
     * Translate parameter type from birt
     * @param param
     * @return
     */
    private ParameterType getParameterType(IParameterDefn param) {
        if (IParameterDefn.TYPE_INTEGER == param.getDataType()) {
            return ParameterType.INTEGER;
        }
        return ParameterType.STRING;
    }
    
    /**
     * return instance of org.eclipse.birt.report.engine.api.IReportRunnable from file report
     * @param name
     * @return
     * @throws EngineException
     */
    private IReportRunnable getReportRunnable(String name) throws EngineException {
    	if (reports.containsKey(name)) {
    		return reports.get(name);
    	}
    	IReportRunnable r = null;
    	File reportFile = new File(reportFolder + File.separator + name.replace(".rptdesign", "") + ".rptdesign");
    	if (reportFile.exists()) {
    		r =  birtEngine.openReportDesign(reportFile.getAbsolutePath());
    		reports.put(name,r ); 
    	}    	
    	return r;
    }
    
    
    /**
     * return metadata of a report from file
     * @param name
     * @return
     */
    public Report getReportMetadata(String name){
    	    	
    	IReportRunnable reportRun;
		try {
			reportRun = getReportRunnable(name);
		} catch (EngineException e) {
			return null;
		}
		
        IGetParameterDefinitionTask task = birtEngine.createGetParameterDefinitionTask(reportRun);
        Report reportItem = new Report(
        		reportRun.getDesignHandle().getProperty("title")!=null?
        				reportRun.getDesignHandle().getProperty("title").toString(): 
        					name, 
        					name);
        for (Object h : task.getParameterDefns(false)) {
            IParameterDefn def = (IParameterDefn) h;
            
            reportItem.getParameters()
              .add(new Parameter(def.getPromptText(), def.getName(), getParameterType(def)));
        }
    	
    	
    	return reportItem;
    }
    

    /**
     * Generate report and put in response
     * @param reportName
     * @param output
     * @param params
     * @param response
     * @param request
     */
    public void generateReport(String reportName, OutputType output,Map<String, Object> params, HttpServletResponse response, HttpServletRequest request) {   

        try {
			generateReport(getReportRunnable(reportName),output,params,null, response, request);
		} catch (EngineException e) {
			throw new IllegalArgumentException("Output type not recognized:" + output);
		}
    }

    /**
     * Generate report and save on disk
     * @param reportName
     * @param output
     * @param params
     * @param fileName
     */
    public void generateReport(String reportName, OutputType output,Map<String, Object> params,String fileName) {   

        try {
			generateReport(getReportRunnable(reportName),output,params,fileName,null,null);
		} catch (EngineException e) {
			throw new IllegalArgumentException("Output type not recognized:" + output);
		}
    }
    
    
    @SuppressWarnings("unchecked")
	private void generateReport(IReportRunnable report, OutputType output,Map<String, Object> params,String fileName, HttpServletResponse response, HttpServletRequest request) throws EngineException {
    	IRunAndRenderTask runAndRenderTask = birtEngine.createRunAndRenderTask(report);
        IRenderOption options = new RenderOption();        
        RenderOption renderOption;
        String contentType = null;
        log.debug("Run report: " + report.getReportName());
        if (params !=null)        {
        	for (String key : params.keySet()) {
        		log.debug(key.toString() + ": " + params.get(key)+ " (" + params.get(key).getClass().getCanonicalName() + ")");
        	}
        }
        
        if (output == null || output == OutputType.DEFAULT) {
        	output = DEFAULT_FORMAT;
        }
        
        
        switch (output) {
        
        	case HTML:
                contentType = birtEngine.getMIMEType("html");
        		HTMLRenderOption htmlOptions = new HTMLRenderOption(options);
                htmlOptions.setOutputFormat("html");
                htmlOptions.setBaseImageURL("/" +  imagesPath);
                htmlOptions.setImageDirectory(imageFolder);
                htmlOptions.setImageHandler(htmlImageHandler);
                if (request!=null) {
                	runAndRenderTask.getAppContext().put(EngineConstants.APPCONTEXT_BIRT_VIEWER_HTTPSERVET_REQUEST, request);
                }              
                htmlOptions.setHtmlPagination(true);
                renderOption = htmlOptions;
                break;
        	case PDF:
                contentType = birtEngine.getMIMEType("pdf");
        		PDFRenderOption pdfRenderOption = new PDFRenderOption(options);
                pdfRenderOption.setOutputFormat("pdf");
                if (request!=null) {
                	runAndRenderTask.getAppContext().put(EngineConstants.APPCONTEXT_PDF_RENDER_CONTEXT, request);
                }
                renderOption = pdfRenderOption;
        		break;    
        	case XLSX:
        		contentType = birtEngine.getMIMEType("xlsx");
        		
        		renderOption = new EXCELRenderOption();
        		renderOption.setOutputFormat("xlsx");
        		break;
        	default:
        		throw new IllegalArgumentException("Output type not recognized: " + output);
        	
        }

        

        
        runAndRenderTask.setRenderOption(renderOption);
        if (params!=null) {
            runAndRenderTask.setParameterValues(params);        	
        }
        try {
        	if (response!=null) {
        		renderOption.setOutputStream(response.getOutputStream());
                response.setContentType(contentType);
        	}
        	
        	//Save file in disk
            if (fileName !=null && !fileName.isEmpty()) {
            	renderOption.setOutputFileName(fileName);
            }
            runAndRenderTask.run();
        } catch (Exception e) {
            throw new EngineException(e.getMessage(), e);
        } finally {
            runAndRenderTask.close();
        }
    	
    }

    /**
     * Stop engine on destroying process
     */
    @Override
    public void destroy() {
        birtEngine.destroy();
        Platform.shutdown();
    }
}