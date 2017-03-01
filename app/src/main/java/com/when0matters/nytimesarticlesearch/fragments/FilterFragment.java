package com.when0matters.nytimesarticlesearch.fragments;

import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;

import com.when0matters.nytimesarticlesearch.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FilterFragment extends AppCompatDialogFragment implements TextView.OnEditorActionListener {


    @BindView(R.id.spinner_sort_order)
    Spinner spinner_sort_order;

    @BindView(R.id.cb_arts)
    CheckBox cb_arts;

    @BindView(R.id.cb_fashion_style)
    CheckBox cb_fashion_style;

    @BindView(R.id.cb_sports)
    CheckBox cb_sports;

    @BindView(R.id.dp_begin_date)
    DatePicker dp_begin_date;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filter, container, false);
        ButterKnife.bind(this,view);
        // Inflate the layout for this fragment
        //Populate the country spinner
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getContext(),
                R.array.filter_sort_order, R.layout.support_simple_spinner_dropdown_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_sort_order.setAdapter(adapter);

        Bundle bundle = this.getArguments();

        cb_sports.setChecked(bundle.getBoolean("isSports"));
        cb_fashion_style.setChecked(bundle.getBoolean("isFashionStyle"));
        cb_arts.setChecked(bundle.getBoolean("isArts"));
        if (bundle.getString("sortOrder") != null){
            int pos = adapter.getPosition(bundle.getString("sortOrder"));
            spinner_sort_order.setSelection(pos);
        }

        if (bundle.getString("beginDate") != null) {
            try {
                SimpleDateFormat sdf_default = new SimpleDateFormat("yyyyMMdd");
                Date date = sdf_default.parse(bundle.getString("beginDate"));
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(date.getTime());
                dp_begin_date.updateDate(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH));
            } catch (ParseException ex) {
            }
        }
        return view;
    }

    @OnClick(R.id.btn_save)
    public void onSave(View view) {
        EditNameDialogListener listener = (EditNameDialogListener) getActivity();

        Calendar c = Calendar.getInstance();
        c.set(dp_begin_date.getYear(), dp_begin_date.getMonth() , dp_begin_date.getDayOfMonth(), 0, 0);
        SimpleDateFormat sdf_default = new SimpleDateFormat("yyyyMMdd");
        String beginDate = sdf_default.format(c.getTime());
        String sortOrder = spinner_sort_order.getSelectedItem().toString();
        boolean isArts = cb_arts.isChecked();
        boolean isFashionStyle = cb_fashion_style.isChecked();
        boolean isSports = cb_sports.isChecked();

        listener.onFinishEditDialog(beginDate,sortOrder,isArts,isFashionStyle,isSports);
        // Close the dialog and return back to the parent activity
        dismiss();
    }

    // 1. Defines the listener interface with a method passing back data result.
    public interface EditNameDialogListener {
        void onFinishEditDialog(String beginDate, String sortOrder, boolean isArts, boolean isFashionStyle, boolean isSports);
    }


    // Fires whenever the textfield has an action performed
    // In this case, when the "Done" button is pressed
    // REQUIRES a 'soft keyboard' (virtual keyboard)
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (EditorInfo.IME_ACTION_DONE == actionId) {
            // Return input text back to activity through the implemented listener
            EditNameDialogListener listener = (EditNameDialogListener) getActivity();

            String beginDate = dp_begin_date.getYear() + "/" + dp_begin_date.getMonth() + "/" + dp_begin_date.getDayOfMonth();
            String sortOrder = spinner_sort_order.getSelectedItem().toString();
            boolean isArts = cb_arts.isChecked();
            boolean isFashionStyle = cb_fashion_style.isChecked();
            boolean isSports = cb_sports.isChecked();

            listener.onFinishEditDialog(beginDate,sortOrder,isArts,isFashionStyle,isSports);
            // Close the dialog and return back to the parent activity
            dismiss();
            return true;
        }
        return false;
    }


}
