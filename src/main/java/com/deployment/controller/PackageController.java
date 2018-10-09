package com.deployment.controller;

import com.deployment.service.PackageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author torvalds on 2018/8/22 23:35.
 * @version 1.0
 */
@Controller
public class PackageController {
    Logger logger = LoggerFactory.getLogger(getClass());


    @Autowired
    PackageService packageService;

    @RequestMapping("upload")
    public void upload(MultipartFile jarFile, HttpServletResponse response) {
        packageService.saveFiles(jarFile);
        try {
            response.sendRedirect("startUp.html");
        } catch (IOException e) {
            logger.error("重定向异常", e);
        }
    }

    @RequestMapping(value = "reviveNotify", method = RequestMethod.POST)
    @ResponseBody
    public String reviveNotify(@RequestBody String fileName) {
        packageService.download(fileName);
        return "success";
    }

}
