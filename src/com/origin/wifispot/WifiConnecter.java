package com.origin.wifispot;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask; 

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.text.format.Formatter;

public class WifiConnecter {

	private static WifiConnecter wificonnecter = null;
	
	//�㲥ע��״̬
	private final int STATE_REGISTRING = 0x01;
	private final int STATE_REGISTERED = 0x02;
	private final int STATE_UNREGISTERING = 0x03;
	private final int STATE_UNREGISTERED = 0x04;	
	private int mHaveRegister = STATE_UNREGISTERED;
	
	//Ԥ�����ּ��ܷ�ʽ
	public static final int TYPE_NO_PASSWD = 0x11;
	public static final int TYPE_WEP = 0x12;
	public static final int TYPE_WPA = 0x13;
	
	//����״̬
	public final int WIFI_CONNECTED = 0x01;
	public final int WIFI_CONNECT_FAILED = 0x02;
	public final int WIFI_CONNECTING = 0x03;
	
	private Context mContext;
	private WifiInfo mWifiInfo;
	private WifiManager mWifiManager;
	private List<String> mWifiList;
	private WifiLock mWifiLock;
	private Timer mTimer = null;
	private int netID = -1;
	private String ssid = null;
	private String serverIP = "";
	
	private WifiStateListener mListener = null;
	
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver()
	{

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if(null == intent || null == intent.getAction()) return;
			
			if (intent.getAction().equals(WifiManager.RSSI_CHANGED_ACTION))
			{
				WifiLOG.d("RSSI_CHANGED_ACTION");
				
				if (isWifiContected(mContext) == WIFI_CONNECTED) 
				{
					if(null != mWifiManager)
					{
						WifiLOG.i("check connetting :"+ssid);
						mWifiInfo = mWifiManager.getConnectionInfo();
						WifiLOG.i("check conneted :"+mWifiInfo.getSSID());
						if(null != ssid && ssid.length()>0 && mWifiInfo.getSSID().indexOf(ssid) != -1)
						{
							WifiLOG.i("WIFI_CONNECTED");
							acquireWifiLock();
							String ip = getIPAddress().toString();
							String _ip[] = ip.split("\\.");
							StringBuilder a = new StringBuilder();
							a.append(_ip[0]).append(".").append(_ip[1]).append(".").append(_ip[2]).append(".").append("1");
							serverIP = a.toString();
							//serverIP = getGateWay(mContext);
							
							//WifiLOG.i("ip: "+ip);
							WifiLOG.i("server ip:  "+serverIP);
							
							callBack(WifiStateListener.WIFI_CONNECT_SUCCESS);
							stopTimer();
							unregist();
						}
						else
						{
							WifiLOG.i("RE_CONNECTED");
							reConnect();
						}
					}
				} 
				else if (isWifiContected(mContext) == WIFI_CONNECT_FAILED) 
				{
					if(null != ssid && ssid.length()>0)
					{
						WifiLOG.i("WIFI_CONNECT_FAILED");
						callBack(WifiStateListener.WIFI_CONNECT_FAILED);
						openWifi();
					}
				} 
				else if (isWifiContected(mContext) == WIFI_CONNECTING) 
				{
					WifiLOG.i("WIFI_CONNECTING");
				}
			}
			else if(intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
			{
				WifiLOG.d("SCAN_RESULTS_AVAILABLE_ACTION");
				if(null != mWifiList)
					mWifiList.clear();
				else
					mWifiList = new ArrayList<String>();
				List<ScanResult> L = mWifiManager.getScanResults();
				
				String ssid = null;
				for(int i = 0; i < L.size(); i++)
				{
					ssid = L.get(i).SSID;
					if(ssid.indexOf(OriginWiFi.SSID_TAG) != -1)
					{
						WifiLOG.i("ssid:  "+ssid);
						mWifiList.add(ssid);
					}
				}
				
				if(null != mWifiList && !mWifiList.isEmpty())
					callBack(WifiStateListener.WIFI_SCAN_SUCCESS);
				L.clear();
				L=null;
			}
		}
	};
	
	private WifiConnecter(Context context)
	{
		this.mContext = context;
		// ȡ��WifiManager����
		mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	}
	
	public static WifiConnecter getInstance(Context context)
	{
		if(null == wificonnecter)
			wificonnecter = new WifiConnecter(context);
		return wificonnecter;
	}
	
	//����
	public void Connect(String ssid)
	{
		WifiLOG.i("connect: "+ssid);
		this.ssid = ssid;
		//closeWifi();
//		int e = WifiAPCreater.closeWifiAp((WifiManager) mContext.getSystemService(Context.WIFI_SERVICE));
//		if(-1 != e)
//			callBack(e);
		WifiConfiguration c = createWifiInfo(ssid);
		netID = mWifiManager.addNetwork(c);
		mWifiManager.enableNetwork(netID, true);
	}
	
	private void reConnect()
	{
		if(null != ssid && ssid.length()>0)
		{
			WifiLOG.i("reConnect to special AP");
			disconnectWifi(mWifiInfo.getNetworkId());
			mWifiManager.enableNetwork(netID, true);
		}
	}
	
	//ɨ��
	public void Scan()
	{				
		if (mTimer != null) {
			stopTimer();
		}
		else
		{
			WifiLOG.i("Close Wifi AP");
			int e = WifiAPCreater.closeWifiAp((WifiManager) mContext.getSystemService(Context.WIFI_SERVICE));
			if(-1 != e)
				callBack(e);
			WifiLOG.i("Open Wifi");
			openWifi();
			//����wifi��
			WifiLOG.i("Create Wifi Lock");
			creatWifiLock();
			//ע��㲥
			regist();
			mTimer = new Timer(true);
			mTimer.schedule(mTimerTask, 1000L ,8000L);
		}
	}
	
	private TimerTask mTimerTask = new TimerTask() 
	{
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			//ɨ���ȵ�
			startScan();
		}
	};
	
	private void stopTimer() 
	{
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}
	}
	
	//���ü���
	public void setListener(WifiStateListener l)
	{
		this.mListener = l;
	}
	
	public void callBack(int type)
	{
		if(null != this.mListener)
			this.mListener.MatrixWifiState(type);
	}
	
	//release
	public void Close()
	{
		try
		{
			unregist();
			
			releaseWifiLock();
			
			disconnectWifi(netID);
			
			stopTimer();
			if(null != mWifiList)
				mWifiList.clear();
			ssid = null;
			mWifiLock = null;
			mWifiList = null;
			mListener = null;
			mWifiManager = null;
			wificonnecter = null;
			//System.gc();
		}catch(Exception e)
		{
			WifiLOG.e("release connect Error: "+e.toString());
		}
	}
	
	private synchronized void regist()
	{
		if (mHaveRegister == STATE_REGISTRING 
				|| mHaveRegister == STATE_REGISTERED) 
		{
			return ;
		}
		WifiLOG.i("regist BroadcastReceiver");
		IntentFilter filter = new IntentFilter();
		filter.addAction(WifiManager.RSSI_CHANGED_ACTION);
		filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		mHaveRegister = STATE_REGISTRING;
		this.mContext.registerReceiver(mBroadcastReceiver, filter);
		mHaveRegister = STATE_REGISTERED;
	}
	
	private synchronized void unregist()
	{
		if (mHaveRegister == STATE_UNREGISTERED 
				|| mHaveRegister == STATE_UNREGISTERING) 
		{
			return ;
		}
		WifiLOG.i("unregist BroadcastReceiver");
		mHaveRegister = STATE_UNREGISTERING;
		this.mContext.unregisterReceiver(mBroadcastReceiver);
		mHaveRegister = STATE_UNREGISTERED;
	}
	
	
	// ��WIFI
	public void openWifi() 
	{
		if (!mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(true);
		}
	}

	// �ر�WIFI
	public void closeWifi() 
	{
		if (mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(false);
		}
	}
	
	//��ʼɨ���ȵ�
	public void startScan() 
	{
		WifiLOG.i("scan wifi ap");
		mWifiManager.startScan();
	}
	
	//��ȡɨ���б�
	public List<String> getScanList()
	{
		return this.mWifiList;
	}
	
	// ����WifiLock
	private void acquireWifiLock() 
	{
		try
		{
		if(null == mWifiLock) return;
		mWifiLock.acquire();
		}catch(Exception e)
		{
			WifiLOG.e("acquireWifiLock  :"+e.toString());
		}
	}

	// ����WifiLock
	private void releaseWifiLock() 
	{
		try
		{
		if(null == mWifiLock) return;
		// �ж�ʱ������
		if (mWifiLock.isHeld()) {
			mWifiLock.release();
		}
		}catch(Exception e)
		{
			WifiLOG.e("releaseWifiLock  :"+e.toString());
		}
	}

	// ����һ��WifiLock
	private void creatWifiLock() 
	{
		WifiLOG.i("creating wifilock");
		mWifiLock = mWifiManager.createWifiLock("Matrix");
		WifiLOG.i("create wifilock success");
	}
	
	//����WifiConfiguration
	private WifiConfiguration createWifiInfo(String SSID) 
	{
		WifiLOG.v("create WifiConfiguration");
		String password = "88888888";
		int type = WifiConnecter.TYPE_WPA;
		if(SSID.indexOf("HTC")!=-1)
		{
			WifiLOG.v("connect to HTC!");
			type = WifiConnecter.TYPE_NO_PASSWD;
		}
		
		WifiConfiguration config = new WifiConfiguration();
		config.allowedAuthAlgorithms.clear();
		config.allowedGroupCiphers.clear();
		config.allowedKeyManagement.clear();
		config.allowedPairwiseCiphers.clear();
		config.allowedProtocols.clear();
		config.SSID = "\"" + SSID + "\"";

		WifiConfiguration tempConfig = this.IsExsits(SSID);
		if (tempConfig != null) {
			mWifiManager.removeNetwork(tempConfig.networkId);
		}
		
		// ��Ϊ���������1û������2��wep����3��wpa����
		if (type == TYPE_NO_PASSWD) {// WIFICIPHER_NOPASS
			WifiLOG.v("connect with TYPE_NO_PASSWD");
			config.wepTxKeyIndex = 0;
			config.allowedPairwiseCiphers.set(1);
			config.allowedPairwiseCiphers.set(2);
			config.allowedProtocols.set(1);
			config.allowedProtocols.set(0);
			config.allowedKeyManagement.set(0);
			config.allowedGroupCiphers.set(0);
			config.allowedGroupCiphers.set(1);
			config.allowedGroupCiphers.set(2);
			config.allowedGroupCiphers.set(3);
			config.preSharedKey = null;
		} else if (type == TYPE_WEP) {  //  WIFICIPHER_WEP 
			WifiLOG.v("connect with TYPE_WEP");
			config.hiddenSSID = true;
			config.wepKeys[0] = "\"" + password + "\"";
			config.allowedAuthAlgorithms
					.set(WifiConfiguration.AuthAlgorithm.SHARED);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
			config.allowedGroupCiphers
					.set(WifiConfiguration.GroupCipher.WEP104);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			config.wepTxKeyIndex = 0;
		} else if (type == TYPE_WPA) {   // WIFICIPHER_WPA
			WifiLOG.v("connect with TYPE_WPA");
			config.preSharedKey = "\"" + password + "\"";
			config.hiddenSSID = false;
			config.allowedAuthAlgorithms
					.set(WifiConfiguration.AuthAlgorithm.OPEN);
			
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
			config.allowedPairwiseCiphers
					.set(WifiConfiguration.PairwiseCipher.TKIP);
			config.allowedPairwiseCiphers
					.set(WifiConfiguration.PairwiseCipher.CCMP);
			// config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
			config.allowedGroupCiphers.set(0);
			config.allowedGroupCiphers.set(1);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			
			config.status = WifiConfiguration.Status.ENABLED;
		} 
		
		return config;
	}
	
	private WifiConfiguration IsExsits(String SSID) 
	{
		List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
		if(null != existingConfigs)
			for (WifiConfiguration existingConfig : existingConfigs) {
				if (existingConfig.SSID.equals("\"" + SSID + "\"") /*&& existingConfig.preSharedKey.equals("\"" + password + "\"")*/) {
					return existingConfig;
				}
			}
		return null;
	}
	
	//�ж��Ƿ����ӳɹ�
	private int isWifiContected(Context context) 
	{
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifiNetworkInfo = connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		
		if (wifiNetworkInfo.getDetailedState() == DetailedState.OBTAINING_IPADDR
				|| wifiNetworkInfo.getDetailedState() == DetailedState.CONNECTING) {
			return WIFI_CONNECTING;
		} else if (wifiNetworkInfo.getDetailedState() == DetailedState.CONNECTED) {
			return WIFI_CONNECTED;
		} else {
			return WIFI_CONNECT_FAILED;
		}
	}
	
	//���ػ�ȡ  
    public String getGateWay(Context context)
    { 
    	WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE); 
    	DhcpInfo dhcpInfo = wifiManager.getDhcpInfo(); 
         
        //dhcpInfo��ȡ�������һ�γɹ��������Ϣ���������ء�ip��  
    	//FormatIP(dhcpInfo.ipAddress)
        return FormatIP(dhcpInfo.gateway);      
    }
	
	// IP��ַת��Ϊ�ַ�����ʽ  
    public String FormatIP(int IpAddress) 
    { 
    	return Formatter.formatIpAddress(IpAddress); 
    } 
	
	// �õ�IP��ַ
	public String getIPAddress() 
	{
		int ipAddress = (mWifiInfo == null) ? 0 : mWifiInfo.getIpAddress();
		if(0 == ipAddress) return null;
		return  ((ipAddress & 0xff)+"."+(ipAddress>>8 & 0xff)+"."  
                +(ipAddress>>16 & 0xff)+"."+(ipAddress>>24 & 0xff));  
	}
	
	// �Ͽ�ָ��ID������
	private void disconnectWifi(int netId)
	{
		if(-1 == netId) return;
		WifiLOG.i("disconnectWifi netId: "+netId);
		mWifiManager.disableNetwork(netId);
		mWifiManager.disconnect();
	}
	
	//��ȡ�����ȵ���豸��ip
	public String getServerIP()
	{
		return this.serverIP;
	}
}
