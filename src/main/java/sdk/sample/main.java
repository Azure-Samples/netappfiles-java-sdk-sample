// Copyright (c) Microsoft and contributors.  All rights reserved.
//
// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package sdk.sample;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.netapp.NetAppFilesManager;
import com.ea.async.Async;
import sdk.sample.common.ProjectConfiguration;
import sdk.sample.common.Utils;

public class main
{
    /**
     * Sample console application that executes CRUD management operations on Azure NetApp Files resources
     * @param args
     */
    public static void main( String[] args )
    {
        Utils.displayConsoleAppHeader();

        try
        {
            Async.init();
            runAsync();
            Utils.writeConsoleMessage("Sample application successfully completed execution");
        }
        catch (Exception e)
        {
            Utils.writeErrorMessage(e.getMessage());
        }

        // Note: this should not be here in a proper environment. I leave it here for a more compact sample that does what it needs to do and exits as soon as it finishes without waiting for other threads
        System.exit(0);
    }

    private static void runAsync()
    {
        // Getting project configuration
        ProjectConfiguration config = Utils.getConfiguration("appsettings.json");
        if (config == null)
            return;

        // Instantiating a new ANF management client
        AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
        TokenCredential credential = new DefaultAzureCredentialBuilder()
                .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
                .build();
        Utils.writeConsoleMessage("Instantiating a new Azure NetApp Files management client...");
        NetAppFilesManager manager = NetAppFilesManager
                .authenticate(credential, profile);

        // Creating ANF resources (Account, Pool, Volumes)
        Creation.runCreationSample(config, manager.serviceClient());

        // Creating and restoring snapshots
        Snapshots.runSnapshotOperationsSample(config, manager.serviceClient());

        // Performing updates on Capacity Pools and Volumes
        Updates.runUpdateOperationsSample(config, manager.serviceClient());

        // WARNING: Destructive operations at this point. You can uncomment relevant lines to clean up all resources created in this example.
        // Deletion operations (snapshots, volumes, capacity pools and accounts)
        // We sleep for 200 seconds to make sure the snapshot that was used to create a new volume has completed the split operation, and also to make sure the new volume is ready.
        //Utils.writeConsoleMessage("Waiting 200 seconds for volume clone operation to complete...");
        //Utils.threadSleep(200000);
        Cleanup.runCleanupTasksSample(config, manager.serviceClient());
    }
}
