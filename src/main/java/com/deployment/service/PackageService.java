package com.deployment.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author torvalds on 2018/10/8 21:16.
 * @version 1.0
 */
public interface PackageService {


    public void notifyNodes(String fileName);

    void saveFiles(MultipartFile jarFile);


    void download(String fileName);
}
