/**
 * 
 */
package com.handu.open.dubbo.monitor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import me.walkongrass.dubbo.tracker.model.BaseTraceData;
import me.walkongrass.dubbo.tracker.service.DubboTracerService;

/**
 * @author Cacti
 * 
 *	2016年11月10日
 * 
 */
public class DubboTracerServiceImpl implements DubboTracerService {
	  @Autowired
	   private MongoTemplate mongoTemplate;

	/* (non-Javadoc)
	 * @see me.walkongrass.dubbo.tracker.service.DubboTracerService#trace(me.walkongrass.dubbo.tracker.model.BaseTraceData)
	 */
	public void trace(BaseTraceData tradeData) {
		if(tradeData != null) {
			try{
				// 保存调用数据
				mongoTemplate.insert(tradeData);
			}catch(Exception e) {
				
			}
			
		}
	}

}
