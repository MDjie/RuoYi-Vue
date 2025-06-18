/*
 用户标注信息表结构迁移脚本 V2
 将单主键(user_id)改为复合主键(user_id, dataset_name, dataset_sub_set)
*/

-- 1. 创建临时表保存现有数据
CREATE TABLE `user_annotation_info_temp` AS 
SELECT * FROM `user_annotation_info`;

-- 2. 删除原表
DROP TABLE IF EXISTS `user_annotation_info`;

-- 3. 创建新表结构
CREATE TABLE `user_annotation_info`  (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `dataset_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '数据集名称',
  `current_index` int NOT NULL DEFAULT 0 COMMENT '当前标注索引',
  `dataset_sub_set` int NOT NULL COMMENT '数据集子集编号',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`, `dataset_name`, `dataset_sub_set`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户标注信息表' ROW_FORMAT = Dynamic;

-- 4. 恢复数据（如果原表中有数据）
INSERT INTO `user_annotation_info` (`user_id`, `dataset_name`, `current_index`, `dataset_sub_set`, `create_time`, `update_time`)
SELECT 
  `user_id`, 
  `dataset_name`, 
  `current_index`, 
  `dataset_sub_set`,
  CURRENT_TIMESTAMP as `create_time`,
  CURRENT_TIMESTAMP as `update_time`
FROM `user_annotation_info_temp`;

-- 5. 删除临时表
DROP TABLE `user_annotation_info_temp`;

-- 6. 验证迁移结果
SELECT 'Migration completed successfully!' as status;
SELECT COUNT(*) as total_records FROM `user_annotation_info`;

-- 7. 显示迁移后的数据
SELECT * FROM `user_annotation_info`; 