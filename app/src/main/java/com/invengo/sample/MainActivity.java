package com.invengo.sample;

import invengo.javaapi.core.BaseReader;
import invengo.javaapi.core.BaseReader.ReaderChannelType;
import invengo.javaapi.core.IMessageNotification;
import invengo.javaapi.core.Util;
import invengo.javaapi.handle.IMessageNotificationReceivedHandle;
import invengo.javaapi.protocol.IRP1.PowerOff;
import invengo.javaapi.protocol.IRP1.RXD_TagData;
import invengo.javaapi.protocol.IRP1.ReadTag;
import invengo.javaapi.protocol.IRP1.ReadTag.ReadMemoryBank;
import invengo.javaapi.protocol.IRP1.Reader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.invengo.lib.diagnostics.InvengoLog;

@SuppressLint("NewApi")
public class MainActivity extends Activity implements
		IMessageNotificationReceivedHandle {

	private static final String TAG = MainActivity.class.getSimpleName();
	private Button connectBtn;
	private Button disconnectBtn;
	private Button readBtn;
	private Button queryBtn;
	private Button stopBtn;
	private Button clearBtn;
	private Reader reader;

	private ListView mEpcListView;
	private EPCEntityAdapter mListAdapter;
	private List<EPCEntity> mEPCEntityList = new ArrayList<EPCEntity>();
	private View mConnectStatusView;
	
	private MainActivity mainActivity;
	private Spinner mDeviceSpinner;
	private static final int SPINNER_STYLE = android.R.layout.simple_spinner_dropdown_item;
	private BluetoothAdapter mAdapter;
	private boolean isExit = false;
	
	private boolean isReading;
	private boolean isConnected;
	private ConnectReaderTask mConnectTask;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mainActivity = this;
		
		mDeviceSpinner = (Spinner) findViewById(R.id.deviceSpinner);
		connectBtn = (Button) findViewById(R.id.connectBtn);
		mConnectStatusView = findViewById(R.id.connect_status);
		
		BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mAdapter = bluetoothManager.getAdapter();
		if(!mAdapter.isEnabled()){
			openBluetooth();
		}
		
		
		disconnectBtn = (Button) findViewById(R.id.disconnectBtn);
		disconnectBtn.setEnabled(true);
		
		readBtn = (Button) findViewById(R.id.readBtn);
		readBtn.setEnabled(false);
		
		queryBtn = (Button) findViewById(R.id.queryButton);
		queryBtn.setEnabled(true);

		stopBtn = (Button) findViewById(R.id.stopBtn);
		stopBtn.setEnabled(false);
		
		clearBtn = (Button) findViewById(R.id.clearButton);
		clearBtn.setEnabled(false);
		
		mListAdapter = new EPCEntityAdapter(this, R.layout.listview_epc_off_rssi_item, mEPCEntityList);
		mEpcListView = (ListView) findViewById(R.id.dataSet);
		mEpcListView.setAdapter(mListAdapter);
		
		addListener();
	}
	
	private void addListener() {
		connectBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				String address = ((String) mDeviceSpinner.getSelectedItem()).split("\\|")[1];
				connectReader(address);
			}
		});

		disconnectBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				new Thread(new Runnable(){

					@Override
					public void run() {
						if (isConnected) {
							isReading = false;
							reader.send(new PowerOff());
							reader.disConnect();
							Message disconnectMsg = new Message();
							disconnectMsg.what = DISCONNECT;
							cardOperationHandler.sendMessage(disconnectMsg);
						}
					}
					
				}).start();
			}
		});
		
		readBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				new Thread(new Runnable() {
					public void run() {
						if(isConnected){
							isReading = true;
							ReadTag readTag = new ReadTag(ReadMemoryBank.EPC_6C);
							boolean result = reader.send(readTag);
							Message readMessage = new Message();
							readMessage.what = START_READ;
							readMessage.obj = result;
							cardOperationHandler.sendMessage(readMessage);
						}
					}
				}).start();
			}
		});
		
		stopBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						if(isConnected){
							isReading = false;
							boolean result = reader.send(new PowerOff());
							Message powerOffMsg = new Message();
							powerOffMsg.what = STOP_READ;
							powerOffMsg.obj = result;
							cardOperationHandler.sendMessage(powerOffMsg);
						}
					}
				}).start();
			}
		});
		
		clearBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mEPCEntityList.clear();
				mListAdapter.notifyDataSetChanged();
			}
		});

//		++++++++++++++++++++++++++++++++++++++
		queryBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				Intent intent = new Intent(MainActivity.this,Detail.class);
				startActivity(intent);


			}
		});

//   +++++++++++++++++++++++++++++++++++++++++++++


	}

	private void openBluetooth() {
		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, R.layout.myspinner);
		spinnerArrayAdapter.setDropDownViewResource(SPINNER_STYLE);
		spinnerArrayAdapter.add(getString(R.string.device_not_found));
		mDeviceSpinner.setAdapter(spinnerArrayAdapter);
		new AlertDialog.Builder(this)
				.setTitle(R.string.alert_dialog_title)
				.setMessage(R.string.alert_dialog_message)
				.setPositiveButton(R.string.alert_dialog_button_yes,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								Intent openBluetoothIntent = new Intent(
										Settings.ACTION_BLUETOOTH_SETTINGS);
								startActivity(openBluetoothIntent);
							}
						})
				.setNegativeButton(R.string.alert_dialog_button_NO,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
							}
						}).show();
	}
	
	private void initBleDevice() {
		Set<BluetoothDevice> devices = mAdapter.getBondedDevices();
		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, R.layout.myspinner);
		spinnerArrayAdapter.setDropDownViewResource(SPINNER_STYLE);
		int selectedPosition = 0;
		if(devices.isEmpty()){//no-bonded devices
			spinnerArrayAdapter.add(getString(R.string.device_not_found));
			showToast(getString(R.string.toast_bluetooth_close));
		}else{//
			Iterator<BluetoothDevice> deviceIterator = devices.iterator();
			while (deviceIterator.hasNext()) {
				BluetoothDevice device = deviceIterator.next();
				String deviceInfo = device.getName() + "|" + device.getAddress();
				spinnerArrayAdapter.add(deviceInfo);
			}
		}
		mDeviceSpinner.setSelection(selectedPosition);
		mDeviceSpinner.setAdapter(spinnerArrayAdapter);
	}
	
	private boolean isRun = true;
	private void disconnect() {
		isRun = false;
		connectBtn.setEnabled(true);
		disconnectBtn.setEnabled(false);
		readBtn.setEnabled(false);
		queryBtn.setEnabled(false);
		stopBtn.setEnabled(false);
		clearBtn.setEnabled(false);
	}

	@Override
	public void messageNotificationReceivedHandle(BaseReader reader,
			IMessageNotification msg) {
		if(isReading){
			if(isConnected){
				if (msg instanceof RXD_TagData) {
					RXD_TagData data = (RXD_TagData) msg;
					String epc = Util.convertByteArrayToHexString(data.getReceivedMessage().getEPC());
					
					boolean isExists = false;
					for(EPCEntity entity : mEPCEntityList){
						String old = entity.getEpcData();
						if(epc.equals(old)){
							isExists = true;
							int oldNumber = entity.getNumber();
							entity.setNumber(oldNumber + 1);
							break;
						}
					}
					
					if(isExists){
						//do nothing
					}else{
						EPCEntity newEntity = new EPCEntity();
						newEntity.setNumber(1);
						newEntity.setEpcData(epc);
						mEPCEntityList.add(newEntity);
					}
					
					Message dataArrivedMsg = new Message();
					dataArrivedMsg.what = DATA_ARRIVED;
					cardOperationHandler.sendMessage(dataArrivedMsg);
				}
			}
		}
	}

	private boolean backDown;
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		InvengoLog.i(TAG, "INFO.onKeyDown().");
		if (keyCode == KeyEvent.KEYCODE_BACK && !backDown) {
			backDown = true;
		}else if((keyCode == KeyEvent.KEYCODE_SHIFT_LEFT
				|| keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT
				|| keyCode == KeyEvent.KEYCODE_SOFT_RIGHT)
				&& event.getRepeatCount() <= 0
				&& isConnected){
			InvengoLog.i(TAG, "INFO.Start/Stop read tag.");	
			if(isReading == false){
				isReading = true;
				ReadTag readTag = new ReadTag(ReadMemoryBank.EPC_6C);
				boolean result = reader.send(readTag);
				Message readMessage = new Message();
				readMessage.what = START_READ;
				readMessage.obj = result;
				cardOperationHandler.sendMessage(readMessage);
			}else if(isReading == true){
				isReading = false;
				boolean result = reader.send(new PowerOff());
				Message powerOffMsg = new Message();
				powerOffMsg.what = STOP_READ;
				powerOffMsg.obj = result;
				cardOperationHandler.sendMessage(powerOffMsg);
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && backDown) {
			backDown = false;
			if(!isExit){
				isExit = true;
				showToast(getString(R.string.toast_exit));
				Message msg = new Message();
				msg.what = EXIT;
				cardOperationHandler.sendMessageDelayed(msg, 1 * 1000);
			}else {
				finish();
			}
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		initBleDevice();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		reader.disConnect();
	}

	private void showToast(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	private void connectReader(String address) {
		mConnectTask = new ConnectReaderTask();
		mConnectTask.execute(address);
//		new Thread(new ConnectRunnable(address)).start();
	}
	
	private static final int START_READ = 0;
	private static final int STOP_READ = 1;
	private static final int DATA_ARRIVED = 2;
	private static final int DISCONNECT = 3;
	private static final int EXIT = 4;
	@SuppressLint("HandlerLeak")
	private Handler cardOperationHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			int what = msg.what;
			switch (what) {
			case START_READ:
				boolean start = (Boolean) msg.obj;
				if (start) {
					showToast(getString(R.string.send_read_card_success));
					readBtn.setEnabled(false);
					clearBtn.setEnabled(false);
				} else {
					showToast(getString(R.string.send_read_card_failed));
				}
				break;
			case STOP_READ:
				
				boolean stop = (Boolean) msg.obj;
				if (stop) {
					showToast(getString(R.string.stop_read_card));
					readBtn.setEnabled(true);
					clearBtn.setEnabled(true);
				} else {
					showToast(getString(R.string.stop_read_card));
				}
				break;
			case DATA_ARRIVED://
				((EPCEntityAdapter)mEpcListView.getAdapter()).notifyDataSetChanged();
				break;
			case DISCONNECT://
				disconnect();
				reader.onMessageNotificationReceived.remove(mainActivity);
				isConnected = false;
				showToast(getString(R.string.diss_connected));
				break;
			case EXIT:
				isExit = false;
				break;
			default:
				break;
			}
		};
	};
	
	private class ConnectReaderTask extends AsyncTask<String, Void, Boolean>{
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showProgress(true);
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			String address = params[0];
			reader = new Reader("Reader1", "Bluetooth", address);
			reader.setChannelType(ReaderChannelType.RFID_CHANNEL_TYPE);
			if (reader.connect()) {
				reader.onMessageNotificationReceived.add(mainActivity);
				isConnected = true;
			} else {
				isConnected = false;
				return false;
			}
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			mConnectTask = null;
			showProgress(false);
			if(result){
				connectBtn.setEnabled(false);
				disconnectBtn.setEnabled(true);
				readBtn.setEnabled(true);
				stopBtn.setEnabled(true);
				clearBtn.setEnabled(true);
				queryBtn.setEnabled(true);
				showToast(getString(R.string.connected_rebuild));
			}else{
				connectBtn.setEnabled(true);
				disconnectBtn.setEnabled(false);
				readBtn.setEnabled(false);
				stopBtn.setEnabled(false);
				clearBtn.setEnabled(false);
				queryBtn.setEnabled(false);
				showToast(getString(R.string.connected_not_rebuild));
			}
		}
		
		@Override
		protected void onCancelled() {
			super.onCancelled();
			mConnectTask = null;
			showProgress(false);
		}
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	private void showProgress(final boolean show){
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mConnectStatusView.setVisibility(View.VISIBLE);
			mConnectStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mConnectStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
						}
					});
		} else {
			mConnectStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
		}
	}
	
	private class EPCEntityAdapter extends ArrayAdapter<EPCEntity>{

		private int resourceId;
		public EPCEntityAdapter(Context context, int textViewResourceId,
				List<EPCEntity> objects) {
			super(context, textViewResourceId, objects);
			this.resourceId = textViewResourceId;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			EPCEntity entity = getItem(position);
			
			View view;
			ViewHolder holder;
			if(null == convertView){
				view = LayoutInflater.from(getContext()).inflate(resourceId, null);
				holder = new ViewHolder();
				holder.number = (TextView) view.findViewById(R.id.epc_off_rssi_times);
				holder.epcData = (TextView) view.findViewById(R.id.epc_off_rssi_data);
				view.setTag(holder);
			}else{
				view = convertView;
				holder = (ViewHolder) view.getTag();
			}
			
			holder.number.setText(String.valueOf(entity.getNumber()));
			holder.epcData.setText(entity.getEpcData());
			
			return view;
		}
		
		class ViewHolder{
			TextView number;
			TextView epcData;
		}
	}
	
	@Deprecated
	private class ConnectRunnable implements Runnable{
		
		private String hostName;
		private int port;
		private Socket client;
		private int timeout = 1000;
		private OutputStream writer;
		private InputStream reader;
		private byte[] inputMsg = new byte[1024];
		public ConnectRunnable(String address) {
			this.hostName = address.substring(0, address.indexOf(':'));
			this.port = Integer.parseInt(address.substring(address.indexOf(':') + 1));
		}
		
		@Override
		public void run() {
			isRun = true;
			try {
				client = new Socket();
				InetSocketAddress remoteAddr = new InetSocketAddress(this.hostName, this.port); 
				client.connect(remoteAddr, timeout);
				
				writer = client.getOutputStream();
				reader = client.getInputStream();
				
				byte[] msg = new byte[]{0x00, 0x02, (byte) 0xD2, 0x00, (byte) 0xEC, 0x24};
				writer.write(msg,0, msg.length);
				writer.flush();
				
				Thread.sleep(500);
				
				while(isRun){
					if(client.isConnected()){
						int hasData = reader.read(inputMsg, 0, inputMsg.length);
						if(hasData > 0){
							byte[] temp = new byte[inputMsg.length];
							System.arraycopy(inputMsg, 0, temp, 0, inputMsg.length);
							Log.i(getLocalClassName(), "Data:" + convertByteArrayToHexString(temp));
						}
					}else{
						Log.w(getLocalClassName(), "Socket disconnect!");
						isRun = false;
					}
				}
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}finally{
				try {
					if (writer != null) {
						writer.close();
					}
					if (reader != null) {
						reader.close();
					}
					if (client != null) {
						client.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	@Deprecated
	private String convertByteArrayToHexString(byte[] byte_array) {
		String s = "";

		if (byte_array == null)
			return s;

		for (int i = 0; i < byte_array.length; i++) {
			String hex = Integer.toHexString(byte_array[i] & 0xff);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			s = s + hex;
		}
		return s.toUpperCase();
	}


}
