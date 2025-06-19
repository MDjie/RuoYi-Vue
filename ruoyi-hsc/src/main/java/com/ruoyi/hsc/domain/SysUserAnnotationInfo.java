package com.ruoyi.hsc.domain;

import com.ruoyi.common.core.domain.BaseEntity;

public class SysUserAnnotationInfo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long userId;
    private String datasetName;
    private Integer currentIndex;
    private Integer datasetSubSet;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getDatasetName() {
        return datasetName;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    public Integer getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(Integer currentIndex) {
        this.currentIndex = currentIndex;
    }

    public Integer getDatasetSubSet() {
        return datasetSubSet;
    }

    public void setDatasetSubSet(Integer datasetSubSet) {
        this.datasetSubSet = datasetSubSet;
    }

    @Override
    public String toString() {
        return "SysUserAnnotationInfo{" +
                "userId=" + userId +
                ", datasetName='" + datasetName + '\'' +
                ", currentIndex=" + currentIndex +
                ", datasetSubSet=" + datasetSubSet +
                '}';
    }
} 