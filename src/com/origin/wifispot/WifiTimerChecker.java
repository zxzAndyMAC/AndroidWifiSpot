package com.origin.wifispot;

public abstract class WifiTimerChecker {
	private int mCount = 0;
	private int mTimeOutCount = 1;
	private int mSleepTime = 1000; // 1s
	private boolean mExitFlag = false;
	private Thread mThread = null;
	
	/**
	 * 避免在下面两个函数里做UI操作
	 */
	public abstract void doTimerCheckWork();
	
	public abstract void doTimeOutWork();
	
	public WifiTimerChecker() {
		mThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				while (!mExitFlag) {
					if(mThread.isInterrupted())
						return;
					mCount++;
					if (mCount < mTimeOutCount) {
						doTimerCheckWork();
						try {
							Thread.sleep(mSleepTime);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							exit();
						}
					} else {
						doTimeOutWork();
					}
				}
			}
		});
	}
	
	/**
	 * start
	 * @param times  How many times will check?
	 * @param sleepTime ms, Every check sleep time.
	 */
	public void start(int timeOutCount, int sleepTime) {
		mTimeOutCount = timeOutCount;
		mSleepTime = sleepTime;
		
		mThread.start();
	}
	
	public void exit() {
		mExitFlag = true;
		try {
			mThread.interrupt();
			mThread.join();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
