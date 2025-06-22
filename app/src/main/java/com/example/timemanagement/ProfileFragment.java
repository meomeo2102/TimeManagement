package com.example.timemanagement;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class ProfileFragment extends Fragment {

    private TextView userInfo;
    private ImageView userAvatar;
    private Button btnGoogleLogin, btnLogout;
    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        userInfo = view.findViewById(R.id.user_info);
        userAvatar = view.findViewById(R.id.user_avatar);
        btnGoogleLogin = view.findViewById(R.id.btn_google_login);
        btnLogout = view.findViewById(R.id.btn_logout);

        // Cấu hình Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

        // Khởi tạo launcher đăng nhập
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        handleSignInResult(task);
                    }
                }
        );

        // Kiểm tra trạng thái đăng nhập
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(requireContext());
        if (account != null) {
            updateUI(account);
        } else {
            updateUI(null);
        }

        // Xử lý nút đăng nhập
        btnGoogleLogin.setOnClickListener(v -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });

        // Xử lý nút đăng xuất
        btnLogout.setOnClickListener(v -> {
            googleSignInClient.signOut().addOnCompleteListener(task -> {
                updateUI(null);
                Toast.makeText(getContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show();
            });
        });

        return view;
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            updateUI(account);
        } catch (ApiException e) {
            Toast.makeText(getContext(), "Đăng nhập thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUI(GoogleSignInAccount account) {
        if (account != null) {
            String info = "👤 " + account.getDisplayName() + "\n📧 " + account.getEmail();
            userInfo.setText(info);

            Glide.with(this)
                    .load(account.getPhotoUrl())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(userAvatar);

            btnGoogleLogin.setVisibility(View.GONE);
            btnLogout.setVisibility(View.VISIBLE);
        } else {
            userInfo.setText("Bạn chưa đăng nhập");
            userAvatar.setImageResource(R.drawable.ic_launcher_foreground);
            btnGoogleLogin.setVisibility(View.VISIBLE);
            btnLogout.setVisibility(View.GONE);
        }
    }
}