package thanhdnh.ueh.edu.article_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class UserAdapter extends BaseAdapter {
    private final ArrayList<UserProfile> users;
    private final Context context;

    public UserAdapter(ArrayList<UserProfile> users, Context context) {
        this.users = users;
        this.context = context;
    }

    @Override
    public int getCount() {
        return users.size();
    }

    @Override
    public Object getItem(int position) {
        return users.get(position);
    }

    @Override
    public long getItemId(int position) {
        return users.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.user_disp_tpl, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        UserProfile user = users.get(position);
        holder.username.setText(user.getUsername());

        Picasso.get()
                .load(user.getAvatarUrl())
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .fit()
                .centerCrop()
                .into(holder.avatar);

        return convertView;
    }

    private static class ViewHolder {
        final ImageView avatar;
        final TextView username;

        ViewHolder(View view) {
            avatar = view.findViewById(R.id.imv_avatar);
            username = view.findViewById(R.id.tv_username);
        }
    }
}
