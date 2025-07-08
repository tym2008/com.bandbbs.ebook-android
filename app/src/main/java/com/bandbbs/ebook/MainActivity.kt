package com.bandbbs.ebook


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.bandbbs.ebook.logic.InterHandshake
import com.bandbbs.ebook.ui.compoments.AboutDialog
import com.bandbbs.ebook.ui.compoments.EbookCard
import com.bandbbs.ebook.ui.compoments.EbookScaffold
import com.bandbbs.ebook.ui.compoments.NormalButton
import com.bandbbs.ebook.ui.compoments.PushButton
import com.bandbbs.ebook.ui.theme.EbookTheme
import com.bandbbs.ebook.utils.FileUtils
import com.bandbbs.ebook.utils.bytesToReadable
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.math.max


class MainActivity : ComponentActivity() {
    private lateinit var conn : InterHandshake
    private lateinit var filePicker: ActivityResultLauncher<Array<String>>
    private lateinit var fileHandler: (Uri?) -> Unit
    private var fileuri: Uri?=null
    private var isFileSelected = false
    private var isConnected = false
    private var updateDevice = {}

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        conn = InterHandshake(this,lifecycleScope)
        (application as App).conn = conn
        filePicker = registerForActivityResult(ActivityResultContracts.OpenDocument()){
            fileHandler(it)
        }
        setContent {
            MainActivityPage()
        }
    }
    override fun onDestroy() {
        conn.destroy()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        updateDevice()
    }
    @Preview
    @Composable
    private fun MainActivityPage() {
        var showAboutDialog by remember { mutableStateOf(false) }
        var showButton by remember { mutableStateOf(false) }
        EbookTheme {
            EbookScaffold(
                title = "喵喵电子书同步器",
                onback = {
                    finish()
                },
                onmenu = {
                    showAboutDialog = !showAboutDialog
                }
            ){
                val context = androidx.compose.ui.platform.LocalContext.current
                val packageManager = context.packageManager
                val packageName = context.packageName
                val packageInfo = packageManager.getPackageInfo(packageName, 0)
                val versionName = packageInfo.versionName


                if (showAboutDialog) {
                    AboutDialog(
                        onDismissRequest = { showAboutDialog = false },
                        onConfirmation = { showAboutDialog = false },
                        versionName = versionName
                    )
                }
                Column (
                    modifier = it
                        .fillMaxSize()
                        .background(
                            color = colorResource(R.color.page_bg)
                        )
                        .padding(10.dp)
                ){
                    Column (
                        modifier = Modifier
                            .fillMaxWidth()
                            .zIndex(1f)
                    ) {
                        DeviceCard()
                        FileSelectCard { show ->
                            showButton = show
                            isFileSelected = show
                        }
                    }

                    AnimatedVisibility(
                        visible = showButton,
                        modifier = Modifier
                            .fillMaxWidth()
                            .zIndex(0f),    // 设个低 zIndex
                        enter = slideInVertically(
                            // 初始偏移：整个内容高度的负值（即在顶部边界外）
                            initialOffsetY = { fullHeight -> -fullHeight },
                            animationSpec = tween(durationMillis = 500)
                        ) + fadeIn(animationSpec = tween(500)),
                        exit = slideOutVertically(
                            // 目标偏移：向上滑动到边界外
                            targetOffsetY = { fullHeight -> -fullHeight },
                            animationSpec = tween(durationMillis = 500)
                        ) + fadeOut(animationSpec = tween(500))
                    ) {
                        Column {
                            NormalButton(
                                text = "重新选择",
                                onClick = { filePicker.launch(arrayOf("text/plain")) }
                            )
                            PushButton(
                                text = "推送",
                                onClick = {
                                    if(!isConnected)return@PushButton
                                    val intent = Intent(this@MainActivity, PushActivity::class.java)
                                        .setData(fileuri)
                                    startActivity(intent)
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))  // 把剩余空间都顶到这里

                    // 应用版本号
                    Text(
                        text = "v${versionName}",
                        color = colorResource(R.color.text_sub),
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 10.dp)
                    )

                    Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                }
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    @Composable
    private fun DeviceCard() {
        var imgSrc by remember { mutableIntStateOf(R.drawable.conn_false) }
        var connState by remember { mutableStateOf("手环连接中") }
        var connDesc by remember { mutableStateOf("请确保小米运动健康后台运行") }
        val scope = rememberCoroutineScope()
        suspend fun initConn(){
                try{
                    conn.destroy().await()
                    val deviceName=conn.connect().await().replace(" ","")
                    conn.auth()
                    try {
                        if(!conn.getAppState().await()){
                            connState = "喵喵电子书未安装"
                            connDesc = "请在手环上安装喵喵电子书"
                            imgSrc = R.drawable.conn_question
                            return
                        }
                    } catch (e: Exception) {
                        connState = "喵喵电子书未安装"
                        connDesc = "请在手环上安装喵喵电子书"
                        imgSrc = R.drawable.conn_question
                        return
                    }
                    conn.openApp()
                    connState = "设备连接成功"
                    connDesc = "$deviceName 已连接"
                    imgSrc = R.drawable.conn_true
                    isConnected=true
                }catch (e:Exception){
                    Log.e("init", "connect fail ${e.message}")
                    imgSrc = R.drawable.conn_false
                    connState = "手环连接失败"
                    connDesc=e.message?:"未知错误"
                    isConnected=false
                }

        }
        EbookCard(
            img = {
                Image(
                    painterResource(imgSrc),
                    modifier = it,
                    contentDescription = null
                )
            },
            title = connState,
            description = connDesc,
            onclick = {
                GlobalScope.launch { initConn() }
            }
        )
        LaunchedEffect(Unit) {
            initConn()
            updateDevice={
                scope.launch {
                    initConn()
                }
            }
        }

    }
    @Composable
    private fun FileSelectCard(
        callback: (Boolean) -> Unit
    ){
        var imgSrc by remember { mutableIntStateOf(R.drawable.addfile) }
        var filename by remember { mutableStateOf("选取文本文件") }
        var fileDesc by remember { mutableStateOf("请选取txt文件") }
        fileHandler= fileHandler@{ uri->
            if(uri==null){
                if (!isFileSelected)return@fileHandler
                filename="选取文本文件"
                fileDesc="请选取txt文件"
                imgSrc=R.drawable.addfile
                callback(false)
                return@fileHandler
            }
            fileuri=uri
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    filename = it.getString(max(it.getColumnIndex(OpenableColumns.DISPLAY_NAME),0))
                    imgSrc=R.drawable.file
                    //文件大小
                    val size =it.getLong(max(it.getColumnIndex(OpenableColumns.SIZE),0))
                    fileDesc = size.let { size1 ->
                        if (size1 != (-1).toLong()) {
                            bytesToReadable(size1)
                        } else {
                            "未知大小"
                        }
                    }
                    callback(true)
                }
            }
        }
        EbookCard(
            img = {
                Image(
                    painterResource(imgSrc),
                    modifier = it
                        .clip(RoundedCornerShape(100))
                        .background(Color.White),
                    contentDescription = null
                )
            },
            title = filename,
            description = fileDesc,
            onclick = {
                if (isFileSelected) {
                    Toast.makeText(this, FileUtils.getRealPath(this, fileuri), Toast.LENGTH_SHORT).show()
                } else {
                    filePicker.launch(arrayOf("text/plain"))
                }
            }
        )
        LaunchedEffect(Unit) {
            intent.data?.let {
                fileHandler(it)
            }
        }
    }
}


