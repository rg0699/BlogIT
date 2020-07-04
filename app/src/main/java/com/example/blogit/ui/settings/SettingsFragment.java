package com.example.blogit.ui.settings;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.blogit.R;
import com.example.blogit.ui.activities.LoginActivity;
import com.example.blogit.ui.activities.MainActivity;
import com.example.blogit.ui.activities.SignUpActivity;
import com.example.blogit.ui.profile.ProfileFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;

public class SettingsFragment extends Fragment {

    private SettingsViewModel settingsViewModel;
    private Button btnChangeEmail, btnChangePassword, btnSendResetEmail, btnRemoveUser,
            changeEmail, changePassword, sendEmail, remove, signOut;

    private EditText oldEmail, newEmail, password, newPassword;
    private ProgressBar progressBar;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    private boolean x=false;
    private DatabaseReference mDatabase;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        settingsViewModel =
                ViewModelProviders.of(this).get(SettingsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        auth = FirebaseAuth.getInstance();

        //get current user
        final FirebaseUser user = auth.getCurrentUser();

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                    //finish();
                }
                else {
                    sendEmailVerificationWithContinueUrl();
                }
            }
        };

        mDatabase= FirebaseDatabase.getInstance().getReference().child("users");

        btnChangeEmail = (Button) root.findViewById(R.id.change_email_button);
        //btnChangePassword = (Button) root.findViewById(R.id.change_password_button);
        btnSendResetEmail = (Button) root.findViewById(R.id.sending_pass_reset_button);
        btnRemoveUser = (Button) root.findViewById(R.id.remove_user_button);
        changeEmail = (Button) root.findViewById(R.id.changeEmail);
        //changePassword = (Button) root.findViewById(R.id.changePass);
        sendEmail = (Button) root.findViewById(R.id.send);
        remove = (Button) root.findViewById(R.id.remove);
        //signOut = (Button) root.findViewById(R.id.sign_out);

        oldEmail = (EditText) root.findViewById(R.id.old_email);
        newEmail = (EditText) root.findViewById(R.id.new_email);
        password = (EditText) root.findViewById(R.id.password);
        //newPassword = (EditText) root.findViewById(R.id.newPassword);

        oldEmail.setVisibility(View.GONE);
        newEmail.setVisibility(View.GONE);
        password.setVisibility(View.GONE);
        //newPassword.setVisibility(View.GONE);
        changeEmail.setVisibility(View.GONE);
        //changePassword.setVisibility(View.GONE);
        sendEmail.setVisibility(View.GONE);
        remove.setVisibility(View.GONE);

        progressBar = (ProgressBar) root.findViewById(R.id.progressBar);

        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        btnChangeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                oldEmail.setVisibility(View.GONE);
                newEmail.setVisibility(View.VISIBLE);
                password.setVisibility(View.GONE);
                //newPassword.setVisibility(View.GONE);
                changeEmail.setVisibility(View.VISIBLE);
                //changePassword.setVisibility(View.GONE);
                sendEmail.setVisibility(View.GONE);
                remove.setVisibility(View.GONE);
            }
        });

        changeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                if (user != null && !newEmail.getText().toString().trim().equals("")) {
                    user.updateEmail(newEmail.getText().toString().trim())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getContext(), "Email address is updated. Please sign in with new email id!", Toast.LENGTH_LONG).show();
                                        signOut();
                                        progressBar.setVisibility(View.GONE);
                                    } else {
                                        Toast.makeText(getContext(), "Failed to update email!", Toast.LENGTH_LONG).show();
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }
                            });
                } else if (newEmail.getText().toString().trim().equals("")) {
                    newEmail.setError("Enter email");
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

//        btnChangePassword.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                oldEmail.setVisibility(View.GONE);
//                newEmail.setVisibility(View.GONE);
//                password.setVisibility(View.GONE);
//                newPassword.setVisibility(View.VISIBLE);
//                changeEmail.setVisibility(View.GONE);
//                changePassword.setVisibility(View.VISIBLE);
//                sendEmail.setVisibility(View.GONE);
//                remove.setVisibility(View.GONE);
//            }
//        });
//
//        changePassword.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                progressBar.setVisibility(View.VISIBLE);
//                if (user != null && !newPassword.getText().toString().trim().equals("")) {
//                    if (newPassword.getText().toString().trim().length() < 6) {
//                        newPassword.setError("Password too short, enter minimum 6 characters");
//                        progressBar.setVisibility(View.GONE);
//                    } else {
//                        user.updatePassword(newPassword.getText().toString().trim())
//                                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<Void> task) {
//                                        if (task.isSuccessful()) {
//                                            Toast.makeText(getContext(), "Password is updated, sign in with new password!", Toast.LENGTH_SHORT).show();
//                                            signOut();
//                                            progressBar.setVisibility(View.GONE);
//                                        } else {
//                                            Toast.makeText(getContext(), "Failed to update password!", Toast.LENGTH_SHORT).show();
//                                            progressBar.setVisibility(View.GONE);
//                                        }
//                                    }
//                                });
//                    }
//                } else if (newPassword.getText().toString().trim().equals("")) {
//                    newPassword.setError("Enter password");
//                    progressBar.setVisibility(View.GONE);
//                }
//            }
//        });

        btnSendResetEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                oldEmail.setVisibility(View.VISIBLE);
                newEmail.setVisibility(View.GONE);
                password.setVisibility(View.GONE);
                //newPassword.setVisibility(View.GONE);
                changeEmail.setVisibility(View.GONE);
                //changePassword.setVisibility(View.GONE);
                sendEmail.setVisibility(View.VISIBLE);
                remove.setVisibility(View.GONE);
            }
        });

        sendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                if (!oldEmail.getText().toString().trim().equals("")) {
                    auth.sendPasswordResetEmail(oldEmail.getText().toString().trim())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getContext(), "Reset password email is sent!", Toast.LENGTH_SHORT).show();
                                        signOut();
                                        progressBar.setVisibility(View.GONE);
                                    } else {
                                        Toast.makeText(getContext(), "Failed to send reset email!", Toast.LENGTH_SHORT).show();
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }
                            });
                } else {
                    oldEmail.setError("Enter email");
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        btnRemoveUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //progressBar.setVisibility(View.VISIBLE);
                if (user != null) {
                    verifyPassword();
//                    user.delete()
//                            .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                @Override
//                                public void onComplete(@NonNull Task<Void> task) {
//                                    if (task.isSuccessful()) {
//                                        Toast.makeText(getContext(), "Your profile is deleted:( Create a account now!", Toast.LENGTH_SHORT).show();
//                                        startActivity(new Intent(getActivity(), SignUpActivity.class));
//                                        //finish();
//                                        progressBar.setVisibility(View.GONE);
//                                    }
//                                    else {
//                                        Toast.makeText(getContext(), "Failed to delete your account!", Toast.LENGTH_SHORT).show();
//                                        progressBar.setVisibility(View.GONE);
//                                    }
//                                }
//                            });
                }
            }
        });

//        signOut.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                signOut();
//            }
//        });


        return root;
    }

    private void verifyPassword(){
        final AlertDialog.Builder alert = new AlertDialog.Builder(requireActivity());
        alert.setTitle("Verify Your Password");
        alert.setCancelable(true);
        // Set an EditText view to get user input
        final EditText input = new EditText(requireActivity());
        alert.setView(input);
        input.setHint("Password");
        input.setTransformationMethod(PasswordTransformationMethod.getInstance());
        alert.setPositiveButton("Verify", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.cancel();
                progressBar.setVisibility(View.GONE);
            }
        });
        final AlertDialog alertDialog = alert.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = input.getText().toString();
                if(s.length()==0) {
                    input.setError("Enter Password");
                    return;
                }
                else {
                    progressBar.setVisibility(View.VISIBLE);
                    reauthenticate(s);
                }
                alertDialog.cancel();
            }
        });
    }

    private void reauthenticate(String s) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user!= null) {

            AuthCredential credential;
            credential = EmailAuthProvider
                    .getCredential(user.getEmail(), s);
            user.reauthenticate(credential)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                //deleteUserData(user.getUid());
                                deleteUser();
                            }
                            else {
                                Toast.makeText(getContext(), "Password Incorrect!", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                            }
                        }
                    });
        }
    }

    private void deleteUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId=user.getUid();
            user.delete()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                deleteUserData(userId);
                                Toast.makeText(getContext(), "Your profile is deleted:( Create a account now!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getActivity(), SignUpActivity.class));
                                //deleteUserData(userId);
                                //finish();
                                progressBar.setVisibility(View.GONE);
                            } else {
                                Toast.makeText(getContext(), "Failed to delete your account!", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                            }
                        }
                    });
        }
    }

    private void deleteUserData(String userId) {
        //mDatabase.child(userId).removeEventListener(ProfileFragment.listener);
        //deleteUser();
        mDatabase.child(userId).removeValue();

    }

    private void sendEmailVerificationWithContinueUrl() {
        // [START send_email_verification_with_continue_url]
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            String url;
            url = "http://www.example.com/verify?uid=" + user.getUid();
            ActionCodeSettings actionCodeSettings = ActionCodeSettings.newBuilder()
                    .setUrl(url)
                    // The default for this is populated with the current android package name.
                    .setAndroidPackageName("com.example.blogit", false, null)
                    .build();

            user.sendEmailVerification(actionCodeSettings)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                            }
                        }
                    });
        }

        // [END send_email_verification_with_continue_url]
        // [START localize_verification_email]
        auth.setLanguageCode("fr");
        // To apply the default app language instead of explicitly setting it.
        // auth.useAppLanguage();
        // [END localize_verification_email]
    }


    //sign out method
    private void signOut() {
        auth.signOut();
//        Toast.makeText(
//                getContext(),
//                "Logged Out",
//                Toast.LENGTH_SHORT)
//                .show();
        Intent loginActivity = new Intent(getActivity(),LoginActivity.class);
        startActivity(loginActivity);
        //finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();
        //auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }
}
