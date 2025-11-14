# microLink - 微服务项目

## Project Overview

`microLink` 是一个基于 Spring Boot 的微服务架构项目，旨在为社交平台提供用户管理、内容发布、社交互动、搜索、数据统计和内容推送等功能。该项目遵循微服务架构，每个功能模块都被实现为一个独立的微服务。通过这种方式，我们能够实现服务的独立部署和扩展。

## Microservice Modules

该项目包含以下微服务模块：

1. **用户管理微服务 (`microlink-user`)**
   - 负责用户的注册、登录、信息管理和权限控制。
   - 使用 JWT 和 OAuth2 进行用户认证。

2. **内容发布微服务 (`microlink-content`)**
   - 负责处理用户在平台上的内容发布、编辑、删除和存储。
   - 支持文本、图片、视频等内容类型，使用 AWS S3 存储大文件。

3. **社交互动微服务 (`microlink-social`)**
   - 处理用户之间的互动，包括点赞、评论、关注等功能。
   - 使用 Redis 缓存实时数据。

4. **搜索微服务 (`microlink-search`)**
   - 提供内容和用户的搜索功能。
   - 使用 Elasticsearch 实现全文检索和索引管理。

5. **数据统计微服务 (`microlink-statistics`)**
   - 负责分析用户行为、内容热度、平台活跃度等统计数据。
   - 使用 Kafka 和 Prometheus 处理高吞吐量实时数据流，并通过 Grafana 展示统计结果。

6. **首页推送内容微服务 (`microlink-push`)**
   - 提供个性化的内容推送服务，包括热门内容、社交动态推送等。
   - 使用 WebSocket 或其他实时推送技术实现通知。

## Environment Requirements

- JDK 21
- Maven 3.x
- Docker (可选，用于容器化部署)
- MySQL/PostgreSQL (根据需求)
- Elasticsearch (用于搜索微服务)
- Kafka (用于数据统计和流处理)

## Build and Run

### 1. 克隆项目

```bash
git clone https://github.com/AntiO2/microLink.git
cd microLink
````

### 2. 构建项目

在父项目目录下运行以下命令来构建所有模块：

```bash
mvn clean install
```

### 3. 启动每个子模块

每个子模块都是一个独立的 Spring Boot 应用。进入每个子模块目录并运行以下命令来启动服务：

#### 启动用户管理微服务

```bash
cd microlink-user
mvn spring-boot:run
```

#### 启动内容发布微服务

```bash
cd microlink-content
mvn spring-boot:run
```

#### 启动社交互动微服务

```bash
cd microlink-social
mvn spring-boot:run
```

#### 启动搜索微服务

```bash
cd microlink-search
mvn spring-boot:run
```

#### 启动数据统计微服务

```bash
cd microlink-statistics
mvn spring-boot:run
```

#### 启动首页推送微服务

```bash
cd microlink-push
mvn spring-boot:run
```

### 4. 使用 Docker 启动服务（可选）

如果你希望使用 Docker 来运行项目，可以使用 Docker Compose 启动所有服务。首先，确保你有一个有效的 `docker-compose.yml` 文件。然后，运行以下命令：

```bash
docker-compose up --build
```

## API Documentation

每个微服务都提供一组 RESTful API 用于与其他服务交互。请参考以下文档以获取详细的 API 接口说明：

* [用户管理微服务 API](docs/microlink-user-api.md)
* [内容发布微服务 API](docs/microlink-content-api.md)
* [社交互动微服务 API](docs/microlink-social-api.md)
* [搜索微服务 API](docs/microlink-search-api.md)
* [数据统计微服务 API](docs/microlink-statistics-api.md)
* [首页推送微服务 API](docs/microlink-push-api.md)

## Technology Stack

* **Spring Boot** - 用于构建微服务。
* **Spring Security** - 用于用户认证与授权。
* **Elasticsearch** - 用于内容和用户的搜索。
* **Kafka** - 用于高吞吐量的实时数据流处理。
* **Redis** - 用于缓存和实时数据处理。
* **AWS S3** - 用于存储大文件。
* **Prometheus** 和 **Grafana** - 用于数据统计和可视化。

## Developers

* **@AntiO2** - 项目管理、搜索微服务、社交互动微服务
* **@gengdy1545** - 用户管理微服务、内容发布微服务
* **@Rolland1944** - 数据统计微服务、首页推送微服务

## License

此项目使用 MIT 许可证，详情请查看 [LICENSE](LICENSE) 文件。
