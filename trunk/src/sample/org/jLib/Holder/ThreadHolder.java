package org.jLib.Holder;

import java.util.Date;

public class ThreadHolder implements Runnable {

	public void run() {
		String key = "date";
		Date d = (Date) PoolHolder.get(key);
		System.out.print(d);
	}
}
