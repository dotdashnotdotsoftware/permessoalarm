package com.dotdashnotdot.permessodisoggiornoalarm

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dotdashnotdot.permessodisoggiornoalarm.ui.theme.PermessoDiSoggiornoAlarmTheme

class ResultActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val requestId = intent.getStringExtra("request_id") ?: "Unknown"
        val resultText = intent.getStringExtra("result_text") ?: getString(R.string.text_no_data)
        val lang = intent.getStringExtra("lang") ?: "italian"
        
        setContent {
            PermessoDiSoggiornoAlarmTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ResultScreen(
                        requestId = requestId,
                        resultText = resultText,
                        lang = lang,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun ResultScreen(requestId: String, resultText: String, lang: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.header_request_id, requestId),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = {
                val url = "https://questure.poliziadistato.it/stranieri/?mime=1&lang=$lang&pratica=$requestId"
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(browserIntent)
            }) {
                Icon(imageVector = Icons.Default.Public, contentDescription = stringResource(R.string.desc_open_browser))
            }
        }
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Text(
                text = resultText,
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center,
                fontSize = 18.sp
            )
        }
    }
}
