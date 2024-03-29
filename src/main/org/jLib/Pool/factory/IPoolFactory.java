package org.jLib.Pool.factory;

public interface IPoolFactory<E> {
	E create();
	
	void expire(E obj);
}
