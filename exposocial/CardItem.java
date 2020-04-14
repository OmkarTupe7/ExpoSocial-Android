package com.example.soumilchugh.exposocial;

public class CardItem {

    private int mTextResource;
    private int mImageResource;

    public CardItem(int text, int img) {
        mTextResource = text;
        mImageResource = img;
    }

    public int getText() {
        return mTextResource;
    }

    public int getImageID() {
        return mImageResource;
    }
}
