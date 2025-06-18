/*
 Navicat Premium Data Transfer

 Source Server         : 4231
 Source Server Type    : MySQL
 Source Server Version : 80037 (8.0.37)
 Source Host           : localhost:3306
 Source Schema         : ry-vue

 Target Server Type    : MySQL
 Target Server Version : 80037 (8.0.37)
 File Encoding         : 65001

 Date: 18/06/2025 16:35:38
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for user_annotation_info
-- ----------------------------
DROP TABLE IF EXISTS `user_annotation_info`;
CREATE TABLE `user_annotation_info`  (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `dataset_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '数据集名称',
  `current_index` int NOT NULL DEFAULT 0 COMMENT '当前标注索引',
  `dataset_sub_set` int NOT NULL COMMENT '数据集子集编号',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`, `dataset_name`, `dataset_sub_set`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户标注信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_annotation_info
-- ----------------------------
INSERT INTO `user_annotation_info` VALUES (100, 'Ag_news', 7, 1, '2025-06-18 10:40:43', '2025-06-18 10:57:14');
INSERT INTO `user_annotation_info` VALUES (100, 'Ag_news', 4, 2, '2025-06-18 10:41:18', '2025-06-18 13:46:09');
INSERT INTO `user_annotation_info` VALUES (100, 'Amazon', 1, 1, '2025-06-18 10:42:41', '2025-06-18 10:42:41');
INSERT INTO `user_annotation_info` VALUES (100, 'empathetic_dialogues', 1, 1, '2025-06-18 11:27:25', '2025-06-18 11:27:25');
INSERT INTO `user_annotation_info` VALUES (100, 'Tweet', 1, 1, '2025-06-18 11:25:27', '2025-06-18 11:25:27');

SET FOREIGN_KEY_CHECKS = 1;
