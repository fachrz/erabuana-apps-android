package com.jayee.erabuana_apps

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jayee.erabuana_apps.helpers.AuthenticationHelper
import com.jayee.erabuana_apps.ui.theme.ErabuanaappsTheme
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AuthenticationHelper.authenticate(this)

        setContent {
            ErabuanaappsTheme {
                ProfileContent(onBackPressed = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileContent(onBackPressed: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var points by remember { mutableStateOf(0) }
    var profile by remember {
        mutableStateOf(
            Profile(
                fullName = "",
                email = "",
                profilePicture = R.drawable.default_profile // Replace with actual drawable
            )
        )
    }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                // Fetch the token from SharedPreferences
                val token = getAuthToken(context)
                if (!token.isNullOrEmpty()) {
                    val decodedToken = parseJwt(token)
                    profile = Profile(
                        fullName = decodedToken?.optString("full_name") ?: "",
                        email = decodedToken?.optString("user_email") ?: "",
                        profilePicture = R.drawable.default_profile // Fallback to default image
                    )
                }

                // Fetch points from the API
                points = fetchPointsFromApi(token)
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to load profile data", Toast.LENGTH_LONG).show()
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_left),
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding), // Include the inner padding
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            ProfileScreen(
                profile = profile,
                points = points,
                onLogout = { handleLogout(context) },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
fun ProfileScreen(profile: Profile, points: Int, onLogout: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color.White)
            .padding(all = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White) // Light background color
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Image(
                    painter = painterResource(id = profile.profilePicture),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = profile.fullName, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(text = profile.email, fontSize = 16.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center // Center the row's content
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_points), // Placeholder for points icon
                        contentDescription = "Points",
                        tint = Color(0xFFFFA500),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "$points Points", fontSize = 16.sp, color = Color.Gray)
                }
            }
        }

        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp), // Rectangular button with full width
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFdc3545)),
            shape = RoundedCornerShape(8.dp) // Rounded corners with 8.dp radius
        ) {
            Text(text = "Logout", color = Color.White)
        }
    }
}

private fun parseJwt(token: String): JSONObject? {
    return try {
        val parts = token.split(".")
        if (parts.size == 3) {
            val decoded = String(Base64.getDecoder().decode(parts[1]))
            JSONObject(decoded)
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}

private fun getAuthToken(context: Context): String? {
    val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    return sharedPreferences.getString("authToken", null)
}

private suspend fun fetchPointsFromApi(token: String?): Int {
    return withContext(Dispatchers.IO) {
        try {
            val url = URL("https://erabuana.jayee.dev/api/sales/points")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Token", token)
            connection.setRequestProperty("Content-Type", "application/json")
            connection.connect()

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(response)
                if (jsonResponse.optString("status") == "success") {
                    jsonResponse.getJSONObject("data").optInt("sales_point", 0)
                } else {
                    throw Exception(jsonResponse.optString("message", "Failed to retrieve points"))
                }
            } else {
                throw Exception("Failed to connect to API")
            }
        } catch (e: Exception) {
            0
        }
    }
}

private fun handleLogout(context: Context) {
    // Remove the token from SharedPreferences
    val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    sharedPreferences.edit().remove("authToken").apply()

    // Redirect to the login activity and clear the back stack
    val intent = Intent(context, LoginActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    context.startActivity(intent)
}

data class Profile(
    val fullName: String,
    val email: String,
    val profilePicture: Int
)