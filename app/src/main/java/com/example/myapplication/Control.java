package com.example.myapplication;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class Control extends Activity {
	private final static String TAG = Control.class.getSimpleName();
	private static final String DB = "debug";
	private final static String UUID_KEY_DATA = "0000F001-0000-1000-8000-00805F9B34FB";
	public static final String EXTRAS_DEVICE = "EXTRAS_DEVICE";
    private String mDeviceName = null;
    public String s;
    private String mDeviceAddress = null;
//	private String mDeviceUuid = "0000F001-0000-1000-8000-00805F9B34FB";
	private String mDeviceUuid = null;
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    private boolean mConnected = false;
    private String rssi_value;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
	private BluetoothLeService mBluetoothLeService = null;
	private BluetoothGattCharacteristic target_character = null;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

	Button but_send1,but_connect,but_stop;
	TextView tv_deviceName,tv_deviceAddr,tv_connstatus,tv_currentRSSI,tv_targetUUID,tv_rx;
	EditText et_duration,et_white,et_yellow;
	ExpandableListView lv;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		tv_deviceName = (TextView) findViewById(R.id.tv_Name);
		tv_deviceAddr = (TextView) findViewById(R.id.tv_MAC);
		tv_connstatus = (TextView) findViewById(R.id.tv_con);
		tv_currentRSSI = (TextView) findViewById(R.id.tv_RSSI);
		tv_currentRSSI.setText("null");
		tv_targetUUID = (TextView) findViewById(R.id.tv_UUID);
		tv_targetUUID.setText("null");
		tv_rx = (TextView) findViewById(R.id.TV_RX);
		et_duration = (EditText) findViewById(R.id.ET_TX1);
		et_white = (EditText) findViewById(R.id.ET_TX3);
		et_yellow = (EditText) findViewById(R.id.ET_TX4);
		lv=(ExpandableListView)this.findViewById(R.id.ELV1);
		lv.setOnChildClickListener(servicesListClickListner);
		
		Intent intent = getIntent();
		Log.d(TAG, "Control onCreate");
		mDeviceAddress = intent.getStringExtra(Device.EXTRA_DEVICE_ADDRESS);
		mDeviceName = intent.getStringExtra(Device.EXTRA_DEVICE_NAME);
		mDeviceUuid = intent.getStringExtra(String.valueOf(target_character));
		Log.d(TAG, "mDeviceAddress = " + mDeviceAddress);
		Log.d(TAG, "mDeviceName = " + mDeviceName);
		Log.d(TAG, "mDeviceUUID = " + mDeviceUuid);
		tv_deviceName.setText(mDeviceName);
		tv_deviceAddr.setText(mDeviceAddress);
		tv_targetUUID.setText(mDeviceUuid);

		Log.d(TAG, "start BluetoothLE Service");
		
		Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
		bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);


		but_send1 = (Button) findViewById(R.id.but_send);
		but_send1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(target_character != null) {
					Log.d(TAG, "cmd start");


					String cmd_duration = et_duration.getText().toString();
					String cmd_white = et_white.getText().toString();
					String cmd_yellow = et_yellow.getText().toString();


					if (!cmd_duration.isEmpty() && !cmd_white.isEmpty() && !cmd_yellow.isEmpty()) {

						int int_Duration = Integer.parseInt(et_duration.getText().toString());
						String hex_Duration = Integer.toHexString(int_Duration);
						String hex_Duration2 = ("00" + hex_Duration).substring(hex_Duration.length());


						Log.d(TAG, "before cmd: hex_duration" + hex_Duration + " hexDuration2: " + hex_Duration2);


						int int_White = Integer.parseInt(et_white.getText().toString());
						String hex_white = Integer.toHexString(int_White);
						String hex_white2 = ("00" + hex_white).substring(hex_white.length());
						Log.d(TAG, "before cmd: hex_white" + hex_white + " hexWhite2: "+ hex_white2);

						int int_Yellow = Integer.parseInt(et_yellow.getText().toString());
						String hex_Yellow = Integer.toHexString(int_Yellow);
						String hex_Yellow2 = ("00" + hex_Yellow).substring(hex_Yellow.length());
						Log.d(TAG, "before cmd: hex_yellow" + hex_Yellow + " hexYellow2: "+ hex_Yellow2);


						String hexarray = "005903" +  hex_Duration2 + "00" + hex_white2 + hex_Yellow2;
						Log.d(TAG, "before cmd hexarray " + hexarray);

						byte [] dataarray = hexStringToByteArray(hexarray);
						Log.d(TAG, "after cmd data array " + Arrays.toString(dataarray));

						target_character.setValue(dataarray);
						mBluetoothLeService.writeCharacteristic(target_character);
						Toast.makeText(Control.this, "S " + target_character, Toast.LENGTH_SHORT).show();
						Log.d(DB, (cmd_duration));
						Log.d(TAG, "sent cmd:" + "array sent data array "  + Arrays.toString(dataarray));
					} else {
						Toast.makeText(Control.this, "Please type your command. No empty fields allowed.", Toast.LENGTH_SHORT).show();
						Log.d(DB, String.valueOf(cmd_duration));
					}
				}else{
					Toast.makeText(Control.this, "Please select a UUID.", Toast.LENGTH_SHORT).show();


				}
			}
		});


		but_stop = (Button) findViewById(R.id.but_stop);
		but_stop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mBluetoothLeService.disconnect();
			}
		});
		but_connect = (Button) findViewById(R.id.but_connect);
		but_connect.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mBluetoothLeService.connect(mDeviceAddress);
			}
		});

	}




	public static byte[] hexStringToByteArray(String s) {

		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
			Log.d(TAG, "sent cmd:" + i + ": "  + (Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
		}
		return data;

	}



	private final ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		
		public void onServiceConnected(ComponentName componentName,
				IBinder service) {
			Log.d(TAG, "start service Connection");
			mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
			if (!mBluetoothLeService.initialize()) {
				Log.e(TAG, "Unable to initialize Bluetooth");
				finish();
			}
			// Automatically connects to the device upon successful start-up
			// initialization.
			Log.d(TAG, "mDeviceAddress = " + mDeviceAddress);
			boolean status = mBluetoothLeService.connect(mDeviceAddress);
			if(status == true){
				Log.d(TAG, "connection OK");
			}else{
				Log.d(TAG, "Connection failed");
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			Log.d(TAG, "end Service Connection");
			mBluetoothLeService = null;
		}
	};

	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "enter BroadcastReceiver");
			final String action = intent.getAction();
			Log.d(TAG, "action = " + action);
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(mConnected);
                System.out.println("BroadcastReceiver :"+"device connected");
              
            } else if(BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(mConnected);

			} else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
				Log.d(TAG, "services discovered!!!");
				//getGattService(mBluetoothLeService.getSupportedGattServices());
				displayGattServices(mBluetoothLeService.getSupportedGattServices());
				startReadRssi();
				//startReadInformation();

			} else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
				Log.d(TAG, "receive data");
                byte[] value = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);

                if(value != null){
                	displayData(value);
                }else{
                	Log.d(TAG, "value = null");
                }
			} else if (BluetoothLeService.ACTION_GATT_RSSI.equals(action)) {
				Log.d(TAG, "BroadCast + RSSI");
				rssi_value = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
				rssi_value += "dB";
				Log.d(TAG, "rssi_value = " + rssi_value);
				updateRSSI(rssi_value);
			}
		}

		private void updateRSSI(String value) {
			// TODO Auto-generated method stub
			if(value != null){
				tv_currentRSSI.setText(value);
			}
		}

		private void updateConnectionState(boolean status) {
			// TODO Auto-generated method stub
			if(status){
				tv_connstatus.setText("connected");
			}else{
				tv_connstatus.setText("unconnected");
			}
		}
	};

	private void startReadRssi() {
		new Thread() {
			public void run() {

				while (true) {
					try {
						mBluetoothLeService.readRssi();
						sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			};
		}.start();
	}
	
	
    private void onCharacteristicsRead(String uuidStr, byte[] value) {
        Log.i(TAG, "onCharacteristicsRead: " + uuidStr);
        if (value != null && value.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(value.length);
            for (byte byteChar : value) {
                stringBuilder.append(String.format("%02X ", byteChar));
            }
            displayData(stringBuilder.toString());
        }
    }
	
	private void displayData(String data) {
		if (data != null) {
			tv_rx.setText(data);
		}
	}
		
	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			mBluetoothLeService.disconnect();
			mBluetoothLeService.close();

			System.exit(0);
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onStop() {
		super.onStop();

		unregisterReceiver(mGattUpdateReceiver);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		mBluetoothLeService.disconnect();
		mBluetoothLeService.close();

		System.exit(0);
	}

	private void displayData(byte[] data) {
		if (data != null) {
			String dataArray = new String(data);
			Log.d(TAG, "data = " + dataArray);
			tv_rx.setText(dataArray);
		}
		
	}
		
	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();

		intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_RSSI);
		return intentFilter;
	}
	
	 private void displayGattServices(List<BluetoothGattService> gattServices) {
		 
		 if (gattServices == null) return;
		 String uuid = "unknown_UUID";
		 String unknownServiceString = "unknown_service";
		 String unknownCharaString = "unknown_characteristic";
		 
		 ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
		 
		 ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        
		 mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
        
		 // Loops through available GATT Services.
		 for (BluetoothGattService gattService : gattServices) {
			 HashMap<String, String> currentServiceData = new HashMap<String, String>();
			 uuid = gattService.getUuid().toString();
			 
			 currentServiceData.put(
					 LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
			 currentServiceData.put(LIST_UUID, uuid);
			 gattServiceData.add(currentServiceData);
            
			 System.out.println("Service uuid:"+uuid);
        	
			 ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            
			 List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            
			 ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();
            
			 // Loops through available Characteristics.
			 for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
				 charas.add(gattCharacteristic);
				 HashMap<String, String> currentCharaData = new HashMap<String, String>();
				 uuid = gattCharacteristic.getUuid().toString();
            
				 currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
				 currentCharaData.put(LIST_UUID, uuid);
                
				 System.out.println("GattCharacteristic uuid:"+uuid);
				// System.out.println("--GattCharacteristic Properties:"+gattCharacteristic.getProperties());
				 mBluetoothLeService.readCharacteristic(gattCharacteristic);
				 // System.out.println("--GattCharacteristic value2:"+gattCharacteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0)); 
				 BluetoothGattDescriptor Descriptor=gattCharacteristic.getDescriptor(gattCharacteristic.getUuid());

				 //System.out.println("--GattCharacteristic Descriptor:"+Descriptor.toString());
                
				 List<BluetoothGattDescriptor> descriptors= gattCharacteristic.getDescriptors();
				 for(BluetoothGattDescriptor descriptor:descriptors){
					 //System.out.println("---descriptor UUID:"+descriptor.getUuid());
					 mBluetoothLeService.getCharacteristicDescriptor(descriptor); 
				 }
				 gattCharacteristicGroupData.add(currentCharaData);
			 }
			 mGattCharacteristics.add(charas);
			 gattCharacteristicData.add(gattCharacteristicGroupData);
		 }
		 SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
				 this,
				 gattServiceData,
				 android.R.layout.simple_expandable_list_item_2,
				 new String[] {LIST_NAME, LIST_UUID},
				 new int[] { android.R.id.text1, android.R.id.text2 },
				 gattCharacteristicData,
				 android.R.layout.simple_expandable_list_item_2,
				 new String[] {LIST_NAME, LIST_UUID},
				 new int[] { android.R.id.text1, android.R.id.text2 }
		);
        lv.setAdapter(gattServiceAdapter);
	 }

	 private final ExpandableListView.OnChildClickListener servicesListClickListner =
		new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                        int childPosition, long id) {
                if (mGattCharacteristics != null) {
                    final BluetoothGattCharacteristic characteristic =
                            mGattCharacteristics.get(groupPosition).get(childPosition);
                    
                    target_character = characteristic;

                    tv_targetUUID.setText(characteristic.getUuid().toString());
                    
                    final int charaProp = characteristic.getProperties();
                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                        // If there is an active notification on a characteristic, clear
                        // it first so it doesn't update the data field on the user interface.
                        if (mNotifyCharacteristic != null) { mBluetoothLeService.setCharacteristicNotification(
                                    mNotifyCharacteristic, false);
                            mNotifyCharacteristic = null;
                        }
                        mBluetoothLeService.readCharacteristic(characteristic);
                    }
                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                        mNotifyCharacteristic = characteristic;
                        mBluetoothLeService.setCharacteristicNotification(
                                characteristic, true);
                    }
                    return true;
                }
                return false;
            }
	    };
}
