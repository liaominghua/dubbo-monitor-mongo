/**
 * Copyright 2006-2015 handu.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.handu.open.dubbo.monitor.controller;

import com.google.common.collect.Lists;
import com.handu.open.dubbo.monitor.DubboMonitorService;
import com.handu.open.dubbo.monitor.domain.DubboInvoke;
import com.handu.open.dubbo.monitor.domain.DubboInvokeLineChart;
import com.handu.open.dubbo.monitor.domain.LineChartSeries;
import com.handu.open.dubbo.monitor.domain.TopChatRequestModel;
import com.handu.open.dubbo.monitor.mvc.UserLogin;
import com.handu.open.dubbo.monitor.support.CommonResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Home Controller
 *
 * @author Silence <me@chenzhiguo.cn>
 *         Created on 15/6/26.
 */
@Controller
@RequestMapping("/")
public class IndexController {

    @Autowired
    private DubboMonitorService dubboMonitorService;
    
    @Autowired
    private UserLogin userLogin;

    @RequestMapping(method = RequestMethod.GET)
    public String home() {
        return "index";
    }

    @RequestMapping(value = "index", method = RequestMethod.GET)
    public String index() {
        return "index";
    }
    
    @RequestMapping(value = "login")
    public String login() {
    	return "login";
    }
    
    @RequestMapping(value = "doLogin")
    public String doLogin(@RequestParam(required=false) String username,
    					  @RequestParam(required=false) String password,
    					  HttpServletRequest request) {
    	HttpSession session = request.getSession();
    	Serializable obj = userLogin.doLogin(username, password);
    	if(obj == null) {
    		return "login";
    	}
    	
    	session.setAttribute("user.key", obj);
    	return "redirect:index";
    }
    
    @RequestMapping(value = "doLogout")
    public String doLogout( HttpServletRequest request) {
    	HttpSession session = request.getSession();
    	session.removeAttribute("user.key");
 		return "login";
    }

    @ResponseBody
    @RequestMapping(value = "loadTopData")
    public CommonResponse loadTopDate(@ModelAttribute TopChatRequestModel topChatRequestModel) {
        CommonResponse commonResponse = CommonResponse.createCommonResponse();
        List<DubboInvokeLineChart> dubboInvokeLineChartList = new ArrayList<DubboInvokeLineChart>();
        DubboInvokeLineChart successDubboInvokeLineChart = new DubboInvokeLineChart();
        List<String> sxAxisCategories = Lists.newArrayList();
        LineChartSeries slineChartSeries = new LineChartSeries();
        List<double[]> sdataList = Lists.newArrayList();
        double[] data;
        Map dubboInvokeMap = dubboMonitorService.countDubboInvokeTopTen(topChatRequestModel);
        List<DubboInvoke> result = (List<DubboInvoke>) dubboInvokeMap.get(topChatRequestModel.getServiceType());
        for (DubboInvoke di : result) {
            sxAxisCategories.add(di.getService()+"."+di.getMethod());
            if(TopChatRequestModel.CONCURRENT.equals(topChatRequestModel.getServiceType())){
            	data = new double[]{di.getConcurrent()};
            }
            else if(TopChatRequestModel.AVG_ELAPSED.equals(topChatRequestModel.getServiceType()) || TopChatRequestModel.ELAPSED.equals(topChatRequestModel.getServiceType())) {
            	data = new double[]{new BigDecimal(String.valueOf(di.getElapsed())).setScale(2, BigDecimal.ROUND_DOWN).doubleValue()};
            }
            else if(TopChatRequestModel.SUCCESS.equals(topChatRequestModel.getServiceType())) {
            	data = new double[]{di.getSuccess()};
            	
            }
            else if(TopChatRequestModel.FAILURE.equals(topChatRequestModel.getServiceType())) {
            	data = new double[]{di.getFailure()};
            }
            else if(TopChatRequestModel.MAXCONCURRENT.equals(topChatRequestModel.getServiceType())) {
            	data = new double[]{di.getMaxConcurrent()};
            }
            else if(TopChatRequestModel.MAXELAPSED.equals(topChatRequestModel.getServiceType())) {
            	data = new double[]{di.getMaxElapsed()};
            }
            else{
            	data = new double[]{0};
            }
            sdataList.add(data);
        }
        slineChartSeries.setData(sdataList);
        slineChartSeries.setName(topChatRequestModel.getType());

        successDubboInvokeLineChart.setxAxisCategories(sxAxisCategories);
        successDubboInvokeLineChart.setSeriesData(Arrays.asList(slineChartSeries));
        successDubboInvokeLineChart.setChartType(topChatRequestModel.getServiceType());
        successDubboInvokeLineChart.setTitle("The Top "+topChatRequestModel.getSize()+" of Invoke "+topChatRequestModel.getServiceType());
        if(TopChatRequestModel.AVG_ELAPSED.equals(topChatRequestModel.getServiceType()) || TopChatRequestModel.ELAPSED.equals(topChatRequestModel.getServiceType())) {
        	 successDubboInvokeLineChart.setyAxisTitle(" ms");
        }
        else {
        	 successDubboInvokeLineChart.setyAxisTitle(" t");
        }
       
        dubboInvokeLineChartList.add(successDubboInvokeLineChart);

        commonResponse.setData(dubboInvokeLineChartList);
        return commonResponse;
    }
}
