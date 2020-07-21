// Copyright (c) Microsoft and contributors.  All rights reserved.
//
// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package sdk.sample;

import sdk.sample.common.ProjectConfiguration;
import sdk.sample.common.ServiceCredentialsAuth;
import sdk.sample.common.Utils;
import com.microsoft.azure.management.netapp.v2019_11_01.implementation.AzureNetAppFilesManagementClientImpl;
import com.microsoft.rest.credentials.ServiceClientCredentials;

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
            runAsync();
            Utils.writeConsoleMessage("Sample application successfully completed execution");
        }
        catch (Exception e)
        {
            Utils.writeErrorMessage(e.getMessage());
        }

        System.exit(0);
    }

    private static synchronized void runAsync()
    {
        // Getting project configuration
        ProjectConfiguration config = Utils.getConfiguration("appsettings.json");
        if (config == null)
        {
            return;
        }

        // Authenticating using service principal, refer to README.md file for requirement details
        ServiceClientCredentials credentials = ServiceCredentialsAuth.getServicePrincipalCredentials(System.getenv("AZURE_AUTH_LOCATION"));
        if (credentials == null)
        {
            return;
        }

        // Instantiating a new ANF management client
        Utils.writeConsoleMessage("Instantiating a new Azure NetApp Files management client...");
        AzureNetAppFilesManagementClientImpl anfClient = new AzureNetAppFilesManagementClientImpl(credentials);
        anfClient.withSubscriptionId(config.getSubscriptionId());
        Utils.writeConsoleMessage("Api Version: " + anfClient.apiVersion());

        // Creating ANF resources (Account, Pool, Volumes)
        Creation.runCreationSampleAsync(config, anfClient);

        // Creating and restoring snapshots
        Snapshots.runSnapshotOperationsSampleAsync(config, anfClient);

        // Performing updates on Capacity Pools and Volumes
        Updates.runUpdateOperationsSampleAsync(config, anfClient);

        // WARNING: Destructive operations at this point. You can uncomment these lines to clean up all resources created in this example.
        // Deletion operations (snapshots, volumes, capacity pools and accounts)
//        Utils.writeConsoleMessage("Waiting for 200 seconds to let the snapshot used to create a new volume complete the split operation. Also to make sure the volume created from the snapshot is ready...");
//        Utils.threadSleep(200000);
//        Cleanup.runCleanupTasksSampleAsync(config, anfClient);
    }
}
