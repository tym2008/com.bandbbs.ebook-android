package com.bandbbs.ebook.ui.compoments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bandbbs.ebook.R

@Composable
fun PushButton(
    modifier: Modifier=Modifier,
    text: String,
    onClick: () -> Unit={},
){
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF89ACFF), Color(0xFF5C89F1))
    )

    Box(
        modifier = modifier
            .padding(bottom = 10.dp)
            .height(54.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(gradientBrush)
    ) {
        Button(
            onClick = onClick,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .matchParentSize(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent
            )
        ) {
            Text(
                text = text,
                color = colorResource(R.color.btn_text_light),
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
        }
    }
}
@Composable
fun NormalButton(
    modifier: Modifier=Modifier,
    text: String,
    onClick: () -> Unit={},
){
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(R.color.card_bg),
        ),
        modifier = modifier
            .padding(bottom = 10.dp)
            .height(54.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = text,
            color = colorResource(R.color.text_main),
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp
        )
    }
}
@Preview
@Composable
fun PreviewPushButton() {
    PushButton(text = "Button", modifier = Modifier.fillMaxWidth().height(60.dp))
}