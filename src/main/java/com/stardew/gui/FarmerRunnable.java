package com.stardew.gui;

import org.w3c.dom.Node;

import lombok.Getter;

@Getter
public abstract class FarmerRunnable implements Runnable {
    private final Long playerUid;
    private final Long farmerUid;

    public FarmerRunnable(Long playerUid, Long farmerUid) {
        this.playerUid = playerUid;
        this.farmerUid = farmerUid;
    }
}
