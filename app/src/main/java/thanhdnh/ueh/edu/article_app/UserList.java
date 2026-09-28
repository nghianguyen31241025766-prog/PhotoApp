package thanhdnh.ueh.edu.article_app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UserList {
    private final ArrayList<UserProfile> users;

    public UserList() {
        users = new ArrayList<>();
    }

    public UserList(List<UserProfile> users) {
        this.users = new ArrayList<>(users);
    }

    public UserList(UserProfile[] users) {
        this.users = new ArrayList<>(Arrays.asList(users));
    }

    public ArrayList<UserProfile> getUsers() {
        return users;
    }
}
