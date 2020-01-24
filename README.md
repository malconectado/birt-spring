#Birt service for spring

This library allow use BIRT in your based spring application.

##Table of Content

- [How use](#how-use)

### How use

Add dependency

```
	<dependency>
		<groupId>com.malconectado</groupId>
		<artifactId>birt-spring</artifactId>
		<version>1.0.0</version>
	</dependency> 
```

Register bean example in configuration

```java
    @Bean
    public BirtReportService getReportService() {
    	return new BirtReportService();
    }
```

Controller example based in [this tutorial](https://www.baeldung.com/birt-reports-spring-boot)

```java

...


/**
 * Birt engine controller based in 
 * 
 * @author jcastillo
 *
 */
@Controller
@RequestMapping("/report")
public class BirtReportController {

    @Autowired
    private BirtReportService reportService;

    /**
     * Get metadata of a report
     * @param name
     * @return
     */
    @GetMapping(produces = "application/json", value = "/metadata/{name}")
    @ResponseBody
    public Report reportMetadata( @PathVariable("name") String name) {
        return reportService.getReportMetadata(name);
    }

    /**
     * Generate report
     * @param response
     * @param request
     * @param name
     * @param output
     * @param params
     */
    @PostMapping("/run/{name}")
    @ResponseBody
    public void generateReport(HttpServletResponse response, HttpServletRequest request,
                                   @PathVariable("name") String name, 
                                   @RequestParam(name = "output", required=false) String output,
                                   @RequestBody(required=false) Map<String, Object> params) {
    	
    	
    	OutputType format = OutputType.from(output);    	
        
        reportService.generateReport(name, format,params, response, request);
    }
}
```
