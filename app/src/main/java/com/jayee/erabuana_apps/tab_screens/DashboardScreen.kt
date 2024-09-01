package com.jayee.erabuana_apps.tab_screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.jayee.erabuana_apps.R
import com.jayee.erabuana_apps.ProfileActivity
import com.jayee.erabuana_apps.widgets.*

@Composable
fun DashboardScreen() {
    var profilePicture by remember { mutableStateOf(R.drawable.default_profile) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Overview", fontSize = 14.sp, color = Color(0xFFadb5bd))
                Text(text = "Dashboard", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
            Image(
                painter = painterResource(id = profilePicture),
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(45.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable { navigateToProfile(context) }
            )
        }

        // Dashboard Widgets
        Column(modifier = Modifier.fillMaxSize()) {
            TotalRevenueTodayWidget()
            TodaysTransactionsWidget()
            TopSalesPersonWidget()
            ActiveSalesPersonWidget()
        }
    }
}

private fun navigateToProfile(context: Context) {
    val intent = Intent(context, ProfileActivity::class.java)
    context.startActivity(intent)
}