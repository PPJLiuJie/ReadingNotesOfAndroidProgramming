package android.me.lj.criminalintent;

import android.app.Activity;
import android.content.Intent;
import android.me.lj.criminalintent.utils.DateFormatUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import java.util.Date;
import java.util.UUID;

import static android.widget.CompoundButton.*;

/**
 * Created by Administrator on 2018/3/5.
 */

public class CrimeFragment extends Fragment {

    private Crime mCrime;

    private EditText mTitleField;
    private Button mDateButton;
    private CheckBox mSolvedCheckBox;

    private static final String ARG_CRIME_ID = "crime_id";
    private static final String DIALOG_DATE = "DialogDate";

    private static final int REQUEST_DATE = 0;

    public static CrimeFragment newInstance(UUID crimeId) {

        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);

        CrimeFragment fragment = new CrimeFragment();

        /**
         * 为什么要使用fragment argument？为什么不直接在CrimeFragment里创建一个实例变量呢？
         *
         * 创建实例变量的方式并不可靠。这是因为，在操作系统重建fragment时（设备配置发生改变）用户暂时离开当前应用（操作系统按需回收内存），
         * 任何实例变量都将不复存在。尤其是内存不够，操作系统强制杀掉应用的情况，可以说是无人能挡。
         * 因此，可以说，fragment argument就是为应对上述场景而生。
         */
        fragment.setArguments(args);

        return fragment;
    }


    /**
     * Fragment.onCreate(Bundle)是公共方法，而Activity.onCreate(Bundle)是受保
     * 护方法。 Fragment.onCreate(Bundle)方法及其他Fragment生命周期方法必须是公共方法，因
     * 为托管fragment的activity要调用它们。
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.getInstance(getActivity()).getCrime(crimeId);
    }

    @Override
    public void onPause() {
        super.onPause();
        /**
         * 修改完成后，你需要刷新CrimeLab中的Crime数据。这可以通过覆盖CrimeFragment.onPause()方法完成
         */
        CrimeLab.getInstance(getActivity()).updateCrime(mCrime);
    }

    /**
     * fragment的视图并没有在Fragment.onCreate(Bundle)方法中生成。虽然我们在该方
     * 法中配置了fragment实例，但创建和配置fragment视图是另一个Fragment生命周期方法onCreateView完成的
     * <p>
     * 该 方 法 实 例 化 fragment 视 图 的 布 局 ， 然 后 将 实 例 化 的 View 返 回 给 托 管 activity 。
     * LayoutInflater及ViewGroup是实例化布局的必要参数。 Bundle用来存储恢复数据，可供该方
     * 法从保存状态下重建视图。
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        /**
         * 参数解析:
         * 第一个参数：略
         * 第二个参数：视图的父视图，我们通常需要父视图来正确配置组件。
         * 第三个参数：告诉布局生成器是否将生成的视图添加给父视图。这里，传入了false参数，因为我们将以代码的方式添加视图。
         */
        View view = inflater.inflate(R.layout.fragment_crime, container, false);

        mTitleField = view.findViewById(R.id.crime_title);
        mTitleField.setText(mCrime.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mCrime.setTitle(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mDateButton = view.findViewById(R.id.crime_date);
        updateDate();
        mDateButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(mCrime.getDate());

                /**
                 * 设置目标fragment
                 * 该方法有两个参数：目标fragment以及类似于传入startActivityForResult(...)方法的请求代码。
                 * 需要时，目标fragment使用请求代码确认是哪个fragment在回传数据。
                 */
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);

                /**
                 * DialogFragment实例也是由托管activity的FragmentManager管理的。
                 *
                 * 要将DialogFragment添加给FragmentManager管理并放置到屏幕上，可调用fragment实例的以下方法：
                 *      public void show(FragmentManager manager, String tag)
                 *      public void show(FragmentTransaction transaction, String tag)
                 *
                 * String参数可唯一识别FragmentManager队列中的DialogFragment。
                 *
                 * 两个方法都可以：
                 *      如果传入FragmentTransaction参数，你自己负责创建并提交事务；
                 *      如果传入FragmentManager参数，系统会自动创建并提交事务。
                 */
                dialog.show(fragmentManager, DIALOG_DATE);
            }
        });

        mSolvedCheckBox = view.findViewById(R.id.crime_solved);
        mSolvedCheckBox.setChecked(mCrime.isSolved());
        mSolvedCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                mCrime.setSolved(isChecked);
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_DATE) {
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            updateDate();
        }
    }

    private void updateDate() {
        mDateButton.setText(DateFormatUtil.format(mCrime.getDate()));
    }
}
