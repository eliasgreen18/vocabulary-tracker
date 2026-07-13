package com.eliasgreen18.vocabularytracker.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eliasgreen18.vocabularytracker.R

@Composable
fun SplashScreen(versionName: String = "1.0.0") {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDFBF7)) // Exact Library Paper background
    ) {
        // 1. THE LOGO - Fixed at center with system-like scale
        // Using 240dp to match the "raw" vector scale of system splash icons
        Box(
            modifier = Modifier.align(Alignment.Center),
            contentAlignment = Alignment.Center
        ) {
            // Decorative background ring
            Surface(
                modifier = Modifier.size(280.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.015f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
            ) {}
            
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier.size(280.dp) // Increased from 260dp to 280dp to match system splash exactly
            )
        }

        // 2. BRANDING TEXT - Using Offset to ensure NO overlapping with the logo's layout box
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-170).dp), // Adjusted offset for larger logo
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "VOCABULARY",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                letterSpacing = 12.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
        
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 170.dp), // Adjusted offset for larger logo
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Tracker",
                style = MaterialTheme.typography.displayMedium,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = (-1).sp
            )
            
            Text(
                text = "Personal Reading Companion",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                fontFamily = FontFamily.Serif
            )
        }

        // 3. VERSION BAR: Absolute Bottom
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            color = MaterialTheme.colorScheme.primary,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Library Edition",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = stringResource(id = R.string.version_label, versionName),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
