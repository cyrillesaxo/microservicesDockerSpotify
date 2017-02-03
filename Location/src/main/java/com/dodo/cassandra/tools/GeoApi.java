package com.dodo.cassandra.tools;


import java.util.concurrent.TimeUnit;

import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;

@Component
public class GeoApi {
	
	  private static final Logger logger = LoggerFactory.getLogger(GeoApi.class);
	  private GeoApiContext context;
	  private Environment env;
		
		@Autowired
		void setEnvironment(Environment e){
			logger.info("  #### google.apiKey:"+e.getProperty("google.apiKey"));
			env = e;
			
		    GeoApiContext context = new GeoApiContext()
		            .setApiKey(env.getProperty("google.apiKey"));
		    
		    this.context = context
			        .setQueryRateLimit(3)
			        .setConnectTimeout(1, TimeUnit.SECONDS)
			        .setReadTimeout(1, TimeUnit.SECONDS)
			        .setWriteTimeout(1, TimeUnit.SECONDS);
		}
		
	public String getAddress(final double lat, final double lng) throws Exception {
		if( lat == 0 || lng ==0)return "";
		logger.info("GeoApi.getAddress lat:"+lat+" -  lng:"+lng);
	    GeocodingResult[] results = GeocodingApi.newRequest(this.context)
	            .latlng(new LatLng(lat, lng))
	            .await();

	       if(results == null || results.length == 0) return "";
	      logger.info(results[0].formattedAddress);

	      return results[0].formattedAddress;
	}

}
