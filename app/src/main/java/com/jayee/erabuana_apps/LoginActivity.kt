package com.jayee.erabuana_apps

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.jayee.erabuana_apps.ui.theme.ErabuanaappsTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class LoginActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ErabuanaappsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
                    LoginScreen(modifier = Modifier.padding(paddingValues))
                }
            }
        }
    }
}

@Composable
fun LoginScreen(modifier: Modifier = Modifier) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .verticalScroll(scrollState) // Makes the screen scrollable
                .imePadding(), // Adds padding to avoid the keyboard covering elements
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo), // Ensure the logo image exists in res/drawable
                contentDescription = "App Logo",
                modifier = Modifier
                    .width(200.dp)
                    .height(100.dp)
                    .padding(bottom = 20.dp)
            )

            Text(
                text = "Login to your account",
                fontSize = 24.sp,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            Button(
                onClick = {
                    keyboardController?.hide() // Hide the keyboard when login button is pressed
                    coroutineScope.launch {
                        loading = true
                        val success = handleLogin(context, email, password)
                        loading = false
                        if (success) {
                            // Navigate to TabsActivity after successful login
                            context.startActivity(Intent(context, TabsActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            })
                            (context as ComponentActivity).finish() // Close LoginActivity
                        } else {
                            Toast.makeText(
                                context,
                                "Login failed. Please check your credentials.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                enabled = !loading,
                shape = RoundedCornerShape(16.dp) // Rounded rectangle shape with 16dp corners
            ) {
                if (loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Login", fontSize = 16.sp)
                }
            }
        }
    }
}

suspend fun handleLogin(context: Context, email: String, password: String): Boolean {
    return withContext(Dispatchers.IO) { // Run the network request on the IO dispatcher
        val client = OkHttpClient()
        val json = JSONObject().apply {
            put("email", email)
            put("password", password)
        }
        val requestBody = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("https://erabuana.jayee.dev/api/sales/login")
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    false // Handle unsuccessful response
                } else {
                    val responseData = response.body?.string()
                    val jsonResponse = JSONObject(responseData ?: "")
                    if (jsonResponse.getString("status") == "success") {
                        val token = jsonResponse.getJSONObject("data").getString("token")
                        val sharedPreferences: SharedPreferences =
                            context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                        sharedPreferences.edit().putString("authToken", token).apply()
                        true // Successful login
                    } else {
                        false // Handle unsuccessful login status
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            false // Handle IOException
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    ErabuanaappsTheme {
        LoginScreen()
    }
}