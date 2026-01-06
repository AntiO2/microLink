## 数据搜索微服务 API 文档

数据搜索微服务通过集成 Elasticsearch 搜索引擎，为 MicroLink 平台提供高性能的检索能力。系统通过异步事件驱动机制维护索引一致性，确保用户能够实时发现优质的内容与社交关系。

### 1. 全文检索接口

接口描述：
支持针对社交动态、文章及视频内容的全文搜索。系统利用 Elasticsearch 的分词器对关键词进行拆分，并在标题、正文及标签等多个维度执行布尔查询。

* 基础检索接口
* URL: /search/query
* 方法: GET
* 参数: q (String, 搜索关键词)
* 返回: Result<List<ContentDoc>> (匹配的内容列表)


* 高亮检索接口
* URL: /search/query/highlight
* 方法: GET
* 参数: q (String, 搜索关键词)
* 返回: Result<List<SearchContentDTO>> (包含 HTML 高亮标签的结果)



### 2. 用户搜索接口

接口描述：
提供基于用户昵称、用户名及个人简介的分页检索。系统在检索时应用了业务加权逻辑，优先展示实名认证用户及活跃用户。

* 用户分页检索
* URL: /search/users
* 方法: GET
* 请求参数:
* keyword: 搜索关键字（必填）
* page: 分页页码（默认 0）
* size: 每页数量（默认 10）


* 返回结果: Result<Page<UserIndex>> (包含当前页用户数据及总记录数)



### 3. 索引维护与同步接口

接口描述：
此组接口主要供工作流引擎或内部微服务调用，用于在业务数据发生变更时实时更新 Elasticsearch 索引。

* 内容索引同步
* URL: /search/index
* 方法: POST
* 请求体: ContentDoc 对象
* 描述: 当内容发布微服务完成数据持久化后，通过此接口将文档同步至 ES 索引库。


* 用户索引同步
* URL: /search/user/index
* 方法: POST
* 请求体: UserIndex 对象
* 描述: 当用户注册、更新资料或通过认证后，同步更新搜索索引中的用户信息。



### 4. 数据结构定义 (DTO)

内容索引模型：

```java
public class ContentDoc {
    private Long id;
    private String title;
    private String content;
    private Long authorId;
    private List<String> tags;
    private String createTime;
}

```

用户索引模型：

```java
public class UserIndex {
    private String id;
    private String nickname;
    private String username;
    private String bio;
    private Boolean isVerified;
}

```

### 5. 搜索服务技术约束

* 一致性保障：系统采用先写数据库、后同步索引的策略。若索引同步失败，工作流节点将触发异常并记录状态。
* 性能优化：查询逻辑完全在内存索引中完成，不直接触达 MySQL 数据库。高频搜索词汇通过 Redis 进行缓存拦截。
* 容错处理：当 Elasticsearch 集群连接异常时，接口将返回 SYSTEM_ERROR 状态码，保障工作流主进程的稳定性。
