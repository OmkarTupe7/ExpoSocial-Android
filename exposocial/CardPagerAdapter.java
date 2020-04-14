package com.example.soumilchugh.exposocial;

import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class CardPagerAdapter extends PagerAdapter implements CardAdapter {

    private List<CardView> mViews;
    private List<CardItem> mData;
    private float mBaseElevation;
    // Define fear level descriptions
    private static String[] fearLevel = { "None", "Mild", "Moderate", "Severe" };
    // Define avoid level descriptions
    private static String[] avoidLevel = { "Never", "Occasionally", "Often", "Usually" };

    public CardPagerAdapter() {
        mData = new ArrayList<>();
        mViews = new ArrayList<>();
    }

    public void addCardItem(CardItem item) {
        mViews.add(null);
        mData.add(item);
    }

    @Override
    public float getBaseElevation() {
        return mBaseElevation;
    }

    @Override
    public CardView getCardViewAt(int position) {
        return mViews.get(position);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public boolean isViewFromObject( View view, Object o) {
        return view == o;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = LayoutInflater.from(container.getContext()).inflate(R.layout.card, container, false);
        container.addView(view);
        // Get current card item
        CardItem item = mData.get(position);
        // Update Question and Image to display
        TextView questionTxtView = (TextView) view.findViewById(R.id.txt_question);
        TextView pageTxt = (TextView) view.findViewById(R.id.txtPage);
        ImageView questionImgView = (ImageView) view.findViewById(R.id.img_question);
        questionTxtView.setText(item.getText());
        pageTxt.setText(position+1 + "/" + getCount());
        questionImgView.setImageResource(item.getImageID());

        // Get SeekBars and textView for display the values
        final SeekBar sb_fear = (SeekBar) view.findViewById(R.id.seekBar_fear);
        final SeekBar sb_avoid = (SeekBar) view.findViewById(R.id.seekBar_avoid);
        final TextView valFear = (TextView) view.findViewById(R.id.txt_fearValue);
        final TextView valAvoid = (TextView) view.findViewById(R.id.txt_avoidValue);

        // Listen to change
        sb_avoid.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {valAvoid.setText(avoidLevel[sb_avoid.getProgress()]);}
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        sb_fear.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {valFear.setText(fearLevel[sb_fear.getProgress()]);}
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Setup the margins
        TextView txtNone = view.findViewById(R.id.txtNone);
        TextView txtMild = view.findViewById(R.id.txtMild);
        TextView txtModerate = view.findViewById(R.id.txtModerate);
        TextView txtSevere = view.findViewById(R.id.txtSevere);
        TextView txtNever = view.findViewById(R.id.txtNever);
        TextView txtOcca = view.findViewById(R.id.txtOcca);
        TextView txtOften = view.findViewById(R.id.txtOften);
        TextView txtUsually = view.findViewById(R.id.txtUsually);

        int w = view.getWidth();
        txtNone.setWidth((int)w/4);
        txtMild.setWidth((int)w/4);
        txtModerate.setWidth((int)w/4);
        txtSevere.setWidth((int)w/4);
        txtNever.setWidth((int)w/4);
        txtOcca.setWidth((int)w/4);
        txtOften.setWidth((int)w/4);
        txtUsually.setWidth((int)w/4);

        CardView cardView = (CardView) view.findViewById(R.id.card_view);

        if (mBaseElevation == 0) {
            mBaseElevation = cardView.getCardElevation();
        }

        cardView.setMaxCardElevation(mBaseElevation * MAX_ELEVATION_FACTOR);
        mViews.set(position, cardView);
        view.setTag(position);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
        mViews.set(position, null);
    }

}
