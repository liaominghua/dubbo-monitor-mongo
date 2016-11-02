package com.handu.open.dubbo.monitor.session;

import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@EnableRedisHttpSession(maxInactiveIntervalInSeconds=1800)
public class SessionConfig {
 
	@Bean
    public JedisConnectionFactory connectionFactory() {
		JedisConnectionFactory jedisConnectionFactory =  new JedisConnectionFactory(); 
		jedisConnectionFactory.setHostName("localhost");
		jedisConnectionFactory.setPort(6379);
		return jedisConnectionFactory;
    }
}
