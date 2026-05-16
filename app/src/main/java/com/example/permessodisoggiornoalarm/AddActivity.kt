package com.example.permessodisoggiornoalarm

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.permessodisoggiornoalarm.ui.theme.PermessoDiSoggiornoAlarmTheme

class AddActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PermessoDiSoggiornoAlarmTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AddScreen(
                        modifier = Modifier.padding(innerPadding),
                        onContinue = { text ->
                            val resultIntent = Intent().apply {
                                putExtra("todo_item", text)
                            }
                            setResult(Activity.RESULT_OK, resultIntent)
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AddScreen(modifier: Modifier = Modifier, onContinue: (String) -> Unit) {
    var text by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = text,
            onValueChange = {
                if (it.length <= 20 && !it.contains("\n")) {
                    text = it
                }
            },
            label = { Text("Enter TODO item (max 20 chars)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { if (text.isNotBlank()) onContinue(text) },
            enabled = text.isNotBlank()
        ) {
            Text("Continue")
        }
    }
}
