package com.ruoyi.system.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class RedisDatasetService {

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    private static final String DATASET_KEY_PREFIX = "dataset:";
    private static final String DATASET_INDEX_KEY_PREFIX = "dataset_index:";

    /**
     * 应用启动时初始化数据集到Redis
     */
    @PostConstruct
    public void init() {
        try {
            // 获取datasets目录下的所有文件
            ClassPathResource resource = new ClassPathResource("datasets");
            if (resource.exists()) {
                String[] files = resource.getFile().list();
                if (files != null) {
                    for (String file : files) {
                        if (file.endsWith(".json")) {
                            loadDatasetToRedis(file);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 加载数据集到Redis
     */
    private void loadDatasetToRedis(String fileName) {
        try {
            ClassPathResource resource = new ClassPathResource("datasets/" + fileName);
            List<JSONObject> jsonList = new ArrayList<>();
            
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonList.add(JSON.parseObject(line));
                }
            }

            // 将每个JSON对象单独存入Redis
            for (int i = 0; i < jsonList.size(); i++) {
                String key = DATASET_KEY_PREFIX + fileName + ":" + i;
                redisTemplate.opsForValue().set(key, jsonList.get(i));
            }

            // 存储数据集大小
            String sizeKey = DATASET_INDEX_KEY_PREFIX + fileName;
            redisTemplate.opsForValue().set(sizeKey, jsonList.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取数据集中的某一行
     */
    public JSONObject getDatasetLine(String fileName, int index) {
        String key = DATASET_KEY_PREFIX + fileName + ":" + index;
        return (JSONObject) redisTemplate.opsForValue().get(key);
    }

    /**
     * 获取数据集大小
     */
    public Integer getDatasetSize(String fileName) {
        String key = DATASET_INDEX_KEY_PREFIX + fileName;
        return (Integer) redisTemplate.opsForValue().get(key);
    }

    /**
     * 更新数据集中的某一行
     */
    public void updateDatasetLine(String fileName, int index, JSONObject newLine) {
        String key = DATASET_KEY_PREFIX + fileName + ":" + index;
        redisTemplate.opsForValue().set(key, newLine);
    }

    /**
     * 导出标注数据到JSON文件
     * @param fileName 原始文件名
     * @return 新文件名
     */
    public String exportAnnotatedData(String fileName) {
        try {
            // 获取数据集大小
            Integer size = getDatasetSize(fileName);
            if (size == null) {
                throw new RuntimeException("数据集不存在");
            }

            // 按顺序读取所有数据
            List<JSONObject> annotatedData = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                JSONObject line = getDatasetLine(fileName, i);
                if (line != null) {
                    annotatedData.add(line);
                }
            }

            // 生成新文件名
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String newFileName = fileName.substring(0, fileName.lastIndexOf(".")) + "_" + timestamp + ".json";

            // 确保输出目录存在
            File outputDir = new File("annotated_datasets");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            // 写入文件
            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(new File(outputDir, newFileName)),
                            StandardCharsets.UTF_8))) {
                for (JSONObject data : annotatedData) {
                    writer.write(data.toJSONString());
                    writer.newLine();
                }
            }

            return newFileName;
        } catch (Exception e) {
            throw new RuntimeException("导出标注数据失败: " + e.getMessage(), e);
        }
    }
} 