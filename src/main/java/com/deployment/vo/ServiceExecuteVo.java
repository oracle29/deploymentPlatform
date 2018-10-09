package com.deployment.vo;

/**
 * @author torvalds on 2018/10/8 14:40.
 * @version 1.0
 */
public class ServiceExecuteVo {
    private String hostAddress;
    private String id;
    private String type;

    public String getHostAddress() {
        return hostAddress;
    }

    public void setHostAddress(String hostAddress) {
        this.hostAddress = hostAddress;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
