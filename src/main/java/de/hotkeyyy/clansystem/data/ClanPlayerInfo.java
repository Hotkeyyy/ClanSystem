package de.hotkeyyy.clansystem.data;

import java.util.UUID;

public class ClanPlayerInfo {

    public UUID id;
    public String name;
    public int clanID;

    public ClanPlayerInfo(UUID uniqueId, String name, int i) {
        this.id = uniqueId;
        this.name = name;
        this.clanID = i;
    }
}
