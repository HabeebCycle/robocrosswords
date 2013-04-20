package com.adamrosenfield.wordswithcrosses;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.Spinner;
import android.widget.TextView;

import com.adamrosenfield.wordswithcrosses.BrowseActivity.Provider;
import com.adamrosenfield.wordswithcrosses.net.Downloader;
import com.adamrosenfield.wordswithcrosses.net.Downloaders;
import com.adamrosenfield.wordswithcrosses.net.DummyDownloader;
import com.adamrosenfield.wordswithcrosses.view.DownloadPickerView;
import com.adamrosenfield.wordswithcrosses.wordswithcrosses.R;


/**
 * Custom dialog for choosing puzzles to download.
 */
public class DownloadPickerDialogBuilder {
    private static DateFormat df = new SimpleDateFormat("EEEE");
    private Activity mActivity;
    private Dialog mDialog;
    private List<Downloader> mAvailableDownloaders;
    private OnDateChangedListener dateChangedListener = new DatePicker.OnDateChangedListener() {
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                mYear = year;
                mMonthOfYear = monthOfYear;
                mDayOfMonth = dayOfMonth;
                updateDateLabel();
                updatePuzzleSelect();
            }
        };

    private Provider<Downloaders> mDownloaders;
    private Spinner mPuzzleSelect;
    private TextView mDateLabel;
    private int mDayOfMonth;
    private int mMonthOfYear;
    private int mYear;

    public DownloadPickerDialogBuilder(Activity a, final OnDownloadSelectedListener downloadButtonListener, int year,
        int monthOfYear, int dayOfMonth, Provider<Downloaders> provider) {
        mActivity = a;

        mYear = year;
        mMonthOfYear = monthOfYear;
        mDayOfMonth = dayOfMonth;

        mDownloaders = provider;

        LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        DownloadPickerView layout = (DownloadPickerView) inflater.inflate(R.layout.download_dialog, (ViewGroup) mActivity.findViewById(R.id.download_root));

        mDateLabel = (TextView) layout.findViewById(R.id.dateLabel);
        updateDateLabel();

        DatePicker datePicker = (DatePicker) layout.findViewById(R.id.datePicker);
        datePicker.init(year, monthOfYear, dayOfMonth, dateChangedListener);

        mPuzzleSelect = (Spinner) layout.findViewById(R.id.puzzleSelect);
        updatePuzzleSelect();

        OnClickListener clickHandler = new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    downloadButtonListener.onDownloadSelected(getCurrentDate(), mAvailableDownloaders,
                        mPuzzleSelect.getSelectedItemPosition());
                }
            };

        ((Button) layout.findViewById(R.id.browse)).setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent i = new Intent();
                    i.setClass(mActivity, WebBrowserActivity.class);
                    mActivity.startActivityForResult(i, 0);
                }
            });
        
        layout.setDownloadPickerDialogBuilder(this);

        AlertDialog.Builder builder = (new AlertDialog.Builder(mActivity)).setPositiveButton("Download", clickHandler)
                                       .setNegativeButton("Cancel", null)
                                       .setTitle("Download Puzzles");

        builder.setView(layout);
        mDialog = builder.create();
    }

    public Dialog getInstance() {
        return mDialog;
    }

    private Date getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(mYear - 1900, mMonthOfYear, mDayOfMonth);
        return calendar.getTime();
    }

    private void updateDateLabel() {
        mDateLabel.setText(df.format(getCurrentDate()));
    }

    private void updatePuzzleSelect() {
        mAvailableDownloaders = mDownloaders.get()
                                            .getDownloaders(getCurrentDate());
        mAvailableDownloaders.add(0, new DummyDownloader());

        ArrayAdapter<Downloader> adapter = new ArrayAdapter<Downloader>(mActivity,
                android.R.layout.simple_spinner_item, mAvailableDownloaders);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mPuzzleSelect.setAdapter(adapter);
    }
    
    public void onViewLayout() {
        updateDateLabel();
        updatePuzzleSelect();
    }

    public interface OnDownloadSelectedListener {
        void onDownloadSelected(Date date, List<Downloader> availableDownloaders, int selected);
    }
}
