-- ============================================================
-- AI心理平台 完整数据库初始化脚本
-- 库名：ai_mental_platform
-- 字符集：utf8mb4 / utf8mb4_unicode_ci
-- 引擎：InnoDB
-- 说明：
--   1. 基线为 前文.txt 中的 15 张表 DDL（字段/类型/默认值/索引/外键保持一致）；
--   2. 另按实体类补齐 5 张表：course_section、content_audit_record、
--      sys_role、sys_permission、sys_role_permission；
--   3. 实体类中多出、而原 DDL 缺少的字段，已按实体补齐：
--      ai_session.is_deleted、crisis_record.handle_remark/handle_time、
--      study_progress.study_duration；
--   4. 所有表统一逻辑删除字段 is_deleted TINYINT NOT NULL DEFAULT 0；
--   5. 本脚本可重复执行：先关外键检查，DROP 后重建表并重灌种子数据。
-- 执行方式：
--   mysql --default-character-set=utf8mb4 -uroot -p123456 < init_full.sql
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS ai_mental_platform
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE ai_mental_platform;

-- ============================================================
-- 一、删除旧表（按依赖倒序，配合 FOREIGN_KEY_CHECKS=0 可任意顺序）
-- ============================================================
DROP TABLE IF EXISTS sys_role_permission;
DROP TABLE IF EXISTS sys_permission;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS system_notice;
DROP TABLE IF EXISTS user_feedback;
DROP TABLE IF EXISTS user_scale_record;
DROP TABLE IF EXISTS study_progress;
DROP TABLE IF EXISTS daily_checkin;
DROP TABLE IF EXISTS crisis_record;
DROP TABLE IF EXISTS content_audit_record;
DROP TABLE IF EXISTS chat_message;
DROP TABLE IF EXISTS ai_session;
DROP TABLE IF EXISTS user_badge;
DROP TABLE IF EXISTS badge;
DROP TABLE IF EXISTS course_section;
DROP TABLE IF EXISTS study_course;
DROP TABLE IF EXISTS scale_question;
DROP TABLE IF EXISTS mental_scale;
DROP TABLE IF EXISTS `user`;
DROP TABLE IF EXISTS admin;

-- ============================================================
-- 二、建表（按依赖正序）
-- ============================================================

-- ------------------------------------------------------------
-- 1. admin 系统管理员表  <->  com.aimental.entity.Admin
-- ------------------------------------------------------------
CREATE TABLE admin (
    admin_id     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
    username     VARCHAR(50)  NOT NULL                COMMENT '管理员账号',
    password     VARCHAR(128) NOT NULL                COMMENT '密码哈希',
    admin_name   VARCHAR(50)  NULL                    COMMENT '管理员姓名',
    create_time  DATETIME     NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    last_login   DATETIME     NULL                    COMMENT '最后登录时间',
    is_deleted   TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除标记：0未删，1已删',
    PRIMARY KEY (admin_id),
    UNIQUE KEY username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统管理员表';

-- ------------------------------------------------------------
-- 2. user 普通用户表  <->  com.aimental.entity.User
-- ------------------------------------------------------------
CREATE TABLE `user` (
    user_id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    phone           VARCHAR(128) NULL                    COMMENT '手机号（加密存储）',
    email           VARCHAR(128) NULL                    COMMENT '邮箱（加密存储）',
    password        VARCHAR(128) NOT NULL                COMMENT 'bcrypt密码哈希',
    nickname        VARCHAR(50)  NOT NULL                COMMENT '昵称',
    avatar_url      VARCHAR(255) NULL                    COMMENT '头像URL',
    register_time   DATETIME     NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    last_login_time DATETIME     NULL                    COMMENT '最后登录时间',
    account_status  TINYINT      NULL DEFAULT 1          COMMENT '账号状态：0禁用，1正常',
    real_auth_flag  TINYINT      NULL DEFAULT 0          COMMENT '实名认证标记：0未认证，1已认证',
    total_points    INT          NULL DEFAULT 0          COMMENT '总积分',
    user_level      INT          NULL DEFAULT 1          COMMENT '用户等级',
    create_time     DATETIME     NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME     NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted      TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除标记：0未删，1已删',
    PRIMARY KEY (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '普通用户表';

-- ------------------------------------------------------------
-- 3. mental_scale 心理评估量表表  <->  com.aimental.entity.MentalScale
-- ------------------------------------------------------------
CREATE TABLE mental_scale (
    scale_id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '量表ID',
    scale_name          VARCHAR(100) NOT NULL                COMMENT '量表名称',
    scale_code          VARCHAR(50)  NOT NULL                COMMENT '量表编码',
    dimension_desc      TEXT         NULL                    COMMENT '维度说明',
    total_question_num  INT          NOT NULL                COMMENT '总题目数',
    reliability         DECIMAL(4,3) NULL                    COMMENT '公开信度系数',
    status              TINYINT      NULL DEFAULT 1          COMMENT '启用状态：0停用，1启用',
    create_time         DATETIME     NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    is_deleted          TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除标记：0未删，1已删',
    PRIMARY KEY (scale_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '心理评估量表表';

-- ------------------------------------------------------------
-- 4. scale_question 评估题目表  <->  com.aimental.entity.ScaleQuestion
-- ------------------------------------------------------------
CREATE TABLE scale_question (
    question_id      BIGINT NOT NULL AUTO_INCREMENT COMMENT '题目ID',
    scale_id         BIGINT NOT NULL                COMMENT '所属量表ID',
    question_content TEXT   NOT NULL                COMMENT '题目内容',
    option_json      TEXT   NULL                    COMMENT '选项JSON',
    score_rule       TEXT   NULL                    COMMENT '计分规则',
    sort_num         INT    NULL DEFAULT 0          COMMENT '排序号',
    is_deleted       TINYINT NOT NULL DEFAULT 0      COMMENT '逻辑删除标记：0未删，1已删',
    PRIMARY KEY (question_id),
    KEY scale_id (scale_id),
    CONSTRAINT scale_question_ibfk_1 FOREIGN KEY (scale_id) REFERENCES mental_scale (scale_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '评估题目表';

-- ------------------------------------------------------------
-- 5. study_course 心理学习课程表  <->  com.aimental.entity.StudyCourse
-- ------------------------------------------------------------
CREATE TABLE study_course (
    course_id     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '课程ID',
    course_name   VARCHAR(100) NOT NULL                COMMENT '课程名称',
    course_desc   TEXT         NULL                    COMMENT '课程简介',
    cover_url     VARCHAR(255) NULL                    COMMENT '封面图',
    total_section INT          NULL                    COMMENT '小节总数',
    status        TINYINT      NULL DEFAULT 1          COMMENT '0下架，1正常',
    create_time   DATETIME     NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    is_deleted    TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除标记：0未删，1已删',
    PRIMARY KEY (course_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '心理学习课程表';

-- ------------------------------------------------------------
-- 6. course_section 课程小节表（实体 course_section，原 DDL 未包含）
--    <->  com.aimental.entity.CourseSection
-- ------------------------------------------------------------
CREATE TABLE course_section (
    section_id      BIGINT       NOT NULL AUTO_INCREMENT COMMENT '小节ID',
    course_id       BIGINT       NOT NULL                COMMENT '所属课程ID',
    section_title   VARCHAR(150) NOT NULL                COMMENT '小节标题',
    section_content TEXT         NULL                    COMMENT '小节内容',
    study_minutes   INT          NULL DEFAULT 10         COMMENT '建议学习时长(分钟)',
    sort_num        INT          NULL DEFAULT 0          COMMENT '排序号',
    create_time     DATETIME     NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    is_deleted      TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除标记：0未删，1已删',
    PRIMARY KEY (section_id),
    KEY idx_section_course (course_id),
    CONSTRAINT course_section_ibfk_1 FOREIGN KEY (course_id) REFERENCES study_course (course_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '课程小节表';

-- ------------------------------------------------------------
-- 7. badge 成就徽章表  <->  com.aimental.entity.Badge
-- ------------------------------------------------------------
CREATE TABLE badge (
    badge_id         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '徽章ID',
    badge_name       VARCHAR(80) NOT NULL                COMMENT '徽章名称',
    badge_icon       VARCHAR(255) NULL                   COMMENT '图标地址',
    obtain_condition TEXT        NULL                    COMMENT '解锁条件',
    description      TEXT        NULL                    COMMENT '徽章说明',
    is_deleted       TINYINT     NOT NULL DEFAULT 0      COMMENT '逻辑删除标记：0未删，1已删',
    PRIMARY KEY (badge_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '成就徽章表';

-- ------------------------------------------------------------
-- 8. user_badge 用户获得徽章关联表  <->  com.aimental.entity.UserBadge
-- ------------------------------------------------------------
CREATE TABLE user_badge (
    ub_id       BIGINT   NOT NULL AUTO_INCREMENT COMMENT '关联记录ID',
    user_id     BIGINT   NOT NULL                COMMENT '用户ID',
    badge_id    BIGINT   NOT NULL                COMMENT '徽章ID',
    obtain_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP COMMENT '获得时间',
    is_deleted  TINYINT  NOT NULL DEFAULT 0      COMMENT '逻辑删除标记：0未删，1已删',
    PRIMARY KEY (ub_id),
    UNIQUE KEY uk_user_badge (user_id, badge_id),
    KEY badge_id (badge_id),
    CONSTRAINT user_badge_ibfk_1 FOREIGN KEY (user_id) REFERENCES `user` (user_id),
    CONSTRAINT user_badge_ibfk_2 FOREIGN KEY (badge_id) REFERENCES badge (badge_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户获得徽章关联表';

-- ------------------------------------------------------------
-- 9. ai_session AI对话会话表  <->  com.aimental.entity.AiSession
--    实体同时保留 del_flag 与 is_deleted，两者均建
-- ------------------------------------------------------------
CREATE TABLE ai_session (
    session_id   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '会话ID',
    user_id      BIGINT       NOT NULL                COMMENT '所属用户ID',
    session_name VARCHAR(100) NULL                    COMMENT '会话自定义名称',
    ai_style     VARCHAR(50)  NULL                    COMMENT 'AI对话风格',
    create_time  DATETIME     NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME     NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    del_flag     TINYINT      NULL DEFAULT 0          COMMENT '逻辑删除标记：0未删，1已删（历史字段）',
    is_deleted   TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除标记：0未删，1已删',
    PRIMARY KEY (session_id),
    KEY user_id (user_id),
    CONSTRAINT ai_session_ibfk_1 FOREIGN KEY (user_id) REFERENCES `user` (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI对话会话表';

-- ------------------------------------------------------------
-- 10. chat_message 对话消息表  <->  com.aimental.entity.ChatMessage
-- ------------------------------------------------------------
CREATE TABLE chat_message (
    msg_id       BIGINT   NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    session_id   BIGINT   NOT NULL                COMMENT '所属会话ID',
    sender_type  TINYINT  NOT NULL                COMMENT '发送方类型：0用户，1AI',
    content      TEXT     NOT NULL                COMMENT '消息内容AES256加密存储',
    send_time    DATETIME NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    audit_status TINYINT  NULL DEFAULT 0          COMMENT '内容审核状态：0待审，1通过，2不通过',
    violate_flag TINYINT  NULL DEFAULT 0          COMMENT '违规标记：0正常，1违规',
    is_deleted   TINYINT  NOT NULL DEFAULT 0      COMMENT '逻辑删除标记：0未删，1已删',
    PRIMARY KEY (msg_id),
    KEY session_id (session_id),
    CONSTRAINT chat_message_ibfk_1 FOREIGN KEY (session_id) REFERENCES ai_session (session_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '对话消息表';

-- ------------------------------------------------------------
-- 11. content_audit_record 内容审核记录表（实体 content_audit_record，原 DDL 未包含）
--     <->  com.aimental.entity.ContentAuditRecord
-- ------------------------------------------------------------
CREATE TABLE content_audit_record (
    audit_id     BIGINT   NOT NULL AUTO_INCREMENT COMMENT '审核记录ID',
    msg_id       BIGINT   NOT NULL                COMMENT '被审核消息ID',
    session_id   BIGINT   NOT NULL                COMMENT '消息所属会话ID',
    audit_result TINYINT  NOT NULL                COMMENT '审核结果：1通过，2不通过',
    violate_flag TINYINT  NULL DEFAULT 0          COMMENT '违规标记：0正常，1违规',
    audit_remark TEXT     NULL                    COMMENT '审核备注',
    auditor_id   BIGINT   NULL                    COMMENT '审核人（admin表ID）',
    audit_time   DATETIME NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
    is_deleted   TINYINT  NOT NULL DEFAULT 0      COMMENT '逻辑删除标记：0未删，1已删',
    PRIMARY KEY (audit_id),
    KEY idx_audit_msg (msg_id),
    KEY idx_audit_session (session_id),
    KEY idx_audit_admin (auditor_id),
    CONSTRAINT content_audit_ibfk_1 FOREIGN KEY (msg_id) REFERENCES chat_message (msg_id),
    CONSTRAINT content_audit_ibfk_2 FOREIGN KEY (session_id) REFERENCES ai_session (session_id),
    CONSTRAINT content_audit_ibfk_3 FOREIGN KEY (auditor_id) REFERENCES admin (admin_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '内容审核记录表';

-- ------------------------------------------------------------
-- 12. crisis_record AI消息风险危机记录表  <->  com.aimental.entity.CrisisRecord
--     实体多出 handle_remark、handle_time，已补齐
-- ------------------------------------------------------------
CREATE TABLE crisis_record (
    crisis_id     BIGINT   NOT NULL AUTO_INCREMENT COMMENT '危机记录ID',
    user_id       BIGINT   NOT NULL                COMMENT '用户ID',
    msg_id        BIGINT   NULL                    COMMENT '关联消息ID',
    risk_level    TINYINT  NULL                    COMMENT '风险等级1-3',
    risk_desc     TEXT     NULL                    COMMENT '风险描述',
    handle_status TINYINT  NULL DEFAULT 0          COMMENT '0待处理，1已处理',
    handle_remark TEXT     NULL                    COMMENT '处理备注',
    handle_time   DATETIME NULL                    COMMENT '处理时间',
    create_time   DATETIME NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    is_deleted    TINYINT  NOT NULL DEFAULT 0      COMMENT '逻辑删除标记：0未删，1已删',
    PRIMARY KEY (crisis_id),
    KEY user_id (user_id),
    CONSTRAINT crisis_record_ibfk_1 FOREIGN KEY (user_id) REFERENCES `user` (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI消息风险危机记录表';

-- ------------------------------------------------------------
-- 13. daily_checkin 每日打卡表  <->  com.aimental.entity.DailyCheckin
-- ------------------------------------------------------------
CREATE TABLE daily_checkin (
    checkin_id   BIGINT   NOT NULL AUTO_INCREMENT COMMENT '打卡ID',
    user_id      BIGINT   NOT NULL                COMMENT '用户ID',
    checkin_date DATE     NOT NULL                COMMENT '打卡日期',
    mood_desc    TEXT     NULL                    COMMENT '心情描述',
    create_time  DATETIME NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    is_deleted   TINYINT  NOT NULL DEFAULT 0      COMMENT '逻辑删除标记：0未删，1已删',
    PRIMARY KEY (checkin_id),
    UNIQUE KEY uk_user_date (user_id, checkin_date),
    CONSTRAINT daily_checkin_ibfk_1 FOREIGN KEY (user_id) REFERENCES `user` (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '每日打卡表';

-- ------------------------------------------------------------
-- 14. study_progress 用户学习进度表  <->  com.aimental.entity.StudyProgress
--     实体多出 study_duration，已补齐
-- ------------------------------------------------------------
CREATE TABLE study_progress (
    progress_id      BIGINT       NOT NULL AUTO_INCREMENT COMMENT '进度ID',
    user_id          BIGINT       NOT NULL                COMMENT '用户ID',
    course_id        BIGINT       NOT NULL                COMMENT '课程ID',
    finished_section INT          NULL DEFAULT 0          COMMENT '已完成小节',
    finish_rate      DECIMAL(5,2) NULL DEFAULT 0.00       COMMENT '完成百分比',
    study_duration   INT          NULL DEFAULT 0          COMMENT '累计学习时长(秒)',
    last_study_time  DATETIME     NULL                    COMMENT '最后学习时间',
    is_deleted       TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除标记：0未删，1已删',
    PRIMARY KEY (progress_id),
    UNIQUE KEY uk_user_course (user_id, course_id),
    KEY course_id (course_id),
    CONSTRAINT study_progress_ibfk_1 FOREIGN KEY (user_id) REFERENCES `user` (user_id),
    CONSTRAINT study_progress_ibfk_2 FOREIGN KEY (course_id) REFERENCES study_course (course_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户学习进度表';

-- ------------------------------------------------------------
-- 15. user_scale_record 用户心理评估记录表  <->  com.aimental.entity.UserScaleRecord
-- ------------------------------------------------------------
CREATE TABLE user_scale_record (
    record_id       BIGINT   NOT NULL AUTO_INCREMENT COMMENT '评估记录ID',
    user_id         BIGINT   NOT NULL                COMMENT '用户ID',
    scale_id        BIGINT   NOT NULL                COMMENT '量表ID',
    answer_json     TEXT     NOT NULL                COMMENT '用户作答JSON',
    total_score     INT      NOT NULL                COMMENT '总分',
    evaluate_result TEXT     NULL                    COMMENT '评估解读结果',
    finish_time     DATETIME NULL DEFAULT CURRENT_TIMESTAMP COMMENT '完成时间',
    is_deleted      TINYINT  NOT NULL DEFAULT 0      COMMENT '逻辑删除标记：0未删，1已删',
    PRIMARY KEY (record_id),
    KEY user_id (user_id),
    KEY scale_id (scale_id),
    CONSTRAINT user_scale_record_ibfk_1 FOREIGN KEY (user_id) REFERENCES `user` (user_id),
    CONSTRAINT user_scale_record_ibfk_2 FOREIGN KEY (scale_id) REFERENCES mental_scale (scale_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户心理评估记录表';

-- ------------------------------------------------------------
-- 16. user_feedback 用户反馈表  <->  com.aimental.entity.UserFeedback
-- ------------------------------------------------------------
CREATE TABLE user_feedback (
    feedback_id      BIGINT   NOT NULL AUTO_INCREMENT COMMENT '反馈ID',
    user_id          BIGINT   NOT NULL                COMMENT '用户ID',
    feedback_content TEXT     NOT NULL                COMMENT '反馈内容',
    status           TINYINT  NULL DEFAULT 0          COMMENT '0待处理，1已回复，2关闭',
    reply_content    TEXT     NULL                    COMMENT '管理员回复',
    create_time      DATETIME NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time      DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted       TINYINT  NOT NULL DEFAULT 0      COMMENT '逻辑删除标记：0未删，1已删',
    PRIMARY KEY (feedback_id),
    KEY user_id (user_id),
    CONSTRAINT user_feedback_ibfk_1 FOREIGN KEY (user_id) REFERENCES `user` (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户反馈表';

-- ------------------------------------------------------------
-- 17. system_notice 系统公告表  <->  com.aimental.entity.SystemNotice
-- ------------------------------------------------------------
CREATE TABLE system_notice (
    notice_id      BIGINT       NOT NULL AUTO_INCREMENT COMMENT '公告ID',
    notice_title   VARCHAR(150) NOT NULL                COMMENT '公告标题',
    notice_content TEXT         NOT NULL                COMMENT '公告正文',
    publish_time   DATETIME     NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    admin_id       BIGINT       NULL                    COMMENT '发布管理员ID',
    status         TINYINT      NULL DEFAULT 1          COMMENT '0草稿，1已发布，2下架',
    is_deleted     TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除标记：0未删，1已删',
    PRIMARY KEY (notice_id),
    KEY idx_notice_admin (admin_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统公告表';

-- ------------------------------------------------------------
-- 18. sys_role 角色表（实体 sys_role，原 DDL 未包含）
--     <->  com.aimental.entity.SysRole
-- ------------------------------------------------------------
CREATE TABLE sys_role (
    role_id     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    role_code   VARCHAR(50)  NOT NULL                COMMENT '角色编码（英文标识，唯一）',
    role_name   VARCHAR(50)  NOT NULL                COMMENT '角色名称',
    description VARCHAR(255) NULL                    COMMENT '角色描述',
    status      TINYINT      NULL DEFAULT 1          COMMENT '状态：0停用，1启用',
    create_time DATETIME     NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    is_deleted  TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除标记：0未删，1已删',
    PRIMARY KEY (role_id),
    UNIQUE KEY uk_role_code (role_code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统角色表';

-- ------------------------------------------------------------
-- 19. sys_permission 权限表（实体 sys_permission，原 DDL 未包含）
--     <->  com.aimental.entity.SysPermission
-- ------------------------------------------------------------
CREATE TABLE sys_permission (
    perm_id     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '权限ID',
    perm_code   VARCHAR(100) NOT NULL                COMMENT '权限编码（如user:list，唯一）',
    perm_name   VARCHAR(80)  NOT NULL                COMMENT '权限名称',
    perm_type   TINYINT      NULL                    COMMENT '权限类型：1菜单，2按钮/操作',
    description VARCHAR(255) NULL                    COMMENT '权限说明',
    create_time DATETIME     NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    is_deleted  TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除标记：0未删，1已删',
    PRIMARY KEY (perm_id),
    UNIQUE KEY uk_perm_code (perm_code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统权限表';

-- ------------------------------------------------------------
-- 20. sys_role_permission 角色权限关联表（实体 sys_role_permission，原 DDL 未包含）
--     <->  com.aimental.entity.SysRolePermission
-- ------------------------------------------------------------
CREATE TABLE sys_role_permission (
    rp_id       BIGINT   NOT NULL AUTO_INCREMENT COMMENT '关联ID',
    role_id     BIGINT   NOT NULL                COMMENT '角色ID',
    perm_id     BIGINT   NOT NULL                COMMENT '权限ID',
    create_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP COMMENT '分配时间',
    is_deleted  TINYINT  NOT NULL DEFAULT 0      COMMENT '逻辑删除标记：0未删，1已删',
    PRIMARY KEY (rp_id),
    UNIQUE KEY uk_role_perm (role_id, perm_id),
    KEY perm_id (perm_id),
    CONSTRAINT sys_role_permission_ibfk_1 FOREIGN KEY (role_id) REFERENCES sys_role (role_id),
    CONSTRAINT sys_role_permission_ibfk_2 FOREIGN KEY (perm_id) REFERENCES sys_permission (perm_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色权限关联表';

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 三、种子数据
-- ============================================================

-- ------------------------------------------------------------
-- 3.1 管理员账号（bcrypt 哈希由 Spring Security BCryptPasswordEncoder(10) 实际计算）
--     密码明文：admin123
--     注意：当前后端没有管理员登录接口（只有 /user/login），该账号数据先就位，
--           待管理端登录接口补齐后可直接使用。
-- ------------------------------------------------------------
INSERT INTO admin (admin_id, username, password, admin_name, create_time, last_login, is_deleted) VALUES
(1, 'admin', '$2a$10$aBjrFwF4WZjk5iwrYwXmnOWmhWqLHsNl7/8JmYgLAHvjtUe78TDou', '系统管理员', NOW(), NULL, 0);

-- ------------------------------------------------------------
-- 3.2 心理韧性量表主记录（CD-RISC 简化版，25 题，信度 0.890）
-- ------------------------------------------------------------
INSERT INTO mental_scale (scale_id, scale_name, scale_code, dimension_desc, total_question_num, reliability, status, create_time, is_deleted) VALUES
(1, '心理韧性量表（CD-RISC简化版）', 'CD-RISC',
 '中文版CD-RISC简化量表，共25题，包含坚韧（13题）、力量（8题）、乐观（4题）三个维度；5级评分（选项1-5对应0-4分），总分0-100分。',
 25, 0.890, 1, NOW(), 0);

-- ------------------------------------------------------------
-- 3.3 量表 25 道题目（与 docs/心理韧性量表题目初始化.sql 内容保持一致）
-- ------------------------------------------------------------
DELETE FROM scale_question WHERE scale_id = 1;

SET @opt = '[{"value":1,"label":"完全不符合"},{"value":2,"label":"不太符合"},{"value":3,"label":"一般"},{"value":4,"label":"比较符合"},{"value":5,"label":"完全符合"}]';
SET @rule_toughness = '{"dimension":"toughness","scoreMap":{"1":0,"2":1,"3":2,"4":3,"5":4}}';
SET @rule_strength  = '{"dimension":"strength","scoreMap":{"1":0,"2":1,"3":2,"4":3,"5":4}}';
SET @rule_optimism  = '{"dimension":"optimism","scoreMap":{"1":0,"2":1,"3":2,"4":3,"5":4}}';

INSERT INTO scale_question (scale_id, question_content, option_json, score_rule, sort_num, is_deleted) VALUES
(1, '我能够适应变化', @opt, @rule_toughness, 1, 0),
(1, '我不容易因为失败而气馁', @opt, @rule_toughness, 2, 0),
(1, '我认为自己能够处理很多难题', @opt, @rule_toughness, 3, 0),
(1, '在压力下，我仍然能够集中注意力思考', @opt, @rule_toughness, 4, 0),
(1, '面对困难，我会坚持想办法解决', @opt, @rule_toughness, 5, 0),
(1, '我可以独自面对压力带来的负面感受', @opt, @rule_toughness, 6, 0),
(1, '我有很强的目标感', @opt, @rule_toughness, 7, 0),
(1, '无论发生什么，我都能够找到办法', @opt, @rule_toughness, 8, 0),
(1, '我可以处理悲伤、恐惧、愤怒这类难受的情绪', @opt, @rule_toughness, 9, 0),
(1, '我相信自己的直觉判断', @opt, @rule_toughness, 10, 0),
(1, '我会持续努力，一步步靠近目标', @opt, @rule_toughness, 11, 0),
(1, '遇到难题，我有办法找到支持和帮助', @opt, @rule_toughness, 12, 0),
(1, '我有能力掌控生活里大部分事情', @opt, @rule_toughness, 13, 0),
(1, '压力和困境，会让我变得更强大', @opt, @rule_strength, 14, 0),
(1, '经历伤病、挫折之后，我恢复得很快', @opt, @rule_strength, 15, 0),
(1, '我有战胜困难的力量感', @opt, @rule_strength, 16, 0),
(1, '我能承受不愉快的体验', @opt, @rule_strength, 17, 0),
(1, '我会尽最大努力去解决问题，不会轻易放弃', @opt, @rule_strength, 18, 0),
(1, '我为自己取得的成就感到自豪', @opt, @rule_strength, 19, 0),
(1, '我有足够的能力应对突发状况', @opt, @rule_strength, 20, 0),
(1, '我相信自己是一个强大的人', @opt, @rule_strength, 21, 0),
(1, '事情不顺利的时候，我不容易陷入低落', @opt, @rule_optimism, 22, 0),
(1, '我可以接受事情不一定按我预想发展', @opt, @rule_optimism, 23, 0),
(1, '我能看到困境里有趣、积极的一面', @opt, @rule_optimism, 24, 0),
(1, '我相信困境总会过去，好事会到来', @opt, @rule_optimism, 25, 0);

-- ------------------------------------------------------------
-- 3.4 徽章种子
--     TODO：原 前文.txt 中的徽章 INSERT 在新机器上缺失，下面这条为可运行的占位数据，
--           拿到原文后请整行替换（字段与表结构一致，替换不影响其他数据）。
-- ------------------------------------------------------------
INSERT INTO badge (badge_id, badge_name, badge_icon, obtain_condition, description, is_deleted) VALUES
(1, '初次打卡', '', '完成首次每日心情打卡', '恭喜完成第一次心情打卡，坚持下去会越来越了解自己。', 0);

-- ------------------------------------------------------------
-- 3.5 最小课程数据（保证课程/训练页有可展示内容，可按需删除）
-- ------------------------------------------------------------
INSERT INTO study_course (course_id, course_name, course_desc, cover_url, total_section, status, create_time, is_deleted) VALUES
(1, '情绪调节与压力管理', '认识情绪、识别压力信号，学习可直接使用的放松与调节技巧。', '', 3, 1, NOW(), 0);

INSERT INTO course_section (section_id, course_id, section_title, section_content, study_minutes, sort_num, create_time, is_deleted) VALUES
(1, 1, '认识情绪', '情绪没有好坏之分，先学会命名与觉察自己的情绪，是调节的第一步。', 10, 1, NOW(), 0),
(2, 1, '识别压力信号', '从身体、想法、行为三个层面识别压力过载的早期信号，及早调整节奏。', 12, 2, NOW(), 0),
(3, 1, '呼吸与放松练习', '练习4-7-8呼吸法与渐进式肌肉放松，在紧张时快速降低生理唤醒。', 15, 3, NOW(), 0);

-- ============================================================
-- 四、自检查询（导入后应看到 20 张表、25 道题、1 条量表、1 条徽章、1 个管理员）
-- ============================================================
SELECT COUNT(*) AS table_count
FROM information_schema.tables
WHERE table_schema = 'ai_mental_platform' AND table_type = 'BASE TABLE';

SELECT COUNT(*) AS fk_count
FROM information_schema.table_constraints
WHERE table_schema = 'ai_mental_platform' AND constraint_type = 'FOREIGN KEY';

SELECT COUNT(*) AS question_count FROM scale_question WHERE scale_id = 1 AND is_deleted = 0;

SELECT scale_id, scale_name, total_question_num, reliability, status
FROM mental_scale WHERE scale_id = 1;

SELECT admin_id, username, admin_name FROM admin WHERE is_deleted = 0;

SELECT badge_id, badge_name FROM badge WHERE is_deleted = 0;
