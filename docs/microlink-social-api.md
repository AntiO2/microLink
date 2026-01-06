## 社交微服务 (Social Service) API 文档

### 1. 合规性检查接口

功能描述：
在社交行为生效前进行合法性判定，包括敏感词过滤、用户权限与账号状态核实。

* 接口路径：POST /social/check/compliance
* 请求方法：POST
* 请求参数：
* userId (String): 用户标识
* action (String): 操作类型 (LIKE/COMMENT/FORWARD)
* content (String): 互动内容文本


* 响应格式：Result<Boolean>
* true: 校验通过
* false: 校验失败（违规）



设计实现：
该接口作为流程的前置准入检查。实现上通过查询敏感词库与用户黑名单进行快速判定。在 BPMN 流程中，其返回值决定排他网关的流转路径。

### 2. 互动记录保存接口

功能描述：
将通过校验的社交互动数据持久化至数据库，并关联当前业务流程实例。

* 接口路径：POST /social/record/save
* 请求方法：POST
* 请求参数：
* userId (String): 操作者标识
* action (String): 动作类型
* instanceId (String): 关联的流程实例 ID
* content (String): 互动内容


* 响应格式：Result<Void>

设计实现：
接口侧重于数据存证与幂等性保障。通过存储 instanceId 实现业务链路追踪。执行成功后，通常会触发后续的统计或推送子流程。

### 3. 调用流程示例

1. 引擎调用 /social/check/compliance 执行风险控制。
2. 依据返回结果，网关判定是否允许执行后续操作。
3. 若允许，引擎调用 /social/record/save 完成数据入库。
4. 流程转向异步通知或统计更新节点。

