// Copyright (c) Microsoft and contributors.  All rights reserved.
//
// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package sdk.sample.common;

import com.azure.resourcemanager.netapp.fluent.NetAppManagementClient;
import com.azure.resourcemanager.netapp.fluent.models.CapacityPoolInner;
import com.azure.resourcemanager.netapp.fluent.models.NetAppAccountInner;
import com.azure.resourcemanager.netapp.fluent.models.SnapshotInner;
import com.azure.resourcemanager.netapp.fluent.models.VolumeInner;
import com.azure.resourcemanager.netapp.models.ExportPolicyRule;
import com.azure.resourcemanager.netapp.models.ServiceLevel;
import com.azure.resourcemanager.netapp.models.VolumePropertiesExportPolicy;
import sdk.sample.model.ModelCapacityPool;
import sdk.sample.model.ModelExportPolicyRule;
import sdk.sample.model.ModelNetAppAccount;
import sdk.sample.model.ModelVolume;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

// Contains public methods for SDK related operations
public class CommonSdk
{
    /**
     * Creates or updates a volume. In this process, notice that we need to create two mandatory objects, one as the
     * export rule list and the volume body itself before we request the volume creation.
     * @param anfClient Azure NetApp Files Management Client
     * @param resourceGroup Resource Group name where volume will be created
     * @param account ModelNetAppAccount object that describes the ANF Account, populated with data from appsettings.json
     * @param pool ModelCapacityPool object that describes the Capacity Pool, populated with data from appsettings.json
     * @param volume ModelVolume object that describes the Volume to be created, populated with data from appsettings.json
     * @return Observable of the newly created Volume
     */
    public static VolumeInner createOrUpdateVolume(NetAppManagementClient anfClient, String resourceGroup, ModelNetAppAccount account, ModelCapacityPool pool, ModelVolume volume)
    {
        List<ExportPolicyRule> ruleList = new ArrayList<>();
        for (ModelExportPolicyRule rule : volume.getExportPolicies())
        {
            ruleList.add(new ExportPolicyRule()
                    .withAllowedClients(rule.getAllowedClients())
                    .withRuleIndex(rule.getRuleIndex())
                    .withUnixReadWrite(rule.isUnixReadWrite())
                    .withUnixReadOnly(rule.isUnixReadOnly())
                    .withCifs(rule.isCifs())
                    .withNfsv3(rule.isNfsv3())
                    .withNfsv41(rule.isNfsv4()));
        }

        VolumePropertiesExportPolicy exportPolicy = new VolumePropertiesExportPolicy().withRules(ruleList);
        List<String> protocol = new ArrayList<>();
        if (!ruleList.isEmpty())
        {
            protocol.add(volume.getExportPolicies().get(0).isNfsv3() ? "NFSv3" : "NFSv4");
        }

        VolumeInner volumeInner = new VolumeInner();
        volumeInner.withCreationToken(volume.getCreationToken());
        volumeInner.withExportPolicy(exportPolicy);
        volumeInner.withServiceLevel(ServiceLevel.fromString(pool.getServiceLevel()));
        volumeInner.withSubnetId(volume.getSubnetId());
        volumeInner.withUsageThreshold(volume.getUsageThreshold());
        volumeInner.withProtocolTypes(protocol);
        volumeInner.withLocation(account.getLocation().toLowerCase());

        return anfClient.getVolumes().beginCreateOrUpdate(resourceGroup, account.getName(), pool.getName(), volume.getName(), volumeInner).getFinalResult();
    }

    /**
     * Creates or updates an Azure NetApp Files Account
     * @param anfClient Azure NetApp Files Management Client
     * @param resourceGroup Resource Group name where the ANF Account will be created
     * @param account ModelNetAppAccount object that describes the ANF Account to be created, populated with data from appsettings.json
     * @return Observable of the newly created Account
     */
    public static NetAppAccountInner createOrUpdateAccount(NetAppManagementClient anfClient, String resourceGroup, ModelNetAppAccount account)
    {
        NetAppAccountInner netAppAccount = new NetAppAccountInner();
        netAppAccount.withLocation(account.getLocation());

        return anfClient.getAccounts().beginCreateOrUpdate(resourceGroup, account.getName(), netAppAccount).getFinalResult();
    }

    /**
     * Creates or updates a Capacity Pool
     * @param anfClient Azure NetApp Files Management Client
     * @param resourceGroup Resource Group name where the Capacity Pool will be created
     * @param accountName Name of the ANF Account this Capacity Pool will be associated with
     * @param pool ModelCapacityPool object that describes the Capacity Pool to be created, populated with data from appsettings.json
     * @return Observable of the newly created Capacity Pool
     */
    public static CapacityPoolInner createOrUpdateCapacityPool(NetAppManagementClient anfClient, String resourceGroup, String accountName, String location, ModelCapacityPool pool)
    {
        CapacityPoolInner capacityPool = new CapacityPoolInner();
        capacityPool.withServiceLevel(ServiceLevel.fromString(pool.getServiceLevel()));
        capacityPool.withSize(pool.getSize());
        capacityPool.withLocation(location);

        return anfClient.getPools().beginCreateOrUpdate(resourceGroup, accountName, pool.getName(), capacityPool).getFinalResult();
    }

    /**
     * Returns an ANF resource or null if it does not exist
     * @param anfClient Azure NetApp Files Management Client
     * @param parameters List of parameters required depending on the resource type:
     *                   Account        -> ResourceGroupName, AccountName
     *                   Capacity Pool  -> ResourceGroupName, AccountName, PoolName
     *                   Volume         -> ResourceGroupName, AccountName, PoolName, VolumeName
     *                   Snapshot       -> ResourceGroupName, AccountName, PoolName, VolumeName, SnapshotName
     * @param clazz Valid class types: NetAppAccountInner, CapacityPoolInner, VolumeInner, SnapshotInner
     * @return Observable of valid resource T
     */
    public static <T> Object getResource(NetAppManagementClient anfClient, String[] parameters, Class<T> clazz)
    {
        try
        {
            switch (clazz.getSimpleName())
            {
                case "NetAppAccountInner":
                    return anfClient.getAccounts().getByResourceGroup(
                            parameters[0],
                            parameters[1]);
                case "CapacityPoolInner":
                    return anfClient.getPools().get(
                            parameters[0],
                            parameters[1],
                            parameters[2]);
                case "VolumeInner":
                    return anfClient.getVolumes().get(
                            parameters[0],
                            parameters[1],
                            parameters[2],
                            parameters[3]);
                case "SnapshotInner":
                    return anfClient.getSnapshots().get(
                            parameters[0],
                            parameters[1],
                            parameters[2],
                            parameters[3],
                            parameters[4]);
            }
        }
        catch (Exception e)
        {
            Utils.writeWarningMessage("Error finding resource - " + e.getMessage());
        }
        return null;
    }

    /**
     * Returns a list of ANF resources or null if resources do not exist
     * @param anfClient Azure NetApp Files Management Client
     * @param parameters List of parameters required depending on the resource type:
     *                   Account        -> ResourceGroupName, AccountName
     *                   Capacity Pool  -> ResourceGroupName, AccountName, PoolName
     *                   Volume         -> ResourceGroupName, AccountName, PoolName, VolumeName
     *                   Snapshot       -> ResourceGroupName, AccountName, PoolName, VolumeName, SnapshotName
     * @param clazz Valid class types: NetAppAccountInner, CapacityPoolInner, VolumeInner, SnapshotInner
     * @return Observable of valid resources
     */
    public static <T> List<Object> listResource(NetAppManagementClient anfClient, String[] parameters, Class<T> clazz)
    {
        try
        {
            switch (clazz.getSimpleName())
            {
                case "NetAppAccountInner":
                    return anfClient.getAccounts().listByResourceGroup(
                            parameters[0]).stream().collect(Collectors.toList());
                case "CapacityPoolInner":
                    return anfClient.getPools().list(
                            parameters[0],
                            parameters[1]).stream().collect(Collectors.toList());
                case "VolumeInner":
                    return anfClient.getVolumes().list(
                            parameters[0],
                            parameters[1],
                            parameters[2]).stream().collect(Collectors.toList());
                case "SnapshotInner":
                    return anfClient.getSnapshots().list(
                            parameters[0],
                            parameters[1],
                            parameters[2],
                            parameters[3]).stream().collect(Collectors.toList());
            }
        }
        catch (Exception e)
        {
            Utils.writeErrorMessage("Error listing resource - " + e.getMessage());
            throw e;
        }
        return Collections.emptyList();
    }

    /**
     * Method to overload function waitForNoANFResource(client, string, int, int, clazz) with default values
     * @param anfClient Azure NetApp Files Management Client
     * @param resourceId Resource id of the resource that was deleted
     * @param clazz Valid class types: NetAppAccountInner, CapacityPoolInner, VolumeInner, SnapshotInner
     */
    public static <T> void waitForNoANFResource(NetAppManagementClient anfClient, String resourceId, Class<T> clazz)
    {
        waitForNoANFResource(anfClient, resourceId, 10, 60, clazz);
    }

    /**
     * This function checks if a specific ANF resource that was recently deleted stops existing. It breaks the wait
     * if the resource is not found anymore or if polling reached its maximum retries.
     * @param anfClient Azure NetApp Files Management Client
     * @param resourceId Resource id of the resource that was deleted
     * @param intervalInSec Time in second that the function will poll to see if the resource has been deleted
     * @param retries Number of times polling will be performed
     * @param clazz Valid class types: NetAppAccountInner, CapacityPoolInner, VolumeInner, SnapshotInner
     */
    public static <T> void waitForNoANFResource(NetAppManagementClient anfClient, String resourceId, int intervalInSec, int retries, Class<T> clazz)
    {
        for (int i = 0; i < retries; i++)
        {
            Utils.threadSleep(intervalInSec*1000);

            try
            {
                switch (clazz.getSimpleName())
                {
                    case "NetAppAccountInner":
                        NetAppAccountInner account = anfClient.getAccounts().getByResourceGroup(ResourceUriUtils.getResourceGroup(resourceId),
                                ResourceUriUtils.getAnfAccount(resourceId));
                        if (account == null)
                            return;

                        continue;

                    case "CapacityPoolInner":
                        CapacityPoolInner pool = anfClient.getPools().get(ResourceUriUtils.getResourceGroup(resourceId),
                                ResourceUriUtils.getAnfAccount(resourceId),
                                ResourceUriUtils.getAnfCapacityPool(resourceId));
                        if (pool == null)
                            return;

                        continue;

                    case "VolumeInner":
                        VolumeInner volume = anfClient.getVolumes().get(ResourceUriUtils.getResourceGroup(resourceId),
                                ResourceUriUtils.getAnfAccount(resourceId),
                                ResourceUriUtils.getAnfCapacityPool(resourceId),
                                ResourceUriUtils.getAnfVolume(resourceId));
                        if (volume == null)
                            return;

                        continue;

                    case "SnapshotInner":
                        SnapshotInner snapshot = anfClient.getSnapshots().get(ResourceUriUtils.getResourceGroup(resourceId),
                                ResourceUriUtils.getAnfAccount(resourceId),
                                ResourceUriUtils.getAnfCapacityPool(resourceId),
                                ResourceUriUtils.getAnfVolume(resourceId),
                                ResourceUriUtils.getAnfSnapshot(resourceId));
                        if (snapshot == null)
                            return;
                }
            }
            catch (Exception e)
            {
                Utils.writeWarningMessage(e.getMessage());
                break;
            }
        }
    }
}
