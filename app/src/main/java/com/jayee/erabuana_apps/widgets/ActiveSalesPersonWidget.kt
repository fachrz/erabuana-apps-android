package com.jayee.erabuana_apps.widgets

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jayee.erabuana_apps.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun ActiveSalesPersonWidget() {
    val context = LocalContext.current
    var loading by remember { mutableStateOf(true) }
    var totalActiveSalesPersons by remember { mutableStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                // Fetch token from shared preferences
                val token = getAuthToken(context)
                if (token.isNullOrEmpty()) {
                    throw Exception("Auth token is missing.")
                }

                // Fetch active sales persons count
                totalActiveSalesPersons = fetchActiveSalesPersons(token)
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to fetch active sales persons"
            } finally {
                loading = false
            }
        }
    }

    if (loading) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (errorMessage != null) {
        Text(text = errorMessage!!, color = Color.Red, modifier = Modifier.padding(16.dp))
    } else {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 0.dp), // Ensure full width by removing horizontal padding
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7)), // Correctly set the card background color
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "TOTAL AKTIF SALES PERSON",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFADB5BD),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_user_check), // Ensure this icon is set correctly
                        contentDescription = "Active Sales Persons",
                        tint = Color(0xFF495057), // Darker shade for icon
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$totalActiveSalesPersons",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF343A40) // Slightly darker shade for the number
                    )
                }
            }
        }
    }
}

private fun getAuthToken(context: Context): String? {
    val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    return sharedPreferences.getString("authToken", null)
}

private suspend fun fetchActiveSalesPersons(token: String): Int {
    return withContext(Dispatchers.IO) {
        val url = URL("https://erabuana.jayee.dev/api/sales/widgets/active-sales-persons")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("Token", token)
        connection.setRequestProperty("Content-Type", "application/json")

        try {
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = connection.inputStream
                val reader = BufferedReader(InputStreamReader(inputStream))
                val response = reader.readText()
                reader.close()

                val jsonObject = JSONObject(response)
                if (jsonObject.getString("status") == "success") {
                    jsonObject.getJSONObject("data").getInt("total_active_sales_persons")
                } else {
                    throw Exception(jsonObject.optString("message", "Error fetching data"))
                }
            } else {
                throw Exception("HTTP error code: $responseCode")
            }
        } finally {
            connection.disconnect()
        }
    }
}