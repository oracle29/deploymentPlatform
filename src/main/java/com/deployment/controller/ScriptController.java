package com.deployment.controller;

import com.deployment.service.ScriptService;
import com.deployment.vo.NodesServiceInfoVo;
import com.deployment.vo.ServiceExecuteVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

/**
 * @author torvalds on 2018/10/8 14:29.
 * @version 1.0
 */
@RestController
public class ScriptController {
    @Autowired
    ScriptService scriptService;

    @RequestMapping("executeScript")
    public String executeScript(@RequestBody ServiceExecuteVo serviceExecuteVo) {
        scriptService.executeScript(serviceExecuteVo);
        return "success";
    }

    @RequestMapping(value = "execute", method = RequestMethod.POST)
    public String execute(@RequestBody ServiceExecuteVo serviceExecuteVo) {
        return scriptService.execute(serviceExecuteVo);
    }


    @RequestMapping("getNodes")
    public Collection<NodesServiceInfoVo> getNodes() {
        return scriptService.getNodes();
    }

}
