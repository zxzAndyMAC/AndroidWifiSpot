package com.origin.wifispot;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

import org.apache.http.conn.util.InetAddressUtils;
 

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

/**
 * wifi�ȵ㴴��
 * @author 51
 *
 */

public class WifiAPCreater {
	private static final String TAG = "APcreate__";
	private static WifiAPCreater wifiapcreater = null;
	
	private WifiManager mWifiManager = null;	
	private Context mContext = null;
	private int mTimeCount=15 ,mTimeSleep=1000;
	private WifiStateListener mListener;
	private static boolean trysecond = false;
	private WifiApManager ApManger;
	private WifiTimerChecker timerCheck = null;
	public boolean mOriWifiState = false;
	
	/*
	 * ����
	 */
	private WifiAPCreater(Context context) 
	{
		mContext = context;		
		mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);  	
		mOriWifiState = mWifiManager.isWifiEnabled();
		closeWifiAp(mWifiManager);
		ApManger = new WifiApManager(mWifiManager);
	}
	
	/*
	 * ��ȡΨһʵ��
	 */
	public static WifiAPCreater getInstance(Context context)
	{
		if(null == wifiapcreater)
			wifiapcreater = new WifiAPCreater(context);
		return wifiapcreater;
	}
	
	/*
	 * �ر��ȵ㣬������Դ
	 */
	public void closeWifiAp(Context context) 
	{
		try
		{
			if(null != timerCheck)
			{
				timerCheck.exit();
				timerCheck = null;
			}
			trysecond = false;
			WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE); 
			int e = closeWifiAp(wifiManager);
			
			if(-1 != e)
				sendState(e);
			
			if(mOriWifiState)
			{
				WifiLOG.i("reStart wifi");
				mWifiManager.setWifiEnabled(true);
			}
			
			mListener = null;
			mWifiManager = null;
			mContext = null;
			wifiapcreater = null;
			//System.gc();
		}catch(Exception e)
		{
			WifiLOG.e("release AP Error: "+e.toString());
		}
	}
	
	/*
	 * ���ü���
	 */
	public void setListener(WifiStateListener l)
	{
		this.mListener = l;
	}
	
	/*
	 * ����״̬
	 */
	private void sendState(int type)
	{
		if(null != this.mListener)
			this.mListener.MatrixWifiState(type);
	}
	
	/*
	 * ���ó�ʱ
	 */
	public void setTimeOut(int count ,int sleep)
	{
		this.mTimeCount = count;
		this.mTimeSleep = sleep;
	}
	
	/*
	 * ����wifi�ȵ�
	 */
	public void startWifiAp(final String name) 
	{
		closeWifiAp(mWifiManager);
		
		String mSSID = OriginWiFi.SSID_TAG+"_"+name+"_"+getIMSI();
		String mPasswd = "88888888";
		
		if (mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(false);
		} 
		
		getWifiApFunc(mSSID ,mPasswd);
		
		timerCheck = new WifiTimerChecker() {
			
			@Override
			public void doTimerCheckWork() {
				// TODO Auto-generated method stub
				
				if (isWifiApEnabled(mWifiManager)) {
					WifiLOG.v("Wifi enabled success!");
					trysecond = false;
					if(null != mListener)
						mListener.MatrixWifiState(WifiStateListener.WIFI_APCREATE_SUCCESS);
					this.exit();
				} else {
					WifiLOG.v("Wifi enabled failed!");
				}
			}

			@Override
			public void doTimeOutWork() {
				// TODO Auto-generated method stub
				if(!trysecond)
				{
					WifiLOG.v("Wifi enabled failed,try second time");
					trysecond = true;
					//closeWifiAp(mWifiManager);
					startWifiAp(name);
				}
				else if(null != mListener)
				{
					WifiLOG.v("Wifi enabled failed,callback");
					mListener.MatrixWifiState(WifiStateListener.WIFI_APCREATE_FAILED);
				}
				this.exit();
			}
		};
		timerCheck.start(mTimeCount, mTimeSleep);
		
	}

	/*
	 * �����ȡfunc
	 * @param mSSID
	 * @param mPasswd
	 */
	public void getWifiApFunc(String mSSID ,String mPasswd) 
	{
		String model = getPhoneModel();
		boolean htc = false;
		if(model.indexOf("HTC") != -1 || model.indexOf("htc") != -1)
		{
			WifiLOG.i("It`s a HTC Phone!!");
			htc = true;
		}
		
		
		try {
//            Field[] fields = localObject2.getClass().getDeclaredFields();
//            for(int t=0;t<fields.length;t++)
//            {
//            	WifiLOG.d("fields["+t+"]: "+fields[t].getName());
//            }
						
			WifiConfiguration configuration = null;  
			
			if(!htc)
			{
				configuration = ApManger.getWifiApConfiguration();
				WifiLOG.i("configuration :"+configuration);
				
				if(null == configuration)
				{
					configuration = new WifiConfiguration();
					configuration.hiddenSSID = false;
					configuration.allowedAuthAlgorithms
							.set(WifiConfiguration.AuthAlgorithm.OPEN);
					configuration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
					configuration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
					configuration.allowedKeyManagement
							.set(WifiConfiguration.KeyMgmt.WPA_PSK);
					configuration.allowedPairwiseCiphers
							.set(WifiConfiguration.PairwiseCipher.CCMP);
					configuration.allowedPairwiseCiphers
							.set(WifiConfiguration.PairwiseCipher.TKIP);
					configuration.allowedGroupCiphers
							.set(WifiConfiguration.GroupCipher.CCMP);
					configuration.allowedGroupCiphers
							.set(WifiConfiguration.GroupCipher.TKIP);
					configuration.allowedGroupCiphers.set(1);
					configuration.allowedGroupCiphers.set(0);				
				}
				configuration.SSID = mSSID;
				configuration.preSharedKey = mPasswd;
				ApManger.setWifiApEnabled(configuration, true);
			}
			else if(htc)
			{
				configuration = ApManger.getWifiApConfiguration();
				WifiLOG.i("configuration :"+configuration);
				configuration.SSID = mSSID+"HTC";
				configuration.preSharedKey = mPasswd;
				ApManger.setWifiApEnabled(configuration, true);			
			}
//		} catch (IllegalArgumentException e) {
//			// TODO Auto-generated catch block
//			WifiLOG.e(TAG+"IllegalArgumentException 3: "+e.toString());
//			sendState(WifiStateListener.WIFI_METHOD_ERROR);
//		} catch (IllegalAccessException e) {
//			// TODO Auto-generated catch block
//			WifiLOG.e(TAG+"IllegalAccessException 3: "+e.toString());
//			sendState(WifiStateListener.WIFI_METHOD_ERROR);
//		} catch (InvocationTargetException e) {
//			// TODO Auto-generated catch block
//			WifiLOG.e(TAG+"InvocationTargetException 3: "+e.toString());
//			sendState(WifiStateListener.WIFI_METHOD_ERROR);
//		} catch (SecurityException e) {
//			// TODO Auto-generated catch block
//			WifiLOG.e(TAG+"SecurityException 3: "+e.toString());
//			sendState(WifiStateListener.WIFI_METHOD_ERROR);
//		} catch (NoSuchMethodException e) {
//			// TODO Auto-generated catch block
//			WifiLOG.e(TAG+"NoSuchMethodException 3: "+e.toString());
//			sendState(WifiStateListener.WIFI_METHOD_ERROR);
		} catch (Exception e) {
			WifiLOG.e(TAG+"Exception 3: "+e.toString());
			sendState(WifiStateListener.WIFI_METHOD_ERROR);
		}
	}
	
    public boolean setWifiApConfig(WifiConfiguration config) {
        try {
            Method method = mWifiManager.getClass().getMethod(
                    "setWifiApConfiguration",WifiConfiguration.class);
            return (Boolean) method.invoke(mWifiManager, config);
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }
	
	public static int closeWifiAp(WifiManager wifiManager) 
	{
		if (isWifiApEnabled(wifiManager)) {
			try {
				Method method = wifiManager.getClass().getMethod("getWifiApConfiguration");
				method.setAccessible(true);

				WifiConfiguration config = (WifiConfiguration) method.invoke(wifiManager);

				Method method2 = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
				method2.invoke(wifiManager, config, false);
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				WifiLOG.e(TAG+"NoSuchMethodException: "+e.toString());
				return WifiStateListener.WIFI_CLOSE_ERROR;
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				WifiLOG.e(TAG+"IllegalArgumentException: "+e.toString());
				return WifiStateListener.WIFI_CLOSE_ERROR;
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				WifiLOG.e(TAG+"IllegalAccessException: "+e.toString());
				return WifiStateListener.WIFI_CLOSE_ERROR;
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				WifiLOG.e(TAG+"InvocationTargetException: "+e.toString());
				return WifiStateListener.WIFI_CLOSE_ERROR;
			} catch (Exception e) {
				WifiLOG.e(TAG+"Exception: "+e.toString());
				return WifiStateListener.WIFI_CLOSE_ERROR;
			}
		}
		return -1;
	}
	
	private static boolean isWifiApEnabled(WifiManager wifiManager) 
	{
		try {
			Method method = wifiManager.getClass().getMethod("isWifiApEnabled");
			method.setAccessible(true);
			return (Boolean) method.invoke(wifiManager);

		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			WifiLOG.e(TAG+"NoSuchMethodException 2: "+e.toString());
		} catch (Exception e) {
			WifiLOG.e(TAG+"Exception 2: "+e.toString());
		}

		return false;
	}
	
	/*   
	 * Ψһ���û�ID��   
	 * ���磺IMSI(�����ƶ��û�ʶ����) for a GSM phone.   
	 * ��ҪȨ�ޣ�READ_PHONE_STATE   
	 */  	
	private String getIMSI()
	{
		String NSC = "NSC"+System.currentTimeMillis();
		TelephonyManager tm = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);  
		if(TelephonyManager.SIM_STATE_ABSENT == tm.getSimState())
			//return "no SIM";
			return NSC;
		else if(TelephonyManager.SIM_STATE_PIN_REQUIRED == tm.getSimState())
			//return "locked need PIN";
			return NSC;
		else if(TelephonyManager.SIM_STATE_PUK_REQUIRED == tm.getSimState())
			//return "locked need PUK";
			return NSC;
		else if(TelephonyManager.SIM_STATE_NETWORK_LOCKED == tm.getSimState())
			//return "locked need PIN";
			return NSC;
		else if(TelephonyManager.SIM_STATE_UNKNOWN == tm.getSimState())
			//return "UNKNOWN";
			return NSC;
		else
		{
			String nsc = tm.getSubscriberId();
			if(nsc.length()<7)
				return NSC;
			else
				return nsc;
		}
	}
	
	public String getlocalip(boolean useIPv4)
	{  
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress().toUpperCase();
                        boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr); 
                        if (useIPv4) {
                            if (isIPv4) 
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 port suffix
                                return delim<0 ? sAddr : sAddr.substring(0, delim);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) { } // for now eat exceptions
        return "";
    }
	
	/*
	 * ��ȡ�ֻ��ͺ�
	 */
	public String getPhoneModel()
	{
		String model = android.os.Build.MANUFACTURER + "_" + android.os.Build.MODEL;
		model = model.replaceAll(" ", "");
		WifiLOG.i("PhoneModel��"+model);
		return model;
	}
}
