package org.yawlfoundation.yawl.elements.state;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.yawlfoundation.yawl.elements.YCondition;
import org.yawlfoundation.yawl.elements.YExternalNetElement;
import org.yawlfoundation.yawl.elements.YNet;
import org.yawlfoundation.yawl.elements.YNetElement;
import org.yawlfoundation.yawl.elements.YTask;

public class YOrJoinUtils
{
  public static synchronized Set reduceToEnabled(YMarking marking, YTask orJoin)
  {
    if ((marking == null) || (orJoin == null)) {
      return null;
    }
    Set visitedFw = new HashSet();
    Set visitingFw = new HashSet();
    for (Iterator iterator = marking.getLocations().iterator(); iterator.hasNext();)
    {
      YNetElement element = (YNetElement)iterator.next();
      if ((element instanceof YExternalNetElement)) {
        visitingFw.add(element);
      }
    }
    Set visitedBk = new HashSet();
    Set visitingBk = new HashSet();
    visitingBk.add(orJoin);
    do
    {
      visitedFw.addAll(visitingFw);
      visitingFw = YNet.getPostset(visitingFw);
      HashSet orJoins = new HashSet();
      for (Iterator iterator = visitingFw.iterator(); iterator.hasNext();)
      {
        YExternalNetElement element = (YExternalNetElement)iterator.next();
        if (((element instanceof YTask)) && 
          (((YTask)element).getJoinType() == 103)) {
          orJoins.add(element);
        }
      }
      visitingFw.removeAll(orJoins);
      visitingFw.removeAll(visitedFw);
    } while (visitingFw.size() > 0);
    do
    {
      visitedBk.addAll(visitingBk);
      visitingBk = YNet.getPreset(visitingBk);
      HashSet orJoins = new HashSet();
      for (Iterator iterator = visitingBk.iterator(); iterator.hasNext();)
      {
        YExternalNetElement element = (YExternalNetElement)iterator.next();
        if (((element instanceof YTask)) && 
          (((YTask)element).getJoinType() == 103)) {
          orJoins.add(element);
        }
      }
      visitingBk.removeAll(orJoins);
      visitingBk.removeAll(visitedBk);
    } while (visitingBk.size() > 0);
    Set enabledNetElements = new HashSet();
    for (Iterator iterator = visitedFw.iterator(); iterator.hasNext();)
    {
      YExternalNetElement element = (YExternalNetElement)iterator.next();
      if ((visitedBk.contains(element)) && 
        ((element instanceof YTask)) && 
        (marking.nonOrJoinEnabled((YTask)element))) {
        enabledNetElements.add(element);
      }
    }
    return enabledNetElements;
  }
  
  public static synchronized YTask pickOptimalEnabledTask(Set enabledTasks, YTask orJoin)
  {
    YTask taskWithShortestPath = null;
    int numSteps = Integer.MAX_VALUE;
    for (Iterator iterator = enabledTasks.iterator(); iterator.hasNext();)
    {
      YTask task = (YTask)iterator.next();
      int num = countStepsInShortestPath(task, orJoin);
      if (taskWithShortestPath == null)
      {
        taskWithShortestPath = task;
        numSteps = num;
      }
      else if (num < numSteps)
      {
        taskWithShortestPath = task;
        numSteps = num;
      }
    }
    return taskWithShortestPath;
  }
  
  public static synchronized YTask pickOptimalEnabledTask(Set enabledTasks, YTask orJoin, YMarking currentlyConsideredMarking, YSetOfMarkings markingsAlreadyConsidered)
  {
    Set enabledTasksThatWillChangeTheMarking = getEnabledTasksThatWillChangeTheMarking(enabledTasks, orJoin, currentlyConsideredMarking, markingsAlreadyConsidered);
    if (enabledTasksThatWillChangeTheMarking.size() > 0) {
      return pickOptimalEnabledTask(enabledTasksThatWillChangeTheMarking, orJoin);
    }
    return pickOptimalEnabledTask(enabledTasks, orJoin);
  }
  
  private static Set getEnabledTasksThatWillChangeTheMarking(Set enabledTasks, YTask orjoin, YMarking currentlyConsideredMarking, YSetOfMarkings markingsAlreadyConsidered)
  {
    Set enabledTasksThatWillChangeTheMarking = new HashSet();
    for (Iterator iterator = enabledTasks.iterator(); iterator.hasNext();)
    {
      YTask task = (YTask)iterator.next();
      Set outputConditions = task.getPostsetElements();
      if (!currentlyConsideredMarking.getLocations().containsAll(outputConditions)) {
        enabledTasksThatWillChangeTheMarking.add(task);
      }
    }
    return enabledTasksThatWillChangeTheMarking;
  }
  
  public static Set getRelevantPostset(YTask aTask, YTask orJoin)
  {
    Set returnSet = new HashSet();
    Set visiting = new HashSet();
    visiting.add(orJoin);
    Set visited = new HashSet();
    while (visiting.size() > 0)
    {
      visited.addAll(visiting);
      visiting = YNet.getPreset(visiting);
      visiting.removeAll(visited);
    }
    Iterator postsetIter = aTask.getPostsetElements().iterator();
    while (postsetIter.hasNext())
    {
      YCondition condition = (YCondition)postsetIter.next();
      if (visited.contains(condition)) {
        returnSet.add(condition);
      }
    }
    return returnSet;
  }
  
  private static synchronized int countStepsInShortestPath(YTask task, YTask orJoin)
  {
    Set visiting = new HashSet();
    visiting.add(task);
    Set visited = new HashSet();
    for (int i = 0; visiting.size() > 0; i++)
    {
      if (visiting.contains(orJoin)) {
        return i;
      }
      visited.addAll(visiting);
      visiting = YNet.getPostset(visiting);
      visiting.removeAll(visited);
    }
    throw new RuntimeException("There is no number of steps between unconnected tasks.");
  }
}
