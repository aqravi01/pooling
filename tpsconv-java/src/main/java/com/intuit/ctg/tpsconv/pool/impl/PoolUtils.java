package com.intuit.ctg.tpsconv.pool.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.log4j.Logger;

import com.intuit.ctg.tpsconv.pool.ConversionResult;
import com.intuit.ctg.tpsconv.pool.TPSConv;

public class PoolUtils {

	static Logger logger = Logger.getLogger(PoolUtils.class);

	private EnginePoolImpl<TPSConv> pool;

	private String poolcommandAndPath;

	private String formPath;

	private int poolConversionTime;

	private int poolSize;

	public static void main(String[] args) throws Exception {
		
		byte[] bytes = IOUtils.toByteArray(new FileInputStream(
				"C:\\tpsconv\\input21.xml"));

		new PoolUtils().convert(bytes);
	}

	void convert(byte[] bytes) throws FileNotFoundException, IOException {

		Map<String, String> options = new HashMap<String, String>();
		options.put("StopForErrors", "yes");
		options.put("ImporterFormsetID", "S2016FDPPER");
		options.put("SuppressEFConversion", "yes");
		options.put("PDFFormat", "client");
		options.put("ImporterVersion", "140");
		
		GenericObjectPoolConfig config = new GenericObjectPoolConfig();
		config.setMaxIdle(1);
		config.setMaxTotal(getPoolSize());
		config.setTestOnBorrow(true);
		config.setTestOnReturn(true);

		try {
			pool = new EnginePoolImpl<TPSConv>(new EnginePoolFactory<TPSConv>(
					getPoolcommandAndPath(), getFormPath()), config);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		TPSConv tpsConv = null;
		try {
			tpsConv = pool.borrowObject( getPoolConversionTime() );
			tpsConv.setConversionTimeOut( getPoolConversionTime() );
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {

			ConversionResult conversionResult = tpsConv.convert(bytes, options);

			if (conversionResult.getErrorCode() != 0) {

				logger.error(conversionResult.getErrorMessage());
				return;
			}

			byte[] outbytes = conversionResult.getContent();
			if (outbytes == null || outbytes.length == 0) {
				logger.error("Invalid data from conversion :  "	+ outbytes);
			}

			FileUtils.writeByteArrayToFile(new File("C:\\tpsconv1\\output"
					+ System.currentTimeMillis() + ".pdf"), outbytes);
			logger.info("Conversion success...");
		} catch (Exception e) {
			logger.error("Error performing conversion", e);
		} finally {
			try {
				tpsConv.close();
				pool.returnObject(tpsConv);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public EnginePoolImpl<TPSConv> getPool() {
		return pool;
	}

	public void setPool(EnginePoolImpl<TPSConv> pool) {
		this.pool = pool;
	}

	public String getPoolcommandAndPath() {
		return "C:\\W2-1099-Reporter";
	}

	public void setPoolcommandAndPath(String poolcommandAndPath) {
		this.poolcommandAndPath = poolcommandAndPath;
	}

	public String getFormPath() {
		return "C:\\W2-1099-Reporter\\Forms";
	}

	public void setFormPath(String formPath) {
		this.formPath = formPath;
	}

	public int getPoolConversionTime() {
		return 1 * 60 * 1000;
	}

	public void setPoolConversionTime(int poolConversionTime) {
		this.poolConversionTime = poolConversionTime;
	}

	public int getPoolSize() {
		return 3;
	}

	public void setPoolSize(int poolSize) {
		this.poolSize = poolSize;
	}

}
