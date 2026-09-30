package com.example.sourcedragons;

public enum Element {
    FIRE("fire_essence"),
    ICE("ice_essence"),
    EARTH("earth_essence"),
    LIGHTNING("lightning_essence"),
    TECH("tech_essence"),
    LIFE("life_essence");

    public final String id;

    Element(String id) {
        this.id = id;
    }
}
