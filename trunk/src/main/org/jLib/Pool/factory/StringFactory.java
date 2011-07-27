package org.jLib.Pool.factory;

import java.math.BigInteger;
import java.security.SecureRandom;

public class StringFactory implements IPoolFactory<StringBuffer> {
	  private SecureRandom random = new SecureRandom();

	public StringBuffer create() {
		StringBuffer sb = new StringBuffer();
		return sb.append(new BigInteger(130, random).toString(32));
	}
	
	public void expire(StringBuffer obj) {
		System.out.println(obj + "  expire..");
	}

}

