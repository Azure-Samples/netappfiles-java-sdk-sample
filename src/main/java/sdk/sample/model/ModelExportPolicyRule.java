// Copyright (c) Microsoft and contributors.  All rights reserved.
//
// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package sdk.sample.model;

// Instantiates a ModelExportPolicyRule object
public class ModelExportPolicyRule
{
    /*
      Rule index.
      This is initially non zero-based index, therefore must start with 1
     */
    private int ruleIndex;

    // Allowed clients
    private String allowedClients;

    // True if volume uses CIFS protocol
    private boolean cifs;

    // True if volume uses NFSv3 protocol
    private boolean nfsv3;

    // True if volume uses NFSv4 protocol
    private boolean nfsv4;

    // True if volume is read only
    private boolean unixReadOnly;

    // True if volume is read/write
    private boolean unixReadWrite;


    public int getRuleIndex() {
        return ruleIndex;
    }

    public void setRuleIndex(int ruleIndex) {
        this.ruleIndex = ruleIndex;
    }

    public String getAllowedClients() {
        return allowedClients;
    }

    public void setAllowedClients(String allowedClients) {
        this.allowedClients = allowedClients;
    }

    public boolean isCifs() {
        return cifs;
    }

    public void setCifs(boolean cifs) {
        this.cifs = cifs;
    }

    public boolean isNfsv3() {
        return nfsv3;
    }

    public void setNfsv3(boolean nfsv3) {
        this.nfsv3 = nfsv3;
    }

    public boolean isNfsv4() {
        return nfsv4;
    }

    public void setNfsv4(boolean nfsv4) {
        this.nfsv4 = nfsv4;
    }

    public boolean isUnixReadOnly() {
        return unixReadOnly;
    }

    public void setUnixReadOnly(boolean unixReadOnly) {
        this.unixReadOnly = unixReadOnly;
    }

    public boolean isUnixReadWrite() {
        return unixReadWrite;
    }

    public void setUnixReadWrite(boolean unixReadWrite) {
        this.unixReadWrite = unixReadWrite;
    }
}
