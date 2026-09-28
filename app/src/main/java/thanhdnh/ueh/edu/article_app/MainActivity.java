package thanhdnh.ueh.edu.article_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
  private GridView gridView;
  private UserData userData;

  private final AdapterView.OnItemClickListener onItemClick = (parent, view, position, id) -> {
    UserProfile user = (UserProfile) parent.getAdapter().getItem(position);
    Intent intent = new Intent(MainActivity.this, ViewArticleActivity.class);
    intent.putExtra("username", user.getUsername());
    intent.putExtra("id", user.getId());
    startActivity(intent);
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    if (getSupportActionBar() != null) {
      getSupportActionBar().hide();
    }

    gridView = findViewById(R.id.gridview);
    gridView.setOnItemClickListener(onItemClick);

    userData = new UserData(this, gridView);
    userData.loadData("https://api.github.com/users?per_page=30", this);
  }

  @Override
  protected void onDestroy() {
    if (userData != null) userData.shutdown();
    super.onDestroy();
  }
}
