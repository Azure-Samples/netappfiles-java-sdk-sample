// Copyright (c) Microsoft and contributors.  All rights reserved.
//
// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package sdk.sample.common;

import sdk.sample.model.ModelCapacityPool;
import sdk.sample.model.ModelExportPolicyRule;
import sdk.sample.model.ModelNetAppAccount;
import sdk.sample.model.ModelVolume;
import com.microsoft.azure.Page;
import com.microsoft.azure.management.netapp.v2019_11_01.ExportPolicyRule;
import com.microsoft.azure.management.netapp.v2019_11_01.ServiceLevel;
import com.microsoft.azure.management.netapp.v2019_11_01.VolumePropertiesExportPolicy;
import com.microsoft.azure.management.netapp.v2019_11_01.implementation.AzureNetAppFilesManagementClientImpl;
import com.microsoft.azure.management.netapp.v2019_11_01.implementation.CapacityPoolInner;
import com.microsoft.azure.management.netapp.v2019_11_01.implementation.NetAppAccountInner;
import com.microsoft.azure.management.netapp.v2019_11_01.implementation.SnapshotInner;
import com.microsoft.azure.management.netapp.v2019_11_01.implementation.VolumeInner;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;

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
     * @return The newly created Volume
     */
    public static synchronized VolumeInner createOrUpdateVolumeAsync(AzureNetAppFilesManagementClientImpl anfClient, String resourceGroup, ModelNetAppAccount account, ModelCapacityPool pool, ModelVolume volume)
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

        Observable<VolumeInner> volumeObservable = anfClient.volumes().createOrUpdateAsync(resourceGroup, account.getName(), pool.getName(), volume.getName(), volumeInner);
        return volumeObservable.toBlocking().first();
    }

    /**
     * Creates or updates an Azure NetApp Files Account
     * @param anfClient Azure NetApp Files Management Client
     * @param resourceGroup Resource Group name where the ANF Account will be created
     * @param account ModelNetAppAccount object that describes the ANF Account to be created, populated with data from appsettings.json
     * @return The newly created Account
     */
    public static synchronized NetAppAccountInner createOrUpdateAccountAsync(AzureNetAppFilesManagementClientImpl anfClient, String resourceGroup, ModelNetAppAccount account)
    {
        NetAppAccountInner netAppAccount = new NetAppAccountInner();
        netAppAccount.withLocation(account.getLocation());

        Observable<NetAppAccountInner> netAppAccountObservable = anfClient.accounts().createOrUpdateAsync(resourceGroup, account.getName(), netAppAccount);
        return netAppAccountObservable.toBlocking().first();
    }

    /**
     * Creates or updates a Capacity Pool
     * @param anfClient Azure NetApp Files Management Client
     * @param resourceGroup Resource Group name where the Capacity Pool will be created
     * @param accountName Name of the ANF Account this Capacity Pool will be associated with
     * @param pool ModelCapacityPool object that describes the Capacity Pool to be created, populated with data from appsettings.json
     * @return The newly created Capacity Pool
     */
    public static synchronized CapacityPoolInner createOrUpdateCapacityPoolAsync(AzureNetAppFilesManagementClientImpl anfClient, String resourceGroup, String accountName, String location, ModelCapacityPool pool)
    {
        CapacityPoolInner capacityPool = new CapacityPoolInner();
        capacityPool.withServiceLevel(ServiceLevel.fromString(pool.getServiceLevel()));
        capacityPool.withSize(pool.getSize());
        capacityPool.withLocation(location);

        Observable<CapacityPoolInner> capacityPoolObservable = anfClient.pools().createOrUpdateAsync(resourceGroup, accountName, pool.getName(), capacityPool);
        return capacityPoolObservable.toBlocking().first();
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
     * @return Valid resource T
     */
    public static synchronized <T> T getResourceAsync(AzureNetAppFilesManagementClientImpl anfClient, String[] parameters, Class<T> clazz)
    {
        try
        {
            switch (clazz.getSimpleName())
            {
                case "NetAppAccountInner":
                    Observable<NetAppAccountInner> account = anfClient.accounts().getByResourceGroupAsync(
                            parameters[0],
                            parameters[1]);
                    return clazz.cast(account.toBlocking().first());

                case "CapacityPoolInner":
                    Observable<CapacityPoolInner> capacityPool = anfClient.pools().getAsync(
                            parameters[0],
                            parameters[1],
                            parameters[2]);
                    return clazz.cast(capacityPool.toBlocking().first());

                case "VolumeInner":
                    Observable<VolumeInner> volume = anfClient.volumes().getAsync(
                            parameters[0],
                            parameters[1],
                            parameters[2],
                            parameters[3]);
                    return clazz.cast(volume.toBlocking().first());

                case "SnapshotInner":
                    Observable<SnapshotInner> snapshot = anfClient.snapshots().getAsync(
                            parameters[0],
                            parameters[1],
                            parameters[2],
                            parameters[3],
                            parameters[4]);
                    return clazz.cast(snapshot.toBlocking().first());
            }
        }
        catch (Exception e)
        {
            Utils.writeErrorMessage("Error finding resource - " + e.getMessage());
            throw e;
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
     * @return List of valid resources
     */
    public static synchronized <T> List<?> listResourceAsync(AzureNetAppFilesManagementClientImpl anfClient, String[] parameters, Class<T> clazz)
    {
        try
        {
            switch (clazz.getSimpleName())
            {
                case "NetAppAccountInner":
                    Observable<Page<NetAppAccountInner>> accounts = anfClient.accounts().listByResourceGroupAsync(
                            parameters[0]);
                    return accounts.toBlocking().first().items();

                case "CapacityPoolInner":
                    Observable<List<CapacityPoolInner>> capacityPools = anfClient.pools().listAsync(
                            parameters[0],
                            parameters[1]);
                    return capacityPools.toBlocking().first();

                case "VolumeInner":
                    Observable<List<VolumeInner>> volumes = anfClient.volumes().listAsync(
                            parameters[0],
                            parameters[1],
                            parameters[2]);
                    return volumes.toBlocking().first();

                case "SnapshotInner":
                    Observable<List<SnapshotInner>> snapshots = anfClient.snapshots().listAsync(
                            parameters[0],
                            parameters[1],
                            parameters[2],
                            parameters[3]);
                    return snapshots.toBlocking().first();
            }
        }
        catch (Exception e)
        {
            Utils.writeErrorMessage("Error listing resource - " + e.getMessage());
            throw e;
        }

        return null;
    }

    /**
     * Method to overload function waitForNoANFResource(client, string, int, int, clazz) with default values
     * @param anfClient Azure NetApp Files Management Client
     * @param resourceId Resource id of the resource that was deleted
     * @param clazz Valid class types: NetAppAccountInner, CapacityPoolInner, VolumeInner, SnapshotInner
     */
    public static synchronized <T> void waitForNoANFResource(AzureNetAppFilesManagementClientImpl anfClient, String resourceId, Class<T> clazz)
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
    public static synchronized <T> void waitForNoANFResource(AzureNetAppFilesManagementClientImpl anfClient, String resourceId, int intervalInSec, int retries, Class<T> clazz)
    {
        for (int i = 0; i < retries; i++)
        {
            Utils.threadSleep(intervalInSec*1000);

            try
            {
                switch (clazz.getSimpleName())
                {
                    case "NetAppAccountInner":
                        Observable<NetAppAccountInner> account = anfClient.accounts().getByResourceGroupAsync(ResourceUriUtils.getResourceGroup(resourceId),
                                ResourceUriUtils.getAnfAccount(resourceId));
                        if (account.toBlocking().first() == null)
                            return;

                        continue;

                    case "CapacityPoolInner":
                        Observable<CapacityPoolInner> pool = anfClient.pools().getAsync(ResourceUriUtils.getResourceGroup(resourceId),
                                ResourceUriUtils.getAnfAccount(resourceId),
                                ResourceUriUtils.getAnfCapacityPool(resourceId));
                        if (pool.toBlocking().first() == null)
                            return;

                        continue;

                    case "VolumeInner":
                        Observable<VolumeInner> volume = anfClient.volumes().getAsync(ResourceUriUtils.getResourceGroup(resourceId),
                                ResourceUriUtils.getAnfAccount(resourceId),
                                ResourceUriUtils.getAnfCapacityPool(resourceId),
                                ResourceUriUtils.getAnfVolume(resourceId));
                        if (volume.toBlocking().first() == null)
                            return;

                        continue;

                    case "SnapshotInner":
                        Observable<SnapshotInner> snapshot = anfClient.snapshots().getAsync(ResourceUriUtils.getResourceGroup(resourceId),
                                ResourceUriUtils.getAnfAccount(resourceId),
                                ResourceUriUtils.getAnfCapacityPool(resourceId),
                                ResourceUriUtils.getAnfVolume(resourceId),
                                ResourceUriUtils.getAnfSnapshot(resourceId));
                        if (snapshot.toBlocking().first() == null)
                            return;

                        continue;
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
