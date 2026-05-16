package com.dotdashnotdot.permessodisoggiornoalarm

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dotdashnotdot.permessodisoggiornoalarm.ui.theme.PermessoDiSoggiornoAlarmTheme

class InfoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PermessoDiSoggiornoAlarmTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    InfoScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun InfoScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = stringResource(R.string.title_info),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Text(
                text = stringResource(R.string.disclaimer_text),
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Start,
                fontSize = 16.sp,
                lineHeight = 24.sp
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = {
                val url = "https://paypal.me/dotnotdotsoftware"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.btn_donate))
        }
    }
}
