package com.dodo.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "location")
public class Location {
	private String bdsid;
	private String driverid;
	private String lat;
	private String lon;
	private String timestamp;
	public Location(){}
	public Location(String bdsid, String driverid, String lat, String lon, String timestamp) {
		super();
		this.bdsid = bdsid;
		this.driverid = driverid;
		this.lat = lat;
		this.lon = lon;
		this.timestamp = timestamp;
	}
	public String getBdsid() {
		return bdsid;
	}
	public void setBdsid(String bdsid) {
		this.bdsid = bdsid;
	}
	public String getDriverid() {
		return driverid;
	}
	public void setDriverid(String driverid) {
		this.driverid = driverid;
	}
	public String getLat() {
		return lat;
	}
	public void setLat(String lat) {
		this.lat = lat;
	}
	public String getLon() {
		return lon;
	}
	public void setLon(String lon) {
		this.lon = lon;
	}
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bdsid == null) ? 0 : bdsid.hashCode());
		result = prime * result + ((driverid == null) ? 0 : driverid.hashCode());
		result = prime * result + ((lat == null) ? 0 : lat.hashCode());
		result = prime * result + ((lon == null) ? 0 : lon.hashCode());
		result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Location other = (Location) obj;
		if (bdsid == null) {
			if (other.bdsid != null)
				return false;
		} else if (!bdsid.equals(other.bdsid))
			return false;
		if (driverid == null) {
			if (other.driverid != null)
				return false;
		} else if (!driverid.equals(other.driverid))
			return false;
		if (lat == null) {
			if (other.lat != null)
				return false;
		} else if (!lat.equals(other.lat))
			return false;
		if (lon == null) {
			if (other.lon != null)
				return false;
		} else if (!lon.equals(other.lon))
			return false;
		if (timestamp == null) {
			if (other.timestamp != null)
				return false;
		} else if (!timestamp.equals(other.timestamp))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "Location [bdsid=" + bdsid + ", driverid=" + driverid + ", lat=" + lat + ", lon=" + lon + ", timestamp="
				+ timestamp + "]";
	}
	
}
