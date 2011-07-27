package org.jLib.Holder;

import java.util.Date;

public class ObjectHolder {

	/**
	 * Valeur par défaut du timeout en ms
	 */
	public static Long DEFAULT_TIMEOUT = 1000L;
	
	/**
	 * Valeur par défaut du timeout en ms
	 */
	public static Long DEFAULT_DEADOUT = 1000L; //*60*60*8L; // 8h
	
	private Long timestamp;
	private Long timeout = DEFAULT_TIMEOUT;
	private Long deadout = DEFAULT_DEADOUT;
	private Object o; 
	
	public Object get() {
		return o;
	}
	
	public ObjectHolder(Object o) {
		this(o,DEFAULT_TIMEOUT, DEFAULT_DEADOUT);
	}
	
	public ObjectHolder(Object o, Long timeout) {
		this(o,timeout, DEFAULT_DEADOUT);
	}

	public ObjectHolder(Object o, Long timeout, Long deadout) {
		this.timestamp = (new Date()).getTime();
		this.timeout = timeout;
		this.deadout = deadout;
		this.o = o;
	}
	
	public Long getTimestamp() {
		return this.timestamp;
	}
	
	public void Touch() {
		this.timestamp = (new Date()).getTime();
	}
	
	public boolean isExpired() {
		return (this.timestamp + this.timeout < (new Date()).getTime());
	}
	
	public boolean isDead() {
		return (this.timestamp + this.deadout < (new Date()).getTime());
	}
}
