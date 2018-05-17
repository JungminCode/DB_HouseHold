package com.jmsoft.db_household;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
{
    // 아아디와 패스워드 임의로 만들어 놓은 것.
    String id = "abc";
    String password = "1234";

    EditText idEdit,passEdit;
    Button loginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // EditText와 버튼 아이디 넣기
        idEdit = findViewById(R.id.idEdit);
        passEdit = findViewById(R.id.passEdit);
        loginBtn = findViewById(R.id.loginBtn);


        // 로그인 버튼을 누를 때,
        loginBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // EditText의 문자열을 집어넣을 배열 준비.
                String [] str = new String[2];
                str[0] = idEdit.getText().toString();           // 아이디 EditText 문자열
                str[1] = passEdit.getText().toString();         // 패스워드 EditText 문자열

                if(!str[0].equals(id))
                {
                    Toast.makeText(getApplicationContext(),"아이디를 확인해주세요.",Toast.LENGTH_SHORT).show();
                    return;
                }
                else
                {
                    if(str[1].equals(password))
                    {
                        Toast.makeText(getApplicationContext(), id + "님 안녕하세요.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(),NowMonthInputActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(),"패스워드를 확인해주세요.",Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
        });
    }
}
