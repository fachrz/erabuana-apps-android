package com.jayee.erabuana_apps.tab_screens

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.jayee.erabuana_apps.NewTransactionActivity
import com.jayee.erabuana_apps.MyTransactionDetailActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun MyTransactionsScreen() {
    val context = LocalContext.current
    var transactions by remember { mutableStateOf(listOf<Transaction>()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var currentPage by remember { mutableStateOf(1) }
    var canLoadMore by remember { mutableStateOf(true) }
    val listState = rememberLazyListState()
    var isRefreshing by remember { mutableStateOf(false) }

    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing)

    // Load initial transactions
    LaunchedEffect(Unit) {
        loadTransactions(context, currentPage) { fetchedTransactions, fetchError, canLoadMoreResponse ->
            if (fetchError != null) {
                error = fetchError
            } else {
                transactions = fetchedTransactions ?: emptyList()
                canLoadMore = canLoadMoreResponse
            }
            loading = false
        }
    }

    // Load more transactions when reaching the end of the list
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 }
            .collectLatest { lastVisibleItemIndex ->
                if (lastVisibleItemIndex == transactions.size - 1 && canLoadMore && !loading) {
                    loading = true
                    loadTransactions(context, currentPage + 1) { fetchedTransactions, fetchError, canLoadMoreResponse ->
                        if (fetchError != null) {
                            error = fetchError
                        } else {
                            transactions = transactions + (fetchedTransactions ?: emptyList())
                            canLoadMore = canLoadMoreResponse
                            currentPage += 1
                        }
                        loading = false
                    }
                }
            }
    }

    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = {
            isRefreshing = true
            loadTransactions(context, 1) { fetchedTransactions, fetchError, canLoadMoreResponse ->
                if (fetchError != null) {
                    error = fetchError
                } else {
                    transactions = fetchedTransactions ?: emptyList()
                    canLoadMore = canLoadMoreResponse
                    currentPage = 1
                }
                isRefreshing = false
            }
        }
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp, start = 20.dp, end = 20.dp, top = 10.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "My Transactions", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Button(
                        onClick = {
                            val intent = Intent(context, NewTransactionActivity::class.java)
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007bff))
                    ) {
                        Text("Tambahkan", color = Color.White)
                    }
                }
            }

            items(transactions) { transaction ->
                TransactionCard(transaction)
            }

            // Display loading at the bottom when loading more
            if (loading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionCard(transaction: Transaction) {
    val context = LocalContext.current

    // Determine the color based on the transaction status
    val statusColor = when (transaction.status.lowercase()) {
        "pending" -> Color(0xFFFFC107) // Yellow for Pending
        "ditolak" -> Color(0xFFDC3545) // Red for Rejected
        "disetujui" -> Color(0xFF28A745) // Green for Approved
        else -> Color(0xFF343A40) // Default color
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable {
                val intent = Intent(context, MyTransactionDetailActivity::class.java)
                intent.putExtra("transaction_id", transaction.transactionId)
                context.startActivity(intent)
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7)),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .background(Color(0xFFF7F7F7))
        ) {
            Text(
                text = "ID Transaksi:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF495057),
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = transaction.transactionId,
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF343A40),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Status:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF495057),
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = transaction.status,
                style = MaterialTheme.typography.bodyLarge,
                color = statusColor, // Apply color based on status
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Date: ${formatDateTransaction(transaction.date)}",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6C757D)
            )
        }
    }
}

data class Transaction(
    val transactionId: String,
    val status: String,
    val date: String
)

fun loadTransactions(
    context: Context,
    page: Int,
    callback: (List<Transaction>?, String?, Boolean) -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val sharedPreferences: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val token = sharedPreferences.getString("authToken", null)

            if (token.isNullOrEmpty()) {
                withContext(Dispatchers.Main) {
                    callback(null, "Auth token is missing.", false)
                }
                return@launch
            }

            val url = URL("https://erabuana.jayee.dev/api/sales/transactions?page=$page")
            val connection = url.openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = "GET"
                setRequestProperty("Token", token)
                setRequestProperty("Content-Type", "application/json")
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = connection.inputStream
                val reader = BufferedReader(InputStreamReader(inputStream))
                val response = reader.readText()
                reader.close()

                val transactions = parseTransactions(response)
                val jsonObject = JSONObject(response).getJSONObject("data")
                val canLoadMore = jsonObject.getInt("current_page") < jsonObject.getInt("last_page")

                withContext(Dispatchers.Main) {
                    callback(transactions, null, canLoadMore)
                }
            } else {
                withContext(Dispatchers.Main) {
                    callback(null, "Failed to fetch transactions: HTTP $responseCode", false)
                }
            }
        } catch (e: Exception) {
            Log.e("TransactionFetch", "Error fetching transactions", e)
            withContext(Dispatchers.Main) {
                callback(null, "Error fetching transactions: ${e.message}", false)
            }
        }
    }
}

fun parseTransactions(response: String): List<Transaction> {
    return try {
        val jsonObject = JSONObject(response)
        val dataObject = jsonObject.getJSONObject("data")
        val dataArray = dataObject.getJSONArray("data")
        List(dataArray.length()) { i ->
            val item = dataArray.getJSONObject(i)
            val mappedStatus = when (val originalStatus = item.getString("status").lowercase()) {
                "pending" -> "Pending"
                "rejected" -> "Ditolak"
                "approved" -> "Disetujui"
                else -> originalStatus
            }

            Transaction(
                transactionId = item.getString("transaction_id"),
                status = mappedStatus,
                date = item.getString("created_at")
            )
        }
    } catch (e: Exception) {
        Log.e("ParseTransactions", "Error parsing transactions", e)
        emptyList()
    }
}

fun formatDateTransaction(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: "")
    } catch (e: Exception) {
        Log.e("DateFormat", "Error formatting date", e)
        dateString
    }
}