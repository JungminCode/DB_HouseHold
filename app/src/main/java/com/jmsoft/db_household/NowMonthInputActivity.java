package com.jmsoft.db_household;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

public class NowMonthInputActivity extends AppCompatActivity
{
    // now_month_input.xml에 있는 것들
    TextView nowDateText,nowResultText;
    ListView list1;
    Button allBtn, sortBtn, addBtn;

    // DB를 만들기 위한 dbHelper와 SQLiteDatabase 객체 생성
    DBHelper dbHelper;
    SQLiteDatabase sqlDB;

    // 현재 날짜를 저장하기 위한 저장 변수
    int nowYear, nowMonth, nowDay;

    // 어댑터뷰 xml에 있는 것들.
    TextView dateText, categoryText, contentText, moneyText;

    // 가계부 ArrayList
    ArrayList<HouseHoldData> houseHoldDataArrayList = new ArrayList<>();

    // Adapter
    NowMonthViewAdapter nowMonthViewAdapter;

    // 시스템에서의 년+월을 비교하기 위한 String문
    String dbDate;

    // 추가 다이얼로그
    EditText dateAddEdit,contentAddEdit,moneyAddEdit;
    RadioGroup rgGroup;
    View addDialogue;

    // Activity 이동하기 위한 intent
    Intent intent;

    // 월의 합계를 찾기 위한 변수
    int save = 0, spend = 0, sum = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_now_month_input);

        // 초기화
        dbHelper = new DBHelper(this);
        nowDateText = findViewById(R.id.nowDateText);
        nowResultText = findViewById(R.id.nowResultText);
        list1 = findViewById(R.id.list1);
        allBtn = findViewById(R.id.allBtn);
        sortBtn = findViewById(R.id.sortBtn);
        addBtn = findViewById(R.id.addBtn);

        Calendar calendar = Calendar.getInstance();
        nowYear = calendar.get(Calendar.YEAR);
        nowMonth = calendar.get(Calendar.MONTH);
        nowDay = calendar.get(Calendar.DAY_OF_MONTH);
        setTitle(nowYear + "년 " + (nowMonth + 1) + "월 " + "내역 보기");

        nowDateText.setText(nowYear + "년 " + (nowMonth + 1) + "월 " + "내역");
        dbDate = nowYear + "-" + (nowMonth+1);

        // 버튼 리스너 넣기
        allBtn.setOnClickListener(new MyBtnListener());
        sortBtn.setOnClickListener(new MyBtnListener());
        addBtn.setOnClickListener(new MyBtnListener());

        list1.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id)
            {
                AlertDialog.Builder dlg = new AlertDialog.Builder(NowMonthInputActivity.this);
                dlg.setIcon(R.drawable.warningicon);
                dlg.setTitle("데이터 삭제");
                dlg.setMessage("데이터를 삭제하시면 되돌릴 수 없습니다.");
                dlg.setPositiveButton("삭제", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        int number = houseHoldDataArrayList.get(position).number;
                        deleteData(number);
                        init(dbDate);
                        nowMonthViewAdapter.notifyDataSetChanged();
                    }
                });
                dlg.setNegativeButton("취소",null);
                dlg.show();
                return false;
            }
        });

        nowMonthViewAdapter = new NowMonthViewAdapter(this);
        list1.setAdapter(nowMonthViewAdapter);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        init(dbDate);
    }

    public void deleteData(int number)
    {
        String query = "delete from housetbl where number = " + number + ";";
        sqlDB = dbHelper.getWritableDatabase();
        try
        {
            Toast.makeText(getApplicationContext(),"삭제되었습니다.",Toast.LENGTH_SHORT).show();
            sqlDB.execSQL(query);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public void init(String date)
    {
        save = 0;
        spend = 0;
        sum = 0;
        houseHoldDataArrayList.clear();
        String query = "select * from housetbl where date like '" + date + "%';";
        sqlDB = dbHelper.getReadableDatabase();
        Cursor cursor;
        cursor = sqlDB.rawQuery(query,null);
        while(cursor.moveToNext())
        {
            HouseHoldData hhd = new HouseHoldData(cursor.getInt(0),cursor.getString(1),cursor.getString(2),cursor.getString(3),cursor.getString(4));
            if(hhd.category.equals("수입"))
            {
                save += Integer.parseInt(hhd.money);
            }
            else
            {
                spend += Integer.parseInt(hhd.money);
            }
            houseHoldDataArrayList.add(hhd);
        }
        sum = save - spend;
        nowResultText.setText(nowYear + "년 " + (nowMonth+1) + "월의 합계 : " + sum + "원");
    }

    public void insertData(String [] str)
    {
        String query = "insert into housetbl values (null,'" + str[0] + "','" + str[1] + "','" + str[2] + "','" + str[3] + "');";
        sqlDB = dbHelper.getWritableDatabase();
        try
        {
            Toast.makeText(getApplicationContext(),"입력되었습니다.",Toast.LENGTH_SHORT).show();
            sqlDB.execSQL(query);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    class MyBtnListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v)
        {
            switch (v.getId())
            {
                case R.id.allBtn:
                    intent = new Intent(getApplicationContext(),AllContentOutputActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                case R.id.sortBtn:
                    intent = new Intent(getApplicationContext(),SaveAndSpendActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                case R.id.addBtn:
                    setAddDialogue();
                    break;
            }
        }
    }

    public void setAddDialogue()
    {
        addDialogue = (View)View.inflate(NowMonthInputActivity.this,R.layout.dialogue,null);
        AlertDialog.Builder dlg = new AlertDialog.Builder(NowMonthInputActivity.this);
        dlg.setTitle("데이터 입력");
        dlg.setView(addDialogue);
        dlg.setPositiveButton("확인", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                String [] str = new String[4];
                dateAddEdit = addDialogue.findViewById(R.id.dateAddEdit);
                contentAddEdit = addDialogue.findViewById(R.id.contentAddEdit);
                moneyAddEdit = addDialogue.findViewById(R.id.moneyAddEdit);
                rgGroup = addDialogue.findViewById(R.id.rgGroup);
                int selectRadioId = rgGroup.getCheckedRadioButtonId();
                str[0] = dateAddEdit.getText().toString();
                str[1] = contentAddEdit.getText().toString();
                str[2] = moneyAddEdit.getText().toString();
                switch(selectRadioId)
                {
                    case R.id.saveAddRb:
                        str[3] = "수입";
                        break;
                    case R.id.spendAddRb:
                        str[3] = "지출";
                        break;
                }
                insertData(str);
                init(dbDate);
                nowMonthViewAdapter.notifyDataSetChanged();
            }
        });
        dlg.setNegativeButton("취소",null);
        dlg.show();
    }

    class NowMonthViewAdapter extends BaseAdapter
    {
        Context context;

        NowMonthViewAdapter(Context con)
        {
            context = con;
        }

        @Override
        public int getCount()
        {
            return houseHoldDataArrayList.size();
        }

        @Override
        public Object getItem(int position)
        {
            return null;
        }

        @Override
        public long getItemId(int position)
        {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            if (convertView == null)
            {
                LayoutInflater inflater = LayoutInflater.from(context);
                convertView = inflater.inflate(R.layout.content, parent, false);
            }

            dateText = (TextView) convertView.findViewById(R.id.dateText);
            categoryText = (TextView) convertView.findViewById(R.id.categoryText);
            contentText = (TextView) convertView.findViewById(R.id.contentText);
            moneyText = (TextView) convertView.findViewById(R.id.moneyText);

            HouseHoldData hhd = houseHoldDataArrayList.get(position);
            dateText.setText(hhd.date);
            categoryText.setText(hhd.category);
            contentText.setText(hhd.content);
            moneyText.setText(hhd.money);

            return convertView;
        }
    }
}
