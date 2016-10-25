/**
 * Copyright 2006-2015 handu.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.handu.open.dubbo.monitor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.ConfigUtils;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.dubbo.monitor.MonitorService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.handu.open.dubbo.monitor.domain.DubboInvoke;
import com.handu.open.dubbo.monitor.domain.TopChatRequestModel;
import com.handu.open.dubbo.monitor.support.QueryConstructor;
import com.handu.open.dubbo.monitor.support.UuidUtil;

/**
 * MonitorService
 *
 * @author Jinkai.Ma
 */
@Service(delay = -1)
public class DubboMonitorService implements MonitorService {

    private static final Logger logger = LoggerFactory.getLogger(DubboMonitorService.class);

//    private static final String[] types = {SUCCESS, FAILURE, ELAPSED, CONCURRENT, MAX_ELAPSED, MAX_CONCURRENT};

    private static final String POISON_PROTOCOL = "poison";

    private static final String TIMESTAMP = "timestamp";

    private volatile boolean running = true;

    private Thread writeThread;

    private BlockingQueue<URL> queue;

    @Autowired
    private RegistryContainer registryContainer;

    @Autowired
    private MongoTemplate mongoTemplate;

    @PostConstruct
    private void init() {
        queue = new LinkedBlockingQueue<URL>(Integer.parseInt(ConfigUtils.getProperty("dubbo.monitor.queue", "100000")));
        writeThread = new Thread(new Runnable() {
            public void run() {
                while (running) {
                    try {
                        writeToDataBase(); // 记录统计日志
                    } catch (Throwable t) { // 防御性容错
                        logger.error("Unexpected error occur at write stat log, cause: " + t.getMessage(), t);
                        try {
                            Thread.sleep(5000); // 失败延迟
                        } catch (Throwable t2) {
                        }
                    }
                }
            }
        });
        writeThread.setDaemon(true);
        writeThread.setName("DubboMonitorAsyncWriteLogThread");
        writeThread.start();
    }

    /**
     * Dubbo调用信息数据写入DB
     *
     * @throws Exception
     */
    private void writeToDataBase() throws Exception {
        URL statistics = queue.take();
        if (POISON_PROTOCOL.equals(statistics.getProtocol())) {
            return;
        }
        String timestamp = statistics.getParameter(Constants.TIMESTAMP_KEY);
        Date now;
        if (timestamp == null || timestamp.length() == 0) {
            now = new Date();
        } else if (timestamp.length() == "yyyyMMddHHmmss".length()) {
            now = new SimpleDateFormat("yyyyMMddHHmmss").parse(timestamp);
        } else {
            now = new Date(Long.parseLong(timestamp));
        }
        DubboInvoke dubboInvoke = new DubboInvoke();

        dubboInvoke.setId(UuidUtil.createUUID());
        try {
            if (statistics.hasParameter(PROVIDER)) {
                dubboInvoke.setType(CONSUMER);
                dubboInvoke.setConsumer(statistics.getHost());
                dubboInvoke.setProvider(statistics.getParameter(PROVIDER));
                int i = dubboInvoke.getProvider().indexOf(':');
                if (i > 0) {
                    dubboInvoke.setProvider(dubboInvoke.getProvider().substring(0, i));
                }
            } else {
                dubboInvoke.setType(PROVIDER);
                String consumer = statistics.getParameter(CONSUMER);
                if (!StringUtils.isEmpty(consumer)) {
                    dubboInvoke.setConsumer(consumer);
                    int i = dubboInvoke.getConsumer().indexOf(':');
                    if (i > 0) {
                        dubboInvoke.setConsumer(dubboInvoke.getConsumer().substring(0, i));
                    }
                }
                dubboInvoke.setProvider(statistics.getHost());
            }
            dubboInvoke.setInvokeDate(now);
            dubboInvoke.setService(statistics.getServiceInterface());
            dubboInvoke.setMethod(statistics.getParameter(METHOD));
            dubboInvoke.setInvokeTime(statistics.getParameter(TIMESTAMP, System.currentTimeMillis()));
            dubboInvoke.setSuccess(statistics.getParameter(SUCCESS, 0));
            dubboInvoke.setFailure(statistics.getParameter(FAILURE, 0));
            dubboInvoke.setElapsed(statistics.getParameter(ELAPSED, 0));
            dubboInvoke.setConcurrent(statistics.getParameter(CONCURRENT, 0));
            dubboInvoke.setMaxElapsed(statistics.getParameter(MAX_ELAPSED, 0));
            dubboInvoke.setMaxConcurrent(statistics.getParameter(MAX_CONCURRENT, 0));
            if (dubboInvoke.getSuccess() == 0 && dubboInvoke.getFailure() == 0 && dubboInvoke.getElapsed() == 0
                    && dubboInvoke.getConcurrent() == 0 && dubboInvoke.getMaxElapsed() == 0 && dubboInvoke.getMaxConcurrent() == 0) {
                return;
            }
            dubboInvoke.setTimeParticle(null);
            mongoTemplate.insert(dubboInvoke);

        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
        }
    }

    public void collect(URL statistics) {
        queue.offer(statistics);
        if (logger.isInfoEnabled()) {
            logger.info("collect statistics: " + statistics);
        }

    }

    public List<URL> lookup(URL query) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * 统计调用数据用于图表展示
     *
     * @param dubboInvoke
     */
    public List<DubboInvoke> countDubboInvoke(DubboInvoke dubboInvoke) {
        if (StringUtils.isEmpty(dubboInvoke.getService())) {
            logger.error("统计查询缺少必要参数！");
            throw new RuntimeException("统计查询缺少必要参数！");
        }

        TypedAggregation<DubboInvoke> aggregation = Aggregation.newAggregation(DubboInvoke.class,
                Aggregation.match(Criteria.where("service").is(dubboInvoke.getService())
                        .and("method").is(dubboInvoke.getMethod())
                        .and("type").is(dubboInvoke.getType())
                        .and("invokeDate").gte(dubboInvoke.getInvokeDateFrom()).lte(dubboInvoke.getInvokeDateTo())
                ),
                Aggregation.project("service", "method", "type", "success", "failure", "elapsed", "maxElapsed", "maxConcurrent", "invokeTime")
                        .andExpression("(invokeTime / " + dubboInvoke.getTimeParticle() + ") * " + dubboInvoke.getTimeParticle()).as("invokeTime"),
                Aggregation.group("service", "method", "type", "invokeTime")
                        .sum("success").as("success")
                        .sum("failure").as("failure")
                        .sum("elapsed").as("elapsed")
                        .max("maxElapsed").as("maxElapsed")
                        .min("maxConcurrent").as("maxConcurrent"),
                Aggregation.sort(Sort.Direction.ASC, "invokeTime")
        );
        AggregationResults<DubboInvoke> result = mongoTemplate.aggregate(aggregation, "dubboInvoke", DubboInvoke.class);
        return result.getMappedResults();
    }

    public Set<String> getMethodsByService(DubboInvoke dubboInvoke) {
        Set<String> methods = Sets.newHashSet();
        
        QueryConstructor qc = QueryConstructor.get()
                .addIsAttribute("service", dubboInvoke.getService())
                .addIsAttribute("invokeDate", dubboInvoke.getInvokeDate())
                .addIsAttribute("provider", dubboInvoke.getProvider())
                .addIsAttribute("consumer", dubboInvoke.getConsumer())
                .addIsAttribute("type", dubboInvoke.getType())
                .addBetweenAttribute("invokeDate", dubboInvoke.getInvokeDateFrom(), dubboInvoke.getInvokeDateTo())
                ;
        if(dubboInvoke.getMethod() != null) {
        	qc.addIsAttribute("method", dubboInvoke.getMethod());
        }
        List<DubboInvoke> result = mongoTemplate.find(qc.getQuery(), DubboInvoke.class, "dubboInvoke");
        if(result != null) {
        	 for (DubboInvoke di : result) {
                 methods.add(di.getMethod());
             }
        }
       
        return methods;
    }

    /**
     * 统计各方法调用信息
     *
     * @param dubboInvoke
     * @return
     */
    public List<DubboInvoke> countDubboInvokeInfo(DubboInvoke dubboInvoke) {
        if (StringUtils.isEmpty(dubboInvoke.getService()) || StringUtils.isEmpty(dubboInvoke.getMethod())
                || StringUtils.isEmpty(dubboInvoke.getType())) {
            logger.error("统计查询缺少必要参数！");
            throw new RuntimeException("统计查询缺少必要参数！");
        }
        TypedAggregation<DubboInvoke> aggregation = Aggregation.newAggregation(DubboInvoke.class,
                Aggregation.match(Criteria.where("service").is(dubboInvoke.getService())
                        .and("method").is(dubboInvoke.getMethod())
                        .and("type").is(dubboInvoke.getType())
                        .and("invokeDate").gte(dubboInvoke.getInvokeDateFrom()).lte(dubboInvoke.getInvokeDateTo())
                ),
                Aggregation.group("service", "method")
                        .sum("success").as("success")
                        .sum("failure").as("failure")
                        .sum("elapsed").as("elapsed")
                        .max("maxElapsed").as("maxElapsed")
                        .min("maxConcurrent").as("maxConcurrent")
        );
        AggregationResults<DubboInvoke> result = mongoTemplate.aggregate(aggregation, "dubboInvoke", DubboInvoke.class);

        return result.getMappedResults();
    }

    /**
     * 统计系统方法调用排序信息
     *
     * @param dubboInvoke
     * @return
     */
    public Map<String, List> countDubboInvokeTopTen(TopChatRequestModel topChatRequestModel) {
        Map<String, List> result = Maps.newHashMap();
        result.put(topChatRequestModel.getServiceType(),getListByResultType(topChatRequestModel));
        return result;
    }
    
    private List<DubboInvoke> getListByResultType(TopChatRequestModel topChatRequestModel){
    	Criteria criteris = Criteria.where("invokeDate").gte(topChatRequestModel.getInvokeDateFrom()).lte(topChatRequestModel.getInvokeDateTo());
    	if(!topChatRequestModel.getType().equals(TopChatRequestModel.DEFAULT_TYPE)) {
    		criteris.and("type").is(topChatRequestModel.getType());
    	}

        List<DubboInvoke> resultList = Lists.newArrayList();
        List<AggregationOperation> operations = new ArrayList<AggregationOperation>();
        operations.add(Aggregation.match(criteris));
        operations.add(Aggregation.group( Fields.fields("service", "method")).sum(topChatRequestModel.getServiceType()).as(topChatRequestModel.getServiceType()));
        operations.add(Aggregation.sort(Sort.DEFAULT_DIRECTION.DESC,topChatRequestModel.getServiceType()));
        operations.add(Aggregation.limit(topChatRequestModel.getSize()));
        operations.add(Aggregation.project("service","method",topChatRequestModel.getServiceType()));
        AggregationResults<DubboInvoke> successResults = mongoTemplate.aggregate(Aggregation.newAggregation(operations),"dubboInvoke", DubboInvoke.class);
        resultList.addAll(successResults.getMappedResults());
        return resultList;
    }
}