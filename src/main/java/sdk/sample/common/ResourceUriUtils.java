// Copyright (c) Microsoft and contributors.  All rights reserved.
//
// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package sdk.sample.common;

// Contains public methods to extract name from ANF resources
public class ResourceUriUtils
{
    /**
     * Gets ANF Account name from resource uri
     * @param resourceUri Value with which to fetch an ANF Account
     * @return Name of Account
     */
    public static String getAnfAccount(String resourceUri)
    {
        if (resourceUri == null || resourceUri.isBlank())
        {
            return null;
        }

        return getResourceValue(resourceUri, "/netAppAccounts");
    }

    /**
     * Gets ANF Capacity pool name from resource uri
     * @param resourceUri Value with which to fetch a Capacity Pool
     * @return Name of Capacity Pool
     */
    public static String getAnfCapacityPool(String resourceUri)
    {
        if (resourceUri == null || resourceUri.isBlank())
        {
            return null;
        }

        return getResourceValue(resourceUri, "/capacityPools");
    }

    /**
     * Gets ANF Volume name from resource uri
     * @param resourceUri Value with which to fetch a Volume
     * @return Name of Volume
     */
    public static String getAnfVolume(String resourceUri)
    {
        if (resourceUri == null || resourceUri.isBlank())
        {
            return null;
        }

        return getResourceValue(resourceUri, "/volumes");
    }

    /**
     * Gets ANF Snapshot name from resource uri
     * @param resourceUri Value with which to fetch a Snapshot
     * @return Name of Snapshot
     */
    public static String getAnfSnapshot(String resourceUri)
    {
        if (resourceUri == null || resourceUri.isBlank())
        {
            return null;
        }

        return getResourceValue(resourceUri, "/snapshots");
    }

    /**
     * Gets the resource group name based on a resource uri
     * @param resourceUri Value with which to fetch a Resource Group
     * @return Name of Resource Group
     */
    public static String getResourceGroup(String resourceUri)
    {
        if (resourceUri == null || resourceUri.isBlank())
        {
            return null;
        }

        return getResourceValue(resourceUri, "/resourceGroups");
    }

    /**
     * Parse the resource value from a resourceUri
     * @param resourceUri Id or similar value of resource
     * @param resourceName Which resource to parse from
     * @return True name of resource
     */
    public static String getResourceValue(String resourceUri, String resourceName)
    {
        if (resourceUri == null || resourceUri.isBlank())
        {
            return null;
        }

        if (!resourceName.startsWith("/"))
        {
            resourceName = "/" + resourceName;
        }

        if (!resourceUri.startsWith("/"))
        {
            resourceUri = "/" + resourceUri;
        }

        // Checks if the resourceName and resourceGroup is the same name, and if so handles it specially
        String rgResourceName = "/resourceGroups" + resourceName;
        int rgIndex = resourceUri.toLowerCase().indexOf(rgResourceName.toLowerCase());
        if (rgIndex != -1) // resourceGroup name and resourceName passed are the same. Example: resourceGroup is "Snapshot" and so is the resourceName
        {
            String[] removedSameRgName = resourceUri.substring(rgIndex+1).split("/");
            return removedSameRgName[1];
        }

        int index = resourceUri.toLowerCase().indexOf(resourceName.toLowerCase());
        if (index != -1)
        {
            String res = resourceUri.substring(index + resourceName.length()).split("/")[1];

            // to handle the partial resource uri that doesn't have real resource name
            if (res.length() > 1)
            {
                return res;
            }
        }

        return null;
    }
}
