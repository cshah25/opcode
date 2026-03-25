package com.example.opcodeapp.view;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.opcodeapp.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.common.base.Preconditions;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class QrCodeViewerFragment extends BottomSheetDialogFragment {
    private static String encodedData;

    public static QrCodeViewerFragment newInstance(String data) {
        encodedData = data;
        return new QrCodeViewerFragment();
    }

    private QrCodeViewerFragment() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new BottomSheetDialog(requireContext(), getTheme());
    }

    @Override
    public int getTheme() {
        return R.style.ThemeOverlay_App_BottomSheetDialog;
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

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onStart() {
        super.onStart();

        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        if (dialog != null) {
            dialog.setDismissWithAnimation(true);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ImageView qrImage = view.findViewById(R.id.qr_image);
        qrImage.post(() -> {
            Bitmap bitmap = generateQrBitmap("event:" + encodedData, qrImage.getWidth());
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
        Preconditions.checkArgument(size > 0, "Invalid size for QR Code (size: " + size + ")");
        Bitmap map = null;
        try {
            BarcodeEncoder encoder = new BarcodeEncoder();
            map = encoder.encodeBitmap(data, BarcodeFormat.QR_CODE, size, size);
        } catch (WriterException e) {
            Log.e("QR CODE", "Error generating the QR code");
            Toast.makeText(requireContext(), "Could not generate QR code for this event", Toast.LENGTH_SHORT).show();
        }
        return map;
    }
}
