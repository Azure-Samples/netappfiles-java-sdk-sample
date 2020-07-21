// Copyright (c) Microsoft and contributors.  All rights reserved.
//
// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package sdk.sample;

import sdk.sample.common.CommonSdk;
import sdk.sample.common.ProjectConfiguration;
import sdk.sample.common.Utils;
import sdk.sample.model.ModelCapacityPool;
import sdk.sample.model.ModelNetAppAccount;
import sdk.sample.model.ModelVolume;
import com.microsoft.azure.management.netapp.v2019_11_01.implementation.AzureNetAppFilesManagementClientImpl;
import com.microsoft.azure.management.netapp.v2019_11_01.implementation.CapacityPoolInner;
import com.microsoft.azure.management.netapp.v2019_11_01.implementation.NetAppAccountInner;
import com.microsoft.azure.management.netapp.v2019_11_01.implementation.VolumeInner;

public class Creation
{
    /**
     * Executes basic CRUD operations using Azure NetApp files SDK
     * @param config Project Configuration
     * @param anfClient Azure NetApp Files Management Client
     */
    public static synchronized void runCreationSampleAsync(ProjectConfiguration config, AzureNetAppFilesManagementClientImpl anfClient)
    {
        /*
          Creating ANF Accounts
         */
        Utils.writeConsoleMessage("Creating Azure NetApp Files Account(s)...");
        if (!config.getAccounts().isEmpty())
        {
            for (ModelNetAppAccount modelAccount : config.getAccounts())
            {
                createOrRetrieveAccountAsync(anfClient, config.getResourceGroup(), modelAccount);
            }
        }
        else
        {
            Utils.writeConsoleMessage("No ANF accounts defined within appsettings.json file. Exiting.");
            return;
        }

        /*
          Creating Capacity Pools
         */
        Utils.writeConsoleMessage("Creating Capacity Pool(s)...");
        for (ModelNetAppAccount modelAccount : config.getAccounts())
        {
            if (!modelAccount.getCapacityPools().isEmpty())
            {
                for (ModelCapacityPool capacityPool : modelAccount.getCapacityPools())
                {
                    createOrRetrieveCapacityPoolAsync(anfClient, config.getResourceGroup(), modelAccount, capacityPool);
                }
            }
            else
            {
                Utils.writeConsoleMessage("No capacity pool defined for account " + modelAccount.getName());
            }
        }

        /*
          Creating Volumes
          Note: Volume creation operations at the RP level are executed serially
         */
        Utils.writeConsoleMessage("Creating Volume(s)...");
        for (ModelNetAppAccount modelAccount : config.getAccounts())
        {
            if (!modelAccount.getCapacityPools().isEmpty())
            {
                for (ModelCapacityPool capacityPool : modelAccount.getCapacityPools())
                {
                    if (!capacityPool.getVolumes().isEmpty())
                    {
                        for (ModelVolume modelVolume : capacityPool.getVolumes())
                        {
                            try
                            {
                                createOrRetrieveVolumeAsync(anfClient, config.getResourceGroup(), modelAccount, capacityPool, modelVolume);
                            }
                            catch (Exception e)
                            {
                                Utils.writeErrorMessage("An error occurred while creating volume " + modelAccount.getName() + " " +
                                        capacityPool.getName() + " " + modelVolume.getName() +". Error message: " + e.getMessage());
                            }
                        }
                    }
                    else
                    {
                        Utils.writeConsoleMessage("No volumes defined for Account: " + modelAccount.getName() + ", Capacity Pool: " + capacityPool.getName());
                    }
                }
            }
        }
    }

    /**
     * Creates or retrieves volume
     * @param anfClient Azure NetApp Files Management Client
     * @param resourceGroup Resource Group name where the Volume will be created
     * @param account ModelNetAppAccount object that describes the ANF Account, populated with data from appsettings.json
     * @param pool ModelCapacityPool object that describes the Capacity Pool, populated with data from appsettings.json
     * @param volume ModelVolume object that describes the Volume to be created, populated with data from appsettings.json
     */
    private static synchronized void createOrRetrieveVolumeAsync(AzureNetAppFilesManagementClientImpl anfClient, String resourceGroup, ModelNetAppAccount account, ModelCapacityPool pool, ModelVolume volume)
    {
        String[] params = {resourceGroup, account.getName(), pool.getName(), volume.getName()};

        VolumeInner volumeInner = CommonSdk.getResourceAsync(anfClient, params, VolumeInner.class);
        if (volumeInner == null)
        {
            volumeInner = CommonSdk.createOrUpdateVolumeAsync(anfClient, resourceGroup, account, pool, volume);
            Utils.writeSuccessMessage("Volume successfully created, resource id: " + volumeInner.id());
        }
        else
        {
            Utils.writeConsoleMessage("Volume already exists, resource id: " + volumeInner.id());
        }
    }

    /**
     * Creates or retrieves a Capacity Pool
     * @param anfClient Azure NetApp Files Management Client
     * @param resourceGroup Resource Group name where the Capacity Pool will be created
     * @param account ModelNetAppAccount object that describes the ANF Account, populated with data from appsettings.json
     * @param pool ModelCapacityPool object that describes the Capacity Pool to be created, populated with data from appsettings.json
     */
    private static synchronized void createOrRetrieveCapacityPoolAsync(AzureNetAppFilesManagementClientImpl anfClient, String resourceGroup, ModelNetAppAccount account, ModelCapacityPool pool)
    {
        String[] params = {resourceGroup, account.getName(), pool.getName()};

        CapacityPoolInner capacityPool = CommonSdk.getResourceAsync(anfClient, params, CapacityPoolInner.class);
        if (capacityPool == null)
        {
            capacityPool = CommonSdk.createOrUpdateCapacityPoolAsync(anfClient, resourceGroup, account.getName(), account.getLocation(), pool);
            Utils.writeSuccessMessage("Capacity Pool successfully created, resource id: " + capacityPool.id());
        }
        else
        {
            Utils.writeConsoleMessage("Capacity Pool already exists, resource id: " + capacityPool.id());
        }
    }

    /**
     * Creates or retrieves an Azure NetApp Files Account
     * @param anfClient Azure NetApp Files Management Client
     * @param resourceGroup Resource Group name where the ANF Account will be created
     * @param account ModelNetAppAccount object that describes the ANF Account to be created, populated with data from appsettings.json
     */
    private static synchronized void createOrRetrieveAccountAsync(AzureNetAppFilesManagementClientImpl anfClient, String resourceGroup, ModelNetAppAccount account)
    {
        String[] params = {resourceGroup, account.getName()};

        NetAppAccountInner anfAccount = CommonSdk.getResourceAsync(anfClient, params, NetAppAccountInner.class);
        if (anfAccount == null)
        {
            anfAccount = CommonSdk.createOrUpdateAccountAsync(anfClient, resourceGroup, account);
            Utils.writeSuccessMessage("Account successfully created, resource id: " + anfAccount.id());
        }
        else
        {
            Utils.writeConsoleMessage("Account already exists, resource id: " + anfAccount.id());
        }
    }
}
