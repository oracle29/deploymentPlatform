package com.deployment.service.impl;

import com.deployment.service.ScriptService;
import com.deployment.vo.Group;
import com.deployment.vo.NodesServiceInfoVo;
import com.deployment.vo.ServiceExecuteVo;
import com.deployment.vo.ServiceScript;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.tomcat.util.security.MD5Encoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * @author torvalds on 2018/10/8 14:30.
 * @version 1.0
 */
@Service
public class ScriptServiceImpl implements ScriptService {
    Logger logger = LoggerFactory.getLogger(getClass());
    @Value("${server.context-path}")
    public String contextPath;
    @Value("${spring.resources.static-locations:}")
    private String fileLocation;
    @Value("${jars.deploy.location}")
    private String jarsDeployLocation;
    @Autowired
    RestTemplate restTemplate;
    Cache<String, NodesServiceInfoVo> cache = CacheBuilder.newBuilder().expireAfterWrite(30, TimeUnit.SECONDS).build();
    ConcurrentMap<String, String> script = new ConcurrentHashMap<>();

    @Override
    public void executeScript(ServiceExecuteVo serviceExecuteVo) {
        String action = "execute";
        NodesServiceInfoVo nodesServiceInfoVo = cache.getIfPresent(serviceExecuteVo.getHostAddress());
        if (nodesServiceInfoVo != null) {
            restTemplate.postForObject(new StringBuilder("http://").append(nodesServiceInfoVo.getHostAddress()).append(":").append(nodesServiceInfoVo.getPort()).append(contextPath).append("/").append(action).toString(), serviceExecuteVo, String.class);
        }
    }

    @Override
    public String execute(ServiceExecuteVo serviceExecuteVo) {
        if (!PackageServiceImpl.decompressionFlag) {
            decompression(PackageServiceImpl.compressionFileName);
            PackageServiceImpl.decompressionFlag = true;
        }
        String scriptFile = script.get(serviceExecuteVo.getId());
        executeShellScript(new String[]{"sh", scriptFile});
        return "success";
    }

    private void decompression(String compressionFileName) {
        String unCompressionFileName = compressionFileName.substring(0, compressionFileName.lastIndexOf("."));
        String tempDir=fileLocation + "/temp/";
        executeShellScript(new String[]{"sh", "-c", "unzip -o " + tempDir+ compressionFileName + " -d " + tempDir + unCompressionFileName +" > "+tempDir+"decompress.log"});
        jarsDeployLocation = jarsDeployLocation.endsWith("/") ? jarsDeployLocation : jarsDeployLocation + "/";
        Path path = Paths.get(jarsDeployLocation);
        if (Files.notExists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        executeShellScript(new String[]{"sh", "-c", "mv  " + fileLocation + "/temp/" + unCompressionFileName + "/* " + jarsDeployLocation});
    }

    @Override
    public void saveOrUpdateScript(NodesServiceInfoVo nodesServiceInfoVo) {
        cache.put(nodesServiceInfoVo.getHostAddress(), nodesServiceInfoVo);
    }

    @Override
    public Collection<NodesServiceInfoVo> getNodes() {
        return cache.asMap().values();
    }


    @Override
    public NodesServiceInfoVo getNodeServiceInfo(String hostAddress, String serverPort, String appRestartScriptPath) {
        NodesServiceInfoVo nodesServiceInfoVo = new NodesServiceInfoVo();
        nodesServiceInfoVo.setHostAddress(hostAddress);
        nodesServiceInfoVo.setPort(serverPort);
        File file = new File(appRestartScriptPath);
        List<Group> groups = new ArrayList<>();
        File[] groupFiles = getRestarts(file);
        Group defultGroup = new Group();
        List<ServiceScript> defualtServiceList = new ArrayList<>();
        defultGroup.setServiceList(defualtServiceList);
        script.clear();
        for (File groupFile : groupFiles) {
            if (groupFile.isFile()) {
                ServiceScript serviceScript = new ServiceScript();
                serviceScript.setId(MD5Encoder.encode(file.getAbsolutePath().getBytes()));
                serviceScript.setName(file.getAbsolutePath());
                defualtServiceList.add(serviceScript);
                script.put(serviceScript.getId(), serviceScript.getName());
            } else {
                Group group = new Group();
                group.setName(groupFile.getName());
                List<ServiceScript> serviceScripts = new ArrayList<>();
                getGroupRestarts(groupFile, serviceScripts);
                group.setServiceList(serviceScripts);
                groups.add(group);
            }

        }
        if (defualtServiceList != null && !defualtServiceList.isEmpty()) {
            groups.add(defultGroup);
        }
        nodesServiceInfoVo.setGroups(groups);
        nodesServiceInfoVo.setTotalSerivceNum(script.size());
        return nodesServiceInfoVo;
    }


    private void getGroupRestarts(File groupFile, List<ServiceScript> serviceScripts) {
        File[] restarts = getRestarts(groupFile);
        if (restarts != null && restarts.length > 0) {
            for (File restart : restarts) {
                if (restart.isDirectory()) {
                    getGroupRestarts(groupFile, serviceScripts);
                } else {
                    ServiceScript serviceScript = new ServiceScript();
                    serviceScript.setId(DigestUtils.md5DigestAsHex(restart.getAbsolutePath().getBytes()));
                    serviceScript.setName(restart.getAbsolutePath());
                    serviceScripts.add(serviceScript);
                    script.put(serviceScript.getId(), serviceScript.getName());
                }
            }
        }
    }

    private File[] getRestarts(File file) {
        return file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory() || pathname.getName().startsWith("restart");
            }
        });
    }

    public void executeShellScript(String... cmdarray) {
        try {
            Process pro = Runtime.getRuntime().exec(cmdarray);
            pro.waitFor();
        } catch (Exception e) {
            logger.error("执行脚本异常", e);
        }
    }
}
