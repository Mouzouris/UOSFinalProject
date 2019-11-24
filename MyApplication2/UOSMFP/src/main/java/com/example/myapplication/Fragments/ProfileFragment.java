package com.example.myapplication.Fragments;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.myapplication.Model.UserModel;
import com.example.myapplication.Model.UserSessionManager;
import com.example.myapplication.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.Collections;
import java.util.HashMap;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;
import static com.example.myapplication.Model.UserSessionManager.KEY_APPROVED;
import static com.example.myapplication.Model.UserSessionManager.KEY_TYPE;


public class ProfileFragment extends Fragment {
    private CircleImageView prof_image;
    private CircleImageView image_profile;
    private FirebaseUser Fuser;
    private TextView name;
    private EditText editText_update_name;
    private TextView mystatustextview;
    private EditText editText_update_surname;
    private StorageReference storageReference;
    private NavigationView navigationView;
    private RadioGroup RadioGroupUpdate;
    private static final int Image_request = 1;
    private Uri ImageUri;
    private ListenerRegistration seenListener;
    private StorageTask<UploadTask.TaskSnapshot> uploadTask;
    private CollectionReference collection;
    private DocumentReference document;
    private UserSessionManager session;
    private UserModel current_user;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getActivity();
        Fuser = FirebaseAuth.getInstance().getCurrentUser();
        //private DatabaseReference database;
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        //database = FirebaseDatabase.getInstance().getReference();
        session = new UserSessionManager(context);


        if (getArguments() != null) {
            String id = getArguments().get("id").toString();
            String email = getArguments().get("email").toString();
            String name = getArguments().get("name").toString();
            String surname = getArguments().get("surname").toString();
            String type = getArguments().get("type").toString();
            String approved = getArguments().get("approved").toString();
            String imageurl = getArguments().get("imageurl").toString();
            current_user = new UserModel(id, email, name, surname, type, approved, imageurl,(Collections.<String>emptyList()));

        }
        initialise_user();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        if (getActivity() != null) {
            prof_image = getActivity().findViewById(R.id.nav_profile_image);
            name = getActivity().findViewById(R.id.nav_usedname);
            navigationView = getActivity().findViewById(R.id.nav_view);
        }
        mystatustextview = view.findViewById(R.id.statustext);
        image_profile = view.findViewById(R.id.profile_image);
        editText_update_name = view.findViewById(R.id.editText_update_name);
        editText_update_surname = view.findViewById(R.id.editText_update_surname);
        RadioGroupUpdate = view.findViewById(R.id.radioTypeChoice);
        storageReference = FirebaseStorage.getInstance().getReference("uploads");
        if (current_user != null) {
            if (current_user.getImageURL().equals("default")) {
                image_profile.setImageResource(R.mipmap.ic_launcher);
            } else {
                Glide.with(getContext()).load(current_user.getImageURL()).into(image_profile);
            }
            if (current_user.getType().equals("Doctor")) {
                mystatustextview.setText("Status: " + current_user.getApproved());
            }else {
                mystatustextview.setVisibility(View.GONE);
                mystatustextview.setVisibility(View.INVISIBLE);
            }
        }
        image_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImage();

            }
        });
        Button button_submit = view.findViewById(R.id.button_submit);
        button_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validateForm()) {
                    return;
                } else {

                    document = FirebaseFirestore.getInstance().collection("User").document(Fuser.getUid());
                    current_user.setName(editText_update_name.getText().toString());
                    current_user.setSurname(editText_update_surname.getText().toString());
                    current_user.setSearch(editText_update_name.getText().toString().toLowerCase());
                    if (RadioGroupUpdate.getCheckedRadioButtonId() == R.id.ChoicePatient) {
                        current_user.setType("Patient");
                    } else if (RadioGroupUpdate.getCheckedRadioButtonId() == R.id.ChoiceDoctor) {
                        current_user.setType("Doctor");
                    }
                    document.set(current_user);
                    name.setText(current_user.getName());
                    hideItems();
                    Toast.makeText(getActivity(), "User information updated", Toast.LENGTH_SHORT).show();

                }

            }
        });

        return view;
    }

    private void modify_header_image() {
        if (current_user != null && prof_image != null) {
            if (!current_user.getImageURL().equals("default")) {
                Glide.with(this).load(current_user.getImageURL()).into(prof_image);

            }

        }
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = editText_update_name.getText().toString();
        if (TextUtils.isEmpty(email)) {
            editText_update_name.setError("Required.");
            valid = false;
        } else {
            editText_update_name.setError(null);
        }


        String name = editText_update_surname.getText().toString();
        if (TextUtils.isEmpty(name)) {
            editText_update_surname.setError("Required.");
            valid = false;
        } else {
            editText_update_surname.setError(null);
        }


        if (!valid) {
            Toast.makeText(getActivity(), "Please fill the information correctly", Toast.LENGTH_SHORT).show();
            clear_editTexts();
        } else {

            final int radioclicked = RadioGroupUpdate.getCheckedRadioButtonId();
            if (radioclicked == -1) {
                Toast.makeText(getActivity(), "Please choose one of the type of clients", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        return valid;
    }

    private void clear_editTexts() {
        editText_update_name.setText("");
        editText_update_surname.setText("");

    }

//    for image upload ******************************************************************************

    private void openImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, Image_request);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage() {
        final ProgressDialog pd = new ProgressDialog(getContext());
        pd.setMessage("Uploading");
        pd.show();

        if (ImageUri != null) {
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                    + "." + getFileExtension(ImageUri));

            uploadTask = fileReference.putFile(ImageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        assert downloadUri != null;
                        String mUri = downloadUri.toString();
                        document = FirebaseFirestore.getInstance().collection("User").document(Fuser.getUid());
                        // database = FirebaseDatabase.getInstance().getReference("User").child(Fuser.getUid());
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("imageURL", mUri);
                        document.update(map);
                        //database.updateChildren(map);

                        pd.dismiss();

                    } else {
                        Toast.makeText(getContext(), "Failed", Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            });
        } else {
            Toast.makeText(getContext(), "No Image Selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void hideItems() {
        String TAG = "Profile Fragment:";
        Log.d(TAG, "Hide items");
        if (current_user != null) {
            session.updateSession(current_user.getType(), current_user.getApproved());
            Log.d(TAG, "this is the session " + session.getUserDetails().get(KEY_TYPE) +"the approved is "+ session.getUserDetails().get(KEY_APPROVED) );
            if (session.getUserDetails().get(KEY_TYPE).equals("Doctor")&& session.getUserDetails().get(KEY_APPROVED).equals("approved")) {
                Log.d(TAG, "IT WENT INSIDEEEEEEE doctor 1");
                navigationView.getMenu().findItem(R.id.nav_scan_ble).setVisible(false);
                navigationView.getMenu().findItem(R.id.nav_searchdoctors).setVisible(false);
                navigationView.getMenu().findItem(R.id.nav_profileedit).setVisible(true);
                navigationView.getMenu().findItem(R.id.nav_chat).setVisible(true);
                navigationView.getMenu().findItem(R.id.nav_doctorApprove).setVisible(false);
                navigationView.getMenu().findItem(R.id.nav_makerequest).setVisible(false);
                navigationView.getMenu().findItem(R.id.nav_myrequests).setVisible(false);
                navigationView.getMenu().findItem(R.id.nav_ApproveRequests).setVisible(true);
                navigationView.getMenu().findItem(R.id.nav_myPatients).setVisible(true);
                navigationView.getMenu().findItem(R.id.nav_MyDoctors).setVisible(false);
                navigationView.getMenu().findItem(R.id.nav_MyData).setVisible(false);
                navigationView.getMenu().findItem(R.id.nav_logout).setVisible(true);
            } else if  (session.getUserDetails().get(KEY_TYPE).equals("Doctor") && (!session.getUserDetails().get(KEY_APPROVED).equals("approved"))) {
                Log.d(TAG, "IT WENT INSIDEEEEEEE doctor 1");
                navigationView.getMenu().findItem(R.id.nav_scan_ble).setVisible(false);
                navigationView.getMenu().findItem(R.id.nav_searchdoctors).setVisible(false);
                navigationView.getMenu().findItem(R.id.nav_profileedit).setVisible(true);
                navigationView.getMenu().findItem(R.id.nav_chat).setVisible(false);
                navigationView.getMenu().findItem(R.id.nav_doctorApprove).setVisible(false);
                navigationView.getMenu().findItem(R.id.nav_makerequest).setVisible(false);
                navigationView.getMenu().findItem(R.id.nav_myrequests).setVisible(false);
                navigationView.getMenu().findItem(R.id.nav_ApproveRequests).setVisible(false);
                navigationView.getMenu().findItem(R.id.nav_myPatients).setVisible(false);
                navigationView.getMenu().findItem(R.id.nav_MyDoctors).setVisible(false);
                navigationView.getMenu().findItem(R.id.nav_MyData).setVisible(false);
                navigationView.getMenu().findItem(R.id.nav_logout).setVisible(true);

            }
            if (current_user.getType().equals("Patient")) {
                Log.d(TAG, "IT WENT INSIDEEEEEEE patient 1");
                navigationView.getMenu().findItem(R.id.nav_scan_ble).setVisible(true);
                navigationView.getMenu().findItem(R.id.nav_searchdoctors).setVisible(true);
                navigationView.getMenu().findItem(R.id.nav_profileedit).setVisible(true);
                navigationView.getMenu().findItem(R.id.nav_chat).setVisible(true);
                navigationView.getMenu().findItem(R.id.nav_doctorApprove).setVisible(false);
                navigationView.getMenu().findItem(R.id.nav_makerequest).setVisible(true);
                navigationView.getMenu().findItem(R.id.nav_myrequests).setVisible(true);
                navigationView.getMenu().findItem(R.id.nav_ApproveRequests).setVisible(false);
                navigationView.getMenu().findItem(R.id.nav_myPatients).setVisible(false);
                navigationView.getMenu().findItem(R.id.nav_MyDoctors).setVisible(true);
                navigationView.getMenu().findItem(R.id.nav_MyData).setVisible(true);
                navigationView.getMenu().findItem(R.id.nav_logout).setVisible(true);
            }
            if (session.getUserDetails().get(KEY_TYPE).equals("Administrator")) {
                Log.d(TAG, "IT WENT INSIDEEEEEEE Administrator 1");
                navigationView.getMenu().findItem(R.id.nav_scan_ble).setVisible(false);
                navigationView.getMenu().findItem(R.id.nav_searchdoctors).setVisible(false);
                navigationView.getMenu().findItem(R.id.nav_profileedit).setVisible(false);
                navigationView.getMenu().findItem(R.id.nav_chat).setVisible(false);
                navigationView.getMenu().findItem(R.id.nav_doctorApprove).setVisible(true);
                navigationView.getMenu().findItem(R.id.nav_makerequest).setVisible(false);
                navigationView.getMenu().findItem(R.id.nav_myrequests).setVisible(false);
                navigationView.getMenu().findItem(R.id.nav_ApproveRequests).setVisible(false);
                navigationView.getMenu().findItem(R.id.nav_myPatients).setVisible(false);
                navigationView.getMenu().findItem(R.id.nav_MyDoctors).setVisible(false);
                navigationView.getMenu().findItem(R.id.nav_MyData).setVisible(false);
                navigationView.getMenu().findItem(R.id.nav_logout).setVisible(true);
            }
        } else {
            if (session.isUserInfoFull()) {
                if (session.getUserDetails().get(KEY_TYPE).equals("Doctor")&& session.getUserDetails().get(KEY_APPROVED).equals("approved")) {
                    Log.d(TAG, "IT WENT INSIDEEEEEEE doctor 2");
                    navigationView.getMenu().findItem(R.id.nav_scan_ble).setVisible(false);
                    navigationView.getMenu().findItem(R.id.nav_searchdoctors).setVisible(false);
                    navigationView.getMenu().findItem(R.id.nav_profileedit).setVisible(true);
                    navigationView.getMenu().findItem(R.id.nav_chat).setVisible(true);
                    navigationView.getMenu().findItem(R.id.nav_doctorApprove).setVisible(false);
                    navigationView.getMenu().findItem(R.id.nav_makerequest).setVisible(false);
                    navigationView.getMenu().findItem(R.id.nav_myrequests).setVisible(false);
                    navigationView.getMenu().findItem(R.id.nav_ApproveRequests).setVisible(true);
                    navigationView.getMenu().findItem(R.id.nav_myPatients).setVisible(true);
                    navigationView.getMenu().findItem(R.id.nav_MyDoctors).setVisible(false);
                    navigationView.getMenu().findItem(R.id.nav_MyData).setVisible(false);
                    navigationView.getMenu().findItem(R.id.nav_logout).setVisible(true);
                } else if  (session.getUserDetails().get(KEY_TYPE).equals("Doctor") && (!session.getUserDetails().get(KEY_APPROVED).equals("approved"))) {
                    Log.d(TAG, "IT WENT INSIDEEEEEEE doctor 1");
                    navigationView.getMenu().findItem(R.id.nav_scan_ble).setVisible(false);
                    navigationView.getMenu().findItem(R.id.nav_searchdoctors).setVisible(false);
                    navigationView.getMenu().findItem(R.id.nav_profileedit).setVisible(true);
                    navigationView.getMenu().findItem(R.id.nav_chat).setVisible(false);
                    navigationView.getMenu().findItem(R.id.nav_doctorApprove).setVisible(false);
                    navigationView.getMenu().findItem(R.id.nav_makerequest).setVisible(false);
                    navigationView.getMenu().findItem(R.id.nav_myrequests).setVisible(false);
                    navigationView.getMenu().findItem(R.id.nav_ApproveRequests).setVisible(false);
                    navigationView.getMenu().findItem(R.id.nav_myPatients).setVisible(false);
                    navigationView.getMenu().findItem(R.id.nav_MyDoctors).setVisible(false);
                    navigationView.getMenu().findItem(R.id.nav_MyData).setVisible(false);
                    navigationView.getMenu().findItem(R.id.nav_logout).setVisible(true);

                }
                if (session.getUserDetails().get(KEY_TYPE).equals("Patient")) {
                    Log.d(TAG, "IT WENT INSIDEEEEEEE patient 2");
                    navigationView.getMenu().findItem(R.id.nav_scan_ble).setVisible(true);
                    navigationView.getMenu().findItem(R.id.nav_searchdoctors).setVisible(true);
                    navigationView.getMenu().findItem(R.id.nav_profileedit).setVisible(true);
                    navigationView.getMenu().findItem(R.id.nav_chat).setVisible(true);
                    navigationView.getMenu().findItem(R.id.nav_doctorApprove).setVisible(false);
                    navigationView.getMenu().findItem(R.id.nav_makerequest).setVisible(true);
                    navigationView.getMenu().findItem(R.id.nav_myrequests).setVisible(true);
                    navigationView.getMenu().findItem(R.id.nav_ApproveRequests).setVisible(false);
                    navigationView.getMenu().findItem(R.id.nav_myPatients).setVisible(false);
                    navigationView.getMenu().findItem(R.id.nav_MyDoctors).setVisible(true);
                    navigationView.getMenu().findItem(R.id.nav_MyData).setVisible(true);
                    navigationView.getMenu().findItem(R.id.nav_logout).setVisible(true);
                }
                if (session.getUserDetails().get(KEY_TYPE).equals("Administrator")) {
                    Log.d(TAG, "IT WENT INSIDEEEEEEE Administrator 2");
                    navigationView.getMenu().findItem(R.id.nav_scan_ble).setVisible(false);
                    navigationView.getMenu().findItem(R.id.nav_searchdoctors).setVisible(false);
                    navigationView.getMenu().findItem(R.id.nav_profileedit).setVisible(false);
                    navigationView.getMenu().findItem(R.id.nav_chat).setVisible(false);
                    navigationView.getMenu().findItem(R.id.nav_doctorApprove).setVisible(true);
                    navigationView.getMenu().findItem(R.id.nav_makerequest).setVisible(false);
                    navigationView.getMenu().findItem(R.id.nav_myrequests).setVisible(false);
                    navigationView.getMenu().findItem(R.id.nav_ApproveRequests).setVisible(false);
                    navigationView.getMenu().findItem(R.id.nav_myPatients).setVisible(false);
                    navigationView.getMenu().findItem(R.id.nav_MyDoctors).setVisible(false);
                    navigationView.getMenu().findItem(R.id.nav_MyData).setVisible(false);
                    navigationView.getMenu().findItem(R.id.nav_logout).setVisible(true);
                }
            }


        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Image_request && resultCode == RESULT_OK && data != null && data.getData() != null) {
            ImageUri = data.getData();

            if (uploadTask != null && uploadTask.isInProgress()) {
                Toast.makeText(getContext(), "Upload in Progress", Toast.LENGTH_SHORT).show();
            } else {
                uploadImage();

            }
        }
    }


    private void initialise_user() {
        document = FirebaseFirestore.getInstance().collection("User").document(Fuser.getUid());
        document.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                 document.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        assert documentSnapshot != null;
                        current_user = documentSnapshot.toObject(UserModel.class);
                        if (current_user != null) {

                            if (current_user.getImageURL().equals("default")&& image_profile!= null) {
                                image_profile.setImageResource(R.mipmap.ic_launcher);

                            } else {
                                if (current_user != null && image_profile != null && getContext() != null) {
                                    Glide.with(getContext()).load(current_user.getImageURL()).into(image_profile);
                                    modify_header_image();


                                }
                            }
                            assert current_user != null;
                            if (current_user.getType().equals("Doctor")) {
                                mystatustextview.setVisibility(View.VISIBLE);
                                mystatustextview.setText("Status: "+current_user.getApproved());
                            }else {
                                mystatustextview.setVisibility(View.GONE);
                                mystatustextview.setVisibility(View.INVISIBLE);
                            }
                        }


                    }


                });
            }
        });

    }
}
