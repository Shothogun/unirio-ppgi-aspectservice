package org.yawlfoundation.yawl.aspectService;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.exception.JDBCConnectionException;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;

public class HibernateEngine
{
  public static final int DB_UPDATE = 0;
  public static final int DB_DELETE = 1;
  public static final int DB_INSERT = 2;
  private static SessionFactory _factory = null;
  private static HibernateEngine _me;
  private static Class[] persistedClasses = { Advice.class };
  private static boolean _persistOn = false;
  private static final Logger _log = Logger.getLogger(HibernateEngine.class);
  
  private HibernateEngine(boolean persistenceOn)
    throws HibernateException
  {
    _persistOn = persistenceOn;
    initialise();
  }
  
  public static HibernateEngine getInstance(boolean persistenceOn)
  {
    if (_me == null) {
      try
      {
        _me = new HibernateEngine(persistenceOn);
      }
      catch (HibernateException he)
      {
        _persistOn = false;
        _log.error("Could not initialise database connection.", he);
      }
    }
    return _me;
  }
  
  private void initialise()
    throws HibernateException
  {
    try
    {
      Configuration _cfg = new Configuration();
      for (Class persistedClass : persistedClasses) {
        _cfg.addClass(persistedClass);
      }
      _factory = _cfg.buildSessionFactory();
      
      new SchemaUpdate(_cfg).execute(false, true);
    }
    catch (MappingException me)
    {
      _log.error("Could not initialise database connection.", me);
    }
  }
  
  public boolean isAvailable(String tableName)
  {
    try
    {
      Session session = _factory.getCurrentSession();
      getOrBeginTransaction();
      Query query = session.createQuery("from " + tableName).setMaxResults(1);
      boolean hasTable = query.list().size() > 0;
      commit();
      return hasTable;
    }
    catch (Exception e) {}
    return false;
  }
  
  public boolean isPersisting()
  {
    return _persistOn;
  }
  
  public boolean exec(Object obj, int action)
  {
    return exec(obj, action, true);
  }
  
  public boolean exec(Object obj, int action, boolean commit)
  {
    Transaction tx = null;
    try
    {
      Session session = _factory.getCurrentSession();
      tx = getOrBeginTransaction();
      if (action == 2) {
        session.save(obj);
      } else if (action == 0) {
        updateOrMerge(session, obj);
      } else if (action == 1) {
        session.delete(obj);
      }
      if (commit) {
        tx.commit();
      }
      return true;
    }
    catch (HibernateException he)
    {
      _log.error("Handled Exception: Error persisting object (" + actionToString(action) + "): " + obj.toString(), he);
      if (tx != null) {
        tx.rollback();
      }
    }
    return false;
  }
  
  public boolean exec(Object obj, int action, Transaction tx)
  {
    try
    {
      Session session = _factory.getCurrentSession();
      if (action == 2) {
        session.save(obj);
      } else if (action == 0) {
        updateOrMerge(session, obj);
      } else if (action == 1) {
        session.delete(obj);
      }
      return true;
    }
    catch (HibernateException he)
    {
      _log.error("Handled Exception: Error persisting object (" + actionToString(action) + "): " + obj.toString(), he);
      if (tx != null) {
        tx.rollback();
      }
    }
    return false;
  }
  
  private void updateOrMerge(Session session, Object obj)
  {
    try
    {
      session.saveOrUpdate(obj);
    }
    catch (Exception e)
    {
      session.merge(obj);
    }
  }
  
  public Transaction getOrBeginTransaction()
  {
    try
    {
      Transaction tx = _factory.getCurrentSession().getTransaction();
      return (tx != null) && (tx.isActive()) ? tx : beginTransaction();
    }
    catch (HibernateException he)
    {
      _log.error("Caught Exception: Error creating or getting transaction", he);
    }
    return null;
  }
  
  public List execQuery(String queryString)
  {
    List result = null;
    Transaction tx = null;
    try
    {
      Session session = _factory.getCurrentSession();
      tx = getOrBeginTransaction();
      Query query = session.createQuery(queryString);
      if (query != null) {
        result = query.list();
      }
    }
    catch (JDBCConnectionException jce)
    {
      _log.error("Caught Exception: Couldn't connect to datasource - starting with an empty dataset");
    }
    catch (HibernateException he)
    {
      _log.error("Caught Exception: Error executing query: " + queryString, he);
      if (tx != null) {
        tx.rollback();
      }
    }
    return result;
  }
  
  public int execUpdate(String queryString)
  {
    return execUpdate(queryString, true);
  }
  
  public int execUpdate(String queryString, boolean commit)
  {
    int result = -1;
    Transaction tx = null;
    try
    {
      Session session = _factory.getCurrentSession();
      tx = getOrBeginTransaction();
      result = session.createQuery(queryString).executeUpdate();
      if (commit) {
        tx.commit();
      }
    }
    catch (JDBCConnectionException jce)
    {
      _log.error("Caught Exception: Couldn't connect to datasource - starting with an empty dataset");
    }
    catch (HibernateException he)
    {
      _log.error("Caught Exception: Error executing query: " + queryString, he);
      if (tx != null) {
        tx.rollback();
      }
    }
    return result;
  }
  
  public Query createQuery(String queryString)
  {
    Transaction tx = null;
    try
    {
      Session session = _factory.getCurrentSession();
      tx = getOrBeginTransaction();
      return session.createQuery(queryString);
    }
    catch (HibernateException he)
    {
      _log.error("Caught Exception: Error creating query: " + queryString, he);
      if (tx != null) {
        tx.rollback();
      }
    }
    return null;
  }
  
  public Transaction beginTransaction()
  {
    return _factory.getCurrentSession().beginTransaction();
  }
  
  public Object load(Class claz, Serializable key)
  {
    getOrBeginTransaction();
    return _factory.getCurrentSession().load(claz, key);
  }
  
  public Object get(Class claz, Serializable key)
  {
    getOrBeginTransaction();
    return _factory.getCurrentSession().get(claz, key);
  }
  
  public void commit()
  {
    try
    {
      Transaction tx = _factory.getCurrentSession().getTransaction();
      if ((tx != null) && (tx.isActive())) {
        tx.commit();
      }
    }
    catch (HibernateException he)
    {
      _log.error("Caught Exception: Error committing transaction", he);
    }
  }
  
  public void rollback()
  {
    try
    {
      Transaction tx = _factory.getCurrentSession().getTransaction();
      if ((tx != null) && (tx.isActive())) {
        tx.rollback();
      }
    }
    catch (HibernateException he)
    {
      _log.error("Caught Exception: Error rolling back transaction", he);
    }
  }
  
  public void closeFactory()
  {
    if (_factory != null) {
      _factory.close();
    }
  }
  
  public List execJoinQuery(String table, String field, String value)
  {
    String qry = String.format("from %s parent where '%s' in elements(parent.%s)", new Object[] { table, value, field });
    
    return execQuery(qry);
  }
  
  public Object selectScalar(String className, String field, String value)
  {
    String qry = String.format("from %s as tbl where tbl.%s = '%s'", new Object[] { className, field, value });
    
    List result = execQuery(qry);
    if ((result != null) && 
      (!result.isEmpty())) {
      return result.iterator().next();
    }
    return null;
  }
  
  public List getObjectsForClass(String className)
  {
    return execQuery("from " + className);
  }
  
  public List getObjectsForClassWhere(String className, String whereClause)
  {
    List result = null;
    try
    {
      String qry = String.format("from %s as tbl where tbl.%s", new Object[] { className, whereClause });
      
      result = execQuery(qry);
    }
    catch (HibernateException he)
    {
      _log.error("Error reading data for class: " + className, he);
    }
    return result;
  }
  
  private String actionToString(int action)
  {
    String result = null;
    switch (action)
    {
    case 0: 
      result = "update"; break;
    case 1: 
      result = "delete"; break;
    case 2: 
      result = "insert";
    }
    return result;
  }
}