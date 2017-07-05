package com.intuit.ctg.tpsconv.pool;

import java.util.ArrayList;
import java.util.List;

public class ConversionResult
{
  public static int STARTED = 1;
  public static int COMPLETED = 2;
  public static int ERROR = 3;
  public static int TIMED_OUT = 4;
  public static int PROCESS_CRASHED = 5;
  private StringBuffer stderr;
  private int errorCode;
  private long size;
  private long readSize;
  private byte[] content;
  private long convertTime;
  private double engineTime;
  private volatile int conversionStatus;
  private volatile boolean conversionStarted;
  private String errorMessage;
  private List<String> options;
  private ErrorContext errorContext;
  
  public ConversionResult()
  {
    this.conversionStatus = 0;
    this.size = -1L;
    this.options = new ArrayList();
    this.errorContext = new ErrorContext();
  }
  
  public boolean isConversionStarted()
  {
    return this.conversionStarted;
  }
  
  public void setConversionStarted(boolean conversionStarted)
  {
    this.conversionStarted = conversionStarted;
  }
  
  public void setConversionStatus(int conversionStatus)
  {
    this.conversionStatus = conversionStatus;
  }
  
  public int getConversionStatus()
  {
    return this.conversionStatus;
  }
  
  public StringBuffer getStderr()
  {
    return this.stderr;
  }
  
  public long getConvertTime()
  {
    return this.convertTime;
  }
  
  public void setConvertTime(long convertTime)
  {
    this.convertTime = convertTime;
  }
  
  public void setStderr(StringBuffer stderr)
  {
    this.stderr = stderr;
  }
  
  public int getErrorCode()
  {
    return this.errorCode;
  }
  
  public void setErrorCode(int errorCode)
  {
    this.errorCode = errorCode;
  }
  
  public long getSize()
  {
    return this.size;
  }
  
  public void setSize(long size)
  {
    this.size = size;
  }
  
  public byte[] getContent()
  {
    return this.content;
  }
  
  public void setContent(byte[] content)
  {
    this.content = content;
  }
  
  public long getReadSize()
  {
    return this.readSize;
  }
  
  public void setReadSize(long readSize)
  {
    this.readSize = readSize;
  }
  
  public void setErrorMessage(String errorMessage)
  {
    this.errorMessage = errorMessage;
  }
  
  public String getErrorMessage()
  {
    return this.errorMessage;
  }
  
  public List<String> getOptions()
  {
    return this.options;
  }
  
  public double getEngineTime()
  {
    return this.engineTime;
  }
  
  public void setEngineTime(double engineTime)
  {
    this.engineTime = engineTime;
  }
  
  public String toString()
  {
    String str = "Size : " + this.size + "\n";
    str = str + "ErrorCode : " + this.errorCode + "\n";
    str = str + "Time : " + this.convertTime + "\n";
    str = str + "Status  : " + this.conversionStatus + "\n";
    
    return str;
  }
}

