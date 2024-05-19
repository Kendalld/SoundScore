package com.example.swapsense.ui.dashboard

import android.Manifest
import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.swapsense.databinding.FragmentSensorBinding

class SensorFragment : Fragment(), SensorEventListener { // Passing in the SensorEventListener interface

    private var _binding: FragmentSensorBinding? = null
    private val binding get() = _binding!!  // Instead of R.findViewByID stuff
    private lateinit var mediaRecorder: MediaRecorder  // without lateinits the compiler complains
    private lateinit var textViewAudioLevel: TextView
    private lateinit var textViewLightLevel: TextView
    private lateinit var sensorManager: SensorManager
    private var lightSensor: Sensor? = null

    private val REQUEST_RECORD_AUDIO_PERMISSION = 200  // A constant to identify our permission request
    private val handler = Handler()  // Handles scheduling of tasks
    private val updateRunnable = object : Runnable {  // A task that can be run on a schedule. In this case updates the audio level indicator.
        override fun run() {
            updateAudioLevelIndicator()
            handler.postDelayed(this, 100)  // Our runnable object r: "this" will run every 100 milliseconds
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSensorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        textViewAudioLevel = binding.textViewAudio
        textViewLightLevel = binding.textViewLight
        requestMicrophonePermission()
        setupLightSensor()
    }

    // Request microphone permission
    private fun requestMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(requireActivity(),
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                    Manifest.permission.RECORD_AUDIO)) {
                Toast.makeText(context, "What?! I can't hear you!", Toast.LENGTH_LONG).show()
            }
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_RECORD_AUDIO_PERMISSION)
        } else {
            setupAndStartMediaRecorder()  // Set up and start the MediaRecorder if permission is already granted
        }
    }

    // Handles callback when user grants or denies permission
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            setupAndStartMediaRecorder()  // Permission was granted, set up and start the recorder
        } else {
            Toast.makeText(context, "ACCESS denied", Toast.LENGTH_SHORT).show()
        }
    }


    // This code block seems ubiquitous for audio control with MediaRecorder.
    private fun setupAndStartMediaRecorder() {
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile("/dev/null")  // We don't save the audio file, just measure amplitude
            prepare()
            start()
        }

        handler.post(updateRunnable)  // Start the periodic updates
    }

    private fun setupLightSensor() {
        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        if (lightSensor != null) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL) // The SensorEventListener is required for the .registerListener method
        } else {
            Toast.makeText(context, "I CANT SEE!!", Toast.LENGTH_SHORT).show()// my phones don't actually have light sensors :(
        }

            }
    // these two methods are required for the SensorEventListener interface and override it.
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            textViewLightLevel.text = "Light Level: ${event.values[0]}"
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Required for SensorEventListener interface, but not used
    }


    private fun updateAudioLevelIndicator() {
        val amplitude = mediaRecorder.maxAmplitude  // Get the current sound amplitude from the recorder
        textViewAudioLevel.text = "Audio Level: $amplitude"  // Update the TextView with this value
    }

    override fun onPause() {
        super.onPause()
        if (::mediaRecorder.isInitialized) { // :: is a reference to the class it is used to safely check if the variable is initialized
            mediaRecorder.stop()
            mediaRecorder.release()  // Release the MediaRecorder resources
        }
        handler.removeCallbacks(updateRunnable)  // Stop updating the UI when the fragment is paused
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // Clean up the binding when the view is destroyed
    }
}