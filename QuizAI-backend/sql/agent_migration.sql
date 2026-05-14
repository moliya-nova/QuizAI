-- ========================================
-- QuizAI Agent 模块数据库迁移脚本
-- 添加 AI 模型预设配置表
-- ========================================

USE `quiz_ai`;

-- ----------------------------
-- Table structure for ai_model_preset
-- ----------------------------
DROP TABLE IF EXISTS `ai_model_preset`;
CREATE TABLE `ai_model_preset`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模型显示名称',
  `provider` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'openai' COMMENT '推荐提供商: openai/anthropic/ollama',
  `protocol` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'openai' COMMENT '当前使用的协议: openai/anthropic/ollama',
  `openai_model` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT 'OpenAI协议模型名称',
  `openai_base_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT 'OpenAI协议API地址',
  `anthropic_model` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT 'Anthropic协议模型名称',
  `anthropic_base_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT 'Anthropic协议API地址',
  `ollama_model` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT 'Ollama协议模型名称',
  `ollama_base_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'http://localhost:11434' COMMENT 'Ollama协议API地址',
  `api_key` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '独立API Key',
  `icon` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '图标标识',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '0' COMMENT '状态: 0=正常 1=停用',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI模型预设配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ai_model_preset
-- ----------------------------
INSERT INTO `ai_model_preset` VALUES (1, 'GPT-4o', 'openai', 'openai', 'gpt-4o', 'https://api.openai.com/v1', '', '', '', 'http://localhost:11434', '', 'openai', 10, '0', '', '2026-05-11 13:37:33', '', '2026-05-11 13:37:33', 'OpenAI 旗舰多模态模型');
INSERT INTO `ai_model_preset` VALUES (2, 'GPT-4o Mini', 'openai', 'openai', 'gpt-4o-mini', 'https://api.openai.com/v1', '', '', '', 'http://localhost:11434', '', 'openai', 20, '0', '', '2026-05-11 13:37:33', '', '2026-05-11 13:37:33', 'OpenAI 轻量模型，性价比高');
INSERT INTO `ai_model_preset` VALUES (3, 'Claude Sonnet 4', 'anthropic', 'anthropic', '', '', 'claude-sonnet-4-20250514', '', '', 'http://localhost:11434', '', 'anthropic', 30, '0', '', '2026-05-11 13:37:33', '', '2026-05-11 13:37:33', 'Anthropic 最新高性能模型');
INSERT INTO `ai_model_preset` VALUES (4, 'Claude Haiku 3.5', 'anthropic', 'anthropic', '', '', 'claude-haiku-4-5-20251001', '', '', 'http://localhost:11434', '', 'anthropic', 40, '0', '', '2026-05-11 13:37:33', '', '2026-05-11 13:37:33', 'Anthropic 快速轻量模型');
INSERT INTO `ai_model_preset` VALUES (5, 'DeepSeek V3', 'openai', 'openai', 'deepseek-chat', 'https://api.deepseek.com/v1', '', '', '', 'http://localhost:11434', '', 'deepseek', 50, '0', '', '2026-05-11 13:37:33', '', '2026-05-11 13:37:33', 'DeepSeek 通用对话模型');
INSERT INTO `ai_model_preset` VALUES (6, 'DeepSeek R1', 'openai', 'openai', 'deepseek-reasoner', 'https://api.deepseek.com/v1', '', '', '', 'http://localhost:11434', '', 'deepseek', 55, '0', '', '2026-05-11 13:37:33', '', '2026-05-11 13:37:33', 'DeepSeek 推理模型');
INSERT INTO `ai_model_preset` VALUES (7, '通义千问 Max', 'openai', 'openai', 'qwen-max', 'https://dashscope.aliyuncs.com/compatible-mode/v1', '', '', '', 'http://localhost:11434', '', 'qwen', 60, '0', '', '2026-05-11 13:37:33', '', '2026-05-11 13:37:33', '阿里云通义千问旗舰模型');
INSERT INTO `ai_model_preset` VALUES (8, '通义千问 Plus', 'openai', 'openai', 'qwen-plus', 'https://dashscope.aliyuncs.com/compatible-mode/v1', '', '', '', 'http://localhost:11434', '', 'qwen', 70, '0', '', '2026-05-11 13:37:33', '', '2026-05-11 13:37:33', '阿里云通义千问均衡模型');
INSERT INTO `ai_model_preset` VALUES (9, '智谱 GLM-4', 'openai', 'openai', 'glm-4', 'https://open.bigmodel.cn/api/paas/v4', '', '', '', 'http://localhost:11434', '', 'zhipu', 80, '0', '', '2026-05-11 13:37:33', '', '2026-05-11 13:37:33', '智谱AI GLM-4 旗舰模型');
INSERT INTO `ai_model_preset` VALUES (10, 'Kimi', 'openai', 'openai', 'moonshot-v1-8k', 'https://api.moonshot.cn/v1', '', '', '', 'http://localhost:11434', '', 'kimi', 90, '0', '', '2026-05-11 13:37:33', '', '2026-05-11 13:37:33', '月之暗面 Kimi 模型');
INSERT INTO `ai_model_preset` VALUES (11, '文心一言 4.0', 'openai', 'openai', 'ernie-4.0-8k', 'https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop', '', '', '', 'http://localhost:11434', '', 'baidu', 100, '0', '', '2026-05-11 13:37:33', '', '2026-05-11 13:37:33', '百度文心一言旗舰模型');
INSERT INTO `ai_model_preset` VALUES (12, '豆包', 'openai', 'openai', 'doubao-pro-32k', 'https://ark.cn-beijing.volces.com/api/v3', '', '', '', 'http://localhost:11434', '', 'doubao', 110, '0', '', '2026-05-11 13:37:33', '', '2026-05-11 13:37:33', '字节跳动豆包模型');
INSERT INTO `ai_model_preset` VALUES (13, '讯飞星火 Max', 'openai', 'openai', 'generalv3.5', 'https://spark-api-open.xf-yun.com/v1', '', '', '', 'http://localhost:11434', '', 'spark', 120, '0', '', '2026-05-11 13:37:33', '', '2026-05-11 13:37:33', '科大讯飞星火大模型');
INSERT INTO `ai_model_preset` VALUES (14, 'Qwen2.5 7B (本地)', 'ollama', 'ollama', '', '', '', '', 'qwen2.5:7b', 'http://localhost:11434', '', 'ollama', 130, '0', '', '2026-05-11 13:37:33', '', '2026-05-11 13:37:33', 'Ollama 本地部署的通义千问 7B');
INSERT INTO `ai_model_preset` VALUES (15, 'Llama3.1 8B (本地)', 'ollama', 'ollama', '', '', '', '', 'llama3.1:8b', 'http://localhost:11434', '', 'ollama', 140, '0', '', '2026-05-11 13:37:33', '', '2026-05-11 13:37:33', 'Ollama 本地部署的 Llama 3.1 8B');
