package com.intuit.ctg.tpsconv.pool;

public abstract interface IConversionListener
{
  public abstract void conversionSuccess(long paramLong);
  
  public abstract void conversionFailed(int paramInt, String paramString);
  
  public abstract void conversionDetails(String paramString);
}
