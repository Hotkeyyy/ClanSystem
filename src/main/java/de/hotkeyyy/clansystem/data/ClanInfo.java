package de.hotkeyyy.clansystem.data;

public class ClanInfo {

    public String name;
    public int id;
    public String ownerID;

    public ClanInfo(int id, String name, String ownerID) {
        this.name = name;
        this.id = id;
        this.ownerID = ownerID;
    }
}

