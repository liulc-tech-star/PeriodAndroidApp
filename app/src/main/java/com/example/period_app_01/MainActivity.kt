package com.example.period_app_01

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.period_app_01.ui.theme.Period_app_01Theme
import com.example.period_app_01.data.DatesDatabase

/**
 * 主活动类，应用入口
 * 负责初始化数据库
 */
class MainActivity : ComponentActivity() {
    private lateinit var periodRecordDao: com.example.period_app_01.data.PeriodRecordDao
    
    /**
     * onCreate 初始化活动
     * 初始化数据库和 UI
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = DatesDatabase.getDatabase(applicationContext)
        periodRecordDao = database.periodRecordDao()

        setContent {
            Period_app_01Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    EnterDate(
                        modifier = Modifier.padding(innerPadding),
                        periodRecordDao = periodRecordDao
                    )
                }
            }
        }
    }
}
