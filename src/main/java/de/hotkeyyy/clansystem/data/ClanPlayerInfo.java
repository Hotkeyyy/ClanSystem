package de.hotkeyyy.clansystem.data;

import java.util.UUID;

public class ClanPlayerInfo {

    public final UUID id;
    public final String name;
    public final int clanID;

    public ClanPlayerInfo(UUID uniqueId, String name, int i) {
        this.id = uniqueId;
        this.name = name;
        this.clanID = i;
    }
}
