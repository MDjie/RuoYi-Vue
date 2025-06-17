package com.ruoyi.system.consts;

import com.alibaba.fastjson2.JSONObject;

/**
 * 数据集配置常量
 */
public class DatasetConfig {
    
    /**
     * 数据集配置信息
     */
    public static final JSONObject DATASET_CONFIG;
    
    static {
        String configJson = "{" +
                "\"Ag_news\": {" +
                "\"accuracy\": 85," +
                "\"0\": \"环球时报\"," +
                "\"1\": \"体育新闻\"," +
                "\"2\": \"金融、经济\"," +
                "\"3\": \"科学、科技\"" +
                "}," +
                "\"Amazon\": {" +
                "\"accuracy\": 85" +
                "}," +
                "\"Civil_Comment\": {" +
                "\"0\": \"不具攻击性\"," +
                "\"1\": \"具有攻击性（粗鲁、不尊重或不合理的评论，可能导致你退出讨论或放弃表达观点）\"" +
                "}," +
                "\"empathetic_dialogues\": {" +
                "\"accuracy\": 85" +
                "}," +
                "\"IMDB\": {" +
                "\"accuracy\": 85," +
                "\"0\": \"差评\"," +
                "\"1\": \"好评\"" +
                "}," +
                "\"rotten\": {" +
                "\"accuracy\": 85," +
                "\"0\": \"Negative\"," +
                "\"1\": \"Positive\"" +
                "}," +
                "\"Tweet\": {" +
                "\"accuracy\": 85" +
                "}," +
                "\"Yahoo\": {" +
                "\"accuracy\": 85," +
                "\"0\": \"社会与文化\"," +
                "\"1\": \"科学与数学\"," +
                "\"2\": \"健康\"," +
                "\"3\": \"教育及参考资料（参考资料主要指用于查阅、辅助学习的资料或工具）\"," +
                "\"4\": \"计算机与互联网\"," +
                "\"5\": \"体育\"," +
                "\"6\": \"商业与金融\"," +
                "\"7\": \"娱乐与音乐\"," +
                "\"8\": \"家庭与关系\"," +
                "\"9\": \"政治与政府\"" +
                "}" +
                "}";
        DATASET_CONFIG = JSONObject.parseObject(configJson);
    }
} 