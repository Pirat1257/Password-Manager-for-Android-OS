package com.example.castle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class MainActivity5 extends AppCompatActivity {

    private EditText resource_text;
    private EditText login_text;
    private EditText pass_text;
    private EditText notes_text;
    private ImageButton copy_button;
    private Button save_button;
    private Button cancel_button;
    private Button delete_button;
    private AppDatabase db;
    private SomeInfoDao someInfoDao;
    private String pass;
    private SomeInfo si;
    private Crypto crypto = new Crypto();
    private Intent answerIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main5);
        // Инициализируем базу данных
        db = App.getInstance().getDatabase();
        someInfoDao = db.someInfoDao();
        // Привязываем элементы
        resource_text = findViewById(R.id.resource_text);
        login_text = findViewById(R.id.login_text);
        pass_text = findViewById(R.id.pass_text);
        notes_text = findViewById(R.id.notes_text);
        copy_button = findViewById(R.id.copy_button);
        save_button = findViewById(R.id.save_button);
        cancel_button = findViewById(R.id.cancel_button);
        delete_button = findViewById(R.id.delete_button);
        // Получаем информацию от MainActivity2
        answerIntent = new Intent();
        Bundle arguments = getIntent().getExtras();
        pass = arguments.get("password").toString();
        si = someInfoDao.getByResource(arguments.getInt("id"));
        // Заполняем поля
        try {
            resource_text.setText(crypto.decrypt(pass, Base64.decode(si.resource.getBytes("UTF-16LE"), Base64.DEFAULT)));
            login_text.setText(crypto.decrypt(pass, Base64.decode(si.login.getBytes("UTF-16LE"), Base64.DEFAULT)));
            pass_text.setText(crypto.decrypt(pass, Base64.decode(si.password.getBytes("UTF-16LE"), Base64.DEFAULT)));
            notes_text.setText(crypto.decrypt(pass, Base64.decode(si.notes.getBytes("UTF-16LE"), Base64.DEFAULT)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Действия при нажатии на кнопку COPY
        copy_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Copied Text", pass_text.getText().toString());
                clipboard.setPrimaryClip(clip);
            }
        });
        // Действия при нажатии на кнопку SAVE
        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (resource_text.getText().toString().isEmpty()) {
                    Toast.makeText(MainActivity5.this, "Resource can't be empty", Toast.LENGTH_LONG).show();
                    return;
                }
                else if (pass_text.getText().toString().isEmpty()) {
                    Toast.makeText(MainActivity5.this, "Password can't be empty", Toast.LENGTH_LONG).show();
                    return;
                }
                try {
                    si.resource = crypto.encrypt(pass.getBytes("UTF-16LE"), resource_text.getText().toString().getBytes("UTF-16LE"));
                    si.login = crypto.encrypt(pass.getBytes("UTF-16LE"), login_text.getText().toString().getBytes("UTF-16LE"));
                    si.password = crypto.encrypt(pass.getBytes("UTF-16LE"), pass_text.getText().toString().getBytes("UTF-16LE"));
                    si.notes = crypto.encrypt(pass.getBytes("UTF-16LE"), notes_text.getText().toString().getBytes("UTF-16LE"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                someInfoDao.update(si);
                setResult(RESULT_OK, answerIntent);
                Toast.makeText(MainActivity5.this, "Information saved", Toast.LENGTH_LONG).show();
            }
        });
        // Действия при нажатии на кнопку CANCEL
        cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        // Действия при нажатии на кнопку DELETE
        delete_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = si.id;
                someInfoDao.delete(si);
                setResult(RESULT_OK, answerIntent);
                finish();
            }
        });
    }
}