package com.example.opcodeapp.view;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.opcodeapp.R;
import com.example.opcodeapp.callback.FirestoreCallbackEventReceive;
import com.example.opcodeapp.controller.SessionController;
import com.example.opcodeapp.model.Event;
import com.example.opcodeapp.model.User;
import com.example.opcodeapp.repository.EventRepository;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

/**
 * Fragment responsible for scanning QR codes using CameraX and ML Kit.
 *
 * <p>This fragment handles:
 * <ul>
 *     <li>Requesting camera permission</li>
 *     <li>Displaying a live camera preview</li>
 *     <li>Scanning QR codes in real time</li>
 *     <li>Parsing the scanned QR data</li>
 *     <li>Navigating to the appropriate event screen</li>
 * </ul>
 * <p>
 * The QR code format is expected to follow:
 * <pre>opcode:event:{eventId}</pre>
 */
public class QrCodeScannerFragment extends Fragment {
    private User user;
    private PreviewView previewView;
    private BarcodeScanner scanner;
    private boolean scanned = false;

    private final ActivityResultLauncher<String> cameraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted) {
            startCamera();
        } else {
            Toast.makeText(requireContext(), "Camera permission required", Toast.LENGTH_SHORT).show();
        }
    });

    /**
     * Constructor method
     */
    public QrCodeScannerFragment() {
        super(R.layout.fragment_qr_code_scanner);
    }

    /**
     * Initializes the camera preview, validates the current user session,
     * configures the QR code scanner, and requests camera permission if needed.
     *
     * @param view               The fragment's root view
     * @param savedInstanceState Previously saved state
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        previewView = view.findViewById(R.id.qr_preview);

        user = SessionController.getInstance(requireContext()).getCurrentUser();

        // Validate current user
        if (user == null) {
            Toast.makeText(requireContext(), "Could not retrieve the current user", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).navigate(R.id.setupFragment);
            return;
        }

        // Configure scanner to only detect QR codes
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build();
        scanner = BarcodeScanning.getClient(options);
        int permission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA);
        if (permission == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    /**
     * Starts the CameraX preview and binds the image analysis pipeline
     * used for real-time QR code scanning. The back camera is used by default
     * and only the latest frame is analyzed to reduce processing overhead.
     */
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Create live camera preview
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                // Create frame analyzer for QR scanning
                ImageAnalysis analysis = new ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();
                analysis.setAnalyzer(ContextCompat.getMainExecutor(requireContext()), this::scanFrame);

                CameraSelector selector = CameraSelector.DEFAULT_BACK_CAMERA;
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(getViewLifecycleOwner(), selector, preview, analysis);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    /**
     * Processes a single camera frame and attempts to detect QR codes.
     *
     * <p>If a valid QR code is found, scanning stops and the QR value
     * is passed to {@link #onQRCodeScanned(String)}.
     *
     * @param imageProxy The current camera frame
     */
    @OptIn(markerClass = ExperimentalGetImage.class)
    private void scanFrame(ImageProxy imageProxy) {
        // Prevent repeated scans after first successful detection
        if (scanned) {
            imageProxy.close();
            return;
        }

        Image mediaImage = imageProxy.getImage();
        if (mediaImage == null) {
            imageProxy.close();
            return;
        }

        // Convert camera frame into ML Kit compatible image
        InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());

        // Process frame using ML Kit barcode scanner
        scanner.process(image).addOnSuccessListener(barcodes -> {
            for (Barcode barcode : barcodes) {
                String value = barcode.getRawValue();

                if (value != null) {
                    scanned = true;
                    onQRCodeScanned(value);
                    break;
                }
            }
        }).addOnCompleteListener(task -> imageProxy.close());
    }

    /**
     * Handles the scanned QR code value.
     *
     * <p>Expected format:
     * <pre>opcode:event:{eventId}</pre>
     *
     * The event is retrieved from Firestore and the user is navigated
     * either to the organizer event view or entrant event details view.
     *
     * @param value The raw scanned QR code string
     */
    private void onQRCodeScanned(String value) {
        Toast.makeText(requireContext(), value, Toast.LENGTH_SHORT).show();
        NavController controller = NavHostFragment.findNavController(this);

        // Validate encoded data
        String[] items = value.split(":");
        if (items.length < 3 || !items[0].equals("opcode") || !items[1].equals("event")) {
            Toast.makeText(requireContext(), "Could not retrieve required data from QR Code", Toast.LENGTH_SHORT).show();
            controller.navigate(R.id.eventListFragment);
            return;
        }

        EventRepository repository = new EventRepository(FirebaseFirestore.getInstance());
        repository.fetchEvent(items[2], new FirestoreCallbackEventReceive() {
            @Override
            public void onDataReceived(@Nullable Event event) {
                if (event == null) {
                    Toast.makeText(requireContext(), "No event match this id", Toast.LENGTH_SHORT).show();
                    controller.navigate(R.id.eventListFragment);
                    return;
                }

                Bundle args = new Bundle();
                args.putParcelable("event", event);
                int destination = event.getOrganizerId().equals(user.getId()) ?
                        R.id.organizerEventFragment : R.id.eventDetailsFragment;
                controller.navigate(destination, args);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(requireContext(), "Could not retrieve the event from Firestore", Toast.LENGTH_SHORT).show();
                controller.navigate(R.id.eventListFragment);
            }
        });
    }
}
