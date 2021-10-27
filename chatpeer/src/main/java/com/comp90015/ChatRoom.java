package com.comp90015;

import java.util.ArrayList;

public class ChatRoom {
    private String roomId;
    private Guest owner;
    private ArrayList<Guest> guests;

    public ChatRoom(String roomId, Guest owner) {
        this.roomId = roomId;
        this.owner = owner;
        this.guests = new ArrayList<>();
    }

    public String getRoomId() {return roomId;}

    public Guest getOwner() {return owner;}

    public ArrayList<Guest> getGuests() {return guests;}

    public void setOwner(Guest owner) {this.owner = owner;}

    public void addGuest(Guest guest) {this.guests.add(guest);}

    public void removeGuest(Guest guest) {this.guests.remove(guest);}
}
