CREATE TABLE IF NOT EXISTS SPRING_AI_CHAT_MEMORY (
    conversation_id VARCHAR(64) NOT NULL,
    content TEXT NOT NULL,
    type VARCHAR(10) NOT NULL,
    `timestamp` TIMESTAMP NOT NULL,
    INDEX SPRING_AI_CHAT_MEMORY_CONVERSATION_ID_TIMESTAMP_IDX (conversation_id, `timestamp`),
    CONSTRAINT SPRING_AI_CHAT_MEMORY_TYPE_CHECK CHECK (type IN ('USER', 'ASSISTANT', 'SYSTEM', 'TOOL'))
);

CREATE TABLE IF NOT EXISTS ai_chat_session (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    conversation_id VARCHAR(64) NOT NULL COMMENT 'AI会话ID',
    title VARCHAR(80) NOT NULL COMMENT '会话标题',
    last_message VARCHAR(256) DEFAULT NULL COMMENT '最后一条消息摘要',
    message_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '消息数量',
    create_time DATETIME DEFAULT NULL COMMENT '创建时间',
    update_time DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_ai_chat_session_conversation_id (conversation_id),
    KEY idx_ai_chat_session_user_update_time (user_id, update_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI对话会话';

CREATE TABLE IF NOT EXISTS ai_chat_message (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    session_id BIGINT UNSIGNED NOT NULL COMMENT 'AI会话表ID',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    conversation_id VARCHAR(64) NOT NULL COMMENT 'AI会话ID',
    role VARCHAR(20) NOT NULL COMMENT '消息角色;user assistant',
    content LONGTEXT NOT NULL COMMENT '消息内容',
    create_time DATETIME DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_ai_chat_message_session_id (session_id, id),
    KEY idx_ai_chat_message_user_conversation (user_id, conversation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI对话消息';
