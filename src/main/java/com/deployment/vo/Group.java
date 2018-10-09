package com.deployment.vo;

import java.util.List;

public class Group {
    private String name;
    private List<ServiceScript> serviceList;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ServiceScript> getServiceList() {
        return serviceList;
    }

    public void setServiceList(List<ServiceScript> serviceList) {
        this.serviceList = serviceList;
    }
}