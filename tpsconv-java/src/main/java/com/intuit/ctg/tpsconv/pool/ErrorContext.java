package com.intuit.ctg.tpsconv.pool;

import java.util.ArrayList;
import java.util.List;

public class ErrorContext
{
  private List<String> lines;
  private ErrorContext subConext;
  
  public ErrorContext()
  {
    this.lines = new ArrayList();
  }
  
  public List<String> getLines()
  {
    return this.lines;
  }
  
  public ErrorContext getSubConext()
  {
    return this.subConext;
  }
  
  public void setSubConext(ErrorContext subConext)
  {
    this.subConext = subConext;
  }
}
