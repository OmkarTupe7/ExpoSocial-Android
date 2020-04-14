package com.example.soumilchugh.exposocial;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;

public class ListAdapter extends BaseAdapter {
    private Context mContext;
    private fearLadder ladder;

    public ListAdapter (Context c, fearLadder l){
        mContext = c;
        ladder = l;
    }

    @Override
    public int getCount() {
        return ladder.situations.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = LayoutInflater.from(mContext).inflate(R.layout.fear_list, viewGroup, false);
        TextView txtSituation = view.findViewById(R.id.txtSituation);
        TextView txtScale = view.findViewById(R.id.txtScale);
        if (i != getCompleteNumExpo(mContext, getCount())){
            txtSituation.setTextColor(view.getResources().getColor(R.color.seek_bar_background));
            txtScale.setTextColor(view.getResources().getColor(R.color.seek_bar_background));
        }
        if (i == getCompleteNumExpo(mContext, getCount())){
            view.setBackgroundColor(view.getResources().getColor(R.color.dot_dark_screen3));
            txtSituation.setTextColor(view.getResources().getColor(R.color.colorPaper));
            txtScale.setTextColor(view.getResources().getColor(R.color.colorPaper));
        }
        txtSituation.setText(ladder.situations.get(i));
        txtScale.setText(ladder.scaleValues.get(i).toString());
        return view;
    }

    private int getCompleteNumExpo(Context context, int lenLadder){
        HashMap<String, HashMap<Integer, Integer>> outData = new HashMap<>();
        String path = context.getFilesDir().getAbsolutePath() + "/" + "finished_expo_data.map";
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(path);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            outData = (HashMap) objectInputStream.readObject();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        int numE = outData.keySet().size();
        int nowE = lenLadder - (numE/5 + 1);
        return nowE;
    }
}
