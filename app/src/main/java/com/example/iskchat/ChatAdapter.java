package com.example.iskchat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.iskchat.Adapter.Chat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class ChatAdapter extends   RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private Context mcontext;
    private List<Chat> mchat;
    public static final int type_left = 0;
    public static final int type_right = 1;
    private String imageurl;
    FirebaseUser fuser;


    public ChatAdapter(Context mcontext, List<Chat> mchat, String imageurl) {
        this.mcontext = mcontext;
        this.mchat = mchat;
        this.imageurl = imageurl;
    }

    @NonNull
    @Override
    public  ChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType==type_right) {
            View view = LayoutInflater.from(mcontext).inflate(R.layout.chat_right, parent, false);
            return new ChatAdapter.ViewHolder(view);
        }
        else {
            View view = LayoutInflater.from(mcontext).inflate(R.layout.chat_left, parent, false);
            return new ChatAdapter.ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.ViewHolder holder, int position) {
        Chat chat = mchat.get(position);
        holder.show_message.setText(chat.getMessage());
        if (imageurl.equals("default")){
            holder.profile_image.setImageResource(R.mipmap.man);
        }
        else
            Glide.with(mcontext).load(imageurl).into(holder.profile_image);
        if (position == mchat.size()-1){
            if (chat.isSeen()){
                holder.txt_seen.setText("Seen");
            }
            else
                holder.txt_seen.setText("Delivered");
        }else {
            holder.txt_seen.setVisibility(View.GONE);
        }
    }


    @Override
    public int getItemCount() {
        return mchat.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView show_message;
        public ImageView profile_image;
        public TextView txt_seen;

        public ViewHolder(View itemView) {
            super(itemView);
            show_message = itemView.findViewById(R.id.show_message);
            profile_image = itemView.findViewById(R.id.profile_image);
            txt_seen = itemView.findViewById(R.id.txt_seen);
        }
    }

    @Override
    public int getItemViewType(int position) {
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        if (mchat.get(position).getSender().equals(fuser.getUid())){
            return type_right;
        }
        else return type_left;
    }
}