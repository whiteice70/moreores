# Fabric 模组开发工程 —— Minecraft 26.3

> 本工程基于 Fabric 官方 `fabric-example-mod` 的 **26.3** 分支搭建，已针对**内网环境（无法访问 Mojang 下载源）**做了完整的镜像化改造。
> 模组内容：**More Ores** —— 新增铀（Uranium）与铅（Lead）两种矿物，含矿石、深层矿石、粗矿、锭与对应方块，并注入主世界世界生成。
> 最后更新：2026-10-10

---

## 1. 项目概况

| 项目 | 值 | 说明 |
|---|---|---|
| Minecraft | **26.3** | Java 版已改用年份版本号（`26.x`），26.3 是 2026-09 的「Wilderness Bound」drop |
| Fabric Loader | 0.19.5 | |
| Fabric API | 0.161.0+26.3 | |
| Fabric Loom | 1.18-SNAPSHOT（实际解析为 **1.18.3**） | 插件 id 为 `net.fabricmc.fabric-loom`（注意不是旧的 `fabric-loom`） |
| Gradle | **9.7.1** | 使用 wrapper，无需全局安装 |
| JDK | **25.0.4.1 LTS**（Microsoft OpenJDK） | MC 26.1 ~ 26.3 官方要求 Java 25 |
| mod id | `moreores` | |
| group / 包名 | `com.example.moreores` | |
| mod 版本 | 1.0.0 | |
| profiles | main + client 双源码集 | 模板默认开启 `splitEnvironmentSourceSets()` |

---

## 2. 当前进度（截至 2026-10-10）

### ✅ 已完成

| # | 事项 | 验证方式 |
|---|---|---|
| 1 | 工程骨架落地 | 官方 26.3 模板，`src/main` + `src/client` 双源码集 |
| 2 | JDK 25 环境 | 项目内自包含 `.jdk\jdk-25`，**未改动系统 `JAVA_HOME`** |
| 3 | Gradle 9.7.1 | `.\dev.ps1 --version` → Launcher JVM 25.0.4.1 |
| 4 | 打通内网到 Minecraft 资源 | 见 §5 |
| 5 | 构建 | `.\dev.ps1 build` → **BUILD SUCCESSFUL**，产物 `build\libs\moreores-1.0.0.jar` |
| 6 | 资源文件 | `.\dev.ps1 downloadAssets` → **BUILD SUCCESSFUL**（复用本地 1.9 GB，零下载） |
| 7 | 占位符替换 | `modid` / `com.example` → `moreores` / `com.example.moreores` |
| 8 | 铀 / 铅矿物内容 | 8 个方块 + 12 个物品 + 14 个配方 + 8 张掉落表 + 5 个标签，见 §8 |
| 9 | 世界生成 | 铅：y -16~80，每区块 8 组；铀：y -60~-4，每区块 2 组（暴露在空气旁会被丢弃） |
| 10 | 数据包校验 | `runServer` → `Done (1.914s)!`，无任何数据包解析错误 |
| 11 | 矿石实际生成 | 解析已生成区块逐一确认，见下 |
| 12 | 客户端资源 | `runClient` 日志无 `Unable to load model` / `Missing texture` |

矿石生成实测（解压 `run\world\dimensions\minecraft\overworld\region\*.mca` 的 529 个区块统计）：

```
moreores:lead_ore              -> 13 chunks
moreores:deepslate_lead_ore    ->  5 chunks
moreores:uranium_ore           ->  0 chunks   ← 铀只在深层生成，石头层不出现，符合设计
moreores:deepslate_uranium_ore ->  8 chunks
minecraft:iron_ore   (原版对照) -> 13 chunks
minecraft:coal_ore   (原版对照) -> 12 chunks
```

> 铅矿的出现频率与原版铁矿持平，说明 `BiomeModifications.addFeature` 注入成功、placed_feature 的 `count` 参数按预期生效。

### ⏳ 待办（下次继续）

1. **阶段二：辐射联动** —— 铀的「辐射中毒」状态效果 + 铅制装备屏蔽。
2. 纹理目前是**原版改色**（铁矿系改青灰 → 铅，钻石矿系改荧光绿 → 铀），可随时替换 `assets/moreores/textures/**` 下的 PNG。
3. 可选：`.\dev.ps1 genSources` 生成反编译源码，方便读 Minecraft 源码。

---

## 3. 快速开始

```powershell
cd e:\Study\PythonTest\my_first_mod

.\dev.ps1 runClient     # 启动开发客户端（会自动开游戏窗口）
.\dev.ps1 runServer     # 启动开发服务端
.\dev.ps1 build         # 打包 → build\libs\
.\dev.ps1 genSources    # 反编译 Minecraft 源码（首次较慢，建议跑一次）
.\dev.ps1 clean         # 清理
```

`dev.bat` 与 `dev.ps1` 等价（内部转发），双击也能用。

**不要直接执行 `gradlew.bat`** —— 系统 `JAVA_HOME` 指向 JDK 8，Gradle 9 会因为 JVM 版本过低直接失败。`dev.ps1` 会自动把 `JAVA_HOME` 指向项目内的 JDK 25，并自动探测本地代理端口。

---

## 4. 目录结构

```
e:\Study\PythonTest\my_first_mod\
├─ .jdk\jdk-25\                        # 项目自带的 JDK 25（已 gitignore）
├─ .gradle\                            # Gradle 项目级缓存（含 loom-cache）
├─ gradle\wrapper\                     # Gradle Wrapper
├─ src\
│  ├─ main\java\com\example\moreores\  # 通用代码（禁止引用 net.minecraft.client.*）
│  │  ├─ MoreOres.java                 # ModInitializer 入口（MOD_ID / id()）
│  │  ├─ ModBlocks.java                # 8 个方块：矿石 / 深层矿石 / 粗矿块 / 金属块
│  │  ├─ ModItems.java                 # 粗矿、锭，以及全部方块对应的 BlockItem
│  │  ├─ ModItemGroups.java            # 「More Ores」创造模式页签
│  │  ├─ world\ModWorldGen.java        # BiomeModifications.addFeature 注入
│  │  └─ mixin\ExampleMixin.java
│  ├─ main\resources\
│  │  ├─ fabric.mod.json               # mod 元信息 + 入口点声明
│  │  ├─ moreores.mixins.json
│  │  ├─ assets\moreores\              # 客户端资源（§8.2）
│  │  │  ├─ blockstates\ models\ items\ lang\ textures\
│  │  │  └─ icon.png
│  │  └─ data\                         # 数据包（§8.3）
│  │     ├─ moreores\                  # loot_table / recipe / worldgen
│  │     ├─ minecraft\tags\block\      # 挖掘等级与工具标签
│  │     └─ c\tags\block\ores\         # Fabric 通用约定标签
│  └─ client\java\com\example\moreores\client\   # 仅客户端代码
│     ├─ MoreOresClient.java                     # ClientModInitializer 入口
│     └─ mixin\ExampleClientMixin.java
├─ run\                                # runClient/runServer 的工作目录（已 gitignore）
├─ build\libs\                         # 构建产物
├─ dev.ps1 / dev.bat                   # 开发脚本
├─ gradle.properties                   # 版本号 + JDK + 镜像配置
├─ build.gradle                        # Loom 配置 + 仓库
└─ settings.gradle                     # 插件解析仓库
```

> ⚠️ **双源码集的坑**：`src/main` 里不能引用任何 `net.minecraft.client.*` 的类，否则 `runServer` 会崩。客户端专属代码（渲染、HUD、按键、屏幕）必须放 `src/client`。

---

## 5. 内网环境改造说明（核心）

### 5.1 环境事实

- 机器处于**内网**，挂代理无效。
- 所有 `*.mojang.com` / `*.minecraft.net` 域名：**DNS 正常、TCP 443 能连，但 TLS 握手被 RST**（典型 SNI 阻断），报错为 `javax.net.ssl.SSLHandshakeException: Remote host terminated the handshake` / `Connection reset`。
- `services.gradle.org`（Gradle 官方分发源）**超时**。
- 可达的源：`bmclapi2.bangbang93.com`、`maven.aliyun.com`、`mirrors.cloud.tencent.com`、`mirrors.huaweicloud.com`、`repo.spongepowered.org`、`maven.fabricmc.net`。
- 系统 `JAVA_HOME=E:\software\jdk8`（**保持原样未动**），本机还装有 JDK 11/17/21，但没有 25。

### 5.2 改动清单

| 文件 | 改动 | 原因 |
|---|---|---|
| `gradle/wrapper/gradle-wrapper.properties` | `distributionUrl` 换成腾讯云镜像；`networkTimeout` 10000→120000；`retries` 0→3 | 官方 Gradle 分发源超时 |
| `settings.gradle` | `pluginManagement.repositories` 加阿里云 public / gradle-plugin | 加速插件解析 |
| `build.gradle` | 加阿里云、华为云、腾讯云、**Sponge** 四个仓库 | 见下 |
| `build.gradle` | `loom { customMinecraftMetadata = "<BMCLAPI 上的 26.3.json>" }` | 版本 JSON 原本要从 `piston-meta.mojang.com` 取，被墙 |
| `gradle.properties` | `org.gradle.java.home` 指向项目内 JDK 25 | MC 26.x 必须 Java 25 |
| `gradle.properties` | `org.gradle.jvmargs=-Xmx2G`（模板默认 1G） | Loom 构建内存不足 |
| `gradle.properties` | `org.gradle.configuration-cache=false` | 配置缓存与 Loom 的自定义元数据存在序列化冲突 |
| `gradle.properties` | 新增 `loom_version_manifests` / `loom_libraries_base` / `loom_resources_base` | Loom 内置的镜像覆盖开关 |
| `.gitignore` | 加 `.jdk/` | 不把 200 MB 的 JDK 提交进仓库 |

### 5.3 `gradle.properties` 里的镜像三件套

```properties
loom_version_manifests=https://bmclapi2.bangbang93.com/mc/game/version_manifest_v2.json
loom_libraries_base=https://maven.aliyun.com/repository/public
loom_resources_base=https://bmclapi2.bangbang93.com/assets/
```

> **注意**：`loom_libraries_base` **不能**用 `https://bmclapi2.bangbang93.com/maven/` —— 它会 302 跳转到中科大镜像 `chinanet.mirrors.ustc.edu.cn`，而该地址在内网被拒绝。

这三个属性的来源：反编译 `fabric-loom-1.18.3.jar` 里的 `net.fabricmc.loom.util.MirrorUtil`，它读取 Gradle 的 extra property（`gradle.properties` 里的属性会被 Loom 识别）。

### 5.4 各下载环节的最终走向

| 原本的地址 | 现在 | 方式 |
|---|---|---|
| `piston-meta`（版本清单） | BMCLAPI | `loom_version_manifests` |
| `piston-meta`（版本 JSON） | BMCLAPI | `customMinecraftMetadata` |
| `piston-data`（client.jar / server.jar） | BMCLAPI 下载后**手动预置**到 Loom 缓存 | §6 |
| `libraries.minecraft.net` | 阿里云（Maven Central 镜像） | `loom_libraries_base` |
| Mojang 私有库 `com.mojang:patchy`、`com.mojang:text2speech` | **Sponge 公共仓库** | `build.gradle` repositories |
| `resources.download.minecraft.net`（资源对象） | BMCLAPI | `loom_resources_base` |
| 资源索引 JSON | 手动预置 | §6 |

关于 Sponge：Minecraft 的绝大多数库都在 Maven Central（阿里云能覆盖），但 `com.mojang:patchy`、`com.mojang:text2speech` 这两个是 Mojang 私有库，中央仓库没有，实测只有 Sponge 的 `maven-public` 提供。

---

## 6. 手动预置的缓存（重要，丢失后构建会失败）

Loom 的下载器（`net.fabricmc.loom.util.download.Download`）在用 `HttpClient` 下载前会**先校验本地缓存文件的 SHA1，匹配就直接跳过下载**。所以对于被墙的 `piston-data.mojang.com`，我们手动放好文件即可绕过。

缓存根目录：`C:\Users\25573\.gradle\caches\fabric-loom\`

| 文件 | SHA1 | 大小 |
|---|---|---|
| `26.3\minecraft-client.jar` | `e877b6a07acd633fb3bb475002175cec036e7b87` | 41,483,720 |
| `26.3\minecraft-server.jar` | `33680f5f2ac32864d6d7cf5e56a705fdb3e05f4c` | 62,294,556 |
| `assets\indexes\26.3-34.json` | `1e4e4a68dece1c0f51b8f93b45da8fe8da51bcd3` | 597,035 |

> 26.3 的版本 JSON（`customMinecraftMetadata` 指向的那份）SHA1 为 `702fe59163c6ee6578607daa85811d9bc9c7cc40`，已确认 BMCLAPI 提供的与官方**逐字节一致**。

### 资源对象目录用「目录联接」复用 PCL2 的数据

```
C:\Users\25573\.gradle\caches\fabric-loom\assets\objects
    → (Junction) C:\Users\25573\AppData\Roaming\.minecraft\assets\objects
```

PCL2 已经把 26.3 的 **10,479 个资源对象（1,893 MB）** 下好了，这里用 junction 直接复用，**没有占用额外磁盘**。

> ⚠️ 如果你以后**移动或删除** `C:\Users\25573\AppData\Roaming\.minecraft`，这个联接会失效，重新跑 `downloadAssets` 会开始联网下载。

### 丢失后的恢复命令

```powershell
# 1) 客户端 / 服务端 jar
$c = "$env:USERPROFILE\.gradle\caches\fabric-loom\26.3"
New-Item -ItemType Directory -Force -Path $c | Out-Null
curl.exe -sL -o "$c\minecraft-client.jar" "https://bmclapi2.bangbang93.com/version/26.3/client"
curl.exe -sL -o "$c\minecraft-server.jar" "https://bmclapi2.bangbang93.com/version/26.3/server"
(Get-FileHash "$c\minecraft-client.jar" -Algorithm SHA1).Hash   # 应为 e877b6a0...
(Get-FileHash "$c\minecraft-server.jar" -Algorithm SHA1).Hash   # 应为 33680f5f...

# 2) 资源索引
$i = "$env:USERPROFILE\.gradle\caches\fabric-loom\assets\indexes"
New-Item -ItemType Directory -Force -Path $i | Out-Null
curl.exe -sL -o "$i\26.3-34.json" "https://bmclapi2.bangbang93.com/v1/packages/1e4e4a68dece1c0f51b8f93b45da8fe8da51bcd3/34.json"
(Get-FileHash "$i\26.3-34.json" -Algorithm SHA1).Hash   # 应为 1e4e4a68...

# 3) 资源对象联接（.minecraft 里已有资源时）
$dst = "$env:USERPROFILE\.gradle\caches\fabric-loom\assets\objects"
$src = "C:\Users\25573\AppData\Roaming\.minecraft\assets\objects"
if (Test-Path $dst) { [System.IO.Directory]::Delete($dst, $true) }
New-Item -ItemType Junction -Path $dst -Target $src
```

---

## 7. 故障排查

| 现象 | 原因 / 解决 |
|---|---|
| `release version 25 not supported` | Gradle 跑在低版本 JDK 上。用 `.\dev.ps1`，或确认 `gradle.properties` 里 `org.gradle.java.home` 路径正确 |
| `Services.gradle.org` 下载超时 | wrapper 的 `distributionUrl` 被改回官方源了，换成腾讯云镜像 |
| `Failed download after 3 attempts` + `SSLHandshakeException` | 某个地方又指向了 Mojang 原始域名。对照 §5.4 检查；若 URL 是 `piston-data.mojang.com`，说明 §6 的预置文件丢了 |
| `Could not find com.mojang:patchy / text2speech` | `build.gradle` 里的 Sponge 仓库被删了 |
| 库里缺 `.pom` 无法解析 | `.minecraft\libraries` 里是 447 个 jar 但 **0 个 pom**，缺 Maven 元数据，**不能**直接当本地仓库用 |
| 日志刷 `authlib ... Connection reset` | 开发客户端尝试连 Mojang 账号服务失败，**离线开发正常现象，不影响功能** |
| 改了 `.minecraft` 路径导致资源重新下载 | 重建 §6 里的 junction |

---

## 8. 模组内容清单（More Ores）

### 8.1 占位符替换（已完成）

`modid` → `moreores`、`com.example` → `com.example.moreores`，同步改了这些地方：

1. `gradle.properties` → `group`
2. `settings.gradle` → `rootProject.name`
3. `build.gradle` → `loom { mods { "moreores" { ... } } }`
4. `src/main/resources/fabric.mod.json` → `id`、`icon`、`entrypoints`
5. `src/main/resources/moreores.mixins.json`、`src/client/resources/moreores.client.mixins.json`
6. `src/main/java/com/example/moreores/**`、`src/client/java/com/example/moreores/client/**`
7. `src/main/resources/assets/moreores/`

> 代码里一律用 `MoreOres.id("xxx")`（内部是 `Identifier.fromNamespaceAndPath`），不要手动拼 ID 字符串。

### 8.2 方块与物品

| 名称 | 类型 | 挖掘等级 | 说明 |
|---|---|---|---|
| `lead_ore` / `deepslate_lead_ore` | 方块 | 石镐 | 掉 1 粗铅，不给经验 |
| `uranium_ore` / `deepslate_uranium_ore` | 方块 | 铁镐 | 掉 1 粗铀 + 3~7 经验，发光等级 8 |
| `raw_lead_block` / `raw_uranium_block` | 方块 | 石镐 / 铁镐 | 9 个粗矿合成，粗铀块发光等级 9 |
| `lead_block` / `uranium_block` | 方块 | 石镐 / 铁镐 | 9 个锭合成，铀块发光等级 12 |
| `raw_lead` / `raw_uranium` | 物品 | — | 矿石掉落物，可烧成锭 |
| `lead_ingot` / `uranium_ingot` | 物品 | — | 熔炼产物 |
| `sulfur_powder` | 物品 | — | 硫磺粉，由硫磺块分解得到，火药原料（见 §8.5） |
| 铅制 / 铀制工具共 10 件 | 物品 | — | 剑、镐、斧、锹、锄各一套（见 §8.6） |

配方共 14 个：8 个熔炼（粗矿 / 矿石 / 深层矿石 → 锭）+ 6 个 3×3 合成与反向拆解。
另外还有 8 张掉落表、3 个 `minecraft` 标签（`mineable/pickaxe`、`needs_stone_tool`、`needs_iron_tool`）、2 个 `c` 约定标签。

### 8.3 26.3 与旧教程的差异（踩坑记录）

| 事项 | 26.3 实际情况 |
|---|---|
| 世界生成层数 | **`ConfiguredFeature` 类已被移除**，只剩 `worldgen/feature/`（已配置的 feature）+ `worldgen/placed_feature/` 两层 |
| 矿石特征参数 | `OreFeature(List<BlockReplacement>, size, discardChanceOnAirExposure)`，`BlockReplacement` 是 record `(RuleTest, BlockState)` |
| 物品模型 | **数据驱动物品模型**：必须写 `assets/<ns>/items/*.json`，形如 `{"model":{"type":"minecraft:model","model":"<ns>:block/xxx"}}`，否则物品没有图标 |
| 掉落表 | 简化为 `"condition": "minecraft:tool/can_silk_touch"`（单数字段）与 `"modifier": [...]`，不再用 `conditions` / `functions` |
| 方块 / 物品注册 | `Blocks` / `Items` 里没有 `register` 辅助方法，要 `Registry.register(BuiltInRegistries.BLOCK, key, block)`，且 **`Properties` 必须 `setId(ResourceKey)`** |
| 创造模式页签 | Fabric 模块更名为 `fabric-creative-tab-api-v1`，自定义页签用 `CreativeModeTab.builder(Row.BOTTOM, 0)` |
| 区块文件位置 | `world\dimensions\minecraft\overworld\region\*.mca`（不再是 `world\region\`） |

### 8.4 光影兼容（Complementary Reimagined r5.9.3）

光影通过 `shaders/block.properties` 里的**方块 ID 名单**决定谁发光，默认不认识 `moreores:*`，所以开箱不会发光。
Complementary 预留了两个空槽位：

| 槽位 | 用途 |
|---|---|
| `block.10024` | 模组矿石：光影会尝试检测纹理上的「矿石像素」发光（需在光影设置里开启 `Enable Glowing Ores`） |
| `block.10028` | 模组光源 / 自发光方块：整块发光 |

已填入 `shaderpacks\ComplementaryReimagined_r5.9.3\shaders\block.properties`：

```properties
block.10024=moreores:uranium_ore moreores:deepslate_uranium_ore
block.10028=moreores:uranium_block moreores:raw_uranium_block
```

> - 光影已经**从 `.zip` 解压成同名的文件夹**（改起来方便，Iris 同样识别），原始压缩包备份为 `ComplementaryReimagined_r5.9.3.zip.bak`。
> - **换光影包或光影更新覆盖后，这两行要重新填**。
> - 我们的矿石纹理是原版改色（石头底 + 彩色斑点），正好符合 `10024` 那个「检测 ore-pixels」的算法；如果换成整块纯色纹理，检测效果会变差。

### 8.5 新增的合成路径

| 配方 | 类型 | 产物 |
|---|---|---|
| 腐肉 → 皮革 | 熔炉熔炼（200 tick / 0.1 经验） | 1 皮革 |
| 硫磺块 → 硫磺粉 | 无序合成 | 9 硫磺粉 |
| 9 硫磺粉 → 硫磺块 | 3×3 有序合成 | 1 硫磺块（`minecraft:sulfur`） |
| 硫磺粉 + 煤/木炭 + 骨粉 | 无序合成 | **1** 火药（原 3 个，已削减）|
| 4 小麦种子（十字）+ 1 铅锭（中心） | 3×3 有序合成 | 4 含铅饼干 |

> - **硫磺粉是本模组新增的物品**：原版 26.3 只有硫磺方块 `minecraft:sulfur`（由硫磺尖刺 2×2 合成），没有粉状物品。
> - 「煤 / 木炭」用的是 26.3 的**内联列表**写法 `["minecraft:coal", "minecraft:charcoal"]`，与 `minecraft:fire_charge` 的结构完全一致（26.3 也支持 `#tag`，但原版风格是列表）。
> - 腐肉 → 皮革原版没有配方，不会冲突。
> - 配方文件都在 `data/moreores/recipe/`，新增配方不需要改 Java 代码。
> - **所有本模组配方都会自动解锁**：玩家背包里只要出现配方里的任意一样东西（产物**或**原料），对应配方就在合成书里亮起，不必先亲手合成过。实现见 `recipe/RecipeAutoUnlock.java` —— 服务端每 tick 扫一遍玩家背包，按启动时从 `RecipeManager` 现建的「物品 → 配方」索引解锁，零额外配置；新增配方也自动纳入（无需改 Java）。

**含铅饼干**（`moreores:lead_cookie`）

| 项 | 值 |
|---|---|
| 合成 | 十字形：上下左右 4 个小麦种子，中心 1 个铅锭 → 4 块 |
| 饱食度 | 与原版曲奇完全一致（`nutrition 2` / `saturationModifier 0.1` → 0.4 饱和度） |
| 特殊 | `FoodProperties.Builder#alwaysEdible()` —— **饥饿值满了也能吃**，当解毒药用 |
| 解毒 | 每块清掉 **200 点**辐射剂量（`ConsumableMixin`） |

> **踩坑**：配方的 `category` 只能填 `building` / `redstone` / `equipment` / `misc`，写 `"category": "food"` 会导致
> `IllegalStateException: Unknown element name:food`，整个配方加载失败。原版的食物配方干脆**省略**这个字段。

### 8.6 铅制与铀制工具

26.3 的工具已经完全组件化：材质是 `ToolMaterial` record（`ModToolMaterials`），
具体工具用 `Item.Properties.sword/pickaxe/axe/shovel/hoe`（`ModTools`）设置。

参数换算公式（与原版 `Items` 一致）：

```
最终攻击伤害 = 1 + 参数1 + 材质的 attackDamageBonus
最终攻击速度 = 4.0 + 参数2
```

**铅 —— 伤害对齐铁，攻速整体低 0.4**

| 工具 | 攻击伤害 | 攻击速度 | 对照铁工具 |
|---|---|---|---|
| 铅剑 | 6 | **1.2** | 铁剑 6 / 1.6 |
| 铅镐 | 4 | **0.8** | 铁镐 4 / 1.2 |
| 铅斧 | 9 | **0.5** | 铁斧 9 / 0.9 |
| 铅锹 | 4.5 | **0.6** | 铁锹 4.5 / 1.0 |
| 铅锄 | 1 | **2.6** | 铁锄 1 / 3.0 |

**铀 —— 完全对齐钻石**

| 工具 | 攻击伤害 | 攻击速度 | 耐久 |
|---|---|---|---|
| 铀剑 | 7 | 1.6 | 1561 |
| 铀镐 | 5 | 1.2 | 1561 |
| 铀斧 | 9 | 1.0 | 1561 |
| 铀锹 | 5.5 | 1.0 | 1561 |
| 铀锄 | 1 | 4.0 | 1561 |

> - 挖掘等级：铅镐 = 铁（挖不了钻石矿），铀镐 = 钻石（能挖黑曜石）。
> - 耐久 / 挖掘速度 / 附魔度：铅 250 / 6.0 / 14，铀 1561 / 8.0 / 10，分别由 `BlockTags.INCORRECT_FOR_IRON_TOOL`、`INCORRECT_FOR_DIAMOND_TOOL` 决定。
> - 修理材料用对应的锭，标签文件：`data/moreores/tags/item/lead_tool_materials.json`、`uranium_tool_materials.json`。
> - 以上数值是从 `minecraft-common-deobf-26.3.jar` 里反汇编 `ToolMaterial` 与 `Items` 的字节码读出来的，不是猜的。
> - 护甲见 [8.9](#89-铅制护甲--钛制护甲--铅桶--矿石生成调整)：铅甲数值对齐铁甲，
>   钛甲是「铁数值 + 铜耐久」，两套各带 **±5% / 件** 的移速。

#### ⚠️ `Item.Properties.xxx()` 不会登记物品标签，必须自己写数据包

踩过的坑：加完剑之后发现**没有横扫**。

原版判断能不能横扫看的是**物品标签**，不是组件（反汇编 `Player#isSweepAttack` 能看到它读 `ItemTags.SWORDS`）：

```java
private boolean isSweepAttack(boolean, boolean, boolean);
    57: getstatic  // Field net/minecraft/tags/ItemTags.SWORDS
```

而 `Item.Properties.sword(...)` 内部只是 `ToolMaterial.applySwordProperties(...)`，**挂的是 `TOOL` / 武器属性等组件，完全不碰物品标签** —— 标签是数据包的事。

所以每个工具/武器都要自己写一份标签，**追加**到原版同名标签里（`values` 默认是合并不是覆盖）：

```
data/minecraft/tags/item/swords.json     → moreores:lead_sword, moreores:uranium_sword
data/minecraft/tags/item/axes.json       → 两把斧
data/minecraft/tags/item/pickaxes.json   → 两把镐
data/minecraft/tags/item/shovels.json    → 两把锹
data/minecraft/tags/item/hoes.json       → 两把锄
```

```json
{
  "values": [
    "moreores:lead_sword",
    "moreores:uranium_sword"
  ]
}
```

这个标签同时影响**横扫**和**附魔**（「锋利」这类只对标签内的物品生效），所以漏了的话两样都会失效。

#### ⚠️ `requiresCorrectToolForDrops()` 必须配套写挖掘标签

高能熔炉曾一度**挖了不掉东西**：

```java
BlockBehaviour.Properties.of()
        .requiresCorrectToolForDrops()   // ← 声明了「需要正确工具才掉落」
        .strength(3.5F)
```

但 `data/minecraft/tags/block/mineable/pickaxe.json` 里**没有它** —— 于是原版认为
「没有任何工具能有效采集这个方块」，任何镐子挖下去都**不掉落**（方块本身还是能挖碎）。

规则是这两者必须成对出现：**开了 `requiresCorrectToolForDrops()`，就必须进某个
`mineable/*` 标签**。反之进了标签却没开这个属性，则会「什么工具都掉、但没工具时也掉」。

> 原版熔炉只在 `mineable/pickaxe` 里，**不在 `needs_stone_tool`** ——
> 也就是**木镐就能挖**。高能熔炉照它保持一致。
> （`needs_iron_tool` 那类标签是另一回事，管的是「最低需要什么等级的工具」。）

自查方式和配方那次一样，做差集：

```
ModBlocks 里带 requiresCorrectToolForDrops 的方块 → 9 个
mineable/* 标签里覆盖的                          → 9 个
差集                                              → 空
```

#### ⚠️ 26.3 的物品要「四件套」齐全，缺一个就是紫黑格

新加物品时最容易漏的是**物品定义**。26.3 里一个物品要在创造栏正常显示，需要：

| 文件 | 作用 | 位置 |
|---|---|---|
| 纹理 | 图案 | `assets/<ns>/textures/item/<id>.png` |
| 模型 | `item/generated` + layer0 | `assets/<ns>/models/item/<id>.json` |
| **物品定义** | **指向模型** | **`assets/<ns>/items/<id>.json`** ← 最容易忘 |
| 配方 | 合成表（可选） | `data/<ns>/recipe/<id>.json` |

物品定义的内容很简单，但**不写就是紫黑格**：

```json
{
  "model": {
    "type": "minecraft:model",
    "model": "moreores:item/nuclear_battery"
  }
}
```

> ⚠️ **注意**：只有纹理是不够的 —— 光画好 `<id>.png` 并不会自动生效，
> 后两个 json 都得有。这次加核子电池就漏了「定义」，游戏里直接是紫黑格。

自查方式是做差集：列出源码里 `register(...)` 的名字，逐个去这四个目录找。
判断方块物品时要跳过 `models/item`（它们走 `blockstates` + `models/block`）。

#### ⚠️ 加了物品别忘写配方

同一个疏忽的另一面：**注册了物品 ≠ 玩家能做出来**。

工具和护甲一开始只写了 `ModTools` / `ModArmor` 的注册代码，`data/moreores/recipe/` 里
一个配方文件都没有 —— 游戏里能 `/give` 拿到，但**合成台里搜不到**。

检查方法是把「注册的物品 id」和「配方产出的 id」做个差集，现在补全后是这样：

```
注册物品 38 个，有产出的 35 个
剩余 3 个无配方：4 个矿石方块（世界生成获得）+ 2 个实体类型（不是物品）
```

工具食谱照抄原版形状，材料走材质标签（和原版 `#minecraft:iron_tool_materials` 同一套写法）：

| 工具 | 图案 | 说明 |
|---|---|---|
| 剑 | `X` / `X` / `#` | X = `#moreores:lead_tool_materials`，`#` = 木棍 |
| 镐 | `XXX` / ` # ` / ` # ` | |
| 斧 | `XX` / `X#` / ` #` | |
| 锹 | `X` / `#` / `#` | |
| 锄 | `XX` / ` #` / ` #` | |

护甲直接指定锭（原版 `iron_helmet` 等也是直接写 `minecraft:iron_ingot`，没用标签）。
**铅甲用铅锭、钛甲用钛锭，两套形状完全一样**：

| 护甲 | 图案 |
|---|---|
| 头盔 | `XXX` / `X X` |
| 胸甲 | `X X` / `XXX` / `XXX` |
| 护腿 | `XXX` / `X X` / `X X` |
| 靴子 | `X X` / `X X` |

### 8.7 辐射（debuff）

效果 id `moreores:radiation`，有害类，荧光绿。核心是**剂量（dose）**这套两段式设计：
环境和手持物品先累积成一个 0~1000 的隐藏数值，剂量再按档位决定挂几级 debuff。

```
每秒剂量 = 周围铀方块 + 手持铀制品        （铅可以屏蔽一部分）
剂量     = clamp(剂量 + 获得 - 3/秒衰减)  （0 ~ 1000）

150+ → 辐射 I     每 2 秒 1 点魔法伤害
400+ → 辐射 II    每 1.25 秒 1 点魔法伤害
700+ → 辐射 III   每 0.6 秒 1 点魔法伤害 + 挖掘疲劳
```

**辐射源**

| 来源 | 每秒剂量 |
|---|---|
| 铀矿石 / 深层铀矿石（周围 9×9×9 内，每个） | +3 |
| 粗铀块（每个） | +6 |
| 铀块（每个） | +12 |
| 手持铀锭 / 粗铀 | +6 |
| 手持铀制工具 | +20 |
| **挖掉一块铀矿** | **一次性 +25** |

**环境辐射的累积上限是 40/秒**（防止玩家盖铀块房子把自己秒了）。这个上限只管**环境与手持**那一路 ——
**挖矿当场吃到的一次性 25 点是直接加在剂量上的，不受它限制**。

**铅防护**

| 来源 | 屏蔽 |
|---|---|
| 铅甲（头盔 / 胸甲 / 腿甲 / 靴子） | **每件 25%** |

**只有穿在装备栏上的护甲算，背包里的铅锭 / 铅块不算。**

> ⚠️ 曾经「背包里揣铅锭也能挡辐射」：每个铅锭 15%、每个铅块 **100%**。
> 已删除 —— 那样往背包塞几个铅块就白嫖满屏蔽，穿护甲的代价（每件 -5% 移速）
> 被完全绕开，护甲本身反而没人穿了。

- 铅甲按**件数**算，穿满四件正好 100% —— 此时剂量**完全不再累积**。
- 铅甲的代价是**每件 -5% 移速**，四件合计 **-20%**。移动速度是直接写在
  物品自带的属性组件里的，不再由辐射系统每 tick 施加（详见 8.9）。
- 屏蔽只削减**新吃到**的剂量，已有剂量照常衰减。

**恢复方式**

- 离开辐射源，剂量以 3/秒 自然衰减（从 I 级阈值降到 0 约 50 秒）
- **喝牛奶会清掉全部剂量**（见下）
- 剂量存在 Fabric Attachment 里且**故意不持久化**，所以退出重进游戏相当于「休养归零」，不会被永久困住

**牛奶怎么生效的**：Mixin 注入 `ItemStack#finishUsingItem`（`MilkBucketMixin`）。
26.3 的牛奶桶已经不是一个单独的类了 —— `MilkBucketItem` 不存在，它只是一个普通 `Item` + `Consumable` 组件，
所以只能挂在所有物品共用的 `finishUsingItem` 上，再判断 `stack.is(Items.MILK_BUCKET)`。

**铀制工具 / 武器**

| 特性 | 实现 |
|---|---|
| 打中生物 → 目标获得辐射 I（10 秒） | `AttackEntityCallback`，连续攻击会刷新持续时间 |

> 铀制工具**不加附魔光效**：试过用 `DataComponents.ENCHANTMENT_GLINT_OVERRIDE` 做常驻闪光，
> 但和真附魔的物品分不清，已移除。

**身上的粒子**：`MobEffectInstance` 的 `visible` 必须是 `true`，粒子才会显示 ——
粒子本身也换成了自定义的荧光绿辐射尘，详见 §8.13。

**动作栏提示**：剂量跨档时会给一次提示（轻度 / 中度 / 重度暴露）并播一声紫水晶音效。

> **26.3 的坑**：动作栏提示不叫 `displayClientMessage` 了，改成了 `ServerPlayer#sendOverlayMessage(Component)`。
> 状态效果用 `Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, id, effect)` 拿到 `Holder<MobEffect>`。

### 8.8 弓与箭

基准：原版弓（耐久 384 / 射程 15）与原版箭（基础伤害 2.0）。

| 物品 | 数值 | 特殊 |
|---|---|---|
| 铅弓 | 耐久 **250**（对齐铁制工具）/ 射程 15 | — |
| 铀弓 | 耐久 **1561**（对齐钻石）/ 射程 **20** | — |
| 铅箭 | 基础伤害 **2.5** | — |
| 铀箭 | 基础伤害 **3.0** | 命中生物 → **辐射 I（8 秒）** |

配方（形状照抄原版，只把材料换成金属锭）：

```
铅箭          铅弓
 L            " LX"      L/U = 铅锭 / 铀锭
 S            "L X"      X   = 线
 F            " LX"      S   = 木棍
→ 4 支        → 1 把     F   = 羽毛
```

**实现要点**

- `RangedBowItem extends BowItem`：只改了两处 —— `getDefaultProjectileRange()`（射程）和 `getAllSupportedProjectiles()`
- `RangedArrowItem extends ArrowItem`：覆写 `createArrow()` 后 `setBaseDamage(...)`
- 铀箭的辐射：`ServerLivingEntityEvents.AFTER_DAMAGE` 里判断
  `source.getDirectEntity() instanceof AbstractArrow` 且 `arrow.getPickupItemStackOrigin()` 是我们的铀箭

> ⚠️ **最大的坑**：原版弓的 `getAllSupportedProjectiles()` 写死了只认 `arrow` / `tipped_arrow` / `spectral_arrow` 三种原版箭，
> **不覆写的话我们自己的箭根本射不出去**（拉弓没反应）。改成 `stack.getItem() instanceof ArrowItem` 即可。
>
> ⚠️ **弓的模型**：原版弓有 4 个状态，数据驱动模型要用 `condition(using_item)` + `range_dispatch(use_duration, scale 0.05)` 组合，
> 每个拉弓档位还有独立纹理（`bow` / `bow_pulling_0/1/2`）。直接照抄原版 `assets/minecraft/items/bow.json` 的结构。
> 手持姿态（`display` 段）也必须从原版 `models/item/bow.json` 抄过来，否则第一/第三人称拿弓的姿势是错的。
>
> **拉弓速度没有改**：原版满力时间由 static 的 `BowItem.getPowerForTime` 决定，想改必须重写整个 `releaseUsing`，代价太大，放弃了。

**弓的纹理怎么只染弓身**

原版 `bow.png` 的颜色构成非常规整（数了一遍）：

| 部位 | 颜色 | 饱和度 |
|---|---|---|
| 弓身 | `#493615` / `#896727` 等棕色 | **0.71** |
| 弓弦 | `#444444` / `#6B6B6B` / `#969696` 灰阶 | **0.00** |

所以染色时跳过 `saturation < 0.10` 的像素，弓弦就保持原样了。

**射出去的箭怎么换成自己的建模**

箭实体长什么样是 `EntityType` 决定的，所以做了：

1. 注册 `moreores:metal_arrow` 实体类型（铅箭铀箭**共用**一个，靠箭身上带的物品区分纹理）
2. `MetalArrow extends Arrow` —— 自定义实体类
3. 客户端 `MetalArrowRenderer extends ArrowRenderer<MetalArrow, MetalArrowRenderState>`，按 RenderState 里的标记选纹理
4. 纹理：`assets/moreores/textures/entity/projectiles/{lead,uranium}_arrow.png`

> ⚠️ **自定义箭实体最大的坑**：`AbstractArrow` 确实有一个带 `EntityType` 的 protected 构造，但
> **Java 不允许跳过直接父类去调祖父类的构造**，而 `Arrow` 的公开带射手构造里把 `EntityType`
> 写死成了 `EntityTypes.ARROW`。所以初始化只能自己复现一遍：
> `setPos` / `setOwner` / `setPickupItemStack` 都能正常调，
> 但保存「发射武器」的 `firedFromWeapon` 是 **private 且没有 setter** ——
> 最后加了 Mixin `@Accessor`（`AbstractArrowAccessor`）补一个，
> 不设置的话弓的附魔（力量、火矢、穿刺）对射出的箭全部不生效。
>
> ⚠️ **26.3 的渲染器取纹理拿到的是 RenderState，不是实体**（`getTextureLocation(S state)`），
> 所以「这是铅箭还是铀箭」必须在 `extractRenderState` 阶段写进自定义的 RenderState 里。

---

### 8.9 铅制护甲 / 钛制护甲 / 铅桶 / 矿石生成调整

**两套护甲**

| 部件 | 护甲值 | 耐久倍率 | 附魔度 | 韧性 | **移速** |
|---|---|---|---|---|---|
| 铅头盔 | 2 | 15 | 9 | 0 | **-5%** |
| 铅胸甲 | 6 | 15 | 9 | 0 | **-5%** |
| 铅护腿 | 5 | 15 | 9 | 0 | **-5%** |
| 铅靴子 | 2 | 15 | 9 | 0 | **-5%** |
| 钛头盔 | 2 | **11** | 9 | 0 | **+5%** |
| 钛胸甲 | 6 | **11** | 9 | 0 | **+5%** |
| 钛护腿 | 5 | **11** | 9 | 0 | **+5%** |
| 钛靴子 | 2 | **11** | 9 | 0 | **+5%** |

- **铅甲**数值完全对齐铁甲；代价是**每件 -5% 移速**，四件刚好 **-20%**。
- **钛甲**沿用工具的设定「**铁的数值 + 铜的耐久**」：护甲值 / 附魔度同铁，
  耐久倍率换成铜的 **11**（铁是 15），换来**每件 +5% 移速**，四件 **+20%**。

两套混穿时**逐件相加**，所以「2 件钛 + 2 件铅」正好抵消（实测 0.00%）。

26.3 的护甲也是组件化的：`ArmorMaterial` record + `Item.Properties.humanoidArmor(材质, ArmorType)`。
材质最后的 `assetId` 是 1.21.2 新增的**穿戴外观资源键**，指向 `assets/moreores/equipment/<名字>.json`，
再由它指向 `textures/entity/equipment/{humanoid,humanoid_leggings}/<名字>.png`。
钛甲那套纹理是把铅甲按同一套「保亮度换色」推导出来的（平均亮度 158 → 199，
和钛锭 +47、钛剑 +46 的幅度一致）。
修理分别用铅锭 / 钛锭（`data/moreores/tags/item/{lead,titanium}_armor_repair.json`）。

#### 移速是怎么挂上去的

直接写在物品的 `ATTRIBUTE_MODIFIERS` 组件里（`ModArmor.register`），
穿上就生效、脱下就恢复 —— **不需要**每 tick 手动加减修饰符。踩过三个坑：

**① `humanoidArmor` 内部会写这个组件，必须「在它之后合并」，不能直接覆盖。**

`Item.Properties.humanoidArmor(材质, 类型)` 内部就是
`attributes(材质.createAttributes(类型))`。所以：

```java
properties.humanoidArmor(material, type)                       // 先让它写
        .attributes(material.createAttributes(type)             // 再基于材质给的那份
                .withModifierAdded(MOVEMENT_SPEED, ..., slot))  // 追加一条
```

要是省掉 `createAttributes` 直接塞一份自己的，**护甲值会被一起抹掉**，穿上就是纸糊的。

**② 修饰符 ID 每件必须不同。**

属性表是按 ID 存的，四件都叫 `..._speed` 会互相覆盖，表现为「穿了四件只生效一件」。
这里用 `<物品名>_speed`，天然每件一个。

**③ 运算方式不能用 `ADD_MULTIPLIED_TOTAL`，要用 `ADD_MULTIPLIED_BASE`。**

属性结算的顺序是：

| 运算 | 多个修饰符之间 |
|---|---|
| `ADD_VALUE` | **相加** |
| `ADD_MULTIPLIED_BASE` | **相加**（每个加「基础值 × 系数」）|
| `ADD_MULTIPLIED_TOTAL` | **叠乘**（每个把总值 ×(1+系数)）|

四件护甲各带一个 `ADD_MULTIPLIED_TOTAL(0.05)`，乘出来是 `1.05⁴ = 1.2155` ——
**+21.55% 而不是 +20%**（铅甲同理只有 -18.55%）。这个偏差实测抓到的：

```
钛甲：+5.00% → +10.25% → +15.76% → +21.55%   ← 不是 20%
```

换成 `ADD_MULTIPLIED_BASE(0.05)` 后四件各自加「基础值 × 5%」，加起来正好 +20% ✓。

**④ 想让一行提示出现在「穿在身上时：」里，就只能用属性修饰符。**

抗辐射原本是 `RadiationManager` 里自己算的隐藏机制，**游戏根本不知道它存在**，
所以那一栏列不出它来。第一版给它挂了 `DataComponents.LORE`，结果是：

```
每件 -25% 辐射                 ← 斜体、紫色，和上面那栏风格完全不一样
四件齐了完全屏蔽，剂量不再累积
```

这类 lore 是"物品说明"，不是"属性"，观感和 `-5% 速度` 对不上。
要长得一样，就得真的走属性 —— 于是注册了一个自定义属性
`moreores:radiation_shielding`，让铅甲像挂移速那样挂它。

> ⚠️ **提示里显不显示百分号，取决于「运算方式」，不是属性本身。**
>
> 原版那套 lang 键是按运算方式的**序号**分的：
>
> | 键 | 格式 | 运算方式 |
> |---|---|---|
> | `attribute.modifier.take.0` | `-%s %s` | `ADD_VALUE` |
> | `attribute.modifier.take.1` | `-%s%% %s` | `ADD_MULTIPLIED_BASE` |
> | `attribute.modifier.take.2` | `-%s%% %s` | `ADD_MULTIPLIED_TOTAL` |
>
> 所以「-5% 速度」带 `%` 只是因为移速用的是 `ADD_MULTIPLIED_BASE`。
> 抗辐射照抄同一种运算方式，出来的就是 `-25% 辐射`；
> 要是用 `ADD_VALUE`，会变成 `-0.25 辐射`。

> ⚠️ **属性的默认值不能是 0。** `ADD_MULTIPLIED_BASE` 加的是
> 「**默认值** × 系数」，默认值是 0 的话乘出来永远是 0，修饰符等于没挂。
> 所以 `radiation_shielding` 的默认值取 **1.0**（"100% 的辐射都穿透"），
> 每件铅甲 -0.25，四件正好归零。

**顺带消掉了一个隐患**：系数原本写死在两处（物品上的修饰符 + `RadiationManager`
的 `SHIELD_PER_ARMOR_PIECE`），改一处忘一处就会"提示显示 25%、实际算 20%"。
现在 `armorShielding` 改成**去读护甲物品自带的修饰符**，真值只留一份。

#### 验证方法（顺带记两个坑）

装备属性是**每 tick 结算**的，所以验证时不能一次连发多条命令 —— RCON 会把它们
全塞进同一个 tick，读到的永远是旧值（第一版验证脚本就是这么被骗的，四件全显示 +0.00%）。
每条命令之间要留出时间。

另外两点：

- **`/data get` 不输出物品的默认组件**（只给 `{count, id}`），所以组件内容读不出来。
  想核对得用 `/execute if items ... <物品>[组件=值]` 去**下断言**。
- **僵尸没有 `attack_speed` 属性**（这个属性只有玩家有），所以工具攻速只能在物品侧断言；
  而 `attribute_modifiers` 是列表，`~` 部分匹配会报 `Not a map`，必须**整表精确匹配**。
  更坑的是 amount 在字节码里是 `fload; f2d` —— **先算 float 再转 double**：
  `-2.0F` 转 double 恰好是 `-2.0`，但 `-2.8F` 是 `-2.799999952316284`，
  按十进制字面量写就对不上。得先按 float32 走一遍再加宽。

实测结果（给 NoAI 僵尸穿装备，再用 `/attribute` 读结算值）：

```
空身 移速 0.230000
钛甲 每件 +0.011500（= 基准 × 5%）  四件累计 +20.00%   护甲 +15
铅甲 每件 -0.011500                  四件累计 -20.00%   护甲 +15
2 钛 + 2 铅                          累计  +0.00%       护甲 +15
```

**铅桶**：3 个铅锭（V 字形）→ 1 个原版桶，和原版铁锭配方并存。

**矿石生成调整**（`data/moreores/worldgen/`）

- **铅矿照抄煤矿的两段结构**：
  - `lead_ore_lower`：埋藏型 feature（`discard_chance_on_air_exposure = 1.0`），count **20**，梯形分布 y 0~192
  - `lead_ore_upper`：普通 feature，count **30**，均匀分布 y 136~世界顶部
  - feature 的 `size` 从 9 提到 **17**（煤矿同款）
- **铀矿提升**：size 5 → **8**，count 2 → **4**，高度上限 -4 → 0，丢弃率 0.8 → **0.5**

实测（新世界 + RCON `forceload` 256 区块，共 **569 个完整地形区块**）：

| 矿石 | 出现区块数 | 占比 | 改动前（同方法不可比） |
|---|---|---|---|
| 铅矿 | 522 | **91.7%** | — |
| 深层铅矿 | 70 | 12.3% | — |
| **煤矿（原版对照）** | **528** | **92.8%** | — |
| 铁矿（原版对照） | 509 | 89.5% | — |
| 深层铀矿 | 498 | **87.5%** | — |
| 钻石矿（原版对照） | 74 | 13.0% | — |

> 铅矿 91.7% vs 煤矿 92.8% —— **生成频率已经和煤矿基本重合**。
>
> ⚠️ 注意铀矿现在 87.5% 的区块都有（比钻石矿的 13% 常见得多）。因为铀矿限制在 y -60~0
> 且丢弃率 0.5，单个区块里的数量不多，但如果觉得「满世界都是铀」，把 `uranium_ore.json` 的
> `size` / `count` 各降一档即可。

### 8.10 纹理重绘：为什么别用「线性 tint」

粗铅 / 粗铀 / 铅块 / 铀块这四张最早是把原版纹理的**明度**线性插值到一条
「暗色 → 亮色」的色带上：

```python
out = dark + (light - dark) * value      # ❌ 色相与饱和度被写死
```

问题在于这条公式把所有像素压到**同一个色相、同一个饱和度**，原图的多色层次全丢了 ——
铅块和铀块看着就是一块纯色板。

改成 **HSV 重映射**：

```python
h, s, v = rgb_to_hsv(pixel)
out = hsv_to_rgb(目标色相, max(s * sat_scale, 最低饱和度), 对比增强后的 v)   # ✅
```

色相换掉，但**饱和度与明度保留原图的比例**。这一步很关键 —— 原版 `raw_iron` 是
h≈0.07、s 0.21~0.49 的暖棕，那个饱和度差正是「石头 / 金属颗粒」的层次来源，压平了就成色块了。

另外做了三件事：

| 处理 | 说明 |
|---|---|
| 对比增强 | 明度以 0.5 为轴拉伸（`iron_block` 的 v 只在 0.73~0.95，不拉根本看不出结构） |
| 细微噪点 | 每像素随机 ±0.03 明度扰动，做出哑光金属的粗糙感 |
| 辐射三叶形 | 铀块用和状态效果图标**同一套几何**画了个标记（核心圆 + 3 个叶片） |

参考数据（反汇编不出，直接数像素得到的）：

```
iron_block : #E6E6E6 x97  s=0.00 v=0.90   ← 纯灰阶，只有明暗
raw_iron   : #AF8E77 x27  h=0.07 s=0.32   ← 暖棕，饱和度高的是金属部分
             #3B3429 x24  h=0.10 s=0.31   ← 暗部
```

---

> **本节之后的编号有断档（缺 8.11 / 8.12 / 8.14）。**
> 那三节写的是**辐射僵尸**和**女仆**两个生物，后来按需求把生物整个删掉了
> （见 §8.31）。编号没重排 —— 正文里有几十处「见 §8.x」的交叉引用，
> 重排的收益不值这个风险。

### 8.13 自定义辐射粒子

原版效果自带的是「气泡 + 颜色」那套粒子，我们换成了自己的荧光绿辐射尘。

**26.3 的踩坑记录**

| 事项 | 26.3 实际情况 |
|---|---|
| 继承哪个粒子基类 | **`SingleQuadParticle`**（旧的 `TextureSheetParticle` 已被删掉） |
| 必须实现的方法 | `getLayer()` 变成抽象方法，返回 `Layer.TRANSLUCENT` |
| 注册 `SimpleParticleType` | 构造函数是 **protected**，外模组不能 `new`，要走 `FabricParticleTypes.simple()` |
| 客户端工厂 | `ParticleProviderRegistry.getInstance().register(type, Provider::new)`，`Provider` 会拿到 `SpriteSet` |
| 效果粒子怎么绑 | `MobEffect(MobEffectCategory, int, ParticleOptions)` 的第三个参数，不传就是原版药水气泡 |

**代码位置**

| 文件 | 作用 |
|---|---|
| `ModParticles.RADIATION` | 双端注册粒子类型 |
| `client/render/RadiationParticle` | 仅客户端：粒子本体 + `Provider` |
| `effect/RadiationEffect` | 构造时把粒子传给 `MobEffect` |
| `assets/moreores/particles/radiation.json` | 贴图清单 |
| `assets/moreores/textures/particle/radiation_0..3.png` | 4 帧脉动贴图 |

**粒子的两个必要条件**（少一个就什么都看不到）：

1. **`MobEffectInstance` 的 `visible` 参数必须是 `true`**。构造签名是
   `(effect, duration, amplifier, ambient, visible, showIcon)`，第 5 个才是它 ——
   之前环境结算 / 近战 / 铀箭三处全传了 `false`，所以生物身上一个粒子都没有，这就是「粒子丢失」的根因。
2. 效果的 `ParticleOptions` 要指向已经注册的粒子类型（只注册粒子、不给效果传，还是原版气泡）。

> 粒子的「闪烁」靠 `setSpriteFromAge(sprites)` 逐帧换贴图做，不是靠改 alpha —— 低画质下也稳。

### 8.15 牛奶瓶

`moreores:milk_bottle` —— 牛奶的便携版。喝下去效果和牛奶桶**完全一致**
（清除全部状态效果 + 辐射剂量清零），但有三个差别：

| | 牛奶桶 | 牛奶瓶 |
|---|---|---|
| 堆叠 | 1 | **16** |
| 喝完 | 变空桶 | **返还玻璃瓶** |
| 获取 | 对牛用桶 | 玻璃瓶挤奶 / 牛奶桶 + 8 玻璃瓶 |

**物品定义**（关键在于「直接复用原版牛奶的消耗组件」）

```java
public static final Item MILK_BOTTLE = register("milk_bottle", Item::new,
        new Item.Properties()
                .stacksTo(16)                                        // 堆叠 16
                .usingConvertsTo(Items.GLASS_BOTTLE)                 // 喝完返还玻璃瓶
                .craftRemainder(Items.BUCKET)                        // 合成时返还桶
                .component(DataComponents.CONSUMABLE, Consumables.MILK_BUCKET));
```

> `Consumables.MILK_BUCKET` 就是原版牛奶桶用的那个 `Consumable`
> （内部是 `ClearAllStatusEffectsConsumeEffect`）。**直接复用而不是自己拼效果** ——
> 以后原版改牛奶行为，牛奶瓶跟着一起变。
>
> 另外 `usingConvertsTo`（使用时返还）和 `craftRemainder`（合成时返还）是两个不同的组件，
> 可以同时挂，分别管「喝」和「合成」两条路径。

**配方**：牛奶桶居中、周围 8 个玻璃瓶 → 8 个牛奶瓶（`craftRemainder` 自动返还桶）。
写在 `data/moreores/recipe/milk_bottle.json`。

**挤奶**：原版的「空桶 → 牛奶桶」写死在 `AbstractCow#mobInteract` 里，加不进更多物品，
所以挂 Fabric 的 `UseEntityCallback` 自己接一档（`interaction/MilkBottleHandler`）：

- 判定条件与原版一致：**成年牛**才能挤（小牛不给）
- 用 `ItemUtils.createFilledResult` 处理「手上只有 1 个瓶 / 有一叠瓶」两种情况
- 客户端只返回 `SUCCESS` 做挥手动画，发物品在服务端做

**纹理**：基于原版蜂蜜瓶（形状已经是「装满的瓶子」），把暖橙色的蜂蜜液体
按 HSV 重映射成乳白 —— 保留液体原本的明暗层次，不是涂成一块死白。

> **验证堆叠上限的小技巧**：`item replace ... with <item> 20` —— 若上限是 16，
> 游戏会直接回 `moreores:milk_bottle can only stack up to 16`；
> 而 `data get Items[0].count` 是**不可靠**的（命令直接写 NBT，给 16 个连牛奶桶都会显示 16）。

### 8.16 盖革计数器

`moreores:geiger_counter` —— **放在背包里**就会按环境辐射强度发出「咔哒」声。

**配方**：2 铅锭 + 2 红石 + 1 金锭

```
. L .
R G R
. L .
```

判定「有没有带着」的逻辑在 `client/GeigerPresence#isCarried`（含快捷栏、副手）。
响铃的频率规则、以及为什么不做 HUD，见 §8.17。

> **HUD 被移除了。** 曾经实现过一个左上角的五读数面板（剂量 / 环境 / 进度条 / 档位），
> 后来因为实用性不高去掉了 —— 盖革计数器的核心体验是「听声音判断危险程度」，
> 盯着数字看反而多余。相关踩坑记录保留在下面，将来要做 HUD 时直接可用。

**剂量不往客户端同步。** 咔哒声用的是客户端自己按同样规则算出的「环境辐射强度」
（`RadiationManager.exposurePerSecond` 的参数是 `Level`，双端都能调），
而不是服务端的累积剂量 —— 所以既不需要 `syncWith`，也不需要自定义网络包。

> 顺带记一笔：如果以后真要把「累积剂量」显示到 HUD 上，光加 `syncWith` **是不够的** ——
> Fabric 的附件只在「玩家加入 / 切换维度 / 实体开始被追踪」这三个时机自动同步，
> **运行时改值不会推送**，数字会一直停在进服那一刻的值。
> 那时需要改值时主动推一次（`AttachmentSync.trySync` + `AttachmentChange`，
> 位于 Fabric 的 `impl` 包但方法是 public），或者干脆自己发网络包。

---

以下为**当年实现 HUD 时踩过的坑**，保留备查。

**26.3 的 HUD API 换了一整套**

| 事项 | 旧写法 | 26.3 |
|---|---|---|
| 注册 | `HudRenderCallback.EVENT.register(...)` | `HudElementRegistry.attachElementAfter(锚点, id, element)` |
| 回调签名 | `onHudRender(GuiGraphics, DeltaTracker)` | `extractRenderState(GuiGraphicsExtractor, DeltaTracker)` |
| 画文字 | `GuiGraphics#drawString` | `GuiGraphicsExtractor#text(Font, Component, x, y, color, shadow)` |
| **文字颜色** | `0xFFFFFF`（纯 RGB） | **`0xFFFFFFFF`（必须带 alpha！）** |
| 隐藏 HUD 判断 | `minecraft.options.hideGui` | **这个字段没了** —— 也**不用自己判断**，F1 隐藏时整个提取阶段会被跳过 |

> ⚠️ **挂 HUD 用 `addFirst`，不要用 `addLast`，也不要用 `attachElementAfter`。**
> 这个坑踩了两轮，最后是翻 Fabric 源码才定位的：
>
> | 写法 | 实际挂在哪 | 结果 |
> |---|---|---|
> | `attachElementAfter(MOB_EFFECTS, ...)` | MOB_EFFECTS 层 | 玩家没状态效果时整层不渲染 → **不显示** |
> | `addLast(...)` | **SUBTITLES 层**（源码里 `LAST = ROOT_ELEMENTS.get(SUBTITLES)`） | `Hud#extractSubtitleOverlay` 是在**界面状态的条件分支里**才调用的 → **不显示** |
> | **`addFirst(...)`** | MISC_OVERLAYS 层 | 该层由 `Hud#extractCameraOverlays` 渲染，在 `Hud#extractRenderState` 里是**无条件调用** → ✅ |
>
> ```java
> HudElementRegistry.addFirst(MoreOres.id("geiger_counter"), GeigerHud::extractRenderState);
> ```
>
> 官方 javadoc 只说「`addFirst` / `addLast` 不继承任何渲染条件」，但**没提层自身的调用时机** ——
> `addLast` 所在的 SUBTITLES 层本身就要看界面状态，这是个隐藏坑。

> ⚠️ **颜色必须带 alpha：写 `0xFFFFFFFF`，不要写 `0xFFFFFF`。**
>
> 这是第二个坑，比上面那个更隐蔽。反编译 `GuiGraphicsExtractor#text` 的字节码，方法体第一件事是：
>
> ```
> 0: iload 5                        // color
> 2: invokestatic ARGB.alpha(I)I    // 取 alpha 位
> 5: ifne 9                         // 非 0 才继续
> 8: return                         // alpha == 0 → 直接返回，一个字都不画
> ```
>
> `0xFFFFFF` 的 alpha 位是 **0**，所以整段 HUD 被静默丢弃：代码不报错、回调确实被调用
> （日志能打出来）、坐标也正常，就是屏幕上什么都没有。跟了很久才发现。
>
> 另外注意 `Component#withStyle(ChatFormatting.XXX)` 提供的颜色是**自带 alpha** 的，
> 所以理论上带样式的行能显示 —— 但外层这个 alpha 检查在**进入样式之前**就 return 了，
> 于是什么都画不出来。

### 8.17 盖革计数器的「咔哒」声

**背包里有**（不要求拿在手上）就会响，频率跟着**环境辐射强度**走：

| 环境辐射（每秒剂量） | 响铃间隔 |
|---|---|
| 0 | **完全不响** |
| 1 | 约 21 tick（≈1 次/秒） |
| 20 | 12 tick |
| 40（上限） | **2 tick（≈10 次/秒，最快）** |

- 客户端 `ClientTickEvents.END_CLIENT_TICK` 实现，无需服务端参与
- 音效借用原版的拉杆声（`SoundEvents.LEVER_CLICK`）—— 自定义音效要额外打包 ogg，这里没必要
- 用 `level().playLocalSound(...)` 只在本机播放，不会打扰别人

**为此把辐射计算开放给了客户端**：`RadiationManager.exposurePerSecond` 的参数
从 `ServerPlayer` 改成了 `Level`（返回值和逻辑完全不变）—— 方块数据客户端本来就有，
自己算一遍比再加一条同步通道简单。

> 顺带一提，这个「环境辐射强度」和 HUD 上显示的**累积剂量**是两个不同的量：
> 前者是「每秒吃进来多少」（0~40，决定响铃快慢），后者是「体内攒了多少」（0~1000，决定扣血档位）。
> HUD 显示的是累积剂量，响铃频率用的是环境强度。

**纹理**：手绘的 16×16（程序生成，没有用原版改色）—— 深色机身、顶部探测管、
中间一块绿色荧光柱状读屏、左上角红色指示灯、底部散热格栅。

> `RadiationManager.exposurePerSecond` 现在同时被服务端 tick 和客户端响铃调用，
> 两边算的是同一套逻辑，所以读数不会出现「HUD 说安全、耳朵却在狂响」的割裂。

> 物品本身零逻辑（就是个加了 `stacksTo(1)` 的普通 `Item`），显示完全由
> `client/hud/GeigerHud` 每帧检查「手上有没有这件东西」决定。
>
> 早期纹理是基于原版时钟的第 0 帧改的 —— 注意**时钟是多帧的，没有单张 `clock.png`**
> （`clock_00` ~ `clock_63`，按时间切换），直接读 `clock.png` 会报 KeyError。
> 后来嫌「太板正」（就是一块深色方盒子），重画成了八角外壳 + 顶部提手 +
> 连通波形屏幕 + 红色指示灯，和核子电池一起重绘。

#### 物品图标怎么「去板正」

核子电池和盖革计数器最初都是硬边矩形，摆一排显得很死。改法不是加细节，而是**先定轮廓**：

| 手段 | 例子 |
|---|---|
| 切角 / 台阶 | 计数器四角各切两级做成八角形；电池顶盖比机身窄一格，形成台阶 |
| 整行渐变 | 电池外壳上亮下暗（量化成 4 档），做出柱面的柔和光 |
| 内部形状 | 核心用**六边形**而不是竖条；屏幕用**连通波形**而不是孤立的方块 |
| 非对称零件 | 提手、指示灯、单颗铆钉 —— 打破左右镜像 |

⚠️ 两个踩过的坑：

1. **收腰**（把中间两行缩进去）本意是增加轮廓变化，结果生成了悬空的孤立色块，反而更乱。
2. **斜向高光扫一道**在 16×16 下读成「一道脏印」。想表现金属光泽，
   用**整行渐变**（不产生游离像素）比斜条纹安全得多。

> 做法上值得记一条：先写一张**轮廓表** `y -> (左边界, 右边界)`，再往里填色。
> 比逐像素 `putpixel` 好维护得多，也不会不小心留下游离像素。

---

### 8.18 矿物种植

**小麦模式**：先用「矿物 + 一圈小麦种子」合成**矿物种子**，再用种子种植。

```
W W W        W = 小麦种子
W M W        M = 对应矿物（锭 / 宝石）
W W W        → 产出 8 个该矿物的种子
```

**种植**：手持矿物种子右键耕地即可（`UseBlockCallback`，种子只是普通 `Item`，
不需要 `ItemNameBlockItem` 那种自带放置逻辑的类）。作物方块**没有对应的物品**。

**目前共 13 种作物**：模组自己的**铅、铀、钛**，原版的铁、金、铜、钻石、绿宝石、青金石、
红石、煤炭、下界石英，外加**下界合金**。
注册逻辑是**数据驱动**的（`ModCrops` 里一张 `种子物品 → 作物方块` 表），加新矿物只要在表里加一行。

#### 三种模组作物各有各的性格

| 作物 | 特色 | 怎么实现的 |
|---|---|---|
| 铅 | 平平无奇，当基准用 | 全默认配置 |
| **铀** | **会照亮周围**（成熟更亮）+ 持续放辐射 | `lightLevel`（未熟 3 / 成熟 7）+ `emissiveRendering` |
| **钛** | **长得快**（约 2 倍）+ **暗处反光** | 覆写 `randomTick` 多掷一次骰子 + `emissiveRendering` |

钛这两个特色都落在它的设定「**轻 + 亮**」上（盔甲 +5% 移速、工具 +0.4 攻速，都是「轻」）。

**「暗处反光」和铀的「照亮环境」是两件不同的事**，容易混：

| | 它自己的纹理 | 周围方块 |
|---|---|---|
| 铀作物 | `emissiveRendering` → 不随环境光变暗 | `lightLevel 7` → **真的把地面照亮** |
| 钛作物 | `emissiveRendering` → 不随环境光变暗 | 什么都不做（`lightLevel` 保持 0）|

也就是：铀是**光源**，钛只是**自己发亮**（像一块反光的银板）。

**「长得快」的做法**（`TitaniumCropBlock`）：没有重写生长逻辑，只是把父类那一次
「随机刻掷骰子」**多掷一遍** —— 原版每次以 `1 / (25 / 生长速度 + 1)` 的概率长一级，
掷两次大约翻倍。光照检测、骨粉、掉落全部照旧走父类。

⚠️ 多掷的那一遍要**重新取一次方块状态**：上一掷可能已经让它长了一级，
拿传进来的旧 `state` 再算会算出同样的年龄（无害，但等于白掷）。


**掉落规则照抄原版小麦**（`data/moreores/loot_table/blocks/*.json`）：

| 状态 | 掉落 |
|---|---|
| 未成熟被破坏 | **矿物种子** ×1（原样返还，不亏不赚） |
| 成熟收获（age=3） | **对应的小单位 ×1** + **矿物种子 ×2**（再受时运加成，最多 +3）|

> **「小单位」是什么**（2026-10 改过一次）：金属作物给**金属粒**、
> 宝石作物给**宝石碎片**、下界合金作物给**下界合金碎片**（原版 `netherite_scrap`）。
> 煤和红石保持原样 —— 它们本身就是原始形态，没有更小的单位可拆。

池子结构和 `minecraft:wheat` 一致：

- **池 1** —— `alternatives`：成熟时给**产物**（小麦给小麦、这里给对应的小单位），
  否则给**种子 ×1**。所以中途铲掉是原样返还。
- **池 2** —— 带 `match_block` 条件（`state: {"age": "3"}`），只在成熟时结算，
  给种子并挂 `set_count`(=2) + `apply_bonus` 时运附魔加成，
  参数 `{"extra": 3, "probability": 0.5714286}` 与小麦一致。

实测掉落（无时运）：

```
成熟   lead_crop   种子×4  产物×1
       coal_crop   种子×2  产物×1
       lapis_crop  种子×5  产物×1
未成熟 lead_crop   种子×1
```

> ⚠️ **26.3 的掉落表字段名和旧版不一样，写错了会被静默忽略**。
>
> | | 旧版 | **26.3** |
> |---|---|---|
> | 条目上的函数列表 | `"functions": [...]` | **`"modifier": [...]`** |
> | 单个函数的类型键 | `"function": "..."` | **`"type": "..."`** |
>
> 我按旧版写了一遍，游戏里**完全不报错**、掉落表也照常加载 ——
> 只是 `set_count` 和 `apply_bonus` 没生效，表现为「种子该给 2 个却只给 1 个、时运也没加成」。
> 从原版 `loot_table/entities/mule.json` 里对出来的写法。
> 判断依据是 `/reload` 不报错 ≠ 字段名对，**得看实际掉落数量**。

**铀作物的辐射**：任意生长阶段都算辐射源，每个 +5/秒（比铀矿石的 3 略高，
毕竟整株都是铀）。已经接进 `RadiationManager.exposurePerSecond` 的方块扫描，
所以**盖革计数器的咔哒声会跟着变快**。

**生长阶段**：只做了 **4 个**（age 0~3），原版土豆/小麦是 8 个 —— 省一半贴图。
代价是必须覆写三处（`getAgeProperty` / `getMaxAge` / `createBlockStateDefinition`），
而且 `createBlockStateDefinition` 里**不能调 super**（父类往状态里塞的是 0~7 的 `AGE`）。

**铀作物的发光**——它是唯一会**真正照亮环境**的作物。两处设置各管一半：

| 设置 | 作用 |
|---|---|
| `lightLevel(state -> ...)` | 向周围方块发光。未成熟 3、成熟（age 3）**7**（对比：火把 14、萤石 15）|
| `emissiveRendering(state -> true)` | 让**它自己的纹理**不随环境光变暗，矿洞里也是鲜绿的 |

> ⚠️ 顺便记一条：**方块可以照亮环境（`lightLevel`），实体不行**。
> 原版 MC 的实体没有动态光照，任何"实体发光"都只能做到「自己看起来是亮的」，
> 脚下地面照样黑。想照出一片光只能靠动态光源（光影或额外模组）。

**纹理差异化**：第一版 11 种作物的贴图**形态完全一样**，只有茎和顶部小点的颜色不同 ——
看着像同一株植物换了个色。现在每种作物有自己的「造型参数」（`_gen_crops.py` 的思路）：

| 维度 | 做法 |
|---|---|
| 高度 | 成熟时 4~7 格不等（石英最高、红石最矮）|
| 茎 | 粗细 / 颜色不同（铅、煤是双像素粗茎）|
| 叶片 | 长在哪个高度、往哪边伸，每种都不一样 |
| 顶部特征物 | **6 种画笔**：圆球 / 方块 / 菱形晶体 / 竖串 / 散粉 / 尖塔 |

比如：煤炭是**粗茎顶着黑色方块**、钻石是**菱形晶体**、红石是**一撮红粉**、
石英是**细长的白色尖塔**、金是**竖着的一串金珠**、铀是**带光环的绿球**。

**26.3 的两处改名**（都是编译期才发现的）：

| 旧 | 26.3 |
|---|---|
| `BlockBehaviour.Properties#noCollission()`（历史上一直拼错） | **`noCollision()`** —— Mojang 终于把拼写改对了 |
| `PushReaction.DESTROY` | **`PushReaction.POPPED`** |

另外掉落表的条件类型是 **`minecraft:match_block`**（`{"blocks": ..., "state": {"age": "3"}}`），
不是老名字 `block_state_property`。

---

### 8.19 高能熔炉

`moreores:high_energy_furnace` —— 普通熔炉的高配版。

**配方**：普通熔炉居中 + 围一圈铅锭（8 个）

```
LLL
LFL
LLL
```

**和普通熔炉的三点差异**（都在 `HighEnergyFurnaceBlockEntity` 里）：

| 项 | 实现 |
|---|---|
| 烧制速度 **×10** | 覆写 `getSpeedMultiplier()` 返回 10.0 |
| 燃料消耗速度 **×10** | 覆写 `getBurnDuration()`，把时长除以 10 |
| **多一个副产物槽** | 覆写 `getContainerSize()` / `getItems()` 改成 4 格 |

> 「燃料消耗 ×10 + 烧制速度 ×10」的组合意味着：**单位燃料能烧出的产物数量和普通熔炉一样**，
> 但**同样时间内的吞吐量是 10 倍**。相当于用速度换时间，不破坏燃料经济的平衡。

**副产物**（`mixin/FurnaceFuelMixin`，只在燃料被消耗时触发，普通熔炉不受影响）：

| 燃料 | 副产物 |
|---|---|
| 铀锭 | **铅锭**（铀的天然衰变链终点就是铅，顺便让"烧铀"有正向收益） |
| 核子电池 | **能源耗尽的核子电池** |

> ⚠️ **岩浆桶不在这张表里。** 原版熔炉烧掉岩浆桶之后，**自己就会把空桶放回燃料槽** ——
> 这里再产一个就是「一个岩浆桶出两个空桶」。实测对照：
>
> ```
> 原版熔炉 + 岩浆桶  → 燃料槽: lava_bucket → bucket          ← 原版干的
> 高能熔炉（修复前）  → 燃料槽: bucket + 副产物槽: bucket       ← 重复
> 高能熔炉（修复后）  → 燃料槽: bucket，副产物槽空              ← 正确
> ```
>
> 判据是「**原版有没有把容器还回来**」：还了的就别插手。
> 铀锭和核子电池都是**自定义物品**，原版不认识、自然不会还，所以只有它们需要这条路。

副产物优先放进第 4 格，塞不下就吐在方块上方，不会凭空消失。

**铀锭当燃料**：热值 = 岩浆桶的 **10 倍**（200000 tick）。
用 `getBurnDuration` 注入实现，而不是给物品挂 `COOKING_FUEL` 组件 ——
那个组件要构造 loot 数值提供器（`ResolvableInt` / `ResolvableFloat`），绕得多；
这样写顺带让铀锭在**普通熔炉里也能烧**。

**界面**：没有继承 `AbstractFurnaceScreen`，因为它的图幅写死 176×166，
而我们要多塞一个槽（图幅改成 176×184）。直接走 `AbstractContainerScreen` + 显式指定图幅，
火焰和箭头用 `blit` 自己画。

**和漏斗的配合**（`getSlotsForFace`，其余沿用原版熔炉）：

| 接入方向 | 可访问的槽 |
|---|---|
| 上方 | 输入槽 |
| 侧面 | 输入槽 + 燃料槽 |
| **下方** | 输出槽 + **副产物槽** + 燃料槽 |

产物和副产物走同一条路被抽走（副产物槽专门加进了 DOWN 的可访问列表）。
燃料槽那一项沿用原版 —— 让漏斗能把烧完的空桶取出来。副产物槽虽然可访问，
但 `canPlaceItem` 对它返回 `false`（而父类的 `canPlaceItemThroughFace` 正是转发到那里），
所以漏斗**只能抽不能塞**。

实测：熔炉 + 下方漏斗 + 箱子，烧完一轮后箱子收到 `lead_ingot ×9`。

### 8.20 铀锭作为燃料（26.3 的组件化燃料）

26.3 的燃料是**组件化 + 数据包**：

- 物品侧：`Item.Properties.cookingFuel(ResourceKey<ContextIntProvider>)` 挂一个**引用**
- 数值侧：本体写在 `data/<ns>/context_int_provider/cooking/*.json`，注册表是
  `Registries.CONTEXT_INT_PROVIDER`
- 判定侧：`AbstractFurnaceMenu.isFuel(stack)` 就是 `stack.has(DataComponents.COOKING_FUEL)`
  （`FurnaceFuelSlot.mayPlace` 正是转发到它）

铀锭热值 = 岩浆桶的 10 倍，照抄原版 `time_lava_bucket.json` 的结构改成 200000：

```
data/moreores/context_int_provider/cooking/time_uranium.json
```

> ⚠️ **只注入 `getBurnDuration` 是不够的**。那个方法只管「烧起来之后多长时间」，
> 而「这算不算燃料、能不能放进燃料槽」看的是**有没有 `COOKING_FUEL` 组件**。
> 上一版只改了时长，结果煤能放、铀锭放不进去。
> 验证方式：`execute if items block <pos> <slot> moreores:uranium_ingot[minecraft:cooking_fuel]`。

### 8.21 铀锭在普通熔炉里一格只放一个

铀锭热值是岩浆桶的 10 倍（200000 tick ≈ 2.8 小时），一叠 64 个塞进普通熔炉够烧一周，
用起来更像"卡在槽里的燃料块"。所以给**普通熔炉**的燃料槽加了限制：铀锭最多放 1 个。

实现是给 `FurnaceFuelSlot#getMaxStackSize(ItemStack)` 挂一个注入
（`mixin/FurnaceFuelSlotMixin`），铀锭返回 1、其余走原逻辑。

关键在于**只改这一个方法** —— 它同时管住「鼠标点击放置」「拖拽分配」「Shift 快速移动」
三条路径，因为它们都会去问槽位能放多少。

> ⚠️ **不要图省事把整个槽替换掉**。第一版是 `@Inject` 到 `AbstractFurnaceMenu` 构造结尾
> 把 1 号槽换成自定义 `Slot`，结果把原槽的 `mayPlace`（燃料判定）一起弄丢了 ——
> 铀锭直接被燃料槽拒收。原版的燃料槽是有独立类的（`FurnaceFuelSlot`），
> 注入它自己的方法才是正解。

> 高能熔炉**不受**这个限制（它烧得快，燃料时长已被除以 10），Mixin 里判断
> `container instanceof HighEnergyFurnaceBlockEntity` 直接放过。

**26.3 的几处 API 变化**（这次踩到的）：

| 旧写法 | 26.3 |
|---|---|
| `BlockEntityType.Builder.of(工厂, 方块).build()` | **没有 Builder 了**，直接 `new BlockEntityType<>(工厂, Set<方块>)` |
| `ServerPlayer.openMenu(provider, buf -> buf.writeBlockPos(pos))` | **只剩 `openMenu(provider)`** 一个重载（物品靠槽位同步、进度靠 `ContainerData`） |
| 覆写 `getDisplayName()` | 要覆写的是 `getDefaultName()`（MenuProvider 的 getDisplayName 已转发过去） |
| `Screen#renderBg` | `extractBackground(GuiGraphicsExtractor, ...)` |

> 另外 `AbstractFurnaceMenu` 的槽位是在构造函数里铺的，没法插队 ——
> 追加的第 4 格**索引被排到最后**（父类已占 0~38），但**坐标**放在熔炉区域里。
> 索引和位置本来就是两回事。
>
> ⚠️ **副产物槽的位置不能随便放**。第一版放在燃料槽下方 `(56,71)`，结果**压到了背包第一行**
> （原版背包自 `y=84` 起、快捷栏自 `142` 起）—— 界面看着非常挤。
> 而父类的槽位 y 是**写死的、改不了**（`Slot.y` 是 `final`），想把背包整体下移就得重写整个菜单。

**最终布局（两行平行，沿用原版 176×166）**：

```
原料 (56,17)  →  烧制物 (116,17)      ← 箭头 y = 17
     火焰
燃料 (56,53)     副产物 (116,53)      ← 和左边严格平行
```

- **左列**（原料 / 燃料）用父类铺好的原版坐标
- **右列**要自己摆：烧制物本该在 `(116,35)`（原版为了让箭头居中而故意放低了半格），
  这里挪到 `(116,17)` 和原料对齐；副产物放 `(116,53)` 和燃料对齐
- 箭头跟着从 `y=34` 上移到 `y=17`；火焰保持原版的 `(56,36)`

> ⚠️ **挪输出槽千万不要用 `slots.set()` 替换槽对象**（这个坑连踩两轮）。
>
> 第一版是往自家菜单里塞普通 `Slot` —— 丢了 `FurnaceResultSlot` 的 `onTake`
> （发经验、解锁配方）。第二版改成 `new FurnaceResultSlot(..., 新坐标)` 塞进去，
> **看着对了，但游戏里烧制物取不出来、火焰进度恒为 0**。
>
> 根因在 26.3 的 `AbstractContainerMenu#addSlot`：它不只是往 `slots` 里加一个槽，
> 还要**同步维护另外两个平行列表** —— `lastSlots`（变化检测）和 `remoteSlots`（客户端视图）：
>
> ```
> slot.index = slots.size();
> slots.add(slot);  lastSlots.add(EMPTY);  remoteSlots.add(...);
> ```
>
> 自己 `slots.set(2, ...)` 只动了第一个，后两个还指向旧槽 —— 于是客户端取不出物品、进度也不刷新。
>
> **正确做法：完全不碰槽位结构，只在字节码层面把原版构造里那个坐标常量改掉**
> （`mixin/AbstractFurnaceMenuMixin`）：
>
> ```java
> @ModifyConstant(
>         method = "<init>(...带 Container 和 ContainerData 的那个构造...)V",
>         constant = @Constant(intValue = 35, ordinal = 0))
> private int moreores$alignResultSlot(int original) {
>     return ((Object) this) instanceof HighEnergyFurnaceMenu ? 17 : original;
> }
> ```
>
> 这样原版该走的流程一步不少，三个列表照常对齐。副作用是**菜单类里一行槽位代码都不用写**
> （副产物槽除外 —— 它是纯追加的第 4 格，直接 `addSlot` 没问题）。
>
> 两个小坑：
> - 判断类型要写 `((Object) this) instanceof HighEnergyFurnaceMenu`，
>   直接 `this instanceof` 编译期会报「不兼容的类型」（Mixin 类此时还不认识目标类）。
> - 那个构造里 `bipush 35` 只出现一次（结果槽的 y），加 `ordinal = 0` 只是为了行为可预期。

> ⚠️ **凹槽是画在 GUI 底图上的，槽位挪了位置底图必须跟着挪。**
>
> 一开始我以为凹槽是运行时自动画的（因为原版 `furnace.png` 的 (116,35) 看着只是块纯灰），
> 结果游戏里出现**物品按新坐标渲染、凹槽还留在老位置** —— 视觉上物品"飘"在方块上方，
> 玩家点的是那个方块，而那里**根本没有槽位**，于是"点不动、拿不下来"。
>
> 反编译 `AbstractContainerScreen#extractSlot` 才确认：它只用 `Slot.x/y` 画**物品**，
> 普通凹槽图案来自底图（只有 `noItemIcon` 那种特殊图标才走 `blitSprite`）。
>
> 于是底图要手工搬：把凹槽图案从原料槽复制到 (116,17) 和 (116,53)，
> 箭头从 `y=34` 搬到 `y=17`，并把原输出槽那块清成底色。
>
> ⚠️ **清理时注意图案的真实尺寸**：原料槽的凹槽是 **18×18**（(55,16)~(72,33)），
> 但原版**输出槽的图案是 26×26**（(111,30)~(136,55)，18×18 外面还有一圈 4px 装饰）。
> 只清 18×18 会剩一圈边框，在界面上表现为槽位旁边两条突兀的竖线。
> 用脚本 `getcolors` / 逐像素包围盒测出真实边界再清，别凭槽位尺寸想当然。

> **方块纹理**（`high_energy_furnace_top / _side / _front / _front_on`）是「铅灰金属 + 绿色高能火光」
> 的主题。⚠️ 第一版这几张 PNG 是**纯黑 RGB(0,0,0)**（只写了 alpha 没写颜色），
> 游戏里表现为**一片纯黑立方体**。另外 blockstate 原本漏了 `lit` 变体，
> 补上 `facing=*,lit=true → *_on` 后，熔炉工作时炉膛才会亮起来。

#### 方块纹理重绘：怎么摆脱「扁平感」

第二版纹理能显示了，但和原版熔炉摆一起就露怯。量化对比很直观：

| | 重绘前 | 原版熔炉 |
|---|---|---|
| 颜色数 | 5~9 | 12~13 |
| 质感 | 纯色几何块 | 石头噪点 |
| 立体感 | 平铺，无明暗 | 有凸起/凹陷 |

结论是**像科技面板而不是熔炉**。重绘时加了三样东西：

1. **金属噪点** —— 每像素对亮度做 ±3.5% 的随机扰动。
   ⚠️ 但**幅度要收住**：第一版用了 ±9%，结果侧面 47 种颜色，看着像**花岗岩**而不是金属。
2. **明暗层次** —— 关键不是"随机"而是"结构"：靠边框的受光面（上/左亮）、
   背光面（下/右暗）、凹槽内壁的渐深来塑造立体感。
3. **立体细节** —— 铆钉、加强筋、散热格栅。

最终颜色数落在 24~30。比原版石头高是正常的 —— 金属需要更多色阶过渡，
石头靠噪点就能出效果。

> 炉口内部特意做了**从上往下的渐深**，不是一块死黑 —— 这样烧起来时
> 底部的绿光才有"从炉膛里透出来"的感觉。熄灭状态则在炉膛深处留一点暗红余温。

> ⚠️ **火焰 / 箭头的图标在 26.3 搬家了**。老版本把它们画在 GUI 底图的**扩展区**
> （`furnace.png` 的 `u >= 176`），所以按老约定用
> `blit(..., FLAME_U=176, FLAME_V=0, 14, 14, 256, 256)` 取图 —— 但在 26.3 里
> **那块区域已经是全透明的了**（实测 `(176,0)-(190,14)` 只有 `(0,0,0,0)`）：
>
> ```
> textures/gui/sprites/container/furnace/lit_progress.png    火焰 14x14
> textures/gui/sprites/container/furnace/burn_progress.png   箭头 24x16
> ```
>
> 26.3 把它们拆成了**独立 sprite**，原版 `AbstractFurnaceScreen` 改用 `blitSprite` 绘制。
> 我们的界面没继承它（图幅是自定的），所以表现是**火焰完全不显示**。
>
> 修法是**把这两张 sprite 按老位置拼回我们自己的 GUI 纹理**：
> `lit_progress` → `(176,0)`、`burn_progress` → `(176,14)`。尺寸和 `FLAME_W/H`、`ARROW_W/H`
> 刚好一致，**`HighEnergyFurnaceScreen` 一行代码都不用改**。
> （两 sprite 的背景色正好是 GUI 底色 `(198,198,198)`，拼上去无缝。）

### 8.22 燃料时长超过 32767 tick 时，火焰会跑飞

**现象**：普通熔炉烧铀锭（200000 tick ≈ 2.8 小时），但界面上的火焰
**2.8 分钟就排空了**，之后还乱跳 —— 偶尔又满格、再熄灭。

**根因：同步数据槽的包用的是 `short`。** 反编译 26.3 的
`ClientboundContainerSetDataPacket`：

```java
private void write(FriendlyByteBuf buf) {
    buf.writeContainerId(this.containerId);
    buf.writeShort(this.id);      // ← 短整型
    buf.writeShort(this.value);   // ← 短整型！
}

private ClientboundContainerSetDataPacket(FriendlyByteBuf buf) {
    this.containerId = buf.readContainerId();
    this.id = buf.readShort();
    this.value = buf.readShort();
}
```

也就是**只能传 `[-32768, 32767]`**。而火焰高度正是
`剩余燃烧时间 / 总燃烧时间`（`AbstractFurnaceMenu#getLitProgress`），
这两个数就走这条包。铀锭 200000 截断之后：

```
200000 mod 65536 = 3392          // 客户端以为总共只要烧 3392 tick
```

3392 ÷ 20 = **169.6 秒 ≈ 2.8 分钟** —— 和观测到的现象完全吻合。

> 原版**所有燃料都 ≤ 20000**（岩浆桶 20000、煤炭块 16000），
> 所以这个坑在纯原版里永远踩不到。

**修法：把「剩余」和「总时长」同时除以一个系数。** 火焰只看比值，
同除之后比值不变，数值又能落进 short。见 `furnace/ShortSafeContainerData`
+ 挂在 `AbstractFurnaceMenu` 构造上的 `@ModifyArg`：

```java
@ModifyArg(method = "<init>(...带 Container 和 ContainerData 的那个构造...)V",
        at = @At(value = "INVOKE",
                target = "Lnet/minecraft/world/inventory/AbstractFurnaceMenu;"
                        + "addDataSlots(Lnet/minecraft/world/inventory/ContainerData;)V"),
        index = 0)
private ContainerData moreores$shrinkLitTime(ContainerData data) {
    return new ShortSafeContainerData(data);
}
```

系数按总时长自适应，于是**幂等**（缩过一次之后必定 ≤ 32767，不会反复缩）：

| 总时长 | 系数 | 同步出去的值 |
|---|---|---|
| ≤ 32767（原版燃料、高能熔炉的 20000） | **1** | **原样，行为完全不变** |
| 200000（铀锭） | 7 | 28571 / 28571 |

⚠️ **只换传给 `addDataSlots` 的那一份参数**。菜单自己的 `data` 字段、
方块实体真正用来倒计时的 `litTimeRemaining` 都不受影响 ——
**实际燃烧时长一点没改**，只是同步给客户端看的数字换了个比例。实测服务端：

```
lit_time_remaining = 199481   lit_total_time = 200000   // 22 秒正好消耗 440 tick
```

**验证方式**：写了个 `FlameProbe`，直接调用真实的 `ShortSafeContainerData`，
走一遍「服务端取值 → `writeShort`/`readShort` 往返 → 算火焰像素高」：

| 已经过 | 真实比例 | 旧（不缩短） | 新 |
|---|---|---|---|
| 0s | 1.000 | 3392/3392 → 14px | 28571/28571 → 14px |
| 50s | 0.995 | 2392/3392 → **11px** | 28428/28571 → 14px |
| 2.8min | 0.983 | 0/3392 → **0px（灭）** | 28086/28571 → 14px |
| 41.7min | 0.750 | 18928/3392 → 14px（回光返照） | 21428/28571 → 11px |
| 83.3min | 0.500 | **-31072**/3392 → 0px | 14285/28571 → 8px |

而总时长 20000 / 16000 两组，「旧」和「新」两列**逐行完全相同**。

> ⚠️ **怎么确认 Mixin 真的生效**：给 JVM 加 `-Dmixin.debug.export=true`
> （用 `JAVA_TOOL_OPTIONS` 环境变量传最省事），Mixin 会把转换后的类导出到
> `run/.mixin.out/class/`。反编译它就能看到注入后的字节码：
>
> ```
> 150: invokevirtual modify$bde000$moreores$shrinkLitTime:(ContainerData)ContainerData
> ...
> private ContainerData modify$bde000$moreores$shrinkLitTime(ContainerData)
>     0: new  #326  // class com/example/moreores/furnace/ShortSafeContainerData
> ```
>
> 这比「客户端没报错」强得多 —— 注入点没匹配上时 Mixin 会抛错，
> 但**注入对了、逻辑写错**是静默的。

### 8.23 钛：一种「必须用高能熔炉才能烧」的矿物

**设定**（三句话）：

| 项 | 取值 |
|---|---|
| 工具数值 | **铁**的挖掘等级 / 速度 / 攻击 / 附魔度（`INCORRECT_FOR_IRON_TOOL`, 6.0, 2.0, 14）|
| 工具耐久 | **铜**的 **190**（铁是 250）|
| 矿石分布 | **完全照抄煤矿**（size 17；上段 `count 30` y 136~顶部、下段 `count 20` 埋藏型 y 0~192）|
| 烧制 | **只有高能熔炉能烧成锭**，普通熔炉 / 高炉 / 烟熏炉 / 营火全都不行 |

原版参照值（反编译 `ToolMaterial` 静态块）：

```
铜：INCORRECT_FOR_COPPER_TOOL, 190, 5.0, 1.0, 13
铁：INCORRECT_FOR_IRON_TOOL,   250, 6.0, 2.0, 14
```

> 顺带一提：26.3 里每件工具的**参数值是各材质通用的**（原版铁剑和钻石剑都是
> `sword(材质, 3.0, -2.4)`），差异全在材质本身。所以「铁的数值」= 直接用铁那套参数。

#### 钛工具攻速：在各自原值上统一 +0.4

攻速参数是「相对 4.0 的偏移」，所以就是在每件工具**原来的取值**上加 0.4：

| 工具 | 原偏移 | 新偏移 | 原最终攻速 | **新最终攻速** | 原本对标 |
|---|---|---|---|---|---|
| 钛剑 | -2.4 | **-2.0** | 1.6 | **2.0** | 铁剑 1.6 |
| 钛镐 | -2.8 | **-2.4** | 1.2 | **1.6** | 铁镐 1.2 |
| 钛斧 | -3.0 | **-2.6** | 1.0 | **1.4** | 钻石斧 1.0（铁斧只有 0.9）|
| 钛锹 | -3.0 | **-2.6** | 1.0 | **1.4** | 铁锹 1.0 |
| 钛锄 | 0.0 | **+0.4** | 4.0 | **4.4** | 钻石锄 4.0 |

换算成「比同类原版快多少」：**剑 / 镐 / 锹比铁快 0.4，斧比铁快 0.5，锄比钻石快 0.4** ——
和铅「比铁低 0.4」正好对称，一轻一重。表里每个数字都是从游戏里实测出来的。

> ⚠️ **顺带查出一个既有问题（本次没动）**：斧和锄的**伤害参数**原本照抄的是**钻石那一套**
> （`5.0` / `-3.0`），但钛材质的 `attackDamageBonus` 是铁的 **2.0** 而不是钻石的 **3.0**，
> 于是这两件的**伤害反而比铁低 1.0**：
>
> ```
> 钛斧 伤害修饰符 7.0     铁斧 8.0     钻石斧 8.0
> 钛锄 伤害修饰符 -1.0    钻石锄 0.0
> ```
>
> 剑 / 镐 / 锹用的是铁的参数（`3.0` / `1.0` / `1.5`），和铁**完全一致** ✓。
> 要修的话，把钛斧的 `5.0F` 改成 `6.0F`、钛锄的 `-3.0F` 改成 `-2.0F` 就行 ——
> 但那是在改伤害，本次只动攻速，所以没碰。

#### 「只有高能熔炉能烧」是怎么做到的

**问题**：配方在 `RecipeManager` 里是**按 `RecipeType` 分桶**存的。高能熔炉原本用的是
`RecipeType.SMELTING`，和普通熔炉同一个桶 —— **没法只让其中一个看见某条配方**。

**做法**：给这类配方单开一个桶。

```
1. 注册自定义类型        moreores:high_energy_smelting
2. 自定义配方类          HighEnergySmeltingRecipe（就是 SmeltingRecipe 换了个 getType()）
3. 高能熔炉构造传自定义类型   super(..., ModRecipeTypes.HIGH_ENERGY_SMELTING)
4. 配方 JSON             "type": "moreores:high_energy_smelting"
```

于是**所有原版炉子天然看不到它**（它们只查自己的桶），而高能熔炉要同时认两个桶。

#### 一个注入点就够了

`AbstractFurnaceBlockEntity` 的配方查找入口是构造里建的那**一个**对象：

```java
this.quickCheck = RecipeManager.createCheck(recipeType);
```

而整个类里用它查配方的地方**只有两处**（`serverTick` 和 `getTotalCookTime`）。
所以只要 `@Redirect` 这一次 `createCheck`，换成 `CombinedCookingCheck`
（先查专属桶、查不到再退回 `SMELTING`），两处调用点全都自动跟着变 ——
**不用去动那两个方法**。

```java
// mixin/FurnaceRecipeCheckMixin
@Redirect(method = "<init>(...RecipeType;)V",
        at = @At(value = "INVOKE",
                target = "Lnet/minecraft/world/item/crafting/RecipeManager;"
                        + "createCheck(...)Lnet/minecraft/world/item/crafting/RecipeManager$CachedCheck;"))
private static RecipeManager.CachedCheck<?, ?> moreores$combineCookingChecks(RecipeType<?> type) {
    return type == ModRecipeTypes.HIGH_ENERGY_SMELTING
            ? new CombinedCookingCheck()
            : RecipeManager.createCheck((RecipeType) type);   // 原版炉子逐字节不变
}
```

注入的方法是构造里的静态调用、而且**参数就带着配方类型**，所以不用 `this`
（此刻也还没构造完）就能区分是不是高能熔炉。

**为什么是「白名单」而不是「黑名单」**：如果把钛写成 `minecraft:smelting`、
再注入 `serverTick` 把非高能熔炉的钛配方滤掉，一旦注入失效，**钛会在普通熔炉里
照常烧起来**（静默退化）。白名单方案下注入失效的后果是「高能熔炉烧不了原版矿」——
吵得响、一眼能发现，而且原版炉子依然烧不出钛。

#### 踩到的坑

> ⚠️ **`RecipeType.register(String)` 不能用**。它内部是
> `Identifier.withDefaultNamespace(name)`，而这个方法会把**整串当成 path**
> 硬加 `minecraft:` 前缀 —— 传 `"moreores:xxx"` 进去就变成
> `minecraft:moreores:xxx`，直接抛 `IdentifierException` 把游戏打崩在启动阶段：
>
> ```
> Non [a-z0-9/._-] character in path of location: minecraft:moreores:high_energy_smelting
>     at Identifier.withDefaultNamespace(Identifier.java:54)
>     at RecipeType.register(RecipeType.java:18)
> ```
>
> 正确做法是手动 `Registry.register(BuiltInRegistries.RECIPE_TYPE, MoreOres.id("..."), ...)`。

> ⚠️ **`AbstractCookingRecipe` 还有个隐藏的抽象方法 `furnaceIcon()`**（返回
> 代表该配方的炉子物品）。反编译时没注意到，编译才报出来。原版熔炉返回熔炉、
> 高炉返回高炉，这里返回高能熔炉本身。

> ⚠️ **资源文件用「复制铅的文件 + 把 `lead` 换成 `titanium`」批量生成**，
> 而不是手写 JSON。一个矿物牵扯 ~70 个 JSON，手写必然出现格式漂移；
> 字符串替换能保证和现有资源**逐字一致**。唯一要单独改的是三条冶炼配方
> （`"type"` 换成自定义类型 + 文件名加 `high_energy_smelting`）。

> ⚠️ **批量生成资源最容易翻的车是「模板选错」，而且自检要走完整条链**。
> 钛的四个方块（两种矿石 + 粗钛块 + 钛块）我都拿 `lead_ore.json` 当模板复制，
> 替换 `lead`→`titanium` 之后它们**全都指向 `titanium_ore`**。
>
> 这个坑**分两次才修干净**，因为一个方块的纹理引用有**三条独立的链**：
>
> | 场景 | 链路 |
> |---|---|
> | 在物品栏 / 拿在手里 | `items/<方块>.json` → 模型 → 纹理 |
> | **放下去之后** | **`blockstates/<方块>.json` → 模型 → 纹理** |
> | 模型内部 | `models/block/<方块>.json` 的 `textures` |
>
> 第一轮我只改了后两条，于是出现一个非常迷惑的现象：
> **物品栏里四个方块正常、拿在手里正常，放下去却全一样**。
>
> 所以生成脚本的收尾自检要**三条链一起走完，并断言「四套引用互不相同」**；
> 只看「文件生成成功」是发现不了的。更稳的做法是对全模组做一次
> 「blockstate → 模型 → 纹理」的解析，列出任何两个方块撞同一张纹理的情况。

> ⚠️ **工具的标签别忘了加**。`Player#isSweepAttack` 的最后一句就是：
>
> ```java
> return this.getItemInHand(InteractionHand.MAIN_HAND).is(ItemTags.SWORDS);
> ```
>
> 剑没进 `minecraft:swords` 物品标签就**扫不了**（横扫的条件是
> 「在地面 + 不疾跑 + 不暴击 + 移动很慢 + 手上是剑」，前四条都满足也没用）。
> 同理还有 `axes` / `pickaxes` / `hoes` / `shovels`。
>
> `SWEEPING_DAMAGE_RATIO` 不用自己挂 —— 它是**玩家自带的基础属性**
> （注册在 `Player.createAttributes()` 里），剑本身不提供。
>
> 验证方式（`execute if items` 支持直接写标签）：
>
> ```
> execute if items block <pos> container.0 #minecraft:swords
> ```

> ⚠️ **纹理换色：三次迭代才走对**。钛的纹理是拿铅的纹理**换算色相**得到的，
> 中间反复了三轮，教训比结论值钱：
>
> | 做法 | 问题 |
> |---|---|
> | ① 直接把亮度映射到银白色阶 | 铅块的亮度集中在很窄一段 → 映射出来是一块**死灰**，没对比度 |
> | ② 每张图各自按 2%/98% 分位数拉伸到满色阶 | 对比度回来了，但**把变体差异也拉平了** —— 深板岩矿和石头矿一样亮 |
> | ③ 同族共享亮度基准（深板岩沿用石头矿的 lo/hi） | 深板岩暗下来了，但**整体偏亮**：钛矿平均亮度 149.7，而铅矿 129.3、铁矿 130.7 —— 所有矿石都挤在 130 附近，**钛一个人高出一档** |
> | ④ **保亮度换色** ✅ | 见下 |
>
> 根子是 ② 的「拉伸」：它会**把任何色阶窄的纹理强行拉满**，等于凭空提对比度。
> 对色阶本来就窄的矿石是过度处理。
>
> 最终做法是**保持源纹理的亮度分布、只换色相**：先算一张
> 「色阶位置 → 实际亮度」的表，再对每个像素**反查** —— 找出哪个色阶位置能还原
> 它原本的亮度。这样钛矿的明暗结构完全等于铅矿，差别只剩色相。
>
> ⚠️ 反查有个前提：**色阶的暗端必须够暗**。第一版色阶最暗只到亮度 92，
> 而深板岩里大量像素比这更暗 —— 反查时全被钳在 `t=0`、硬抬到 92，
> `deepslate` 的平均亮度虚高到 113（应该是 ~105）。把色阶从亮度 26 拉到 255 就正常了。
>
> 实测（`GAIN = 1.05`，即整体比对应的铅纹理亮 5%）：
>
> | 矿石 | 平均亮度 | 最亮像素 |
> |---|---|---|
> | 铅矿（参照） | 129.3 | 197 |
> | 铁矿石（原版） | 130.7 | 200 |
> | **钛矿** | **135.9** | **206** |
> | 钻石矿石（原版） | 135.2 | 241 |
> | 深层铅矿 | 100.0 | 197 |
> | 深层铁矿石（原版） | 101.2 | 200 |
> | **深层钛矿** | **105.2** | **206** |
>
> 一句话：**想让「只换色相」，就别顺手改亮度分布**。

> ⚠️⚠️ **矿石纹理是「两簇结构」，换色时只能动其中一簇** —— 这条比上一条还重要，
> 而且亮度对上了也照样踩。
>
> 拆开铅矿的配色才看清它的构造：
>
> | | 颜色 | `b - r` | 占比 |
> |---|---|---|---|
> | **石头底** | 中性灰 `(127,127,127)` `(143,143,143)`… | **0** | **76%** |
> | **矿点** | 蓝灰 `(125,151,175)` `(90,114,136)`… | 36~62 | 24% |
>
> 铀矿同构，只是矿点那簇换成绿色（`g` 最大）。
>
> 而「保亮度换色」是把**整张图**（含石头底）一起染成银灰 ——
> 两簇并成了一簇，石头底失去「中性灰」的身份，整块变成一片灰蓝银斑。
> 放进世界里就**格外违和**，像贴了张别的图：平均亮度明明已经对上了，
> 看着还是不属于这个世界。
>
> 正解是**只换矿点那一簇**：用 `b - r > 20` 当遮罩挑出矿点
> （石头底是 0、深板岩底 6~7、矿点 36~62，余量非常大），
> **石头底一个像素都不动**，只把矿点按亮度映射到钛的亮银色阶。
>
> 实测：石头底 **193 个像素原样保留、0 个被改动**，整体均值 131.7（铅矿 129.3）。
>
> 一句话：**矿石 = 石头底 + 异色矿点。只想换矿物，就只换矿点，别碰石头底。**
> （固体金属块没有石头底，整块换色才是对的，所以 `raw_titanium_block` /
> `titanium_block` 沿用「保亮度换色」没问题。）

> ⚠️ **木柄要按色相识别并跳过 —— 但判据不能用「绝对亮度阈值」。**
>
> 铅制工具的木柄有**四档**颜色：
>
> | 木柄色 | `r` | `r > 90` 判定 | 镐子中出现 |
> |---|---|---|---|
> | `(40, 30, 11)` | 40 | ❌ **漏** | **11 px** |
> | `(73, 54, 21)` | 73 | ❌ **漏** | **10 px** |
> | `(104, 78, 30)` | 104 | ✓ | 5 px |
> | `(137, 103, 39)` | 137 | ✓ | 5 px |
>
> 第一版判据写的是 `r > 90 && ...`，于是**两档最暗的木柄漏判了** ——
> 而它们恰恰是木柄的**主要部分**（亮的两档各只有 5px）。结果木柄暗部被漂成银色、
> 只剩零星亮部还是棕的，看起来就是**「木柄泛白」**。
>
> 正解是**按色相判，不用绝对阈值**：
>
> ```
> r > g && g >= b && (r - b) >= 8
> ```
>
> 木柄是暖色（`r > g > b`），四档全中。当时金属一律是冷色（`r < g < b`），
> 一条都不会误判。实测木柄像素 **31/31、27/27、25/25、20/20 逐位保留、0 个被误改**。
>
> ⚠️ **这条判据在「钛改暖」之后不再成立**（详见 8.26）：钛金属现在是 `r > g > b`，
> 会被 `is_wood` 认成木柄。以后要处理钛工具的木柄，得再加上「不是金属色」的条件
> （比如限制亮度范围，或者改成从**铅**工具上取木柄模板）。
>
> 教训：**识别「类别」要用相对关系（色相、比值），不要用绝对亮度** ——
> 同一个部件的不同明暗档位，绝对值可以差一倍以上。

**第二个坑：木柄还有一条「暗边线」，光看颜色分不出来。**

木柄并不是一整片棕色 —— 它是「填充色 + 1 格暗边」。那格暗边是 `(66,73,80)`，
**冷色**（`r < g < b`），所以按色相判定时会被归给金属，一起被漂白：

| | 铅锄 | 漂白后 |
|---|---|---|
| 木柄填充色 | 亮度 29 ~ 106（平均 74.5） | 不变 |
| **柄的暗边** | **亮度 72.9**（比木柄略暗） | **亮度 131**（比木柄亮了！）|

结果木柄的**轮廓线反而比木柄本身还亮** —— 整根柄就显得"发白"、
边缘"糊"了。这正是「钛锄木柄还是有点漂白」的原因。

麻烦在于 `(66,73,80)` **既是木柄的暗边、也是金属的暗部**（刃的下缘、镐头的轮廓），
**光靠颜色永远分不开**。区别只在**位置**：柄的暗边一定**紧贴木色像素**。

所以用两级生长（只往「暗像素」里长）：

| 步骤 | 规则 | 作用 |
|---|---|---|
| ① | **4 邻接**木色 | 紧贴的暗边，稳 |
| ② | **8 邻接**且有 **≥ 2** 个已在柄内的邻居 | 补上边缘的补角像素 |

第一步特意用 **4 邻接**而不是 8 邻接 —— 因为锄子上刃的下缘**正好斜挨着柄顶**
（`(9,4)` 挨着木色 `(10,5)`），用 8 邻接会把这一格刃的轮廓也误收进来。

实测（`木色 + 暗边 = 总共保留`）：

| 纹理 | 木色 | 暗边 | 保留 | 被误改 |
|---|---|---|---|---|
| 钛锄 | 20 | **+13** | 33 | **0** |
| 钛斧 | 27 | +2 | 29 | **0** |
| 钛剑 | 7 | +2 | 9 | **0** |
| 钛镐 | 31 | +1 | 32 | **0** |
| 钛锹 | 25 | +0 | 25 | **0** |

- **锄子的 13 格正好是整条柄的边线**，刃上一个像素没误收；变化的 18 格全在刃部，
  柄的所有行与原图**完全相同**。
- **锹的 `+0` 是对的**：它的柄上段被铲刃盖住，源图里柄本来就没有暗边。
- 斧头有一格 `(7,6)` 是第二级生长出来的（不直接挨木色）。放大核对后确认它是
  **柄颈左侧的轮廓**（铅斧里同样是暗的）—— 保留它才是忠实原图。

#### 三种粗矿：一矿一张脸

原来的 `raw_lead` / `raw_uranium` / `raw_titanium` **都是 168 个不透明像素** ——
和原版 `raw_iron` 分毫不差，也就是说它们是**同一个轮廓换了三次色**。
现在改成一矿一设计：

| | 轮廓 | 配色 | 想表达 |
|---|---|---|---|
| **粗铅** | **少而大**的圆钝重块（4 块） | 深蓝灰、哑光 | 铅沉、软 |
| **粗铀** | 暗色母岩 + **绿色晶簇** | 暗橄榄底 + 亮绿晶 | 放射性 |
| **粗钛** | **多而细**的尖锐碎片（6 片） | 亮银白 | 钛轻、硬、亮 |

⚠️ **判断「是不是换色」最简单的方法就是数不透明像素** ——
轮廓相同的话像素数必然一样（都是 168）。

覆盖面积也刻意拉开（142 / 131 / 115 px），三者的体量感不同。

#### 钛的锭 / 工具要「更白」

原来钛锭主色 `(169,175,184)` 和铅锭 `(159,168,176)` **只差 10** ——
放一起根本分不出来。现在加一道**去饱和 + 提亮**（去饱和 0.45、幂曲线指数 0.52）：

| | 平均亮度 |
|---|---|
| 铅锭（对照） | 137 |
| **钛锭** | **183** |
| 铅镐（对照） | 89 |
| **钛镐** | **136** |

顺带把 `titanium_block`（147 → 191）和 `raw_titanium_block`（111 → 163）一起提白 ——
它们和锭同属一族，不提白放在一起会明显不搭。

> ⚠️ **提白这一步不幂等**：直接对成品再跑一次会越提越白（提亮曲线会叠加）。
> 所以脚本每次都**从铅的原图重新推导**整条链：
>
> ```
> 铅的纹理 -> 去饱和 -> 幂曲线提亮 -> 按提亮后的亮度反查钛色阶
> ```
>
> 这样脚本可以反复跑、参数（γ / 去饱和系数）也随手能调。

#### 验证

| 项 | 结果 |
|---|---|
| 普通熔炉烧粗钛 | 产物槽空，`cooking 0/200 lit=0` —— **根本没点火** ✓ |
| 高能熔炉烧粗钛 | 1 秒产出 `titanium_ingot` ✓ |
| 高能熔炉烧原版铁矿 | 正常产出 `iron_ingot`（兜底没坏）✓ |
| 普通熔炉烧原版铁矿 | 10 秒一个铁锭，与改动前一致 ✓ |
| 钛矿石掉落 | 普通挖掘给粗钛；**精准采集给矿石本身** ✓ |
| 世界生成 | 新区块内用 `/fill ... replace` 数出 **70 个钛矿石**（煤矿是 50 个/区块）✓ |
| 钛作物掉落 | 钛锭 ×1 + 钛种子 ×3 ✓ |

> 注：这是当时的实测记录。**作物掉落已经改过** —— 现在金属作物给**粒**、
> 宝石作物给**碎片**、下界合金作物给**下界合金碎片**，见上面「掉落规则」一节。

> ⚠️ 写验证脚本时连着踩了两个**测试侧**的坑，值得记一下：
>
> 1. `f"data get block {pos}"` 传元组会生成 `data get block (0, 70, 0)` ——
>    带括号、命令非法、静默返回错误，于是「什么都没烧出来」。坐标要拆开拼。
> 2. 测试点必须落在**同一个 `forceload` 覆盖的区块内**，否则方块不被 tick，
>    种进去的东西永远不烧。

---

### 8.24 金属粒：粒 ↔ 锭，以及工具 / 盔甲回炉

对齐原版**铁粒 / 金粒**那套，给三种金属各加一颗粒：

```
moreores:lead_nugget       铅粒
moreores:uranium_nugget    铀粒
moreores:titanium_nugget   钛粒
```

**粒 ↔ 锭**（结构照抄原版 `iron_nugget` / `iron_ingot_from_nuggets`）：

| 配方 | 类型 | 内容 |
|---|---|---|
| 1 锭 → 9 粒 | `crafting_shapeless` | `group` 用 `<金属>_nugget` |
| 9 粒 → 1 锭 | `crafting_shaped` 3×3 | `group` 用 `<金属>_ingot` |

`group` 是必须的 —— 原版 `iron_ingot_from_block` 也用 `iron_ingot` 这个组，
同一组里只显示一个，不然合成书里会挤一堆。

**工具 / 盔甲回炉成粒**（这是原版的既有玩法：铁装备烧掉给铁粒）：

| 金属 | 配方类型 | 说明 |
|---|---|---|
| 铅 / 铀 | `minecraft:smelting` + `minecraft:blasting` | 普通熔炉和高炉都行 |
| **钛** | **`moreores:high_energy_smelting`** | **只有高能熔炉**，和钛锭一个规矩 |

> 💡 原版这类配方用的是**列表形式的 ingredient**（一个文件里塞全部物品），
> 而不是一件一个文件：
>
> ```json
> { "type": "minecraft:smelting", "cookingtime": 200, "experience": 0.1,
>   "ingredient": ["minecraft:iron_pickaxe", "minecraft:iron_shovel", ...],
>   "result": { "id": "minecraft:iron_nugget" } }
> ```
>
> 所以 3 种金属一共只要 **5 份**配方文件（铅 2 + 铀 2 + 钛 1），不是 45 份。

**实测**（RCON + 真实熔炉，读产物槽和 `RecipesUsed`）：

| 输入 | 炉子 | 产物 | RecipesUsed |
|---|---|---|---|
| 铅剑 | 普通熔炉 | `lead_nugget` ✓ | `moreores:lead_nugget_from_smelting` |
| 铅胸甲 | 普通熔炉 | `lead_nugget` ✓ | 同上 |
| 铅锄 | 普通熔炉 | `lead_nugget` ✓ | 同上 |
| 铀剑 | 高炉 | `uranium_nugget` ✓ | `moreores:uranium_nugget_from_blasting` |
| **钛剑** | **普通熔炉** | **空，且 `lit=0`（连火都不点）** ✓ | — |
| **钛剑** | **高炉** | **空，且 `lit=0`** ✓ | — |
| 钛剑 / 钛胸甲 | 高能熔炉 | `titanium_nugget` ✓ | `moreores:titanium_nugget_from_high_energy_smelting` |
| （对照）铁矿 | 普通熔炉 | `iron_ingot` ✓ | — |

>「`lit=0`」是钛这一档最强的证据：不是「烧了但没产物」，而是**炉子压根没点火** ——
>说明普通熔炉的配方表里根本没有钛装备，配方是真的没登记，不是被产物逻辑挡了。

> ⚠️ 两个**测试侧**的坑，都让第一版脚本误报过：
>
> 1. **26.3 的方块实体 NBT 没有 `container` 这个路径** —— 槽位在 `Items` 列表里
>    （每条带 `Slot: 2b`）。按 `container.2` 读会全部读到「空」，
>    **连原版铁矿的对照组都「失败」** —— 这时候就该知道是脚本的问题，不是配方的问题。
> 2. **一定要 `/forceload`**，而且测试点要落在**会 tick 的那一块**。没有玩家时，
>    服务器上并不是所有区块都 tick：实测同一台服务器上 `(12,-58,12)` 的熔炉能烧、
>    `(20,-58,20)` 的完全不点火。表现是「前几条能烧、后面全空」，很容易误判成配方坏了。

---

### 8.25 美术重整：把明暗结构对齐原版

**问题**：新加的锭 / 工具 / 护甲和原版比，**边缘和暗部对比度不足、高光也几乎没有**。
先量化，再动手：

| | 原版铁锭 | 模组铅锭 | |
|---|---|---|---|
| 暗部像素 | 14 | 13 | ✓ 差不多 |
| **高光像素** | **49** | **0** | ❌ 完全没有 |
| **亮度跨度** | **202** | **100** | ❌ 只有一半 |

结构是对的，但**亮度被整体压扁了**。

#### 两个关键发现

**① 模组的贴图和原版是逐像素对齐的。**

`lead_sword` / `lead_helmet` / `titanium_ingot` … 全部与原版对应物品的**轮廓完全一致**
（差 0 格）。既然形状一样，就可以直接**借用原版的明暗**。

**② 原版所有金属共用同一套明暗，只换色相。**

| | 均值 | 暗部 | 高光 |
|---|---|---|---|
| 铁头盔 | 91.1 | 46 | 5 |
| 钻石头盔 | 91.7 | 46 | 5 |
| 铁胸甲 | 134.3 | 46 | 17 |
| 钻石胸甲 | 132.2 | 46 | 17 |

两者几乎一模一样 —— 所以**不需要**给每种金属挑不同的「参照」，用一套就行。

#### 做法

```
新亮度 = f(原版对应像素的亮度)          f 是各金属的身份曲线
新颜色 = 自己的 RGB × (新亮度 / 自己原来的亮度)
```

乘一个标量等于整体缩放，**色相和饱和度比例一点不变**，只改明暗。

**「钛更白」怎么保住**：用一条对**白端做压缩**的曲线

```
f(L) = L + k·(252 − L)·(L/255)²      k = 0.55
```

两头自动收敛 —— 越暗抬得越少（`L=53` 只抬 2.7，暗部对比度不丢），
越亮抬得也越少（`L=237` 只抬 7，不会冲成纯白），中间调抬得最多。

> 这条曲线**试错了两版**：
>
> | 尝试 | 结果 |
> |---|---|
> | 抛物线 `L + k·L·(255−L)/255` | 抬的是**中间调**、暗部跟着亮，对比度反而变小；还把一批 150~200 的像素推过 200，胸甲高光从原版 17 个涨到 **63** 个 |
> | 三次方 `L + k·255·(L/255)³` | 暗部保住了，但**白端没有上限** —— 护甲贴层（原版均值就有 201）整片冲成纯白，细节全丢 |
> | ✅ 白端压缩版 | 各项指标都贴住原版 |

**木柄要整体跳过**：工具的柄本来就是原版木棍的颜色，再按金属色相缩放会被染成金属色。
判定沿用之前那套「暖色 + 紧贴木色的暗边」。

#### 结果

| 指标 | 改后与原版的平均偏差 |
|---|---|
| **暗部像素数** | **0.0（最大 1）** |
| 亮度跨度 | 3.2% |
| 平均亮度 | 3.7% |
| 高光像素数 | 3.5（最大 46，见下）|

共重整 **36 张**：3 种金属 ×（锭 / 5 件工具），铅和钛 ×（4 件护甲 + 2 张穿戴贴层），
外加铅 / 铀的弓与箭。

> 两个「看起来偏差大但其实正常」的地方：
>
> - **铀的跨度只有 198（原版 231）**：绿色本身就有亮度上限（纯绿的亮度只有 0.587×255 ≈ 150），
>   高光端到不了纯白。这是色相的物理限制，不是没做对。
> - **钛的护甲贴层「高光像素 445（原版 245）」**：那张贴层原版均值就有 201，
>   半数以上像素本来就 > 200。对已经很亮的图来说，「> 200 的个数」这个指标不敏感，
>   该看的是**跨度**（63 vs 72 ✓）和**均值**（+6 ✓）。
>
> 顺带说一句，`_art_fix.py` 把 36 张图**原地改写**，跑之前一定要先备份。

---

### 8.26 配色：铅 = 冷调蓝灰，钛 = 微黄暖银

之前两种金属**都是冷色**（`b > g > r`），放一起要靠亮度才分得清。现在从色相上直接分开：

| 金属 | 色相（每通道均值 ÷ 均值亮度） | 观感 |
|---|---|---|
| **铅** | `(0.90, 0.98, 1.13)` | **冷调蓝灰** |
| **钛** | `(1.05, 1.01, 0.93)` | **微黄暖银** |
| 铀 | 绿（未动） | — |

改的是**色相，亮度一点没动** —— 上一轮刚把明暗结构对齐原版，这里不能白费：

```
新颜色 = 自己的 RGB × 通道系数        （系数 = 目标色相 ÷ 当前色相）
再乘一个标量把亮度拉回原值
```

覆盖范围：锭 / 粒 / 工具 / 护甲 / 穿戴贴层 / 种子 / 作物四个阶段 / 两种方块，
铅还包含弓和箭。

> ⚠️ **矿石（`lead_ore` / `titanium_ore` 等）没有动** —— 那几张图 76% 是石头底，
> 整体染色会把石头也染了（8.23 里专门记过这个坑）。矿点保持原来的色调。

#### 踩到的三个坑

**① 色相不能在「纯白像素」上量。**

原版最亮处是纯白 `(255,255,255)`，**纯白没有色相**，会把这个指标往下拽。
拿它当收敛目标的话怎么调都追不上 —— 实测迭代会一路发散：

```
一开始想用「目标 ÷ 当前」迭代求解，结果系数跑到了 (0.77, 0.80, 0.88)，
色相还是没到位。改成只统计亮度 60~220 的中间调像素才正常。
```

**② 系数必须在「任何文件被改之前」一次性算好。**

第一版是循环到哪张图才现算系数，结果轮到方块那批时，**锭已经被改成目标色了**，
解出来的系数就是 `1` —— 方块原封不动（实测 `lead_block` 改前改后一模一样）。

**③ 「暖银」不能整体加暖，高光必须去色。**

钛的底色本来就亮（均值 ~180），整体加暖之后**高光也跟着黄**，
16×16 下读出来是「**米黄 / 奶油**」而不是「暖银」：

| 版本 | 结果 |
|---|---|
| 目标 `(1.075, 1.020, 0.905)` | 偏米黄，不像银 |
| 目标 `(1.048, 1.008, 0.928)` | 好一些，还是偏奶油 |
| ✅ 再加一道**高光去色** | 高光回白、暖色只留在中间调 —— 真正的暖银 |

高光去色是把亮度 > 195 的像素按比例往 `(L, L, L)` 混，
**亮度一点都不变**，所以不会破坏对齐好的明暗结构。

#### 顺带：钛种子补进了创造栏

`moreores:titanium_seeds` 的物品、配方（钛锭 + 一圈小麦种子 → 8 个）、纹理、语言
**之前就都在**，只有创造模式物品栏漏了它 —— 所以游戏里 `/give` 拿得到、
但翻页签找不到。已补。

#### 又顺带：钛头盔的语言条目漏了

`item.moreores.titanium_helmet` 中英都缺 —— 游戏里直接显示**原始键名**
（`item.moreores.titanium_helmet`），而不是「钛头盔」。已补。

**为什么会漏**：加钛甲那轮是按字母序往语言文件里插条目的，
一次插 `boots` / `chestplate`、一次插 `leggings`、再加 `nugget`，
`helmet` 正好夹在两次插入之间，谁都没碰到它。

> 💡 **补完之后应该顺手扫一遍整张表**，别只修报出来的那一个：
> 把 `assets/moreores/items/*.json` 和 `blockstates/*.json` 里的名字
> 跟 `item.moreores.*` / `block.moreores.*` 的键做差集。
> 这次扫下来是「**68 个物品 + 26 个方块全部有条目，中英各 81 条对齐，没有多余也没有缺失**」。
>
> 这类漏条目**不报错、也不影响加载**，只在玩家把鼠标移上去的那一刻暴露 ——
> 所以只能靠这种全表核对，不能靠日志。

---

### 8.27 生物催化器：玻璃罩里的钛制「种树机」

> **原名「植物催化器」**。范围扩到肉类 / 鱼类 / 羊毛之后，整机改名为
> **生物催化器**：方块 id `moreores:bio_catalyzer`、配方类型
> `moreores:bio_catalyzing`、Java 类 `BioCatalyzer*` / `BioCatalyzingRecipe`、
> 资源文件名与语言条目全部跟着换了一遍。
>
> 改名是脚本做的，但**第一版漏了 Java 类文件** —— 判断条件只认带下划线的
> `plant_catalyzer`，而类名是驼峰 `PlantCatalyzerBlock`，两边对不上，
> 于是「内容改了、文件名没改」。Java 要求 public 类名与文件名一致，
> 必然编译失败。教训：**改名要按「去掉下划线后的小写」比，别按原样比**。

把**树苗 / 种子**催化成**木头 / 作物**的机器。合成表：

```
G G G     G = 玻璃
  D       D = 泥土（用 #minecraft:dirt 标签，草方块 / 灰化土 / 菌丝体 / 泥巴都行）
T T T     T = 钛锭
```

**外观**：自下而上三层 —— 钛底座（直接用钛块纹理）、罩内草坪（原版草方块顶面）、
玻璃罩（原版玻璃，占 12/16 ≈ 2/3）。

#### 四条和熔炉不同的规矩

| 规矩 | 怎么实现的 |
|---|---|
| **只吃核子电池** | `canPlaceItem` 对燃料槽卡死。煤、岩浆桶、铀锭一律塞不进来 |
| **燃料 10 倍速、产物速度不变** | 只覆写 `getBurnDuration` 除以 10，**不动** `getSpeedMultiplier` |
| **催化物不消耗** | `BioCatalyzerBlock` 里「快照 + 还原」（见下） |
| **催化物只放一个** | `mixin/FurnaceInputSlotMixin` 把输入槽的 `getMaxStackSize` 卡到 1 |

> ⚠️ **第 2 条和高能熔炉正好相反，别抄错**：
> 高能熔炉是 `speedMultiplier = 10` **且**时长 ÷10，两者抵消，
> 单位燃料的产出和普通熔炉一样（换来的是吞吐量 ×10）；
> 催化器**只除时长**，所以单位燃料的产出只有普通熔炉的 **1/10**。
>
> 一颗核子电池 200000 tick ÷10 = 20000 tick，按 200 tick/个算，
> **一颗电池正好出 100 个产物**。

#### 「催化物不消耗」怎么做的

没有去改字节码。父类的 `serverTick` 在产物完成时会 `shrink(1)`，
但那一步藏在很深的字节码里；这里改成**把 ticker 包一层**：

```
① 记下催化物    catalyzer.getCatalyst()
② 正常跑熔炉逻辑  furnaceTick.tick(...)     ← 这一步可能会把催化物吃掉
③ 还回去        catalyzer.restoreCatalyst(...)
```

逻辑留在自己的代码里、一眼能看懂，代价只是每 tick 多一次 `ItemStack.copy()`
（一个短命小对象，可以忽略）。

`restoreCatalyst` 只在「确实是同一种、且数量变少」时才补 ——
玩家中途把槽清空或换成别的东西时不会凭空冒出物品。

#### 能源耗尽的核子电池

核子电池烧完留下**能源耗尽的核子电池**（`textures/item/depleted_nuclear_battery.png`
是把核子电池去饱和 + 压暗 55% 得到的：同一副壳子，电用完了），
可以分解回 **4 个铅锭**。

> ⚠️ 它**不是**靠 `craftRemainder` 实现的。那个组件在熔炉里同样生效，
> 会和「副产物槽」那条路**重复产出两个**。统一走
> `mixin/FurnaceFuelMixin` 的副产物逻辑（高能熔炉早就是这么处理铀锭 → 铅锭的），
> 产出物落进第 4 格。

#### 界面与「透过玻璃看到种子」

- **界面**：`BioCatalyzerScreen` 直接**复用高能熔炉那张 GUI 底图**，
  槽位布局也一样（催化物 / 燃料 在左，产物 / 副产物 在右），
  连「产物槽上移到 y=17」都是同一条 Mixin 顺手管的。
  没继承 `HighEnergyFurnaceMenu` 是因为它的 `super(...)` 写死了菜单类型 ——
  而菜单类型正是客户端「用哪个界面」的依据，继承过来两台机器就混用一个类型了。
- **看到的是什么**：`BioCatalyzerRenderer` 在罩内草坪上**固定**摆一棵
  **橡树树苗**的微缩。

  > ⚠️ **它和槽里放什么无关。** 这里是整个渲染器最省事的地方 ——
  > 罩子里永远是同一棵小树苗，它只是台"生物机器"的装饰，
  > 不再承担"显示当前催化物"的职责。所以渲染器**根本不读方块实体**，
  > 连渲染状态都不需要带额外字段（直接用基类 `BlockEntityRenderState`，
  > 自己那个 `BioCatalyzerRenderState` 已经删掉了）。

  位置取 `y = 0.40`（模型里草坪顶面是 `6/16 = 0.375`），
  所以是「种在罩子里」而不是悬空旋转。

  > ⚠️ **方块模型的原点在角上，物品是居中的。** 所以缩放的前后要各动一次：
  >
  > ```java
  > pose.translate(0.5F, LAWN_TOP, 0.5F);
  > pose.scale(SCALE, SCALE, SCALE);
  > pose.translate(-0.5F, 0.0F, -0.5F);   // ← 少这一行会跑到右下角去
  > ```

  > ⚠️ **两个解析器的拿法不一样**：物品那个在 `Minecraft` 单例上
  > （`getItemModelResolver()`），方块那个 **只在渲染器上下文里给**
  > （`context.blockModelResolver()`）—— 必须从构造函数接过来存着。

#### 踩到的两个坑

**① 玻璃罩的渲染层，26.3 改由模型自己声明。**

旧版要写 `BlockRenderLayerMap.INSTANCE.putBlock(block, TRANSLUCENT)`，
但当前 Fabric API 里**这个类已经没有了**。26.3 的做法是在模型的纹理条目上标：

```json
"glass": { "force_translucent": true, "sprite": "minecraft:block/glass" }
```

（从原版 `models/block/glass.json` 里对出来的。）于是这次一行 Java 都没写。

**② 方块注册了，物品没注册 —— 服务器直接起不来。**

`ModBlocks` 里加了方块、`ModItems.register()` 里**忘了加
`registerBlockItem("bio_catalyzer", ...)`**，于是配方和掉落表引用的
`moreores:bio_catalyzer` 物品不存在，服务器启动就炸：

```
Failed to parse moreores:bio_catalyzer from pack moreores
Unknown registry key ... minecraft:item: moreores:bio_catalyzer
Failed to load datapacks, can't proceed with server load
```

> 教训：**加方块时，方块本体和它的 BlockItem 是两件事**，
> 少一个不会在编译期报错，只在数据包加载时才炸。

#### 坑③：模型不写 `parent`，物品栏里的图标就是「扁平」的

第一版的 `bio_catalyzer.json` **没有 `parent`**，只有一个裸的
`textures` + `elements`。方块在世界里看着正常，但**物品栏图标是平的**。

原因：`display` 变换是从 `parent` 继承来的。
裸模型没有 `display`，GUI 里用的就是**单位变换** ——
相机正对着 `+Z`，于是只能看到一个正对镜头的面（`south`），
三个元素在屏幕上**上下叠成一条**，完全没有立体感。

对照一下炉子的模型就清楚了，它写的是：

```json
"parent": "minecraft:block/orientable_with_bottom"
```

而这条链最终会走到原版模板 `minecraft:block/block`，里面正是标准的那套：

```json
"gui_light": "side",
"display": {
  "gui": { "rotation": [30, 225, 0], "scale": [0.625, 0.625, 0.625] }
}
```

`rotation [30, 225, 0]` 就是那个用了十几年的**等轴测图标视角**。

所以修法只有一行 —— 给模型补上：

```json
"parent": "minecraft:block/block"
```

> ⚠️ `block/block` 只提供 `display` 和 `gui_light`，**不含** `elements` / `textures`
> （那两个是子模型自己给的），所以几何和贴图完全不受影响，
> 只是把「怎么摆给镜头看」这套默认值继承了过来。

**验证办法**：按 `display` 的旋转做一次等轴测投影，数一下「正面朝向镜头的面有几个」：

| | 朝向镜头的面 | 效果 |
|---|---|---|
| 不写 `parent` | **3**（三个元素各一个 `south` 面）| 上下叠成一条，**扁平** |
| `block/block` | **9**（每个元素 顶 + 两侧）| 立方体，**有立体感** |

顺带一提：`ambientocclusion` 默认就是 `true`，所以补 `parent` **不会**改变方块在世界里的样子。

#### 实测

```
放：催化物 = 橡树树苗，燃料 = 核子电池，等 14 秒
  RecipesUsed: moreores:bio_catalyzing/tree_oak
  槽 0 催化物  -> oak_sapling ×1            ← 原样还在（没被消耗）✓
  槽 2 产物    -> oak_log ×1                ✓
  槽 3 副产物  -> depleted_nuclear_battery  ✓
  槽 1 燃料    -> 已烧尽                     ✓
再等 14 秒：催化物仍是 1 个，产物翻到 2 个    ← 「不消耗」是持续的 ✓

小麦种子 -> 小麦 ✓
```

催化配方一共 **57** 条：

| 类别 | 条数 | 写法 |
|---|---|---|
| 树苗 → 原木 | 9 | `oak_sapling` → `oak_log` |
| 原版作物原料 → 产物 | 9 | `wheat_seeds` → `wheat`、`sweet_berries` → `sweet_berries` |
| 本模组的矿物种子 → 自身 | 13 | `titanium_seeds` → `titanium_seeds` |
| 发光浆果 → 自身 | 1 | `glow_berries` → `glow_berries` |
| **生肉 → 自身** | **5** | 牛肉 / 猪排 / 鸡肉 / 羊肉 / 兔肉 |
| **生鱼 → 自身** | **4** | 鳕鱼 / 鲑鱼 / 热带鱼 / 河豚 |
| **羊毛 → 线** | **16** | 16 色全部，**一次出 1 根线** |

#### 两条刻意的"不给"

**① 熟肉 / 熟鱼不能复制。**

只给生肉。所以「先烧熟再无限拿」这条路是堵死的 ——
实测把 `cooked_beef` 放进去，炉子**连火都不点**（`lit_time_remaining = 0`），
不是"烧了没产物"，是压根没匹配上配方。

**② 羊毛只出线，不出羊毛。**

一次 **1 根线**（不是原版换算的 4 根）。羊毛本身不消耗，
所以产出是持续的 —— 这里卖的是"不消耗"，不再在比例上打折。

**为什么矿物种子是「复制」而不是出金属**：

① 模组的矿物作物成熟后掉的是**金属锭**。而这个机器的**催化物不消耗**，
   所以「种子 → 锭」等于「**一颗核子电池刷 100 个锭**」（电池 200000 tick
   ÷10 = 20000 tick，按 200 tick/个算正好 100 个）—— 挖矿那条线直接废掉。

② 那给「粒」行不行？也不行 —— 原版只有**铁 / 金 / 铜**粒，
   而模组的 13 种作物里有**钻石、绿宝石、青金石、红石、煤、石英**这 6 种
   根本没有对应的粒，做不到统一。

所以选**复制**，和已有的 `crop_sweet_berries`（浆果 → 浆果）是同一个套路。
实测（催化物不消耗这条对新配方同样成立）：

```
钛种子     -> titanium_seeds    催化物还在: moreores:titanium_seeds  crop_titanium
钻石种子    -> diamond_seeds     催化物还在: moreores:diamond_seeds   crop_diamond
下界合金种子  -> netherite_seeds   催化物还在: moreores:netherite_seeds crop_netherite
发光浆果    -> glow_berries      催化物还在: minecraft:glow_berries   crop_glow_berries
（对照）小麦种子 -> wheat ✓   胡萝卜 -> carrot ✓   橡树树苗 -> oak_log ✓
```

> ⚠️ 验证时限**别卡太紧**。这台机器要先「烧起来」才开始计进度，
> 最初我用 13 秒等 `wheat_seeds`，拿到的却是空槽 —— 看着像配方坏了，
> 其实就是差几十 tick。改成 25 秒后正常。判断有没有跑完要看
> `lit_time_remaining`（剩余燃料）折算出的实际运行时间，不能光看墙钟。

#### 矿物催化器（同款外壳）

`moreores:mineral_catalyzer` 复用了生物催化器的外壳与全部机器逻辑，只换三样东西：

- **配方桶**：独立的 `moreores:mineral_catalyzing`，只支持石头 / 泥土 / 黑曜石等
  20 种自然方块 → 自身的复制，两台催化器互相看不到对方的配方。
- **合成**：生物催化器中间的泥土换成石头。
- **外观**：罩内草坪上换成一小片岩浆（模型直接引用 `minecraft:block/lava_still`，
  保留原版动画），并恒定亮度 13 + `emissiveRendering`，也就是「常态发光」。

`MineralCatalyzerBlockEntity` 直接继承 `BioCatalyzerBlockEntity`，只改配方桶、
方块实体类型和菜单；「催化物不消耗」「只吃核子电池」「副产物槽」全部原样复用。

生物催化器另外新增了 **鸡蛋 → 鸡蛋** 的复制配方（45 条）。

---

### 8.28 下界合金胚：自己造下界合金

> **名字里不带颜色。** 它最初叫「玫瑰红的下界合金胚」，后来去掉了前缀 ——
> 和「钛锭」「铅锭」一个规矩：**名字只说是什么东西，外观交给贴图自己表达**。
> 贴图本身仍然是玫瑰红的（下面那套推导没动）。

原版下界合金靠「下界合金碎片 + 金锭」在锻造台上做。这里换一条路：
用模组已有的金属体系合成一块**中间产物**，再烧成下界合金锭。

**合成（17 个锭 → 1 块胚）**

```
G G G     G = 金锭
L U L     L = 铅锭      U = 铀块
I I I     I = 铁锭
```

| 用 | 产出 |
|---|---|
| 3 金 + 2 铅 + **9 铀**（一整块铀块）+ 3 铁 | **1 个**胚 |

> **芯是一整块铀块，不是一颗铀锭。** 下界合金是终局材料，中间产物也该有
> 对应的分量 —— 一格铀锭太轻了。于是成本从 9 个锭抬到 17 个锭。

> 曾经还有一条「9 个粒 → 1 个胚」的路子（9 粒 = 1 锭，换算下来和锭那条等价，
> 初衷是让手里零散的粒也用上），**现已删除** ——
> 留着等于给粒开了条捷径，和下界合金该有的成本不相称。

**冶炼**：`moreores:high_energy_smelting`，**只认高能熔炉**（和钛一个规矩）：

| 炉子 | 结果 |
|---|---|
| 普通熔炉 | 空，且 `lit=0` —— **连火都不点** ✓ |
| 高炉 | 空，且 `lit=0` ✓ |
| 高能熔炉 | `minecraft:netherite_ingot` ✓ |

#### 贴图

沿用本项目一贯的做法：**借原版铁锭的明暗结构，换成自己的色相**
（模组其它锭都是这个结构，摆一起才齐整）。

色相取玫瑰红 `(1.45, 0.72, 0.85)`，再按加权亮度归一化。

> ⚠️ **光有颜色不够，还得压暗。** 第一版直接套用原版铁锭的亮度（156），
> 出来是**粉红**而不是玫瑰红 —— 玫瑰红本身就是个**深红**色。
> 乘 0.65 后平均亮度落到 **101**，正好在「原版铁锭 156」和
> 「原版下界合金锭 59」之间，符合它「下界合金半成品」的身份。

#### 升级模板也能自己做了

原版的**下界合金升级模板**（`minecraft:netherite_upgrade_smithing_template`）
是堡垒遗迹里的战利品，只能"先捡到一个、再拿钻石复制"。
这里补一条**从零合成**的路子：

```
 D D D     D = 钻石
 D B D     B = 书
 D D D
```

也就是**书居中、八颗钻石围一圈** → 1 个升级模板。

这样一来，配合上面的合金胚路线，「钻石装备 → 下界合金装备」这条链
**全程不需要靠运气**：模板能合成、合金锭能烧制。

> 注：原版「复制模板」那条配方（7 钻石 + 1 下界岩 + 1 模板，保留原模板）
> 依然在，两条并存、互不冲突。新模板 id 与它不同，形状也完全不一样 ——
> 加完顺手扫了一遍全部定形配方，`DDD/DBD/DDD` 这个 pattern 没有撞车。

#### 顺带：生物催化器的合成表加了两块玻璃板

```
G G G     →     G G G
  D             P D P     P = 玻璃板
T T T           T T T
```

---

### 8.29 六张物品贴图重绘：碎片不再是同一个轮廓换色

§8.25 定的规矩是「明暗结构对齐原版」，但那条规矩管的是**同族物品**（锭 / 工具 / 盔甲）。
到了「碎片」这一族就翻了车 —— 四种宝石碎片**共用一个轮廓**：

| | 钻石碎片 | 绿宝石碎片 | 青金石碎片 | 石英碎片 |
|---|---|---|---|---|
| 改动前不透明像素 | 89 | 89 | 89 | 89 |

分毫不差。也就是说它们其实是**同一张斜棱柱贴图**刷了四次色相 ——
同类问题还有自然护符（绿宝石换色）和钛矿碎片（粗钛缩小 78%）。

现在改成**六种互不相同的轮廓**：

| 贴图 | 轮廓 | 想表达 | 实心像素 |
|---|---|---|---|
| 钻石碎片 | **倒三角**（钻石的亭部），尖被敲断 | 钻石被敲掉的那半个 | 106 |
| 绿宝石碎片 | **阶梯切面**：外圈 / 台阶 / 台面三级同心八边形 | 祖母绿切工 | 116 |
| 青金石碎片 | **不规则碎块 + 明暗两种斑点** | 青金石本来就是斑驳的石头 | 113 |
| 石英碎片 | **细长针状**，沿对角线摆 | 石英是尖锐的半透明白晶 | 68 |
| 钛矿碎片 | **灰矿块 + 三簇嵌着的金属亮斑** | 矿石 = 石头 + 里面的金属 | 114 |
| 自然护符 | **挂环 + 挂绳 + 一片叶子** | 挂在身上的护身符，不是一颗宝石 | 113 |

> 六个数字全不一样 —— 轮廓确实换掉了。**数不透明像素**依旧是判断
> 「是不是换色」最快的一招（见 §8.23 的「三种粗矿」）。

#### 两个设计上的取巧

**① 护符不做「圆牌 + 牌面上的图案」。** 16×16 上，一个 11 像素宽的圆牌
里再刻图案，图案只剩 3~4 像素，糊成一个点；而圆牌本身又和「宝石」撞脸。
所以直接把**形状本身**做成叶子（肩在上接挂环、顶尖朝下、一条主叶脉 + 三条侧脉）。

**② 阶梯切面靠 45° 分光才看得出来。** 绿宝石碎片是三级同心八边形，
每级比外一级缩进恰好 1 像素（`x±1 / y±1 / 倒角-1`）。
但只堆同心环的话，远看就是一块绿疙瘩 —— 阶梯的「棱」要靠明暗分界来表达：
按 `x + y` 切一刀，左上角提亮、右下角压暗，角上是斜的、正上和正下是横的，
台阶才立起来。

#### 做法：脚本生成，不手抠像素

新增 `_gen_textures.py`（后来金属块也并了进来，见 §8.30）。
手写 6 张 16×16 的像素表，错一位根本看不出来，所以分成三步：

1. `bands()` 声明每一行的左右端点 —— **形状**；
2. `recolor*()` 只给**已经存在**的像素改明暗 —— **配色**。
   这样明暗永远画不出界，改形状时也不用同步改配色（第一版就是在这里踩的：
   形状改窄了、配色还是老范围，边上多出一排悬空像素）；
3. `outline()` 自动描边 —— 紧挨着实心像素的透明格子一律变轮廓色，
   轮廓一定连续、一定闭合。

跑一次会生成六张图，外加：

* `_preview_item_textures.png` —— 上排是改动前的旧图（取自 `build/` 里那一份）、
  下排是新图，逐列对照；重新构建过之后 `build/` 会被覆盖，这张图就只剩对照价值了；
* `_preview_item_textures_zoom.png` —— 新图放到 20 倍，逐个像素核对。

#### 顺带：下界合金胚的芯换成铀块

原配方中间那格是**一颗铀锭**，现在改成**一整块铀块**（= 9 个铀锭）：

```
G G G     G = 金锭
L U L     L = 铅锭      U = 铀块
I I I     I = 铁锭
```

成本从「9 个锭」抬到 **3 金 + 2 铅 + 9 铀 + 3 铁 = 17 个锭**。
下界合金是终局材料，中间产物也该有对应的分量。

> 配方文件跟着改名为 `netherite_blank.json` —— 原料里已经有块了，
> 再叫 `from_ingots` 就是错的；这个配方也只有这一条，不需要用来源区分。

---

### 8.30 三种金属块：把「纯色板」做成金属

§8.21 就写过「摆脱扁平感靠结构不靠随机」，但那一条只落到了高能熔炉上。
这次一量，**方块级的金属**其实一直是纯色板：

| 方块 | 改动前 | | | 改动后 | | |
|---|---|---|---|---|---|---|
| | 颜色数 | 亮度区间 | 均值 | 颜色数 | 亮度区间 | 均值 |
| 铅块 | 62 | **124~156** | 145 | 36 | **51~198** | 140 |
| 钛块 | 21 | **175~198** | 188 | 48 | **64~255** | 174 |
| 铀块 | 67 | 134~209 | 166 | 23 | **34~244** | 148 |

铅块只跨 **32 档**亮度、钛块只跨 **23 档** —— 那不是金属，是一块刷了色的板子。
加多少噪点都救不回来：**没有明暗结构，噪点只是脏。**

#### 金属板的通用骨架

三张图共用同一套结构，观感才是一家人：

1. **外圈暗框** → **第二圈左上受光 / 右下背光** → 面；
2. **面内竖向渐变**：一块板子被上方的光照亮，上段亮、下段暗；
3. **金属噪点**：每像素 ±3.5% 亮度扰动，再量化到 4 的整数倍
   （不量化的话 256 个像素会各自变成一个颜色，噪点就"脏"了）。

在这之上，铅和钛**工艺不同**，所以细节不同：

| | 表面 | 立体细节 | 观感 |
|---|---|---|---|
| **铅块** | 横向轧制痕 | **四角铆钉**（2×2，左上受光右下背光） | 压延出来的铅板，哑光 |
| **钛块** | 一段式竖向渐变 | **45° 镜面反射** + 左上角抛光高光 | 抛光件，亮 |

> 钛块第一版在面上铺了「每 3 像素一道 45° 拉丝」——
> 结果 16×16 下密到看不出方向，读出来是**花岗岩 / 粗花呢**，
> 正是 §8.21 警告过的那个坑。抛光面不需要纹理，需要的是**干净的大色阶**。
> 换成「一条 `x+y=10/11` 的斜向亮带」之后，镜面反射一眼就出来了。

配色仍按 §8.26：铅 = 冷调蓝灰、钛 = 微黄暖银。实测中间调色相：

| | 实测 | §8.26 的目标 |
|---|---|---|
| 铅块 | `(0.899, 1.017, 1.175)` | `(0.90, 0.98, 1.13)` |
| 钛块 | `(1.019, 1.002, 0.943)` | `(1.04, 1.01, 0.95)` |

钛比目标略淡一点 —— 坑③ 已经记过：暖色一重就成**奶油色**，宁可欠一点。

#### 铀块：花纹换了三代，最后落在一块六边形晶核上

| 代 | 花纹 | 问题 |
|---|---|---|
| ① | 荧光绿底 + 一朵四瓣的浅色「花」 | 既不像金属也不像放射源 |
| ② | 深色板 + **辐射三叶标志** | 三个扇叶在 12×12 上量化出来就是**三个三角**，读着像三角形拼图 |
| ③ | 深色板 + **发光六边形晶核** | ✅ 没有三角，也更像"一块会发光的铀" |

② 的两个坑仍然值得记：

**① 用「半径 ± 张角」算扇形是不行的。** 公式本身没错，但在 12×12 上量化之后：
外圈被裁成方的、扇叶忽宽忽窄，中心点和扇叶之间还会贴上，
两轮出来的都是**三叶草**。改成手摆 12 行字符表之后形状是对的，
**但形状对不等于读得出来** —— 那三个扇叶最后看着就是三个三角，
所以 ③ 干脆换成没有歧义的六边形。

**② 花纹不能描光晕。** 花纹几乎占满 12×12 的面，往外扩一圈光晕就会
把「扇叶之间的缝」一起填亮 —— 而**那道缝正是花纹的轮廓**。
上一版铺了两层光晕，整个面几乎全是光晕，板子也不暗了，花纹反而糊掉。
最后贴着花纹一圈光晕都不留，深底 + 亮花纹就够了；
真要发光应该走方块自发光（`lightLevel`），不是画在贴图上。

③ 的做法：六边形**手摆成 10 行**（最宽 8 像素），
最外一圈提亮、内部按 `x+y` 做 45° 分光（左上受光、右下背光），
正中一枚 **2-4-2 的小菱形白热核**。核的大小试过三档：
等宽竖条读成"药丸"、铺到 16 像素读成"绿眼睛"，8 像素刚好是"里头有东西在发亮"。

#### 顺带：物品贴图和方块贴图合成一个脚本

原来只有 `_gen_item_textures.py`。这次既然要动方块贴图，就并成了
`_gen_textures.py`（物品 / 金属块两节 + 一个通用的 `render()`），
并完拿哈希对过：**六张物品图逐字节不变**。

脚本另外多了一个 `--ascii` 开关：把图按亮度打成字符画。
16×16 上「结构对不对」看字符比看图准 —— 上面那两个坑都是这么看出来的。

---

### 8.31 把两个生物整个删掉了

模组**不再新增任何生物**。删掉的是：

| 生物 | 删掉的东西 |
|---|---|
| **辐射僵尸** | 实体类、渲染器、皮肤、刷怪蛋（物品 + 模型 + 贴图）、战利品表、`addSpawn` 生成注入 |
| **女仆** | 实体类、模型（4 个类）、渲染器、贴图、刷怪蛋、Blockbench 工程与生成脚本 |

**没删的**（这些不属于「生物」）：

- **辐射粒子** `ModParticles.RADIATION` —— 它是 `RadiationEffect` 用的粒子，
  辐射 debuff 自己要用，删了效果就没粒子了。
- **辐射系统**整体（剂量结算、铅甲屏蔽、盖革计数器、铀箭）—— 照常。

#### 一个容易误判的现象：旧存档会刷 `Skipping Entity`

删完跑服务端，日志里跳出：

```
Skipping Entity with id moreores:maid
chunk@[0, 0].Entities[1]: Unknown registry key in ... entity_type: moreores:maid
```

这**不是删漏了** —— 是旧存档里还存着这两只生物的实体数据。
实体类型注销之后，MC 读存档时认不出 id，就跳过并警告。
属预期行为，原版卸模组也是这套表现，存档不会坏。
真要清干净，进游戏用 `/kill @e[type=...]` 提前清一遍即可。

> ⚠️ 反直觉的点：存档里的**物品**数据同样会失效。
> `maid_spawn_egg` 这个 Item 注销后，日志会追加一条
> `Unknown registry key ... item: moreores:maid_spawn_egg`。
> 所以删生物要**四处一起清**：实体类型、刷怪蛋物品、战利品表、生物群系生成注入。
> 漏掉任何一处都**不会编译报错**，只会在运行时冒警告。

#### 清理清单

删文件（14 个）：

```
main/java/.../entity/RadiationZombie.java
main/java/.../entity/Maid.java
client/java/.../render/RadiationZombieRenderer.java
client/java/.../render/MaidModel.java / MaidModelBase.java
client/java/.../render/MaidRenderer.java / MaidRenderState.java
resources/assets/.../textures/entity/maid.png
resources/assets/.../textures/entity/zombie/radiation_zombie.png
resources/assets/.../textures/item/radiation_zombie_spawn_egg.png
resources/assets/.../items/radiation_zombie_spawn_egg.json
resources/assets/.../models/item/radiation_zombie_spawn_egg.json
resources/data/.../loot_table/entities/radiation_zombie.json
```

改注册（7 处）：

| 文件 | 改动 |
|---|---|
| `ModEntities` | 删两个实体类型，连带清掉 `FabricEntityType` / `SpawnPlacementTypes` / `Monster` / `Heightmap` 导入 |
| `ModItems` | 删两个刷怪蛋 + `SpawnEggItem` 导入 |
| `ModItemGroups` | 删两行 `output.accept` |
| `world/ModWorldGen` | 删 `RADIATION_ZOMBIE_WEIGHT` 常量与 `addSpawn` 调用 + 两个导入 |
| `MoreOresClient` | 删两个 `EntityRendererRegistry.register` + 导入 |
| `lang/zh_cn.json` | 删 4 条（2 实体名 + 2 刷怪蛋） |
| `lang/en_us.json` | 同上 |

#### 验证（服务端 + 客户端）

```
maid_spawn_egg 物品               已移除 ✓
summon moreores:maid              Can't find element ✓
summon moreores:radiation_zombie  Can't find element ✓
summon moreores:metal_arrow       OK ✓（没误伤别的实体）
effect moreores:radiation 给猪     Applied effect Radiation ✓（辐射系统还在）
客户端                             零缺贴图 / 坏模型告警 ✓
```

---

## 9. 参考链接

- Fabric 官方版本/模板生成器：<https://fabricmc.net/develop/> 、<https://fabricmc.net/develop/template/>
- Fabric 开发文档：<https://docs.fabricmc.net/develop/>
- Loom 选项参考：<https://docs.fabricmc.net/zh_cn/develop/loom/options>
- BMCLAPI（国内 Minecraft 镜像）：<https://bmclapi2.bangbang93.com>
- 官方示例仓库：<https://github.com/FabricMC/fabric-example-mod>（分支 `26.3`）

---

## 10. 许可证

模板部分来自 Fabric 官方 `fabric-example-mod`，采用 CC0-1.0（见 `LICENSE`）。
