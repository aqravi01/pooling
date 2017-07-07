package com.intuit.ctg.tpsconv.pool.impl;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

public class EnginePoolImpl<T> extends GenericObjectPool<T> {
	
	public EnginePoolImpl(EnginePoolFactory<T> factory) {
		super(factory);
	}
	
	public EnginePoolImpl(EnginePoolFactory<T> factory,
			GenericObjectPoolConfig config) {
		super(factory, config);
	}
	
	@Override
	public void addObject() throws Exception {
		super.addObject();
	}
	
	
	
	@Override
	public T borrowObject() throws Exception {
		return super.borrowObject();
	}
	
	@Override
	public void returnObject(T obj) {
		super.returnObject(obj);
	}	
	
}