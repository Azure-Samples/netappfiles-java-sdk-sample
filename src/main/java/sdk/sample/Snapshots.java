// Copyright (c) Microsoft and contributors.  All rights reserved.
//
// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package sdk.sample;

import com.azure.resourcemanager.netapp.fluent.NetAppManagementClient;
import com.azure.resourcemanager.netapp.fluent.models.SnapshotInner;
import com.azure.resourcemanager.netapp.fluent.models.VolumeInner;
import sdk.sample.common.ProjectConfiguration;
import sdk.sample.common.ResourceUriUtils;
import sdk.sample.common.Utils;
import sdk.sample.model.ModelCapacityPool;
import sdk.sample.model.ModelNetAppAccount;
import sdk.sample.model.ModelVolume;

import java.util.NoSuchElementException;
import java.util.UUID;

public class Snapshots
{
    /**
     * Executes Snapshot related operations
     * @param config Project Configuration
     * @param anfClient Azure NetApp Files Management Client
     */
    public static void runSnapshotOperationsSample(ProjectConfiguration config, NetAppManagementClient anfClient)
    {
        /*
          Creating snapshot from first volume of the first capacity pool
         */
        Utils.writeConsoleMessage("Performing snapshot operations");
        Utils.writeConsoleMessage("Creating snapshot...");

        String snapshotName = "Snapshot-" + UUID.randomUUID();

        SnapshotInner snapshotBody = new SnapshotInner();
        snapshotBody.withLocation(config.getAccounts().get(0).getLocation());

        SnapshotInner snapshot;
        try
        {
            ModelNetAppAccount account = config.getAccounts().stream().findFirst().orElseThrow();
            ModelCapacityPool pool = account.getCapacityPools().stream().findFirst().orElseThrow();
            ModelVolume volume = pool.getVolumes().stream().findFirst().orElseThrow();
            snapshot = anfClient.getSnapshots().beginCreate(
                    config.getResourceGroup(),
                    account.getName(),
                    pool.getName(),
                    volume.getName(),
                    snapshotName,
                    snapshotBody).getFinalResult();

            Utils.writeSuccessMessage("Snapshot created successfully. Snapshot resource id: " + snapshot.id());
        }
        catch (Exception e)
        {
            if (e instanceof NoSuchElementException)
                Utils.writeErrorMessage("An error occurred while creating a snapshot, element missing in config file");
            else
                Utils.writeErrorMessage("An error occurred while creating a snapshot of volume " +
                        config.getAccounts().get(0).getCapacityPools().get(0).getVolumes().get(0).getName() + ".\nError message: " + e.getMessage());
            throw e;
        }

        /*
          Creating a volume from snapshot
         */
        Utils.writeConsoleMessage("Creating new volume from snapshot...");
        String newVolumeName = "Vol-" + ResourceUriUtils.getAnfSnapshot(snapshot.id());

        VolumeInner snapshotVolume;
        try
        {
            snapshotVolume = anfClient.getVolumes().get(
                    ResourceUriUtils.getResourceGroup(snapshot.id()),
                    ResourceUriUtils.getAnfAccount(snapshot.id()),
                    ResourceUriUtils.getAnfCapacityPool(snapshot.id()),
                    ResourceUriUtils.getAnfVolume(snapshot.id()));
        }
        catch (Exception e)
        {
            Utils.writeErrorMessage("An error occurred trying to obtain information about volume " +
                    ResourceUriUtils.getAnfVolume(snapshot.id()) + " from snapshot " + snapshot.id() + "\nError message: " + e.getMessage());
            throw e;
        }

        VolumeInner newVolumeFromSnapshot;
        try
        {
            /*
              Notice that SnapshotId is not the actual resource Id of the snapshot, this value is the unique identifier
              (guid) of the snapshot, represented by the SnapshotId instead.
             */
            VolumeInner volumeFromSnapshotBody = new VolumeInner()
                    .withSnapshotId(snapshot.snapshotId())
                    //.withExportPolicy(snapshotVolume.exportPolicy())
                    .withLocation(snapshotVolume.location())
                    .withProtocolTypes(snapshotVolume.protocolTypes())
                    .withServiceLevel(snapshotVolume.serviceLevel())
                    .withUsageThreshold(snapshotVolume.usageThreshold())
                    .withSubnetId(snapshotVolume.subnetId())
                    .withCreationToken(newVolumeName);

            ModelNetAppAccount account = config.getAccounts().stream().findFirst().orElseThrow();
            ModelCapacityPool pool = account.getCapacityPools().stream().findFirst().orElseThrow();
            newVolumeFromSnapshot = anfClient.getVolumes().beginCreateOrUpdate(
                    config.getResourceGroup(),
                    account.getName(),
                    pool.getName(),
                    newVolumeName,
                    volumeFromSnapshotBody).getFinalResult();

            Utils.writeSuccessMessage("Volume successfully created from snapshot. Volume resource id: " + newVolumeFromSnapshot.id());
        }
        catch (Exception e)
        {
            Utils.writeErrorMessage("An error occurred while creating a volume " + newVolumeName + " from snapshot " + snapshot.id() + ".\nError message: " + e.getMessage());
            throw e;
        }
    }
}
