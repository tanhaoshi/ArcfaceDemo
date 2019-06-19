package com.arcsoft.arcfacedemo.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.arcsoft.arcfacedemo.R;
import com.arcsoft.arcfacedemo.db.dao.RegisterInfo;
import com.bumptech.glide.Glide;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainRcListAdapter extends RecyclerView.Adapter<MainRcListAdapter.RegisterInfoListViewHolder>{

    private List<RegisterInfo> mRegisterInfoList;

    private Context mContext;

    public MainRcListAdapter(List<RegisterInfo> list,Context context){
        this.mContext = context;
        this.mRegisterInfoList = list;
    }

    public void add(int position,List<RegisterInfo> list){
        this.mRegisterInfoList = list;
        notifyItemInserted(position);
    }

    public void remove(int position,List<RegisterInfo> list){
        this.mRegisterInfoList = list;
        notifyItemRemoved(position);
    }

    @NonNull
    @Override
    public RegisterInfoListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.main_rc_list_item,viewGroup,false);
        return new RegisterInfoListViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RegisterInfoListViewHolder RegisterInfoListViewHolder, int i) {
        RegisterInfoListViewHolder.bindPosition(i);
    }

    @Override
    public int getItemCount() {
        return mRegisterInfoList.size();
    }


    class RegisterInfoListViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.adapter_main_list_avatar)
        ImageView amAvatar;
        @BindView(R.id.adapter_main_list_number)
        TextView amNumber;
        @BindView(R.id.adapter_main_list_name)
        TextView  amName;

        RegisterInfoListViewHolder(View view){
            super(view);
            ButterKnife.bind(this,view);
        }

        public void bindPosition(int position){
            RegisterInfo registerInfo = mRegisterInfoList.get(position);
            Glide.with(mContext).load(registerInfo.getPath()).into(amAvatar);
            amNumber.setText("编号:"+registerInfo.getSerial());
            amName.setText("名字:"+registerInfo.getName());
        }
    }
}
