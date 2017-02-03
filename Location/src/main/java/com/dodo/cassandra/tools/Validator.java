package com.dodo.cassandra.tools;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class Validator {
	private static final Logger logger = LoggerFactory.getLogger(Validator.class);
	
	public boolean isLatitudeAndLongitudeRight(Exchange exchange) {
		
		boolean ok = true;
		String latitude = (String)exchange.getIn().getHeader("latitude",String.class);
		String longitude = (String)exchange.getIn().getHeader("longitude",String.class);
		
		if(null == latitude || "".equals(latitude.trim()) || !isNumeric(latitude.trim())) return false;
		if(null == longitude || "".equals(longitude.trim()) || !isNumeric(longitude.trim())) return false;
		
		logger.info(" str --GeoApi.getAddress lat:"+latitude+" -  lng:"+longitude);
		logger.info("numb -- GeoApi.getAddress lat:"+new Double(latitude)+" -  lng:"+new Double(longitude));
		if(validateFormat(latitude.trim()+","+longitude.trim())) return false;
		
		exchange.getIn().setBody(exchange);
		return ok;
	}
	
	//String latlon = "3.169054, 101.714108";
	public boolean validateFormat1(String latlon){
		boolean noMatch = true;
        String regex_coords = "([+-]?\\d+\\.?\\d+)\\s*,\\s*([+-]?\\d+\\.?\\d+)";
        Pattern compiledPattern2 = Pattern.compile(regex_coords, Pattern.CASE_INSENSITIVE);
        Matcher matcher2 = compiledPattern2.matcher(latlon);
        while (matcher2.find()) {
            //logger.info("Do we have validate coordinate: " + matcher2.group());
            noMatch = false;
        }
        return noMatch;
	}
	
	public static boolean isNumeric(String str){
	  return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
	}
	
	  public boolean validateFormat( String line ) {
		  boolean noMatch = true;
	      // String to be scanned to find the pattern.
	      //String line = "This order was placed for QT3000! OK?";
	      String pattern = "([+-]?\\d+\\.?\\d+)\\s*,\\s*([+-]?\\d+\\.?\\d+)";

	      // Create a Pattern object
	      Pattern r = Pattern.compile(pattern);

	      // Now create matcher object.
	      Matcher m = r.matcher(line);
	      if (m.find( )) {
	    	  logger.info("Found value: " + m.group(0) );
	    	  logger.info("Found value: " + m.group(1) );
	      }else {
	         logger.info("NO MATCH");
	         noMatch = true;
	      }
	      return false; 
	   }
	  

}
