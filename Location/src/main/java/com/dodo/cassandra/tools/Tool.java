package com.dodo.cassandra.tools;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dodo.cassandra.persistence.LocationPersistence;

public class Tool {
	private static final Logger logger = LoggerFactory.getLogger(LocationPersistence.class);
	
	public static Properties prop(){
		Properties prop = new Properties();
    	InputStream input = null;

    	try {
    		String propertyFile = "";
    		propertyFile = (System.getProperty("property.file")!=null)? System.getProperty("property.file"):"";
    		logger.info("propertyFile:"+propertyFile);
    		
    		//input = CassandraConnector.class.getClassLoader().getResourceAsStream(path+filename);
    		input = new FileInputStream(propertyFile);
    		prop.load(input);

    		return prop;
    		
    	} catch (IOException ex) {
    		ex.printStackTrace();
        } finally{
        	if(input!=null){
        		try {
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        	}
        }
    	return prop;
	}
}
