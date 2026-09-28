package thanhdnh.ueh.edu.article_app;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Downloader {
  public static String cached_file_path = "";

  public interface DownloadListener {
    void onSuccess();
    void onError(String message);
  }

  private Downloader() { }

  public static File downloadFile(String url, File cached) {
    OkHttpClient client = new OkHttpClient();
    Request request = new Request.Builder()
            .url(url)
            .header("Accept", "application/vnd.github+json")
            .build();

    try (Response response = client.newCall(request).execute()) {
      if (!response.isSuccessful() || response.body() == null) return null;

      String extension = getExtensionFromMimeType(response.header("Content-Type", ""));
      File file = File.createTempFile("downloaded_file", extension, cached);

      try (InputStream input = response.body().byteStream();
           OutputStream output = new FileOutputStream(file)) {
        byte[] buffer = new byte[8192];
        int count;
        while ((count = input.read(buffer)) != -1) {
          output.write(buffer, 0, count);
        }
      }
      return file;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static String readText(File file) throws IOException {
    StringBuilder builder = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
      String line;
      while ((line = reader.readLine()) != null) {
        builder.append(line).append('\n');
      }
    }
    return builder.toString();
  }

  public static void downloadWithProgress(
          String inputUrl,
          Handler mainHandler,
          Context context,
          File whereToStore,
          ProgressBar progressBar,
          ImageView imageView,
          DownloadListener listener) {

    OkHttpClient client = new OkHttpClient();
    Request request = new Request.Builder().url(inputUrl).build();

    client.newCall(request).enqueue(new Callback() {
      @Override
      public void onFailure(Call call, IOException e) {
        mainHandler.post(() -> {
          progressBar.setVisibility(ProgressBar.GONE);
          listener.onError("Tải file thất bại: " + e.getMessage());
        });
      }

      @Override
      public void onResponse(Call call, Response response) {
        if (!response.isSuccessful() || response.body() == null) {
          mainHandler.post(() -> {
            progressBar.setVisibility(ProgressBar.GONE);
            listener.onError("Server trả về lỗi: " + response.code());
          });
          return;
        }

        long totalBytes = response.body().contentLength();
        String extension = getExtensionFromMimeType(response.header("Content-Type", ""));
        if (extension.isEmpty()) extension = ".jpg";
        File outputFile = new File(whereToStore, "downloaded_avatar" + extension);

        try (InputStream input = response.body().byteStream();
             OutputStream output = new FileOutputStream(outputFile)) {

          byte[] buffer = new byte[8192];
          long downloadedBytes = 0;
          int bytesRead;

          while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
            downloadedBytes += bytesRead;

            if (totalBytes > 0) {
              int progress = (int) ((downloadedBytes * 100) / totalBytes);
              final int value = progress;
              mainHandler.post(() -> progressBar.setProgress(value));
            }
          }
          output.flush();

          cached_file_path = outputFile.getAbsolutePath();
          String finalPath = cached_file_path;
          mainHandler.post(() -> {
            imageView.setImageURI(Uri.parse(finalPath));
            progressBar.setProgress(100);
            progressBar.setVisibility(ProgressBar.GONE);
            listener.onSuccess();
          });
        } catch (Exception e) {
          mainHandler.post(() -> {
            progressBar.setVisibility(ProgressBar.GONE);
            listener.onError("Không lưu được file: " + e.getMessage());
          });
        }
      }
    });
  }

  private static String getExtensionFromMimeType(String mimeType) {
    if (mimeType == null) return "";
    String type = mimeType.toLowerCase();
    if (type.contains("image/jpeg")) return ".jpg";
    if (type.contains("image/png")) return ".png";
    if (type.contains("image/webp")) return ".webp";
    if (type.contains("application/json")) return ".json";
    return "";
  }
}
