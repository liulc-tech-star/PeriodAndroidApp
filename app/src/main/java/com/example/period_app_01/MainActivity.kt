package com.example.period_app_01

import android.os.Bundle
import android.content.Context
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
//import androidx.compose.ui.tooling.preview.Preview
import com.example.period_app_01.ui.theme.Period_app_01Theme
import com.example.period_app_01.data.DatesDatabase
import com.example.period_app_01.data.DatesDao

// ADD COMMENTS TO NEW FUNCTIONALITY
// DEPLOY APP TO MOTOROLA
// SETUP UNIT TESTS
// REFACTOR ENTERDATE.KT (too much going on in single class)

/*
 * main class, extends ComponentActivity class
 * what is ComponentActivity class?
 * what is an Activity?
 */
class MainActivity : ComponentActivity() {
    /*
     * declaration of variable, DatesDao object, implements methods which allow access to data
     * lateinit allows declaration of non-nullable property without performing null check (i.e. == null)
     * why use laneinit in this context?
     */
    private lateinit var datesDao: DatesDao
    /*
     * overrides onCreate method as defined in ComponentActivity class
     * onCreate initializes an activity (e.g. initializing UI, preparing data, etc.)
     * 
     * what is an Activity?
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        //
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        //
        val database = DatesDatabase.getDatabase(applicationContext)
        //
        datesDao = database.datesDao()

        // 检查是否首次启动
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val isFirstLaunch = !sharedPreferences.getBoolean("privacy_accepted", false)

        setContent {
            Period_app_01Theme {
                var showPrivacyDialog by remember { mutableStateOf(isFirstLaunch) }
                var showMainContent by remember { mutableStateOf(!isFirstLaunch) }

                if (showPrivacyDialog) {
                    PrivacyPolicyDialog(
                        onAccept = {
                            // 保存用户同意状态
                            sharedPreferences.edit()
                                .putBoolean("privacy_accepted", true)
                                .apply()
                            showPrivacyDialog = false
                            showMainContent = true
                        },
                        context = applicationContext
                    )
                }

                if (showMainContent) {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        EnterDate(
                            messageDate = resources.getString(R.string.enter_date),
                            modifier = Modifier.padding(innerPadding),
                            datesDao = datesDao
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PrivacyPolicyDialog(onAccept: () -> Unit, context: Context) {
    // 从 assets 目录读取隐私政策HTML文件内容
    val htmlContent = try {
        context.assets.open("privacy.html").bufferedReader().use { it.readText() }
    } catch (e: Exception) {
        // 如果读取失败，返回简化版本
        """
        <html><body style="padding:15px;font-family:Arial;line-height:1.6;">
        <h2 style="text-align:center;">隐私政策</h2>
        <p>本应用不联网，您的所有个人信息仅存储在本地设备中。</p>
        <p>开发者：微同学</p>
        <p>联系邮箱：liulc_tech@outlook.com</p>
        </body></html>
        """.trimIndent()
    }

    AlertDialog(
        onDismissRequest = { /* 禁止点击外部关闭 */ },
        title = { Text("隐私政策") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            ) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.javaScriptEnabled = false
                            settings.setSupportZoom(true)
                            settings.builtInZoomControls = true
                            settings.displayZoomControls = false
                            loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onAccept) {
                Text("同意并继续")
            }
        },
        dismissButton = null // 不提供"拒绝"按钮，必须同意才能使用
    )
}

/* below is preview, will use when working on more sophisticated UI
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Period_app_01Theme {
        Greeting("Android")
    }
}
*/