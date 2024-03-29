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
import com.google.firebase.database.DatabaseReference;

import java.util.List;

public class ChatAdapter extends   RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private Context mcontext;
    private List<Chat> mchat;
    public static final int type_left = 0;
    public static final int type_right = 1;
    private String imageurl;
    private DatabaseReference reference;

    FirebaseUser fuser;


    public ChatAdapter(Context mcontext, List<Chat> mchat, String imageurl) {
        this.mcontext = mcontext;
        this.mchat = mchat;
        this.imageurl = imageurl;
    }

    @NonNull
    @Override
    public  ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
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
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        Chat chat = mchat.get(position);
        holder.show_message.setText(chat.getMessage());
        holder.msg_time.setText(chat.getTime().split(" ")[3]);

        if (imageurl.equals("default")){
            holder.profile_image.setImageResource(R.mipmap.man);
        }
        else
            Glide.with(mcontext).load(imageurl).into(holder.profile_image);
            if (chat.isSeen()){
                holder.txt_seen.setImageResource(R.mipmap.seen);
            }
            else
                holder.txt_seen.setImageResource(R.mipmap.send);


    }


    @Override
    public int getItemCount() {
        return mchat.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView show_message,msg_time;
        public ImageView profile_image,txt_seen;


        public ViewHolder(View itemView) {
            super(itemView);
            show_message = itemView.findViewById(R.id.show_message);
            profile_image = itemView.findViewById(R.id.profile_image);
            txt_seen = itemView.findViewById(R.id.txt_seen);
            msg_time=itemView.findViewById(R.id.msg_time);
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