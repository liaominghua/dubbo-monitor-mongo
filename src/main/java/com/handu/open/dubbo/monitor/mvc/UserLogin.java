/**
 * 
 */
package com.handu.open.dubbo.monitor.mvc;

import java.io.Serializable;

/**
 * @author Cacti
 * 
 *	2016年10月28日
 * 
 */
public interface UserLogin {
	
	/**
	 * 
	 * @param username
	 * @param password
	 * @return
	 * 			
	 */
	public Serializable doLogin(String username,String password);

}
