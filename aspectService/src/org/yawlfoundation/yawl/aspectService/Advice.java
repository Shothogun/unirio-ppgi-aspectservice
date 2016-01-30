package org.yawlfoundation.yawl.aspectService;

public class Advice
{
  private long id;
  private String parentSpecID;
  private String parentSpecName;
  private String parentCaseID;
  private String adjpID;
  private String adjpChildID;
  private String adjpDataString;
  private String adviceName;
  private String adviceCaseID;
  private String proceedID;
  private boolean synched;
  private String proceedName;
  
  public String getProceedName()
  {
    return this.proceedName;
  }
  
  public void setProceedName(String proceedName)
  {
    this.proceedName = proceedName;
  }
  
  public String getAdviceName()
  {
    return this.adviceName;
  }
  
  public void setAdviceName(String adviceName)
  {
    this.adviceName = adviceName;
  }
  
  public long getId()
  {
    return this.id;
  }
  
  public void setId(long id)
  {
    this.id = id;
  }
  
  public String getParentSpecID()
  {
    return this.parentSpecID;
  }
  
  public void setParentSpecID(String parentSpecID)
  {
    this.parentSpecID = parentSpecID;
  }
  
  public String getAdjpChildID()
  {
    return this.adjpChildID;
  }
  
  public void setAdjpChildID(String adjpChildID)
  {
    this.adjpChildID = adjpChildID;
  }
  
  public String getParentSpecName()
  {
    return this.parentSpecName;
  }
  
  public void setParentSpecName(String parentSpecName)
  {
    this.parentSpecName = parentSpecName;
  }
  
  public String getParentCaseID()
  {
    return this.parentCaseID;
  }
  
  public void setParentCaseID(String parentCaseID)
  {
    this.parentCaseID = parentCaseID;
  }
  
  public String getAdjpID()
  {
    return this.adjpID;
  }
  
  public void setAdjpID(String adjpID)
  {
    this.adjpID = adjpID;
  }
  
  public String getAdjpDataString()
  {
    return this.adjpDataString;
  }
  
  public void setAdjpDataString(String adjpDataString)
  {
    this.adjpDataString = adjpDataString;
  }
  
  public String getAdviceCaseID()
  {
    return this.adviceCaseID;
  }
  
  public void setAdviceCaseID(String adviceCaseID)
  {
    this.adviceCaseID = adviceCaseID;
  }
  
  public String getProceedID()
  {
    return this.proceedID;
  }
  
  public void setProceedID(String proceedID)
  {
    this.proceedID = proceedID;
  }
  
  public boolean isSynched()
  {
    return this.synched;
  }
  
  public void setSynched(boolean synched)
  {
    this.synched = synched;
  }
}