/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azuretools.utils;

import org.apache.commons.lang3.ArrayUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Enumeration;

/**
 * Networking-related convenience methods.
 */
public final class NetUtils {

    private NetUtils() {
        // empty
    }

    /**
     *  Returns the local network interface's MAC address if possible. The local network interface is defined here as
     *  the {@link NetworkInterface} that is both up and not a loopback interface.
     *
     * @return the MAC address of the local network interface or {@code null} if no MAC address could be determined.
     */
    public static byte[] getMacAddress() {
        byte[] mac = null;
        try {
            final InetAddress localHost = InetAddress.getLocalHost();
            try {
                final NetworkInterface localInterface = NetworkInterface.getByInetAddress(localHost);
                if (isUpAndNotLoopback(localInterface)) {
                    mac = localInterface.getHardwareAddress();
                }
                if (mac == null) {
                    final Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
                    if (networkInterfaces != null) {
                        while (networkInterfaces.hasMoreElements() && mac == null) {
                            final NetworkInterface nic = networkInterfaces.nextElement();
                            if (isUpAndNotLoopback(nic)) {
                                mac = nic.getHardwareAddress();
                            }
                        }
                    }
                }
            } catch (final SocketException e) {
                // log.error(e);
            }
            if (ArrayUtils.isEmpty(mac) && localHost != null) {
                // Emulate a MAC address with an IP v4 or v6
                final byte[] address = localHost.getAddress();
                // Take only 6 bytes if the address is an IPv6 otherwise will pad with two zero bytes
                mac = Arrays.copyOf(address, 6);
            }
        } catch (final UnknownHostException ignored) {
            // ignored
        }
        return mac;
    }

    /**
     * Returns the mac address, if it is available, as a string with each byte separated by a ":" character.
     * @return the mac address String or null.
     */
    public static String getMacAddressString() {
        final byte[] macAddr = getMacAddress();
        if (!ArrayUtils.isEmpty(macAddr)) {
            StringBuilder sb = new StringBuilder(String.format("%02X", macAddr[0]));
            for (int i = 1; i < macAddr.length; ++i) {
                sb.append("-").append(String.format("%02X", macAddr[i]));
            }
            return sb.toString();

        }
        return null;
    }

    private static boolean isUpAndNotLoopback(final NetworkInterface ni) throws SocketException {
        return ni != null && !ni.isLoopback() && ni.isUp();
    }

}
