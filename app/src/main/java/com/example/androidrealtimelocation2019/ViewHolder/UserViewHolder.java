package com.example.androidrealtimelocation2019.ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidrealtimelocation2019.Interface.IRecyclerItemClickListener;
import com.example.androidrealtimelocation2019.R;

public class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView txt_user_email;
    public ImageView image_prof;
    IRecyclerItemClickListener iRecyclerItemClickListener;

    public void setiRecyclerItemClickListener(IRecyclerItemClickListener iRecyclerItemClickListener) {
        this.iRecyclerItemClickListener = iRecyclerItemClickListener;
    }

    public UserViewHolder(@NonNull View itemView) {
        super(itemView);
        txt_user_email = (TextView)itemView.findViewById(R.id.txt_user_email);
        image_prof = (ImageView)itemView.findViewById(R.id.image_prof);
        itemView.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        iRecyclerItemClickListener.onItemClickListener(view,getAdapterPosition());

    }
}
