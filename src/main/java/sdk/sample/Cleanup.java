// Copyright (c) Microsoft and contributors.  All rights reserved.
//
// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package sdk.sample;

import sdk.sample.common.CommonSdk;
import sdk.sample.common.ProjectConfiguration;
import sdk.sample.common.ResourceUriUtils;
import sdk.sample.common.Utils;
import sdk.sample.model.ModelCapacityPool;
import sdk.sample.model.ModelNetAppAccount;
import sdk.sample.model.ModelVolume;
import com.microsoft.azure.management.netapp.v2019_11_01.implementation.AzureNetAppFilesManagementClientImpl;
import com.microsoft.azure.management.netapp.v2019_11_01.implementation.CapacityPoolInner;
import com.microsoft.azure.management.netapp.v2019_11_01.implementation.NetAppAccountInner;
import com.microsoft.azure.management.netapp.v2019_11_01.implementation.SnapshotInner;
import com.microsoft.azure.management.netapp.v2019_11_01.implementation.VolumeInner;
import com.microsoft.rest.ServiceResponse;
import rx.Observable;

import java.util.List;

public class Cleanup
{
    /**
     * Removes all created resources
     * @param config Project Configuration
     * @param anfClient Azure NetApp Files Management Client
     */
    public static synchronized void runCleanupTasksSampleAsync(ProjectConfiguration config, AzureNetAppFilesManagementClientImpl anfClient)
    {
        // Disable Illegal Reflective Access warning related to accessing Observable<ServiceResponse<Void>>
        Utils.suppressWarning();

        /*
          Clean up snapshots
         */
        Utils.writeConsoleMessage("Cleaning up Snapshot(s)...");
        for (ModelNetAppAccount account : config.getAccounts())
        {
            if (account.getCapacityPools() != null)
            {
                for (ModelCapacityPool pool : account.getCapacityPools())
                {
                    if (pool.getVolumes() != null)
                    {
                        for (ModelVolume volume : pool.getVolumes())
                        {
                            String[] parameters = {config.getResourceGroup(), account.getName(), pool.getName(), volume.getName()};
                            List<?> snapshots = CommonSdk.listResourceAsync(anfClient, parameters, SnapshotInner.class);
                            if (snapshots != null && snapshots.size() > 0)
                            {
                                /*
                                  Snapshot name property (and other ANF's related nested resources) return a relative path up to the name
                                  and to use this property with deleteAsync for example, the argument needs to be sanitized and just the
                                  actual name needs to be used.
                                  Snapshot name property example: "johndoe-anf01/pool01/johndoe-anf01-pool01-vol01/test-a"
                                  "test-a" is the actual name that needs to be used. Below is a sample function that parses the name
                                  from snapshot resource id
                                 */
                                for (Object snapshotObject : snapshots)
                                {
                                    SnapshotInner snapshot = (SnapshotInner) snapshotObject;
                                    try
                                    {
                                        Observable<ServiceResponse<Void>> response = anfClient.snapshots().deleteWithServiceResponseAsync(config.getResourceGroup(), account.getName(), pool.getName(), volume.getName(), ResourceUriUtils.getAnfSnapshot(snapshot.id()));
                                        Utils.writeConsoleMessage("Correlation-id of Snapshot DELETE request: " + response.toBlocking().first().response().headers().get("x-ms-correlation-request-id"));
                                    }
                                    catch (Exception e)
                                    {
                                        Utils.writeErrorMessage("An error occurred while deleting Snapshot: " + snapshot.id());
                                        Utils.writeConsoleMessage("Error: " + e);
                                        throw e;
                                    }

                                    // Adding a final verification if the resource completed deletion since it may take a few seconds between ARM Cache and the Resource Provider to be fully in sync
                                    CommonSdk.waitForNoANFResource(anfClient, snapshot.id(), SnapshotInner.class);
                                    Utils.writeSuccessMessage("Successfully deleted Snapshot: " + snapshot.id());
                                }
                            }
                        }
                    }
                }
            }
        }

        /*
          Clean up all volumes
          Note: Volume deletion operations at the RP level are executed serially
         */
        Utils.writeConsoleMessage("Cleaning up Volume(s)...");
        for (ModelNetAppAccount account : config.getAccounts())
        {
            if (account.getCapacityPools() != null)
            {
                for (ModelCapacityPool pool : account.getCapacityPools())
                {
                    if (pool.getVolumes() != null)
                    {
                        String[] parameters = {config.getResourceGroup(), account.getName(), pool.getName()};
                        List<?> volumes = CommonSdk.listResourceAsync(anfClient, parameters, VolumeInner.class);
                        if (volumes != null && volumes.size() > 0)
                        {
                            for (Object volumeObject : volumes)
                            {
                                VolumeInner volume = (VolumeInner) volumeObject;
                                try
                                {
                                    Observable<ServiceResponse<Void>> response = anfClient.volumes().deleteWithServiceResponseAsync(config.getResourceGroup(), account.getName(), pool.getName(), ResourceUriUtils.getAnfVolume(volume.id()));
                                    Utils.writeConsoleMessage("Correlation-id of Volume DELETE request: " + response.toBlocking().first().response().headers().get("x-ms-correlation-request-id"));

                                    CommonSdk.waitForNoANFResource(anfClient, volume.id(), VolumeInner.class);
                                    Utils.writeSuccessMessage("Successfully deleted Volume: " + volume.id());
                                }
                                catch (Exception e)
                                {
                                    Utils.writeErrorMessage("An error occurred while deleting Volume: " + volume.id());
                                    Utils.writeConsoleMessage("Error: " + e);
                                    throw e;
                                }
                            }
                        }
                    }
                    else
                    {
                        Utils.writeConsoleMessage("No Volumes defined for Account: " + account.getName() + ", Capacity Pool: " + pool.getName());
                    }
                }
            }
        }

        /*
          Clean up capacity pools
         */
        Utils.writeConsoleMessage("Cleaning up Capacity Pool(s)...");
        for (ModelNetAppAccount account : config.getAccounts())
        {
            if (account.getCapacityPools() != null)
            {
                for (ModelCapacityPool pool : account.getCapacityPools())
                {
                    String[] parameters = {config.getResourceGroup(), account.getName(), pool.getName()};
                    CapacityPoolInner capacityPool = CommonSdk.getResourceAsync(anfClient, parameters, CapacityPoolInner.class);
                    if (capacityPool != null)
                    {
                        try
                        {
                            Observable<ServiceResponse<Void>> response = anfClient.pools().deleteWithServiceResponseAsync(config.getResourceGroup(), account.getName(), ResourceUriUtils.getAnfCapacityPool(capacityPool.id()));
                            Utils.writeConsoleMessage("Correlation-id of Capacity Pool DELETE request: " + response.toBlocking().first().response().headers().get("x-ms-correlation-request-id"));
                        }
                        catch (Exception e)
                        {
                            Utils.writeErrorMessage("An error occurred while deleting Capacity Pool: " + capacityPool.id());
                            Utils.writeConsoleMessage("Error: " + e);
                            throw e;
                        }

                        CommonSdk.waitForNoANFResource(anfClient, capacityPool.id(), CapacityPoolInner.class);
                        Utils.writeSuccessMessage("Successfully deleted Capacity Pool: " + capacityPool.id());
                    }
                }
            }
        }

        /*
          Clean up accounts
         */
        Utils.writeConsoleMessage("Waiting for 1 minute before deleting Accounts to make sure all nested resources have been removed...");
        Utils.threadSleep(60000);
        Utils.writeConsoleMessage("Cleaning up Account(s)...");
        if (config.getAccounts() != null)
        {
            for (ModelNetAppAccount account : config.getAccounts())
            {
                String[] parameters = {config.getResourceGroup(), account.getName()};
                NetAppAccountInner anfAccount = CommonSdk.getResourceAsync(anfClient, parameters, NetAppAccountInner.class);
                if (anfAccount != null)
                {
                    try
                    {
                        Observable<ServiceResponse<Void>> response = anfClient.accounts().deleteWithServiceResponseAsync(config.getResourceGroup(), anfAccount.name());
                        Utils.writeConsoleMessage("Correlation-id of Account DELETE request: " + response.toBlocking().first().response().headers().get("x-ms-correlation-request-id"));
                    }
                    catch (Exception e)
                    {
                        Utils.writeErrorMessage("An error occurred while deleting Account: " + anfAccount.id());
                        Utils.writeConsoleMessage("Error: " + e);
                        throw e;
                    }

                    CommonSdk.waitForNoANFResource(anfClient, anfAccount.id(), NetAppAccountInner.class);
                    Utils.writeSuccessMessage("Successfully deleted Account: " + anfAccount.id());
                }
            }
        }
    }
}
