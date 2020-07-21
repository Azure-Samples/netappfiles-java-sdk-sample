// Copyright (c) Microsoft and contributors.  All rights reserved.
//
// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package sdk.sample.common;

import sdk.sample.model.ModelNetAppAccount;
import com.google.gson.Gson;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.Map;

public class ProjectConfiguration
{
    // List of Accounts to be created
    private List<ModelNetAppAccount> accounts;

    // Subscription Id where account(s) will be deployed
    private String subscriptionId;

    // Resource group where the ANF account(s) will be created
    private String resourceGroup;

    public static ProjectConfiguration readFromJsonFile(String path)
    {
        Gson gson = new Gson();
        AppSettings appSettings;

        try
        {
            appSettings = gson.fromJson(new FileReader(path), AppSettings.class);
        }
        catch (FileNotFoundException e)
        {
            Utils.writeWarningMessage("Could not find appsettings.json. Unable to load project configuration. Exiting.");
            return null;
        }

        ProjectConfiguration config = new ProjectConfiguration();
        config.setAccounts(appSettings.getAccounts());
        config.setResourceGroup(appSettings.getGeneral().get("resourceGroup"));
        config.setSubscriptionId(appSettings.getGeneral().get("subscriptionId"));

        return config;
    }

    public List<ModelNetAppAccount> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<ModelNetAppAccount> accounts) {
        this.accounts = accounts;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(String resourceGroup) {
        this.resourceGroup = resourceGroup;
    }


    private static class AppSettings
    {
        private List<ModelNetAppAccount> accounts;
        private Map<String, String> general;

        public List<ModelNetAppAccount> getAccounts() {
            return accounts;
        }

        public void setAccounts(List<ModelNetAppAccount> accounts) {
            this.accounts = accounts;
        }

        public Map<String, String> getGeneral() {
            return general;
        }

        public void setGeneral(Map<String, String> general) {
            this.general = general;
        }
    }
}
