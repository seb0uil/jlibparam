package org.jLib.Pool.factory;

public class StringFactory implements IPoolFactory<String> {

	public String create() {
		return new String();
	}

}
