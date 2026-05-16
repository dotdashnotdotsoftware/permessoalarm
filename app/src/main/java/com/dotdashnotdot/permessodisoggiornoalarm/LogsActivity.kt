package com.dotdashnotdot.permessodisoggiornoalarm

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dotdashnotdot.permessodisoggiornoalarm.ui.theme.PermessoDiSoggiornoAlarmTheme

class LogsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PermessoDiSoggiornoAlarmTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LogsScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun LogsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var logs by remember { mutableStateOf("") }
    val noLogsMsg = stringResource(R.string.msg_no_logs)

    LaunchedEffect(Unit) {
        logs = LogHelper.getLogs(context)
        if (logs.isEmpty()) logs = noLogsMsg
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.title_app_logs),
                style = MaterialTheme.typography.headlineSmall
            )
            Button(onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("App Logs", logs)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, context.getString(R.string.msg_logs_copied), Toast.LENGTH_SHORT).show()
            }) {
                Text(stringResource(R.string.btn_copy))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.medium
        ) {
            Box(modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(8.dp)
            ) {
                Text(
                    text = logs,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TextButton(
            onClick = {
                LogHelper.clearLogs(context)
                logs = noLogsMsg
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Text(stringResource(R.string.btn_clear_logs))
        }
    }
}
