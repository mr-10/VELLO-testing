package com.mr10.vello.ui.util

import android.content.Context
import android.media.MediaPlayer
import com.mr10.vello.R

object SoundHelper {
    private var mediaPlayer: MediaPlayer? = null

    fun playSound(context: Context, resId: Int, loop: Boolean = false) {
        stopSound()
        mediaPlayer = MediaPlayer.create(context, resId).apply {
            isLooping = loop
            start()
        }
    }

    fun stopSound() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun getNotificationSoundRes(index: Int): Int {
        return if (index == 1) R.raw.sound_2 else R.raw.sound_1
    }

    fun getRingtoneRes(index: Int): Int {
        return if (index == 1) R.raw.ringtone_2 else R.raw.ringtone_1
    }
    
    fun getDialingToneRes(): Int {
        return R.raw.dialing_tone
    }
}
