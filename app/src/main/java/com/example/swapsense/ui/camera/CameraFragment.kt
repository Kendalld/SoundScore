package com.example.swapsense.ui.camera

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.swapsense.R


class CameraFragment : Fragment() {

    private lateinit var imageView: ImageView
    private lateinit var deleteButton: Button

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) {
        imageView.setImageURI(it)
        imageView.visibility = View.VISIBLE
        deleteButton.visibility = View.VISIBLE
    }

    private val captureImage = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        imageView.setImageBitmap(bitmap)
        imageView.visibility = View.VISIBLE
        deleteButton.visibility = View.VISIBLE
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        imageView = view.findViewById(R.id.selected_image_view)
        deleteButton = view.findViewById(R.id.button_delete_image)

        // Set an onClickListener for the "Add Image" button to handle camera permission and opening the camera
        view.findViewById<Button>(R.id.button_add_image).setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // Request camera permission if not granted
                requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
            } else {
                // Open the camera if permission is granted
                openCamera(requireContext())
            }
        }

        view.findViewById<Button>(R.id.button_add_image_lib).setOnClickListener{
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // Request camera permission if not granted
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), IMAGE_CAPTURE_CODE)

            } else {
                getContent.launch("image/*")

            }

        }

        // Set an onClickListener for the "Delete Image" button to clear and hide the image and button
        deleteButton.setOnClickListener {
            imageView.setImageBitmap(null) // Clears the image from ImageView
            imageView.visibility = ImageView.GONE // Hides the ImageView
            deleteButton.visibility = Button.GONE // Hides the Delete Button
        }
    }
    // Function to open the camera using an implicit intent
    private fun openCamera(context: Context) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(context.packageManager) != null) {
            startActivityForResult(intent, IMAGE_CAPTURE_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission was granted, open the camera
            openCamera(requireContext())
        } else {
            // Permission was denied, handle the case
        }
    }

    // Callback for the result from capturing an image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == IMAGE_CAPTURE_CODE) {
            // Process and display the captured image
            val imageBitmap = data?.extras?.get("data") as Bitmap
            imageView.setImageBitmap(imageBitmap)
            imageView.visibility = ImageView.VISIBLE // Show the ImageView
            deleteButton.visibility = Button.VISIBLE // Show the Delete Button
        }
    }


    companion object {
        private const val CAMERA_REQUEST_CODE = 1003
        private const val IMAGE_CAPTURE_CODE = 1002
        //fun newInstance() = SwapFragment()
    }
}

