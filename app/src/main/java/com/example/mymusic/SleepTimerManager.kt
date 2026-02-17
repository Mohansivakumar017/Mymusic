package com.example.mymusic

import android.os.CountDownTimer

class SleepTimerManager {
    private var countDownTimer: CountDownTimer? = null
    private var onTimerTick: ((Long) -> Unit)? = null
    private var onTimerFinish: (() -> Unit)? = null
    
    fun startTimer(durationMinutes: Int, onTick: (Long) -> Unit, onFinish: () -> Unit) {
        cancelTimer()
        
        this.onTimerTick = onTick
        this.onTimerFinish = onFinish
        
        val durationMillis = durationMinutes * 60 * 1000L
        
        countDownTimer = object : CountDownTimer(durationMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                onTimerTick?.invoke(millisUntilFinished)
            }
            
            override fun onFinish() {
                onTimerFinish?.invoke()
                countDownTimer = null
            }
        }.start()
    }
    
    fun cancelTimer() {
        countDownTimer?.cancel()
        countDownTimer = null
    }
    
    fun isTimerActive(): Boolean {
        return countDownTimer != null
    }
}
