package com.bala.firebaselogin;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class FirestoreManager {
    private FirebaseFirestore db;
    private CollectionReference groceryListRef;

    public FirestoreManager() {
        db = FirebaseFirestore.getInstance();
        groceryListRef = db.collection("groceryList");
    }

    public void addItem(GroceryItem item, OnCompleteListener<DocumentReference> onCompleteListener) {
        groceryListRef.add(item).addOnCompleteListener(onCompleteListener);
    }

    public void getItems(OnCompleteListener<QuerySnapshot> onCompleteListener) {
        groceryListRef.get().addOnCompleteListener(onCompleteListener);
    }

    public void updateItemStatus(String itemId, boolean status, OnCompleteListener<Void> onCompleteListener) {
        DocumentReference itemRef = groceryListRef.document(itemId);
        itemRef.update("status", status).addOnCompleteListener(onCompleteListener);
    }

    public void deleteItem(String itemId, OnCompleteListener<Void> onCompleteListener) {
        DocumentReference itemRef = groceryListRef.document(itemId);
        itemRef.delete().addOnCompleteListener(onCompleteListener);
    }
}