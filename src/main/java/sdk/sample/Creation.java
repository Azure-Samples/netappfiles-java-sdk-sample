// Copyright (c) Microsoft and contributors.  All rights reserved.
//
// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package sdk.sample;

import rx.Observable;
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

import java.util.concurrent.CompletableFuture;

import static com.ea.async.Async.await;

public class Creation
{
    /**
     * Executes basic CRUD operations using Azure NetApp files SDK
     * @param config Project Configuration
     * @param anfClient Azure NetApp Files Management Client
     */
    public static CompletableFuture<Void> runCreationSampleAsync(ProjectConfiguration config, AzureNetAppFilesManagementClientImpl anfClient)
    {
        /*
          Creating ANF Accounts
         */
        Utils.writeConsoleMessage("Creating Azure NetApp Files Account(s)...");
        if (!config.getAccounts().isEmpty())
        {
            config.getAccounts().forEach(account -> await(createOrRetrieveAccountAsync(anfClient, config.getResourceGroup(), account)));
        }
        else
        {
            Utils.writeConsoleMessage("No ANF accounts defined within appsettings.json file. Exiting.");
            return CompletableFuture.completedFuture(null);
        }

        /*
          Creating Capacity Pools
         */
        Utils.writeConsoleMessage("Creating Capacity Pool(s)...");
        for (ModelNetAppAccount modelAccount : config.getAccounts())
        {
            if (!modelAccount.getCapacityPools().isEmpty())
            {
                modelAccount.getCapacityPools().forEach(pool -> await(createOrRetrieveCapacityPoolAsync(anfClient, config.getResourceGroup(), modelAccount, pool)));
            }
            else
            {
                Utils.writeConsoleMessage("No capacity pool defined for account " + modelAccount.getName());
                return CompletableFuture.completedFuture(null);
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
                                Void result = await(createOrRetrieveVolumeAsync(anfClient, config.getResourceGroup(), modelAccount, capacityPool, modelVolume));
                            }
                            catch (Exception e)
                            {
                                Utils.writeErrorMessage("An error occurred while creating volume " + modelAccount.getName() + " " +
                                        capacityPool.getName() + " " + modelVolume.getName() + ".\nError message: " + e.getMessage());
                                throw e;
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

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Creates or retrieves volume
     * @param anfClient Azure NetApp Files Management Client
     * @param resourceGroup Resource Group name where the Volume will be created
     * @param account ModelNetAppAccount object that describes the ANF Account, populated with data from appsettings.json
     * @param pool ModelCapacityPool object that describes the Capacity Pool, populated with data from appsettings.json
     * @param volume ModelVolume object that describes the Volume to be created, populated with data from appsettings.json
     */
    private static CompletableFuture<Void> createOrRetrieveVolumeAsync(AzureNetAppFilesManagementClientImpl anfClient, String resourceGroup, ModelNetAppAccount account, ModelCapacityPool pool, ModelVolume volume)
    {
        String[] params = {resourceGroup, account.getName(), pool.getName(), volume.getName()};

        Observable<VolumeInner> anfVolume = await(CommonSdk.getResourceAsync(anfClient, params, VolumeInner.class));
        anfVolume.subscribe(volumeInner -> {
            if (volumeInner == null)
            {
                Observable<VolumeInner> newVolume = await(CommonSdk.createOrUpdateVolumeAsync(anfClient, resourceGroup, account, pool, volume));
                Utils.writeSuccessMessage("Volume successfully created, resource id: " + newVolume.toBlocking().first().id());
            }
            else
            {
                Utils.writeConsoleMessage("Volume already exists, resource id: " + volumeInner.id());
            }
        });

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Creates or retrieves a Capacity Pool
     * @param anfClient Azure NetApp Files Management Client
     * @param resourceGroup Resource Group name where the Capacity Pool will be created
     * @param account ModelNetAppAccount object that describes the ANF Account, populated with data from appsettings.json
     * @param pool ModelCapacityPool object that describes the Capacity Pool to be created, populated with data from appsettings.json
     */
    private static CompletableFuture<Void> createOrRetrieveCapacityPoolAsync(AzureNetAppFilesManagementClientImpl anfClient, String resourceGroup, ModelNetAppAccount account, ModelCapacityPool pool)
    {
        String[] params = {resourceGroup, account.getName(), pool.getName()};

        Observable<CapacityPoolInner> capacityPool = await(CommonSdk.getResourceAsync(anfClient, params, CapacityPoolInner.class));
        capacityPool.subscribe(capacityPoolInner -> {
            if (capacityPoolInner == null)
            {
                Observable<CapacityPoolInner> newCapacityPool = await(CommonSdk.createOrUpdateCapacityPoolAsync(anfClient, resourceGroup, account.getName(), account.getLocation(), pool));
                Utils.writeSuccessMessage("Capacity Pool successfully created, resource id: " + newCapacityPool.toBlocking().first().id());
            }
            else
            {
                Utils.writeConsoleMessage("Capacity Pool already exists, resource id: " + capacityPoolInner.id());
            }
        });

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Creates or retrieves an Azure NetApp Files Account
     * @param anfClient Azure NetApp Files Management Client
     * @param resourceGroup Resource Group name where the ANF Account will be created
     * @param account ModelNetAppAccount object that describes the ANF Account to be created, populated with data from appsettings.json
     */
    private static CompletableFuture<Void> createOrRetrieveAccountAsync(AzureNetAppFilesManagementClientImpl anfClient, String resourceGroup, ModelNetAppAccount account)
    {
        String[] params = {resourceGroup, account.getName()};

        Observable<NetAppAccountInner> anfAccount = await(CommonSdk.getResourceAsync(anfClient, params, NetAppAccountInner.class));
        anfAccount.subscribe(netAppAccountInner -> {
            if (netAppAccountInner == null)
            {
                Observable<NetAppAccountInner> newAccount = await(CommonSdk.createOrUpdateAccountAsync(anfClient, resourceGroup, account));
                Utils.writeSuccessMessage("Account successfully created, resource id: " + newAccount.toBlocking().first().id());
            }
            else
            {
                Utils.writeConsoleMessage("Account already exists, resource id: " + netAppAccountInner.id());
            }
        });

        return CompletableFuture.completedFuture(null);
    }
}
