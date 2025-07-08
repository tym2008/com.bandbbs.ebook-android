package com.bandbbs.ebook

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bandbbs.ebook.logic.InterHandshake
import com.bandbbs.ebook.logic.InterconnetFile
import com.bandbbs.ebook.ui.compoments.EbookScaffold
import com.bandbbs.ebook.ui.compoments.NormalButton
import com.bandbbs.ebook.ui.theme.EbookTheme
import com.bandbbs.ebook.utils.UritoFile
import com.bandbbs.ebook.utils.bytesToReadable

class PushActivity: ComponentActivity()  {
    private lateinit var conn: InterHandshake
    private lateinit var fileconn: InterconnetFile
    private var btnText = "取消"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        conn=(application as App).conn
        fileconn= InterconnetFile(conn)
        if(fileconn.busy){
            Toast.makeText(this,"文件推送中",Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        enableEdgeToEdge()
        setContent {
            PushActivityPage()
        }
    }
    override fun onStop() {
        super.onStop()
        if(fileconn.busy)fileconn.cancel()
        super.onDestroy()
    }
    @Preview
    @Composable
    private fun PushActivityPage() {
        EbookTheme {
            EbookScaffold(
                title = "推送中",
                onback = {
                    finish()
                }
            ){
                Push(it.fillMaxSize())
            }
        }
    }
    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    private fun Push(modifier: Modifier){
        var filename by remember { mutableStateOf(".txt") }
        var filesize by remember { mutableStateOf("0MB") }
        var progress by remember { mutableDoubleStateOf(0.0) }
        var chunkPreview by remember { mutableStateOf("...") }
        var speedText by remember { mutableStateOf("0") }
        var imgSrc by remember { mutableIntStateOf(R.drawable.uploading) }
        LaunchedEffect(Unit) {
            conn.init()

                intent.data?.let{
                    val file=UritoFile(uri = it,context = this@PushActivity)!!
                    filename=file.name
                    filesize = bytesToReadable(file.length())
                    fileconn.sentFile(
                        file = file,
                        onError = { error, count ->
                            runOnUiThread {
                                Toast.makeText(this@PushActivity, "文件上传失败,$error", Toast.LENGTH_SHORT).show()
                                imgSrc=R.drawable.fail
                                btnText = "取消"
                            }
                        },
                        onSuccess = { msg,count->
                            runOnUiThread {
                                Toast.makeText(this@PushActivity, "文件上传成功", Toast.LENGTH_SHORT).show()
                                imgSrc=R.drawable.success
                                btnText = "完成"
                            }

                        },
                        onProgress = { p,preview,speed->
                            progress=p
                            chunkPreview=preview
                            speedText=speed
                        },
                    )


            }
        }


        Column (
            modifier
                .fillMaxSize()
                .background(
                    color = colorResource(R.color.page_bg)
                )
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ){
            Image(
                modifier=Modifier
                    .padding(top = 40.dp)
                    .size(130.dp)
                    .clip(RoundedCornerShape(100))
                    .background(
                        color = colorResource(R.color.card_bg)
                    )
                    .padding(20.dp),
                painter = painterResource(id = imgSrc),
                contentDescription = "null"
            )
            Text(
                text = filename,
                modifier=Modifier.padding(top = 20.dp),
                color = colorResource(R.color.text_main),
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
            Text(
                text = filesize,
                color = colorResource(R.color.text_sub),
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            )
            Card(
                modifier=Modifier
                    .padding(top = 20.dp)
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = colorResource(R.color.text_prev_bg),
                ),
            ) {
                Text(
                    text = chunkPreview,
                    color = colorResource(R.color.text_sub),
                    modifier=Modifier
                        .padding(16.dp)
                        .weight(1f),
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Text(
                text = "正在推送 ${(progress*100).toInt()}%"+speedText,
                color = colorResource(R.color.title),
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                modifier=Modifier.padding(top = 16.dp)
            )

            LinearWavyProgressIndicator(
                progress = { progress.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                stopSize = 0.dp
            )

            NormalButton(
                modifier=Modifier
                    .padding(top = 10.dp),
                text = btnText,
                onClick = {
                    finish()
                }
            )

            Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }

    }
}