package com.jayee.erabuana_apps

import android.content.Context
import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
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

class MyPropertyDetailActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AuthenticationHelper.authenticate(this)

        val propertyId = intent.getStringExtra("property_id").toString()

        setContent {
            MyPropertyDetailScreen(propertyId) { finish() } // Pass finish() as back action
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPropertyDetailScreen(propertyId: String, onBack: () -> Unit) {
    var propertyDetails by remember { mutableStateOf<PropertyDetails?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    LaunchedEffect(propertyId) {
        fetchPropertyDetails(context, propertyId) { details, fetchError ->
            propertyDetails = details
            error = fetchError
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Properti") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_left), // Use your custom icon
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when {
                loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                error != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = error ?: "An unknown error occurred.", color = MaterialTheme.colorScheme.error)
                    }
                }
                else -> {
                    propertyDetails?.let { details ->
                        PropertyDetailsView(details)
                    } ?: run {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "No property details available.")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PropertyDetailsView(details: PropertyDetails) {
    val context = LocalContext.current // Retrieve context here

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        item {
            PropertyDetailItem(
                label = "ID Properti",
                value = details.propertyId,
                onCopy = { copyToClipboard(context, "ID Properti", details.propertyId) }
            )
        }
        item { PropertyDetailItem(label = "Properti Key", value = details.propertyKey) }
        item { PropertyDetailItem(label = "Nomor Unit", value = details.unitNumber) }
        item { PropertyDetailItem(label = "Alamat", value = details.streetAddress) }
        item { PropertyDetailItem(label = "Kota", value = details.city) }
        item { PropertyDetailItem(label = "Provinsi", value = details.province) }
        item { PropertyDetailItem(label = "Kode Pos", value = details.zipCode) }
        item { PropertyDetailItem(label = "Luas Tanah", value = "${details.landArea} m²") }
        item { PropertyDetailItem(label = "Luas Bangunan", value = "${details.buildingArea} m²") }
        item { PropertyDetailItem(label = "Jumlah Lantai", value = details.numberOfFloors.toString()) }
        item { PropertyDetailItem(label = "Jumlah Kamar Tidur", value = details.bedrooms.toString()) }
        item { PropertyDetailItem(label = "Jumlah Kamar Mandi", value = details.bathrooms.toString()) }
        item { PropertyDetailItem(label = "Didaftarkan pada", value = details.registeredAt) }
        item { PropertyDetailItem(label = "Catatan", value = details.notes ?: "Tidak ada catatan.") }
    }
}

@Composable
fun PropertyDetailItem(label: String, value: String, onCopy: (() -> Unit)? = null) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
            .padding(16.dp)
            .clickable { onCopy?.invoke() } // Allow clicking to copy if onCopy is provided
    ) {
        Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboardManager.setPrimaryClip(clip)
    Toast.makeText(context, "$label copied to clipboard.", Toast.LENGTH_SHORT).show()
}

data class PropertyDetails(
    val propertyId: String,
    val propertyKey: String,
    val unitNumber: String,
    val streetAddress: String,
    val city: String,
    val province: String,
    val zipCode: String,
    val landArea: Int,
    val buildingArea: Int,
    val numberOfFloors: Int,
    val bedrooms: Int,
    val bathrooms: Int,
    val registeredAt: String,
    val notes: String?
)

fun fetchPropertyDetails(context: Context, propertyId: String, callback: (PropertyDetails?, String?) -> Unit) {
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

            val url = URL("https://erabuana.jayee.dev/api/sales/property-registration/registered-property?property_id=$propertyId")
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
                    val details = PropertyDetails(
                        propertyId = data.getString("property_id"),
                        propertyKey = data.getString("property_key"),
                        unitNumber = data.getString("unit_number"),
                        streetAddress = data.getString("street_address"),
                        city = data.getString("city"),
                        province = data.getString("province"),
                        zipCode = data.getString("zip_code"),
                        landArea = data.getInt("land_area"),
                        buildingArea = data.getInt("building_area"),
                        numberOfFloors = data.getInt("number_of_floors"),
                        bedrooms = data.getInt("bedrooms"),
                        bathrooms = data.getInt("bathrooms"),
                        registeredAt = data.getString("created_at"),
                        notes = data.optString("notes", null)
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
                    callback(null, "Failed to fetch property details: HTTP $responseCode")
                }
            }
        } catch (e: Exception) {
            Log.e("FetchPropertyDetails", "Error fetching property details", e)
            withContext(Dispatchers.Main) {
                callback(null, "Error fetching property details: ${e.message}")
            }
        }
    }
}