package com.vivo.hdp.sleep;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;

/**
 * Created by 10957084 on 2017/10/31.
 */

public class MyAdapter extends BaseAdapter{

    Context context;
    List<AppInfo> appInfos;
    private static Map<Integer,Boolean> pkgData = new HashMap<>();
    private static HashMap<Integer, Boolean> isSelected;
    private ArrayList<String> mData;
    MyDBHelper dbHelper = new MyDBHelper(getContext());


    private void initData() {
        for (int i = 0; i < appInfos.size(); i++) {
            getIsSelected().put(i, false);
        }
    }

    public MyAdapter(Context context , List<AppInfo> infos,ArrayList<String> data){
        this.context = context;
        this.appInfos = infos;
        this.mData = data;
        isSelected = new HashMap<Integer, Boolean>();
        Log.e(TAG, "MyAdapter: "+isSelected);
        initData();
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public List<AppInfo> getAppInfos() {
        return appInfos;
    }

    public void setAppInfos(List<AppInfo> appInfos) {
        this.appInfos = appInfos;
    }


    public Context getContext() {
        return context;
    }

    @Override
    public int getCount() {
        return appInfos!=null?appInfos.size():0;
    }

    @Override
    public Object getItem(int i) {
        return appInfos.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        ViewHolder holder = null;
        final AppInfo infos = appInfos.get(i);
        if (view == null) {
            holder = new ViewHolder();
            LayoutInflater mInflater = LayoutInflater.from(context);
            view = mInflater.inflate(R.layout.list_view, null);
            holder.appIconImg = view.findViewById(R.id.image);
            holder.appNameText = view.findViewById(R.id.apkName);
            holder.appPackageText = view.findViewById(R.id.pkgName);
            view.setTag(holder);
        }else {
            holder = (ViewHolder)view.getTag();
        }

        holder.cb = view.findViewById(R.id.checkbox);
        holder.appIconImg.setBackground(infos.getDrawable());
        holder.appNameText.setText(infos.getAppName());
        holder.appPackageText.setText(infos.getPackageName());

        holder.cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
                if (isChecked){
//                    buttonView.setButtonDrawable(R.drawable.r02);
                    mData.add(infos.getPackageName());
                    isSelected.put(i, true);
                }else{
//                    buttonView.setButtonDrawable(R.drawable.r01);
                    mData.remove(infos.getPackageName());
                    isSelected.put(i, false);
               }
            }
        });
        holder.cb.setChecked(getIsSelected().get(i));
        return view;
    }

    public static Map<Integer, Boolean> getpkgData() {
        return pkgData;
    }

    public final class ViewHolder{
        public ImageView appIconImg;
        public TextView appNameText;
        public TextView appPackageText;
        public CheckBox cb;
    }

    public static void setIsSelected(HashMap<Integer, Boolean> isSelected)   {
        MyAdapter.isSelected = isSelected;
    }

    public static HashMap<Integer, Boolean> getIsSelected() {
        return isSelected;
    }
}
