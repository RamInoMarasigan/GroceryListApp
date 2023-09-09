package com.bala.firebaselogin;

public class GroceryItem {
    private String name;
    private boolean status;

    public GroceryItem() {
        // Default constructor required for Firestore
    }

    public GroceryItem(String name, boolean status) {
        this.name = name;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public boolean getStatus() {
        return status;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
