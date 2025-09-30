package com.example.composeauthapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay

// ---------- Validation ----------
// Email: local <=64, total <=254, domain labels with letters/digits/hyphens, TLD letters only.
private fun isValidEmail(raw: String): Boolean {
    val email = raw.trim()
    val pattern = Regex(
        "^(?=.{1,254}$)(?=.{1,64}@)[A-Za-z0-9._%+-]+@" +
                "(?:[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?\\.)+[A-Za-z]{2,63}$"
    )
    return pattern.matches(email)
}

// DOB format only (YYYY-MM-DD). You can tighten with a real date parser later.
private val dobRegex = Regex("^\\d{4}-\\d{2}-\\d{2}$")

// ---------- Persistence (SharedPreferences) ----------
private const val PREFS = "auth_prefs"
private const val KEY_EMAIL = "email"
private const val KEY_PASSWORD = "password"
private const val KEY_LOGGED_IN = "logged_in"

private fun saveUser(ctx: Context, email: String, password: String) {
    ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .edit()
        .putString(KEY_EMAIL, email.trim())
        .putString(KEY_PASSWORD, password)
        .apply()
}

private fun loadUser(ctx: Context): Pair<String?, String?> {
    val sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    return sp.getString(KEY_EMAIL, null) to sp.getString(KEY_PASSWORD, null)
}

private fun setLoggedIn(ctx: Context, value: Boolean) {
    ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .edit().putBoolean(KEY_LOGGED_IN, value).apply()
}

private fun isLoggedIn(ctx: Context): Boolean {
    return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .getBoolean(KEY_LOGGED_IN, false)
}

// ============================================================================

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}

// Root navigation graph
@Composable
fun App() {
    val nav = rememberNavController()
    MaterialTheme {
        NavHost(navController = nav, startDestination = "splash") {
            composable("splash")   { SplashScreen(nav) }
            composable("authGate") { AuthGate(nav) }
            composable("login")    { LoginScreen(nav) }
            composable("register") { RegisterScreen(nav) }
            composable("home")     { HomeScreen(nav) }
        }
    }
}

// ---------- Screens ----------

// Splash with picture + spinner; auto-route to Home if session active
@Composable
fun SplashScreen(nav: NavHostController) {
    val ctx = LocalContext.current
    LaunchedEffect(Unit) {
        delay(800)
        if (isLoggedIn(ctx)) {
            nav.navigate("home") { popUpTo("splash") { inclusive = true } }
        } else {
            nav.navigate("authGate") { popUpTo("splash") { inclusive = true } }
        }
    }
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier.size(96.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text("ComposeAuthApp", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            androidx.compose.material3.CircularProgressIndicator()
        }
    }
}

// Welcome gate
@Composable
fun AuthGate(nav: NavHostController) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Welcome!", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(24.dp))
            Button(onClick = { nav.navigate("login") }, modifier = Modifier.fillMaxWidth()) {
                Text("Log In")
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = { nav.navigate("register") }, modifier = Modifier.fillMaxWidth()) {
                Text("Register")
            }
        }
    }
}

// Login must match previously-registered credentials
@Composable
fun LoginScreen(nav: NavHostController) {
    val ctx = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AuthScaffold(title = "Login", onBack = { nav.popBackStack() }) {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            OutlinedTextField(
                value = email, onValueChange = { email = it },
                label = { Text("Email") }, singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password, onValueChange = { password = it },
                label = { Text("Password") }, singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    error = when {
                        email.isBlank() || password.isBlank() -> "All fields required"
                        !isValidEmail(email)                   -> "Enter a valid email"
                        else -> null
                    }
                    if (error == null) {
                        val (savedEmail, savedPass) = loadUser(ctx)
                        error = when {
                            savedEmail.isNullOrBlank() || savedPass.isNullOrBlank() ->
                                "No account found. Please register first."
                            email.trim() != savedEmail || password != savedPass ->
                                "Email or password incorrect."
                            else -> null
                        }
                        if (error == null) {
                            setLoggedIn(ctx, true)
                            nav.navigate("home") {
                                popUpTo("authGate") { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Log In") }

            error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

// Registration with validation + success dialog
@Composable
fun RegisterScreen(nav: NavHostController) {
    val ctx = LocalContext.current
    var first by remember { mutableStateOf("") }
    var last by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var showSuccess by remember { mutableStateOf(false) }

    AuthScaffold(title = "Register", onBack = { nav.popBackStack() }) {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            OutlinedTextField(first, { first = it }, label = { Text("First name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(last, { last = it }, label = { Text("Family name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(dob, { dob = it }, label = { Text("Date of birth (YYYY-MM-DD)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(email, { email = it }, label = { Text("Email") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = password, onValueChange = { password = it },
                label = { Text("Password") }, singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    error = when {
                        first.length !in 3..30        -> "First name must be 3–30 chars"
                        last.isBlank()                 -> "Family name required"
                        !dobRegex.matches(dob)         -> "DOB must be YYYY-MM-DD"
                        !isValidEmail(email)           -> "Enter a valid email"
                        password.isBlank()             -> "Password required"
                        else -> null
                    }
                    if (error == null) {
                        saveUser(ctx, email, password)
                        showSuccess = true
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Create Account") }

            error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }

        if (showSuccess) {
            AlertDialog(
                onDismissRequest = { showSuccess = false },
                title = { Text("Registration Successful") },
                text  = { Text("You can now log in using your email and password.") },
                confirmButton = {
                    TextButton(onClick = {
                        showSuccess = false
                        nav.popBackStack() // back to gate
                    }) { Text("OK") }
                }
            )
        }
    }
}

// Home after login, with email intent + logout
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(nav: NavHostController) {
    val ctx = LocalContext.current
    val (email, _) = loadUser(ctx)
    Scaffold(
        topBar = { TopAppBar(title = { Text("Home") }) }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Logged in as", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(6.dp))
            Text(email ?: "(unknown)", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(24.dp))

            // Intent demo: open email app to contact support
            OutlinedButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:")
                        putExtra(Intent.EXTRA_EMAIL, arrayOf("support@example.com"))
                        putExtra(Intent.EXTRA_SUBJECT, "ComposeAuthApp feedback")
                    }
                    ctx.startActivity(intent)
                }
            ) { Text("Contact support (email)") }

            Spacer(Modifier.height(12.dp))

            // Logout: clear session and return to gate
            OutlinedButton(
                onClick = {
                    setLoggedIn(ctx, false)
                    nav.navigate("authGate") {
                        popUpTo("authGate") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            ) { Text("Log out") }
        }
    }
}

// Shared scaffold used by Login/Register
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuthScaffold(
    title: String,
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}
