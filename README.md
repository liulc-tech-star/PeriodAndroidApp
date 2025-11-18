# 月期知纪 · Period-Mobile

一款基于 Jetpack Compose 开发的 Android 经期跟踪应用，提供完全离线的经期记录、周期预测和可视化日历功能。应用专注于隐私保护，所有数据保存在本地设备，永不联网。

## ✨ 核心特性

### 📅 日历式交互
- **直观的日历视图**：月度日历显示，支持左右切换月份
- **点击记录经期**：
  - 第一次点击：标记经期开始（深红色）
  - 第二次点击：标记经期结束（浅红色），自动填充中间日期
  - 第三次点击：删除整个经期记录
- **实时预览**：待完成的开始日期以黄色高亮显示

### 🔮 智能周期分析
- **自动计算周期**：基于最近两次经期自动计算周期长度
- **四阶段显示**：
  - 🩷 经期（Period）- 红色系
  - 🟣 卵泡期（Follicular Phase）- 浅紫色
  - 🟨 排卵期（Ovulation Phase）- 浅黄色
  - 🟢 黄体期（Luteal Phase）- 浅绿色
- **排卵日标记**：用 👶 图标直观显示排卵日
- **下次经期预测**：浅粉红色显示预测的下次经期日期

### 📊 完整历史支持
- **多周期显示**：所有历史周期的阶段信息都会在日历中显示
- **无限回溯**：支持查看任意月份的经期记录和周期阶段
- **持续预测**：基于最新两个周期持续更新预测

### 🎨 精美UI设计
- **Material 3 设计**：现代化的 Material Design 3 风格
- **颜色系统**：
  - 经期：深红 → 红 → 浅红（三种深浅）
  - 预测经期：浅粉红
  - 卵泡期：浅紫
  - 排卵期：浅黄 + 👶 排卵日标记
  - 黄体期：浅绿
- **横排图例**：简洁明了的图例说明，一目了然
- **今日标记**：当前日期用粉色圆圈边框高亮

### 🔒 隐私至上
- **完全离线**：无网络权限，数据永不离开设备
- **本地存储**：Room 数据库本地存储，卸载即删除
- **首次隐私协议**：首次启动需同意隐私政策才能使用

## 🏗️ 技术架构

### 核心技术栈
- **语言**：Kotlin 100%
- **UI框架**：Jetpack Compose Material 3
- **数据库**：Room Database 2.7.0（版本 4）
- **架构模式**：MVVM + Repository Pattern
- **日期处理**：Java Time API (LocalDate, YearMonth)
- **异步处理**：Kotlin Coroutines + Flow

### 构建配置
- Android SDK 35 (API Level 35)
- Gradle 8.11.1
- JDK 21 (HotSpot)
- Kotlin 1.9.0

## 🚀 快速开始

### 环境要求
- Android Studio (Hedgehog 或更高版本)
- Android SDK 35
- JDK 21

生成的 APK 位于：`app\build\outputs\apk\debug\app-debug.apk`

## 📱 使用指南

### 首次使用
1. 启动应用，阅读并同意隐私协议（内容来自 `privacy.html`）
2. 进入日历主界面

### 记录经期
1. **标记开始**：点击经期开始日期（变为深红色）
2. **标记结束**：点击经期结束日期（自动填充中间日期）
3. **删除记录**：再次点击该经期的任意日期，删除整个经期

### 查看周期信息
- **切换月份**：点击左右箭头切换月份
- **查看历史**：所有历史周期的阶段都会显示在对应月份
- **查看预测**：粉红色区域为预测的下次经期
- **识别排卵日**：👶 图标标记的日期为排卵日

### 图例说明
- 🔴 **经期开始** - 深红色
- 🔴 **经期中** - 红色
- 🔴 **经期结束** - 浅红色
- 🩷 **预测经期** - 浅粉红色
- 🟣 **卵泡期** - 浅紫色
- 🟨 **排卵期** - 浅黄色
- 👶 **排卵日** - 婴儿图标
- 🟢 **黄体期** - 浅绿色

## 📂 项目结构

```
Period-Mobile/
├── app/
│   ├── src/main/java/com/example/period_app_01/
│   │   ├── MainActivity.kt              # 应用入口
│   │   ├── EnterDate.kt                 # 主界面
│   │   ├── CalendarView.kt              # 日历视图组件
│   │   └── data/
│   │       ├── Dates.kt                 # 旧数据模型（已废弃）
│   │       ├── DatesDao.kt              # 旧DAO（已废弃）
│   │       ├── PeriodRecord.kt          # 新经期记录模型
│   │       ├── PeriodRecordDao.kt       # 新DAO
│   │       ├── CycleAnalysis.kt         # 周期分析数据类
│   │       ├── DatesDatabase.kt         # Room数据库
│   │       └── OfflineDatesRepository.kt # 数据仓库
│   ├── src/main/assets/
│   │   └── privacy.html                 # 隐私政策
│   └── src/main/res/                    # 资源文件
├── build.gradle.kts                     # 构建配置
├── gradle.properties                    # Gradle属性
├── settings.gradle.kts                  # 项目设置
└── README.md                            # 本文件
```

## 🗃️ 数据库设计

### PeriodRecord 表（版本 4）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Int | 主键，自增 |
| date | LocalDate | 日期 |
| recordType | Int | 0=待定, 1=开始, 2=中间, 3=结束 |
| periodId | Long | 经期ID（同一经期共享） |
| createdAt | Long | 创建时间戳 |

### 周期计算逻辑
```kotlin
// 基于两次经期开始日期计算周期
cycleLength = 第二次开始 - 第一次开始

// 预测下次经期
nextPeriodDate = 最近开始 + cycleLength

// 排卵日（黄体期通常14天）
ovulationDay = nextPeriodDate - 14天

// 排卵期（排卵日前3天至后4天，共8天）
ovulationPhase = ovulationDay ± 3-4天

// 卵泡期（经期结束后至排卵期前）
follicularPhase = 经期结束 到 排卵期开始

// 黄体期（排卵期结束至下次经期前）
lutealPhase = 排卵期结束 到 下次经期前
```

## 🔐 隐私与数据安全

### 隐私保护措施
- ✅ **零网络权限**：AndroidManifest.xml 中无任何网络权限
- ✅ **本地存储**：所有数据仅存储在本地 Room 数据库
- ✅ **随时删除**：用户可随时删除任何记录
- ✅ **卸载即清除**：卸载应用即删除所有数据
- ✅ **首次协议**：首次启动必须同意隐私政策

### 数据使用说明
- 经期记录用于计算周期长度和预测
- 周期分析用于显示各阶段日期范围
- 所有计算均在本地完成
- 不收集、不上传、不共享任何数据

## 🎯 开发路线图

### 已完成 ✅
- [x] 日历式交互界面
- [x] 点击记录经期（开始/结束/删除）
- [x] 自动周期计算
- [x] 四阶段显示（经期/卵泡期/排卵期/黄体期）
- [x] 排卵日标记
- [x] 下次经期预测
- [x] 多周期历史显示
- [x] 横排图例
- [x] Material 3 UI
- [x] 完全离线

### 计划中 🚧
- [ ] 症状记录（痛经、情绪等）
- [ ] 数据导出/导入
- [ ] 经期提醒通知
- [ ] 统计图表
- [ ] 深色模式
- [ ] 多语言支持

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

### 开发流程
1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

### 代码规范
- 遵循 Kotlin 官方编码规范
- 使用 Compose 最佳实践
- 添加必要的注释和文档
- 保持代码简洁可读

## 📄 开源协议

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

## 👨‍💻 作者

**liulc-tech-star**
- GitHub: [@liulc-tech-star](https://github.com/liulc-tech-star)
- Repository: [Period-Mobile](https://github.com/liulc-tech-star/Period-Mobile)

## 🙏 致谢

- [Jetpack Compose](https://developer.android.com/jetpack/compose) - 现代化 Android UI 工具包
- [Room Database](https://developer.android.com/training/data-storage/room) - Android 持久化库
- [Material Design 3](https://m3.material.io/) - Google 设计系统

---

**注意**：本应用仅用于个人经期记录和参考，不能替代专业医疗建议。如有健康问题，请咨询医生。
