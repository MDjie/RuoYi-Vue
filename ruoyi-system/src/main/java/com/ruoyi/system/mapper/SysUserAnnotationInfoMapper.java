package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.SysUserAnnotationInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysUserAnnotationInfoMapper {
    
    /**
     * 根据用户ID查询所有标注信息
     */
    List<SysUserAnnotationInfo> selectByUserId(@Param("userId") Long userId);
    
    /**
     * 根据用户ID、数据集名称和子集编号查询标注信息
     */
    SysUserAnnotationInfo selectByUserAndDataset(@Param("userId") Long userId, 
                                                @Param("datasetName") String datasetName, 
                                                @Param("datasetSubSet") Integer datasetSubSet);
    
    /**
     * 更新用户标注信息
     */
    int updateAnnotationInfo(SysUserAnnotationInfo info);
    
    /**
     * 插入用户标注信息
     */
    int insertAnnotationInfo(SysUserAnnotationInfo info);
}
