package com.eyekage.feature.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eyekage.ui.theme.CameraIndicatorActive
import com.eyekage.ui.theme.DarkOutline
import com.eyekage.ui.theme.DarkSurfaceVariant
import com.eyekage.ui.theme.Typography

private val ShieldGreen = CameraIndicatorActive
private const val CARD_CORNER_RADIUS_DP = 16
private const val SHIELD_CONTAINER_SIZE_DP = 32
private const val SHIELD_ALPHA = 0.15f
private const val TEXT_ALPHA = 0.7f

@Composable
fun DataPrivacyDisclosureCard(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CARD_CORNER_RADIUS_DP.dp),
        color = DarkSurfaceVariant,
        border = BorderStroke(1.dp, DarkOutline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            DisclosureHeader()
            Spacer(modifier = Modifier.height(16.dp))
            DisclosureList()
        }
    }
}

@Composable
private fun DisclosureHeader() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(SHIELD_CONTAINER_SIZE_DP.dp)
                .background(ShieldGreen.copy(alpha = SHIELD_ALPHA), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "🛡", fontSize = 16.sp)
        }

        Column {
            Text(
                text = "Data & Privacy Transparency",
                style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Text(
                text = "Local & Private by Design",
                style = Typography.bodyMedium.copy(fontSize = 12.sp),
                color = ShieldGreen
            )
        }
    }
}

@Composable
private fun DisclosureList() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PrivacyBulletItem(
            title = "Zero Internet Access",
            description = "No network permission requested or declared. Operates 100% offline with zero cloud sync."
        )
        PrivacyBulletItem(
            title = "RAM-Only Vision Processing",
            description = "Camera frames are analyzed in transient RAM and discarded. Never saved to disk."
        )
        PrivacyBulletItem(
            title = "No Biometric Profiles",
            description = "Detects normalized face coordinates only. Zero identification or biometric profiles."
        )
        PrivacyBulletItem(
            title = "Strict 5-Key Local Storage",
            description = "Stores only 5 preferences: default eye mode, camera, reduced motion, haptics, desk mode."
        )
    }
}

@Composable
private fun PrivacyBulletItem(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "✓",
            color = ShieldGreen,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 1.dp)
        )
        Column {
            Text(
                text = title,
                style = Typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = Typography.bodyMedium.copy(fontSize = 12.sp, lineHeight = 16.sp),
                color = Color.White.copy(alpha = TEXT_ALPHA)
            )
        }
    }
}
