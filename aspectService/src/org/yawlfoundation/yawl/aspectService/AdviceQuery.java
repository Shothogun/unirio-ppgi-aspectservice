package org.yawlfoundation.yawl.aspectService;

import java.util.List;

public class AdviceQuery
{
  private Persister _reader;
  private static final String _baseQuery = "FROM Advice AS adv";
  
  public AdviceQuery()
  {
    this._reader = Persister.getInstance();
  }
  
  public List getByAdviceCaseID(String caseID)
  {
    String query = String.format("%s WHERE adv.adviceCaseID = '%s'", new Object[] { "FROM Advice AS adv", caseID });
    return execQuery(query);
  }
  
  public List getByAdviceName(String adviceName)
  {
    String query = String.format("%s WHERE adv.adviceName = '%s'", new Object[] { "FROM Advice AS adv", adviceName });
    return execQuery(query);
  }
  
  public List getAdvicesByAdviceJoinPointID(String adviceJoinPointID)
  {
    String query = String.format("%s WHERE adv.adjpID = '%s'", new Object[] { "FROM Advice AS adv", adviceJoinPointID });
    return execQuery(query);
  }
  
  private List execQuery(String query)
  {
    List rows = null;
    if (this._reader != null)
    {
      rows = this._reader.execQuery(query);
      this._reader.commit();
    }
    return rows;
  }
}