package com.origin.wifispot;

import java.util.List;

import android.content.Context;

/**
 * 
 * <p>OriginWiFi，对外接口总汇<br/>
 * <p>Log TAG="Origin_WIFI"<br/>
 * @see 权限
 * 此SDK需要以下权限 ：<br/> 
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
	 * SSID 唯一标示符
	 * 为了不与其他应用的热点名字冲突，请务必设置此字段,默认"Origin"
	 */
	public static String SSID_TAG = "Origin";
	/**
	 * 构造
	 * @param context 上下文
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
	 * 获取SDK唯一实例
	 * @param context 上下文
	 * @return MatrixWiFi实例
	 */
	public static OriginWiFi getInstance(Context context)
	{
		if(null == matrixwifi)
			matrixwifi = new OriginWiFi(context);
		return matrixwifi;
	}
	
	/**
	 * 设置热点创建状态监听
	 * @param l WifiStateListener
	 */
	public void setAPCreaterListener(WifiStateListener l)
	{
		WifiAPCreater.getInstance(mContext).setListener(l);
	}
	
	/**
	 *  设置热点连接状态监听
	 * @param l WifiStateListener
	 */
	public void setAPConnecterListener(WifiStateListener l)
	{
		WifiConnecter.getInstance(mContext).setListener(l);
	}
	
	/**
	 * 创建wifi热点
	 * ssid格式   MatrixWiFi.SSID_TAG + name + "_" + IMSI;
	 * @param name  ssid name
	 */
	public void CreateAPwithName(String name)
	{
		CreateRealease = false;
		WifiAPCreater.getInstance(mContext).startWifiAp(name);
	}
	
	/**
	 * 获取本地ip
	 * @return ip
	 */
	public String getLocalIP()
	{
		return WifiAPCreater.getInstance(mContext).getlocalip(true);
	}
	
	/**
	 *  设置热点创建超时
	 * @param count 超时次数
	 * @param sleep 每次超时时间
	 */
	public void setAPCreateTimeOut(int count ,int sleep)
	{
		WifiAPCreater.getInstance(mContext).setTimeOut(count, sleep);
	}
	
	/**
	 * 关闭wifi热点
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
	 * 扫描热点
	 */
	public void StartScanAP()
	{
		ConnectRealease = false;
		WifiConnecter.getInstance(mContext).Scan();
	}
	
	/**
	 * 获取热点扫描列表
	 * 请参见WifiStateListener.WIFI_SCAN_SUCCESS
	 * 每次随着WifiStateListener.WIFI_SCAN_SUCCESS状态触发而更新列表
	 * @return 返回扫描列表，ssid
	 */
	public List<String> getScanList()
	{
		return WifiConnecter.getInstance(mContext).getScanList();
	}
	
	/**
	 * 连接指定ssid的wifi热点
	 * ssid从返回的扫描列表中获取 
	 * ssid格式   "Matrix_"+name+"_"+IMSI;
	 * @param ssid
	 */
	public void ConnectAP(String ssid)
	{
		WifiConnecter.getInstance(mContext).Connect(ssid);
	}
	
	/**
	 * 断开与热点的连接
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
	 * 连接热点成功后，将取得热点端ip
	 */
	public String getServerIP()
	{
		return WifiConnecter.getInstance(mContext).getServerIP();
	}
	
	/**
	 * 设置wifi日志功能是否打开
	 * @param able ，true for enable,false for disable
	 */
	public void setDebug(boolean able)
	{
		WifiLOG.LOG = able;
	}
}
