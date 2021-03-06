package com.example.michal.pulsesensor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/*
* Bilbioteka zewnętrzna bazująca na funkcjonalnościach zaimplemetnowanych w BluetoothLeService
* dostosowana do urządzenia BlunoBeetle
*
*/


//aktywność, zarządzająca interfejsem do komunikowania się

public abstract  class BlunoLibrary  extends Activity {

    private Context mainContext=this;



    public abstract void onConectionStateChange(connectionStateEnum theconnectionStateEnum);
    public abstract void onSerialReceived(String theString);
    public void serialSend(String theString){
        if (mConnectionState == connectionStateEnum.isConnected) {
            mSCharacteristic.setValue(theString);
            mBluetoothLeService.writeCharacteristic(mSCharacteristic);
        }
    }

    private int mBaudrate=115200;	//ustawienie domyślnego baudratu
    private String mPassword="AT+PASSWOR=DFRobot\r\n"; //hasło do urządzenia


    private String mBaudrateBuffer = "AT+CURRUART="+mBaudrate+"\r\n"; //string z ustawieniem baudratu przez komendy AT


    public void serialBegin(int baud){
        mBaudrate=baud;
        mBaudrateBuffer = "AT+CURRUART="+mBaudrate+"\r\n"; //string z ustawieniem baudratu przez komendy AT, z możliwością zmiany baudratu
    }


    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }
    private static BluetoothGattCharacteristic mSCharacteristic, mModelNumberCharacteristic, mSerialPortCharacteristic, mCommandCharacteristic;
    BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private LeDeviceListAdapter mLeDeviceListAdapter=null;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning =false;
    AlertDialog mScanDeviceDialog;
    private String mDeviceName;
    private String mDeviceAddress;
    public enum connectionStateEnum{isNull, isScanning, isToScan, isConnecting , isConnected, isDisconnecting};
    public connectionStateEnum mConnectionState = connectionStateEnum.isNull;
    private static final int REQUEST_ENABLE_BT = 1;

    private Handler mHandler= new Handler();

    public boolean mConnected = false;

    private final static String TAG = BlunoLibrary.class.getSimpleName();

    //otwieranie połączenia
    private Runnable mConnectingOverTimeRunnable=new Runnable(){

        @Override
        public void run() {
            if(mConnectionState==connectionStateEnum.isConnecting)
                mConnectionState=connectionStateEnum.isToScan;
            onConectionStateChange(mConnectionState);
            mBluetoothLeService.close();
        }};


    //zamykania połączenia
    private Runnable mDisonnectingOverTimeRunnable=new Runnable(){

        @Override
        public void run() {
            if(mConnectionState==connectionStateEnum.isDisconnecting)
                mConnectionState=connectionStateEnum.isToScan;
            onConectionStateChange(mConnectionState);
            mBluetoothLeService.close();
        }};

    //konkretne parametry do komunikacji z BlunoBeetle
    public static final String SerialPortUUID="0000dfb1-0000-1000-8000-00805f9b34fb";
    public static final String CommandUUID="0000dfb2-0000-1000-8000-00805f9b34fb";
    public static final String ModelNumberStringUUID="00002a24-0000-1000-8000-00805f9b34fb";


    //obsługa pojawienia się na ekranie aktywności
    public void onCreateProcess()
    {
        // sprawdzanie czy Bluetooth jest wspierany, jezeli nie to wyłączamy aktywność
        if(!initiate())
        {
            Toast.makeText(mainContext, R.string.error_bluetooth_not_supported,
                    Toast.LENGTH_SHORT).show();
            ((Activity) mainContext).finish();
        }


        //uruchamianie serwisu do zarządzania Bluetooth'em
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        mLeDeviceListAdapter = new LeDeviceListAdapter();

        //inicjalizacja dialogu do skanowania i wykrywania urządzeń
        mScanDeviceDialog = new AlertDialog.Builder(mainContext)
                .setTitle("BLE Device Scan...").setAdapter(mLeDeviceListAdapter, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        final BluetoothDevice device = mLeDeviceListAdapter.getDevice(which);
                        if (device == null)
                            return;
                        scanLeDevice(false);

                        if(device.getName()==null || device.getAddress()==null)
                        {
                            mConnectionState=connectionStateEnum.isToScan;
                            onConectionStateChange(mConnectionState);
                        }
                        else{

                            System.out.println("onListItemClick " + device.getName().toString());

                            System.out.println("Device Name:"+device.getName() + "   " + "Device Name:" + device.getAddress());

                            mDeviceName=device.getName().toString();
                            mDeviceAddress=device.getAddress().toString();

                            if (mBluetoothLeService.connect(mDeviceAddress)) {
                                Log.d(TAG, "Connect request success");
                                mConnectionState=connectionStateEnum.isConnecting;
                                onConectionStateChange(mConnectionState);
                                mHandler.postDelayed(mConnectingOverTimeRunnable, 10000);
                            }
                            else {
                                Log.d(TAG, "Connect request fail");
                                mConnectionState=connectionStateEnum.isToScan;
                                onConectionStateChange(mConnectionState);
                            }
                        }
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface arg0) {
                        System.out.println("mBluetoothAdapter.stopLeScan");

                        mConnectionState = connectionStateEnum.isToScan;
                        onConectionStateChange(mConnectionState);
                        mScanDeviceDialog.dismiss();

                        scanLeDevice(false);
                    }
                }).create();

    }



    //obsługa powrotu do aktywności(gdy ponownie pojawi się na ekranie)
    public void onResumeProcess() {
        System.out.println("BlUNOActivity onResume");
       //jezeli bluetooth nie jest włączony włączamy dialog, który to umożliwi
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(
                        BluetoothAdapter.ACTION_REQUEST_ENABLE);
                ((Activity) mainContext).startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        mainContext.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

    }


    //obsługa częsciowego zaniku aktywności z ekranu, celowe usunięcie kodu abyb nie przerwyać połączenia
    public void onPauseProcess() {
        System.out.println("BLUNOActivity onPause");
        scanLeDevice(false);
        mainContext.unregisterReceiver(mGattUpdateReceiver);
        mLeDeviceListAdapter.clear();
        mConnectionState=connectionStateEnum.isToScan;
        onConectionStateChange(mConnectionState);
        mScanDeviceDialog.dismiss();
        if(mBluetoothLeService!=null)
        {
            mBluetoothLeService.disconnect();
            mHandler.postDelayed(mDisonnectingOverTimeRunnable, 10000);

//			mBluetoothLeService.close();
        }
        mSCharacteristic=null;

    }

    //obsługa całkowitego zaniku aktywności z ekranu, celowe usunięcie kodu abyb nie przerwyać połączenia
    public void onStopProcess() {
        System.out.println("MiUnoActivity onStop");
        if(mBluetoothLeService!=null)
        {
//			mBluetoothLeService.disconnect();
//            mHandler.postDelayed(mDisonnectingOverTimeRunnable, 10000);
            mHandler.removeCallbacks(mDisonnectingOverTimeRunnable);
            mBluetoothLeService.close();
        }
        mSCharacteristic=null;
    }


    //obsługa zamknięcia aktyności
    public void onDestroyProcess() {

        mainContext.unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    public void onActivityResultProcess(int requestCode, int resultCode, Intent data) {
        //jezeli uzytkownik nie włączy bluetooth'a to zamykamy
        if (requestCode == REQUEST_ENABLE_BT
                && resultCode == Activity.RESULT_CANCELED) {
            ((Activity) mainContext).finish();
            return;
        }
    }


    boolean initiate()
    {
        // sprawdzanie czy bluetooth jest wspierany przez system
        if (!mainContext.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_BLUETOOTH_LE)) {
            return false;
        }

        //inicjalizacja adaptera , dla API powyzej 18 robimy to przez BluetoothManager.
        final BluetoothManager bluetoothManager = (BluetoothManager) mainContext.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) {
            return false;
        }
        return true;
    }


    //obsługa komunikacji z serwisem BletoothLeService
    // ACTION_GATT_CONNECTED: połączono do GATT serwisu
    // ACTION_GATT_DISCONNECTED: rozłączono z GATT
    // ACTION_GATT_SERVICES_DISCOVERED: odkryto GATT
    // ACTION_DATA_AVAILABLE: otrzymano dane z urządzenia, rezultat odczytu charakterystyki bądź notyfikacji
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @SuppressLint("DefaultLocale")
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            System.out.println("mGattUpdateReceiver->onReceive->action="+action);
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                mHandler.removeCallbacks(mConnectingOverTimeRunnable);

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                mConnectionState = connectionStateEnum.isToScan;
                onConectionStateChange(mConnectionState);
                mHandler.removeCallbacks(mDisonnectingOverTimeRunnable);
                mBluetoothLeService.close();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                for (BluetoothGattService gattService : mBluetoothLeService.getSupportedGattServices()) {
                    System.out.println("ACTION_GATT_SERVICES_DISCOVERED  "+
                            gattService.getUuid().toString());
                }
                getGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                if(mSCharacteristic==mModelNumberCharacteristic)
                {
                    if (intent.getStringExtra(BluetoothLeService.EXTRA_DATA).toUpperCase().startsWith("DF BLUNO")) {
                        mBluetoothLeService.setCharacteristicNotification(mSCharacteristic, false);
                        mSCharacteristic=mCommandCharacteristic;
                        mSCharacteristic.setValue(mPassword);
                        mBluetoothLeService.writeCharacteristic(mSCharacteristic);
                        mSCharacteristic.setValue(mBaudrateBuffer);
                        mBluetoothLeService.writeCharacteristic(mSCharacteristic);
                        mSCharacteristic=mSerialPortCharacteristic;
                        mBluetoothLeService.setCharacteristicNotification(mSCharacteristic, true);
                        mConnectionState = connectionStateEnum.isConnected;
                        onConectionStateChange(mConnectionState);

                    }
                    else {
                        Toast.makeText(mainContext, "Please select DFRobot devices",Toast.LENGTH_SHORT).show();
                        mConnectionState = connectionStateEnum.isToScan;
                        onConectionStateChange(mConnectionState);
                    }
                }
                else if (mSCharacteristic==mSerialPortCharacteristic) {
                    onSerialReceived(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                }


                System.out.println("displayData "+intent.getStringExtra(BluetoothLeService.EXTRA_DATA));

            }
        }
    };


    //obsługa kliknięcia przycisku "SCAN"
    void buttonScanOnClickProcess()
    {
        switch (mConnectionState) {
            case isNull:
                mConnectionState=connectionStateEnum.isScanning;
                onConectionStateChange(mConnectionState);
                scanLeDevice(true);
                mScanDeviceDialog.show();
                break;
            case isToScan:
                mConnectionState=connectionStateEnum.isScanning;
                onConectionStateChange(mConnectionState);
                scanLeDevice(true);
                mScanDeviceDialog.show();
                break;
            case isScanning:

                break;

            case isConnecting:

                break;
            case isConnected:
                mBluetoothLeService.disconnect();
                mHandler.postDelayed(mDisonnectingOverTimeRunnable, 10000);

			   // mBluetoothLeService.close();
                mConnectionState=connectionStateEnum.isDisconnecting;
                onConectionStateChange(mConnectionState);
                break;
            case isDisconnecting:

                break;

            default:
                break;
        }


    }

    void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.

            System.out.println("mBluetoothAdapter.startLeScan");

            if(mLeDeviceListAdapter != null)
            {
                mLeDeviceListAdapter.clear();
                mLeDeviceListAdapter.notifyDataSetChanged();
            }

            if(!mScanning)
            {
                mScanning = true;
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            }
        } else {
            if(mScanning)
            {
                mScanning = false;
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            }
        }
    }


    ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            System.out.println("mServiceConnection onServiceConnected");
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                ((Activity) mainContext).finish();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            System.out.println("mServiceConnection onServiceDisconnected");
            mBluetoothLeService = null;
        }
    };

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi,
                             byte[] scanRecord) {
            ((Activity) mainContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    System.out.println("mLeScanCallback onLeScan run ");
                    mLeDeviceListAdapter.addDevice(device);
                    mLeDeviceListAdapter.notifyDataSetChanged();
                }
            });
        }
    };

    private void getGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        mModelNumberCharacteristic=null;
        mSerialPortCharacteristic=null;
        mCommandCharacteristic=null;
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            uuid = gattService.getUuid().toString();
            System.out.println("displayGattServices + uuid="+uuid);

            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                uuid = gattCharacteristic.getUuid().toString();
                if(uuid.equals(ModelNumberStringUUID)){
                    mModelNumberCharacteristic=gattCharacteristic;
                    System.out.println("mModelNumberCharacteristic  "+mModelNumberCharacteristic.getUuid().toString());
                }
                else if(uuid.equals(SerialPortUUID)){
                    mSerialPortCharacteristic = gattCharacteristic;
                    System.out.println("mSerialPortCharacteristic  "+mSerialPortCharacteristic.getUuid().toString());
//                    updateConnectionState(R.string.comm_establish);
                }
                else if(uuid.equals(CommandUUID)){
                    mCommandCharacteristic = gattCharacteristic;
                    System.out.println("mSerialPortCharacteristic  "+mSerialPortCharacteristic.getUuid().toString());
//                    updateConnectionState(R.string.comm_establish);
                }
            }
            mGattCharacteristics.add(charas);
        }

        if (mModelNumberCharacteristic==null || mSerialPortCharacteristic==null || mCommandCharacteristic==null) {
            Toast.makeText(mainContext, "Please select DFRobot devices",Toast.LENGTH_SHORT).show();
            mConnectionState = connectionStateEnum.isToScan;
            onConectionStateChange(mConnectionState);
        }
        else {
            mSCharacteristic=mModelNumberCharacteristic;
            mBluetoothLeService.setCharacteristicNotification(mSCharacteristic, true);
            mBluetoothLeService.readCharacteristic(mSCharacteristic);
        }

    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator =  ((Activity) mainContext).getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            if (!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view
                        .findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view
                        .findViewById(R.id.device_name);
                System.out.println("mInflator.inflate  getView");
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText(R.string.unknown_device);
            viewHolder.deviceAddress.setText(device.getAddress());

            return view;
        }
    }
}
