package com.handu.open.dubbo.monitor.mvc.session;

import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer;

public class SessionInitializer extends AbstractHttpSessionApplicationInitializer {
	public SessionInitializer() 
	{
		super(SessionConfig.class);
	}
}