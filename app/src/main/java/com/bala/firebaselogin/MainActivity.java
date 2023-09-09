package com.bala.firebaselogin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private EditText editTextItemName;
    private Button buttonAddItem;
    private ListView listViewGroceryList;

    private ArrayList<String> groceryList;
    private ArrayAdapter<String> groceryListAdapter;

    private Button logoutButton;
    private Button buttonRemoveItems;
    private TextView welcomeTextView;

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();

        editTextItemName = findViewById(R.id.editTextItemName);
        buttonAddItem = findViewById(R.id.buttonAddItem);
        listViewGroceryList = findViewById(R.id.listViewGroceryList);

        logoutButton = findViewById(R.id.maLogoutButton);
        //buttonRemoveItems = findViewById(R.id.buttonRemoveItems);

        welcomeTextView = findViewById(R.id.tv);

        groceryList = new ArrayList<>();
        groceryListAdapter = new ArrayAdapter<>(this, R.layout.list_item_grocery, R.id.textViewItemName, groceryList);
        listViewGroceryList.setAdapter(groceryListAdapter);
        listViewGroceryList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

        // Show the user's email when logging in
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        String welcomeText = "Welcome, " + email + "!";
        welcomeTextView.setText(welcomeText);

        //Logout button
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Retrieve grocery list
        DocumentReference userDocRef = db.collection("user").document(email);
        userDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    List<String> items = (List<String>) documentSnapshot.get("items");
                    if (items != null) {
                        groceryList.addAll(items);
                        groceryListAdapter.notifyDataSetChanged();
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Failed to retrieve grocery list", Toast.LENGTH_SHORT).show();
            }
        });

        //Add item button
        buttonAddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String itemName = editTextItemName.getText().toString().trim();
                if (!itemName.isEmpty()) {
                    String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                    DocumentReference userDocRef = db.collection("user").document(email);

                    userDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                // Document exists, update the "items" field
                                List<String> items = (List<String>) documentSnapshot.get("items");
                                if (items == null) {
                                    items = new ArrayList<>();
                                }
                                items.add(itemName);
                                userDocRef.update("items", items)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(MainActivity.this, "Successful", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                // Document doesn't exist, create a new document with the "items" field
                                Map<String, Object> user = new HashMap<>();
                                user.put("items", Arrays.asList(itemName));

                                userDocRef.set(user)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(MainActivity.this, "Successful", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    });

                    groceryList.add(itemName);
                    groceryListAdapter.notifyDataSetInvalidated();
                    editTextItemName.setText("");
                }
            }
        });

        //region Not working remove button
        /*
        //Remove button
        buttonRemoveItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SparseBooleanArray checkedItemPositions = listViewGroceryList.getCheckedItemPositions();
                List<String> itemsToDelete = new ArrayList<>();

                for (int i = checkedItemPositions.size() - 1; i >= 0; i--) {
                    int position = checkedItemPositions.keyAt(i);
                    if (checkedItemPositions.valueAt(i)) {
                        String item = groceryList.get(position);
                        itemsToDelete.add(item);
                    }
                }

                deleteItemsFromFirebase(itemsToDelete);
            }
        });
    }

    //METHOD------------------------------------------------------------
    private void deleteItemsFromFirebase(List<String> itemsToDelete) {
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        DocumentReference userDocRef = db.collection("user").document(email);

        userDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    List<String> items = (List<String>) documentSnapshot.get("items");
                    if (items != null) {
                        items.removeAll(itemsToDelete);

                        userDocRef.update("items", items)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(MainActivity.this, "Items deleted successfully", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(MainActivity.this, "Failed to delete items", Toast.LENGTH_SHORT).show();
                                    }
                                });

                        groceryList.clear();
                        groceryList.addAll(items);
                        groceryListAdapter.notifyDataSetChanged();
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Failed to retrieve grocery list", Toast.LENGTH_SHORT).show();
            }
        });
         */
        //endregion
    }
}