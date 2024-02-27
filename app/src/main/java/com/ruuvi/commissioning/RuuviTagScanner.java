package com.ruuvi.commissioning;

        import android.bluetooth.BluetoothAdapter;
        import android.bluetooth.BluetoothDevice;
        import android.bluetooth.BluetoothManager;
        import android.bluetooth.le.BluetoothLeScanner;
        import android.bluetooth.le.ScanCallback;
        import android.bluetooth.le.ScanFilter;
        import android.bluetooth.le.ScanResult;
        import android.bluetooth.le.ScanSettings;
        import android.content.Context;
        import android.location.Location;
        import android.os.ParcelUuid;
        import android.util.Log;

        import org.jetbrains.annotations.NotNull;

        import java.util.ArrayList;
        import java.util.List;

public class RuuviTagScanner {
    private static final String TAG = "RuuviTagScanner";
    private RuuviTagListener listener;

    private BluetoothAdapter bluetoothAdapter;
    private ScanSettings scanSettings;
    private BluetoothLeScanner scanner;
    private boolean scanning = false;
    public Location location;
    private Context context;

    public void Init(@NotNull Context context) {
        this.context = context;
        scanSettings = new ScanSettings.Builder()
                .setReportDelay(0)
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();

        final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        scanner = bluetoothAdapter.getBluetoothLeScanner();
    }

    public void setListener(RuuviTagListener listener) {
        this.listener = listener;
    }

    public void Start() {
        Log.d(TAG, "Start scanning");
        scanning = true;
        scanner.startScan(getScanFilters(), scanSettings, nsCallback);
    }

    public void Stop() {
        Log.d(TAG, "Stop scanning");
        scanning = false;
        scanner.stopScan(nsCallback);
    }

    private ScanCallback nsCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            foundDevice(result.getDevice(), result.getRssi(), result.getScanRecord().getBytes());
        }
    };

    private void foundDevice(BluetoothDevice device, int rssi, byte[] data) {
        RuuviTag tag = RuuviTag.Parse(device, rssi, data);

        Log.d(TAG, "found: " + device.getAddress());
        if (tag != null) {
            if (listener != null) listener.tagFound(tag);
            //resultHandler.Save(tag, location);
            Utils.Companion.setTag(context, tag);
        }
    }

    public static List<ScanFilter> getScanFilters() {
        List<ScanFilter> filters = new ArrayList<>();
        ScanFilter ruuviFilter = new ScanFilter.Builder()
                .setManufacturerData(0x0499, new byte[] {})
                .build();
        ScanFilter eddystoneFilter = new ScanFilter.Builder()
                .setServiceUuid(ParcelUuid.fromString("0000feaa-0000-1000-8000-00805f9b34fb"))
                .build();
        filters.add(ruuviFilter);
        filters.add(eddystoneFilter);
        return filters;
    }
}