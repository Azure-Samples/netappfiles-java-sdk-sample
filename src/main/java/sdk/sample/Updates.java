// Copyright (c) Microsoft and contributors.  All rights reserved.
//
// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package sdk.sample;

import com.azure.resourcemanager.netapp.fluent.NetAppManagementClient;
import com.azure.resourcemanager.netapp.fluent.models.CapacityPoolInner;
import com.azure.resourcemanager.netapp.fluent.models.VolumeInner;
import com.azure.resourcemanager.netapp.models.CapacityPoolPatch;
import com.azure.resourcemanager.netapp.models.ExportPolicyRule;
import com.azure.resourcemanager.netapp.models.VolumePatch;
import com.azure.resourcemanager.netapp.models.VolumePatchPropertiesExportPolicy;
import sdk.sample.common.ProjectConfiguration;
import sdk.sample.common.Utils;
import sdk.sample.model.ModelCapacityPool;
import sdk.sample.model.ModelNetAppAccount;
import sdk.sample.model.ModelVolume;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

public class Updates
{
    /**
     * Executes some updates on first capacity pool and first volume listed in the configuration file (appsettings.json)
     * @param config Project Configuration
     * @param anfClient Azure NetApp Files Management Client
     */
    public static void runUpdateOperationsSample(ProjectConfiguration config, NetAppManagementClient anfClient)
    {
        /*
          Capacity Pool Updates
         */
        Utils.writeConsoleMessage("Performing size update on a Capacity Pool");

        // Get current Capacity Pool information
        CapacityPoolInner capacityPool;
        try
        {
            ModelNetAppAccount account = config.getAccounts().stream().findFirst().orElseThrow();
            ModelCapacityPool pool = account.getCapacityPools().stream().findFirst().orElseThrow();
            capacityPool = anfClient.getPools().get(
                    config.getResourceGroup(),
                    account.getName(),
                    pool.getName());
        }
        catch (Exception e)
        {
            if (e instanceof NoSuchElementException)
                Utils.writeErrorMessage("An error occurred while creating a snapshot, element missing in config file");
            else
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
            ModelNetAppAccount account = config.getAccounts().stream().findFirst().orElseThrow();
            ModelCapacityPool pool = account.getCapacityPools().stream().findFirst().orElseThrow();
            CapacityPoolInner updatedCapacityPool = anfClient.getPools().beginUpdate(
                    config.getResourceGroup(),
                    account.getName(),
                    pool.getName(),
                    capacityPoolPatch).getFinalResult();

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
            ModelNetAppAccount account = config.getAccounts().stream().findFirst().orElseThrow();
            ModelCapacityPool pool = account.getCapacityPools().stream().findFirst().orElseThrow();
            ModelVolume modelVolume = pool.getVolumes().stream().findFirst().orElseThrow();
            volume = anfClient.getVolumes().get(
                    config.getResourceGroup(),
                    account.getName(),
                    pool.getName(),
                    modelVolume.getName());
        }
        catch (Exception e)
        {
            if (e instanceof NoSuchElementException)
                Utils.writeErrorMessage("An error occurred while creating a snapshot, element missing in config file");
            else
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
            ModelNetAppAccount account = config.getAccounts().stream().findFirst().orElseThrow();
            ModelCapacityPool pool = account.getCapacityPools().stream().findFirst().orElseThrow();
            ModelVolume modelVolume = pool.getVolumes().stream().findFirst().orElseThrow();
            VolumeInner updatedVolume = anfClient.getVolumes().beginUpdate(
                    config.getResourceGroup(),
                    account.getName(),
                    pool.getName(),
                    modelVolume.getName(),
                    volumePatch).getFinalResult();

            Utils.writeSuccessMessage("Volume successfully updated, new size: " + Utils.getTBFromBytes(updatedVolume.usageThreshold()) +
                    "TB, export policy rule count: " + updatedVolume.exportPolicy().rules().size() + ", resource id: " + updatedVolume.id());
        }
        catch (Exception e)
        {
            Utils.writeErrorMessage("An error occurred while updating Volume " + volume.id() + "\nError message: " + e.getMessage());
            throw e;
        }
    }
}
