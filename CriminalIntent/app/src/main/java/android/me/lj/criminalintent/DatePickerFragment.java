package android.me.lj.criminalintent;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by Administrator on 2018/3/7.
 */

public class DatePickerFragment extends DialogFragment {

    public static final String EXTRA_DATE = "android.me.lj.criminalintent.date";
    private static final String ARG_DATE = "date";

    private DatePicker mDatePicker;

    public static DatePickerFragment newInstance(Date date) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_DATE, date);

        DatePickerFragment fragment = new DatePickerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Date date = (Date) getArguments().getSerializable(ARG_DATE);

        /**
         * DatePickerFragment使用Date中的信息来初始化DatePicker对象。
         * 然而， DatePicker对象的初始化需整数形式的月、日、年。 Date是时间戳，无法直接提供整数。
         * 要达到目的，必须首先创建一个Calendar对象，然后用Date对象配置它，再从Calendar对象中取回所需信息。
         */
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);


        /**
         * 采用以下代码也能创建DatePicker对象，为何还要费事地定义XML布局文件，再去实例化视图对象呢？

             DatePicker datePicker = new DatePicker(getActivity());
             return new AlertDialog.Builder(getActivity())
                        .setView(datePicker)
                        ...
                        .create();
         *
         * 这是因为，想调整对话框的显示内容时，直接修改布局文件会更容易些。
         * 例如，如果想在对话框的DatePicker旁再添加一个TimePicker，只需更新布局文件就能显示新视图。
         *
         * 即使设备旋转，用户所选日期也都会得到保留（试试看）。这是如何做到的呢？
         * 这是因为，设备配置改变时，具有ID属性的视图可以保存运行状态；而我们以dialog_date.xml布局创建DatePicker时，编译工具已为DatePicker生成了唯一的ID。
         * 如果以代码的方式创建DatePicker，想看到同样的效果，需要为其设置ID属性。
         */


        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_date, null);

        mDatePicker = view.findViewById(R.id.dialog_date_picker);
        mDatePicker.init(year, month, day, null);

        /**
         * android.support.v7.app.AlertDialog
         */
        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle(R.string.date_picker_title)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        int year = mDatePicker.getYear();
                        int month = mDatePicker.getMonth();
                        int day = mDatePicker.getDayOfMonth();
                        Date date = new GregorianCalendar(year, month, day).getTime();
                        sendResult(Activity.RESULT_OK, date);
                    }
                })
                .create();
        /**
         * 建议将AlertDialog封装在DialogFragment（Fragment的子类）实例中使用。
         * 当然，不使用DialogFragment也可显示AlertDialog视图，但不推荐这样做。
         * 使用FragmentManager管理对话框，可以更灵活地显示对话框。
         * 另外，如果旋转设备，单独使用的AlertDialog会消失，而封装在fragment中的AlertDialog则不会有此问题（旋转后，对话框会被重建恢复）。
         */
    }

    private void sendResult(int resultCode, Date date) {
        if (getTargetFragment() == null) {
            return;
        }
        Intent intent = new Intent();
        intent.putExtra(EXTRA_DATE, date);
        /**
         * 目标fragment和请求代码由FragmentManager负责跟踪管理，
         * 我们可调用fragment（设置目标fragment的fragment）的getTargetFragment()方法和getTargetRequestCode()方法获取它们
         */
        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
    }
}
