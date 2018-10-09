package com.deployment.controller;

import com.deployment.service.HealthService;
import com.deployment.vo.NodesServiceInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

/**
 * @author torvalds on 2018/8/23 8:48.
 * @version 1.0
 */
@RestController
public class HealthController {
    @Autowired
    HealthService healthService;

    @RequestMapping(value = "nodeHealth", method = RequestMethod.POST)
    public String leaderTransport(@RequestBody NodesServiceInfoVo nodesServiceInfoVo) {
        return healthService.healthCheck(nodesServiceInfoVo);
    }

}
