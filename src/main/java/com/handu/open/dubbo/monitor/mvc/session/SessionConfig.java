package com.handu.open.dubbo.monitor.mvc.session;

import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

import redis.clients.jedis.JedisPoolConfig;

@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 1800)
@PropertySource("classpath:application.properties")
public class SessionConfig implements EnvironmentAware {
	private Environment env;

	@Bean
	public JedisConnectionFactory connectionFactory() {

		JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMaxWaitMillis(env.getProperty("redis.maxWaitMillis", Integer.class));
		poolConfig.setMaxTotal(env.getProperty("redis.maxTotal", Integer.class));
		poolConfig.setMinIdle(env.getProperty("redis.minIdle", Integer.class));
		poolConfig.setMaxIdle(env.getProperty("redis.maxIdle", Integer.class));

		JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(poolConfig);
		jedisConnectionFactory.setHostName(env.getProperty("redis.host", String.class));
		jedisConnectionFactory.setPort(env.getProperty("redis.port", Integer.class,6379));
		jedisConnectionFactory.setDatabase(env.getProperty("redis.dataBaseIndex", Integer.class,0));
		jedisConnectionFactory.setPassword(env.getProperty("redis.password", String.class,""));
		return jedisConnectionFactory;
	}

	public void setEnvironment(Environment environment) {
		this.env = environment;
	}
}