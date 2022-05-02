package org.transitclock.utils;

import java.util.ArrayList;
import java.util.List;

public class QueryBuilder {


  public static String buildInClause(List<String> args) {
    StringBuffer sb = new StringBuffer();
    for (String arg : args) {
      sb.append(arg)
              .append(",");
    }
    return sb.substring(0, sb.length()-1);
  }
}
