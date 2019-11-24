package com.example.myapplication.Activities;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.myapplication.Model.UserModel;
import com.example.myapplication.Model.UserSessionManager;
import com.example.myapplication.R;
import com.example.myapplication.Services.MyFirebaseInstanceIDService;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.Collections;

import static com.example.myapplication.R.layout.activity_login;


public class LoginActivity extends BaseActivity implements View.OnClickListener {

    private EditText editText_username, editText_password;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseuser;
    TextView forgot_password;
    private UserSessionManager session;
    private Intent intent;
    private CollectionReference collection;
    private CallbackManager callbackManager;

    private DocumentReference document;
    public String TAG = "Something";
    private static final int RC_SIGN_IN = 9001;

    private GoogleSignInClient mGoogleSignInClient;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_login);

        // User Session Manager
        session = new UserSessionManager(getApplicationContext());

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        firebaseuser=mAuth.getCurrentUser();
        SignInButton googlesign = findViewById(R.id.googlesign_in_button);
        LoginButton facebooksign = findViewById(R.id.facebook_login_button);

        editText_username =findViewById(R.id.username);
        editText_password =findViewById(R.id.password);
        //signup button
        findViewById(R.id.signin).setOnClickListener(this);
        findViewById(R.id.signupredirect).setOnClickListener(this);
        forgot_password = findViewById(R.id.forgot_password);

        forgot_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
            }
        });

        //Google Sign In
        googlesign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                google_sign_in();
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        //Facebook Sign in
        callbackManager = CallbackManager.Factory.create();
        facebooksign.setPermissions("email", "public_profile");
        facebooksign.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                FacebookAccessToken(loginResult.getAccessToken());

            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });

    }

    @Override
    public void onStart(){
        super.onStart();
        if(session.isUserLoggedIn()){
            updateUI(firebaseuser);}

    }


    private void google_sign_in() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                assert account != null;
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // [START_EXCLUDE]
                updateUI(null);
                // [END_EXCLUDE]
            }
        }else//Facebook Sign in
            callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            final FirebaseUser user = mAuth.getCurrentUser();
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("Google", "true");
                            bundle.putString("Facebook", "false");
                            intent.putExtras(bundle);


                            document = FirebaseFirestore.getInstance().collection("User").document(user.getUid());
                            document.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.getResult().getData() == null) {
                                        document.set(new UserModel(user.getUid(), user.getEmail(), user.getDisplayName(), user.getDisplayName(), "Patient", "False", "default", "offline", user.getDisplayName().toLowerCase(), (Collections.<String>emptyList())));
                                        Log.d(TAG, "First time access");
                                        firebaseuser = mAuth.getCurrentUser();
                                        assert firebaseuser != null;
                                        session.createUserLoginSession(firebaseuser.getDisplayName(), firebaseuser.getEmail(),"False");
                                        updateUI(firebaseuser);
                                    } else {
                                        Log.d(TAG, "Google User has accessed before");
                                        firebaseuser = mAuth.getCurrentUser();
                                        assert firebaseuser != null;
                                        document = FirebaseFirestore.getInstance().collection("User").document(user.getUid());
                                        document.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                UserModel userModel = task.getResult().toObject(UserModel.class);
                                                Log.d(TAG,"the id that we got is  " + user.getUid());
                                                assert userModel != null;
                                                session.createUserLoginSession(firebaseuser.getDisplayName(), firebaseuser.getEmail(), userModel.getApproved()  );
                                                updateUI(firebaseuser);
                                                Log.d(TAG,"display name " + firebaseuser.getDisplayName()+"email :" + firebaseuser.getEmail() );
                                            }
                                        });

                                    }

                                }

                            });


                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(getApplicationContext(),"Google Authentication failed",Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        hideProgressDialog();

                    }
                });
    }
//facebook void

    private void FacebookAccessToken(final AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        showProgressDialog();
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            final FirebaseUser user = mAuth.getCurrentUser();
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("Google", "false");
                            bundle.putString("Facebook", "true");
                            intent.putExtras(bundle);



                            document = FirebaseFirestore.getInstance().collection("User").document(user.getUid());
                            document.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.getResult().getData() == null) {
                                        document.set(new UserModel(user.getUid(), user.getEmail(), user.getDisplayName(), user.getDisplayName(), "Patient", "False", "default", "offline", user.getDisplayName().toLowerCase(), (Collections.<String>emptyList())));
                                        Log.d(TAG, "First time access");
                                        firebaseuser = mAuth.getCurrentUser();
                                        assert firebaseuser != null;
                                        session.createUserLoginSession(firebaseuser.getDisplayName(), firebaseuser.getEmail(),"False");
                                        updateUI(firebaseuser);
                                    } else {
                                        Log.d(TAG, "Google User has accessed before");
                                        firebaseuser = mAuth.getCurrentUser();
                                        assert firebaseuser != null;
                                        document = FirebaseFirestore.getInstance().collection("User").document(firebaseuser.getUid());
                                        document.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                UserModel userModel = task.getResult().toObject(UserModel.class);
                                                Log.d(TAG,"the id that we got is  " + user.getUid());
                                                assert userModel != null;
                                                session.createUserLoginSession(firebaseuser.getDisplayName(), firebaseuser.getEmail(), userModel.getApproved()  );
                                                updateUI(firebaseuser);
                                                Log.d(TAG,"display name " + firebaseuser.getDisplayName()+"email :" + firebaseuser.getEmail() );
                                            }
                                        });

                                    }

                                }

                            });


                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(getApplicationContext(),"Google Authentication failed",Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        hideProgressDialog();

                    }
                });
    }


    public boolean validateEntry(String username,String password){
        boolean valid = true;
        if (TextUtils.isEmpty(username)) {
            editText_username.setError("Required.");
            valid = false;
        }
        if (TextUtils.isEmpty(password)) {
            editText_password.setError("Required.");
            valid = false;
        }
        return valid;
    }
    public void updateUI(FirebaseUser user){
        if(user!=null) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }


    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.signin) {
            // Get username, password from EditText
            final String username = editText_username.getText().toString();
            String password = editText_password.getText().toString();
            // Validate if username, password is filled
            if (validateEntry(username, password)) {
                showProgressDialog();
                mAuth.signInWithEmailAndPassword(username, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull final Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    firebaseuser = mAuth.getCurrentUser();
                                    assert firebaseuser != null;
                                    document = FirebaseFirestore.getInstance().collection("User").document(firebaseuser.getUid());
                                    document.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    UserModel userModel = task.getResult().toObject(UserModel.class);
                                    Log.d(TAG, "the id that we got is  " + firebaseuser.getUid());
                                    assert userModel != null;
                                    session.createUserLoginSession(firebaseuser.getDisplayName(), firebaseuser.getEmail(), userModel.getApproved());
                                    updateUI(firebaseuser);

                                        }
                                    });




                                        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                                if (!task.isSuccessful()) {
                                                    Log.w(TAG, "getInstanceId failed", task.getException());
                                                    return;
                                                }

                                                String refreshToken = (task.getResult()).getToken();
                                                MyFirebaseInstanceIDService.Companion.updateToken(refreshToken);

                                            }
                                        });

                                } else {
                                    Toast.makeText(LoginActivity.this, "Authentication failure: Either username or password is wrong",
                                            Toast.LENGTH_SHORT).show();
                                    updateUI(null);
                                }
                                hideProgressDialog();
                            }
                        });
            } else {
                Toast.makeText(this, "Please fill both username and password fields", Toast.LENGTH_SHORT).show();

            }
        }
        if (i == R.id.signupredirect) {
            Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
            startActivity(intent);
            finish();
        }

    }
}
