// Copyright (c) Microsoft and contributors.  All rights reserved.
//
// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package sdk.sample;

import sdk.sample.common.ProjectConfiguration;
import sdk.sample.common.ResourceUriUtils;
import sdk.sample.common.Utils;
import com.microsoft.azure.management.netapp.v2019_11_01.implementation.AzureNetAppFilesManagementClientImpl;
import com.microsoft.azure.management.netapp.v2019_11_01.implementation.SnapshotInner;
import com.microsoft.azure.management.netapp.v2019_11_01.implementation.VolumeInner;

import java.util.UUID;

public class Snapshots
{
    /**
     * Executes Snapshot related operations
     * @param config Project Configuration
     * @param anfClient Azure NetApp Files Management Client
     */
    public static synchronized void runSnapshotOperationsSampleAsync(ProjectConfiguration config, AzureNetAppFilesManagementClientImpl anfClient)
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
            snapshot = anfClient.snapshots().createAsync(
                    config.getResourceGroup(),
                    config.getAccounts().get(0).getName(),
                    config.getAccounts().get(0).getCapacityPools().get(0).getName(),
                    config.getAccounts().get(0).getCapacityPools().get(0).getVolumes().get(0).getName(),
                    snapshotName,
                    snapshotBody
            ).toBlocking().first();

            Utils.writeSuccessMessage("Snapshot created successfully. Snapshot resource id: " + snapshot.id());
        }
        catch (Exception e)
        {
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
            snapshotVolume = anfClient.volumes().getAsync(
                    ResourceUriUtils.getResourceGroup(snapshot.id()),
                    ResourceUriUtils.getAnfAccount(snapshot.id()),
                    ResourceUriUtils.getAnfCapacityPool(snapshot.id()),
                    ResourceUriUtils.getAnfVolume(snapshot.id())
            ).toBlocking().first();
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
            VolumeInner volumeFromSnapshotBody = new VolumeInner();
            volumeFromSnapshotBody.withSnapshotId(snapshot.snapshotId());
            volumeFromSnapshotBody.withExportPolicy(snapshotVolume.exportPolicy());
            volumeFromSnapshotBody.withLocation(snapshotVolume.location());
            volumeFromSnapshotBody.withProtocolTypes(snapshotVolume.protocolTypes());
            volumeFromSnapshotBody.withServiceLevel(snapshotVolume.serviceLevel());
            volumeFromSnapshotBody.withUsageThreshold(snapshotVolume.usageThreshold());
            volumeFromSnapshotBody.withSubnetId(snapshotVolume.subnetId());
            volumeFromSnapshotBody.withCreationToken(newVolumeName);

            newVolumeFromSnapshot = anfClient.volumes().createOrUpdateAsync(
                    config.getResourceGroup(),
                    config.getAccounts().get(0).getName(),
                    config.getAccounts().get(0).getCapacityPools().get(0).getName(),
                    newVolumeName,
                    volumeFromSnapshotBody
            ).toBlocking().first();

            Utils.writeSuccessMessage("Volume successfully created from snapshot. Volume resource id: " + newVolumeFromSnapshot.id());
        }
        catch (Exception e)
        {
            Utils.writeErrorMessage("An error occurred while creating a volume " + newVolumeName + " from snapshot " + snapshot.id() + ".\nError message: " + e.getMessage());
            throw e;
        }
    }
}
