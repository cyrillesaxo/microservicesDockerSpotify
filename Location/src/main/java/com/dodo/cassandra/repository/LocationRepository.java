package com.dodo.cassandra.repository;

import java.util.List;

import org.apache.camel.Exchange;

import com.dodo.model.Location;


public interface LocationRepository {

	public void insertLocation(Exchange ex)throws Exception;
	public  List<Location>  selectLocation(Exchange ex)throws Exception;
}
