package org.meluskyc.codebriefcase.utils;

import android.util.Log;

import org.apache.commons.lang3.ArrayUtils;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;

public class AppUtils {
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
}
