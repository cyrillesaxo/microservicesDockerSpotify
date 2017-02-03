package com.dodo.locationService.rest;

import static org.apache.camel.model.rest.RestParamType.path;

import org.apache.camel.Exchange;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.spring.SpringRouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.dodo.cassandra.persistence.LocationPersistence;
import com.dodo.cassandra.tools.GeoApi;
import com.dodo.error.ErrorService;
import com.dodo.model.Geocoding;
import com.dodo.model.Locations;
import com.fasterxml.jackson.core.JsonParseException;


/**
 * Define REST services using the Camel REST DSL
 */
@Configuration
public class LocationServicesRouteBuilder extends SpringRouteBuilder {
	
	private static final Logger logger = LoggerFactory.getLogger(LocationServicesRouteBuilder.class);
	
	private Environment env;
	
	private GeoApi geoApi;

	@Autowired
	void setGeoApi(GeoApi geoApi){
		this.geoApi =geoApi;
	}
	
	@Autowired
	void setEnvironment(Environment e){
		logger.info(" LocationServicesRouteBuilder  #### env:"+e.getProperty("broker.url"));
		env = e;
	}
	
	@Override
    public void configure() throws Exception {

		//env = Tool.prop();
		logger.info("swagger.host:"+env.getProperty("swagger.host"));
		onException(JsonParseException.class)
	    .handled(true)
	    .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
	    .setHeader(Exchange.CONTENT_TYPE, constant("text/plain"))
	    .setBody().constant("Invalid json data");
		
        // configure we want to use servlet as the component for the rest DSL
        // and we enable json binding mode //netty4-http
        restConfiguration().component("servlet").bindingMode(RestBindingMode.json)
        	.host(env.getProperty("swagger.host"))
            .dataFormatProperty("prettyPrint", "true")
            .contextPath("Location")
            .port(env.getProperty("swagger.port"))
            .apiContextPath("/api-doc")
            .apiProperty("api.title", env.getProperty("swagger.title"))
            .apiProperty("api.description", env.getProperty("swagger.description"))
            .apiProperty("api.termsOfServiceUrl", env.getProperty("swagger.termsOfServiceUrl"))
            .apiProperty("api.license", env.getProperty("swagger.license"))
            .apiProperty("api.licenseUrl", env.getProperty("swagger.licenseUrl"))
            .apiProperty("api.version", env.getProperty("swagger.version"))
             .apiProperty("cors", env.getProperty("swagger.cors"));

        
        rest("/api").description("Location Services")
            .consumes("application/json").produces("application/json")

		.get("/ping")
		.outType(Geocoding.class)
		.to("direct:ping")
		
		
		//https://host:port/locations/bds/{bdsid}/driver/{driverid}?from=2016-09-18T00:00:00Z&to=2016-09-18T14:39:00Z
		.get("/location/latitude/{latitude}/longitude/{longitude}")
		.param().name("latitude").type(path).description("The  latitude ").dataType("string").endParam()
		.param().name("longitude").type(path).description("The  longitude ").dataType("string").endParam()
		.outType(Geocoding.class)
		.to("direct:location");		
		

        from("direct:ping")
        .id("ping")
        .routeId("ping")
        .process(exchange -> {
        	String address = geoApi.getAddress(33.969601,-84.100033);
        	Geocoding geocoding = new Geocoding();
        	geocoding.setLatitude("33.969601");
        	geocoding.setLongitude("-84.100033");
        	geocoding.setAddress(address);
			exchange.getIn().setBody(geocoding);
		});
		
  
		from("direct:location")
		.id("location")
		.routeId("location")
    	.choice()
    	.when().simple("${bean:validator?method=isLatitudeAndLongitudeRight}")
		.process(exchange -> {
			logger.info(" ############  EXCHANGE:"+exchange);
			String longitude = (String)exchange.getIn().getHeader("longitude",String.class);
			String latitude = (String)exchange.getIn().getHeader("latitude",String.class);
			String address = geoApi.getAddress(new Double(latitude),new Double(longitude));
			Geocoding geocoding = new Geocoding();
			geocoding.setLatitude(latitude);
			geocoding.setLongitude(longitude);
        	geocoding.setAddress(address);
			exchange.getIn().setBody(geocoding);
		}).otherwise()
    	.bean(new ErrorService(), "locationError");	 
          
    }
	


}
