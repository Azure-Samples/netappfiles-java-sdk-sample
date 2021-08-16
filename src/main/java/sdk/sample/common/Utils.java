// Copyright (c) Microsoft and contributors.  All rights reserved.
//
// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package sdk.sample.common;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

// Contains public methods to get configuration settings, display app header, conversion of bytes, etc.
public class Utils
{
    static DateTimeFormatter pattern = DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * Simple function to display this console app basic information
     */
    public static void displayConsoleAppHeader()
    {
        System.out.println("Azure NetAppFiles Java SDK Samples - Sample project that performs CRUD management operations with Azure NetApp Files SDK");
        System.out.println("----------------------------------------------------------------------------------------------------------------------------");
        System.out.println();
    }

    /**
     * Create the configuration object containing ANF resource information
     * @param filename Name of file configuration file
     * @return Project Configuration as read from the json file
     */
    public static ProjectConfiguration getConfiguration(String filename)
    {
        return ProjectConfiguration.readFromJsonFile(filename);
    }

    /**
     * Display console messages
     * @param message Message to be written in console
     */
    public static void writeConsoleMessage(String message)
    {
        System.out.println(LocalTime.now().format(pattern) + " " + message);
    }

    /**
     * Displays errors messages in red
     * @param message Message to be written in console
     */
    public static void writeErrorMessage(String message)
    {
        System.out.println(LocalTime.now().format(pattern) + " " + ConsoleColors.RED + message + ConsoleColors.RESET);
    }

    /**
     * Display success messages in green
     * @param message Message to be written in console
     */
    public static void writeSuccessMessage(String message)
    {
        System.out.println(LocalTime.now().format(pattern) + " " + ConsoleColors.GREEN + message + ConsoleColors.RESET);
    }

    public static void writeWarningMessage(String message)
    {
        System.out.println(LocalTime.now().format(pattern) + " " + ConsoleColors.YELLOW + message + ConsoleColors.RESET);
    }

    /**
     * Converts bytes into TB, rounded to one decimal point
     * @param size Size in bytes to convert
     * @return the value of bytes in terabytes
     */
    public static double getTBFromBytes(long size)
    {
        double tb = size / 1024.0 / 1024 / 1024 / 1024;
        return Math.round(tb * 10) / 10.0;
    }

    /**
     * Converts TB into bytes
     * @param size Size in TB to convert
     * @return the value of terabytes in bytes
     */
    public static long getBytesFromTB(int size)
    {
        return size * 1024L * 1024L * 1024L * 1024L;
    }

    /**
     * A simple, albeit not recommended, method to suppress Illegal Reflective Access warnings
     */
    public static void suppressWarning()
    {
        System.err.close();
        System.setErr(System.out);
    }

    /**
     * Simple method to make current thread sleep a given amount of time
     * @param millisecond the time in millisecond the thread should sleep
     */
    public static void threadSleep(int millisecond)
    {
        try
        {
            Thread.sleep(millisecond);
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
        }
    }

    private static class ConsoleColors
    {
        public static final String RESET = "\033[0m";

        public static final String RED = "\033[0;31m";
        public static final String GREEN = "\033[0;32m";
        public static final String YELLOW = "\033[0;33m";
    }
}
