// Copyright (c) Microsoft and contributors.  All rights reserved.
//
// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package sdk.sample.model;

import java.util.List;

// // Instantiates a ModelCapacityPool object
public class ModelCapacityPool
{
    // List of Volumes in this Capacity Pool
    private List<ModelVolume> volumes;

    // Name of Capacity Pool
    private String name;

    /*
      Size of Capacity Pool.
      Provisioned size of the pool (in bytes). Allowed values are in 4TiB chunks (value must be multiply of 4398046511104).
     */
    private long size;

    /*
      Service Level.
      The service level of the file system. Possible values include: 'Standard', 'Premium','Ultra'
     */
    private String serviceLevel;


    public List<ModelVolume> getVolumes() {
        return volumes;
    }

    public void setVolumes(List<ModelVolume> volumes) {
        this.volumes = volumes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getServiceLevel() {
        return serviceLevel;
    }

    public void setServiceLevel(String serviceLevel) {
        this.serviceLevel = serviceLevel;
    }
}
