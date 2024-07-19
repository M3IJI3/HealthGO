package com.example.healthgo;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;

public class WeightBottomSheetFragment extends BottomSheetDialogFragment {
    private BMIBottomSheetListener mListener;
    public interface BMIBottomSheetListener {
        void onSaveClicked(Float weight);
    }

    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);
        try {
            mListener = (BMIBottomSheetListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement BottomSheetListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bmi_fragment_bottom_sheet, container, false);

        NumberPicker numberPicker1 = view.findViewById(R.id.number_picker_1);
        NumberPicker numberPicker2 = view.findViewById(R.id.number_picker_2);
        Button saveButton = view.findViewById(R.id.save_button);


        // Configure number pickers
        numberPicker1.setMinValue(40);
        numberPicker1.setMaxValue(150);
        numberPicker1.setValue(70);

        numberPicker2.setMinValue(0);
        numberPicker2.setMaxValue(9);
        numberPicker2.setValue(0);

        saveButton.setOnClickListener(v -> {
            // Handle save button click
            float weight = numberPicker1.getValue() + numberPicker2.getValue() / 10.0f;
            mListener.onSaveClicked(weight);
            dismiss();
        });
        return view;
    }
}