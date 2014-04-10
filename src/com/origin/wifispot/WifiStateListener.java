package com.origin.wifispot;

/**
 * <p>wifi事件监听类<br/>
 * <p>Log TAG="Matrix_WIFI"<br/>
 * @author Andy
 * @version v1.0
 */
public interface WifiStateListener {
	/**
	 * 反射机智出错
	 */
	public final int WIFI_METHOD_ERROR = 0x1;
	
	/**
	 * 销毁wifi热点出错
	 */
	public final int WIFI_CLOSE_ERROR  = 0x2;
	
	/**
	 * 检测wifi状态出错
	 */
	public final int WIFI_CHECK_ERROR  = 0x3;
	
	/**
	 * wifi热点创建成功
	 */
	public final int WIFI_APCREATE_SUCCESS = 0x4;
	
	/**
	 * wifi热点创建失败
	 */
	public final int WIFI_APCREATE_FAILED = 0x5;
		
	/**
	 * wifi热点扫描成功并返回扫描列表
	 */
	public final int WIFI_SCAN_SUCCESS  = 0x6;
	
	/**
	 * wifi热点扫描失败
	 */
	public final int WIFI_SEARCH_FAILED  = 0x7;
	
	/**
	 * wifi连接失败
	 */
	public final int WIFI_CONNECT_FAILED  = 0x8;
	
	/**
	 * wifi连接成功
	 */
	public final int WIFI_CONNECT_SUCCESS  = 0x9;
	
	
	/**
	 * 监听wifi状态,创建ap状态，连接ap状态
	 * @param state wifi连接状态
	 * {@link com.origin.wifispot.WifiStateListener#WIFI_SCAN_SUCCESS}  ;
	 * {@link com.origin.wifispot.WifiStateListener#WIFI_SEARCH_FAILED}  ;
	 * {@link com.origin.wifispot.WifiStateListener#WIFI_METHOD_ERROR}  ;
	 * {@link com.origin.wifispot.WifiStateListener#WIFI_CLOSE_ERROR}  ;
	 * {@link com.origin.wifispot.WifiStateListener#WIFI_CHECK_ERROR}  ;
	 * {@link com.origin.wifispot.WifiStateListener#WIFI_APCREATE_SUCCESS}  ;
	 * {@link com.origin.wifispot.WifiStateListener#WIFI_APCREATE_FAILED}  ;
	 * {@link com.origin.wifispot.WifiStateListener#WIFI_CONNECT_FAILED}  ;
	 * {@link com.origin.wifispot.WifiStateListener#WIFI_CONNECT_SUCCESS}  ;
	 */
	public abstract void MatrixWifiState(int state);
}
