package io.gex.core.model;

import java.util.List;

public class CheckHardwareResult {
    private NetworkAdapter networkAdapter;
    private List<NetworkAdapter> allNetworkAdapters;

    public NetworkAdapter getNetworkAdapter() {
        return networkAdapter;
    }

    public void setNetworkAdapter(NetworkAdapter networkAdapter) {
        this.networkAdapter = networkAdapter;
    }

    public List<NetworkAdapter> getAllNetworkAdapters() {
        return allNetworkAdapters;
    }

    public void setAllNetworkAdapters(List<NetworkAdapter> allNetworkAdapters) {
        this.allNetworkAdapters = allNetworkAdapters;
    }
}
