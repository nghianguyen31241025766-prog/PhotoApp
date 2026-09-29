package thanhdnh.ueh.edu.article_app;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import java.io.File;

public class ViewArticleActivity extends AppCompatActivity {
  private ImageView avatar;
  private TextView username;
  private TextView email;
  private TextView name;
  private TextView description;
  private TextView company;
  private TextView location;
  private TextView hobby;
  private TextView githubUrl;
  private ProgressBar progressBar;
  private Button downloadButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_view_article);

    if (getSupportActionBar() != null) {
      getSupportActionBar().hide();
    }

    avatar = findViewById(R.id.iv_detail);
    username = findViewById(R.id.tv_detail_username);
    email = findViewById(R.id.tv_detail_email);
    name = findViewById(R.id.tv_detail_name);
    description = findViewById(R.id.tv_detail_description);
    company = findViewById(R.id.tv_detail_company);
    location = findViewById(R.id.tv_detail_location);
    hobby = findViewById(R.id.tv_detail_hobby);
    githubUrl = findViewById(R.id.tv_detail_url);
    progressBar = findViewById(R.id.download_progress);
    downloadButton = findViewById(R.id.btn_download);

    String usernameValue = getIntent().getStringExtra("username");
    int id = getIntent().getIntExtra("id", 0);

    UserProfile listUser = UserData.getUserById(id);
    if (listUser != null) {
      showUser(listUser);
    }

    if (usernameValue != null && !usernameValue.isEmpty()) {
      loadFullUser(usernameValue);
    }
  }

  private void showUser(UserProfile user) {
    username.setText("@" + safe(user.getUsername(), "Unknown"));
    email.setText("Email: " + safe(user.getEmail(), "Không công khai"));
    name.setText("Name: " + safe(user.getName(), "Chưa cập nhật"));
    description.setText(safe(user.getDescription(), "Chưa có mô tả"));
    company.setText("Company: " + safe(user.getCompany(), "Chưa cập nhật"));
    location.setText("Location: " + safe(user.getLocation(), "Chưa cập nhật"));
    hobby.setText("Hobby: " + safe(user.getHobby(), "Chưa cập nhật"));
    githubUrl.setText(safe(user.getHtmlUrl(), ""));

    Picasso.get()
            .load(user.getAvatarUrl())
            .placeholder(R.mipmap.ic_launcher)
            .error(R.mipmap.ic_launcher)
            .fit()
            .centerCrop()
            .into(avatar);

    downloadButton.setOnClickListener(v -> downloadAvatar(user.getAvatarUrl()));
  }

  private void loadFullUser(String usernameValue) {
    new Thread(() -> {
      File file = Downloader.downloadFile(
              "https://api.github.com/users/" + usernameValue,
              getCacheDir());

      if (file == null) {
        runOnUiThread(() -> Toast.makeText(this,
                "Không tải được thông tin User", Toast.LENGTH_LONG).show());
        return;
      }

      try {
        String json = Downloader.readText(file);
        UserProfile user = new com.google.gson.Gson().fromJson(json, UserProfile.class);
        runOnUiThread(() -> showUser(user));
      } catch (Exception e) {
        runOnUiThread(() -> Toast.makeText(this,
                "Lỗi đọc thông tin User", Toast.LENGTH_LONG).show());
      }
    }).start();
  }

  private void downloadAvatar(String url) {
    if (url == null || url.trim().isEmpty()) {
      Toast.makeText(this, "User không có avatar", Toast.LENGTH_SHORT).show();
      return;
    }

    progressBar.setProgress(0);
    progressBar.setVisibility(View.VISIBLE);
    downloadButton.setEnabled(false);

    Handler handler = new Handler(Looper.getMainLooper());
    Downloader.downloadWithProgress(
            url,
            handler,
            this,
            getCacheDir(),
            progressBar,
            avatar,
            new Downloader.DownloadListener() {
              @Override
              public void onSuccess() {
                downloadButton.setEnabled(true);
                Toast.makeText(ViewArticleActivity.this,
                        "Đã tải avatar vào bộ nhớ cache", Toast.LENGTH_SHORT).show();
              }

              @Override
              public void onError(String message) {
                downloadButton.setEnabled(true);
                Toast.makeText(ViewArticleActivity.this, message, Toast.LENGTH_LONG).show();
              }
            });
  }

  private String safe(String value, String fallback) {
    return value == null || value.trim().isEmpty() ? fallback : value;
  }
}
