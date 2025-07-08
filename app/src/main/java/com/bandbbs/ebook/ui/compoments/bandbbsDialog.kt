package com.bandbbs.ebook.ui.compoments

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.bandbbs.ebook.R


@Composable
fun AboutDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    versionName:  String? = "null"
) {
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = colorResource(R.color.page_bg)
                    )
                    .padding(
                        top = 10.dp,
                        start = 10.dp,
                        end = 10.dp,
                        bottom = 0.dp
                    ),
            ) {
                Column (
                    Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Row (
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_launcher_foreground),
                            contentDescription = "喵喵电子书",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .width(50.dp)
                                .height(50.dp)
                                .scale(2.6f)
                        )

                        Column (
                            Modifier.padding(start = 16.dp)
                        ) {
                            Text(
                                text = "喵喵电子书同步器",
                                color = colorResource(R.color.text_main),
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp,
                            )

                            Text(
                                text = "$versionName",
                                color = colorResource(R.color.text_sub),
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                    }


                }


                Spacer(modifier = Modifier.weight(1f))


                Column (
                    Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Text(
                        text = "参与开发的人员",
                        color = colorResource(R.color.text_main),
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        modifier=Modifier.padding(top = 20.dp)
                    )

                    Text(
                        text = "NEORUAA\n乐色桶\n无源流沙",
                        color = colorResource(R.color.text_sub),
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(top = 10.dp)
                    )

                    Text(
                        text = "更多资源请访问",
                        color = colorResource(R.color.text_main),
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(top = 10.dp)
                    )

                    Text(
                        text = "bandbbs.cn",
                        color = colorResource(R.color.text_sub),
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }

                NormalButton(
                    modifier = Modifier.padding(top = 10.dp),
                    text = "关闭",
                    onClick = {
                        onConfirmation()
                    }
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewAboutDialog() {
    AboutDialog(
        onDismissRequest = { },
        onConfirmation = { },
        versionName = "1.0.0"
    )
}