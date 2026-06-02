USE `quiz_ai`;

/*Table structure for table `forum_post` */

DROP TABLE IF EXISTS `forum_post`;

CREATE TABLE `forum_post` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '发帖用户ID',
  `title` varchar(120) NOT NULL COMMENT '帖子标题',
  `content` text NOT NULL COMMENT '帖子内容',
  `type` tinyint(1) NOT NULL DEFAULT '0' COMMENT '帖子类型: 0普通帖, 1求助帖',
  `question_id` bigint DEFAULT NULL COMMENT '关联题目ID(仅求助帖)',
  `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '审核状态: 0待审核, 1已通过, 2已拒绝, 3已关闭',
  `like_count` int NOT NULL DEFAULT '0' COMMENT '点赞数',
  `comment_count` int NOT NULL DEFAULT '0' COMMENT '评论数',
  `favorite_count` int NOT NULL DEFAULT '0' COMMENT '收藏数',
  `view_count` int NOT NULL DEFAULT '0' COMMENT '浏览量',
  `reject_reason` varchar(255) DEFAULT NULL COMMENT '拒绝原因',
  `anonymous` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否匿名: 0否, 1是',
  `images` text COMMENT '图片URL列表(JSON数组)',
  `videos` text COMMENT '视频URL列表(JSON数组)',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0未删除, 1已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_type` (`type`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='论坛帖子表';

/*Table structure for table `forum_comment` */

DROP TABLE IF EXISTS `forum_comment`;

CREATE TABLE `forum_comment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `post_id` bigint NOT NULL COMMENT '所属帖子ID',
  `user_id` bigint NOT NULL COMMENT '评论用户ID',
  `content` varchar(1000) NOT NULL COMMENT '评论内容',
  `parent_id` bigint DEFAULT NULL COMMENT '父评论ID(NULL为一级评论)',
  `reply_to_user_id` bigint DEFAULT NULL COMMENT '被回复用户ID',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态: 0隐藏, 1正常',
  `images` text COMMENT '图片URL列表(JSON数组)',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0未删除, 1已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_post_id` (`post_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='论坛评论表';

/*Table structure for table `forum_like` */

DROP TABLE IF EXISTS `forum_like`;

CREATE TABLE `forum_like` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `post_id` bigint NOT NULL COMMENT '帖子ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_post` (`user_id`,`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='论坛点赞表';

/*Table structure for table `forum_favorite` */

DROP TABLE IF EXISTS `forum_favorite`;

CREATE TABLE `forum_favorite` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `post_id` bigint NOT NULL COMMENT '帖子ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_post` (`user_id`,`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='论坛收藏表';

/*Table structure for table `forum_report` */

DROP TABLE IF EXISTS `forum_report`;

CREATE TABLE `forum_report` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '举报用户ID',
  `post_id` bigint NOT NULL COMMENT '被举报帖子ID',
  `reason` varchar(255) NOT NULL COMMENT '举报原因',
  `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '处理状态: 0待处理, 1已驳回, 2已下架',
  `handle_remark` varchar(255) DEFAULT NULL COMMENT '处理备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '举报时间',
  `handle_time` datetime DEFAULT NULL COMMENT '处理时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_post_id` (`post_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='论坛举报表';

/*Table structure for table `sensitive_word` */

DROP TABLE IF EXISTS `sensitive_word`;

CREATE TABLE `sensitive_word` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `word` varchar(50) NOT NULL COMMENT '敏感词',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_word` (`word`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='敏感词表';

-- ============================================================
-- 增量更新（已有数据库执行以下语句）
-- ============================================================
-- ALTER TABLE `forum_post` ADD COLUMN `view_count` int NOT NULL DEFAULT '0' COMMENT '浏览量' AFTER `favorite_count`;
-- ALTER TABLE `forum_post` ADD COLUMN `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0未删除, 1已删除' AFTER `videos`;
-- ALTER TABLE `forum_post` ADD KEY `idx_deleted` (`deleted`);
-- ALTER TABLE `forum_comment` ADD COLUMN `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0未删除, 1已删除' AFTER `images`;
-- ALTER TABLE `forum_comment` ADD KEY `idx_deleted` (`deleted`);
