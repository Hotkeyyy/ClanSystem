package de.hotkeyyy.clansystem.data;

public class ClanInfo {

    public final String name;
    public final int id;
    public final String ownerID;

    public ClanInfo(int id, String name, String ownerID) {
        this.name = name;
        this.id = id;
        this.ownerID = ownerID;
    }
}

