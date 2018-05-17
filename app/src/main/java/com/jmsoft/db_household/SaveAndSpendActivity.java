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
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class SaveAndSpendActivity extends AppCompatActivity
{
    // save_and_spend.xml에 있는 것들.
    TextView sasText, sortResultText;
    Spinner sasSortSpinner;
    ListView sasList;
    Button sasAddBtn, sasBackBtn;

    // DB를 만들기 위한 dbHelper와 SQLiteDatabase 객체 생성
    DBHelper dbHelper;
    SQLiteDatabase sqlDB;

    // 어댑터뷰 xml에 있는 것들.
    TextView dateText, categoryText, contentText, moneyText;

    // 가계부 ArrayList
    ArrayList<HouseHoldData> houseHoldDataArrayList = new ArrayList<>();

    // Adapter
    SortViewAdapter sortViewAdapter;

    // 추가 다이얼로그
    EditText dateAddEdit, contentAddEdit, moneyAddEdit;
    RadioGroup rgGroup;
    View addDialogue;

    // 수정 다이얼로그
    EditText dateModifyEdit, contentModifyEdit, moneyModifyEdit;
    RadioGroup rgGroup1;
    RadioButton saveModifyRb, spendModifyRb;
    View modifyDialogue;

    // Activity 이동하기 위한 intent
    Intent intent;

    // 수입,지출 정렬순으로 볼 지 말지를 판단 할 boolean값
    boolean saved = true;       // true면, 수입 아니면 지출.
    String query;               // query를 전역변수로 두어, 수입 또는 지출에 따라, 다르게 쿼리를 변경.

    // 리스트 클릭인지 롱클릭인지를 판단하여, 둘이 같이 실행을 못하게 막아주는 변수
    boolean updated = true;
    boolean deleted = true;

    // Spinner에 집어넣을 문자열 배열.
    String[] spinName = {"수입 정렬 순", "지출 정렬 순"};

    // 스피너를 사용하기 위한, ArrayAdapter
    ArrayAdapter<String> arrayAdapter = null;

    int save = 0, spend = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_and_spend);
        setTitle("분류 순 보기");
        dbHelper = new DBHelper(this);

        // 아이디 연결
        sasText = findViewById(R.id.sasText);
        sasSortSpinner = findViewById(R.id.sasSortSpinner);
        sasList = findViewById(R.id.sasList);
        sasAddBtn = findViewById(R.id.sasAddBtn);
        sasBackBtn = findViewById(R.id.sasBackBtn);
        sortResultText = findViewById(R.id.sortResultText);

        // arrayAdapter로, 스피너를 초기화.
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, spinName);
        // 스피너에 arrayAdapter를 넣어준다.
        sasSortSpinner.setAdapter(arrayAdapter);

        // 리스너 연결
        sasAddBtn.setOnClickListener(new MyBtnListener());
        sasBackBtn.setOnClickListener(new MyBtnListener());

        sasList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id)
            {
                setModifyDialogue(position);
            }
        });

        sasList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id)
            {
                if(deleted)
                {
                    updated = false;
                    AlertDialog.Builder dlg = new AlertDialog.Builder(SaveAndSpendActivity.this);
                    dlg.setTitle("데이터 삭제");
                    dlg.setIcon(R.drawable.warningicon);
                    dlg.setMessage("데이터를 삭제하시면 되돌릴 수 없습니다.");
                    dlg.setPositiveButton("삭제", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            updated = true;
                            int number = houseHoldDataArrayList.get(position).number;
                            deleteData(number);
                            init();
                            sortViewAdapter.notifyDataSetChanged();
                        }
                    });
                    dlg.setNegativeButton("취소", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            updated = true;
                        }
                    });
                    dlg.show();
                }
                return false;
            }
        });

        sasSortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                // 수입 정렬 순을 클릭하면,
                if(sasSortSpinner.getItemAtPosition(position).toString().equals("수입 정렬 순"))
                {
                    saved = true;
                    init();
                    sortViewAdapter.notifyDataSetChanged();
                }
                else
                {
                    saved = false;
                    init();
                    sortViewAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });

        // 어댑터 생성 및 연결
        sortViewAdapter = new SortViewAdapter(this);
        sasList.setAdapter(sortViewAdapter);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        init();
    }

    public void init()
    {
        save = 0;
        spend = 0;
        houseHoldDataArrayList.clear();
        if(saved)
        {
            query = "select * from housetbl where category = '수입';";
        }
        else if(!saved)
        {
            query = "select * from housetbl where category = '지출';";
        }
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
        if(saved)
        {
            sortResultText.setText("수입 합계 : " + save + "원");
        }
        else if(!saved)
        {
            sortResultText.setText("지출 합계 : " + spend + "원");
        }
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

    public void setUpdateData(String [] str,int number)
    {
        String query = "update housetbl set date = '" + str[0] + "',content = '" + str[1] + "',money = '" + str[2] + "',category = '" + str[3] + "' where number = " + number;
        sqlDB = dbHelper.getWritableDatabase();
        try
        {
            Toast.makeText(getApplicationContext(), "수정 되었습니다.", Toast.LENGTH_SHORT).show();
            sqlDB.execSQL(query);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public void setModifyDialogue(final int position)
    {
        if(updated)
        {
            deleted = false;
            modifyDialogue = (View) View.inflate(SaveAndSpendActivity.this, R.layout.dialogue1, null);
            AlertDialog.Builder dlg = new AlertDialog.Builder(SaveAndSpendActivity.this);
            dlg.setTitle("데이터 수정");
            dlg.setView(modifyDialogue);

            // 아이디 연결
            dateModifyEdit = modifyDialogue.findViewById(R.id.dateModifyEdit);
            contentModifyEdit = modifyDialogue.findViewById(R.id.contentModifyEdit);
            moneyModifyEdit = modifyDialogue.findViewById(R.id.moneyModifyEdit);
            saveModifyRb = modifyDialogue.findViewById(R.id.saveModifyRb);
            spendModifyRb = modifyDialogue.findViewById(R.id.spendModifyRb);


            dateModifyEdit.setText(houseHoldDataArrayList.get(position).date.toString());
            contentModifyEdit.setText(houseHoldDataArrayList.get(position).content.toString());
            moneyModifyEdit.setText(houseHoldDataArrayList.get(position).money.toString());
            if (houseHoldDataArrayList.get(position).category.equals("수입"))
            {
                saveModifyRb.setChecked(true);
                spendModifyRb.setChecked(false);
            }
            else
            {
                saveModifyRb.setChecked(false);
                spendModifyRb.setChecked(true);
            }

            dlg.setPositiveButton("수정", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    int number = houseHoldDataArrayList.get(position).number;
                    String[] str = new String[4];
                    str[0] = dateModifyEdit.getText().toString();
                    str[1] = contentModifyEdit.getText().toString();
                    str[2] = moneyModifyEdit.getText().toString();
                    rgGroup1 = modifyDialogue.findViewById(R.id.rgGroup1);
                    int selected = rgGroup1.getCheckedRadioButtonId();
                    switch (selected)
                    {
                        case R.id.saveModifyRb:
                            str[3] = "수입";
                            break;
                        case R.id.spendModifyRb:
                            str[3] = "지출";
                            break;
                    }
                    deleted = true;
                    setUpdateData(str, number);
                    init();
                    sortViewAdapter.notifyDataSetChanged();
                }
            });
            dlg.setNegativeButton("취소", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    deleted = true;
                }
            });
            dlg.show();
        }
    }

    public void setAddDialogue()
    {
        addDialogue = (View)View.inflate(SaveAndSpendActivity.this,R.layout.dialogue,null);
        AlertDialog.Builder dlg = new AlertDialog.Builder(SaveAndSpendActivity.this);
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
                init();
                sortViewAdapter.notifyDataSetChanged();
            }
        });
        dlg.setNegativeButton("취소",null);
        dlg.show();
    }

    class MyBtnListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v)
        {
            switch(v.getId())
            {
                case R.id.sasAddBtn:
                    setAddDialogue();
                    break;
                case R.id.sasBackBtn:
                    intent = new Intent(getApplicationContext(),NowMonthInputActivity.class);
                    startActivity(intent);
                    finish();
                    break;
            }
        }
    }

    class SortViewAdapter extends BaseAdapter
    {
        Context context;

        public SortViewAdapter(Context context)
        {
            this.context = context;
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
