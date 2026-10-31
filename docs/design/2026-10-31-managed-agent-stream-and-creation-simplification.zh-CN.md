# Managed Agent 事件流与创建入口精简

[English](2026-10-31-managed-agent-stream-and-creation-simplification.md) | [简体中文](2026-10-31-managed-agent-stream-and-creation-simplification.zh-CN.md)

状态：已实现，针对性本地验证通过，完整 CI 验收待完成。

## 问题与范围

Public 与 WebShell 的 Session 事件流重复维护相同的回放、实时投递、心跳、权限复查和终止循环。Workspace Session 创建有九个仓库重载，最终包含二十个位置参数；契约与 JDBC 实现分别维护默认参数转发链。

在现有 Maven 边界内分别完成这两项修改。保持可观察行为、HTTP/OpenAPI 模型、持久化字节、SQL 和准入顺序不变。本次不涉及 Runtime provisioner、派发租约、恢复状态机和摘要工具。

## 事件流循环

保留两个公开入口及其同步的 Session 可读性准入。通过事件和重新同步消息的转换函数，委托给一个私有循环。事件标识、类型及删除判断使用原始事件记录。保留 Public 的秒级、WebShell 的毫秒级投影及各自的重新同步记录类型。

保持先订阅后回放的顺序，以及分页、溢出协调、心跳调度、权限复查时机、中断处理和完成回调的位置不变。重新同步帧仍不得携带 SSE ID。不引入公开流式抽象，也不修改事件中心。

## 创建命令

在契约模块引入 `WorkspaceSessionCommand` record，承载现有二十个值。为测试夹具广泛使用的十字段基础形式保留一个便捷构造器，严格补入原有默认值。完整构造器保留 null 和集合引用，不进行规范化或新增校验。

将 `AgentStateStore` 的九个创建重载替换为一个接收命令的方法。在 `ManagedAgentStore` 上保留显式事务覆盖，继续使用专用事务管理器。保留完整事务体及检查顺序。`ManagedAgentService` 构造包含可空 worktree plan 的一个命令，替代十九参数和二十参数调用之间的分支。同步迁移仓库调用方、夹具和 mock 预期。

本次修改内部 Java 仓库 API：源码调用方须将原参数包装为命令，已编译调用方须重新构建。不修改公开 HTTP API、SDK 请求格式或数据库 schema。不将旧的渐进重载链保留为兼容层。

## 验证与验收

遵循模块 AGENTS.md 的共享十分钟本地 Java 验证预算，使用 JDK 21、隔离 Maven 仓库和匹配源码的 SDK。选择事件流权限、回放及重新同步测试，创建、准入、技能、模型及 worktree 调用方测试，以及专用 Spring 事务的提交与回滚验证。迁移调用点时保留现有行为覆盖；将已删除接口默认实现的断言替换为真实仓库的审批能力协商检查。现有覆盖不足时，补充时间单位和命令参数传递的针对性断言。

创建 API 修改必须保留真实代理的专用连接与回滚行为。无法运行的真实 MySQL 检查明确留待 CI；H2 不证明 MySQL 行为。运行 API 契约与 surface 门禁，检测意外的线上协议变化。共享契约验证还包括仓库根目录的 build/typecheck 要求。完整 Java、MySQL 和 Hosted 验收保留在 CI。

验收要求为单一事件流循环、单一仓库创建方法、保持线上消息及准入语义、适用的针对性检查通过，以及连续两轮自审无新问题。分别记录失败、跳过及待 CI 验证的项目。当前无未决设计问题。

## 本地证据

在 macOS、JDK 21.0.8 和 Maven 3.9.14 下，针对性证据覆盖 808 个单元/H2 用例，包括两个 API 契约门禁及事务提交、回滚。首次 753 用例运行中，已删除接口默认实现的旧断言导致一个失败。替换该过时断言后，16 个审批选择用例全部通过；其余 738 个通过用例的证据仍适用。另行补跑的 10 个 worktree 仓库/服务用例和 44 个 Session 行锁顺序检查也全部通过。整个 reactor 的 Checkstyle 通过。

MySQL 8.4.11 的五个选定用例通过：三种模式下的初始模型与审批选择、区分大小写的 Workspace actor 和命令键，以及默认值变化后跨 JVM 重放原绑定。临时服务使用 `max_allowed_packet=134217728` 并关闭 binary logging；macOS 的 native AIO 设置与 Linux CI 不同。进程和数据目录均已清理。首次直接调用 Failsafe 时，因 reactor 父模块没有匹配测试而停止；修正后的生命周期调用及 verification 模块报告检查通过。

根目录 `npm run build` 和 `npm run typecheck` 通过。这些结果及 808 用例/MySQL 证据取得于 rebase 到 `1ccf0d43f3` 之前。Rebase 后，本次重构的全部源码与测试文件保持不变。由于新基线增加了配置元数据生成步骤，rebase 后重新运行 reactor 编译、Checkstyle，以及 52 个选定 API 契约、事件流、创建/worktree 和事务代理用例，均通过；未重复运行更广的检查。

完整 Java/SpotBugs、全量 MySQL 及 Hosted/fault profiles 仍待 CI。这些本地检查仅证明所选路径，不构成生产或跨平台验收。
