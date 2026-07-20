# Spring Boot 依赖解耦改造计划与执行记录

本文档用于持续记录 OriginalKeen License 的 Spring Boot 依赖解耦工作，包括计划、设计决策、已执行改动、验证结果、执行过程中发现的问题以及后续任务。

更新规则：

1. 开始新任务前，先在“改造计划”中登记目标和验收标准。
2. 每次完成代码或构建改动后，更新任务状态和“执行记录”。
3. 测试失败、环境问题、错误尝试及其解决方式都必须记录，不能只记录最终成功结果。
4. 尚未验证的事项必须保留为待办，不以推测标记为完成。

## 当前状态

- 当前版本：`1.3.0-SNAPSHOT`
- Java 基线：Java 17+
- Spring Boot 支持范围：Boot 3.2+，限定在 Boot 3.x
- Spring Boot 编译基线：`3.2.0`
- 已验证的较新版本：`3.5.9`
- 最后更新：2026-07-20

## 改造目标

- `model`、`core`、`runtime` 不依赖 Spring，也不由 Spring Boot BOM 管理依赖版本。
- 只有 `spring-boot-autoconfigure` 和 `spring-boot-starter` 感知 Spring Boot。
- OriginalKeen License 按自身 API 和功能演进版本，不跟随 Spring Boot 小版本或补丁版本发布。
- 使用者通过自己的 Spring Boot Parent 或 BOM 决定最终 Boot 版本。
- 同一版本的 OriginalKeen Starter 能在支持范围内的多个 Boot 3.x 版本中使用。
- 发布检查能够阻止核心模块重新引入 Spring 依赖。

## 非目标

- 本轮不修改许可证校验、安装、硬件识别和 Web 过滤业务逻辑。
- 本轮不承诺 Spring Boot 2.x 或 4.x 兼容性。
- 本轮不重写已经验证过的 Samples 业务代码。
- 本轮不执行 Maven Central 发布。

## 改造计划

| 编号 | 任务 | 状态 | 验收标准 |
| --- | --- | --- | --- |
| P1 | 确定 Java 与 Spring Boot 兼容基线 | 已完成 | Java 17+；Boot 3.2.0 编译；Boot 3.5.9 兼容验证 |
| P2 | 根 POM 移除全局 Boot BOM | 已完成 | 根 `pom.xml` 不包含 `spring-boot-dependencies` |
| P3 | 核心模块建立独立版本治理 | 已完成 | `model/core/runtime` 依赖树中没有 Spring |
| P4 | 收敛 Boot 集成模块 | 已完成 | 只有 autoconfigure/starter 导入 Boot BOM |
| P5 | 验证现有 Samples 消费 `1.3.0-SNAPSHOT` | 已完成 | Spring、Java Runtime、CLI 均编译成功 |
| P6 | 建立 Boot 兼容检查 | 已完成 | Boot 3.5.9 下自动配置测试通过 |
| P7 | 建立依赖边界发布门禁 | 已完成 | WSL 中边界脚本执行成功 |
| P8 | 将 Samples 根 POM 改为中性聚合器 | 已完成 | Java Runtime 与 CLI 构建不再继承 Boot Parent |
| P9 | 验证 Samples 完整 XJar/Docker 打包 | 用户手动验证 | 完成 `package` 和 Docker 启动验证 |
| P10 | 执行带 GPG 的完整发布预检 | 用户手动验证 | `scripts/check-release.sh` 完整通过，但不发布 |
| P11 | 同步主项目到 WSL并安装到 Windows 本地 Maven 仓库 | 已完成 | WSL `clean package` 成功；Windows `clean install` 成功；不执行发布 |
| P12 | 为四个 Samples 补充可执行验收步骤 | 已完成 | README 包含命令、预期输出或 HTTP 状态以及失败判断 |
| P13 | 为 Samples 增加本地环境文件 | 已完成 | `.env.example` 可提交；`.env` 可直接使用且被 Git忽略 |
| P14 | 为 Platform 增加许可证签发 HTTP 请求 | 已完成 | IDEA HTTP Client 可直接创建许可证并保存签发产物 |
| P15 | 创建许可证接口返回 licenseId 并串联 HTTP Client | 已完成 | 创建响应 data 为新 ID；HTTP Client 自动下载；文档同步 |
| P16 | 建立持续集成兼容门禁 | 已完成 | CI 执行完整构建、依赖边界检查及 Boot 3.2.0/3.5.9 兼容验证 |

## 已执行改动

### 1. 中性父 POM

文件：根 `pom.xml`

- 删除根 POM 对 `spring-boot-dependencies:3.5.9` 的导入。
- 保留 TrueLicense 版本管理。
- 增加独立的 SLF4J、JUnit Jupiter 和 Mockito 版本属性。
- Spring Boot 版本不再由中性父 POM定义。

### 2. Core 与 Runtime

文件：

- `originalkeen-license-core/pom.xml`
- `originalkeen-license-runtime/pom.xml`

已执行：

- Core 显式声明 SLF4J API 版本。
- Core 和 Runtime 显式声明各自的 JUnit、Mockito 测试版本。
- 测试依赖继续使用 `test` scope，不会传递给使用者。

### 3. Spring Boot 集成模块

文件：

- `originalkeen-license-spring-boot-autoconfigure/pom.xml`
- `originalkeen-license-spring-boot-starter/pom.xml`

已执行：

- 两个模块分别导入 Spring Boot `3.2.0` BOM。
- Boot `3.2.0` 仅作为编译和发布 POM 的最低基线。
- 使用者的 Boot Parent/BOM 可以覆盖为较新的 Boot 3.x 版本。

### 4. 自动化检查

新增：

- `scripts/check-dependency-boundaries.sh`
- `scripts/check-spring-boot-compatibility.sh`

修改：

- `scripts/check-release.sh`

检查内容：

- 根 POM 不允许重新导入 Boot BOM。
- `model/core/runtime` 依赖树不允许出现 Spring 或 Spring Boot。
- Reactor 使用 Boot 3.2 基线执行正常测试。
- `SKIP_TESTS=false` 时，额外使用 Boot 3.5.9 执行自动配置兼容测试。

### 5. Samples 中性聚合器

仓库：`E:\Project\OriginalKeen\originalkeen-license-samples`

文件：

- 根 `pom.xml`
- `spring-boot-demo/pom.xml`

已执行：

- 从 Samples 根 POM移除 `spring-boot-starter-parent:3.5.9`。
- 根 POM只保留 Java 17 编译配置、OriginalKeen BOM和 Maven Compiler Plugin 版本管理。
- `java-runtime-demo` 和 `cli-demo` 继续继承中性 Samples Parent，不再继承任何 Boot Parent 或 BOM。
- Boot `3.5.9` BOM下沉到 `spring-boot-demo`。
- `spring-boot-maven-plugin` 的版本和 `repackage` 执行在 Spring Demo 中显式声明。
- Samples 默认仍引用已经发布的 OriginalKeen `1.2.0`；本轮通过命令行覆盖为 `1.3.0-SNAPSHOT` 验证，避免把未发布版本写入示例仓库默认配置。

### 6. WSL 打包与 Windows 本地安装

执行日期：2026-07-20

- 将 Windows 主项目完整同步到 `/usr/local/project/originalkeen-license`。
- 同步时保留 WSL 项目的 `.git`，并排除 `.idea`、`target` 和 `.flattened-pom.xml` 等本地或构建产物。
- 在 WSL 使用 `/usr/local/apache-maven-3.9.6/bin/mvn` 执行 `clean package`。
- WSL 构建运行完整测试并生成 `1.3.0-SNAPSHOT` 构件，没有执行 `install`、`deploy` 或 Central 发布。
- 在 Windows 使用 `E:\Dev\Maven\apache-maven-3.9.6\bin\mvn.cmd` 执行 `clean install -DskipTests=true`。
- Windows 安装使用该 Maven 的 `conf/settings.xml`，构件写入 `E:\Dev\Maven\apache-maven-3.9.6\repository`。
- 没有启用 `release` Profile，也没有执行 `deploy`。

### 7. Samples README 验收步骤

仓库：`E:\Project\OriginalKeen\originalkeen-license-samples`

已更新：

- 根 `README.md`：增加四类 Demo 的快速通过条件表。
- `spring-boot-demo/README.md`：增加 curl、PowerShell HTTP 请求、预期 `200`/`test` 响应以及 `403` 排查说明。
- `java-runtime-demo/README.md`：增加 Maven Exec Plugin 运行命令、退出码和 `valid=true`/`failureCode=null` 判断。
- `cli-demo/README.md`：增加 verify、install、hardware、watch 命令及各自通过条件。
- `docker-demo/README.md`：增加容器状态、HTTP 请求、预期响应、日志和许可证挂载检查命令。

说明：

- `startupInstall` 可能因为许可证已经安装而为 `false`，不能单独作为失败条件。
- 有效验证的稳定判断是进程退出码 `0`、`valid=true`，并且失败码为空。
- HTTP Demo 的稳定判断是状态码 `200` 且响应体为 `test`。
- 本轮没有自动执行 CLI `hardware` 命令，避免在工具输出中采集和展示本机 MAC、CPU及主板序列号；命令结构已经按实际入口代码核对。

### 8. Samples dotenv 配置

仓库：`E:\Project\OriginalKeen\originalkeen-license-samples`

已执行：

- 新增可提交的 `.env.example`，包含许可证目录、subject、公钥别名、公钥密码、Preferences 节点和 watch 间隔示例。
- 新增本机 `.env`，许可证目录使用 Windows 可识别的正斜杠绝对路径。
- 修改 `.gitignore`，忽略 `.env` 和本地 dotenv 变体，但明确保留 `.env.example`。
- 新增 `.dockerignore`，避免本机 `.env`、Git 元数据、IDE配置和 target 构建产物进入 Docker 构建上下文。
- Spring Demo 的 `application.yml` 可选导入当前工作目录或上级目录中的 `.env`，同时兼容从仓库根目录和模块目录启动。
- README 说明 Spring Demo 会自动导入 `.env`；Java Runtime/CLI 仍需由 IDE或启动 Shell 导入环境变量。

### 9. Platform 许可证签发 HTTP Client

仓库：`E:\Project\OriginalKeen\originalkeen-license-platform`

已执行：

- 新增 `requests/license-issue.http`，供 IntelliJ IDEA HTTP Client 直接运行。
- 请求流程包含创建许可证、可选分页确认、按创建响应 ID 下载签发 ZIP 三个步骤。
- 创建请求使用用户提供的项目、联系人和硬件绑定数据。
- 将示例到期时间从已经过去的 `2026-07-20 16:00:00` 调整为 `2027-07-20 16:00:00`。
- 下载响应保存为 `requests/license-<id>.zip`，ZIP 应包含 `license.lic` 和 `publicCerts.keystore`。
- 修改 Platform `.gitignore`，忽略 `requests/*.zip` 签发下载产物。
- 修改 Platform README，增加 `.http` 文件入口。
- 保留用户已经将 Platform 依赖改为 `originalkeen-license.version=1.3.0-SNAPSHOT` 的现有改动。

### 10. Platform 创建接口返回 licenseId

仓库：`E:\Project\OriginalKeen\originalkeen-license-platform`

已执行：

- 将 `LicenseService.createLicense()`、实现类和 Controller 的返回类型统一为 `Response<String>`。
- 保存许可证记录后读取 MyBatis-Flex 回填的 Snowflake 主键，并以十进制字符串返回，避免 JavaScript 解析 64 位整数时丢失精度。
- 增加主键为空保护，避免保存成功但未回填 ID 时继续在 `null` 目录生成签发文件。
- 修改 `LicenseControllerTest`，断言创建响应的 `data` 是新许可证 ID，而不是原来的布尔值。
- 修改 `requests/license-issue.http`：创建请求验证响应并把 `response.body.data` 保存为全局 `licenseId`，下载请求直接复用，不再手工填写 ID。
- 修改 Platform README，说明返回结构、`data` 的含义及 HTTP Client 自动串联方式。
- 本轮未发现新的项目问题；离线构建和全部后端测试均通过。

### 11. 持续集成兼容门禁

仓库：`E:\Project\OriginalKeen\originalkeen-license`

已执行：

- 新增 `.github/workflows/verify.yml`，在 push 和 pull request 时运行 Java 17 验证。
- CI 的 `build-and-boundaries` Job 执行完整 Reactor `clean verify` 和核心模块 Spring 依赖边界检查。
- CI 的 `spring-boot-compatibility` Job 使用矩阵分别验证 Boot `3.2.0` 与 `3.5.9`，并关闭 fail-fast，便于一次看到两个版本的结果。
- 两个 Bash 检查脚本保留 WSL 固定 Maven/settings 优先级，在标准环境中可回退到 `PATH` 中的 `mvn` 和默认 settings。
- 两个脚本为选定模块增加 `-am`，确保全新检出、尚未安装内部 SNAPSHOT 时也会构建 Reactor 上游模块。
- 更新 `docs/dependency-governance.md`，记录 CI 门禁、矩阵和脚本环境发现规则。

## 验证结果

### Windows

- 完整 Reactor 测试成功。
- Core：6 个测试通过。
- Runtime：11 个测试通过。
- Spring Boot Autoconfigure：9 个测试通过。
- 合计：26 个测试，0 失败，0 错误。
- `model/core/runtime` Spring 依赖树匹配结果为空。
- 本地 `1.3.0-SNAPSHOT` 安装成功。
- Samples 使用命令行覆盖到 `1.3.0-SNAPSHOT` 后：
  - `spring-boot-demo` 编译成功；
  - `java-runtime-demo` 编译成功；
  - `cli-demo` 编译成功。
- Samples 依赖树确认应用最终使用 Boot `3.5.9`，Starter 中的 Boot `3.2.0` 被应用 BOM 正确覆盖。
- Samples 改为中性父 POM后，四模块 Reactor 再次构建成功。
- `java-runtime-demo` 和 `cli-demo` 的 Spring/Spring Boot 依赖树匹配结果均为空。

### WSL

- 已将本轮修改同步到 `/usr/local/project/originalkeen-license`。
- Maven `clean install` 成功，7 个 Reactor 模块全部通过。
- `scripts/check-dependency-boundaries.sh` 执行成功。
- `scripts/check-spring-boot-compatibility.sh` 在 Boot `3.5.9` 下执行成功，9 个测试全部通过。
- 两个检查脚本和发布脚本均通过 Bash 语法检查。
- 完整覆盖同步后再次执行 `clean package`，7 个模块全部成功。
- 本次 WSL 打包运行 26 个测试，0 失败，0 错误，总耗时 29.791 秒。

### Windows 本地 Maven 仓库

- `clean install -DskipTests=true` 成功，7 个 Reactor 模块全部完成安装。
- 已核对 `org/eu/originalkeen` 下的 `1.3.0-SNAPSHOT` POM、JAR和本地元数据文件。
- 已安装模块：parent、dependencies BOM、model、core、runtime、spring-boot-autoconfigure、spring-boot-starter。
- `dependencies` 和 parent 是 POM构件，不生成 JAR；其余五个模块的 JAR均存在。

### Samples README

- 5 个 README 的 Markdown差异检查通过。
- 所有 HTTP 路径均与 `TestController` 的 `/test` 映射和端口 `9998` 一致。
- Java Runtime 成功字段与 `LicenseRuntimeEndToEndTest` 的断言一致：`valid=true`、`failureCode=null`、`mismatchType=null`。
- CLI 命令名称、参数和退出码判断均与 `CliDemoApplication` 实现一致。

### Samples dotenv

- `git check-ignore` 确认 `.env` 命中忽略规则。
- `git status` 确认 `.env.example` 和 `.dockerignore` 是可提交的新文件。
- Spring Demo 在加入 `.env` 可选导入后执行 Maven `test` 阶段成功。

### Platform HTTP Client

- Platform Backend 离线测试执行成功：2 个测试，0 失败，0 错误。
- `LicenseControllerTest` 已验证创建接口响应 `data` 为 `licenseId`。
- `LicenseCreatorIntegrationTest` 通过，签发核心流程未受返回值调整影响。
- Controller 路径确认为 `POST /system/license`、`GET /system/license` 和 `GET /system/license/downloadZip/{id}`。
- 请求字段和日期格式与 `CreateLicenseDto`、`LicenseCheckDto` 的实际定义一致。
- 分页参数 `currentPage`、`pageSize` 和 `projectName` 与查询 DTO一致。

### 持续集成兼容门禁

- 两个 Bash 脚本通过 `bash -n` 语法检查。
- WSL 执行与 CI 相同的完整 Reactor `clean verify`，7 个模块全部构建成功。
- WSL 依赖边界检查通过，`model/core/runtime` 未检测到 Spring 依赖。
- WSL Boot `3.2.0` 兼容验证通过，自动配置模块 9 个测试全部成功。
- WSL Boot `3.5.9` 兼容验证通过，自动配置模块 9 个测试全部成功。
- 工作流 YAML 可被本地解析器读取；实际 GitHub Actions 运行状态需在提交并推送后确认。

## 执行过程中发现的问题

### I1. 首次完整测试超时

- 现象：Windows `mvn clean test` 在 Runtime 端到端测试期间超过工具的 120 秒超时。
- 判断：不是测试失败；当时 Core 6 个测试已经全部通过，Runtime 正在执行慢测试。
- 处理：从 Runtime 模块续跑并扩大超时时间。
- 结果：Runtime 11 个测试全部通过。

### I2. JUnit 版本混用

- 现象：第一轮解耦后，Autoconfigure 测试尝试组合 JUnit Jupiter `5.12.2` 与 Boot 3.2 管理的 JUnit Platform `1.10.1`。
- 原因：中性父 POM直接管理 JUnit 版本，优先级影响了 Boot 模块自己的 BOM。
- 处理：从父 POM的 `dependencyManagement` 移除 SLF4J/JUnit/Mockito 条目；Core 和 Runtime 在自己的依赖中显式引用版本；Boot 模块完整采用自己的 Boot BOM。
- 结果：核心测试与 Boot 自动配置测试全部通过，版本职责分离。

### I3. 本地缺少 Boot 3.2 测试构件

- 现象：离线构建缺少 `junit-platform-launcher:1.10.1`。
- 原因：本机 Maven 仓库此前没有缓存该版本。
- 处理：允许 Maven 下载缺失构件后重新测试。
- 结果：Autoconfigure 9 个测试通过。

### I4. PowerShell 拆分带点号的 Maven 属性

- 现象：`-Doriginalkeen-license.version=1.3.0-SNAPSHOT` 和 `-Dgpg.skip=true` 被错误拆分，Maven分别继续使用 `1.2.0` 或报告未知生命周期阶段 `.skip=true`。
- 原因：PowerShell 向外部程序转交带点号参数时的解析方式。
- 处理：对完整 `-Dkey=value` 参数显式加引号。
- 结果：Windows Samples 版本覆盖和 WSL Maven 构建均成功。

### I5. Samples 的 XJar 插件无法解析

- 现象：Samples 执行 `package` 时无法解析 `com.github.core-lib:xjar-maven-plugin:4.0.2`。
- 原因：当前配置的阿里云 Maven 镜像中找不到该插件；本地只有不完整或来源不可用的缓存记录。
- 影响：阻断 Spring Demo 的 XJar `package` 阶段，但不影响依赖解析、Java 编译和 `test` 阶段。
- 临时处理：执行到 `test` 阶段，三个 Java 模块全部编译成功。
- 后续：由用户手动验证 XJar 与 Docker 完整打包；必要时单独调整插件仓库或替换插件。

### I6. GPG 发布预检需要交互解锁

- 现象：自动执行 `scripts/check-release.sh` 时，GPG 在默认密钥签名检查处返回 `cancelled by user`。
- 原因：当前非交互调用没有解锁 GPG 密钥。
- 影响：发布预检未运行到后续 Maven 和边界检查阶段。
- 已完成的替代验证：WSL Maven 构建、依赖边界脚本和 Boot 兼容脚本均已分别执行成功。
- 后续：由用户在可交互的 WSL 终端手动运行完整发布预检。

### I7. WSL 首次需要下载 Boot 3.2 依赖

- 现象：首次 WSL 构建下载了 Boot 3.2 BOM及其管理的相关 BOM和依赖。
- 原因：WSL Maven 本地仓库此前主要缓存的是 Boot 3.5.9 构建依赖。
- 处理：完成一次联网构建并填充缓存。
- 结果：WSL `clean install` 成功。

### I8. 移除 Boot Parent 会同时移除插件生命周期配置

- 风险：原 Spring Demo 只声明了 `spring-boot-maven-plugin`，其 `repackage` 生命周期绑定由 Boot Parent 隐式提供。
- 如果遗漏：Spring Demo 虽然能够编译，但 `package` 可能只生成普通 JAR，Docker 运行方式会发生变化。
- 处理：在 `spring-boot-demo/pom.xml` 中显式声明插件版本和 `repackage` goal。
- 结果：移除 Boot Parent 后仍保留原有可执行 Spring Boot JAR打包语义。

### I9. Spring Demo 的相对默认路径依赖进程工作目录

- 实际现象：用户从 IDEA启动 Spring Demo，但没有配置环境变量；请求 `/test` 返回 `LICENSE_FILE_MISSING`。
- 文件核对：`license/license.lic` 和 `license/publicCerts.keystore` 均实际存在。
- 原因：Spring Demo 的默认目录固定为 `../license`。当进程工作目录是 Samples 仓库根目录时，它会错误解析为 `E:\Project\OriginalKeen\license`，而不是 `originalkeen-license-samples\license`。
- 立即处理：在 IDEA中设置 `OK_LICENSE_DIR=E:\Project\OriginalKeen\originalkeen-license-samples\license`，或将 Working directory 设置为 `spring-boot-demo`，然后重启应用。
- 文档处理：修正 Samples 根 README 的默认路径说明；根目录 Maven启动示例显式设置 `OK_LICENSE_DIR`；补充错误路径示例。
- 结果：文档不再暗示 Spring Demo 能从任意工作目录自动找到相对许可证路径。

### I10. `.env` 不是 Java 标准自动加载机制

- 风险：仅创建 `.env` 并不能让普通 Java进程自动获得其中的值；`System.getenv()` 只读取进程环境变量。
- Spring Demo 处理：使用 `spring.config.import` 将 `.env` 按 properties 格式可选导入，因此不需要额外 dotenv 依赖。
- Java Runtime/CLI 处理：保持零额外依赖，不在示例代码中内置 dotenv 解析器；由 IDEA环境文件功能或启动 Shell 导出变量。
- Docker 处理：Compose 运行时继续使用容器内的 `/licenses` 配置，同时通过 `.dockerignore` 防止本机 `.env` 进入构建上下文。

### I11. 用户提供的许可证到期时间已经过去

- 现象：请求示例的 `expiryTime` 是 `2026-07-20 16:00:00`，早于本次创建 `.http` 文件的时间。
- 影响：即使签发成功，Samples 随后也会返回 `EXPIRED`，无法用于有效许可证回归。
- 处理：默认签发请求改为 `2027-07-20 16:00:00`，并在请求注释中提醒到期时间必须晚于当前时间。

### I12. 创建许可证响应原先不返回新记录 ID（已解决）

- 原现象：`LicenseServiceImpl.createLicense()` 返回 `Response.success()`，响应中的 `data` 为空。
- 原影响：HTTP Client 无法从创建响应中自动提取 ID 并立即下载 ZIP。
- 处理：P15 将创建接口改为 `Response<String>` 并返回回填主键的十进制字符串；`.http` 自动保存 `response.body.data` 为 `licenseId`。
- 结果：列表查询降为可选确认步骤，创建后可以直接执行 ZIP 下载请求。

### I13. Platform 认证拦截器当前未注册到 MVC

- 现象：存在 `AuthorizeHandlerInterceptor` Bean，但代码中没有找到 `addInterceptors` 注册或相同作用的安全链配置。
- 影响：`/system/license` 签发、查询和下载接口当前看起来不要求 `Authorization` 请求头。
- 本轮处理：为保持现有本地测试行为，`.http` 不添加无效 Token，也不擅自改变认证逻辑。
- 后续建议：Platform 对外部署前应单独确认并补齐签发接口访问控制。

### I14. YAML 1.1 解析器会把未加引号的 on 当作布尔值

- 现象：使用本地 PyYAML 做结构检查时，工作流顶层 `on` 被解析为布尔值 `true`。
- 影响：GitHub Actions 本身支持未加引号写法，但跨解析器校验结果容易产生误导。
- 处理：将工作流键写为 `"on"`；语义不变，同时兼容 YAML 1.1 本地解析器。

### I15. 并行启动 WSL 语法检查出现一次瞬时失败

- 现象：同时启动两个 `wsl.exe` 检查进程时，其中一次返回 WSL 初始化/商店提示，没有进入 Bash。
- 判断：不是脚本语法失败；改为一次 WSL 会话内顺序执行后，两份脚本均通过 `bash -n`。
- 处理：后续 WSL 门禁按单会话顺序执行，避免并发启动发行版。

### I16. Starter 构建提示 JAR 为空

- 现象：完整 Reactor 构建中，Maven 对 `originalkeen-license-spring-boot-starter` 输出 `JAR will be empty` 警告。
- 原因：该 Starter 当前只通过 POM 聚合依赖，不包含 Java 类或资源；真正的自动配置元数据位于 autoconfigure 模块。
- 判断：这是依赖型 Starter 的预期结构，不影响使用者通过 Starter POM 获得依赖，也不影响本次构建成功。
- 处理：记录该提示但不为消除警告而添加无意义的占位类。

## 后续任务

### Samples 手动验收收尾

- P8 的构建解耦已经完成，不再作为后续待办重复列出。
- P9 保持“用户手动验证”，待用户完成剩余 Demo/XJar/Docker 验收后再更新状态。

### 兼容矩阵扩展原则

- 默认只验证最低编译基线和一个较新版本，避免为每个 Boot 补丁版本复制工程。
- 发现实际兼容问题时，再增加有代表性的中间版本。
- Boot 4 必须经过单独设计和验证后才能加入支持范围。

## 手动验证命令

完整发布预检：

```bash
cd /usr/local/project/originalkeen-license
SKIP_TESTS=false bash scripts/check-release.sh
```

仅验证依赖边界：

```bash
bash scripts/check-dependency-boundaries.sh
```

仅验证较新 Boot 版本：

```bash
bash scripts/check-spring-boot-compatibility.sh
```

临时增加多个 Boot 版本：

```bash
BOOT_COMPATIBILITY_VERSIONS="3.4.0 3.5.9" \
  bash scripts/check-spring-boot-compatibility.sh
```
