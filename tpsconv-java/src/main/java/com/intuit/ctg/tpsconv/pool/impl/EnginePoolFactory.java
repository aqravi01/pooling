package com.intuit.ctg.tpsconv.pool.impl;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import com.intuit.ctg.tpsconv.pool.TPSConv;

public class EnginePoolFactory<T> extends BasePooledObjectFactory<T> {

	private String enginePath;
	private String formsPath;

	public EnginePoolFactory(String enginePath, String formsPath) {
		// super();
		setEnginePath(enginePath);
		setFormsPath(formsPath);
	}

	@SuppressWarnings("unchecked")
	@Override
	public T create() throws Exception {
		TPSConv tpsConv = new TPSConv();
		tpsConv.setExecPath(getEnginePath());
		tpsConv.setFormPath(getFormsPath());
		tpsConv.init();
		return (T) tpsConv;
	}

	@Override
	public PooledObject<T> makeObject() throws Exception {
		return wrap(create());
	}

	@Override
	public PooledObject<T> wrap(T obj) {
		return new DefaultPooledObject<T>(obj);
	}

	@Override
	public void destroyObject(PooledObject<T> p) throws Exception {
		TPSConv tpsConv = (TPSConv) p.getObject();
		tpsConv.terminate();
	}

	@Override
	public boolean validateObject(PooledObject<T> p) {
		Boolean isValid = Boolean.FALSE;
		try {
			if (p.getObject() instanceof TPSConv)
				return isValid = Boolean.TRUE;
		} catch (Exception e) {
			return isValid;
		}
		return isValid;
	}

	public String getEnginePath() {
		return enginePath;
	}

	public void setEnginePath(String enginePath) {
		this.enginePath = enginePath;
	}

	public String getFormsPath() {
		return formsPath;
	}

	public void setFormsPath(String formsPath) {
		this.formsPath = formsPath;
	}

}
