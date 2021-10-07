package com.bc.communitybc.dialog;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bc.communitybc.MainActivity;
import com.bc.communitybc.R;
import com.github.library.bubbleview.BubbleTextView;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder>{

    private final LayoutInflater inflater;
    private final List<MessageDto> allMessageDto;

    public MessageAdapter(Context context, List<MessageDto> allMessageDto) {
        this.allMessageDto = allMessageDto;
        this.inflater = LayoutInflater.from(context);
    }
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.message_list_item, parent, false);
        return new MessageAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MessageAdapter.ViewHolder holder, int position) {
        MessageDto messageDto = allMessageDto.get(position);

        //Сообщения текущего пользователя справа, а сообщения собеседника слева
        if(messageDto.getSender_id() == MainActivity.getIdCurrentUserDto()) {
            holder.message_BubbleLayout.setGravity(Gravity.RIGHT);
        } else {
            holder.message_BubbleLayout.setGravity(Gravity.LEFT);
        }

        holder.msg_user.setText(messageDto.getSender_name());
        holder.msg_text.setText(messageDto.getContent());
        holder.msg_time.setText(messageDto.getTimestamp());
    }

    @Override
    public int getItemCount() {
        if (allMessageDto == null)
            return 0;
        return allMessageDto.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView msg_user, msg_time;
        BubbleTextView msg_text;
        LinearLayout message_BubbleLayout;
        ViewHolder(View view){
            super(view);
            message_BubbleLayout = view.findViewById(R.id.message_BubbleLayout);
            msg_user = view.findViewById(R.id.textNameUser);
            msg_text = view.findViewById(R.id.textMessage);
            msg_time = view.findViewById(R.id.textDate);

        }
    }
}