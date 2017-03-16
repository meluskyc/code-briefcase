package org.meluskyc.codebriefcase.utils;

import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;

public class AppUtils {
    public static final String QUERY_PARAMETER_DISTINCT = "distinct";

    public static String formatIpAddress(int ip) {
        byte[] myIPAddress = BigInteger.valueOf(ip).toByteArray();

        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ArrayUtils.reverse(myIPAddress);
        }

        InetAddress myInetIP = null;
        try {
            myInetIP = InetAddress.getByAddress(myIPAddress);
        }
        catch (UnknownHostException e) {
            Log.e("Utils", "Unable to convert IP address");
        }

        return (myInetIP == null) ? null : myInetIP.getHostAddress();
    }

    public static boolean isQueryDistinct(Uri uri){
        return !TextUtils.isEmpty(uri.getQueryParameter(QUERY_PARAMETER_DISTINCT));
    }

    public static String formatQueryDistinctParameter(String parameter){
        return QUERY_PARAMETER_DISTINCT + " " + parameter;
    }

    // http://stackoverflow.com/questions/13070791/android-cursor-to-jsonarray
    public static JSONArray cur2Json(Cursor cursor) {
        JSONArray resultSet = new JSONArray();
        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {
            final int totalColumn = cursor.getColumnCount();
            JSONObject rowObject = new JSONObject();
            int i;
            for (  i = 0; i < totalColumn; i++) {
                if (cursor.getColumnName(i) != null) {
                    Object val;
                    if (cursor.getType(i) == Cursor.FIELD_TYPE_INTEGER) {
                        val = cursor.getLong(i);
                    } else {
                        val = cursor.getString(i);
                    }
                    String getcol = cursor.getColumnName(i);

                    try {
                        rowObject.put(getcol, val);
                    } catch (JSONException e) {
                        Log.e("Server", "Unable to serialize cursor");
                    }
                }
            }
            resultSet.put(rowObject);
            cursor.moveToNext();
        }

        return resultSet;
    }
}
