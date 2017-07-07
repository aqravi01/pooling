package com.intuit.ctg.tpsconv.pool;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Map;
import java.util.UUID;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.tika.Tika;
import org.apache.tika.io.IOUtils;

public class TPSConv {
	static Logger logger = Logger.getLogger(TPSConv.class);
	private String execPath;
	private String formPath;
	private Process pTPSConv;
	private String id;
	private int conversionTimeOut;
	private OutputStream outputStream;
	private InputStream inputStream;
	private InputStream errorStream;

	public TPSConv() {
		this.id = UUID.randomUUID().toString();
		this.conversionTimeOut = 15000;
	}

	public String getId() {
		return this.id;
	}

	public String getExecPath() {
		return this.execPath;
	}

	public void setExecPath(String execPath) {
		this.execPath = execPath;
	}

	public String getFormPath() {
		return this.formPath;
	}

	public void setFormPath(String formPath) {
		this.formPath = formPath;
	}

	public int getConversionTimeOut() {
		return this.conversionTimeOut;
	}

	public void setConversionTimeOut(int conversionTimeOut) {
		this.conversionTimeOut = conversionTimeOut;
	}

	public boolean isAlive() {
		try {
			int exitCode = this.pTPSConv.exitValue();
			if (exitCode != 0) {

			}
			return false;
		} catch (IllegalThreadStateException e) {
		}
		return true;
	}

	private String getOSBinary(String baseName) {
		String os = System.getProperty("os.name").toLowerCase();
		if (os.contains("win")) {
			return baseName + ".exe";
		}
		if (os.contains("mac")) {
			return baseName;
		}
		if (os.contains("linux")) {
			return baseName + ".linux32";
		}
		logger.warn("Unknown OS : '" + os + "', assuming base name ");
		return baseName;
	}

	public void info(Object o) {
		logger.info(this.id + " : " + o);
	}

	public void debug(Object o) {
		logger.debug(this.id + " : " + o);
	}

	public void init() throws Exception {
		Runtime runtime = Runtime.getRuntime();

		String exe = this.execPath + "/" + getOSBinary("tpsconv");
		if (!new File(exe).exists()) 
		{
			throw new Exception(exe + " does not exist!");
		}
		if (!new File(this.formPath).exists()) 
		{
			throw new Exception(this.formPath + " does not exist!");
		}
		File cacheDir = new File(getId());
		cacheDir.mkdirs();

		String[] commandline = { exe, "FormsPath=" + this.formPath,
				"BatchMode=yes", "CachePath=" + getId() };
		this.pTPSConv = runtime.exec(commandline);

		outputStream = this.pTPSConv.getOutputStream();
		inputStream = this.pTPSConv.getInputStream();
		errorStream = this.pTPSConv.getErrorStream();

		if (isAlive()) {
			info("TPSConv initialized");
		} else {
			outputStream.close();
			inputStream.close();
			errorStream.close();
			throw new Exception("Failed to initialize.. "
					+ this.pTPSConv.exitValue());
		}
	}

	public void close() throws IOException {
		errorStream.close();
		inputStream.close();
		outputStream.close();
	}

	public void write(byte[] bytes, OutputStream outstream) throws Exception {
		outstream.write(bytes);
		outstream.flush();
	}

	public void terminate() {
		try {
			this.pTPSConv.destroy();
			this.pTPSConv.waitFor();
			info("terminated");
			FileUtils.forceDelete(new File(getId()));
			info("Deleted cache");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public byte[] uncompress(byte[] data) throws IOException {
		Tika tika = new Tika();
		String contentType = tika.detect(new ByteArrayInputStream(data));
		if ("application/gzip".equals(contentType)) {
			debug("Gzipped file.. Uncompressing... ");
			GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(
					data));

			ByteArrayOutputStream zipcontent = new ByteArrayOutputStream();
			IOUtils.copy(gis, zipcontent);

			gis.close();
			return zipcontent.toByteArray();
		}
		return data;
	}

	public ConversionResult convert(byte[] data, Map<String, String> options)
			throws Exception {
		long startTime = System.currentTimeMillis();

		byte[] inputBytes = uncompress(data);
		if ((inputBytes == null) || (inputBytes.length == 0)) {
			throw new Exception("Invalid data stream of size " + data.length);
		}

		ConversionListener conversionListener = new ConversionListener(
				inputStream, this.conversionTimeOut);
		Thread conversionListenerThread = new Thread(conversionListener);

		ConversionStatusReader conversionStatusReader = new ConversionStatusReader(
				new InputStreamReader(errorStream), conversionListener);
		Thread conversionStatusReaderThread = new Thread(conversionStatusReader);
		conversionStatusReaderThread.start();

		int bytes = inputBytes.length;
		String optBytes = "<BatchMode bytes=\"" + bytes + "\"/>\n";
		write(optBytes.getBytes(), outputStream);
		for (String option : options.keySet()) {
			String command = "<option>" + option + "="
					+ (String) options.get(option) + "</option>\n";
			write(command.getBytes(), outputStream);
		}
		conversionListenerThread.start();

		debug("Writing bytes");
		write(inputBytes, outputStream);
		debug("Writing bytes - done ");

		conversionListenerThread.join();
		if (conversionListener.getConversionResult().getErrorCode() == ConversionResult.TIMED_OUT) {
			this.pTPSConv.destroyForcibly().waitFor();
			init();
		}
		conversionListener.getConversionResult().setConvertTime(
				System.currentTimeMillis() - startTime);

		return conversionListener.getConversionResult();
	}

	public ConversionResult convertQEF(byte[] data, Map<String, String> options)
			throws Exception {
		long startTime = System.currentTimeMillis();

		byte[] inputBytes = uncompress(data);
		if ((inputBytes == null) || (inputBytes.length == 0)) {
			throw new Exception("Invalid data stream of size " + data.length);
		}

		OutputStream pStdIn = this.pTPSConv.getOutputStream();
		InputStream pStdOut = this.pTPSConv.getInputStream();

		ConversionListener conversionListener = new ConversionListener(pStdOut,
				this.conversionTimeOut);
		Thread conversionListenerThread = new Thread(conversionListener);

		ConversionStatusReader conversionStatusReader = new ConversionStatusReader(
				new InputStreamReader(this.pTPSConv.getErrorStream()),
				conversionListener);
		Thread conversionStatusReaderThread = new Thread(conversionStatusReader);
		conversionStatusReaderThread.start();

		int bytes = inputBytes.length;
		String optBytes = "<BatchMode bytes=\"" + bytes + "\"/>\n";
		write(optBytes.getBytes(), pStdIn);
		for (String option : options.keySet()) {
			String command = "<option>" + option + "="
					+ (String) options.get(option) + "</option>\n";
			write(command.getBytes(), pStdIn);
		}
		conversionListenerThread.start();

		debug("Writing bytes");
		write(inputBytes, pStdIn);
		debug("Writing bytes - done ");

		conversionListenerThread.join();
		if (conversionListener.getConversionResult().getErrorCode() == ConversionResult.TIMED_OUT) {
			this.pTPSConv.destroyForcibly().waitFor();
			init();
		}
		conversionListener.getConversionResult().setConvertTime(
				System.currentTimeMillis() - startTime);

		return conversionListener.getConversionResult();
	}

	public OutputStream getOutputStream() {
		return outputStream;
	}

	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	public InputStream getIntputStream() {
		return inputStream;
	}

	public void setIntputStream(InputStream intputStream) {
		this.inputStream = intputStream;
	}

	public InputStream getErrorStream() {
		return errorStream;
	}

	public void setErrorStream(InputStream errorStream) {
		this.errorStream = errorStream;
	}
}
