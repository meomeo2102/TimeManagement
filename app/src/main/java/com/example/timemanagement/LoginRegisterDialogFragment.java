package com.example.timemanagement;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.concurrent.*;

public class LoginRegisterDialogFragment extends DialogFragment {
    private EditText edtUsername, edtPassword, edtConfirm;
    private TaskDatabase db;
    private Executor executor = Executors.newSingleThreadExecutor();
    private boolean isRegisterMode = false;

    public static LoginRegisterDialogFragment newInstance() {
        return new LoginRegisterDialogFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_login_register, container, false);
    }
    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow()
                    .setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        edtUsername = view.findViewById(R.id.edtUsername);
        edtPassword = view.findViewById(R.id.edtPassword);
        edtConfirm = view.findViewById(R.id.edtConfirmPassword);
        Button btnToggle = view.findViewById(R.id.btnToggleMode);
        Button btnSubmit = view.findViewById(R.id.btnSubmit);
        db = TaskDatabase.getInstance(requireContext());

        updateUI();

        btnToggle.setOnClickListener(v -> {
            isRegisterMode = !isRegisterMode;
            updateUI();
        });
        TextView tvForgot = view.findViewById(R.id.tvForgotPassword);
        tvForgot.setOnClickListener(v -> {
            if (isRegisterMode) {
                Toast.makeText(getContext(), "Chức năng chỉ áp dụng khi đăng nhập", Toast.LENGTH_SHORT).show();
                return;
            }

            String username = edtUsername.getText().toString().trim();
            if (username.isEmpty()) {
                edtUsername.setError("Vui lòng nhập tên đăng nhập trước");
                return;
            }

            executor.execute(() -> {
                UserModel.UserDao userDao = db.userDao();
                UserModel.User user = userDao.findByUsername(username);

                if (user == null) {
                    requireActivity().runOnUiThread(() ->
                            edtUsername.setError("Tài khoản không tồn tại"));
                } else {
                    requireActivity().runOnUiThread(() -> showResetPasswordDialog(username));
                }
            });
        });
        btnSubmit.setOnClickListener(v -> {
            String username = edtUsername.getText().toString().trim();
            String password = edtPassword.getText().toString();
            String confirm = edtConfirm.getText().toString();

            if (username.length() < 4) {
                edtUsername.setError("Tên đăng nhập phải có ít nhất 4 ký tự");
                return;
            }
            if (username.contains(" ")) {
                edtUsername.setError("Không được chứa khoảng trắng");
                return;
            }
            if (password.length() < 6) {
                edtPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
                return;
            }
            if (isRegisterMode && !password.equals(confirm)) {
                edtConfirm.setError("Mật khẩu xác nhận không khớp");
                return;
            }

            String passwordHash = AuthUtils.PasswordUtil.hash(password);

            executor.execute(() -> {
                UserModel.UserDao userDao = db.userDao();
                UserModel.User user = userDao.findByUsername(username);

                if (isRegisterMode) {
                    if (user != null) {
                        requireActivity().runOnUiThread(() ->
                                edtUsername.setError("Tên người dùng đã tồn tại"));
                    } else {
                        userDao.insert(new UserModel.User(username, passwordHash, System.currentTimeMillis()));
                        AuthUtils.SessionManager.login(requireContext(), username);
                        requireActivity().runOnUiThread(() -> handleLoginOrRegisterSuccess(username, true));
                    }
                } else {
                    if (user == null || !user.passwordHash.equals(passwordHash)) {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "Sai tên đăng nhập hoặc mật khẩu", Toast.LENGTH_SHORT).show());
                    } else {
                        AuthUtils.SessionManager.login(requireContext(), username);
                        requireActivity().runOnUiThread(() -> handleLoginOrRegisterSuccess(username, false));
                    }
                }
            });
        });
    }

    private void showResetPasswordDialog(String username) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_reset_password, null);
        EditText edtNewPassword = dialogView.findViewById(R.id.edtNewPassword);
        EditText edtConfirm = dialogView.findViewById(R.id.edtConfirmNewPassword);

        new AlertDialog.Builder(requireContext())
                .setTitle("Đặt lại mật khẩu")
                .setView(dialogView)
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    String newPass = edtNewPassword.getText().toString();
                    String confirmPass = edtConfirm.getText().toString();

                    if (newPass.length() < 6) {
                        Toast.makeText(getContext(), "Mật khẩu phải ≥ 6 ký tự", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!newPass.equals(confirmPass)) {
                        Toast.makeText(getContext(), "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    executor.execute(() -> {
                        String hashed = AuthUtils.PasswordUtil.hash(newPass);
                        db.userDao().updatePassword(username, hashed);
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "Đặt lại mật khẩu thành công", Toast.LENGTH_SHORT).show());
                    });
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void handleLoginOrRegisterSuccess(String username, boolean isRegister) {
        Fragment parent = getParentFragment();
        if (parent instanceof ProfileFragment) {
            ((ProfileFragment) parent).updateUIWithUsername(username);
        }

        if (requireActivity() instanceof MainActivity) {
            MainActivity main = (MainActivity) requireActivity();
            main.reloadTasks();
        }

        BottomNavigationView bottomNavSecondary = requireActivity().findViewById(R.id.bottom_nav_secondary);
        bottomNavSecondary.setSelectedItemId(R.id.nav_profile);

        Toast.makeText(getContext(),
                isRegister ? "Đăng ký & đăng nhập thành công" : "Đăng nhập thành công",
                Toast.LENGTH_SHORT).show();

        dismiss();
    }

    private void updateUI() {
        edtConfirm.setVisibility(isRegisterMode ? View.VISIBLE : View.GONE);
        Button btnSubmit = requireView().findViewById(R.id.btnSubmit);
        Button btnToggle = requireView().findViewById(R.id.btnToggleMode);
        btnSubmit.setText(isRegisterMode ? "Đăng ký" : "Đăng nhập");
        btnToggle.setText(isRegisterMode ? "Chuyển sang đăng nhập" : "Chuyển sang đăng ký");
    }
}