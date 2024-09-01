package com.jayee.erabuana_apps.widgets

import android.annotation.SuppressLint
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.NumberFormat
import java.util.Locale

@SuppressLint("DefaultLocale")
@Composable
fun TotalRevenueTodayWidget() {
    val context = LocalContext.current
    var loading by remember { mutableStateOf(true) }
    var revenueData by remember { mutableStateOf(RevenueData(emptyList(), emptyList(), 0.0)) }
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

                // Fetch today's revenue data
                revenueData = fetchRevenueData(token)
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to fetch revenue data"
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
                .padding(horizontal = 0.dp)
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "PENDAPATAN HARI INI",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFADB5BD),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    // Format revenue using NumberFormat
                    val formattedRevenue = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                        .format(revenueData.totalRevenues.sum())

                    Text(
                        text = formattedRevenue,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF343A40),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    if (revenueData.percentageChange != 0.0) {
                        val formattedPercentage = String.format(
                            "%+,.2f%%",
                            revenueData.percentageChange
                        ) // Adds "+" for positive and "-" for negative
                        Text(
                            text = formattedPercentage,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (revenueData.percentageChange > 0) Color.Green else Color.Red
                        )
                    }
                }
            }
        }
    }
}

data class RevenueData(
    val datetimes: List<String>,
    val totalRevenues: List<Double>,
    val percentageChange: Double
)

private fun getAuthToken(context: Context): String? {
    val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    return sharedPreferences.getString("authToken", null)
}

private suspend fun fetchRevenueData(token: String): RevenueData {
    return withContext(Dispatchers.IO) {
        val url = URL("https://erabuana.jayee.dev/api/sales/widgets/todays-revenue")
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
                    val data = jsonObject.getJSONObject("data")
                    RevenueData(
                        datetimes = data.getJSONArray("datetimes").let { jsonArray ->
                            List(jsonArray.length()) { jsonArray.getString(it) }
                        },
                        totalRevenues = data.getJSONArray("total_revenues").let { jsonArray ->
                            List(jsonArray.length()) { jsonArray.getDouble(it) }
                        },
                        percentageChange = data.getDouble("percentage_change")
                    )
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