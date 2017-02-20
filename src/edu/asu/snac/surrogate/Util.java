package edu.asu.snac.surrogate;

import org.json.JSONArray;
import org.json.JSONObject;

public class Util {
  public static final String SERVICE_NAME = "service";
  public static final String METHOD_NAME = "method";
  public static final String PARAM_LIST = "params";

  public static String getMethodName(String input) {
    return new JSONObject(input).getString(METHOD_NAME);
  }

  public static String getServiceName(String input) {
    return new JSONObject(input).getString(SERVICE_NAME);
  }

  public static Object[] getInvokationParams(String input) {
    JSONArray arr = new JSONObject(input).getJSONArray(PARAM_LIST);
    Object[] ret = new Object[arr.length()];
    for (int i = 0; i < arr.length(); i++) {
      ret[i] = arr.get(i);
    }
    return ret;
  }

  @SuppressWarnings("rawtypes")
  public static Class[] getInvokationParamTypes(String input) {
    Object[] params = getInvokationParams(input);
    Class[] ret = new Class[params.length];
    for (int i = 0; i < params.length; i++) {
      ret[i] = params[i].getClass();
    }
    return ret;
  }
}
