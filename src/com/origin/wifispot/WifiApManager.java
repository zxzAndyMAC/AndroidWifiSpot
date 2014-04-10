package com.origin.wifispot;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
 
import java.util.HashMap;
import java.util.Map;

 
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;

public class WifiApManager {
	
	private final WifiManager mWifiManager;

	private static final int WIFI_AP_STATE_UNKNOWN = -1;

	private static final String METHOD_GET_WIFI_AP_STATE = "getWifiApState";
	private static final String METHOD_SET_WIFI_AP_ENABLED = "setWifiApEnabled";
	private static final String METHOD_GET_WIFI_AP_CONFIG = "getWifiApConfiguration";
	private static final String METHOD_IS_WIFI_AP_ENABLED = "isWifiApEnabled";

	private static final Map<String, Method> methodMap = new HashMap<String, Method>();
	private static Boolean mIsSupport;
	private static boolean mIsHtc;

	public synchronized final boolean isSupport() {
		
		if (mIsSupport != null) {
			return mIsSupport;
		}

		boolean result = Build.VERSION.SDK_INT > Build.VERSION_CODES.ECLAIR_MR1;// FROYO;
		if (result) {
			try {
				Field field = WifiConfiguration.class
						.getDeclaredField("mWifiApProfile");
				mIsHtc = field != null;
			} catch (Exception e) {
			}
		}
		
		if (result) {  
            try {  
                String name = METHOD_GET_WIFI_AP_STATE;  
                Method method = WifiManager.class.getMethod(name);  
                methodMap.put(name, method);  
                result = method != null;  
            } catch (SecurityException e) {  
            	WifiLOG.e("SecurityException"+e.toString());  
            } catch (NoSuchMethodException e) {  
            	WifiLOG.e("NoSuchMethodException"+e.toString());  
            }  
        }
		
		if (result) {  
            try {  
                String name = METHOD_SET_WIFI_AP_ENABLED;  
				Method method = WifiManager.class.getMethod(name,
						WifiConfiguration.class, boolean.class);
                methodMap.put(name, method);  
                result = method != null;  
            } catch (SecurityException e) {  
            	WifiLOG.e("SecurityException"+e.toString());  
            } catch (NoSuchMethodException e) {  
            	WifiLOG.e("NoSuchMethodException"+e.toString());  
            }  
        }
		
		if (result) {  
            try {  
                String name = METHOD_GET_WIFI_AP_CONFIG;  
                Method method = WifiManager.class.getMethod(name);  
                methodMap.put(name, method);  
                result = method != null;  
            } catch (SecurityException e) {  
            	WifiLOG.e("SecurityException"+e.toString());  
            } catch (NoSuchMethodException e) {  
            	WifiLOG.e("NoSuchMethodException"+e.toString());  
            }  
        }
		
		if (result) {  
            try {  
                String name = getSetWifiApConfigName();  
				Method method = WifiManager.class.getMethod(name,
						WifiConfiguration.class);
                methodMap.put(name, method);  
                result = method != null;  
            } catch (SecurityException e) {  
            	WifiLOG.e("SecurityException"+ e.toString());  
            } catch (NoSuchMethodException e) {  
            	WifiLOG.e("NoSuchMethodException"+ e.toString());  
            }  
        }
		
		if (result) {  
            try {  
                String name = METHOD_IS_WIFI_AP_ENABLED;  
                Method method = WifiManager.class.getMethod(name);  
                methodMap.put(name, method);  
                result = method != null;  
            } catch (SecurityException e) {  
            	WifiLOG.e("SecurityException"+ e.toString());  
            } catch (NoSuchMethodException e) {  
            	WifiLOG.e("NoSuchMethodException "+ e.toString());  
            }  
        }
		
		mIsSupport = result;
		return isSupport();
	}
	
	public WifiApManager(WifiManager manager) {
		mWifiManager = manager;
		if (!isSupport()) {
			throw new RuntimeException("Unsupport Ap!");
		}
		WifiLOG.i("Build.BRAND -----------> " + Build.BRAND);		
	}

	public WifiManager getWifiManager() {
		return mWifiManager;
	}

	public int getWifiApState() {
		try {
			Method method = methodMap.get(METHOD_GET_WIFI_AP_STATE);
			return (Integer) method.invoke(mWifiManager);
		} catch (Exception e) {
			WifiLOG.e(e.toString());
		}
		return WIFI_AP_STATE_UNKNOWN;
	}
	
	private WifiConfiguration getHtcWifiApConfiguration(
			WifiConfiguration standard) {
        WifiConfiguration htcWifiConfig = standard;  
        try {  
        	Field mWifiApProfileValue = WifiConfiguration.class
					.getDeclaredField("mWifiApProfile");
            if (mWifiApProfileValue != null) {  
                mWifiApProfileValue.setAccessible(true);
            	Object hotSpotProfile = mWifiApProfileValue.get(htcWifiConfig);
            	if (hotSpotProfile != null){
            		Field ssidField = hotSpotProfile.getClass().getDeclaredField("SSID");
            		htcWifiConfig.SSID = (String)ssidField.get(mWifiApProfileValue);
            	}  
            }  
        } catch (Exception e) {  
        	WifiLOG.e(e.toString());  
        }  
        return htcWifiConfig;  
    }
	
    public WifiConfiguration getWifiApConfiguration() {  
        WifiConfiguration configuration = null;  
        try {  
            Method method = methodMap.get(METHOD_GET_WIFI_AP_CONFIG);  
            configuration = (WifiConfiguration) method.invoke(mWifiManager);  
            if(isHtc()){  
                if (configuration == null){
                    configuration = new WifiConfiguration();
                    configuration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                    configuration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                    configuration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                    configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                } else {
                    configuration = getHtcWifiApConfiguration(configuration);  
                }
            }  
        } catch (Exception e) {  
        	WifiLOG.e(e.toString());  
        }  
        return configuration;  
    }
    
	public boolean setWifiApConfiguration(WifiConfiguration netConfig) {
		boolean result = false;
		try {
			if (isHtc()) {
				setupHtcWifiConfiguration(netConfig);
			}

			Method method = methodMap.get(getSetWifiApConfigName());
			Class<?>[] params = method.getParameterTypes();
			for (Class<?> clazz : params) {
				WifiLOG.i("param -> " + clazz.getSimpleName());
			}

			if (isHtc()) {
				int rValue = (Integer) method.invoke(mWifiManager, netConfig);
				WifiLOG.i("rValue -> " + rValue);
				result = rValue > 0;
			} else {
				result = (Boolean) method.invoke(mWifiManager, netConfig);
			}
		} catch (Exception e) {
			WifiLOG.e(e.toString());
		}
		return result;
	}

	public boolean setWifiApEnabled(WifiConfiguration configuration,
			boolean enabled) {
		boolean result = false;
		try {
		    if (isHtc()){
		        setupHtcWifiConfiguration(configuration);
		    }
			Method method = methodMap.get(METHOD_SET_WIFI_AP_ENABLED);
			result = (Boolean) method.invoke(mWifiManager, configuration,
					enabled);
		} catch (Exception e) {
			WifiLOG.e(e.toString());
		}
		return result;
	}

	public boolean isWifiApEnabled() {
		boolean result = false;
		try {
			Method method = methodMap.get(METHOD_IS_WIFI_AP_ENABLED);
			result = (Boolean) method.invoke(mWifiManager);
		} catch (Exception e) {
			WifiLOG.e(e.toString());
		}
		return result;
	}

	private void setupHtcWifiConfiguration(WifiConfiguration config) {
		try {
			WifiLOG.d("setupHtcWifiConfiguration =  " + config);
			Field mWifiApProfileValue = WifiConfiguration.class.getDeclaredField("mWifiApProfile");
			mWifiApProfileValue.setAccessible(true);
			Object hotSpotProfile = mWifiApProfileValue.get(config);
			mWifiApProfileValue.setAccessible(false);
	        
			if (hotSpotProfile != null) {
				Field ssidField = hotSpotProfile.getClass().getDeclaredField("SSID");
				ssidField.setAccessible(true);
				ssidField.set(hotSpotProfile, config.SSID);
				ssidField.setAccessible(false);
				
				Field bssidField = hotSpotProfile.getClass().getDeclaredField("BSSID");
				bssidField.setAccessible(true);
				bssidField.set(hotSpotProfile, config.BSSID);
				bssidField.setAccessible(false);
				
				Field secureField = hotSpotProfile.getClass().getDeclaredField("secureType");
				secureField.setAccessible(true);
				secureField.set(hotSpotProfile, "open");
				secureField.setAccessible(false);
				
				Field dhcpEnableField = hotSpotProfile.getClass().getDeclaredField("dhcpEnable");
				dhcpEnableField.setAccessible(true);
				dhcpEnableField.set(hotSpotProfile, 1);
				dhcpEnableField.setAccessible(false);
				
				Field maxConnsField = hotSpotProfile.getClass().getDeclaredField("maxConns");
				maxConnsField.setAccessible(true);
				maxConnsField.set(hotSpotProfile, 10);
				maxConnsField.setAccessible(false);
				
				Field maxDhcpClientsField = hotSpotProfile.getClass().getDeclaredField("maxDhcpClients");
				maxDhcpClientsField.setAccessible(true);
				maxDhcpClientsField.set(hotSpotProfile, 10);
				maxDhcpClientsField.setAccessible(false);
			}
		} catch (Exception e) {
			WifiLOG.e(e.toString());
		}
	}

	public static boolean isHtc() {
		return mIsHtc;
	}

	private static String getSetWifiApConfigName() {
		return mIsHtc ? "setWifiApConfig" : "setWifiApConfiguration";
	}

}