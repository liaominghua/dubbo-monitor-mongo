package com.handu.open.dubbo.monitor.mvc;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.web.servlet.ModelAndView;

public class UserPutInterceptor extends CheckUserInterceptor {
	
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		return true;
	}
	
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		 HttpSession session = request.getSession();
	        Serializable sessionData = (Serializable)session.getAttribute(getUserSessionKey());
	        if ( sessionData != null && modelAndView != null) {
	        	modelAndView.addObject("headerUserName", sessionData);
	        } 
	}
}
