package com.bc.communitybc.user;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bc.communitybc.R;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder>{

    public interface OnItemClickListener{
        void onItemClick(UserDto userDto, int position);
    }
    private final OnItemClickListener onClickListener;

    private final LayoutInflater inflater;
    private final List<UserDto> allUserDtos;

    public UserAdapter(Context context, List<UserDto> allUserDtos, OnItemClickListener onClickListener) {
        this.allUserDtos = allUserDtos;
        this.inflater = LayoutInflater.from(context);
        this.onClickListener = onClickListener;
    }
    @Override
    public UserAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.user_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(UserAdapter.ViewHolder holder, int position) {
        UserDto userDto = allUserDtos.get(position);
        holder.textNameUserFriend.setText(userDto.getName());
        holder.textLoginUserFriends.setText(userDto.getLogin());

        holder.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                onClickListener.onItemClick(userDto, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (allUserDtos == null)
            return 0;
        return allUserDtos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView textNameUserFriend, textLoginUserFriends;
        ViewHolder(View view){
            super(view);
            textNameUserFriend = (TextView) view.findViewById(R.id.textNameUserFriend);
            textLoginUserFriends = (TextView) view.findViewById(R.id.textLoginUserFriends);
        }
    }
}
