package com.example.lab6_20197115.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult;
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lab6_20197115.R;
import com.example.lab6_20197115.ui.tasks.MainActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

// Google Identity (One Tap)
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;

// Google Sign-In clásico
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;

// Firebase
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.FacebookAuthProvider;

// Facebook SDK
import com.facebook.CallbackManager;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private TextInputEditText inEmail, inPass;

    // --- Google One Tap ---
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;   // cuentas ya autorizadas
    private BeginSignInRequest signUpRequest;   // fallback para nuevas cuentas

    private final ActivityResultLauncher<IntentSenderRequest> oneTapLauncher =
            registerForActivityResult(new StartIntentSenderForResult(), result -> {
                if (result.getData() == null) { toast("Google cancelado"); return; }
                try {
                    SignInCredential cred = Identity.getSignInClient(this)
                            .getSignInCredentialFromIntent(result.getData());
                    String idToken = cred.getGoogleIdToken();
                    if (idToken == null || idToken.isEmpty()) { toast("Token Google nulo"); return; }
                    AuthCredential firebaseCred = GoogleAuthProvider.getCredential(idToken, null);
                    auth.signInWithCredential(firebaseCred).addOnCompleteListener(t -> {
                        if (t.isSuccessful()) next();
                        else toast("Firebase Google: " + (t.getException()!=null? t.getException().getMessage():"Error"));
                    });
                } catch (ApiException e) {
                    toast("Google One Tap error (" + e.getStatusCode() + ")");
                }
            });

    // --- Google Sign-In clásico (segundo fallback) ---
    private GoogleSignInClient classicClient;
    private final ActivityResultLauncher<Intent> classicLauncher =
            registerForActivityResult(new StartActivityForResult(), result -> {
                if (result.getData() == null) { toast("Google cancelado"); return; }
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                try {
                    GoogleSignInAccount acc = task.getResult(ApiException.class);
                    if (acc == null || acc.getIdToken() == null) { toast("Token Google nulo"); return; }
                    AuthCredential cred = GoogleAuthProvider.getCredential(acc.getIdToken(), null);
                    auth.signInWithCredential(cred).addOnCompleteListener(t -> {
                        if (t.isSuccessful()) next();
                        else toast("Firebase Google clásico: " + (t.getException()!=null? t.getException().getMessage():"Error"));
                    });
                } catch (ApiException e) {
                    toast("Google clásico error (" + e.getStatusCode() + ")");
                }
            });

    // --- Facebook ---
    private CallbackManager fbCallbackManager;

    @Override
    protected void onCreate(@Nullable Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        inEmail = findViewById(R.id.inputEmail);
        inPass  = findViewById(R.id.inputPassword);

        // Email/Password
        ((MaterialButton) findViewById(R.id.btnLogin)).setOnClickListener(v -> doEmailLogin());
        ((MaterialButton) findViewById(R.id.btnRegister)).setOnClickListener(
                v -> startActivity(new Intent(this, RegisterActivity.class)));

        // 👉 Olvidé contraseña
        TextView tvForgot = findViewById(R.id.tvForgotPassword);
        tvForgot.setOnClickListener(v ->
                startActivity(new Intent(this, ResetPasswordActivity.class)));

        // ====== GOOGLE ======
        setupGoogleOneTap();
        setupClassicGoogle(); // listo para el segundo fallback

        findViewById(R.id.btnGoogleNative).setOnClickListener(v -> {
            // 1) intentamos sign-in (autorizadas/guardadas)
            oneTapClient.beginSignIn(signInRequest)
                    .addOnSuccessListener(this::launchOneTap)
                    .addOnFailureListener(e ->
                            // 2) fallback: sign-up (mostrar todas las cuentas del dispositivo)
                            oneTapClient.beginSignIn(signUpRequest)
                                    .addOnSuccessListener(this::launchOneTap)
                                    .addOnFailureListener(er -> {
                                        // 3) segundo fallback (opcional): Google clásico
                                        if (classicClient != null) {
                                            classicLauncher.launch(classicClient.getSignInIntent());
                                        } else {
                                            toast("Google clásico no disponible");
                                        }
                                    })
                    );
        });

        // ====== FACEBOOK ======
        fbCallbackManager = CallbackManager.Factory.create();
        LoginButton fbButton = findViewById(R.id.btnFacebookNative);
        fbButton.setPermissions(Arrays.asList("email", "public_profile"));
        fbButton.registerCallback(fbCallbackManager, new com.facebook.FacebookCallback<LoginResult>() {
            @Override public void onSuccess(LoginResult loginResult) {
                AuthCredential cred = FacebookAuthProvider.getCredential(
                        loginResult.getAccessToken().getToken());
                auth.signInWithCredential(cred).addOnCompleteListener(t -> {
                    if (t.isSuccessful()) next();
                    else toast("Firebase Facebook: " + (t.getException()!=null? t.getException().getMessage():"Error"));
                });
            }
            @Override public void onCancel() { toast("Facebook cancelado"); }
            @Override public void onError(FacebookException error) { toast("Facebook error: " + error.getMessage()); }
        });

        // Autologin si ya hay sesión
        if (auth.getCurrentUser() != null) next();
    }

    // ---------- Google One Tap ----------
    private void setupGoogleOneTap() {
        oneTapClient = Identity.getSignInClient(this);
        String webClientId = getString(R.string.default_web_client_id);

        // Sign-IN: solo cuentas previamente autorizadas
        signInRequest = new BeginSignInRequest.Builder()
                .setGoogleIdTokenRequestOptions(
                        new BeginSignInRequest.GoogleIdTokenRequestOptions.Builder()
                                .setSupported(true)
                                .setServerClientId(webClientId)
                                .setFilterByAuthorizedAccounts(true)
                                .build()
                )
                .setAutoSelectEnabled(false)
                .build();

        // Sign-UP: mostrar todas las cuentas del dispositivo (primer ingreso)
        signUpRequest = new BeginSignInRequest.Builder()
                .setGoogleIdTokenRequestOptions(
                        new BeginSignInRequest.GoogleIdTokenRequestOptions.Builder()
                                .setSupported(true)
                                .setServerClientId(webClientId)
                                .setFilterByAuthorizedAccounts(false)
                                .build()
                )
                .setAutoSelectEnabled(false)
                .build();
    }

    private void launchOneTap(BeginSignInResult result) {
        IntentSenderRequest req = new IntentSenderRequest.Builder(
                result.getPendingIntent().getIntentSender()).build();
        oneTapLauncher.launch(req);
    }

    // ---------- Google clásico ----------
    private void setupClassicGoogle() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        classicClient = GoogleSignIn.getClient(this, gso);
    }

    // ---------- Email/Password ----------
    private void doEmailLogin() {
        String email = text(inEmail), pass = text(inPass);
        if (email.isEmpty() || pass.isEmpty()) { toast("Completa email y contraseña"); return; }
        auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(t -> {
            if (t.isSuccessful()) next();
            else toast("Email/clave: " + (t.getException()!=null? t.getException().getMessage():"Error"));
        });
    }

    // Facebook SDK
    @Override protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (fbCallbackManager != null) fbCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void next() {
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    private static String text(TextInputEditText e) { return e.getText()==null? "": e.getText().toString().trim(); }
    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }
}