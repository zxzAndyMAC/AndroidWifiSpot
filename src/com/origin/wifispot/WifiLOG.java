package com.origin.wifispot;

import android.util.Log;

public class WifiLOG {
	private static final String TAG = "Origin_WIFI";
	private static final String _TAG = "===============";
	public  static boolean LOG = true;
	
	public static void d(String s)
	{
		if(LOG)
			Log.d(TAG,_TAG+s);
	}
	
	public static void i(String s)
	{
		if(LOG)
			Log.i(TAG, _TAG+s);
	}
	
	public static void e(String s)
	{
		if(LOG)
			Log.e(TAG, _TAG+s);
	}
	
	public static void w(String s)
	{
		if(LOG)
			Log.w(TAG, _TAG+s);
	}
	
	public static void v(String s)
	{
		if(LOG)
			Log.v(TAG, _TAG+s);
	}
}
