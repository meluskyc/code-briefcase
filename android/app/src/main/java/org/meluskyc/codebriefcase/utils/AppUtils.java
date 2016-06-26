package org.meluskyc.codebriefcase.utils;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import org.apache.commons.lang3.ArrayUtils;

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
}
