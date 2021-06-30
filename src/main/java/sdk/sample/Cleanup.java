// Copyright (c) Microsoft and contributors.  All rights reserved.
//
// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package sdk.sample;

import com.azure.resourcemanager.netapp.fluent.NetAppManagementClient;
import com.azure.resourcemanager.netapp.fluent.models.CapacityPoolInner;
import com.azure.resourcemanager.netapp.fluent.models.NetAppAccountInner;
import com.azure.resourcemanager.netapp.fluent.models.SnapshotInner;
import com.azure.resourcemanager.netapp.fluent.models.VolumeInner;
import sdk.sample.common.CommonSdk;
import sdk.sample.common.ProjectConfiguration;
import sdk.sample.common.ResourceUriUtils;
import sdk.sample.common.Utils;
import sdk.sample.model.ModelCapacityPool;
import sdk.sample.model.ModelNetAppAccount;
import sdk.sample.model.ModelVolume;

import java.util.List;

public class Cleanup
{
    /**
     * Deletes all created resources
     * @param config Project Configuration
     * @param anfClient Azure NetApp Files Management Client
     */
    public static void runCleanupTasksSample(ProjectConfiguration config, NetAppManagementClient anfClient)
    {
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

                            List<Object> snapshotList = CommonSdk.listResource(anfClient, parameters, SnapshotInner.class);
                            snapshotList.forEach(o -> {
                                /*
                                  Snapshot name property (and other ANF's related nested resources) return a relative path up to the name
                                  and to use this property in delete for example, the argument needs to be sanitized and just the
                                  actual name needs to be used.
                                  Snapshot name property example: "johndoe-anf01/pool01/johndoe-anf01-pool01-vol01/test-a"
                                  "test-a" is the actual name that needs to be used. Below is a sample function that parses the name
                                  from snapshot resource id
                                */
                                SnapshotInner snapshot = (SnapshotInner) o;
                                try
                                {
                                    anfClient.getSnapshots().beginDelete(
                                            config.getResourceGroup(),
                                            account.getName(),
                                            pool.getName(),
                                            volume.getName(),
                                            ResourceUriUtils.getAnfSnapshot(snapshot.id())).getFinalResult();
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
                            });
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

                        List<Object> volumeList = CommonSdk.listResource(anfClient, parameters, VolumeInner.class);
                        volumeList.forEach(o -> {
                            VolumeInner volume = (VolumeInner) o;
                            try
                            {
                                anfClient.getVolumes().beginDelete(config.getResourceGroup(), account.getName(), pool.getName(), ResourceUriUtils.getAnfVolume(volume.id())).getFinalResult();

                                CommonSdk.waitForNoANFResource(anfClient, volume.id(), VolumeInner.class);
                                Utils.writeSuccessMessage("Successfully deleted Volume: " + volume.id());
                            }
                            catch (Exception e)
                            {
                                Utils.writeErrorMessage("An error occurred while deleting Volume: " + volume.id());
                                Utils.writeConsoleMessage("Error: " + e);
                                throw e;
                            }
                        });
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

                    CapacityPoolInner capacityPool = (CapacityPoolInner) CommonSdk.getResource(anfClient, parameters, CapacityPoolInner.class);
                    if (capacityPool != null)
                    {
                        try
                        {
                            anfClient.getPools().beginDelete(config.getResourceGroup(), account.getName(), ResourceUriUtils.getAnfCapacityPool(capacityPool.id())).getFinalResult();
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
        Utils.writeConsoleMessage("Cleaning up Account(s)...");
        if (config.getAccounts() != null)
        {
            for (ModelNetAppAccount account : config.getAccounts())
            {
                String[] parameters = {config.getResourceGroup(), account.getName()};

                NetAppAccountInner anfAccount = (NetAppAccountInner) CommonSdk.getResource(anfClient, parameters, NetAppAccountInner.class);
                if (anfAccount != null)
                {
                    try
                    {
                        anfClient.getAccounts().beginDelete(config.getResourceGroup(), anfAccount.name()).getFinalResult();
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
