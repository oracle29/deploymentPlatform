package com.deployment.vo;

import java.util.List;

/**
 * @author torvalds on 2018/9/27 22:54.
 * @version 1.0
 */
public class NodesServiceInfoVo {
    private String hostAddress;
    private String port;
    private List<Group> groups;
    private Integer totalSerivceNum;

    public String getHostAddress() {
        return hostAddress;
    }

    public Integer getTotalSerivceNum() {
        return totalSerivceNum;
    }

    public void setTotalSerivceNum(Integer totalSerivceNum) {
        this.totalSerivceNum = totalSerivceNum;
    }

    public void setHostAddress(String hostAddress) {
        this.hostAddress = hostAddress;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }


}
