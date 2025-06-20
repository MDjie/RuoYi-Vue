package com.ruoyi.hsc.domain;

import com.ruoyi.common.core.domain.BaseEntity;

public class SysUserAnnotationInfo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long userId;
    private String datasetName;
    private Integer currentIndex;
    private Integer datasetSubSet;
    private Integer relabelRound;

    private String round1Result;
    private String round2Result;
    private String round3Result;

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

    public Integer getRelabelRound() {
        return relabelRound;
    }

    public void setRelabelRound(Integer relabelRound) {
        this.relabelRound = relabelRound;
    }

    public String getRound1Result() {
        return round1Result;
    }

    public void setRound1Result(String round1Result) {
        this.round1Result = round1Result;
    }

    public String getRound2Result() {
        return round2Result;
    }

    public void setRound2Result(String round2Result) {
        this.round2Result = round2Result;
    }

    public String getRound3Result() {
        return round3Result;
    }

    public void setRound3Result(String round3Result) {
        this.round3Result = round3Result;
    }

    @Override
    public String toString() {
        return "SysUserAnnotationInfo{" +
                "userId=" + userId +
                ", datasetName='" + datasetName + '\'' +
                ", currentIndex=" + currentIndex +
                ", datasetSubSet=" + datasetSubSet +
                ", relabelRound=" + relabelRound +
                ", round1Result='" + round1Result + '\'' +
                ", round2Result='" + round2Result + '\'' +
                ", round3Result='" + round3Result + '\'' +
                '}';
    }
} 