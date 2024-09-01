package com.jayee.erabuana_apps

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.jayee.erabuana_apps.helpers.AuthenticationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class MyTransactionDetailActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AuthenticationHelper.authenticate(this)

        val transactionId = intent.getStringExtra("transaction_id").toString()
        setContent {
            MyTransactionDetailScreen(transactionId) { finish() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTransactionDetailScreen(transactionId: String, onBack: () -> Unit) {
    var transactionDetails by remember { mutableStateOf<TransactionDetails?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    LaunchedEffect(transactionId) {
        fetchTransactionDetails(context, transactionId) { details, fetchError ->
            transactionDetails = details
            error = fetchError
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Transaksi") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_left),
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when {
                loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                error != null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = error ?: "An unknown error occurred.", color = MaterialTheme.colorScheme.error)
                }
                else -> transactionDetails?.let { details ->
                    TransactionDetailsView(details)
                } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "No transaction details available.")
                }
            }
        }
    }
}

@Composable
fun TransactionDetailsView(details: TransactionDetails) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        item { TransactionDetailItem(label = "ID Transaksi", value = details.transactionId) }
        item { TransactionDetailItem(label = "Nama Pembeli", value = details.buyerName) }
        item { TransactionDetailItem(label = "Alamat Pembeli", value = details.buyerAddress) }
        item { TransactionDetailItem(label = "No.Telp Pembeli", value = details.buyerPhone) }
        item { TransactionDetailItem(label = "Email Pembeli", value = details.buyerEmail) }
        item { TransactionDetailItem(label = "Harga Jual", value = formatCurrency(details.salePrice)) }
        item { TransactionDetailItem(label = "Metode Pembayaran", value = details.paymentMethod) }
        item { TransactionDetailItem(label = "Uang Tanda Jadi (DP)", value = formatCurrency(details.downPayment)) }
        item { TransactionDetailItem(label = "Rencana Tanggal Pembayaran Uang Muka", value = formatDateWithoutTime(details.downPaymentDate)) }
        item { TransactionDetailItem(label = "Rencana Tanggal Pelunasan", value = formatDateWithoutTime(details.settlementDate)) }
        item { TransactionDetailItem(label = "ID Properti", value = details.propertyId) }
        item { TransactionDetailItem(label = "Status", value = mapStatus(details.status), color = getStatusColor(details.status)) }
        details.approvedAt?.let {
            item { TransactionDetailItem(label = "Disetujui pada", value = formatDate(it)) }
        }
    }
}

@Composable
fun TransactionDetailItem(label: String, value: String?, color: Color = MaterialTheme.colorScheme.onSurface) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value ?: "-", fontSize = 16.sp, color = color)
    }
}

data class TransactionDetails(
    val transactionId: String,
    val buyerName: String,
    val buyerAddress: String,
    val buyerPhone: String,
    val buyerEmail: String,
    val buyerIdCard: String,
    val salePrice: String,
    val paymentMethod: String,
    val downPayment: String,
    val downPaymentDate: String?,
    val settlementDate: String?,
    val propertyId: String,
    val salesId: Int,
    val status: String,
    val approvedAt: String?,
    val createdAt: String,
    val updatedAt: String
)

fun fetchTransactionDetails(context: Context, transactionId: String, callback: (TransactionDetails?, String?) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val token = sharedPreferences.getString("authToken", null)

            if (token.isNullOrEmpty()) {
                withContext(Dispatchers.Main) {
                    callback(null, "Auth token is missing.")
                }
                return@launch
            }

            val url = URL("https://erabuana.jayee.dev/api/sales/transaction?transaction_id=$transactionId")
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

                val jsonObject = JSONObject(response)
                if (jsonObject.getString("status") == "success") {
                    val data = jsonObject.getJSONObject("data")
                    val details = TransactionDetails(
                        transactionId = data.getString("transaction_id"),
                        buyerName = data.optString("buyer_name", "-"),
                        buyerAddress = data.optString("buyer_address", "-"),
                        buyerPhone = data.optString("buyer_phone", "-"),
                        buyerEmail = data.optString("buyer_email", "-"),
                        buyerIdCard = data.optString("buyer_id_card", "-"),
                        salePrice = data.getString("sale_price"),
                        paymentMethod = data.optString("payment_method", "-"),
                        downPayment = data.getString("down_payment"),
                        downPaymentDate = data.optString("down_payment_date").takeIf { it.isNotBlank() },
                        settlementDate = data.optString("settlement_date").takeIf { it.isNotBlank() },
                        propertyId = data.getString("property_id"),
                        salesId = data.getInt("sales_id"),
                        status = data.getString("status"),
                        approvedAt = data.optString("approved_at").takeIf { it.isNotBlank() },
                        createdAt = data.getString("created_at"),
                        updatedAt = data.getString("updated_at")
                    )
                    withContext(Dispatchers.Main) {
                        callback(details, null)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        callback(null, jsonObject.getString("message"))
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    callback(null, "Failed to fetch transaction details: HTTP $responseCode")
                }
            }
        } catch (e: Exception) {
            Log.e("FetchTransactionDetails", "Error fetching transaction details", e)
            withContext(Dispatchers.Main) {
                callback(null, "Error fetching transaction details: ${e.message}")
            }
        }
    }
}

fun formatDate(dateString: String?): String {
    if (dateString.isNullOrEmpty()) return "-"
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: "")
    } catch (e: Exception) {
        Log.e("DateFormat", "Error formatting date", e)
        "-"
    }
}

fun formatDateWithoutTime(dateString: String?): String {
    if (dateString.isNullOrEmpty()) return "-"
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: "")
    } catch (e: Exception) {
        Log.e("DateFormat", "Error formatting date", e)
        "-"
    }
}

fun mapStatus(status: String): String {
    return when (status.lowercase()) {
        "pending" -> "Pending"
        "rejected" -> "Ditolak"
        "approved" -> "Disetujui"
        else -> status.capitalize()
    }
}

fun getStatusColor(status: String): Color {
    return when (status.lowercase()) {
        "pending" -> Color(0xFFFFC107) // Yellow for Pending
        "rejected" -> Color(0xFFDC3545) // Red for Rejected
        "approved" -> Color(0xFF28A745) // Green for Approved
        else -> Color(0xFF343A40) // Default color
    }
}

fun formatCurrency(amount: String): String {
    return try {
        val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        numberFormat.format(amount.toDouble())
    } catch (e: Exception) {
        Log.e("CurrencyFormat", "Error formatting currency", e)
        amount
    }
}