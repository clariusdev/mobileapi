package me.clarius.sdk.mobileapi.example;

import android.graphics.Bitmap;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * Share data from activity to fragments.
 * <p>
 * https://developer.android.com/guide/fragments/communicate
 */

public class ImageViewModel extends ViewModel {
    private final MutableLiveData<Bitmap> bImageData = new MutableLiveData<>();

    public LiveData<Bitmap> getBImage() {
        return bImageData;
    }

    public void setBImage(Bitmap bImage) {
        bImageData.setValue(bImage);
    }
}
