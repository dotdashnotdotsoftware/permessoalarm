package com.example.permessodisoggiornoalarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class PermessoReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val msg = "Alarm received, triggering background check"
        LogHelper.log(context, msg)
        
        val workRequest = OneTimeWorkRequestBuilder<PermessoWorker>().build()
        WorkManager.getInstance(context).enqueue(workRequest)
        
        // Reschedule for tomorrow
        PermessoViewModel.scheduleDailyWork(context)
    }
}
