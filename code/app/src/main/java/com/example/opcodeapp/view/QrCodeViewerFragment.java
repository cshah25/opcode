package com.example.opcodeapp.view;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.example.opcodeapp.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class QrCodeViewerFragment extends BottomSheetDialogFragment {
    private static String encodedData;
    private ImageView qrImage;

    public static QrCodeViewerFragment newInstance(String eventId) {
        encodedData = eventId;
        return new QrCodeViewerFragment();
    }

    private QrCodeViewerFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.dialog_qr_code, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        if (dialog != null)
            dialog.setDismissWithAnimation(true);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        qrImage = view.findViewById(R.id.qr_image);

        MaterialButton saveButton = view.findViewById(R.id.qr_code_save);
        MaterialButton shareButton = view.findViewById(R.id.qr_code_share);

        saveButton.setOnClickListener(v -> saveHandler());
        shareButton.setOnClickListener(v -> shareHandler());

        qrImage.post(() -> {
            Bitmap bitmap = generateQrBitmap("opcode:event:" + encodedData, qrImage.getWidth());
            if (bitmap != null)
                qrImage.setImageBitmap(bitmap);
        });
    }

    /**
     * Generates a QR code with the data passed
     *
     * @param data Data to encode into the QR code
     * @param size Dimensions of the image
     * @return QR code bitmap
     */
    private Bitmap generateQrBitmap(String data, int size) {
        if (size <= 0) {
            Log.e("QrCodeViewerFragment", "Invalid size for QR Code (size: " + size + ")");
            return null;
        }

        Bitmap bitmap = null;
        try {
            BarcodeEncoder encoder = new BarcodeEncoder();
            bitmap = encoder.encodeBitmap(data, BarcodeFormat.QR_CODE, size, size);
        } catch (WriterException e) {
            Log.e("QR CODE", "Error generating the QR code");
            Toast.makeText(requireContext(), "Could not generate QR code for this event", Toast.LENGTH_SHORT).show();
        }
        return bitmap;
    }

    /**
     * On click handler for saving the qr code to the device's gallery
     */
    private void saveHandler() {
        Bitmap bitmap = ((BitmapDrawable) qrImage.getDrawable()).getBitmap();

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "qr_code_" + System.currentTimeMillis() + ".png");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

        Uri uri = requireContext().getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
        );

        if (uri != null) {
            try (OutputStream out = requireContext().getContentResolver().openOutputStream(uri)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                Toast.makeText(requireContext(), "QR code saved", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(requireContext(), "Failed to save QR code", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * On click handler for the share button that will open the share menu for
     * sharing the qr code
     */
    private void shareHandler() {
        Bitmap bitmap = ((BitmapDrawable) qrImage.getDrawable()).getBitmap();
        File file = new File(requireContext().getCacheDir(), "shared_qr.png");

        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (IOException e) {
            Toast.makeText(requireContext(),
                    "Failed to share QR code",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Uri uri = FileProvider.getUriForFile(requireContext(),
                requireContext().getPackageName() + ".provider",
                file);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/png");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(intent, "Share QR Code"));
    }
}
