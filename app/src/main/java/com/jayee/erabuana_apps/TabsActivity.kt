package com.jayee.erabuana_apps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.Image
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.ColorFilter
import com.jayee.erabuana_apps.helpers.AuthenticationHelper
import com.jayee.erabuana_apps.ui.theme.ErabuanaappsTheme
import com.jayee.erabuana_apps.tab_screens.DashboardScreen
import com.jayee.erabuana_apps.tab_screens.MyPropertyScreen
import com.jayee.erabuana_apps.tab_screens.MyTransactionsScreen

class TabsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AuthenticationHelper.authenticate(this)

        setContent {
            ErabuanaappsTheme {
                MainTabs()
            }
        }
    }
}

@Composable
fun MainTabs() {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        BottomNavItem("Dashboard", R.drawable.ic_dashboard),
        BottomNavItem("My Property", R.drawable.ic_building_community),
        BottomNavItem("My Transactions", R.drawable.ic_file_certificate)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        when (selectedTabIndex) {
            0 -> DashboardScreen() // Ensure that this is a composable function
            1 -> MyPropertyScreen()
            2 -> MyTransactionsScreen()
        }
        CustomBottomNavigationBar(
            items = tabs,
            selectedTabIndex = selectedTabIndex,
            onTabSelected = { index ->
                selectedTabIndex = index
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun CustomBottomNavigationBar(
    items: List<BottomNavItem>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(70.dp)
            .background(Color(0xFFF8F8F8))
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, item ->
            val isSelected = selectedTabIndex == index
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onTabSelected(index) }
                    )
            ) {
                Image(
                    painter = painterResource(id = item.icon),
                    contentDescription = item.title,
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(if (isSelected) Color(0xFF2196F3) else Color.Gray),
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(50))
                        .padding(bottom = 4.dp)
                )
                Text(
                    text = item.title,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color(0xFF2196F3) else Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

data class BottomNavItem(val title: String, val icon: Int)