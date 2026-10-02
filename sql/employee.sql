DROP TABLE IF EXISTS employee;

  CREATE TABLE employee (
      id          BIGINT       NOT NULL AUTO_INCREMENT
  COMMENT '主键',
      name        VARCHAR(32)  NOT NULL COMMENT '姓名',
      username    VARCHAR(32)  NOT NULL COMMENT '用户名',
      password    VARCHAR(64)  NOT NULL COMMENT '密码',
      phone       VARCHAR(11)  NOT NULL COMMENT '手机号',
      status      INT          NOT NULL DEFAULT 1 COMMENT
  '状态 1启用 0禁用',
      create_time DATETIME     NOT NULL COMMENT '创建时间',
      update_time DATETIME     NOT NULL COMMENT '更新时间',
      PRIMARY KEY (id),
      UNIQUE KEY idx_username (username)
  ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT =
  '员工表';

  INSERT INTO employee (name, username, password, phone,
  status, create_time, update_time)
  VALUES ('张三', 'zhangsan', '123456', '13800138000', 1,
  NOW(), NOW());