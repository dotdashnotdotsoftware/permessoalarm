package com.dotdashnotdot.permessodisoggiornoalarm

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dotdashnotdot.permessodisoggiornoalarm.ui.theme.PermessoDiSoggiornoAlarmTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.createNotificationChannel(this)
        enableEdgeToEdge()
        setContent {
            PermessoDiSoggiornoAlarmTheme {
                PermessoApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermessoApp(viewModel: PermessoViewModel = viewModel()) {
    val context = LocalContext.current
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showAlarmPermissionDialog by remember { mutableStateOf(false) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                showAlarmPermissionDialog = true
            }
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val name = result.data?.getStringExtra("name")
            val requestId = result.data?.getStringExtra("request_id")
            if (name != null && requestId != null) {
                viewModel.addItem(name, requestId)
            }
        }
    }

    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguage = viewModel.selectedLanguage,
            onLanguageSelected = {
                viewModel.setLanguage(it)
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false }
        )
    }

    if (showAlarmPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showAlarmPermissionDialog = false },
            title = { Text(stringResource(R.string.dialog_exact_alarm_title)) },
            text = { Text(stringResource(R.string.dialog_exact_alarm_text)) },
            confirmButton = {
                TextButton(onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }
                    showAlarmPermissionDialog = false
                }) {
                    Text(stringResource(R.string.btn_open_settings))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAlarmPermissionDialog = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { showLanguageDialog = true }) {
                        Icon(imageVector = Icons.Default.Language, contentDescription = stringResource(R.string.desc_language))
                    }
                    IconButton(onClick = {
                        val intent = Intent(context, LogsActivity::class.java)
                        context.startActivity(intent)
                    }) {
                        Icon(imageVector = Icons.Default.Description, contentDescription = stringResource(R.string.desc_logs))
                    }
                    IconButton(
                        onClick = {
                            val intent = Intent(context, AddActivity::class.java)
                            launcher.launch(intent)
                        },
                        enabled = viewModel.permessoItems.size < 5
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(R.string.desc_add))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.title_permesso_list),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
            }
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .navigationBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            val timeParts = viewModel.randomTime.split(":")
                            val initialHour = timeParts.getOrNull(0)?.toIntOrNull() ?: 12
                            val initialMinute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0
                            
                            TimePickerDialog(
                                context,
                                { _, hour, minute ->
                                    val formattedTime = "$hour:${minute.toString().padStart(2, '0')}"
                                    viewModel.updateRandomTime(formattedTime)
                                },
                                initialHour,
                                initialMinute,
                                true // 24 hour format
                            ).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(viewModel.randomTime)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (viewModel.permessoItems.isNotEmpty()) {
                                val lang = viewModel.getLangParam()
                                viewModel.permessoItems.forEach { item ->
                                    viewModel.checkStatus(item.requestId) { result ->
                                        NotificationHelper.showNotification(context, item.requestId, result, lang)
                                    }
                                }
                            } else {
                                Toast.makeText(context, context.getString(R.string.error_add_item), Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Sync, contentDescription = stringResource(R.string.desc_check_now))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val intent = Intent(context, InfoActivity::class.java)
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.btn_info))
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            items(viewModel.permessoItems) { item ->
                PermessoItemRow(
                    item = item,
                    onDelete = { viewModel.removeItem(item) },
                    onOpenBrowser = {
                        val lang = viewModel.getLangParam()
                        val url = "https://questure.poliziadistato.it/stranieri/?mime=1&lang=$lang&pratica=${item.requestId}"
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(browserIntent)
                    }
                )
            }
        }
    }
}

@Composable
fun LanguageSelectionDialog(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val languages = listOf(
        "Italiano", "English", "Español", "Français", "Русский", "український", "الْعَرَبيّة"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_select_language)) },
        text = {
            Column {
                languages.forEach { language ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLanguageSelected(language) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (language == currentLanguage),
                            onClick = { onLanguageSelected(language) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = language)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    )
}

@Composable
fun PermessoItemRow(item: PermessoItem, onDelete: () -> Unit, onOpenBrowser: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.name, style = MaterialTheme.typography.titleMedium)
            Text(text = item.requestId, style = MaterialTheme.typography.bodyMedium)
        }
        Row {
            IconButton(onClick = onOpenBrowser) {
                Icon(imageVector = Icons.Default.Public, contentDescription = stringResource(R.string.desc_open_browser))
            }
            IconButton(onClick = onDelete) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = stringResource(R.string.desc_delete))
            }
        }
    }
}
