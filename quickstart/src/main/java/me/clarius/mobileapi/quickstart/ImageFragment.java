package me.clarius.mobileapi.quickstart;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

/**
 * Display the B-Image
 * <p>
 * The bitmap data is provided by the observable LiveData object shared in the ImageModelView class.
 */

public class ImageFragment extends Fragment {
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.image_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageView imageView = requireActivity().findViewById(R.id.imageView);
        ImageViewModel viewModel = new ViewModelProvider(requireActivity()).get(ImageViewModel.class);
        viewModel.getBImage().observe(getViewLifecycleOwner(), imageView::setImageBitmap);
    }
}
