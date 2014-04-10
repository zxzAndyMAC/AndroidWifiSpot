package com.origin.wifispot;

import java.util.List;

import android.content.Context;

/**
 * 
 * <p>OriginWiFi������ӿ��ܻ�<br/>
 * <p>Log TAG="Origin_WIFI"<br/>
 * @see Ȩ��
 * ��SDK��Ҫ����Ȩ�� ��<br/> 
 *  1.&lt;uses-permission android:name="android.permission.CHANGE_NETWORK_STATE" /&gt;<br/>
 *  2.&lt;uses-permission android:name="android.permission.CHANGE_WIFI_STATE" /&gt;<br/>
 *  3.&lt;uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" /&gt;<br/>
 *  4.&lt;uses-permission android:name="android.permission.ACCESS_WIFI_STATE" /&gt;<br/>
 *  5.&lt;uses-permission android:name="android.permission.READ_PHONE_STATE" /&gt;<br/>
 *	6.&lt;uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/&gt;<br/>
 *	7.&lt;uses-permission android:name="android.permission.INTERNET"/&gt;<br/>
 *	8.&lt;uses-permission android:name="android.permission.WAKE_LOCK"/&gt;<br/>
 *	9.&lt;uses-permission android:name="android.permission.ACCES_MOCK_LOCATION"/&gt;<br/>
 *	10.&lt;uses-permission android:name="android.permission.WRITE_SETTINGS"/&gt;<br/>
 * @author  AndyZheng
 * @version v1.2
 *
 */
public class OriginWiFi {
	private Context mContext;
	
	private static OriginWiFi matrixwifi = null;
	
	private static boolean ConnectRealease = false;
	private static boolean CreateRealease = false;
	
	/**
	 * SSID Ψһ��ʾ��
	 * Ϊ�˲�������Ӧ�õ��ȵ����ֳ�ͻ����������ô��ֶ�,Ĭ��"Origin"
	 */
	public static String SSID_TAG = "Origin";
	/**
	 * ����
	 * @param context ������
	 */
	OriginWiFi(Context context) {
		// TODO Auto-generated constructor stub
		this.mContext = context;
	}
	
	/**
	 * release
	 * make sure you released wifi first
	 */
	public void release()
	{
		if(null != matrixwifi)
		{
			WifiLOG.d("release instance 1");
			matrixwifi = null;
			WifiLOG.d("release instance 2");
		}
	}
	
	/**
	 * ��ȡSDKΨһʵ��
	 * @param context ������
	 * @return MatrixWiFiʵ��
	 */
	public static OriginWiFi getInstance(Context context)
	{
		if(null == matrixwifi)
			matrixwifi = new OriginWiFi(context);
		return matrixwifi;
	}
	
	/**
	 * �����ȵ㴴��״̬����
	 * @param l WifiStateListener
	 */
	public void setAPCreaterListener(WifiStateListener l)
	{
		WifiAPCreater.getInstance(mContext).setListener(l);
	}
	
	/**
	 *  �����ȵ�����״̬����
	 * @param l WifiStateListener
	 */
	public void setAPConnecterListener(WifiStateListener l)
	{
		WifiConnecter.getInstance(mContext).setListener(l);
	}
	
	/**
	 * ����wifi�ȵ�
	 * ssid��ʽ   MatrixWiFi.SSID_TAG + name + "_" + IMSI;
	 * @param name  ssid name
	 */
	public void CreateAPwithName(String name)
	{
		CreateRealease = false;
		WifiAPCreater.getInstance(mContext).startWifiAp(name);
	}
	
	/**
	 * ��ȡ����ip
	 * @return ip
	 */
	public String getLocalIP()
	{
		return WifiAPCreater.getInstance(mContext).getlocalip(true);
	}
	
	/**
	 *  �����ȵ㴴����ʱ
	 * @param count ��ʱ����
	 * @param sleep ÿ�γ�ʱʱ��
	 */
	public void setAPCreateTimeOut(int count ,int sleep)
	{
		WifiAPCreater.getInstance(mContext).setTimeOut(count, sleep);
	}
	
	/**
	 * �ر�wifi�ȵ�
	 */
	public void CloseWifiAP()
	{
		if(!CreateRealease)
		{
			CreateRealease = true;
			WifiAPCreater.getInstance(mContext).closeWifiAp(mContext);
		}
	}
	
	/**
	 * ɨ���ȵ�
	 */
	public void StartScanAP()
	{
		ConnectRealease = false;
		WifiConnecter.getInstance(mContext).Scan();
	}
	
	/**
	 * ��ȡ�ȵ�ɨ���б�
	 * ��μ�WifiStateListener.WIFI_SCAN_SUCCESS
	 * ÿ������WifiStateListener.WIFI_SCAN_SUCCESS״̬�����������б�
	 * @return ����ɨ���б�ssid
	 */
	public List<String> getScanList()
	{
		return WifiConnecter.getInstance(mContext).getScanList();
	}
	
	/**
	 * ����ָ��ssid��wifi�ȵ�
	 * ssid�ӷ��ص�ɨ���б��л�ȡ 
	 * ssid��ʽ   "Matrix_"+name+"_"+IMSI;
	 * @param ssid
	 */
	public void ConnectAP(String ssid)
	{
		WifiConnecter.getInstance(mContext).Connect(ssid);
	}
	
	/**
	 * �Ͽ����ȵ������
	 */
	public void CloseConnect()
	{
		if(!ConnectRealease)
		{
			ConnectRealease = true;
			WifiConnecter.getInstance(mContext).Close();
		}
	}
	
	/**
	 * �����ȵ�ɹ��󣬽�ȡ���ȵ��ip
	 */
	public String getServerIP()
	{
		return WifiConnecter.getInstance(mContext).getServerIP();
	}
	
	/**
	 * ����wifi��־�����Ƿ��
	 * @param able ��true for enable,false for disable
	 */
	public void setDebug(boolean able)
	{
		WifiLOG.LOG = able;
	}
}
