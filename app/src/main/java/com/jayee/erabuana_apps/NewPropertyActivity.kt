package com.jayee.erabuana_apps

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class NewPropertyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AuthenticationHelper.authenticate(this)

        setContent {
            ErabuanaappsTheme {
                NewPropertyScreen { finish() }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewPropertyScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Form states
    var noUnit by remember { mutableStateOf("") }
    var streetAddress by remember { mutableStateOf("") }
    var zipCode by remember { mutableStateOf("") }
    var province by remember { mutableStateOf("") }
    var provinceId by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var cityId by remember { mutableStateOf("") }
    var landArea by remember { mutableStateOf("") }
    var buildingArea by remember { mutableStateOf("") }
    var numberOfFloors by remember { mutableStateOf("") }
    var bedrooms by remember { mutableStateOf("") }
    var bathrooms by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var provinces by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var cities by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var expandedProvince by remember { mutableStateOf(false) }
    var expandedCity by remember { mutableStateOf(false) }

    // Error states for property fields
    var noUnitError by remember { mutableStateOf("") }
    var streetAddressError by remember { mutableStateOf("") }
    var zipCodeError by remember { mutableStateOf("") }
    var provinceError by remember { mutableStateOf("") }
    var cityError by remember { mutableStateOf("") }
    var landAreaError by remember { mutableStateOf("") }
    var buildingAreaError by remember { mutableStateOf("") }
    var numberOfFloorsError by remember { mutableStateOf("") }
    var bedroomsError by remember { mutableStateOf("") }
    var bathroomsError by remember { mutableStateOf("") }
    var notesError by remember { mutableStateOf("") }

    // Fetch provinces when the screen is first launched
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            fetchProvinces(context) { fetchedProvinces ->
                provinces = fetchedProvinces
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tambahkan properti baru") },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Form fields with validation error messages
            item {
                OutlinedTextField(
                    value = noUnit,
                    onValueChange = { noUnit = it },
                    label = { Text("No Unit") },
                    isError = noUnitError.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (noUnitError.isNotEmpty()) {
                    Text(
                        text = noUnitError.trim('"'),
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            item {
                OutlinedTextField(
                    value = streetAddress,
                    onValueChange = { streetAddress = it },
                    label = { Text("Alamat") },
                    isError = streetAddressError.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (streetAddressError.isNotEmpty()) {
                    Text(
                        text = streetAddressError.trim('"'),
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            item {
                OutlinedTextField(
                    value = zipCode,
                    onValueChange = { zipCode = it },
                    label = { Text("Kode Pos") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = zipCodeError.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (zipCodeError.isNotEmpty()) {
                    Text(
                        text = zipCodeError.trim('"'),
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Province Selection
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    ExposedDropdownMenuBox(
                        expanded = expandedProvince,
                        onExpandedChange = {
                            expandedProvince = !expandedProvince
                            keyboardController?.hide()
                        }
                    ) {
                        OutlinedTextField(
                            value = province,
                            onValueChange = {},
                            label = { Text("Provinsi") },
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProvince) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            isError = provinceError.isNotEmpty()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedProvince,
                            onDismissRequest = { expandedProvince = false }
                        ) {
                            provinces.forEach { (provinceName, id) ->
                                DropdownMenuItem(
                                    text = { Text(provinceName) },
                                    onClick = {
                                        province = provinceName
                                        provinceId = id
                                        expandedProvince = false
                                        city = ""
                                        cityId = ""
                                        cities = emptyList()
                                        coroutineScope.launch {
                                            fetchCities(context, provinceId) { fetchedCities ->
                                                cities = fetchedCities
                                                if (fetchedCities.isEmpty()) {
                                                    Toast.makeText(context, "No cities available", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                    if (provinceError.isNotEmpty()) {
                        Text(
                            text = provinceError.trim('"'),
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // City Selection
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    ExposedDropdownMenuBox(
                        expanded = expandedCity,
                        onExpandedChange = {
                            expandedCity = !expandedCity
                            keyboardController?.hide()
                        }
                    ) {
                        OutlinedTextField(
                            value = city,
                            onValueChange = {},
                            label = { Text("Kota") },
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCity) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            isError = cityError.isNotEmpty()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedCity,
                            onDismissRequest = { expandedCity = false }
                        ) {
                            cities.forEach { (cityName, id) ->
                                DropdownMenuItem(
                                    text = { Text(cityName) },
                                    onClick = {
                                        city = cityName
                                        cityId = id
                                        expandedCity = false
                                    }
                                )
                            }
                        }
                    }
                    if (cityError.isNotEmpty()) {
                        Text(
                            text = cityError.trim('"'),
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Other form fields
            item {
                OutlinedTextField(
                    value = landArea,
                    onValueChange = { landArea = it },
                    label = { Text("Luas Tanah (m²)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = landAreaError.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (landAreaError.isNotEmpty()) {
                    Text(
                        text = landAreaError.trim('"'),
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            item {
                OutlinedTextField(
                    value = buildingArea,
                    onValueChange = { buildingArea = it },
                    label = { Text("Luas Bangunan (m²)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = buildingAreaError.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (buildingAreaError.isNotEmpty()) {
                    Text(
                        text = buildingAreaError.trim('"'),
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            item {
                OutlinedTextField(
                    value = numberOfFloors,
                    onValueChange = { numberOfFloors = it },
                    label = { Text("Jumlah Lantai") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = numberOfFloorsError.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (numberOfFloorsError.isNotEmpty()) {
                    Text(
                        text = numberOfFloorsError.trim('"'),
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            item {
                OutlinedTextField(
                    value = bedrooms,
                    onValueChange = { bedrooms = it },
                    label = { Text("Kamar Tidur") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = bedroomsError.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (bedroomsError.isNotEmpty()) {
                    Text(
                        text = bedroomsError.trim('"'),
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            item {
                OutlinedTextField(
                    value = bathrooms,
                    onValueChange = { bathrooms = it },
                    label = { Text("Kamar Mandi") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = bathroomsError.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (bathroomsError.isNotEmpty()) {
                    Text(
                        text = bathroomsError.trim('"'),
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            item {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    maxLines = 4,
                    isError = notesError.isNotEmpty()
                )
                if (notesError.isNotEmpty()) {
                    Text(
                        text = notesError.trim('"'),
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
                            registerProperty(
                                context = context,
                                noUnit = noUnit,
                                streetAddress = streetAddress,
                                zipCode = zipCode,
                                provinceId = provinceId,
                                cityId = cityId,
                                landArea = landArea,
                                buildingArea = buildingArea,
                                numberOfFloors = numberOfFloors,
                                bedrooms = bedrooms,
                                bathrooms = bathrooms,
                                notes = notes,
                                onSuccess = { onBack() },
                                onValidationError = { errors ->
                                    noUnitError = errors.optJSONArray("no-unit")?.join(", ") ?: ""
                                    streetAddressError = errors.optJSONArray("street-address")?.join(", ") ?: ""
                                    zipCodeError = errors.optJSONArray("zip-code")?.join(", ") ?: ""
                                    provinceError = errors.optJSONArray("province")?.join(", ") ?: ""
                                    cityError = errors.optJSONArray("city")?.join(", ") ?: ""
                                    landAreaError = errors.optJSONArray("land-area")?.join(", ") ?: ""
                                    buildingAreaError = errors.optJSONArray("building-area")?.join(", ") ?: ""
                                    numberOfFloorsError = errors.optJSONArray("number-of-floors")?.join(", ") ?: ""
                                    bedroomsError = errors.optJSONArray("bedrooms")?.join(", ") ?: ""
                                    bathroomsError = errors.optJSONArray("bathrooms")?.join(", ") ?: ""
                                    notesError = errors.optJSONArray("notes")?.join(", ") ?: ""
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007bff)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                ) {
                    Text("Register Property", color = Color.White)
                }
            }
        }
    }
}

suspend fun fetchProvinces(context: Context, onResult: (List<Pair<String, String>>) -> Unit) {
    withContext(Dispatchers.IO) {
        try {
            val url = URL("https://erabuana.jayee.dev/api/sales/provinces")
            val connection = url.openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = "GET"
                setRequestProperty("Content-Type", "application/json")
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = connection.inputStream
                val reader = BufferedReader(InputStreamReader(inputStream))
                val response = reader.readText()
                reader.close()

                val jsonObject = JSONObject(response)
                if (jsonObject.getBoolean("success")) {
                    val dataArray = jsonObject.getJSONArray("data")
                    val provinces = List(dataArray.length()) { i ->
                        val provinceObject = dataArray.getJSONObject(i)
                        provinceObject.getString("name") to provinceObject.getString("id")
                    }
                    withContext(Dispatchers.Main) {
                        onResult(provinces)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onResult(emptyList()) // Handle the else case
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    onResult(emptyList()) // Handle failed response
                }
            }
        } catch (e: Exception) {
            Log.e("FetchProvinces", "Error fetching provinces", e)
            withContext(Dispatchers.Main) {
                onResult(emptyList()) // Handle exceptions
            }
        }
    }
}

suspend fun fetchCities(context: Context, provinceId: String, onResult: (List<Pair<String, String>>) -> Unit) {
    withContext(Dispatchers.IO) {
        try {
            val url = URL("https://erabuana.jayee.dev/api/sales/cities?province_id=$provinceId")
            val connection = url.openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = "GET"
                setRequestProperty("Content-Type", "application/json")
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = connection.inputStream
                val reader = BufferedReader(InputStreamReader(inputStream))
                val response = reader.readText()
                reader.close()

                val jsonObject = JSONObject(response)
                if (jsonObject.getBoolean("success")) {
                    val dataArray = jsonObject.getJSONArray("data")
                    val cities = List(dataArray.length()) { i ->
                        val cityObject = dataArray.getJSONObject(i)
                        cityObject.getString("name") to cityObject.getString("id")
                    }
                    withContext(Dispatchers.Main) {
                        onResult(cities)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onResult(emptyList()) // Handle the else case
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    onResult(emptyList()) // Handle failed response
                }
            }
        } catch (e: Exception) {
            Log.e("FetchCities", "Error fetching cities", e)
            withContext(Dispatchers.Main) {
                onResult(emptyList()) // Handle exceptions
            }
        }
    }
}

suspend fun registerProperty(
    context: Context,
    noUnit: String,
    streetAddress: String,
    zipCode: String,
    provinceId: String,
    cityId: String,
    landArea: String,
    buildingArea: String,
    numberOfFloors: String,
    bedrooms: String,
    bathrooms: String,
    notes: String,
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

            val url = URL("https://erabuana.jayee.dev/api/sales/property-registration/register-property")
            val connection = url.openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Token", token)
                doOutput = true
                outputStream.write(
                    JSONObject().apply {
                        put("no-unit", noUnit)
                        put("street-address", streetAddress)
                        put("zip-code", zipCode)
                        put("province", provinceId)
                        put("city", cityId)
                        put("land-area", landArea.toFloatOrNull() ?: 0.0f)
                        put("building-area", buildingArea.toFloatOrNull() ?: 0.0f)
                        put("number-of-floors", numberOfFloors.toIntOrNull() ?: 0)
                        put("bedrooms", bedrooms.toIntOrNull() ?: 0)
                        put("bathrooms", bathrooms.toIntOrNull() ?: 0)
                        put("notes", notes)
                    }.toString().toByteArray()
                )
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Transaction registered successfully.", Toast.LENGTH_SHORT).show()
                    onSuccess()
                }
            } else if (responseCode == 422) { // Handling validation errors
                val errorStream = connection.errorStream
                val errorMessage = errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                val errorJson = JSONObject(errorMessage)
                val validationErrors = errorJson.optJSONObject("errors")

                withContext(Dispatchers.Main) {
                    if (validationErrors != null) {
                        onValidationError(validationErrors)
                    } else {
                        Toast.makeText(context, errorJson.optString("message", "Failed to register property."), Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                val errorStream = connection.errorStream
                val errorMessage = errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to register property: $errorMessage", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error registering property: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}