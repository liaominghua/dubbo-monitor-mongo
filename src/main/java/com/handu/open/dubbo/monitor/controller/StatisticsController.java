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

import com.handu.open.dubbo.monitor.DubboMonitorService;
import com.handu.open.dubbo.monitor.domain.DubboInvoke;
import com.handu.open.dubbo.monitor.domain.DubboStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Statistics Controller
 *
 * @author Zhiguo.Chen <me@chenzhiguo.cn>
 *         Created on 15/7/2.
 */
@Controller
@RequestMapping("/services/statistics")
public class StatisticsController {

    @Autowired
    private DubboMonitorService dubboMonitorService;

    @RequestMapping()
    public String index(@ModelAttribute DubboInvoke dubboInvoke, Model model) {
        // Set default Search Date
        if (dubboInvoke.getInvokeDateFrom() == null && dubboInvoke.getInvokeDateTo() == null) {
            Calendar fromDate = new GregorianCalendar();
            fromDate.set(Calendar.HOUR_OF_DAY, 0);
            fromDate.set(Calendar.MINUTE, 0);
            fromDate.set(Calendar.SECOND, 0);
            dubboInvoke.setInvokeDateFrom(fromDate.getTime());
            Calendar endDate = new GregorianCalendar();
            endDate.set(Calendar.HOUR_OF_DAY, 23);
            endDate.set(Calendar.MINUTE, 59);
            endDate.set(Calendar.SECOND, 59);
            dubboInvoke.setInvokeDateTo(endDate.getTime());
        }
        //获取Service方法
        List<DubboInvoke> dubboInvokes;
        List<DubboStatistics> dubboStatisticses = new ArrayList<DubboStatistics>();
        DubboStatistics dubboStatistics;
        
        dubboInvoke.setType("provider");
        dubboInvokes = dubboMonitorService.countDubboInvokeInfo(dubboInvoke,null);
        Map<String,DubboStatistics> map = new HashMap<String, DubboStatistics>();
        for (DubboInvoke di : dubboInvokes) {
            if (di == null) {
                continue;
            }
            dubboStatistics = new DubboStatistics();
            dubboStatistics.setMethod(di.getMethod());
            dubboStatistics.setProviderSuccess(di.getSuccess());
            dubboStatistics.setProviderFailure(di.getFailure());
            dubboStatistics.setProviderAvgElapsed(di.getSuccess() != 0 ? Double.valueOf(String.format("%.0f", di.getElapsed() / di.getSuccess())) : 0);
            dubboStatistics.setProviderMaxElapsed(di.getMaxElapsed());
            dubboStatistics.setProviderMaxConcurrent(di.getMaxConcurrent());
            dubboStatisticses.add(dubboStatistics);
            map.put(di.getMethod(), dubboStatistics);
        }
        
        dubboInvoke.setType("consumer");
        dubboInvokes = dubboMonitorService.countDubboInvokeInfo(dubboInvoke,null);
        for (DubboInvoke di : dubboInvokes) {
            if (di == null) {
                continue;
            }
            dubboStatistics = map.get(di.getMethod());
            if(dubboStatistics == null ) {
            	dubboStatistics = new DubboStatistics();
            	dubboStatistics.setMethod(di.getMethod());
            	dubboStatisticses.add(dubboStatistics);
            }
            
            dubboStatistics.setConsumerSuccess(di.getSuccess());
            dubboStatistics.setConsumerFailure(di.getFailure());
            dubboStatistics.setConsumerAvgElapsed(di.getSuccess() != 0 ? Double.valueOf(String.format("%.0f", di.getElapsed() / di.getSuccess())) : 0);
            dubboStatistics.setConsumerMaxElapsed(di.getMaxElapsed());
            dubboStatistics.setConsumerMaxConcurrent(di.getMaxConcurrent());
        }
        
        
        
        //
        List<DubboStatistics> dubboStatisticsesConsumer = new ArrayList<DubboStatistics>();
        dubboInvoke.setMethod(null);
        dubboInvoke.setType("consumer");
        dubboInvokes = dubboMonitorService.countDubboInvokeInfo(dubboInvoke,"consumer");
        String method = null;
        String prefix = "&emsp;|-->";
        for (DubboInvoke di : dubboInvokes) {
            if (di == null) {
                continue;
            }
            String curMethod = di.getMethod();
            if(method == null || !curMethod.equals(method)) {
            	method = curMethod;
            	 dubboStatistics = new DubboStatistics();
            	 dubboStatistics.setMethod(method);
            	 dubboStatisticsesConsumer.add(dubboStatistics);
            }
            dubboStatistics = new DubboStatistics();
            dubboStatistics.setMethod(prefix.concat(di.getConsumer()));
            dubboStatistics.setConsumerSuccess(di.getSuccess());
            dubboStatistics.setConsumerFailure(di.getFailure());
            dubboStatistics.setConsumerAvgElapsed(di.getSuccess() != 0 ? Double.valueOf(String.format("%.0f", di.getElapsed() / di.getSuccess())) : 0);
            dubboStatistics.setConsumerMaxElapsed(di.getMaxElapsed());
            dubboStatistics.setConsumerMaxConcurrent(di.getMaxConcurrent());
            dubboStatisticsesConsumer.add(dubboStatistics);
        }
        
        List<DubboStatistics> dubboStatisticsesProvider = new ArrayList<DubboStatistics>();
        dubboInvoke.setType("provider");
        dubboInvokes = dubboMonitorService.countDubboInvokeInfo(dubboInvoke,"provider");
         method = null;
         prefix = "&emsp;|<--";
        for (DubboInvoke di : dubboInvokes) {
            if (di == null) {
                continue;
            }
            String curMethod = di.getMethod();
            if(method == null || !curMethod.equals(method)) {
            	method = curMethod;
            	 dubboStatistics = new DubboStatistics();
            	 dubboStatistics.setMethod(method);
            	 dubboStatisticsesProvider.add(dubboStatistics);
            }
            dubboStatistics = new DubboStatistics();
            dubboStatistics.setMethod(prefix.concat(di.getProvider()));
            dubboStatistics.setProviderSuccess(di.getSuccess());
            dubboStatistics.setProviderFailure(di.getFailure());
            dubboStatistics.setProviderAvgElapsed(di.getSuccess() != 0 ? Double.valueOf(String.format("%.0f", di.getElapsed() / di.getSuccess())) : 0);
            dubboStatistics.setProviderMaxElapsed(di.getMaxElapsed());
            dubboStatistics.setProviderMaxConcurrent(di.getMaxConcurrent());
            dubboStatisticsesProvider.add(dubboStatistics);
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        model.addAttribute("dateRange",sdf.format(dubboInvoke.getInvokeDateFrom()).concat(" ~ ").concat(sdf.format(dubboInvoke.getInvokeDateTo())));
        model.addAttribute("rows", dubboStatisticses);
        model.addAttribute("rowsConsumer", dubboStatisticsesConsumer);
        model.addAttribute("rowsProvider", dubboStatisticsesProvider);
        model.addAttribute("service", dubboInvoke.getService());
        return "service/statistics";
    }

}

