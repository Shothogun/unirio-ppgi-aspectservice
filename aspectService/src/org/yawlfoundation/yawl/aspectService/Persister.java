package org.yawlfoundation.yawl.aspectService;

import java.io.Serializable;
import java.util.List;
import org.hibernate.Query;
import org.hibernate.Transaction;

public final class Persister
  implements Serializable
{
  private HibernateEngine _db;
  private static Persister _me;
  private static final int _UPDATE = 0;
  private static final int _DELETE = 1;
  private static final int _INSERT = 2;
  
  private Persister()
  {
    this._db = HibernateEngine.getInstance(true);
  }
  
  public static Persister getInstance()
  {
    if (_me == null) {
      _me = new Persister();
    }
    return _me;
  }
  
  public List select(Object obj)
  {
    return select(obj.getClass().getName());
  }
  
  public List select(String className)
  {
    return this._db.getObjectsForClass(className);
  }
  
  public List selectWhere(String className, String whereClause)
  {
    return this._db.getObjectsForClassWhere(className, whereClause);
  }
  
  public List execQuery(String query)
  {
    return this._db.execQuery(query);
  }
  
  public int execUpdate(String statement)
  {
    return this._db.execUpdate(statement);
  }
  
  public int execUpdate(String statement, boolean commit)
  {
    return this._db.execUpdate(statement, commit);
  }
  
  public Query createQuery(String query)
  {
    return this._db.createQuery(query);
  }
  
  public Transaction beginTransaction()
  {
    return this._db.beginTransaction();
  }
  
  public Transaction getOrBeginTransaction()
  {
    return this._db.getOrBeginTransaction();
  }
  
  public Object load(Class claz, Serializable key)
  {
    return this._db.load(claz, key);
  }
  
  public Object get(Class claz, Serializable key)
  {
    return this._db.get(claz, key);
  }
  
  public void commit()
  {
    this._db.commit();
  }
  
  public void rollback()
  {
    this._db.rollback();
  }
  
  public void closeDB()
  {
    this._db.closeFactory();
  }
  
  public Object selectScalar(String className, String id)
  {
    //Object retObj;
    Object retObj;
    if (className.endsWith("Participant"))
    {
      retObj = this._db.selectScalar(className, "_resourceID", id);
    }
    else
    {
      //Object retObj;
      if (className.endsWith("UserPrivileges"))
      {
        retObj = this._db.selectScalar(className, "_participantID", id);
      }
      else
      {
        //Object retObj;
        if ((className.endsWith("QueueSet")) || (className.endsWith("WorkQueue")))
        {
          retObj = this._db.selectScalar(className, "_ownerID", id);
        }
        else
        {
          //Object retObj;
          if (className.endsWith("AutoTask")) {
            retObj = this._db.selectScalar(className, "_wirID", id);
          } else {
            retObj = this._db.selectScalar(className, "_id", id);
          }
        }
      }
    }
    return retObj;
  }
  
  public void update(Object obj)
  {
    this._db.exec(obj, 0);
  }
  
  public void delete(Object obj)
  {
    this._db.exec(obj, 1);
  }
  
  public void insert(Object obj)
  {
    this._db.exec(obj, 2);
  }
  
  public void update(Object obj, Transaction tx)
  {
    this._db.exec(obj, 0, tx);
  }
  
  public void delete(Object obj, Transaction tx)
  {
    this._db.exec(obj, 1, tx);
  }
  
  public void insert(Object obj, Transaction tx)
  {
    this._db.exec(obj, 2, tx);
  }
  
  public void update(Object obj, boolean commit)
  {
    this._db.exec(obj, 0, commit);
  }
  
  public void delete(Object obj, boolean commit)
  {
    this._db.exec(obj, 1, commit);
  }
  
  public void insert(Object obj, boolean commit)
  {
    this._db.exec(obj, 2, commit);
  }
}