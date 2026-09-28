package thanhdnh.ueh.edu.article_app;

import android.app.Activity;
import android.content.Context;
import android.widget.GridView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserData {
    public static UserList data;

    private final Context context;
    private final GridView gridView;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public UserData(Context context, GridView gridView) {
        this.context = context;
        this.gridView = gridView;
    }

    public static UserProfile getUserById(int id) {
        if (data == null) return null;
        for (UserProfile user : data.getUsers()) {
            if (user.getId() == id) return user;
        }
        return null;
    }

    public void loadData(String url, Activity activity) {
        executor.execute(() -> {
            File file = Downloader.downloadFile(url, context.getCacheDir());
            if (file == null) {
                activity.runOnUiThread(() -> Toast.makeText(context,
                        "Không thể tải danh sách User", Toast.LENGTH_LONG).show());
                return;
            }

            try {
                String json = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
                UserProfile[] users = new Gson().fromJson(json, UserProfile[].class);
                data = new UserList(users);

                activity.runOnUiThread(() -> {
                    UserAdapter adapter = new UserAdapter(data.getUsers(), context);
                    gridView.setAdapter(adapter);
                });
            } catch (Exception e) {
                activity.runOnUiThread(() -> Toast.makeText(context,
                        "Lỗi đọc dữ liệu User: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    public void shutdown() {
        executor.shutdownNow();
    }
}
