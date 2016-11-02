package com.handu.open.dubbo.monitor.session;

import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer;


public class Initializer extends AbstractHttpSessionApplicationInitializer {
	public Initializer() {
		super(SessionConfig.class);
	}
}
