package com.example.usbdev;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.HashMap;

public class  MainActivity extends Activity {
    private TextView textView;
    private Button button;
    private UsbManager usbManager;
    private UsbDevice device;
    private PendingIntent permissionIntent;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        textView.append("\nPermission allowed");
                    }
                    else {
                        textView.append("\nPermission denied for device");
                    }

                    if(device != null){
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            textView.append("\nPermission allowed"+device.getDeviceName());
                        }
                        else {
                            textView.append("\nPermission denied for device"+device.getDeviceName());
                        }
                    }
                    else textView.append("\nDevice null");
                }
            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textView);
        button = findViewById(R.id.button);
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(usbReceiver, filter, Context.RECEIVER_EXPORTED);
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executeButton();
            }
        });
    }


    private void executeButton() {
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        device = null;

        for (UsbDevice d : deviceList.values()) {
            device = d;
            break; // Exit the loop after finding the first device
        }
        if (device != null) {
            // If a device is found, request permission for it
            textView.append("\nDevice: "+device.getDeviceName());
            usbManager.requestPermission(device, permissionIntent);

        } else {
            // No USB devices connected
            textView.append("\nNo connected devices");
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(usbReceiver);
    }
}
