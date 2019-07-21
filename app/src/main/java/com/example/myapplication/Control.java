package com.example.myapplication;

import java.io.OptionalDataException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import android.app.ActionBar;
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
import android.util.Xml;
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
	private static String targetchar =  "0000F001-0000-1000-8000-00805F9B34FB";
	private final static String UUID_KEY_DATA = "0000F001-0000-1000-8000-00805F9B34FB";
//	private BluetoothGattCharacteristic characteristicTXRX = null;
	public static final String EXTRAS_DEVICE = "EXTRAS_DEVICE";
    private String mDeviceName = null;
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

	Button but_send1;
	TextView tv_deviceName,tv_deviceAddr,tv_connstatus,tv_currentRSSI,tv_targetUUID,tv_rx;
	EditText et_duration,et_white,et_yellow;
	ExpandableListView lv;

	String hex_Whitevalue;


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
		//getActionBar().setTitle(mDeviceName);
		//getActionBar().setDisplayHomeAsUpEnabled(true);

		// getActionBar().setTitle(mTitle +menutitles[0]);
		Log.d(TAG, "start BluetoothLE Service");
		
		Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
		bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

		but_send1 = (Button) findViewById(R.id.but_send);
		but_send1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(target_character != null) {
					String cmd_duration = "0x"+ et_duration.getText().toString();
//					int cmd_white = Integer.parseInt(et_white.getText().toString());
					String cmd_white = "0x"+et_white.getText().toString();
					Log.d(TAG, "before cmd" + cmd_white);
//					hex_Whitevalue = Integer.toHexString(cmd_white);
					Log.d(TAG, "after cmd:" + hex_Whitevalue);
					byte [] i = cmd_white.getBytes();
					//String wled= "0x"+hex_Whitevalue;
					Log.d(TAG, "after cmd2:" + Arrays.toString(i));
					String cmd_yellow = "0x"+et_yellow.getText().toString();
					Log.d(TAG, "send cmd:" + cmd_duration);
					if (cmd_duration != null) {

						byte b = 0x00;
						byte c = 0x59;
						byte d = 0x03;
						byte e = 0x03;
						byte f = 0x00;
						byte g = (byte) 0xFF;
//						byte g = Byte.parseByte(wled);
						Log.d(TAG, "after cmd3:" + g);
						byte h = (byte) 0xFF;
						byte[] tmp = cmd_duration.getBytes();
						byte[] tx = new byte[tmp.length];
//								byte[] tx = new byte[tmp.length + 1];
						tx[0] = b;
						tx[1] = c;
						tx[2] = d;
						tx[3] = e;
						tx[4] = f;
						tx[5] = g;
						tx[6] = h;
						//		for (int i = 1; i < tmp.length + 1; i++) {
						//			tx[i] = tmp[i - 1];

						//		}
						//		for (int i = 0; i < tmp.length + 1; i++) {
						//			tx[i] = tmp[i - 1];

						//		}



						target_character.setValue(tx);
						//		Log.i(TAG, "data " + cmd);

						//		target_character.setValue(cmd);
						Log.d(TAG, "send cmd2:" + cmd_duration);

						mBluetoothLeService.writeCharacteristic(target_character);
						//et_duration.setText(cmd_duration);
//						et_send.setText("0000F001-0000-1000-8000-00805F9B34FB");
						Toast.makeText(Control.this, "S " + target_character, Toast.LENGTH_SHORT).show();
						Log.d(DB, (cmd_duration));
						Log.d(TAG, "sent cmd:" + cmd_duration + " "  + Arrays.toString(tmp) + " " + Arrays.toString(tx));

					} else {
						Toast.makeText(Control.this, "Please type your command.", Toast.LENGTH_SHORT).show();
						Log.d(DB, String.valueOf(cmd_duration));
					}
				}else{
					Toast.makeText(Control.this, "Please select a UUID." + targetchar, Toast.LENGTH_SHORT).show();

					Log.d(DB, String.valueOf(targetchar));

				}
			}

		});

	}




		/* btn_write.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(target_character != null) {
					String cmd = et_send.getText().toString();
					Log.d(TAG, "send cmd:" + cmd);
					if (cmd != null) {

					//	byte[] value = new byte[1];
					//	value[0] = (byte) (21 & 0xFF);
					//	target_character.setValue(value);
					//	mBluetoothLeService.writeCharacteristic(target_character);

						try {
							Log.i(TAG, "data " + URLEncoder.encode(, "utf-8"));

							target_character.setValue(URLEncoder.encode(target_character, "utf-8"));

							// TODO
							mBluetoothLeService.writeCharacteristic(target_character);
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}




						Toast.makeText(Control.this, "A" + target_character, Toast.LENGTH_SHORT).show();



					} else {
						Toast.makeText(Control.this, "Please type your command.", Toast.LENGTH_SHORT).show();
						Log.d(DB, String.valueOf(cmd));
					}
				}else{
					Toast.makeText(Control.this, "Please select a UUID." + characteristicTXRX, Toast.LENGTH_SHORT).show();
					et_send.setText((CharSequence) target_character);
					Log.d(DB, String.valueOf(characteristicTXRX));

				}
				et_send.setText(null);
			}

		}); */



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
