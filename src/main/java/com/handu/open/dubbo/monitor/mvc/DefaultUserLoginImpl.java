package com.handu.open.dubbo.monitor.mvc;

import java.io.Serializable;

public class DefaultUserLoginImpl implements UserLogin {
	
	private String userName;
	
	private String password;

	public Serializable doLogin(String username, String password) {
		if(this.userName.equals(username) && this.password.equals(password)) {
			return this.userName;
		}
		return null;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
