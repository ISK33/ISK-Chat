package com.example.iskchat;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.iskchat.Adapter.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
  private Context mcontext;
  private List<User> mUser;
  public UserAdapter(Context mcontext,List<User> mUser){
      this.mcontext=mcontext;
      this.mUser=mUser;
  }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View view= LayoutInflater.from(mcontext).inflate(R.layout.user,parent,false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
final User user=mUser.get(position);
holder.username.setText(user.getUsername());
if (user.getImageURL().equals("default")){
    holder.profile_image.setImageResource(R.mipmap.man);

}
else
    Glide.with(mcontext).load(user.getImageURL()).into(holder.profile_image);

holder.itemView.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        Intent intent = new Intent(mcontext,MainChatActivity.class);
        intent.putExtra("userid",user.getId());
        mcontext.startActivity(intent);

    }
});
    }


    @Override
    public int getItemCount() {
        return mUser.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
      public TextView username;
      public ImageView profile_image;
      public ViewHolder(View itemView){
          super(itemView);
          username=itemView.findViewById(R.id.username);
          profile_image=itemView.findViewById(R.id.profile_image);
      }
}
}
