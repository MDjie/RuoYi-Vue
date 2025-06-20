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

 Date: 20/06/2025 16:51:37
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
  `relabel_round` int NULL DEFAULT 1,
  `round_1_result` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `round_2_result` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `round_3_result` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`user_id`, `dataset_name`, `dataset_sub_set`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户标注信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_annotation_info
-- ----------------------------
INSERT INTO `user_annotation_info` VALUES (100, 'Ag_news', 1, 2, '2025-06-18 10:41:18', '2025-06-20 16:51:06', 1, NULL, NULL, NULL);
INSERT INTO `user_annotation_info` VALUES (100, 'Ag_news', 1, 10, '2025-06-18 10:40:43', '2025-06-20 16:51:19', 1, '\n评估结果:\n总问题数: 20\n匹配到参考答案的问题数: 20\n准确率(Accuracy): 0.7000\n精确率(Precision): 0.7639\n召回率(Recall): 0.7000\nF1分数: 0.6994\n\n准确率要求: 85%\n很遗憾，您未达到准确率要求。\n', '\n评估结果:\n总问题数: 20\n匹配到参考答案的问题数: 20\n准确率(Accuracy): 0.8000\n精确率(Precision): 0.8889\n召回率(Recall): 0.8000\nF1分数: 0.8036\n\n准确率要求: 85%\n很遗憾，您未达到准确率要求。\n', '\n评估结果:\n总问题数: 20\n匹配到参考答案的问题数: 20\n准确率(Accuracy): 0.8500\n精确率(Precision): 0.8869\n召回率(Recall): 0.8500\nF1分数: 0.8453\n\n准确率要求: 85%\n恭喜！您已通过准确率要求！\n');
INSERT INTO `user_annotation_info` VALUES (100, 'Ag_news', 1, 100, '2025-06-19 10:15:02', '2025-06-20 16:51:08', 1, NULL, NULL, NULL);
INSERT INTO `user_annotation_info` VALUES (100, 'Amazon', 1, 1, '2025-06-18 10:42:41', '2025-06-20 16:51:08', 1, NULL, NULL, NULL);
INSERT INTO `user_annotation_info` VALUES (100, 'empathetic_dialogues', 1, 1, '2025-06-18 11:27:25', '2025-06-20 16:51:09', 1, NULL, NULL, NULL);
INSERT INTO `user_annotation_info` VALUES (100, 'Tweet', 1, 1, '2025-06-18 11:25:27', '2025-06-20 16:51:12', 1, NULL, NULL, NULL);

SET FOREIGN_KEY_CHECKS = 1;
