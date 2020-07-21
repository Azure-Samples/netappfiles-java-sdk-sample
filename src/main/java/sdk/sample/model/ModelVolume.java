// Copyright (c) Microsoft and contributors.  All rights reserved.
//
// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package sdk.sample.model;

import java.util.List;

// Instantiates a ModelVolume object
public class ModelVolume
{
    // List of Export Policies
    private List<ModelExportPolicyRule> exportPolicies;

    /*
      Volume's Usage Threshold.
      Maximum storage quota allowed for a file system in bytes. This is a soft quota used for alerting only.
      Minimum size is 100 GiB. Upper limit is 100TiB. Number must  be represented in bytes = 107370000000.
     */
    private long usageThreshold;

    /*
      Volume's Creation Token or File Path
      A unique file path for the volume. Used when creating mount targets
     */
    private String creationToken;

    // Volume type
    private String type;

    // The Volume's name
    private String name;

    // The Azure Resource URI for a delegated subnet. Must have the delegation Microsoft.NetApp/volumes
    private String subnetId;


    public List<ModelExportPolicyRule> getExportPolicies() {
        return exportPolicies;
    }

    public void setExportPolicies(List<ModelExportPolicyRule> exportPolicies) {
        this.exportPolicies = exportPolicies;
    }

    public long getUsageThreshold() {
        return usageThreshold;
    }

    public void setUsageThreshold(long usageThreshold) {
        this.usageThreshold = usageThreshold;
    }

    public String getCreationToken() {
        return creationToken;
    }

    public void setCreationToken(String creationToken) {
        this.creationToken = creationToken;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubnetId() {
        return subnetId;
    }

    public void setSubnetId(String subnetId) {
        this.subnetId = subnetId;
    }
}
