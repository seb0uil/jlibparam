package org.jLib.Pool.factory;

public class IntegerFactory implements IPoolFactory<Integer> {

	public Integer create() {
		return new Integer(0);
	}

}
