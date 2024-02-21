package com.example.findany.Firebase;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.findany.callbacks.BooleanCallBack;
import com.example.findany.callbacks.FirestoreCallback;
import com.example.findany.callbacks.ListReceived;
import com.example.findany.callbacks.MapCallback;
import com.example.findany.callbacks.StringCallback;
import com.example.findany.utils.sharedprefs;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Firestore {
    private static final String TAG = "FirestoreUtil";
    private static FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static void uploadData(Map<String, Object> data, String collectionName, String documentName, FirestoreCallback callback) {
        DocumentReference documentReference = db.collection(collectionName).document(documentName);

        documentReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    documentReference.update(data)
                            .addOnSuccessListener(aVoid -> {
                                if (callback != null) {
                                    callback.onSuccess();
                                }
                            })
                            .addOnFailureListener(e -> {
                                if (callback != null) {
                                    callback.onFailure(e);
                                }
                            });
                } else {
                    documentReference.set(data)
                            .addOnSuccessListener(aVoid -> {
                                if (callback != null) {
                                    callback.onSuccess();
                                }
                            })
                            .addOnFailureListener(e -> {
                                if (callback != null) {
                                    callback.onFailure(e);
                                }
                            });
                }
            } else {
                if (callback != null) {
                    callback.onFailure(task.getException());
                }
            }
        });
    }

    public static void deleteDocument(String collectionName, String documentName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference documentRef = db.collection(collectionName).document(documentName);

        documentRef.delete()
                .addOnSuccessListener(aVoid -> {
                    // Document successfully deleted
                    System.out.println("DocumentSnapshot successfully deleted!");
                })
                .addOnFailureListener(e -> {
                    // Handle errors here
                    System.out.println("Error deleting document: " + e.getMessage());
                });
    }

    public static void deleteDocument(String collectionName, String documentName, String subcollectionName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference documentRef = db.collection(collectionName).document(documentName);

        documentRef.delete()
                .addOnSuccessListener(aVoid -> {
                    // Document successfully deleted
                    System.out.println("DocumentSnapshot successfully deleted!");
                })
                .addOnFailureListener(e -> {
                    // Handle errors here
                    System.out.println("Error deleting document: " + e.getMessage());
                });

        deleteSubCollection(documentRef, subcollectionName);
    }

    public static void deleteSubCollection(DocumentReference documentRef, String subcollection) {
        CollectionReference collectionReference = documentRef.collection(subcollection);

        // Check if subcollection exists before attempting to delete
        collectionReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();

                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        // Subcollection exists, proceed with deletion
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            document.getReference().delete();
                        }

                        // After deleting all documents in the subcollection, delete the subcollection itself
                        deleteSubcollectionReference(documentRef, subcollection);
                    } else {
                        // Subcollection does not exist
                        System.out.println("Subcollection does not exist.");
                    }
                } else {
                    // Error getting documents
                    Exception exception = task.getException();
                    if (exception != null) {
                        System.out.println("Error getting documents: " + exception.getMessage());
                    }
                }
            }
        });
    }

    // Helper method to delete the subcollection reference itself
    private static void deleteSubcollectionReference(DocumentReference documentRef, String subcollection) {
        documentRef.collection(subcollection).document().delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        System.out.println("Subcollection deleted successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println("Error deleting subcollection: " + e.getMessage());
                    }
                });
    }


    public static void getFieldValueAndSaveToPrefs(Context context, String collectionName, String documentId, String fieldName, String prefName, String prefKey) {
        DocumentReference docRef = db.collection(collectionName).document(documentId);

        docRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Get the value of the specified field
                            Object fieldValue = document.get(fieldName);
                            if (fieldValue != null) {
                                // Save the field value to SharedPreferences
                                sharedprefs.saveValueToPrefs(context, prefName, prefKey, fieldValue.toString());
                                System.out.println("Field Value: " + fieldValue.toString());
                            } else {
                                System.out.println("Field not found or is null");
                            }
                        } else {
                            System.out.println("Document does not exist");
                        }
                    } else {
                        System.out.println("Error getting document: " + task.getException());
                    }
                });
    }

    public static void returnFieldValue(
            String collectionName,
            String documentId,
            String fieldName,
            OnFieldValueCallback callback) {

        DocumentReference docRef = db.collection(collectionName).document(documentId);

        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Get the value of the specified field
                    Object fieldValue = document.get(fieldName);
                    if (fieldValue != null) {
                        // Return the field value through the callback
                        callback.onCallback(fieldValue.toString());
                    } else {
                        System.out.println("Field not found or is null");
                    }
                } else {
                    System.out.println("Document does not exist");
                }
            } else {
                System.out.println("Error getting document: " + task.getException());
            }
        });
    }

    public interface OnFieldValueCallback {
        void onCallback(String value);
    }

    public static void getAllDataFromDocument(Context context, String collectionName, String documentId, FirestoreCallback callback) {
        DocumentReference docRef = db.collection(collectionName).document(documentId);

        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null) {
                    if (document.exists()) {
                        Map<String, Object> data = document.getData();
                        if (data != null) {
                            for (Map.Entry<String, Object> entry : data.entrySet()) {
                                String key = entry.getKey();
                                Object value = entry.getValue();
                                sharedprefs.saveValueToPrefs(context, "UserDetails", key, (value != null) ? value.toString() : null);
                                System.out.println("Key: " + key + ", Value: " + value);
                            }
                        } else {
                            System.out.println("Document is empty");
                        }
                        // Execute the callback indicating success
                        if (callback != null) {
                            callback.onSuccess();
                        }
                    } else {
                        System.out.println("Document does not exist");
                        // Execute the callback indicating failure
                        if (callback != null) {
                            callback.onFailure(new Exception("Document does not exist"));
                        }
                    }
                } else {
                    System.out.println("Document is null");
                    // Execute the callback indicating failure
                    if (callback != null) {
                        callback.onFailure(new Exception("Document is null"));
                    }
                }
            } else {
                System.out.println("Error getting document: " + task.getException());
                // Execute the callback indicating failure
                if (callback != null) {
                    callback.onFailure(task.getException());
                }
            }
        });
    }


    public static void storeDataInFirestore(String collection, String documentName, Map<String, String> data) {
        // Get a reference to the document
        DocumentReference documentRef = db.collection(collection).document(documentName);

        // Check if the document exists
        documentRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Document exists, update the document with the specified data
                            updateDocumentFields(documentRef, data);
                        } else {
                            // Document does not exist, create it with the specified data
                            createDocumentWithFields(documentRef, data);
                        }
                    } else {
                        // Handle failures
                        Log.e(TAG, "Error checking document existence", task.getException());
                    }
                });
    }

    private static void updateDocumentFields(DocumentReference documentRef, Map<String, String> data) {
        // Convert the Map<String, String> to Map<String, Object>
        Map<String, Object> updatedData = new HashMap<>();
        for (Map.Entry<String, String> entry : data.entrySet()) {
            updatedData.put(entry.getKey(), entry.getValue());
        }

        // Update the specified fields in the document
        documentRef.update(updatedData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {
                        // Handle failures
                        Log.e(TAG, "Error updating fields", task.getException());
                    }
                });
    }

    private static void createDocumentWithFields(DocumentReference documentRef, Map<String, String> data) {
        // Create a new document with the specified fields and values
        documentRef.set(data)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Document created with data: " + data + ", Document ID: " + documentRef.getId());
                    } else {
                        // Handle failures
                        Log.e(TAG, "Error creating document", task.getException());
                    }
                });
    }
    public static void uploadFieldAndValueToSubDocument(String collection, String documentId, String subcollection, String subdocumentId,Map<String,String> data,FirestoreCallback callback) {
        // Reference to the specific document in the collection
        DocumentReference documentReference = db.collection(collection).document(documentId);

        // Reference to the subcollection within the document
        CollectionReference subcollectionReference = documentReference.collection(subcollection);

        // Reference to the subdocument within the subcollection
        DocumentReference subdocumentReference = subcollectionReference.document(subdocumentId);

        // Check if the document in the subcollection already exists
        subdocumentReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                // Document exists, update the field
                updateDocumentFields(subdocumentReference, data,callback);
            } else {
                // Document doesn't exist, create the document with the field
                createDocumentWithFields(subdocumentReference, data,callback);
            }
        });
    }

    private static void updateDocumentFields(DocumentReference documentRef, Map<String, String> data,FirestoreCallback callback) {
        // Convert the Map<String, String> to Map<String, Object>
        Map<String, Object> updatedData = new HashMap<>();
        for (Map.Entry<String, String> entry : data.entrySet()) {
            updatedData.put(entry.getKey(), entry.getValue());
        }

        // Update the specified fields in the document
        documentRef.update(updatedData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess();
                    } else {
                        // Handle failures
                        Log.e(TAG, "Error updating fields", task.getException());
                    }
                });
    }

    private static void createDocumentWithFields(DocumentReference documentRef, Map<String, String> data,FirestoreCallback callback) {
        // Create a new document with the specified fields and values
        documentRef.set(data)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Document created with data: " + data + ", Document ID: " + documentRef.getId());
                        callback.onSuccess();
                    } else {
                        // Handle failures
                        Log.e(TAG, "Error creating document", task.getException());
                    }
                });
    }
    public static void removeValueFromFirestore(FirebaseFirestore db, String collectionName, String documentName, String regNo) {
        // Specify the document reference
        DocumentReference documentReference = db.collection(collectionName).document(documentName);

        // Create a map to represent the data to update
        Map<String, Object> updateData = new HashMap<>();
        updateData.put(regNo, FieldValue.delete());

        // Use update with the updateData map
        documentReference.update(updateData)
                .addOnSuccessListener(aVoid -> {
                    // Successfully removed the FCM token
                    Log.d(TAG, "FCM Token for RegNo " + regNo + " removed successfully from Firestore");
                })
                .addOnFailureListener(e -> {
                    // Check if the failure is due to document not found
                    if (e instanceof FirebaseFirestoreException) {
                        FirebaseFirestoreException firestoreException = (FirebaseFirestoreException) e;
                        if (firestoreException.getCode() == FirebaseFirestoreException.Code.NOT_FOUND) {
                            // Log the information without treating it as an error
                            Log.d(TAG, "Document not found for RegNo " + regNo);
                            return; // Exit the method to prevent the error log below
                        }
                    }
                    // Failed to remove the FCM token (treat as error)
                    Log.e(TAG, "Error removing FCM Token for RegNo " + regNo + " from Firestore", e);
                });
    }
    public static void getAllFcmTokens(FirebaseFirestore db, String collection, String documentName, OnCompleteListener<List<String>> onCompleteListener) {
        DocumentReference documentReference = db.collection(collection).document(documentName);

        documentReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    Map<String, Object> documentData = documentSnapshot.getData();
                    List<String> fcmTokens = new ArrayList<>();

                    if (documentData != null) {
                        for (Object value : documentData.values()) {
                            if (value instanceof String) {
                                fcmTokens.add((String) value);
                            }
                        }
                    }

                    onCompleteListener.onComplete(Tasks.forResult(fcmTokens));  // Use Tasks.forResult
                } else {
                    Log.d(TAG, "Document does not exist");
                    onCompleteListener.onComplete(Tasks.forResult(new ArrayList<>()));  // Empty list for non-existent document
                }
            } else {
                Log.e(TAG, "Error getting FCM tokens from Firestore", task.getException());
                onCompleteListener.onComplete(Tasks.forException(task.getException()));  // Use Tasks.forException
            }
        });
    }
    public static void getFileFromStorage(Context context, String filepath, String filename, String prefname) {
        FirebaseStorage storage = FirebaseStorage.getInstance("gs://adroit-chemist-368310.appspot.com");
        StorageReference storageRef = storage.getReference().child(filepath).child(filename);

        storageRef.getMetadata().addOnCompleteListener(metadataTask -> {
            if (metadataTask.isSuccessful()) {
                StorageMetadata metadata = metadataTask.getResult();
                if (metadata != null) {
                    long lastModifiedMillis = metadata.getUpdatedTimeMillis();
                    long lastModifiedSeconds = lastModifiedMillis / 1000;

                    String savedLastModifiedSeconds = sharedprefs.getValueFromPrefs(context, prefname, filename);

                    Log.d(TAG, "Last Modified (seconds): " + lastModifiedSeconds);

                    if (savedLastModifiedSeconds == null) {
                        // Download the file since the timestamp is not available
                        downloadFile(context, storageRef, filename, prefname, lastModifiedSeconds);
                    } else {
                        // Check if it's a valid numeric value before parsing
                        try {
                            long savedLastModified = Long.parseLong(savedLastModifiedSeconds);

                            if (savedLastModified < lastModifiedSeconds) {
                                // Download the file only if it's newer
                                downloadFile(context, storageRef, filename, prefname, lastModifiedSeconds);
                            } else {
                                Log.d(TAG, filename + ": is up-to-date. No download needed.");
                            }
                        } catch (NumberFormatException e) {
                            Log.e(TAG, "Invalid numeric format for savedLastModifiedSeconds: " + savedLastModifiedSeconds);
                        }
                    }
                }
            } else {
                Log.e(TAG, "Failed to get metadata: " + metadataTask.getException());
            }
        });
    }

    public static void downloadFile(Context context, StorageReference storageRef, String filename, String prefname, long lastModifiedSeconds) {
        File localFile = new File(context.getDatabasePath(filename).getPath());

        StorageTask<FileDownloadTask.TaskSnapshot> storageTask = storageRef.getFile(localFile);

        storageTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, filename + ": Downloaded successfully");

                // Save the new last modified time to SharedPreferences
                sharedprefs.saveValueToPrefs(context, prefname, filename, String.valueOf(lastModifiedSeconds));
            } else {
                Log.e(TAG, filename + ": Download failed: " + task.getException());
            }
        });
    }

    public static void getAllDataFromCollection(Context context, String collectionName, String extraString, String prefName, String documentname, String timefieldname, String timepref) {

        returnFieldValue(collectionName, documentname, timefieldname, new OnFieldValueCallback() {
            @Override
            public void onCallback(String value) {

                if(value!=null && !value.isEmpty()){
                    long lastmodified=Long.parseLong(value);
                    String stringValue = sharedprefs.getValueFromPrefs(context, timepref, collectionName + "_modified");
                    long savedmodified = 0;

                    if (stringValue != null && !stringValue.isEmpty()) {
                        try {
                            savedmodified = Long.parseLong(stringValue);
                        } catch (NumberFormatException e) {
                            System.out.println("Error parsing value to long: " + e.getMessage());
                        }
                    }

                    Log.d(TAG,"Value is not null: "+value+"  lastmodified: "+lastmodified+"  savedmodified: "+savedmodified);

                    if (lastmodified > savedmodified) {
                        loopGetValues(context,collectionName,extraString,prefName,timepref,lastmodified);
                    }else{
                        Log.d(TAG,collectionName+" data is upToDate");
                    }

                }else{
                    Log.d(TAG,"Value is null:");
                }
            }
        });
    }

    public static void loopGetValues(Context context,String collectionName,String extraString,String prefName,String timepref,long longlastmodified){
        db.collection(collectionName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String documentId = document.getId();

                            Map<String, Object> fields = document.getData();

                            if (fields != null) {
                                String detailsJson = convertToJson(fields);
                                sharedprefs.saveValueToPrefs(context, prefName, documentId + extraString, detailsJson);
                            }
                        }
                        // Save the new last modified timestamp
                        sharedprefs.saveValueToPrefs(context, timepref, collectionName + "_modified", String.valueOf(longlastmodified));
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }
    private static String convertToJson(Map<String, Object> fields) {
        JSONObject json = new JSONObject(fields);
        return json.toString();
    }
    public static void CurrentTimestamp() {
        long currentTimestampMillis = System.currentTimeMillis();
        System.out.println("Current Timestamp in milliseconds: " + currentTimestampMillis);

        // If you want the timestamp in seconds, divide by 1000
        long currentTimestampSeconds = currentTimestampMillis / 1000;
        System.out.println("Current Timestamp in seconds: " + currentTimestampSeconds);

    }
    public static void getsubvalues(Context context, String collectionName, String documentName, String subCollectionName, String subDocumentName, String prefName, String key,String timepref) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection(collectionName).document(documentName);

        returnFieldValue(collectionName, documentName, "Changes", new OnFieldValueCallback() {
            @Override
            public void onCallback(String value) {
                if(value!=null && !value.isEmpty()){
                    long lastmodified=Long.parseLong(value);
                    String stringValue = sharedprefs.getValueFromPrefs(context, timepref, collectionName + "_modified");
                    long savedmodified = 0;

                    if (stringValue != null && !stringValue.isEmpty()) {
                        try {
                            savedmodified = Long.parseLong(stringValue);
                        } catch (NumberFormatException e) {
                            System.out.println("Error parsing value to long: " + e.getMessage());
                        }
                    }

                    Log.d(TAG,"Value is not null: "+value+"  lastmodified: "+lastmodified+"  savedmodified: "+savedmodified);

                    if (lastmodified > savedmodified) {

                        docRef.get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    Log.d(TAG, "DocumentSnapshot data: " + document.getData());

                                    // Access the sub-collection
                                    DocumentReference subDocRef = docRef.collection(subCollectionName).document(subDocumentName);
                                    subDocRef.get().addOnCompleteListener(subTask -> {
                                        if (subTask.isSuccessful()) {
                                            DocumentSnapshot subDocument = subTask.getResult();
                                            if (subDocument.exists()) {
                                                // Save values in SharedPreferences
                                                sharedprefs.saveValueToPrefs(context, prefName,key+subDocumentName, String.valueOf(subDocument.getData()));
                                                sharedprefs.saveValueToPrefs(context, timepref, collectionName + "_modified", String.valueOf(lastmodified));

                                                Log.d(TAG,"get slots: "+String.valueOf(subDocument.getData()));
                                            } else {
                                                Log.d(TAG, "No such subdocument");
                                            }
                                        } else {
                                            Log.d(TAG, "get failed with ", subTask.getException());
                                        }
                                    });
                                } else {
                                    Log.d(TAG, "No such document");
                                }
                            } else {
                                Log.d(TAG, "get failed with ", task.getException());
                            }
                        });


                    }else{
                        Log.d(TAG,collectionName+" data is upToDate");
                    }
                }else{
                    Log.d(TAG,collectionName+" Value is null:");
                }
            }
        });
    }
    public static void updateFieldInAllDocuments(final String collectionPath, final String fieldToUpdate, final Object newValue) {
        // Get a reference to the collection
        CollectionReference collectionRef = db.collection(collectionPath);

        // Retrieve all documents in the collection
        collectionRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            // Iterate through all documents
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Get the reference to each document
                                DocumentReference documentRef = collectionRef.document(document.getId());

                                // Check if the field exists in the document
                                if (document.contains(fieldToUpdate)) {
                                    // Create a map with the field and its new value
                                    Map<String, Object> updateMap = new HashMap<>();
                                    updateMap.put(fieldToUpdate, newValue);

                                    // Update the document with the new value for the specified field
                                    documentRef.update(updateMap)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        // Log the document name when the update is successful
                                                        Log.d(TAG, "Document updated successfully: " + document.getId());
                                                    } else {
                                                        Log.d(TAG, "Unable to update the document: " + document.getId());
                                                    }
                                                }
                                            });
                                } else {
                                    Log.d(TAG, "Field does not exist in the document: " + document.getId());
                                }
                            }
                        } else {
                            Log.d(TAG, "Collection Not found");
                        }
                    }
                });
    }

    public static Task<Boolean> isAdmin(String documentName, String regNo) {
        // Get a reference to the "ADMINS" collection
        CollectionReference adminsCollectionRef = db.collection("ADMINS");

        // Get the reference to the specific document
        DocumentReference documentRef = adminsCollectionRef.document(documentName);

        // Check if the document exists and contains the "RegNo" field
        return documentRef.get().continueWith(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot.exists() && documentSnapshot.contains(regNo)) {
                    // The document exists and contains the "RegNo" field
                    return true;
                } else {
                    // The document does not exist or does not contain the "RegNo" field
                    return false;
                }
            } else {
                // Handle the failure to get the document
                return false;
            }
        });
    }
    public static void getAllDocumentIds(String collectionName, final ListReceived listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference collectionReference = db.collection(collectionName);

        collectionReference.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<String> documentIds = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Retrieve the document ID
                                String documentId = document.getId();
                                documentIds.add(documentId);
                            }

                            // Callback to the listener with the list of document IDs
                            listener.onListSuccess(documentIds);
                        } else {
                            // Callback to the listener with the error
                            listener.onListFailure(task.getException());
                        }
                    }
                });
    }

    public static void getFieldValueData(String collectionName, String documentName, MapCallback callback) {
        DocumentReference docRef = db.collection(collectionName).document(documentName);

        // Retrieve the document
        docRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            // Document exists, get the data as a Map
                            Map<String, Object> data = documentSnapshot.getData();

                            // Callback to the listener with the data
                            if (callback != null) {
                                callback.onCallback(data);
                            }
                        } else {
                            Log.d("FirestoreData", "No such document");
                            if (callback != null) {
                                callback.onCallback(null); // Document does not exist, return null
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("FirestoreData", "Error getting document", e);
                        if (callback != null) {
                            callback.onFailure(e);
                        }
                    }
                });
    }


    public static void checkDocumentExistsAndCreateIfNotExists(String collectionName, String documentId, BooleanCallBack callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection(collectionName).document(documentId);

        docRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Document exists
                            deleteAllFields(docRef, document);
                            callback.onExists(true);
                        } else {
                            // Document does not exist, create it with empty fields
                            callback.onExists(false);
                        }
                    } else {
                        // Handle errors
                        callback.onError(task.getException());
                    }
                });
    }

    private static void deleteAllFields(DocumentReference docRef, DocumentSnapshot document) {
        Map<String, Object> data = document.getData();
        if (data != null) {
            // Iterate through all fields and delete them
            for (String fieldName : data.keySet()) {
                docRef.update(fieldName, FieldValue.delete());
            }
        }
    }

    public static void createDocumentWithEmptyFields(String collectionName, String documentId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a map with empty fields
        Map<String, Object> data = new HashMap<>();
        // Add additional fields as needed

        db.collection(collectionName)
                .document(documentId)
                .set(data)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Document created with empty fields"))
                .addOnFailureListener(e -> Log.e(TAG, "Error creating document with empty fields", e));
    }
    public static void retrieveDocumentNames(String collectionName, String documentName, String subcollectionName, String fieldName, StringCallback callback) {

        CollectionReference collectionReference = db.collection(collectionName)
                .document(documentName)
                .collection(subcollectionName);

        collectionReference.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                // Add the document name (ID) to the list
                                String documentId = document.getId();
                                if (document.getData() != null) {
                                    Log.d(TAG, "FieldName to find: " + fieldName);
                                    Log.d(TAG, "Document ID: " + documentId);

                                    // Check if the field name is not empty or null
                                    if (fieldName != null && !fieldName.isEmpty()) {
                                        // Check if the specified field exists in the document
                                        if (document.contains(fieldName)) {
                                            // Retrieve the field value for the specified field name
                                            Object fieldValue = document.get(fieldName);
                                            // Invoke the callback with the retrieved field value
                                            Log.d(TAG, "FieldName Found: " + fieldName);
                                            callback.onFieldValueRetrieved(fieldValue.toString());

                                            // Break out of the loop once the field is found
                                            return;
                                        } else {
                                            Log.d(TAG, "Field '" + fieldName + "' Not Found in Document ID: " + documentId);
                                        }
                                    } else {
                                        // Handle the case where fieldName is empty or null
                                        Log.e(TAG, "Invalid fieldName");
                                        callback.onFieldNotFound("Invalid fieldName");
                                        return;
                                    }
                                }
                            }
                            // If the loop completes and no matching field is found
                            callback.onFieldNotFound("Field '" + fieldName + "' Not Found in any Document");
                        } else {
                            // Subcollection does not exist or is empty
                            callback.onFieldNotFound("Subcollection '" + subcollectionName + "' Not Found or Empty");
                        }
                    } else {
                        // Error in getting documents
                        Log.e(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }

    public static void checkSubcollections(String collectionName, String documentId, String subcollection, BooleanCallBack callback) {

        // Reference to the specific document in the collection
        DocumentReference documentReference = db.collection(collectionName).document(documentId);

        // Reference to the subcollection within the document
        CollectionReference subcollectionReference = documentReference.collection(subcollection);

        // Check if the subcollection exists
        subcollectionReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                // Subcollection exists
                callback.onExists(true);
            } else {
                // Subcollection does not exist
                callback.onExists(false);
            }
        });
    }


}
