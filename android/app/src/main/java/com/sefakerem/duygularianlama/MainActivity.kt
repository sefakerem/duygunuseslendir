package com.sefakerem.duygularianlama

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.*

class MainActivity : AppCompatActivity() {
    private var mediaRecorder: MediaRecorder? = null
    private var audioFilePath: String? = null
    private var isRecording = false
    private lateinit var emotionGame: EmotionGame

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val startButton = findViewById<Button>(R.id.startRecordingButton)
        val stopButton = findViewById<Button>(R.id.stopRecordingButton)
        val resultTextView = findViewById<TextView>(R.id.resultTextView)
        val emotionTextView = findViewById<TextView>(R.id.emotionTextView)

        audioFilePath = "${externalCacheDir?.absolutePath}/testaudio.3gp"
        emotionGame = EmotionGame()

        startButton.setOnClickListener {
            if (checkPermissions()) {
                startRecording()
            }
        }

        stopButton.setOnClickListener {
            stopRecording()
            resultTextView.text = "Kayıt Durduruldu. Dosya yükleniyor..."
            emotionTextView.text = emotionGame.currentEmotion
        }

        updateEmotion()
    }

    private fun updateEmotion() {
        findViewById<TextView>(R.id.emotionTextView).text = "Duygu: ${emotionGame.currentEmotion}"
    }

    private fun startRecording() {
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(audioFilePath)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            start()
            isRecording = true
        }
    }

    private fun stopRecording() {
        if (isRecording) {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            mediaRecorder = null
            isRecording = false

            val pcmFile = File(audioFilePath!!)
            val wavFile = File(pcmFile.parent, "audio.wav")
            pcmToWav(pcmFile, wavFile)

            uploadAudioFile(wavFile)
        }
    }

    private fun pcmToWav(pcmFile: File, wavFile: File, sampleRate: Int = 44100, channels: Int = 2, bitRate: Int = 16) {
        val bufferSize = 1024
        val audioData = ByteArray(bufferSize)
        var bytesRead: Int
        val byteRate = bitRate * sampleRate * channels / 8
        val headerSize = 44

        val dataOutputStream = DataOutputStream(FileOutputStream(wavFile))
        val dataInputStream = DataInputStream(FileInputStream(pcmFile))

        dataOutputStream.writeBytes("RIFF")
        dataOutputStream.writeInt(Integer.reverseBytes(headerSize + dataInputStream.available() - 8))
        dataOutputStream.writeBytes("WAVE")
        dataOutputStream.writeBytes("fmt ")
        dataOutputStream.writeInt(Integer.reverseBytes(16)) // Sub-chunk size, 16 for PCM
        dataOutputStream.writeShort(java.lang.Short.reverseBytes(1.toShort()).toInt()) // AudioFormat, 1 for PCM
        dataOutputStream.writeShort(java.lang.Short.reverseBytes(channels.toShort()).toInt()) // Number of channels
        dataOutputStream.writeInt(Integer.reverseBytes(sampleRate)) // Sample rate
        dataOutputStream.writeInt(Integer.reverseBytes(byteRate)) // Byte rate
        dataOutputStream.writeShort(java.lang.Short.reverseBytes((channels * bitRate / 8).toShort()).toInt()) // Block align
        dataOutputStream.writeShort(java.lang.Short.reverseBytes(bitRate.toShort()).toInt()) // Bits per sample
        dataOutputStream.writeBytes("data")
        dataOutputStream.writeInt(Integer.reverseBytes(dataInputStream.available()))

        while (dataInputStream.read(audioData).also { bytesRead = it } != -1) {
            dataOutputStream.write(audioData, 0, bytesRead)
        }

        dataInputStream.close()
        dataOutputStream.close()
    }

    private fun uploadAudioFile(wavFile: File) {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000")
            .build()

        val service = retrofit.create(UploadService::class.java)
        val requestFile = wavFile.readBytes().toRequestBody("audio/wav".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", wavFile.name, requestFile)

        service.uploadAudioFile(body).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val responseBody = response.body()?.string()
                if (responseBody != null) {
                    val jsonObject = JSONObject(responseBody)
                    val predictedEmotionScores = mutableMapOf<String, Float>()
                    jsonObject.keys().forEach { key ->
                        val valueString = jsonObject.getString(key).removeSuffix("%")
                        predictedEmotionScores[key] = valueString.toFloat() / 100
                    }
                    val score = emotionGame.evaluateResponse(predictedEmotionScores)
                    findViewById<TextView>(R.id.resultTextView).text = "Skor: $score"
                    if (emotionGame.moveToNextEmotion()) {
                        updateEmotion()
                    } else {
                        findViewById<TextView>(R.id.resultTextView).text = "Oyun bitti! Toplam skor: ${emotionGame.totalScore}"
                    }
                } else {
                    findViewById<TextView>(R.id.resultTextView).text = "Sonuç alınamadı."
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                findViewById<TextView>(R.id.resultTextView).text = "Hata: ${t.message}"
            }
        })
    }

    private fun checkPermissions(): Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 200)
            return false
        }
        return true
    }
}
