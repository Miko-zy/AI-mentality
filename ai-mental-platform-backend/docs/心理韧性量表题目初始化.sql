-- ============================================================
-- 心理韧性量表（CD-RISC简化版，scale_id=1）完整25题初始化脚本
-- 作用：替换原7条英文占位题，写入3个维度共25道中文题目
-- 计分：选项value 1-5，scoreMap映射为0-4分，每题满分4分，总分0~100
-- 维度：toughness坚韧13题 / strength力量8题 / optimism乐观4题
-- 幂等：可重复执行（每次先清空该量表题目再插入）
-- 执行：mysql --default-character-set=utf8mb4 -uroot -p123456 ai_mental_platform < 本文件
-- ============================================================

DELETE FROM scale_question WHERE scale_id = 1;

-- 统一5级选项
SET @opt = '[{"value":1,"label":"完全不符合"},{"value":2,"label":"不太符合"},{"value":3,"label":"一般"},{"value":4,"label":"比较符合"},{"value":5,"label":"完全符合"}]';

-- 三个维度的计分规则（选项1-5对应得分0-4）
SET @rule_toughness = '{"dimension":"toughness","scoreMap":{"1":0,"2":1,"3":2,"4":3,"5":4}}';
SET @rule_strength  = '{"dimension":"strength","scoreMap":{"1":0,"2":1,"3":2,"4":3,"5":4}}';
SET @rule_optimism  = '{"dimension":"optimism","scoreMap":{"1":0,"2":1,"3":2,"4":3,"5":4}}';

-- ==================== 坚韧维度（13题，sort 1-13）：困境下坚持、抗压、主动应对 ====================
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
(1, '我有能力掌控生活里大部分事情', @opt, @rule_toughness, 13, 0);

-- ==================== 力量维度（8题，sort 14-21）：挫折后复原、自我效能、压力中获得力量 ====================
INSERT INTO scale_question (scale_id, question_content, option_json, score_rule, sort_num, is_deleted) VALUES
(1, '压力和困境，会让我变得更强大', @opt, @rule_strength, 14, 0),
(1, '经历伤病、挫折之后，我恢复得很快', @opt, @rule_strength, 15, 0),
(1, '我有战胜困难的力量感', @opt, @rule_strength, 16, 0),
(1, '我能承受不愉快的体验', @opt, @rule_strength, 17, 0),
(1, '我会尽最大努力去解决问题，不会轻易放弃', @opt, @rule_strength, 18, 0),
(1, '我为自己取得的成就感到自豪', @opt, @rule_strength, 19, 0),
(1, '我有足够的能力应对突发状况', @opt, @rule_strength, 20, 0),
(1, '我相信自己是一个强大的人', @opt, @rule_strength, 21, 0);

-- ==================== 乐观维度（4题，sort 22-25）：接纳变化、积极看待逆境、相信好事会发生 ====================
INSERT INTO scale_question (scale_id, question_content, option_json, score_rule, sort_num, is_deleted) VALUES
(1, '事情不顺利的时候，我不容易陷入低落', @opt, @rule_optimism, 22, 0),
(1, '我可以接受事情不一定按我预想发展', @opt, @rule_optimism, 23, 0),
(1, '我能看到困境里有趣、积极的一面', @opt, @rule_optimism, 24, 0),
(1, '我相信困境总会过去，好事会到来', @opt, @rule_optimism, 25, 0);

-- 校验：应返回 25 行，三个维度分别为 13/8/4
SELECT COUNT(*) AS total FROM scale_question WHERE scale_id = 1 AND is_deleted = 0;
SELECT JSON_UNQUOTE(JSON_EXTRACT(score_rule, '$.dimension')) AS dim, COUNT(*) AS cnt
FROM scale_question WHERE scale_id = 1 AND is_deleted = 0 GROUP BY dim ORDER BY MIN(sort_num);
