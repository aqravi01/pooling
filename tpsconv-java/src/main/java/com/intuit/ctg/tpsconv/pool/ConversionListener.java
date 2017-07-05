package com.intuit.ctg.tpsconv.pool;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.apache.log4j.Logger;

public class ConversionListener
  implements Runnable, IConversionListener
{
  static Logger logger = Logger.getLogger(ConversionListener.class);
  private InputStream stream;
  private ByteArrayOutputStream dataStream;
  private volatile boolean conversionCompleted;
  private volatile boolean conversionSuccess;
  private volatile long bytesToRead = -1L;
  private int timeout;
  private ConversionResult conversionResult = new ConversionResult();
  
  public ConversionListener(InputStream stream, int timeout)
  {
    this.stream = stream;
    this.dataStream = new ByteArrayOutputStream();
    this.timeout = timeout;
  }
  
  public void conversionSuccess(long datasize)
  {
    this.bytesToRead = datasize;
    this.conversionCompleted = true;
    this.conversionSuccess = true;
    
    logger.debug("conversionSuccess() : datasize = " + datasize);
  }
  
  public void conversionFailed(int errorCode, String errorContext)
  {
    this.conversionResult.setErrorCode(errorCode);
    this.conversionResult.setErrorMessage(errorContext);
    
    this.conversionCompleted = true;
    
    logger.debug("Conversion failed... Notify ");
    this.conversionSuccess = false;
  }
  
  public void conversionDetails(String detail)
  {
    if (detail.contains("<ET>convert: "))
    {
      String time = detail.replace("<ET>convert: ", "").replace("ms</ET>", "");
      this.conversionResult.setEngineTime(Double.parseDouble(time));
    }
    else
    {
      this.conversionResult.getOptions().add(detail);
    }
  }
  
  public void run()
  {
    logger.debug("ConversionListener thread started");
    
    long t1 = System.currentTimeMillis();
    
    byte[] buffer = new byte[10485760];
    for (;;)
    {
      int elapsed = (int)(System.currentTimeMillis() - t1);
      if (elapsed > this.timeout)
      {
        this.conversionResult.setErrorCode(ConversionResult.TIMED_OUT);
        this.conversionResult.setErrorMessage("Conversion timed out after " + this.timeout + " ms");
        break;
      }
      try
      {
        if ((this.conversionCompleted) && (this.conversionSuccess) && (this.bytesToRead != -1L) && (this.dataStream.size() == this.bytesToRead))
        {
          logger.debug("All bytes are read, exiting conversionlistener thread..");
          this.conversionResult.setContent(this.dataStream.toByteArray());
          break;
        }
        if ((this.conversionCompleted) && (!this.conversionSuccess))
        {
          logger.debug("Conversion failed, exiting listener");
          break;
        }
        int available = this.stream.available();
        if (available != 0)
        {
          logger.debug("Starting to read " + available + " bytes");
          int bytesRead = this.stream.read(buffer, 0, available);
          if (bytesRead == -1)
          {
            this.conversionResult.setErrorCode(ConversionResult.PROCESS_CRASHED);
            this.conversionResult.setErrorMessage("read() returned -1! End of Stream has been reached... Process crashed?");
          }
          else
          {
            this.dataStream.write(buffer, 0, bytesRead);
            logger.debug("Bytes read : " + bytesRead + " , " + this.dataStream.size() + "/" + this.bytesToRead);
          }
        }
        else
        {
          try
          {
            Thread.sleep(5L);
          }
          catch (InterruptedException e)
          {
            e.printStackTrace();
          }
        }
      }
      catch (IOException exception)
      {
        logger.error("Error reading from output stream", exception);
        this.conversionResult.setErrorCode(ConversionResult.PROCESS_CRASHED);
        this.conversionResult.setErrorMessage("Error reading from output stream, Process crashed!");
        break;
      }
    }
  }
  
  public ConversionResult getConversionResult()
  {
    return this.conversionResult;
  }
}

