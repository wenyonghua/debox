# Debox Reward System

合规积分活动与会员奖励系统后端，基于 Spring Boot、MyBatis-Plus、MySQL、Redis 搭建。

> 说明：本项目按“积分活动/会员营销奖励系统”设计，不实现非法彩票、赌博、资金盘、传销或无资质虚拟资产交易逻辑。

## 技术栈

- Java 17+
- Spring Boot 3.3.x
- MyBatis-Plus 3.5.x
- MySQL 8.x
- Redis 7.x
- Maven

## 快速启动

1. 创建 MySQL 数据库并导入 `src/main/resources/db/schema.sql`
2. 修改 `src/main/resources/application.yml` 中的 MySQL 与 Redis 配置
3. 启动项目：

```bash
mvn spring-boot:run
```

## 文档

- [开发设计文档](docs/development-plan.md)
