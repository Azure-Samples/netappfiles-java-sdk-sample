// Copyright (c) Microsoft and contributors.  All rights reserved.
//
// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package sdk.sample.common;

import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.rest.credentials.ServiceClientCredentials;

import java.io.File;
import java.io.IOException;

public class ServiceCredentialsAuth
{
    /**
     * Gets service principal based credentials
     * @param authEnvironmentVariable Environment variable that points to the file system secured azure auth settings
     * @return The service client credentials
     */
    public static synchronized ServiceClientCredentials getServicePrincipalCredentials(String authEnvironmentVariable)
    {
        if (authEnvironmentVariable == null)
        {
            Utils.writeWarningMessage("Environment variable AZURE_AUTH_LOCATION does not exist. Exiting");
            return null;
        }

        File file = new File(authEnvironmentVariable);
        if (!file.exists())
        {
            Utils.writeWarningMessage("Could not find azure auth file at " + authEnvironmentVariable + " to authenticate. Exiting");
            return null;
        }

        ApplicationTokenCredentials credentials;
        try
        {
            credentials = ApplicationTokenCredentials.fromFile(file);
        }
        catch (IOException e)
        {
            Utils.writeWarningMessage("Unable to create credentials from auth file\n");
            Utils.writeConsoleMessage(e.getMessage());
            return null;
        }

        return credentials;
    }
}
