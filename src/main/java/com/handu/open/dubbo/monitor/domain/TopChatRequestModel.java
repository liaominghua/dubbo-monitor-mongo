/**
 * 
 */
package com.handu.open.dubbo.monitor.domain;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * @author Cacti
 * 
 *         2016年10月24日
 * 
 */
public class TopChatRequestModel {
	
	public static final String SUCCESS = "success";
	
	public static final String FAILURE = "failure";
	
	public static final String ELAPSED = "elapsed";
	
	public static final String CONCURRENT = "concurrent";
	
	public static final String MAXELAPSED = "maxElapsed";
	
	public static final String MAXCONCURRENT = "maxConcurrent";
	
	public static final String DEFAULT_TYPE = "consumer";
	
	public static final String AVG_ELAPSED="agvelapsed";
	
	private static final Map<String, String>  map = new HashMap<String, String>();
	static {
		map.put("concur",CONCURRENT);
		map.put("suc",SUCCESS);
		map.put("fail",FAILURE);
		map.put("maxconcur",MAXCONCURRENT);
		map.put("maxelap",MAXELAPSED);
		map.put("agvelapsed",AVG_ELAPSED);
	}
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date invokeDateFrom;
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date invokeDateTo;
	
	private String type;
	
	private String serviceType;
	
	private Integer size;

	public Date getInvokeDateFrom() {
		return invokeDateFrom;
	}

	public void setInvokeDateFrom(Date invokeDateFrom) {
		this.invokeDateFrom = invokeDateFrom;
	}

	public Date getInvokeDateTo() {
		return invokeDateTo;
	}

	public void setInvokeDateTo(Date invokeDateTo) {
		this.invokeDateTo = invokeDateTo;
	}

	public String getType() {
		if(type == null || StringUtils.isBlank(type)) {
			return DEFAULT_TYPE;
		}
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getServiceType() {
		String returnType = map.get(serviceType);
		if(returnType == null || StringUtils.isBlank(returnType)) {
			return SUCCESS;
		}
		return returnType; 
	}

	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

	public Integer getSize() {
		if(size == null || size <= 0 ) {
			return 20;
		}
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	
}
