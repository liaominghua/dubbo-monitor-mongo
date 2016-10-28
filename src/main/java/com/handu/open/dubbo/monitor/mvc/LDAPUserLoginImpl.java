package com.handu.open.dubbo.monitor.mvc;

import java.io.Serializable;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class LDAPUserLoginImpl implements UserLogin {
	
	private static final Logger logger = Logger.getLogger(LDAPUserLoginImpl.class);
	
	private String domainAddress;
	

	public Serializable doLogin(String username, String password) {
		if(StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
			return null;
		}
        Hashtable<String,String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, "ldap://"+domainAddress );
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, username);
        env.put(Context.SECURITY_CREDENTIALS, password);
        DirContext ctx = null;
        try {
            // 链接ldap
            ctx = new InitialDirContext(env);
            logger.warn("User:"+username+" 认证成功.");
        } catch (javax.naming.AuthenticationException e) {
        	logger.warn("User:"+username+" 认证失败.");
        } catch (Exception e) {
        	logger.warn("User:"+username+" 认证失败.");
        } 
        
        try{
        	if(ctx != null) {
        		ctx.close();
        		return username;
        	}
        }catch(Exception e) {
        }
        return null;
	}
	
	public String getDomainAddress() {
		return domainAddress;
	}

	public void setDomainAddress(String domainAddress) {
		this.domainAddress = domainAddress;
	}
}
