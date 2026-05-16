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
                        onContinue = { name, todo ->
                            val resultIntent = Intent().apply {
                                putExtra("name", name)
                                putExtra("todo_item", todo)
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
fun AddScreen(modifier: Modifier = Modifier, onContinue: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var todo by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = name,
            onValueChange = {
                if (it.length <= 20 && !it.contains("\n")) {
                    name = it
                }
            },
            label = { Text("Name (max 20 chars)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = todo,
            onValueChange = {
                if (it.length <= 20 && !it.contains("\n")) {
                    todo = it
                }
            },
            label = { Text("TODO item (max 20 chars)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { if (name.isNotBlank() && todo.isNotBlank()) onContinue(name, todo) },
            enabled = name.isNotBlank() && todo.isNotBlank()
        ) {
            Text("Continue")
        }
    }
}
