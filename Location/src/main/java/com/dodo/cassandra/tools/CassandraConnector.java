package com.dodo.cassandra.tools;

import static java.lang.System.out;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.CodecRegistry;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.Session;
import com.datastax.driver.extras.codecs.joda.DateTimeCodec;
import com.datastax.driver.extras.codecs.date.SimpleTimestampCodec;

public abstract class CassandraConnector {
	private Cluster cluster;
	private Session session;
	private Properties prop ;
	private String ip;
	private int port;
	private String keyspace;

	/**
	 * Connect to Cassandra Cluster specified by provided node IP address and
	 * port number.
	 * @param node
	 *            Cluster node IP address.
	 * @param port
	 *            Port of cluster host.
	 */
	public void connect(){
		this.config();
		this.connect(ip, port);
	}
	public void connect(final String node, final int port) {

		this.cluster = Cluster.builder().addContactPoint(node).withPort(port)
				 .withCodecRegistry(new CodecRegistry().register(SimpleTimestampCodec.instance))
				.build();
		
		final Metadata metadata = cluster.getMetadata();
		out.printf("Connected to cluster: %s\n", metadata.getClusterName());
		for (final Host host : metadata.getAllHosts()) {
			out.printf("Datacenter: %s; Host: %s; Rack: %s\n", host.getDatacenter(), host.getAddress(), host.getRack());
		}
		session = cluster.connect();
	}

	/**
	 * Provide my Session.
	 * @return My session.
	 */
	public Session getSession() {
		return this.session;
	}

	/** Close cluster. */
	public void close(){
		cluster.close();
	}
	
	public void config(){
		prop = Tool.prop();
		ip = prop.getProperty("cassandra.contactpoints");
		if(ip ==null || "".equals(ip.trim()))ip="127.0.0.1";
		port = new Integer(prop.getProperty("cassandra.port"));
		if(port ==0)port=9042;
		keyspace = prop.getProperty("cassandra.keyspace");
	}
}
