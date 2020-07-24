// Copyright (c) Microsoft and contributors.  All rights reserved.
//
// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package sdk.sample;

import com.ea.async.Async;
import sdk.sample.common.ProjectConfiguration;
import sdk.sample.common.ServiceCredentialsAuth;
import sdk.sample.common.Utils;
import com.microsoft.azure.management.netapp.v2019_11_01.implementation.AzureNetAppFilesManagementClientImpl;
import com.microsoft.rest.credentials.ServiceClientCredentials;

import java.util.concurrent.CompletableFuture;

import static com.ea.async.Async.await;

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

    private static CompletableFuture<Void> runAsync()
    {
        // Getting project configuration
        ProjectConfiguration config = Utils.getConfiguration("appsettings.json");
        if (config == null)
        {
            return CompletableFuture.completedFuture(null);
        }

        // Authenticating using service principal, refer to README.md file for requirement details
        ServiceClientCredentials credentials = await(ServiceCredentialsAuth.getServicePrincipalCredentials(System.getenv("AZURE_AUTH_LOCATION")));
        if (credentials == null)
        {
            return CompletableFuture.completedFuture(null);
        }

        // Instantiating a new ANF management client
        Utils.writeConsoleMessage("Instantiating a new Azure NetApp Files management client...");
        AzureNetAppFilesManagementClientImpl anfClient = new AzureNetAppFilesManagementClientImpl(credentials);
        anfClient.withSubscriptionId(config.getSubscriptionId());
        Utils.writeConsoleMessage("Api Version: " + anfClient.apiVersion());

        // Creating ANF resources (Account, Pool, Volumes)
        await(Creation.runCreationSampleAsync(config, anfClient));

        // Creating and restoring snapshots
        await(Snapshots.runSnapshotOperationsSampleAsync(config, anfClient));

        // Performing updates on Capacity Pools and Volumes
        await(Updates.runUpdateOperationsSampleAsync(config, anfClient));

        // WARNING: Destructive operations at this point. You can uncomment relevant lines to clean up all resources created in this example.
        // Deletion operations (snapshots, volumes, capacity pools and accounts)
        // We sleep for 200 seconds to make sure the snapshot that was used to create a new volume has completed the split operation, and also to make sure the new volume is ready.
//        Utils.writeConsoleMessage("Waiting 200 seconds for volume clone operation to complete...");
//        Utils.threadSleep(200000);
//        await(Cleanup.runCleanupTasksSampleAsync(config, anfClient));

        return CompletableFuture.completedFuture(null);
    }
}
