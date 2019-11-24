package com.example.myapplication.Activities;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.myapplication.Fragments.ApproveDoctor;
import com.example.myapplication.Fragments.ApproveRequests;
import com.example.myapplication.Fragments.InitialFragment;
import com.example.myapplication.Fragments.MakeRequests;
import com.example.myapplication.Fragments.MyData;
import com.example.myapplication.Fragments.MyDoctors;
import com.example.myapplication.Fragments.MyPatients;
import com.example.myapplication.Fragments.MyRequests;
import com.example.myapplication.Fragments.ProfileFragment;
import com.example.myapplication.Fragments.Scan_BLE;
import com.example.myapplication.Fragments.SearchFragment;
import com.example.myapplication.Model.UserModel;
import com.example.myapplication.Model.UserSessionManager;
import com.example.myapplication.R;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
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

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.myapplication.Model.UserSessionManager.KEY_APPROVED;
import static com.example.myapplication.Model.UserSessionManager.KEY_TYPE;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Fragment frag1, frag2, frag3, frag4, frag5 , frag6, frag7, frag8, frag9, frag10, frag11;
    private FirebaseUser user;
    private FirebaseAuth mAuth;

    private CollectionReference collection;
    private DocumentReference StoreUser;
    private DocumentReference document;
    private CircleImageView prof_image;
    NavigationView navigationView;
    private TextView username;
    private TextView name;
    UserSessionManager session;
    UserModel current_user;
    Intent intent;
    String TAG = "main act:";
    public String gsign;
    public  String fbsign;
    private boolean facebooksignedin = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_main, new InitialFragment())
                .commit();


        frag1 = frag2 = frag3 = frag4 = frag5 = frag6 = frag7 = frag8 =frag9 = frag10 = frag11 = null;
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        //database = FirebaseDatabase.getInstance().getReference();

        // Session class instance
        session = new UserSessionManager(getApplicationContext());

        initialise_user();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            gsign = extras.getString("Google");
            fbsign = extras.getString("Facebook");
            Log.d(TAG, gsign+" left googlr facebook: "+fbsign);
        }
        if (fbsign =="true"){
            facebooksignedin= true;
        }
        // Check user login (this is the important point)
        // If User is not logged in , This will redirect user to LoginActivity
        // and finish current activity from activity stack.
        if (session.checkLogin())
            finish();

    }

    public void onResume() {
        super.onResume();
        Toast.makeText(this, "User: " + user.getEmail() + " is signed in", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment = null;

        if (id == R.id.nav_scan_ble) {
            if (frag1 == null)
                frag1 = new Scan_BLE();
            fragment = frag1;
        } else if (id == R.id.nav_searchdoctors) {
            if (frag2 == null)
                frag2 = new SearchFragment();
            fragment = frag2;
        } else if (id == R.id.nav_profileedit) {
            if (frag3 == null)
                frag3 = new ProfileFragment();
            fragment = frag3;
        }  else if (id == R.id.nav_doctorApprove) {
            if (frag4 == null)
                frag4 = new ApproveDoctor();
            fragment = frag4;
        }   else if (id == R.id.nav_makerequest) {
            if (frag5 == null)
                frag5 = new MakeRequests();
            fragment = frag5;
        }else if (id == R.id.nav_ApproveRequests) {
            if (frag6 == null)
                frag6 = new ApproveRequests();
            fragment = frag6;
            }else if (id == R.id.nav_myrequests) {
            if (frag7 == null)
                frag7 = new MyRequests();
            fragment = frag7;
        }else if (id == R.id.nav_chat) {
            startActivity(new Intent(this, Chat.class));
        }
        else if (id == R.id.nav_myPatients) {
            if (frag8 == null)
                frag8 = new MyPatients();
            fragment = frag8;
        }else if (id == R.id.nav_MyDoctors) {
            if (frag9 == null)
                frag9 = new MyDoctors();
            fragment = frag9;
        }else if (id == R.id.nav_MyData) {
                if (frag10 == null)
                    frag10 = new MyData();
                fragment = frag10;
        } else if (id == R.id.nav_logout) {
            if (facebooksignedin = true){
                LoginManager.getInstance().logOut();
                session.logoutUser();
                mAuth.signOut();
                finish();
            }else
                session.logoutUser();
                mAuth.signOut();
                finish();


        }



        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_main, fragment);
            ft.addToBackStack(null);
            ft.commit();
        }


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.d(TAG, "On Prepare works");
        prof_image = findViewById(R.id.nav_profile_image);
        username = findViewById(R.id.nav_username);
        name = findViewById(R.id.nav_usedname);
        hideItems();
        modify_header_image();
        return super.onPrepareOptionsMenu(menu);
    }

    public void hideItems() {
        Log.d(TAG, "Hide items");
        if (current_user != null) {
            if (!session.isUserInfoFull())
                session.updateSession(current_user.getType(),current_user.getApproved());
            if (session.getUserDetails().get(KEY_TYPE).equals("Doctor") && session.getUserDetails().get(KEY_APPROVED).equals("approved")) {
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
            if (session.getUserDetails().get(KEY_TYPE).equals("Patient")) {
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

    public void initialise_user() {
        //database = FirebaseDatabase.getInstance().getReference().child("User").child(user.getUid());
            StoreUser = FirebaseFirestore.getInstance().collection("User").document(user.getUid());
            StoreUser.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    StoreUser.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot snapshot,
                                            @Nullable FirebaseFirestoreException e) {
                            assert snapshot != null;
                            assert  current_user != null;
                            current_user = snapshot.toObject(UserModel.class);
                            Log.w(TAG, "it made it to the object phase");
                            if (e != null) {
                                Log.w(TAG, "Listen failed.", e);
                                return;
                            }

                            if (current_user != null && snapshot.exists() && name != null && username != null) {
                                Log.d(TAG, "Current data: " + snapshot.getData());
                                name.setText(current_user.getName());
                                username.setText(current_user.getUsername());
                            } else {
                                Log.d(TAG, "Current data: null");
                            }
                            hideItems();
                            modify_header_image();
                        }

                    });

                }

            });

        }



    private void modify_header_image() {
        if (current_user != null && prof_image != null) {

            if (!current_user.getImageURL().equals("default")) {
                Glide.with(getApplicationContext()).load(current_user.getImageURL()).into(prof_image);
            }
            name.setText(current_user.getName());
            username.setText(current_user.getUsername());


        }

    }
}
