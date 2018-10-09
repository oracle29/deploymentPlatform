package com.deployment.service.impl;

import com.deployment.service.HealthService;
import com.deployment.service.ScriptService;
import com.deployment.vo.NodesServiceInfoVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author torvalds on 2018/8/23 14:09.
 * @version 1.0
 */
@Service
public class HealthServiceImpl implements HealthService, DisposableBean {
    Logger logger = LoggerFactory.getLogger(getClass());
    private ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(1);
    private final int HEARTBEAT_PERIOD = 8;
    @Autowired
    RestTemplate restTemplate;
    @Value("${health.manage.url}")
    String healthManageUrl;

    @Value("${server.port}")
    String serverPort;
    @Value("${app.restart.script.folder}")
    String appRestartScriptPath;

    @Autowired
    ScriptService scriptService;

    @Override
    public String healthCheck(NodesServiceInfoVo nodesServiceInfoVo) {
        scriptService.saveOrUpdateScript(nodesServiceInfoVo);
        return "success";
    }


    @PostConstruct
    public void sendHeartbeat() {
        if (StringUtils.hasText(healthManageUrl)) {
            scheduledExecutor.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    heartbeat();
                }
            }, HEARTBEAT_PERIOD, HEARTBEAT_PERIOD, TimeUnit.SECONDS);
        }
    }

    private void heartbeat() {
        try {
            restTemplate.postForObject(healthManageUrl, scriptService.getNodeServiceInfo(InetAddress.getLocalHost().getHostAddress(), serverPort, appRestartScriptPath), String.class);
        } catch (UnknownHostException e) {
            logger.error("健康检查异常healthManageUrl={}", healthManageUrl, e);
        }
    }

    @Override
    public void destroy() throws Exception {
        scheduledExecutor.shutdown();
    }
}
