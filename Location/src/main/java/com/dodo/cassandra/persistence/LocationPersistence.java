package com.dodo.cassandra.persistence;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import org.apache.camel.Exchange;
import org.joda.time.format.DateTimeFormat;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.dodo.cassandra.repository.LocationRepository;
import com.dodo.cassandra.tools.CassandraConnector;
import com.dodo.model.Location;
import com.dodo.model.Locations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//@Component
public class LocationPersistence extends CassandraConnector implements LocationRepository{
	private static final Logger logger = LoggerFactory.getLogger(LocationPersistence.class);
			
	public LocationPersistence(){
		this.connect();
	}
	public void insertLocation(Exchange ex)throws Exception {
		if(!(ex.getIn().getBody(Location.class) instanceof Location))throw new Exception(" The parameter provided in the method InsertLocation is not a location !");
		Location location =  (Location)ex.getIn().getBody(Location.class);
		if(location ==null)throw new Exception(" Location parameter is the method InsertLocation is null!");
		logger.info("####  Location:"+location);
		
			PreparedStatement prepared = this.getSession().prepare(
					"insert into LocationServices.Location(bdsid, driverid, lat, lon, loctimestamp ) VALUES (?, ?, ?, ?, ?) ");
			
			BoundStatement bound = prepared.bind(location.getBdsid(), location.getDriverid(),  location.getLat(), location.getLon() );
			bound.setTimestamp(4, getDate(location.getTimestamp()));
			this.getSession().execute(bound);
	}

	
	 public static Date getDate(String dateInString) {
		 												       
	        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
	       // String dateInString = "2014-10-05T15:23:01Z";

	        try {

	            Date date = formatter.parse(dateInString.replaceAll("Z$", "+0000"));

	            logger.info("time zone : " + TimeZone.getDefault().getID());
	            logger.info(formatter.format(date));
	            return date;
	        } catch (ParseException e) {
	            e.printStackTrace();
	        }
	        return null;
	    }
	 
	public List<Location> selectLocation(Exchange ex)throws Exception{
		String test = ex.getIn().getBody(String.class);
		logger.info("exchange:"+test);
		
		if(!(ex.getIn().getBody(Locations.class) instanceof Locations))throw new Exception(" The parameter provided in the method selectLocation is not a list of locations !");
		
		Locations inlocations =(Locations)ex.getIn().getBody(Locations.class);
		return queryLocations(inlocations);
	}


	private List<Location> queryLocations(Locations inlocations) throws Exception {
		Location location =  inlocations.getLocations().get(0);
		
		if(location ==null)throw new Exception(" Location parameter is the method selectLocation is null!");
		logger.info("####  Location:"+location);
		
		List<Location> locations = new LinkedList<Location>();
		Location rlocation=null;
		
		String bdsid="";
		String driverid="";
		String lat="";
		String lon="";
		Date date=null;
		String loctimestamp="";
		ResultSet resultSet = this.getSession().execute(buildQuery(location));
		logger.info(" RESULT FROM CASSANDRA ");
		
		try{
		for (Row row : (ResultSet) resultSet) {
			
			bdsid=row.getString("bdsid");
			driverid=row.getString("driverid");
			lat=row.getString("lat");
			lon=row.getString("lon");
			date= row.getTimestamp(1);
			loctimestamp =new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(date);
			
			logger.info("bdsid: " + row.getString("bdsid") 
			+ " - driverid: " + driverid 
			+ " - lat: " + lat
			+ " - lon: " + lon 
			+ " - loctimestamp: "  + loctimestamp  ); 
			
			rlocation = new Location(bdsid, driverid, lat, lon, loctimestamp); 
			locations.add(rlocation);
		}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return locations;
	}
	
	
	private List<Location> queryLocationsByBdsid(Locations inlocations, String from, String to) throws Exception {
		Location location =  inlocations.getLocations().get(0);
		
		if(location ==null)throw new Exception(" Location parameter is the method selectLocation is null!");
		logger.info("####  Location:"+location);
		
		if( null == location.getBdsid() || "".equals(location.getBdsid().trim())) throw new Exception(" Location parameter BDSID is the method queryLocationsByBdsid is null!");
		
		List<Location> locations = new LinkedList<Location>();
		Location rlocation=null;
		
		String bdsid="";
		String driverid="";
		String lat="";
		String lon="";
		Date date = null;
		String loctimestamp="";
		
		String query = null;
		PreparedStatement pStatement = null;
		BoundStatement bStatement = null;
		
		if(null == from && null == to){
			query = "select * from LocationServices.location where bdsid=?";  
			pStatement = this.getSession().prepare(query);
			   bStatement = new BoundStatement(pStatement);
			    bStatement.bind(location.getBdsid().trim());  
		}else if(null != from && null == to){
			query = "select * from LocationServices.location where bdsid=? and  loctimestamp >=?";  
			pStatement = this.getSession().prepare(query);
			    bStatement = new BoundStatement(pStatement);
			    bStatement.bind(location.getBdsid().trim()); 
			   // bStatement.bind(2, getDate(from));
			    bStatement.setTimestamp(1, getDate(from));
		}else if(null == from && null != to){
			query = "select * from LocationServices.location where bdsid=? and  loctimestamp >=?";  
			pStatement = this.getSession().prepare(query);
			    bStatement = new BoundStatement(pStatement);
			    bStatement.bind(location.getBdsid().trim()); 
			    bStatement.setTimestamp(1, getDate(to));
		}else {
			query = "select * from LocationServices.location where bdsid=? and  loctimestamp >=? and  loctimestamp <=? ";  
			pStatement = this.getSession().prepare(query);
			    bStatement = new BoundStatement(pStatement);
			    bStatement.bind(location.getBdsid().trim()); 
			    bStatement.setTimestamp(1, getDate(from)); 
			    bStatement.setTimestamp(2, getDate(to));
		}
		    
		    ResultSet resultSet = this.getSession().execute(bStatement);
		
		logger.info(" RESULT FROM CASSANDRA ");
		
		try{
		for (Row row : (ResultSet) resultSet) {
			
			bdsid=row.getString("bdsid");
			driverid=row.getString("driverid");
			lat=row.getString("lat");
			lon=row.getString("lon");
			date= row.getTimestamp(1);
			loctimestamp =new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(date);
			
			logger.info("bdsid: " + row.getString("bdsid") 
			+ " - driverid: " + driverid 
			+ " - lat: " + lat
			+ " - lon: " + lon 
			+ " - loctimestamp: "  + loctimestamp  ); 
			
			rlocation = new Location(bdsid, driverid, lat, lon, loctimestamp); 
			locations.add(rlocation);
		}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return locations;
	}


	private List<Location> queryLocationsByBdsidAndDriverid(Locations inlocations, String from, String to) throws Exception {
		Location location =  inlocations.getLocations().get(0);
		
		if(location ==null)throw new Exception(" Location parameter is the method selectLocation is null!");
		logger.info("####  Location:"+location);
		
		if( null == location.getBdsid() || "".equals(location.getBdsid().trim())) throw new Exception(" Location parameter Bdsid is the method queryLocationsByBdsidAndDriverid is null!");

		if( null == location.getDriverid() || "".equals(location.getDriverid().trim())) throw new Exception(" Location parameter Driverid is the method queryLocationsByBdsidAndDriverid is null!");

		List<Location> locations = new LinkedList<Location>();
		Location rlocation=null;
		
		String bdsid="";
		String driverid="";
		String lat="";
		String lon="";
		Date date = null;
		String loctimestamp="";
		
		String query = null;
		PreparedStatement pStatement = null;
		BoundStatement bStatement = null;
		
		if(null == from && null == to){
			query = "select * from LocationServices.location where bdsid=? and driverid=? ";  
			pStatement = this.getSession().prepare(query);
			   bStatement = new BoundStatement(pStatement);
			   bStatement.bind(location.getBdsid().trim());  
			   bStatement.bind(location.getDriverid().trim()); 
		}else if(null != from && null == to){
			query = "select * from LocationServices.location where bdsid=? and driverid=?  and  loctimestamp >=?";  
			pStatement = this.getSession().prepare(query);
			    bStatement = new BoundStatement(pStatement);
			    bStatement.bind(location.getBdsid().trim()); 
			    bStatement.bind(location.getDriverid().trim());
			    bStatement.setTimestamp(2, getDate(from));
		}else if(null == from && null != to){
			query = "select * from LocationServices.location where bdsid=? and driverid=?  and  loctimestamp >=?";  
			pStatement = this.getSession().prepare(query);
			    bStatement = new BoundStatement(pStatement);
			    bStatement.bind(location.getBdsid().trim()); 
			    bStatement.bind(location.getDriverid().trim());
			    bStatement.setTimestamp(2, getDate(to));
		}else {
			query = "select * from LocationServices.location where bdsid=? and driverid=? and  loctimestamp >=? and  loctimestamp <=? ";  
			pStatement = this.getSession().prepare(query);
			    bStatement = new BoundStatement(pStatement);
			    bStatement.bind(location.getBdsid().trim()); 
			    bStatement.bind(location.getDriverid().trim());
			    bStatement.setTimestamp(2, getDate(from)); 
			    bStatement.setTimestamp(3, getDate(to));
		}
		    
		    ResultSet resultSet = this.getSession().execute(bStatement);
		
		logger.info(" RESULT FROM CASSANDRA ");
		
		try{
		for (Row row : (ResultSet) resultSet) {
			
			bdsid=row.getString("bdsid");
			driverid=row.getString("driverid");
			lat=row.getString("lat");
			lon=row.getString("lon");
			date= row.getTimestamp(1);
			loctimestamp =new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(date);
			
			logger.info("bdsid: " + row.getString("bdsid") 
			+ " - driverid: " + driverid 
			+ " - lat: " + lat
			+ " - lon: " + lon 
			+ " - loctimestamp: "  + loctimestamp  ); 
			
			rlocation = new Location(bdsid, driverid, lat, lon, loctimestamp); 
			locations.add(rlocation);
		}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return locations;
	}
	
	
	public String space(String str){
		return " "+str+" ";
	}
	
	public Statement buildQuery(Location location){
		String Query ="select bdsid, driverid, lat, lon, loctimestamp from location";
		
		String bdsid = "";
		String driverid = "";
		String lat = "";
		String lon = "";
		String loctimestamp = "";
		
		
		String lbdsid = "";
		String ldriverid = "";
		String llat = "";
		String llon = "";
		String lloctimestamp = "";		
		String where ="";
		boolean ifwhere =false;
		
	
		//select
		//Builder  query= QueryBuilder.select();
		
		Select xselect= QueryBuilder.select().all().from("LocationServices","Location").allowFiltering();
		Select.Where xwhere = null;

		if( (null != location.getBdsid() && !"".equals(location.getBdsid().trim()))){
				bdsid  = " bdsid ";
				lbdsid  = " bdsid = '"+location.getBdsid()+"'";
				ifwhere = true;
				xwhere =xselect.where(QueryBuilder.eq(bdsid,location.getBdsid()));
		}if( (null != location.getDriverid() && !"".equals(location.getDriverid().trim()))){
				driverid  = " driverid ";
				ldriverid  = " driverid ='"+location.getDriverid()+"'";
				if(ifwhere)ldriverid = " and "+ldriverid;
					
				if(!ifwhere)xwhere =xselect.where(QueryBuilder.eq(driverid,location.getDriverid()));
				else xwhere.and(QueryBuilder.eq(driverid,location.getDriverid()));
				ifwhere = true;
		}if( (null != location.getLat() && !"".equals(location.getLat().trim()))){
				lat  = " lat ";
				llat  = " lat ='"+location.getLat()+"'";
				if(ifwhere)llat = " and "+llat;
		
				if(!ifwhere)xwhere =xselect.where(QueryBuilder.eq(lat,location.getLat()));
				else xwhere.and(QueryBuilder.eq(lat,location.getLat()));
				ifwhere = true;
		}if( (null != location.getLon() && !"".equals(location.getLon().trim()))){
				lon = " lon ";
				llon  = " lon ='"+location.getLon()+"'";
				if(ifwhere)llon = " and "+llon;
				
				if(!ifwhere)xwhere =xselect.where(QueryBuilder.eq(lon,location.getLon()));
				else xwhere.and(QueryBuilder.eq(lon,location.getLon()));
				ifwhere = true;
		}if( (null != location.getTimestamp() && !"".equals(location.getTimestamp().trim()))){
				loctimestamp  = " loctimestamp ";
				
				String [] tab = location.getTimestamp().trim().split("\\|");
				String from = (tab.length>0)?tab[0]:"";
				String to =(tab.length>1)?tab[1]:"";
				
				logger.info("from :"+from+"---- to:"+to);
				
				lloctimestamp  = " loctimestamp ='"+location.getTimestamp()+"'";
				if(ifwhere)lloctimestamp = " and "+lloctimestamp;
				
				try {
						if(!ifwhere){
							xwhere = settimestampOrInterval(location, loctimestamp, xselect, xwhere, from, to);
						}else{
							xwhere = settimestampOrInterval(location, loctimestamp, xselect, xwhere, from, to);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				ifwhere = true;
		}		
		
		
		  Statement stmt =  xwhere;
		
		
	if(ifwhere)where = " where ";
	/*			
		Select.Where  xwhere =xselect.where(QueryBuilder.gt("height",450));
		
		xwhere.and(QueryBuilder.eq("product","test"));
		xwhere.and(QueryBuilder.eq("product","test"));
		xwhere.and(QueryBuilder.eq("product","test")).limit(50).orderBy(QueryBuilder.asc("id"));*/
		
	logger.info(llat);
		Query+=space(where)+space(lbdsid)+space(ldriverid)+space(llat)+space(llon)+space(lloctimestamp);
		
		logger.info("Query:"+Query);
		return stmt;
	}


	private Select.Where settimestampOrInterval(Location location, String loctimestamp, Select xselect,
			Select.Where xwhere, String from, String to) throws Exception {
		if(!location.getTimestamp().contains("|"))xwhere =xselect.where(QueryBuilder.eq(loctimestamp,getDate(formatTimeStamp(location.getTimestamp()))));
		else {
			if(null != from && !"".equals(from.trim()))xwhere =xselect.where(QueryBuilder.gte(loctimestamp,getDate(formatTimeStamp(from))));
			if( null != to && !"".equals(to.trim()))xwhere =xselect.where(QueryBuilder.lte(loctimestamp,getDate(formatTimeStamp(to))));
		}
		return xwhere;
	}
	
	public static String formatTimeStamp(String notFormatedTimestamp)throws Exception{
		String formatedTimestamp = null;
		if(!notFormatedTimestamp.contains("+"))return notFormatedTimestamp;
		String [] str_arr = notFormatedTimestamp.trim().split("\\+");
		
		if(str_arr.length != 2) throw new Exception(" Invalid timestamp!");
		//cqlsh:bdslocservfeat> select * from location where loctimestamp = '2016-09-18 14:09:00.000+0000' ALLOW FILTERING;
		formatedTimestamp =str_arr[0].substring(0, 23)+"+"+str_arr[1];
		//logger.info("received output:"+formatedTimestamp);
		return formatedTimestamp;
	}
	
/*	public static void main(String [] args) throws Exception{
		String str= "2016-09-18 14:09:00.000000+0000 ";
		
		String out ="2016-09-18 14:09:00.000+0000";
		logger.info("input :"+str);
		
		 formatTimeStamp(str);
		 logger.info("expected ouput :"+out);
		
	}*/
	
	public List<Location> getByBds(Exchange ex)throws Exception{
		if(!(ex.getIn().getHeader("bdsid") instanceof String))throw new Exception(" The parameter bdsid provided in the method selectLocation is not a String !");
		String BdsId = (String)ex.getIn().getHeader("bdsid",String.class);
		if(null == BdsId || "".equals(BdsId))throw new Exception(" The parameter bdsid provided in the method selectLocation is not either null or just empty !");
		
		Location location = new Location();
		location.setBdsid(BdsId);
		Locations inlocations = new Locations(new LinkedList<Location>());
		inlocations.getLocations().add(location);
		return queryLocations(inlocations);
	}
	
	public List<Location> getByDriver(Exchange ex)throws Exception{

		if(!(ex.getIn().getHeader("bdsid") instanceof String))throw new Exception(" The parameter bdsid provided in the header is not a String !");
		String BdsId = (String)ex.getIn().getHeader("bdsid",String.class);
		if(null == BdsId || "".equals(BdsId))throw new Exception(" The parameter bdsid provided in the method selectLocation is not either null or just empty !");
		
		if(!(ex.getIn().getHeader("driverid") instanceof String))throw new Exception(" The parameter bdsid provided in the header is not a String !");
		String driverId = (String)ex.getIn().getHeader("driverid",String.class);
		if(null == driverId || "".equals(driverId))throw new Exception(" The parameter bdsid provided in the method selectLocation is not either null or just empty !");
	
		Location location = new Location();
		location.setBdsid(BdsId);
		location.setDriverid(driverId);
		Locations inlocations = new Locations(new LinkedList<Location>());
		inlocations.getLocations().add(location);
		return queryLocations(inlocations);
	}
	
	public List<Location> getByTimeStamp(Exchange ex)throws Exception{
		if(!(ex.getIn().getHeader("bdsid") instanceof String))throw new Exception(" The parameter bdsid provided in the header is not a String !");
		String BdsId = (String)ex.getIn().getHeader("bdsid",String.class);
		if(null == BdsId || "".equals(BdsId))throw new Exception(" The parameter bdsid provided in the method selectLocation is not either null or just empty !");
		
		if(!(ex.getIn().getHeader("driverid") instanceof String))throw new Exception(" The parameter driverid provided in the header is not a String !");
		String driverId = (String)ex.getIn().getHeader("driverid",String.class);
		if(null == driverId || "".equals(driverId))throw new Exception(" The parameter driverid provided in the method selectLocation is not either null or just empty !");
		
/*		if(!(ex.getIn().getHeader("timestamp") instanceof String))throw new Exception(" The parameter timestamp provided in the header is not a String !");
		String timeStamp = (String)ex.getIn().getHeader("timeStamp",String.class);
		if(null == timeStamp || "".equals(timeStamp))throw new Exception(" The parameter timestamp provided in the method selectLocation is not either null or just empty !");
*/
		String from = (String)ex.getIn().getHeader("from",String.class);
		String to = (String)ex.getIn().getHeader("to",String.class);
		logger.info("from:"+from+"  ---------   to:"+to);
		
		Location location = new Location();
		location.setBdsid(BdsId);
		location.setDriverid(driverId);	
		String timestamp ="";
		if(null != from && !"".equals(from)  )timestamp =from;
		if("".equals(timestamp) && null != from && !"".equals(from) && null!=to && !"".equals(to) )timestamp =to;
		if(!"".equals(timestamp) && null != from && !"".equals(from) && null!=to && !"".equals(to) )timestamp =from+"|"+to;
		if(!"".equals(timestamp))location.setTimestamp(timestamp);
		Locations inlocations = new Locations(new LinkedList<Location>());
		inlocations.getLocations().add(location);
		return queryLocations(inlocations);
	}
	
	
	public List<Location>  getByBdsTimestamp(Exchange ex)throws Exception{
		if(!(ex.getIn().getHeader("bdsid") instanceof String))throw new Exception(" The parameter bdsid provided in the header is not a String !");
		String BdsId = (String)ex.getIn().getHeader("bdsid",String.class);
		if(null == BdsId || "".equals(BdsId))throw new Exception(" The parameter bdsid provided in the method selectLocation is not either null or just empty !");
		
/*		if(!(ex.getIn().getHeader("driverid") instanceof String))throw new Exception(" The parameter driverid provided in the header is not a String !");
		String driverId = (String)ex.getIn().getHeader("driverid",String.class);
		if(null == driverId || "".equals(driverId))throw new Exception(" The parameter driverid provided in the method selectLocation is not either null or just empty !");
*/		
		
		String from= (String)ex.getIn().getHeader("from",String.class);
		String to= (String)ex.getIn().getHeader("to",String.class);
		Location location = new Location();
		
		String timestamp ="";
		if(null != from && !"".equals(from)  )timestamp =from;
		if("".equals(timestamp) && null != from && !"".equals(from) && null!=to && !"".equals(to) )timestamp =to;
		if(!"".equals(timestamp) && null != from && !"".equals(from) && null!=to && !"".equals(to) )timestamp =from+"|"+to;
		if(!"".equals(timestamp))location.setTimestamp(timestamp);
		
		
		location.setBdsid(BdsId);
		//location.setDriverid(driverId);		
		//location.setTimestamp(timeStamp);
		Locations inlocations = new Locations(new LinkedList<Location>());
		inlocations.getLocations().add(location);
		//return queryLocations(inlocations);
		if(null != BdsId  && !"".equals(BdsId.trim())) return  queryLocationsByBdsid(inlocations, from, to);
		return queryLocations(inlocations);
	}
	
	
	public List<Location>  getByDriverTimestamp(Exchange ex)throws Exception{
		if(!(ex.getIn().getHeader("bdsid") instanceof String))throw new Exception(" The parameter bdsid provided in the header is not a String !");
		String BdsId = (String)ex.getIn().getHeader("bdsid",String.class);
		if(null == BdsId || "".equals(BdsId))throw new Exception(" The parameter bdsid provided in the method selectLocation is not either null or just empty !");
		
		if(!(ex.getIn().getHeader("driverid") instanceof String))throw new Exception(" The parameter driverid provided in the header is not a String !");
		String driverId = (String)ex.getIn().getHeader("driverid",String.class);
		if(null == driverId || "".equals(driverId))throw new Exception(" The parameter driverid provided in the method selectLocation is not either null or just empty !");
		
		Location location = new Location();
		location.setBdsid(BdsId);
		location.setDriverid(driverId);		
		//location.setTimestamp(timeStamp);
		Locations inlocations = new Locations(new LinkedList<Location>());
		inlocations.getLocations().add(location);
		return queryLocations(inlocations);		
	}
}
