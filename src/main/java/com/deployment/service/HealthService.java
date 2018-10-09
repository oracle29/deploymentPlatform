package com.deployment.service;

import com.deployment.vo.NodesServiceInfoVo;

import java.util.Collection;

/**
 * @author torvalds on 2018/8/23 14:09.
 * @version 1.0
 */
public interface HealthService {
    /**
     * 健康检查
     *
     * @param nodesServiceInfoVo
     * @return
     */
    public String healthCheck(NodesServiceInfoVo nodesServiceInfoVo);


}
