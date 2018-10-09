package com.deployment.service.impl;

import com.deployment.service.PackageService;
import com.deployment.service.ScriptService;
import com.deployment.vo.NodesServiceInfoVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author torvalds on 2018/10/8 21:17.
 * @version 1.0
 */
@Service
public class PackageServiceImpl implements PackageService {
    Logger logger = LoggerFactory.getLogger(getClass());
    @Value("${spring.resources.static-locations:}")
    private String fileLocation;
    @Autowired
    ScriptService scriptService;
    @Autowired
    RestTemplate restTemplate;
    ExecutorService executorService = new ThreadPoolExecutor(10, 15, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    @Value("${server.context-path}")
    public String contextPath;
    @Value("${leader.url}")
    private String leaderUrl;
    @Value("${copy.init.size}")
    private Integer copyInitSize;
    public static Boolean decompressionFlag = false;
    public static String compressionFileName = "";

    @Override
    public void notifyNodes(final String fileName) {
        Collection<NodesServiceInfoVo> nodes = scriptService.getNodes();
        List<Future> futures = new ArrayList<>();
        for (final NodesServiceInfoVo nodesServiceInfoVo : nodes) {
            Future<?> futture = executorService.submit(new Runnable() {
                @Override
                public void run() {
                    String action = "reviveNotify";
                    restTemplate.postForObject(new StringBuilder("http://").append(nodesServiceInfoVo.getHostAddress()).append(":").append(nodesServiceInfoVo.getPort()).append(contextPath).append("/").append(action).toString(), fileName, String.class);
                }
            });
            futures.add(futture);
        }
        for (Future future : futures) {
            try {
                future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void saveFiles(MultipartFile jarFile) {
        long startTime = System.currentTimeMillis();
        try {
            Path path = Paths.get(fileLocation);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
            InputStream inputStream = jarFile.getInputStream();
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            OutputStream outputStream = new FileOutputStream(new File(fileLocation + "/" + jarFile.getOriginalFilename()));
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
            int len;
            byte[] bytes = new byte[copyInitSize];
            while ((len = bufferedInputStream.read(bytes)) > 0) {
                bufferedOutputStream.write(bytes, 0, len);
            }

            bufferedInputStream.close();
            inputStream.close();
            bufferedOutputStream.close();
            outputStream.close();
            logger.info("上传拷贝文件耗时:{}", System.currentTimeMillis() - startTime);
//            Files.write(Paths.get(Paths.get(fileLocation) + "/" + jarFile.getOriginalFilename()), jarFile.getBytes());
            notifyNodes(jarFile.getOriginalFilename());
        } catch (Exception e) {
            logger.error("上传异常fileLocation={}", fileLocation, e);
        }
    }

    @Override
    public void download(String fileName) {
        long startTime = System.currentTimeMillis();
        leaderUrl = leaderUrl.endsWith("/") ? leaderUrl : leaderUrl + "/";
        try {
            Path path = Paths.get(fileLocation + "/temp/");
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
            /**
             * 清空旧文件
             */
            for (File file : path.toFile().listFiles()) {
                deleteOldFiles(file);
            }
            InputStream inputStream = new URL(leaderUrl + fileName).openConnection().getInputStream();
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            OutputStream outputStream = new FileOutputStream(new File(path.toString() + "/" + fileName));
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
            byte[] bytes = new byte[copyInitSize];
            int len;
            while ((len = bufferedInputStream.read(bytes)) > 0) {
                bufferedOutputStream.write(bytes, 0, len);
            }
            bufferedInputStream.close();
            inputStream.close();
            bufferedOutputStream.close();
            outputStream.close();
            compressionFileName = fileName;
            decompressionFlag = false;
            logger.info("下载拷贝文件耗时:{}", System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteOldFiles(File file) {
        if (file.isFile() || file.listFiles() == null || file.listFiles().length == 0) {
            file.delete();
        } else {
            for (File childfile : file.listFiles()) {
                deleteOldFiles(childfile);
            }
            file.delete();
        }
    }

}
