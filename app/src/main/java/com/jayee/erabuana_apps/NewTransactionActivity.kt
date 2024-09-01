package com.jayee.erabuana_apps

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.jayee.erabuana_apps.helpers.AuthenticationHelper
import com.jayee.erabuana_apps.ui.theme.ErabuanaappsTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class NewTransactionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AuthenticationHelper.authenticate(this)

        setContent {
            ErabuanaappsTheme {
                NewTransactionScreen { finish() }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTransactionScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Form states
    var buyerName by remember { mutableStateOf("") }
    var buyerAddress by remember { mutableStateOf("") }
    var buyerPhone by remember { mutableStateOf("") }
    var buyerEmail by remember { mutableStateOf("") }
    var buyerIdCard by remember { mutableStateOf("") }
    var salePrice by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("") }
    var downPayment by remember { mutableStateOf("") }
    var downPaymentDate by remember { mutableStateOf("") }
    var settlementDate by remember { mutableStateOf("") }
    var propertyId by remember { mutableStateOf("") }
    var expandedPaymentMethod by remember { mutableStateOf(false) }
    val paymentMethods = listOf("cash", "credit", "installment")

    // Error states
    var buyerNameError by remember { mutableStateOf("") }
    var buyerAddressError by remember { mutableStateOf("") }
    var buyerPhoneError by remember { mutableStateOf("") }
    var buyerEmailError by remember { mutableStateOf("") }
    var buyerIdCardError by remember { mutableStateOf("") }
    var salePriceError by remember { mutableStateOf("") }
    var paymentMethodError by remember { mutableStateOf("") }
    var downPaymentError by remember { mutableStateOf("") }
    var downPaymentDateError by remember { mutableStateOf("") }
    var settlementDateError by remember { mutableStateOf("") }
    var propertyIdError by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tambahkan transaksi baru") },
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
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp) // Proper spacing to prevent overlap
        ) {
            // Form fields with error handling
            item {
                OutlinedTextField(
                    value = buyerName,
                    onValueChange = { buyerName = it },
                    label = { Text("Nama Pembeli") },
                    isError = buyerNameError.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (buyerNameError.isNotEmpty()) {
                    Text(
                        text = buyerNameError.trim('"'), // Remove unnecessary quotes
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            item {
                OutlinedTextField(
                    value = buyerAddress,
                    onValueChange = { buyerAddress = it },
                    label = { Text("Alamat Pembeli") },
                    isError = buyerAddressError.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (buyerAddressError.isNotEmpty()) {
                    Text(
                        text = buyerAddressError.trim('"'), // Remove unnecessary quotes
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            item {
                OutlinedTextField(
                    value = buyerPhone,
                    onValueChange = { buyerPhone = it },
                    label = { Text("No. Telp Pembeli") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    isError = buyerPhoneError.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (buyerPhoneError.isNotEmpty()) {
                    Text(
                        text = buyerPhoneError.trim('"'), // Remove unnecessary quotes
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            item {
                OutlinedTextField(
                    value = buyerEmail,
                    onValueChange = { buyerEmail = it },
                    label = { Text("Email Pembeli") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isError = buyerEmailError.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (buyerEmailError.isNotEmpty()) {
                    Text(
                        text = buyerEmailError.trim('"'), // Remove unnecessary quotes
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            // KTP Pembeli Field
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = buyerIdCard,
                        onValueChange = { buyerIdCard = it },
                        label = { Text("KTP Pembeli") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        isError = buyerIdCardError.isNotEmpty()
                    )
                    if (buyerIdCardError.isNotEmpty()) {
                        Text(
                            text = buyerIdCardError.trim('"'), // Remove unnecessary quotes
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = salePrice,
                        onValueChange = { salePrice = it },
                        label = { Text("Harga Jual") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        isError = salePriceError.isNotEmpty()
                    )
                    if (salePriceError.isNotEmpty()) {
                        Text(
                            text = salePriceError.trim('"'), // Remove unnecessary quotes
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    ExposedDropdownMenuBox(
                        expanded = expandedPaymentMethod,
                        onExpandedChange = {
                            expandedPaymentMethod = !expandedPaymentMethod
                            keyboardController?.hide()
                        }
                    ) {
                        OutlinedTextField(
                            value = paymentMethod,
                            onValueChange = {},
                            label = { Text("Metode Pembayaran") },
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPaymentMethod) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            isError = paymentMethodError.isNotEmpty()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedPaymentMethod,
                            onDismissRequest = { expandedPaymentMethod = false }
                        ) {
                            paymentMethods.forEach { method ->
                                DropdownMenuItem(
                                    text = { Text(method) },
                                    onClick = {
                                        paymentMethod = method
                                        expandedPaymentMethod = false
                                    }
                                )
                            }
                        }
                    }
                    if (paymentMethodError.isNotEmpty()) {
                        Text(
                            text = paymentMethodError.trim('"'), // Remove unnecessary quotes
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Other form fields
            item {
                DatePickerField(
                    label = "Tanggal Pembayaran Uang Muka",
                    selectedDate = downPaymentDate,
                    onDateSelected = { downPaymentDate = it },
                    isError = downPaymentDateError.isNotEmpty()
                )
                if (downPaymentDateError.isNotEmpty()) {
                    Text(
                        text = downPaymentDateError.trim('"'), // Remove unnecessary quotes
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            item {
                DatePickerField(
                    label = "Tanggal Pelunasan",
                    selectedDate = settlementDate,
                    onDateSelected = { settlementDate = it },
                    isError = settlementDateError.isNotEmpty()
                )
                if (settlementDateError.isNotEmpty()) {
                    Text(
                        text = settlementDateError.trim('"'), // Remove unnecessary quotes
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            item {
                OutlinedTextField(
                    value = downPayment,
                    onValueChange = { downPayment = it },
                    label = { Text("Uang Muka") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = downPaymentError.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (downPaymentError.isNotEmpty()) {
                    Text(
                        text = downPaymentError.trim('"'), // Remove unnecessary quotes
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            item {
                OutlinedTextField(
                    value = propertyId,
                    onValueChange = { propertyId = it },
                    label = { Text("ID Properti") },
                    isError = propertyIdError.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (propertyIdError.isNotEmpty()) {
                    Text(
                        text = propertyIdError.trim('"'), // Remove unnecessary quotes
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Register button
            item {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            registerTransaction(
                                context = context,
                                buyerName = buyerName,
                                buyerAddress = buyerAddress,
                                buyerPhone = buyerPhone,
                                buyerEmail = buyerEmail,
                                buyerIdCard = buyerIdCard,
                                salePrice = salePrice,
                                paymentMethod = paymentMethod,
                                downPayment = downPayment,
                                downPaymentDate = downPaymentDate,
                                settlementDate = settlementDate,
                                propertyId = propertyId,
                                onSuccess = { onBack() },
                                onValidationError = { errors ->
                                    // Update error messages based on API response
                                    buyerNameError = errors.optJSONArray("nama_pembeli")?.join(", ") ?: ""
                                    buyerAddressError = errors.optJSONArray("alamat_pembeli")?.join(", ") ?: ""
                                    buyerPhoneError = errors.optJSONArray("telepon_pembeli")?.join(", ") ?: ""
                                    buyerEmailError = errors.optJSONArray("email_pembeli")?.join(", ") ?: ""
                                    buyerIdCardError = errors.optJSONArray("ktp_pembeli")?.join(", ") ?: ""
                                    salePriceError = errors.optJSONArray("harga_jual")?.join(", ") ?: ""
                                    paymentMethodError = errors.optJSONArray("metode_pembayaran")?.join(", ") ?: ""
                                    downPaymentError = errors.optJSONArray("uang_tanda_jadi")?.join(", ") ?: ""
                                    downPaymentDateError = errors.optJSONArray("tanggal_uang_muka")?.join(", ") ?: ""
                                    settlementDateError = errors.optJSONArray("tanggal_pelunasan")?.join(", ") ?: ""
                                    propertyIdError = errors.optJSONArray("property_id")?.join(", ") ?: ""
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007bff)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                ) {
                    Text("Buat Transaksi", color = Color.White)
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun DatePickerField(label: String, selectedDate: String, onDateSelected: (String) -> Unit, isError: Boolean) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val datePickerDialog = android.app.DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val date = String.format("%02d-%02d-%04d", dayOfMonth, month + 1, year)
            onDateSelected(date)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    OutlinedTextField(
        value = selectedDate,
        onValueChange = { },
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { datePickerDialog.show() },
        readOnly = true,
        isError = isError,
        trailingIcon = {
            IconButton(onClick = { datePickerDialog.show() }) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_today),
                    contentDescription = "Select Date"
                )
            }
        }
    )
}

suspend fun registerTransaction(
    context: Context,
    buyerName: String,
    buyerAddress: String,
    buyerPhone: String,
    buyerEmail: String,
    buyerIdCard: String,
    salePrice: String,
    paymentMethod: String,
    downPayment: String,
    downPaymentDate: String,
    settlementDate: String,
    propertyId: String,
    onSuccess: () -> Unit,
    onValidationError: (JSONObject) -> Unit
) {
    withContext(Dispatchers.IO) {
        try {
            val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val token = sharedPreferences.getString("authToken", null)

            if (token.isNullOrEmpty()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Auth token is missing.", Toast.LENGTH_SHORT).show()
                }
                return@withContext
            }

            val url = URL("https://erabuana.jayee.dev/api/sales/transactions")
            val connection = url.openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Token", token)
                doOutput = true
                outputStream.write(
                    JSONObject().apply {
                        put("nama_pembeli", buyerName)
                        put("alamat_pembeli", buyerAddress)
                        put("telepon_pembeli", buyerPhone)
                        put("email_pembeli", buyerEmail)
                        put("ktp_pembeli", buyerIdCard)
                        put("harga_jual", salePrice.toDoubleOrNull() ?: 0.0)
                        put("metode_pembayaran", paymentMethod)
                        put("uang_tanda_jadi", downPayment.toDoubleOrNull() ?: 0.0)
                        put("tanggal_uang_muka", downPaymentDate)
                        put("tanggal_pelunasan", settlementDate)
                        put("property_id", propertyId)
                    }.toString().toByteArray()
                )
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Transaction registered successfully.", Toast.LENGTH_SHORT).show()
                    onSuccess()
                }
            } else {
                val errorStream = connection.errorStream
                val errorMessage = errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                val errorJson = JSONObject(errorMessage)
                val validationErrors = errorJson.optJSONObject("errors")

                withContext(Dispatchers.Main) {
                    if (validationErrors != null) {
                        onValidationError(validationErrors)
                    } else {
                        Toast.makeText(context, errorJson.optString("message", "Failed to register transaction."), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error registering transaction: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}