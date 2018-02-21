package com.example.michal.pulsesensor;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.List;


/**
 * ZEWNETRZNA BIBLIOTEKA
 * Serwis do zarządzania połączeniem i danymi komunikacji przez serwer GATT na danym urządzeniu BLE
 * Zarządznie:
 * -peryferami Bluetooth(np. adapter w telefonie)
 * -serwisami GATT
 * -charakterystykami (pisanie, odczytywanie)
 * -deskryptorami
 */
public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    BluetoothGatt mBluetoothGatt;
    public String mBluetoothDeviceAddress;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    public int mConnectionState = STATE_DISCONNECTED;


    //okreslenie nowej charakterystyki
    private static final int WRITE_NEW_CHARACTERISTIC = -1;
    //maksymalna długość chcarakterystyki
    private static final int MAX_CHARACTERISTIC_LENGTH = 17;
    //okreslenie czy charakterystyka jest nadpisywana czy nie
    private boolean mIsWritingCharacteristic=false;

    //klasa to gromadzenie charakterystyk i zawartosci w ring bufferze
    private class BluetoothGattCharacteristicHelper{
        BluetoothGattCharacteristic mCharacteristic;
        String mCharacteristicValue;
        BluetoothGattCharacteristicHelper(BluetoothGattCharacteristic characteristic, String characteristicValue){
            mCharacteristic=characteristic;
            mCharacteristicValue=characteristicValue;
        }
    }
    //ring buffer
    private RingBuffer<BluetoothGattCharacteristicHelper> mCharacteristicRingBuffer = new RingBuffer<BluetoothGattCharacteristicHelper>(8);

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

    //Implemetntacja metody dla zdarzen zwiazancyh z serwerem GATT np. zmiana statusu połączenia czy odkrycie nowego serwisu
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            System.out.println("BluetoothGattCallback----onConnectionStateChange"+newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                //Po połączeniu próba wykrycia istniejących serwisów
                if(mBluetoothGatt.discoverServices())
                {
                    Log.i(TAG, "Attempting to start service discovery:");

                }
                else{
                    Log.i(TAG, "Attempting to start service discovery:not success");

                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override //wykrywanie nowych serwisów
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            System.out.println("onServicesDiscovered "+status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override //implementacja metody zajmująca się wpisywaniem danych do charakterystyk
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
        {
            synchronized(this)
            {
                //w zależnosci od status, wykonujemy dane operacje
                //Pierwszy przypadek gdy charatkerystyka istnieje
                if(status == BluetoothGatt.GATT_SUCCESS)
                {
                    System.out.println("onCharacteristicWrite success:"+ new String(characteristic.getValue()));
                    if(mCharacteristicRingBuffer.isEmpty())
                    {
                        mIsWritingCharacteristic = false;
                    }
                    else
                    {
                        BluetoothGattCharacteristicHelper bluetoothGattCharacteristicHelper = mCharacteristicRingBuffer.next();
                        if(bluetoothGattCharacteristicHelper.mCharacteristicValue.length() > MAX_CHARACTERISTIC_LENGTH)
                        {
                            try {
                                bluetoothGattCharacteristicHelper.mCharacteristic.setValue(bluetoothGattCharacteristicHelper.mCharacteristicValue.substring(0, MAX_CHARACTERISTIC_LENGTH).getBytes("ISO-8859-1"));

                            } catch (UnsupportedEncodingException e) {
                                throw new IllegalStateException(e);
                            }

                            if(mBluetoothGatt.writeCharacteristic(bluetoothGattCharacteristicHelper.mCharacteristic))
                            {
                                System.out.println("writeCharacteristic init "+new String(bluetoothGattCharacteristicHelper.mCharacteristic.getValue())+ ":success");
                            }else{
                                System.out.println("writeCharacteristic init "+new String(bluetoothGattCharacteristicHelper.mCharacteristic.getValue())+ ":failure");
                            }
                            bluetoothGattCharacteristicHelper.mCharacteristicValue = bluetoothGattCharacteristicHelper.mCharacteristicValue.substring(MAX_CHARACTERISTIC_LENGTH);
                        }
                        else
                        {
                            try {
                                bluetoothGattCharacteristicHelper.mCharacteristic.setValue(bluetoothGattCharacteristicHelper.mCharacteristicValue.getBytes("ISO-8859-1"));
                            } catch (UnsupportedEncodingException e) {
                                throw new IllegalStateException(e);
                            }

                            if(mBluetoothGatt.writeCharacteristic(bluetoothGattCharacteristicHelper.mCharacteristic))
                            {
                                System.out.println("writeCharacteristic init "+new String(bluetoothGattCharacteristicHelper.mCharacteristic.getValue())+ ":success");
                            }else{
                                System.out.println("writeCharacteristic init "+new String(bluetoothGattCharacteristicHelper.mCharacteristic.getValue())+ ":failure");
                            }
                            bluetoothGattCharacteristicHelper.mCharacteristicValue = "";
                            mCharacteristicRingBuffer.pop();
                        }
                    }
                }
                //Pisanie nowej charkaterystyki
                else if(status == WRITE_NEW_CHARACTERISTIC)
                {
                    if((!mCharacteristicRingBuffer.isEmpty()) && mIsWritingCharacteristic==false)
                    {
                        BluetoothGattCharacteristicHelper bluetoothGattCharacteristicHelper = mCharacteristicRingBuffer.next();
                        if(bluetoothGattCharacteristicHelper.mCharacteristicValue.length() > MAX_CHARACTERISTIC_LENGTH)
                        {

                            try {
                                bluetoothGattCharacteristicHelper.mCharacteristic.setValue(bluetoothGattCharacteristicHelper.mCharacteristicValue.substring(0, MAX_CHARACTERISTIC_LENGTH).getBytes("ISO-8859-1"));
                            } catch (UnsupportedEncodingException e) {
                                throw new IllegalStateException(e);
                            }

                            if(mBluetoothGatt.writeCharacteristic(bluetoothGattCharacteristicHelper.mCharacteristic))
                            {
                                System.out.println("writeCharacteristic init "+new String(bluetoothGattCharacteristicHelper.mCharacteristic.getValue())+ ":success");
                            }else{
                                System.out.println("writeCharacteristic init "+new String(bluetoothGattCharacteristicHelper.mCharacteristic.getValue())+ ":failure");
                            }
                            bluetoothGattCharacteristicHelper.mCharacteristicValue = bluetoothGattCharacteristicHelper.mCharacteristicValue.substring(MAX_CHARACTERISTIC_LENGTH);
                        }
                        else
                        {
                            try {
                                bluetoothGattCharacteristicHelper.mCharacteristic.setValue(bluetoothGattCharacteristicHelper.mCharacteristicValue.getBytes("ISO-8859-1"));
                            } catch (UnsupportedEncodingException e) {
                                throw new IllegalStateException(e);
                            }


                            if(mBluetoothGatt.writeCharacteristic(bluetoothGattCharacteristicHelper.mCharacteristic))
                            {
                                System.out.println("writeCharacteristic init "+new String(bluetoothGattCharacteristicHelper.mCharacteristic.getValue())+ ":success");
                            }else{
                                System.out.println("writeCharacteristic init "+new String(bluetoothGattCharacteristicHelper.mCharacteristic.getValue())+ ":failure");
                            }
                            bluetoothGattCharacteristicHelper.mCharacteristicValue = "";
                            mCharacteristicRingBuffer.pop();
                        }
                    }

                    mIsWritingCharacteristic = true;

                    //czyszczenie buffora zeby zapobieg blogowaniu mIsWritingCharacteristic
                    if(mCharacteristicRingBuffer.isFull())
                    {
                        mCharacteristicRingBuffer.clear();
                        mIsWritingCharacteristic = false;
                    }
                }
                else
                //ostatni przypadek gdy nie udalo sie odtworzyc charakterystyki
                {
                    mCharacteristicRingBuffer.clear();
                    System.out.println("onCharacteristicWrite fail:"+ new String(characteristic.getValue()));
                    System.out.println(status);
                }
            }
        }

        @Override //implemetnacja odczytywania charakterystyk
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                System.out.println("onCharacteristicRead  "+characteristic.getUuid().toString());
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }
        @Override //oblsuga deskryptorów
        public void  onDescriptorWrite(BluetoothGatt gatt,
                                       BluetoothGattDescriptor characteristic,
                                       int status){
            System.out.println("onDescriptorWrite  "+characteristic.getUuid().toString()+" "+status);
        }
        @Override //obsluga zmiany danych w charakterystyce
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            System.out.println("onCharacteristicChanged  "+new String(characteristic.getValue()));
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };

    // updatowanie serwisu
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        System.out.println("BluetoothLeService broadcastUpdate");
        final byte[] data = characteristic.getValue();
        if (data != null && data.length > 0) {
            intent.putExtra(EXTRA_DATA, new String(data));
            sendBroadcast(intent);
        }
//        }
    }
    //interefejs konieczny do działania serwisu
    public class LocalBinder extends Binder {
        BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override //interefejs konieczny do działania serwisu
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * inicjalizacja referancji do adaptera Bluetooth
     * zwraca true jezezli operacja sie powiodla
     */
    public boolean initialize() {
        // dla API powyzej 18m pobieranie referencji do adaptera przez BluetoothManagera
        System.out.println("BluetoothLeService initialize"+mBluetoothManager);
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * łączenie się z serwerem GATT na urządznieu BLE
     *
     * @param address adres urządzenia
     *
     * @return zwraca true jezeli incjalizacja polaczenia sie powiodla
     *
     */
    public boolean connect(final String address) {
        System.out.println("BluetoothLeService connect"+address+mBluetoothGatt);
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }


        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        System.out.println("device.connectGatt connect");
        synchronized(this)
        {
            mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        }
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    /**
     * rozłączenie istniejącego połączenia bądź anulowanie nadchodzącego
     */
    public void disconnect() {
        System.out.println("BluetoothLeService disconnect");
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * Zamykanie wszelkim peryferiów biorączych udział w komunikacji
     */
    public void close() {
        System.out.println("BluetoothLeService close");
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Żądanie odczytu danej charakterysyki (BluetoothGattCharacteristic)
     * @param characteristic charaktektrystyka, z której należy czytać dane
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }


    /**
     * Wpisywanie informacji do urządzenia danej chrakterystyki (BluetoothGattCharacteristic)
     * @param characteristic charakterystyka, do której należy wpisywać dane
     */
    public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        String writeCharacteristicString;
        try {
            writeCharacteristicString = new String(characteristic.getValue(),"ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
        System.out.println("allwriteCharacteristicString:"+writeCharacteristicString);

        mCharacteristicRingBuffer.push(new BluetoothGattCharacteristicHelper(characteristic,writeCharacteristicString) );
        System.out.println("mCharacteristicRingBufferlength:"+mCharacteristicRingBuffer.size());


        mGattCallback.onCharacteristicWrite(mBluetoothGatt, characteristic, WRITE_NEW_CHARACTERISTIC);

    }

    /**
     * Aktywowanie bądź deaktywowanie notyfukacji z danej charakterystki
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic określenie chrakterystyki
     * @param enabled jeżeli chcemy notyfikacjie - true, jeżel nie - false
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
    }

    /**
     * Odtwarzanie listy wsperanych serwisów GATT na urządzeniu
     * @return zwraca liste urządzeń
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }


}
