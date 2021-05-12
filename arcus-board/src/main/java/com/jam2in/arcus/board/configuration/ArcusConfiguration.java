package com.jam2in.arcus.board.configuration;

import net.spy.memcached.ArcusClient;
import net.spy.memcached.ArcusClientPool;
import net.spy.memcached.ConnectionFactoryBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:arcus.properties")
public class ArcusConfiguration {

	@Value("${arcus.address}")
	private String address;

	@Value("${arcus.serviceCode}")
	private String serviceCode;

	@Value("${arcus.poolSize}")
	private int poolSize;

	@Bean
	public ArcusClientPool arcusClient() {
		return ArcusClient.createArcusClientPool(
			address, serviceCode, new ConnectionFactoryBuilder(), poolSize);
	}

}