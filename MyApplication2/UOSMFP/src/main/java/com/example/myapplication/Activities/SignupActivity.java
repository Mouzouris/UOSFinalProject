package com.example.myapplication.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.myapplication.Model.UserModel;
import com.example.myapplication.Model.UserSessionManager;
import com.example.myapplication.R;
import com.example.myapplication.Services.MyFirebaseInstanceIDService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.Collections;

public class SignupActivity extends BaseActivity {

    private EditText usernameEditText, nameEditText, surnameEditText, passwordEditText;
    // User Session Manager Class
    private UserSessionManager session;
    private FirebaseAuth mAuth;
    private RadioGroup TypeRadioGroup;
    private static final String TAG = "SignUp Activity";
    private TextView statusTextView;
    private ProgressDialog mProgressDialog;
    private FirebaseFirestore database;
    //private DatabaseReference database;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // User Session Manager
        session = new UserSessionManager(getApplicationContext());

        // Initialize Firebase Auth and Database
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
        //database = FirebaseDatabase.getInstance().getReference();

        //Views
        statusTextView = findViewById(R.id.status);
        usernameEditText = findViewById(R.id.username);
        nameEditText = findViewById(R.id.name);
        surnameEditText = findViewById(R.id.surname);
        passwordEditText = findViewById(R.id.password);

        //Buttons

        //Choice
        TypeRadioGroup = findViewById(R.id.radioTypeChoice);
    }

    private void createAccount(String email, String password, String name, String surname, final RadioGroup typeRadioGroup) {
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm()) {
            return;
        }
        showProgressDialog();
        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            UserModel current_user;
                            String type;
                            if (typeRadioGroup.getCheckedRadioButtonId() == R.id.ChoicePatient) {
                                type = "Patient";
                                current_user = new UserModel(user.getUid(), usernameEditText.getText().toString(), nameEditText.getText().toString(), surnameEditText.getText().toString(), type, "False", "default","offline",nameEditText.getText().toString().toLowerCase(), (Collections.<String>emptyList()));
                            } else {
                                type = "Doctor";
                                current_user = new UserModel(user.getUid(), usernameEditText.getText().toString(), nameEditText.getText().toString(), surnameEditText.getText().toString(), type, "False", "default", "offline",nameEditText.getText().toString().toLowerCase(),(Collections.<String>emptyList()));
                            }
                            FirebaseFirestore.getInstance().collection("User").document(user.getUid()).set(current_user);
                            session.createUserLoginSession(current_user.getName(),current_user.getUsername(),"False");
                            updateUI(user);
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
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignupActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // [START_EXCLUDE]
                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
        // [END create_user_with_email]
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = usernameEditText.getText().toString();
        if (TextUtils.isEmpty(email)) {
            usernameEditText.setError("Required.");
            valid = false;
        } else {
            usernameEditText.setError(null);
        }

        String password = passwordEditText.getText().toString();
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Required.");
            valid = false;
        } else {
            passwordEditText.setError(null);
        }

        String name = nameEditText.getText().toString();
        if (TextUtils.isEmpty(name)) {
            nameEditText.setError("Required.");
            valid = false;
        } else {
            nameEditText.setError(null);
        }

        String surname = surnameEditText.getText().toString();
        if (TextUtils.isEmpty(surname)) {
            surnameEditText.setError("Required.");
            valid = false;
        } else {
            surnameEditText.setError(null);
        }

        if (!valid) {
            Toast.makeText(getApplicationContext(), "Please fill the information correctly", Toast.LENGTH_SHORT).show();
            clear_editTexts();
        } else if (password.length() < 6) {
            Toast.makeText(getApplicationContext(), "There must be at least 6 characters on the password", Toast.LENGTH_SHORT).show();
            clear_editTexts();
            return false;
        } else {

                final int radioclicked = TypeRadioGroup.getCheckedRadioButtonId();
                if  (radioclicked == -1)
                {
                    Toast.makeText(getApplicationContext(), "Please choose one of the type of clients", Toast.LENGTH_SHORT).show();
                    return false; }}

        return valid;
    }

    private void updateUI(FirebaseUser user) {
        hideProgressDialog();
        if (user != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();

        }

    }

    public void clear_editTexts() {
        nameEditText.setText("");
        surnameEditText.setText("");
        usernameEditText.setText("");
        passwordEditText.setText("");
    }

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.signup) {
            createAccount(usernameEditText.getText().toString(), passwordEditText.getText().toString(), nameEditText.getText().toString(), surnameEditText.getText().toString(), TypeRadioGroup);

        } else if (i == R.id.BackButton) {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
}

