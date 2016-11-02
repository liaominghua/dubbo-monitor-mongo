package com.handu.open.dubbo.monitor.mvc.session;

import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@EnableRedisHttpSession(maxInactiveIntervalInSeconds=10)
public class SessionConfig {
	@Bean
	public JedisConnectionFactory connectionFactory()
	{
		JedisConnectionFactory connection = new JedisConnectionFactory();
		connection.setPort(6379);
		connection.setHostName("localhost");
		return connection;
	}
}