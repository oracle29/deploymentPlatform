package com.deployment.service;

import com.deployment.vo.NodesServiceInfoVo;
import com.deployment.vo.ServiceExecuteVo;

import java.util.Collection;

/**
 * @author torvalds on 2018/10/8 14:30.
 * @version 1.0
 */
public interface ScriptService {
    void executeScript(ServiceExecuteVo serviceExecuteVo);

    void saveOrUpdateScript(NodesServiceInfoVo nodesServiceInfoVo);

    /**
     * 获取全部节点列表
     *
     * @return
     */
    public Collection<NodesServiceInfoVo> getNodes();

    public NodesServiceInfoVo getNodeServiceInfo(String hostAddress, String serverPort, String appRestartScriptPath);

    String execute(ServiceExecuteVo serviceExecuteVo);

}
