package com.example.timemanagement;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Random;
import java.util.concurrent.Executors;

public class ProfileFragment extends Fragment {
    private TextView tvCompleted, tvUncompleted, userInfo;
    private ImageView userAvatar;
    private Button btnGoogleLogin, btnLogout, btnLocalLogin;

    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;
    private TextView tvTip;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        tvTip = view.findViewById(R.id.tv_tip);
        showRandomTip();
        tvCompleted = view.findViewById(R.id.tv_completed);
        tvUncompleted = view.findViewById(R.id.tv_uncompleted);
        userInfo = view.findViewById(R.id.user_info);
        userAvatar = view.findViewById(R.id.user_avatar);
        btnGoogleLogin = view.findViewById(R.id.btn_google_login);
        btnLogout = view.findViewById(R.id.btn_logout);
        btnLocalLogin = view.findViewById(R.id.btn_local_login);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail().build();
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        handleSignInResult(task);
                    }
                }
        );

        // Kiểm tra trạng thái đăng nhập ban đầu
        updateUI(GoogleSignIn.getLastSignedInAccount(requireContext()));

        btnGoogleLogin.setOnClickListener(v -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });

        btnLocalLogin.setOnClickListener(v -> {
            LoginRegisterDialogFragment.newInstance().show(getParentFragmentManager(), "LoginDialog");
        });

        btnLogout.setOnClickListener(v -> {
            // Đăng xuất Room
            AuthUtils.SessionManager.logout(requireContext());

            // Đăng xuất Google
            googleSignInClient.signOut().addOnCompleteListener(task -> {
                updateUI(null);
                Toast.makeText(getContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show();

                // Replace TaskFragment để ẩn task của user cũ
                switchToTaskFragmentFor("guest");
            });
        });

        return view;
    }

    private void showRandomTip() {
        String[] tips = {
                "Chia nhỏ công việc lớn thành các nhiệm vụ nhỏ hơn để dễ bắt đầu hơn.",
                "Sử dụng kỹ thuật Pomodoro: Làm việc 25 phút, nghỉ 5 phút.",
                "Ưu tiên 3 việc quan trọng nhất trong ngày.",
                "Bắt đầu với việc dễ để tạo đà, hoặc bắt đầu với việc khó nhất nếu bạn đang sung sức.",
                "Hạn chế đa nhiệm — tập trung vào một việc tại một thời điểm.",
                "Thêm thời gian đệm giữa các cuộc hẹn để tránh căng thẳng.",
                "Đặt deadline cá nhân sớm hơn deadline thật để tránh bị dí sát.",
                "Mỗi tối dành 5 phút xem lại ngày hôm nay và lên kế hoạch cho ngày mai."
        };

        int idx = new Random().nextInt(tips.length);
        String tip = "Mẹo: " + tips[idx];
        tvTip.setText(tip);

    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            updateUI(account);
            Toast.makeText(getContext(), "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

            if (requireActivity() instanceof MainActivity) {
                ((MainActivity) requireActivity()).reloadTasks();
            }

            BottomNavigationView bottomNavSecondary = requireActivity().findViewById(R.id.bottom_nav_secondary);
            bottomNavSecondary.setSelectedItemId(R.id.nav_profile);

        } catch (ApiException e) {
            Toast.makeText(getContext(), "Đăng nhập thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    public void updateUI(GoogleSignInAccount account) {
        String localUsername = AuthUtils.SessionManager.getLoggedInUsername(requireContext());

        if (account != null) {
            userInfo.setText("👤 " + account.getDisplayName() + "\n📧 " + account.getEmail());
            Glide.with(this).load(account.getPhotoUrl())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(userAvatar);
            btnGoogleLogin.setVisibility(View.GONE);
            btnLocalLogin.setVisibility(View.GONE);
            btnLogout.setVisibility(View.VISIBLE);
            fetchTaskStats(account.getEmail());

        } else if (localUsername != null) {
            userInfo.setText("👤 " + localUsername);
            userAvatar.setImageResource(R.drawable.ic_launcher_foreground);
            btnGoogleLogin.setVisibility(View.GONE);
            btnLocalLogin.setVisibility(View.GONE);
            btnLogout.setVisibility(View.VISIBLE);
            fetchTaskStats(localUsername);

        } else {
            userInfo.setText("Bạn chưa đăng nhập");
            userAvatar.setImageResource(R.drawable.ic_launcher_foreground);
            btnGoogleLogin.setVisibility(View.VISIBLE);
            btnLocalLogin.setVisibility(View.VISIBLE);
            btnLogout.setVisibility(View.GONE);
            tvCompleted.setText("");
            tvUncompleted.setText("");
        }
    }

    private void fetchTaskStats(String owner) {
        Executors.newSingleThreadExecutor().execute(() -> {
            TaskDatabase db = TaskDatabase.getInstance(requireContext());
            int completed = db.taskDao().countCompletedByOwner(owner, true);
            int uncompleted = db.taskDao().countCompletedByOwner(owner, false);

            requireActivity().runOnUiThread(() -> {
                tvCompleted.setText(String.valueOf(completed));
                tvUncompleted.setText(String.valueOf(uncompleted));
            });
        });
    }

    private void switchToTaskFragmentFor(String owner) {
        Fragment taskFragment = TaskFragment.newInstance("category", "Tất cả", owner);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_frame, taskFragment)
                .commitAllowingStateLoss();
    }

    public void updateUIWithUsername(String username) {
    }
}