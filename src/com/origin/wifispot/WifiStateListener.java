package com.origin.wifispot;

/**
 * <p>wifi�¼�������<br/>
 * <p>Log TAG="Matrix_WIFI"<br/>
 * @author Andy
 * @version v1.0
 */
public interface WifiStateListener {
	/**
	 * ������ǳ���
	 */
	public final int WIFI_METHOD_ERROR = 0x1;
	
	/**
	 * ����wifi�ȵ����
	 */
	public final int WIFI_CLOSE_ERROR  = 0x2;
	
	/**
	 * ���wifi״̬����
	 */
	public final int WIFI_CHECK_ERROR  = 0x3;
	
	/**
	 * wifi�ȵ㴴���ɹ�
	 */
	public final int WIFI_APCREATE_SUCCESS = 0x4;
	
	/**
	 * wifi�ȵ㴴��ʧ��
	 */
	public final int WIFI_APCREATE_FAILED = 0x5;
		
	/**
	 * wifi�ȵ�ɨ��ɹ�������ɨ���б�
	 */
	public final int WIFI_SCAN_SUCCESS  = 0x6;
	
	/**
	 * wifi�ȵ�ɨ��ʧ��
	 */
	public final int WIFI_SEARCH_FAILED  = 0x7;
	
	/**
	 * wifi����ʧ��
	 */
	public final int WIFI_CONNECT_FAILED  = 0x8;
	
	/**
	 * wifi���ӳɹ�
	 */
	public final int WIFI_CONNECT_SUCCESS  = 0x9;
	
	
	/**
	 * ����wifi״̬,����ap״̬������ap״̬
	 * @param state wifi����״̬
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
