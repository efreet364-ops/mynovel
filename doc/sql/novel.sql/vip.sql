ALTER TABLE `user_info`
  ADD COLUMN `vip_expire_time` datetime DEFAULT NULL COMMENT 'VIP到期时间' AFTER `account_balance`;

CREATE TABLE IF NOT EXISTS `vip_product` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(50) NOT NULL COMMENT 'VIP套餐名',
  `duration_days` int unsigned NOT NULL COMMENT '有效天数',
  `price_cent` int unsigned NOT NULL COMMENT '价格，单位分',
  `status` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '状态;0-启用 1-停用',
  `sort` int unsigned NOT NULL DEFAULT '10' COMMENT '排序',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='VIP套餐';

INSERT INTO `vip_product` (`id`, `name`, `duration_days`, `price_cent`, `status`, `sort`, `create_time`, `update_time`)
VALUES
  (1, '月卡VIP', 30, 1200, 0, 1, NOW(), NOW()),
  (2, '季卡VIP', 90, 3000, 0, 2, NOW(), NOW()),
  (3, '年卡VIP', 365, 9900, 0, 3, NOW(), NOW())
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `duration_days` = VALUES(`duration_days`),
  `price_cent` = VALUES(`price_cent`),
  `status` = VALUES(`status`),
  `sort` = VALUES(`sort`),
  `update_time` = NOW();

ALTER TABLE `pay_stripe`
  ADD COLUMN `product_type` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '商品类型;0-屋币 1-VIP' AFTER `coin_value`,
  ADD COLUMN `product_id` bigint unsigned DEFAULT NULL COMMENT '商品ID' AFTER `product_type`,
  ADD COLUMN `product_name` varchar(255) DEFAULT NULL COMMENT '商品名' AFTER `product_id`,
  ADD COLUMN `product_value` int unsigned DEFAULT NULL COMMENT '商品值;屋币数量或VIP天数' AFTER `product_name`;
