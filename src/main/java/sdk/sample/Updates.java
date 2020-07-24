// Copyright (c) Microsoft and contributors.  All rights reserved.
//
// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package sdk.sample;

import sdk.sample.common.ProjectConfiguration;
import sdk.sample.common.Utils;
import com.microsoft.azure.management.netapp.v2019_11_01.CapacityPoolPatch;
import com.microsoft.azure.management.netapp.v2019_11_01.ExportPolicyRule;
import com.microsoft.azure.management.netapp.v2019_11_01.VolumePatch;
import com.microsoft.azure.management.netapp.v2019_11_01.VolumePatchPropertiesExportPolicy;
import com.microsoft.azure.management.netapp.v2019_11_01.implementation.AzureNetAppFilesManagementClientImpl;
import com.microsoft.azure.management.netapp.v2019_11_01.implementation.CapacityPoolInner;
import com.microsoft.azure.management.netapp.v2019_11_01.implementation.VolumeInner;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Updates
{
    /**
     * Executes some updates on first capacity pool and first volume listed in the configuration file (appsettings.json)
     * @param config Project Configuration
     * @param anfClient Azure NetApp Files Management Client
     */
    public static CompletableFuture<Void> runUpdateOperationsSampleAsync(ProjectConfiguration config, AzureNetAppFilesManagementClientImpl anfClient)
    {
        /*
          Capacity Pool Updates
         */
        Utils.writeConsoleMessage("Performing size update on a Capacity Pool");

        // Get current Capacity Pool information
        CapacityPoolInner capacityPool;
        try
        {
            capacityPool = anfClient.pools().getAsync(
                    config.getResourceGroup(),
                    config.getAccounts().get(0).getName(),
                    config.getAccounts().get(0).getCapacityPools().get(0).getName()
            ).toBlocking().single();
        }
        catch (Exception e)
        {
            Utils.writeErrorMessage("An error occurred while getting current Capacity Pool information " +
                    config.getAccounts().get(0).getCapacityPools().get(0).getName() + "\nError message: " + e.getMessage());
            throw e;
        }

        int newCapacityPoolSizeTB = 10;
        Utils.writeConsoleMessage("Changing Capacity Pools size from " + Utils.getTBFromBytes(capacityPool.size()) + "TB to " + newCapacityPoolSizeTB + "TB");

        // New size in bytes
        long newCapacityPoolSizeBytes = Utils.getBytesFromTB(newCapacityPoolSizeTB);

        // Create capacity pool patch object passing required arguments and the updated size
        CapacityPoolPatch capacityPoolPatch = new CapacityPoolPatch();
        capacityPoolPatch.withLocation(capacityPool.location());
        capacityPoolPatch.withSize(newCapacityPoolSizeBytes);

        // Update Capacity Pool resource
        try
        {
            CapacityPoolInner updatedCapacityPool = anfClient.pools().updateAsync(
                    config.getResourceGroup(),
                    config.getAccounts().get(0).getName(),
                    config.getAccounts().get(0).getCapacityPools().get(0).getName(),
                    capacityPoolPatch
            ).toBlocking().single();

            Utils.writeSuccessMessage("Capacity Pool successfully updated, new size: " + Utils.getTBFromBytes(updatedCapacityPool.size()) + "TB, resource id: " + updatedCapacityPool.id());
        }
        catch (Exception e)
        {
            Utils.writeErrorMessage("An error occurred while updating Capacity Pool " + capacityPool.id() + "\nError message: " + e.getMessage());
            throw e;
        }

        /*
          Volume Updates
         */
        Utils.writeConsoleMessage("Performing size and export policy update on a Volume");

        // Get current Volume information
        VolumeInner volume;
        try
        {
            volume = anfClient.volumes().getAsync(
                    config.getResourceGroup(),
                    config.getAccounts().get(0).getName(),
                    config.getAccounts().get(0).getCapacityPools().get(0).getName(),
                    config.getAccounts().get(0).getCapacityPools().get(0).getVolumes().get(0).getName()
            ).toBlocking().single();
        }
        catch (Exception e)
        {
            Utils.writeErrorMessage("An error occurred while getting current Volume information " +
                    config.getAccounts().get(0).getCapacityPools().get(0).getVolumes().get(0).getName() + "\nError message: " + e.getMessage());
            throw e;
        }

        int newVolumeSizeTB = 1;
        Utils.writeConsoleMessage("Changing Volume size from " + Utils.getTBFromBytes(volume.usageThreshold()) +
                "TB to " + newVolumeSizeTB + "TB. Also adding new export policy rule, current count is " + volume.exportPolicy().rules().size());

        // New size in bytes
        long newVolumeSizeBytes = Utils.getBytesFromTB(newVolumeSizeTB);

        // New Export Policy rule
        List<ExportPolicyRule> ruleList = volume.exportPolicy().rules();

        // Sort the list so our new Export Policy will have highest rule index
        Comparator<ExportPolicyRule> compareByRuleIndex = Comparator.comparing(ExportPolicyRule::ruleIndex);
        ruleList.sort(compareByRuleIndex);

        // Currently, ANF's volume export policy supports up to 5 rules
        VolumePatchPropertiesExportPolicy exportPoliciesPatch = null;
        if (ruleList.size() <= 4)
        {
            ruleList.add(new ExportPolicyRule()
                .withAllowedClients("10.0.0.4/32")
                .withCifs(false)
                .withNfsv3(true)
                .withNfsv41(false)
                .withUnixReadOnly(false)
                .withUnixReadWrite(true)
                .withRuleIndex(ruleList.get(ruleList.size() - 1).ruleIndex() + 1));

            exportPoliciesPatch = new VolumePatchPropertiesExportPolicy();
            exportPoliciesPatch.withRules(ruleList);
        }

        // Create volume patch object passing required arguments and the updated size
        VolumePatch volumePatch = new VolumePatch();
        if (exportPoliciesPatch != null)
        {
            volumePatch.withLocation(volume.location());
            volumePatch.withUsageThreshold(newVolumeSizeBytes);
            volumePatch.withExportPolicy(exportPoliciesPatch);
        }
        else
        {
            volume.withLocation(volume.location());
            volume.withUsageThreshold(newVolumeSizeBytes);
        }

        // Update size at volume resource
        try
        {
            VolumeInner updatedVolume = anfClient.volumes().updateAsync(
                    config.getResourceGroup(),
                    config.getAccounts().get(0).getName(),
                    config.getAccounts().get(0).getCapacityPools().get(0).getName(),
                    config.getAccounts().get(0).getCapacityPools().get(0).getVolumes().get(0).getName(),
                    volumePatch
            ).toBlocking().single();

            Utils.writeSuccessMessage("Volume successfully updated, new size: " + Utils.getTBFromBytes(updatedVolume.usageThreshold()) +
                    "TB, export policy rule count: " + updatedVolume.exportPolicy().rules().size() + ", resource id: " + updatedVolume.id());
        }
        catch (Exception e)
        {
            Utils.writeErrorMessage("An error occurred while updating Volume " + volume.id() + "\nError message: " + e.getMessage());
            throw e;
        }

        return CompletableFuture.completedFuture(null);
    }
}
