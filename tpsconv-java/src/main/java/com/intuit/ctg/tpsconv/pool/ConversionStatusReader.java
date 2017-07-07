package com.intuit.ctg.tpsconv.pool;

import java.io.BufferedReader;
import java.io.Reader;
import org.apache.log4j.Logger;

public class ConversionStatusReader
  implements Runnable
{
  static Logger logger = Logger.getLogger(ConversionStatusReader.class);
  private IConversionListener conversionListener;
  private Reader errorReader;
  private volatile boolean error;
  
  public ConversionStatusReader(Reader errorReader, IConversionListener conversionListener)
  {
    this.errorReader = errorReader;
    this.conversionListener = conversionListener;
  }
  
  public void run()
  {
    try
    {
      BufferedReader reader = new BufferedReader(this.errorReader);
      String option = null;
      
      long bytesToRead = 0L;
      StringBuffer errorContext = new StringBuffer();
      boolean isErrorContext = false;
      int errorCode = -1;
      int contextStartCount = 0;int contextEndCount = 0;
      while ((option = reader.readLine()) != null)
      {
        logger.debug("Option : " + option);
        if (isErrorContext)
        {
          errorContext.append(option + "\n");
          if (option.startsWith("</ERROR_CONTEXT>"))
          {
            contextEndCount++;
            if (contextStartCount == contextEndCount)
            {
              isErrorContext = false;
              this.conversionListener.conversionFailed(errorCode, errorContext.toString());
              logger.debug("Captured Error Context. Exiting ConversionStatusReader");
              logger.info("Error Context : \n" + errorContext);
              this.error = true;
              break;
            }
          }
        }
        if (option.contains("<DATA size="))
        {
          String dataSize = option.replace("<DATA size=\"", "").replace("\"/>", "");
          bytesToRead = Long.parseLong(dataSize);
        }
        else if (option.startsWith("<ERROR code="))
        {
          errorCode = Integer.parseInt(option.replace("<ERROR code=\"", "").replace("\"/>", ""));
          logger.info("Conversion completed, ERROR code : " + errorCode + " : data size = " + bytesToRead);
          if (errorCode == 0)
          {
            this.conversionListener.conversionSuccess(bytesToRead);
            break;
          }
        }
        else if (option.startsWith("<ERROR_CONTEXT>"))
        {
          contextStartCount++;
          isErrorContext = true;
        }
        else
        {
          this.conversionListener.conversionDetails(option);
        }
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  
  public boolean isError()
  {
    return this.error;
  }
}