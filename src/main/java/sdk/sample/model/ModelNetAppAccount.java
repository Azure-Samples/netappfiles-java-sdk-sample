// Copyright (c) Microsoft and contributors.  All rights reserved.
//
// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package sdk.sample.model;

import java.util.List;

// Instantiates a ModelNetAppAccount object
public class ModelNetAppAccount
{
    // A list of capacity pools within the Account
    private List<ModelCapacityPool> capacityPools;

    // Name of Account
    private String name;

    // Location of Account
    private String location;


    public List<ModelCapacityPool> getCapacityPools() {
        return capacityPools;
    }

    public void setCapacityPools(List<ModelCapacityPool> capacityPools) {
        this.capacityPools = capacityPools;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
