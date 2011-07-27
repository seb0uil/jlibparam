package org.jLib.Holder;

import java.sql.Date;

public class HolderSample {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		Date d1 = new Date(2001,07,01);
		Date d2 = new Date(2001,07,02);
		Date d3 = new Date(2001,07,03);
		Date d4 = new Date(2001,07,04);
		Date d5 = new Date(2001,07,05);
		Date d6 = new Date(2001,07,06);

//		ThreadHolder p = new ThreadHolder();
//		Thread t = new Thread(p);
//		t.start();

		
		String key = "date";
		PoolHolder.put(key, new Object());
		PoolHolder.put(key, "ee");
		PoolHolder.put(key, d3);
		PoolHolder.put(key, d4);
		PoolHolder.put(key, d5);
		PoolHolder.put(key, d6);
		
		
		PoolHolder.put(key, d1);
		PoolHolder.put(key, d2);
		
		PoolHolder.get(key);
		PoolHolder.put(key, d1);
		
	}

}
