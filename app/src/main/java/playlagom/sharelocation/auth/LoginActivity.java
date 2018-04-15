package playlagom.sharelocation.auth;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import playlagom.sharelocation.DisplayActivity;
import playlagom.sharelocation.MainActivity;
import playlagom.sharelocation.R;
import playlagom.sharelocation.models.User;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "LoginActivity";
    Button btnSignIn;
    EditText etLoginEmail, etLoginPassword;
    TextView tvSignUp;

    ProgressDialog progressDialog;
    FirebaseAuth firebaseAuth;

    private int counter = 1;
    public static boolean isNameProvided = false;
    DatabaseReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_login);

        // check internet connection
        if (!isInternetOn()) {
            Toast.makeText(this, "Please ON your internet", Toast.LENGTH_LONG).show();
            finish();
        }
        Log.d(TAG, "----onCreate: " + isInternetOn());

        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            startActivity(new Intent(this, DisplayActivity.class));
            finish();
        }
        // WHEN user not logged in THEN check if user's name is provided or not
        ref = FirebaseDatabase.getInstance().getReference();

        progressDialog = new ProgressDialog(this);

        etLoginEmail = findViewById(R.id.etLoginEmail);
        etLoginPassword = findViewById(R.id.etLoginPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        tvSignUp = findViewById(R.id.tvSignUp);

        btnSignIn.setOnClickListener(this);
        tvSignUp.setOnClickListener(this);
    }

    // Paste this on activity from where you need to check internet status
    public boolean isInternetOn() {
        // get Connectivity Manager object to check connection
        ConnectivityManager connec =
                (ConnectivityManager) getSystemService(getBaseContext().CONNECTIVITY_SERVICE);

        // Check for network connections
        if (connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTED ||
                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED) {
            return true;
        } else if (
                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.DISCONNECTED ||
                        connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.DISCONNECTED) {
            return false;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        if (v == btnSignIn) {
            loginUser();
        }
        if (v == tvSignUp) {
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            finish();
        }
    }

    private void loginUser() {
        // register user
        String email = etLoginEmail.getText().toString().trim();
        String password = etLoginPassword.getText().toString().trim();

        // Form validation part: Start
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_LONG).show();
            etLoginEmail.setError("Email is required");
            etLoginEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etLoginEmail.setError("Please enter a valid Email");
            etLoginEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter your password", Toast.LENGTH_LONG).show();
            etLoginPassword.setError("Password is required");
            etLoginPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "Invalid password", Toast.LENGTH_LONG).show();
            return;
        } // Form validation part: End

        progressDialog.setMessage("Login...");
        progressDialog.show();
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Login successful and move to next page

                    // TODO: 4/15/2018      CODE is ready to CHANGE
                    // CHECK isNameProvided
                    Log.d(TAG, "BEFORE CALLING...DEBUGGER----- isNameProvided = " + isNameProvided);
                    isNameProvided(firebaseAuth.getCurrentUser().getUid());
                    Log.d(TAG, "AFTER CALLING...DEBUGGER----- isNameProvided = " + isNameProvided);

                    // TODO: 4/15/2018      NO need THREAD
//                    final Thread thread2 = new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            // TODO: 4/15/2018      WAITING when db async response event done
//                            while (isNameProvided){
//                                finish();
//                                startActivity(new Intent(LoginActivity.this, DisplayActivity.class));
//                                Toast.makeText(LoginActivity.this, "Login Successfully", Toast.LENGTH_SHORT).show();
//                                progressDialog.dismiss();
//                                isNameProvided = false;
//                                try {
//                                    Log.d(TAG, "onComplete: DEBUGGER----- Waiting...");
////                                Log.d("MainActivity", "=== try ====");
//                                    Thread.sleep(100);
//                                } catch (InterruptedException e) {
//                                    e.printStackTrace();
//                                } finally {
////                                Log.d("MainActivity", "=== finally ====");
////                                Intent intent = new Intent(LoginActivity.this, DisplayActivity.class);
////                                startActivity(intent);
////                                finish();
//                                }
//                            }
//
//
//                        }
//                    });
//                    thread2.start();
                } else {
                    progressDialog.dismiss();
                    if (counter > 3) {
                        finish();
                        Toast.makeText(LoginActivity.this, "Could not login. Try again later", Toast.LENGTH_LONG).show();
                    }
                    if (counter == 3) {
                        Toast.makeText(LoginActivity.this, "Invalid email or password.  Last chance", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "Invalid email or password.  " + (3 - counter) + " chance left", Toast.LENGTH_LONG).show();
                    }
                    counter++;
                }
            }

            // anonymous method: isNameProvided
            private void isNameProvided(String currentUser) {

                // TODO: 4/15/2018 WORKING
//                try{
//                    Log.d(TAG, "onDataChange: ------DEBUGGER------ USERID: " + currentUser);
//                } catch (Exception e) {
//                    Log.d(TAG, "onDataChange: DEBUGGER, EXCEPTION");
//                }

//                ref.child("users").child("" + currentUser).addListenerForSingleValueEvent(new ValueEventListener() {
                ref.child("users").child("" + currentUser)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);

                        if (dataSnapshot.getChildrenCount() == 2) {

                            finish();
                            startActivity(new Intent(LoginActivity.this, DisplayActivity.class));
                            Toast.makeText(LoginActivity.this, "Login Successfully", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();

                            isNameProvided = true;
                            Log.d(TAG, "----------DEBUGGER-----isNameProvided = " + isNameProvided);
//                            Toast.makeText(getApplicationContext(), "2 child", Toast.LENGTH_LONG).show();
                            try {
                                // TODO: 4/15/2018 DEBUGGER: 2
                                Log.d(TAG, "onDataChange: DEBUGGER-----INSIDE isNameProvided ------KEY: " + dataSnapshot.getKey() + ", " + dataSnapshot.getChildrenCount() + ", NAME: " + user.getName());
                            } catch (Exception e) {
                                Log.d(TAG, "onDataChange: DEBUGGER-----INSIDE isNameProvided: childCount 2: EXCEPTION");
                            }
                        } else if (dataSnapshot.getChildrenCount() == 3) {
                            isNameProvided = true;
                            Log.d(TAG, "----------DEBUGGER-----isNameProvided = " + isNameProvided);
                            try {
                                // TODO: 4/15/2018 DEBUGGER: 1
                                Log.d(TAG, "onDataChange: DEBUGGER-----INSIDE isNameProvided ------KEY: " + dataSnapshot.getKey() + ", " + dataSnapshot.getChildrenCount() + ", NAME: " + user.getName());
                            } catch (Exception e) {
                                Log.d(TAG, "onDataChange: DEBUGGER-----INSIDE isNameProvided: EXCEPTION");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                Log.d(TAG, "isNameProvided: DEBUGGER----- isNameProvided: " + isNameProvided);
            }
        });
    }
}
