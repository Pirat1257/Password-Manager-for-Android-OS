package com.example.castle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity3 extends AppCompatActivity {

    private EditText resource_text;
    private EditText login_text;
    private EditText pass_text;
    private EditText notes_text;
    private Button save_button;
    private Button cancel_button;
    private String pass;
    private Crypto crypto = new Crypto();
    private AppDatabase db;
    private SomeInfoDao someInfoDao;
    private Intent answerIntent;
    private boolean saved = false;
    int new_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        // Инициализируем базу данных
        db = App.getInstance().getDatabase();
        someInfoDao = db.someInfoDao();
        // Получаем информацию от MainActivity
        answerIntent = new Intent();
        Bundle arguments = getIntent().getExtras();
        pass = arguments.get("password").toString();
        // Привязываем элементы
        resource_text = findViewById(R.id.source_text);
        login_text = findViewById(R.id.login_text);
        pass_text = findViewById(R.id.pass_text);
        notes_text = findViewById(R.id.notes_text);
        save_button = findViewById(R.id.save_button);
        cancel_button = findViewById(R.id.cancel_button);
        // Нажатие на клавишу CANCEL
        cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        // Нажатие на клавишу SAVE
        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Проверка на заполнение необходимых полей
                if (resource_text.getText().toString().isEmpty()) {
                    Toast.makeText(MainActivity3.this, "Resource can't be empty", Toast.LENGTH_LONG).show();
                    return;
                }
                else if (pass_text.getText().toString().isEmpty()) {
                    Toast.makeText(MainActivity3.this, "Password can't be empty", Toast.LENGTH_LONG).show();
                    return;
                }
                SomeInfo new_si = new SomeInfo();
                // Производим подбор id для нового ресурса
                if (saved == false) {
                    new_id = 1;
                    while (true) {
                        if (someInfoDao.getByResource(new_id) == null)
                            break;
                        else new_id++;
                    }
                }
                new_si.id = new_id;
                try {
                    new_si.resource = crypto.encrypt(pass.getBytes("UTF-16LE"), resource_text.getText().toString().getBytes("UTF-16LE"));
                    new_si.login = crypto.encrypt(pass.getBytes("UTF-16LE"), login_text.getText().toString().getBytes("UTF-16LE"));
                    new_si.password = crypto.encrypt(pass.getBytes("UTF-16LE"), pass_text.getText().toString().getBytes("UTF-16LE"));
                    new_si.notes = crypto.encrypt(pass.getBytes("UTF-16LE"), notes_text.getText().toString().getBytes("UTF-16LE"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (saved == false) {
                    saved = true;
                    someInfoDao.insert(new_si);
                }
                else {
                    someInfoDao.update(new_si);
                }
                setResult(RESULT_OK, answerIntent);
                Toast.makeText(MainActivity3.this, "Information saved", Toast.LENGTH_LONG).show();
            }
        });
    }
}