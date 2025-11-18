# PeriodAndroidApp

<div align="center">

**一款注重隐私的 Android 经期跟踪应用**

基于 Jetpack Compose 构建 | 完全离线 | Material Design 3

[![Kotlin](https://img.shields.io/badge/Kotlin-100%25-blue.svg)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-26%2B-green.svg)](https://developer.android.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

</div>

## 概述

PeriodAndroidApp 是一款完全离线的 Android 经期跟踪应用，提供智能周期分析和直观的日历界面。应用采用 Material Design 3 设计语言，所有数据本地存储，永不联网。

### 核心功能

- 📅 **日历式交互** - 点击即可记录经期开始、结束和删除
- 🔮 **智能预测** - 基于历史数据自动计算周期并预测下次经期
- 📊 **四阶段可视化** - 经期、卵泡期、排卵期、黄体期
- 🔒 **完全离线** - 零网络权限，数据永不离开设备
- 🎨 **Material 3 设计** - 现代化界面，支持今日高亮

## 截图

> *待添加应用截图*

## 技术栈

| 类别 | 技术 |
|------|------|
| **语言** | Kotlin 100% |
| **UI 框架** | Jetpack Compose + Material 3 |
| **数据库** | Room Database 2.7.0 |
| **架构** | MVVM + Repository Pattern |
| **异步** | Kotlin Coroutines + Flow |
| **日期处理** | Java Time API (LocalDate) |

### 构建环境

```
Android SDK: 35 (API Level 35)
Gradle: 8.11.1
Kotlin: 1.9.0
JDK: 11
Min SDK: 26 (Android 8.0)
```

## 快速开始

### 环境要求
- Android Studio (Hedgehog 或更高版本)
- Android SDK 35
- JDK 21

生成的 APK 位于：`app\build\outputs\apk\debug\app-debug.apk`

## 使用指南

### 记录经期

| 操作 | 效果 |
|------|------|
| 第一次点击日期 | 标记经期开始（深红色） |
| 第二次点击日期 | 标记经期结束（自动填充中间日期） |
| 第三次点击 | 删除整个经期记录 |

### 界面说明

- **切换月份**：点击左右箭头
- **今日标记**：粉色圆圈边框
- **颜色图例**：
  - 🔴 深红/红/浅红 - 经期开始/中/结束
  - 🩷 浅粉红 - 预测经期
  - 🟣 浅紫 - 卵泡期
  - 🟨 浅黄 - 排卵期
  - 👶 婴儿图标 - 排卵日
  - 🟢 浅绿 - 黄体期

## 项目结构

```
PeriodAndroidApp/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/period_app_01/
│   │   │   ├── MainActivity.kt           # 应用入口
│   │   │   ├── EnterDate.kt              # 主界面
│   │   │   ├── CalendarView.kt           # 日历组件
│   │   │   └── data/
│   │   │       ├── PeriodRecord.kt       # 数据模型
│   │   │       ├── PeriodRecordDao.kt    # DAO
│   │   │       ├── CycleCalculator.kt    # 周期计算
│   │   │       └── DatesDatabase.kt      # Room 数据库
│   │   ├── assets/
│   │   │   └── privacy.html              # 隐私政策
│   │   └── res/                          # 资源文件
│   └── build.gradle.kts
├── gradle/
│   └── libs.versions.toml                # 依赖版本管理
├── build.gradle.kts
└── settings.gradle.kts
```

## 数据库设计

### PeriodRecord 表

```kotlin
@Entity(tableName = "period_records")
data class PeriodRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: LocalDate,              // 日期
    val recordType: Int,              // 0=待定, 1=开始, 2=中间, 3=结束
    val periodId: Long,               // 经期ID（同一周期共享）
    val createdAt: Long               // 创建时间戳
)
```

### 周期计算算法

```kotlin
// 1. 周期长度 = 最近两次经期开始日期的间隔
cycleLength = secondStartDate - firstStartDate

// 2. 预测下次经期 = 最近开始日期 + 周期长度
nextPeriod = lastStartDate + cycleLength

// 3. 排卵日 = 预测经期 - 14天（黄体期固定约14天）
ovulationDay = nextPeriod - 14

// 4. 排卵期 = 排卵日 ± 3-4天（共8天）
ovulationPhase = [ovulationDay-3, ovulationDay+4]

// 5. 卵泡期 = 经期结束 到 排卵期开始
follicularPhase = [periodEnd+1, ovulationPhase.start-1]

// 6. 黄体期 = 排卵期结束 到 下次经期前
lutealPhase = [ovulationPhase.end+1, nextPeriod-1]
```

## 隐私与安全

### 隐私承诺

- ✅ **零网络权限** - `AndroidManifest.xml` 无网络权限声明
- ✅ **本地存储** - 所有数据仅存储在 Room 数据库
- ✅ **用户控制** - 可随时删除任何记录
- ✅ **卸载清除** - 卸载应用即删除所有数据
- ✅ **隐私协议** - 首次启动需同意隐私政策

### 数据使用

- 经期记录仅用于本地周期计算和预测
- 所有计算均在设备本地完成
- 不收集、不上传、不共享任何个人数据

## 路线图

### ✅ 已完成

- [x] 日历式交互界面
- [x] 经期记录（开始/结束/删除）
- [x] 智能周期计算
- [x] 四阶段可视化
- [x] 排卵日标记
- [x] 下次经期预测
- [x] 多周期历史显示
- [x] Material 3 UI

### 🚧 计划中

- [ ] 症状记录（痛经、情绪等）
- [ ] 数据导出/导入
- [ ] 本地提醒通知
- [ ] 统计图表与报告
- [ ] 深色模式
- [ ] 多语言支持（英语、日语等）

## 贡献

欢迎提交 Issue 和 Pull Request！

### 开发流程

1. Fork 本仓库
2. 创建特性分支：`git checkout -b feature/your-feature`
3. 提交更改：`git commit -m 'Add your feature'`
4. 推送分支：`git push origin feature/your-feature`
5. 提交 Pull Request

### 代码规范

- 遵循 [Kotlin 官方编码规范](https://kotlinlang.org/docs/coding-conventions.html)
- 使用 Jetpack Compose 最佳实践
- 保持代码简洁可读，添加必要注释

## 许可证

本项目采用 [MIT 许可证](LICENSE)。

## 作者

**liulc-tech-star**

- GitHub: [@liulc-tech-star](https://github.com/liulc-tech-star)
- 项目地址: [PeriodAndroidApp](https://github.com/liulc-tech-star/PeriodAndroidApp)

## 致谢

- [Jetpack Compose](https://developer.android.com/jetpack/compose) - Android 现代 UI 工具包
- [Room Database](https://developer.android.com/training/data-storage/room) - Android 持久化库
- [Material Design 3](https://m3.material.io/) - Google 设计系统

---

<div align="center">

**免责声明**

本应用仅供个人经期记录和参考使用，不能替代专业医疗建议。  
如有健康问题，请咨询专业医生。

Made with ❤️ by liulc-tech-star

</div>
