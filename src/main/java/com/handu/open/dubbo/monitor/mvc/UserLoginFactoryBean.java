package com.handu.open.dubbo.monitor.mvc;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.FactoryBean;

/**
 * @author Cacti
 * 
 *	2016年10月28日
 *
 */
public class UserLoginFactoryBean implements FactoryBean<UserLogin> {
	
	
	private String ldapAddress;
	
	private String managerUsername;
	
	private String managerPassword;
	
	// ldap or local
	private String authType;
	
	

	public UserLogin getObject() throws Exception {
		if(!StringUtils.isEmpty(authType) && "ldap".equals(authType)) {
			LDAPUserLoginImpl ldapUserLogin =  new LDAPUserLoginImpl();
			ldapUserLogin.setDomainAddress(ldapAddress);
			return ldapUserLogin;
		}
		else if(!StringUtils.isEmpty(authType) && "local".equals(authType)) {
			DefaultUserLoginImpl defaultUserLoginImpl = new DefaultUserLoginImpl();
			defaultUserLoginImpl.setUserName(managerUsername);
			defaultUserLoginImpl.setPassword(managerPassword);
			return defaultUserLoginImpl;
		}
		else {
			throw new Exception("没有指定认证类型");
		}
	}

	public Class<?> getObjectType() {
		return UserLogin.class;
	}

	public boolean isSingleton() {
		return false;
	}

	public String getLdapAddress() {
		return ldapAddress;
	}

	public void setLdapAddress(String ldapAddress) {
		this.ldapAddress = ldapAddress;
	}

	public String getManagerUsername() {
		return managerUsername;
	}

	public void setManagerUsername(String managerUsername) {
		this.managerUsername = managerUsername;
	}

	public String getManagerPassword() {
		return managerPassword;
	}

	public void setManagerPassword(String managerPassword) {
		this.managerPassword = managerPassword;
	}

	public String getAuthType() {
		return authType;
	}

	public void setAuthType(String authType) {
		this.authType = authType;
	}

}
