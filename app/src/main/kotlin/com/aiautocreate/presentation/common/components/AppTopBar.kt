package com.aiautocreate.presentation.common.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.aiautocreate.R
import com.aiautocreate.presentation.ui.theme.*

@Composable
fun AppTopBar(
    title: String,
    onMenuClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // ✅ سجل للتأكد من استدعاء الدالة
    Log.d("AppTopBar", "=== تم استدعاء AppTopBar بالعنوان: $title ===")

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(ComponentSize.topBarHeight)
                .background(BackgroundTopbar)
                .padding(horizontal = Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // زر القائمة (يظهر فقط إذا كان onMenuClick != null)
            if (onMenuClick != null) {
                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier.size(ComponentSize.iconButton)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_menu),
                        contentDescription = "القائمة",
                        modifier = Modifier.size(IconSize.md),
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(Spacing.sm))
            }

            // الشعار
            Icon(
                painter = painterResource(id = R.drawable.ic_logo_ai),
                contentDescription = "Logo",
                modifier = Modifier.size(IconSize.xl),
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.width(Spacing.sm))

            // العنوان (ديناميكي)
            Text(
                text = title,
                color = PrimaryLight,
                fontWeight = FontWeight.Bold,
                fontSize = AppFontSize.titleSmall
            )
        }

        // الخط الفاصل أسفل الهيدر
        HorizontalDivider(
            color = Color(0xFF172036),
            thickness = Border.thin
        )
    }
}
