package com.bc.communitybc.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bc.communitybc.R;

import java.util.List;

public class DialogAdapter extends RecyclerView.Adapter<DialogAdapter.ViewHolder>{

    public interface OnItemClickListener{
        void onItemClick(DialogDto dialogDto, int position);
    }
    private final OnItemClickListener onClickListener;

    private final LayoutInflater inflater;
    private final List<DialogDto> allDialogDtos;

    public DialogAdapter(Context context, List<DialogDto> allDialogDtos, OnItemClickListener onClickListener) {
        this.allDialogDtos = allDialogDtos;
        this.inflater = LayoutInflater.from(context);
        this.onClickListener = onClickListener;

    }
    @Override
    public DialogAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.dialog_list_item, parent, false);
        return new DialogAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DialogAdapter.ViewHolder holder, int position) {
        DialogDto dialogDto = allDialogDtos.get(position);

        holder.textNameDialog.setText(dialogDto.getName() == null ? "error: no name" : dialogDto.getName());
        holder.textLastMessage.setText(dialogDto.getLastMessage() == null ? "error: no last message" : dialogDto.getLastMessage());

        holder.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                onClickListener.onItemClick(dialogDto, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (allDialogDtos == null)
            return 0;
        return allDialogDtos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView textNameDialog, textLastMessage;
        ViewHolder(View view){
            super(view);
            textNameDialog = (TextView) view.findViewById(R.id.textNameDialog);
            textLastMessage = (TextView) view.findViewById(R.id.textLastMessage);
        }
    }
}