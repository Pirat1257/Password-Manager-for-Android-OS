package com.example.castle;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.util.Iterator;
import java.util.List;

public class MainActivity4 extends AppCompatActivity {

    private EditText old_pass_text;
    private EditText new_pass_text;
    private Button save_button;
    private Button cancel_button;
    private String pass;
    private Crypto crypto = new Crypto();
    private AppDatabase db;
    private SomeInfoDao someInfoDao;
    private List<SomeInfo> si_list; // Лист бд
    private Iterator<SomeInfo> it; // Итератор для работы с листом бд
    private Intent answerIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);
        // Инициализируем базу данных
        db = App.getInstance().getDatabase();
        someInfoDao = db.someInfoDao();
        // Привязываем элементы
        old_pass_text = findViewById(R.id.old_pass_text);
        new_pass_text = findViewById(R.id.new_pass_text);
        save_button = findViewById(R.id.save_button);
        cancel_button = findViewById(R.id.cancel_button);
        // Получаем информацию от MainActivity
        answerIntent = new Intent();
        Bundle arguments = getIntent().getExtras();
        pass = arguments.get("old_pass").toString();
        // Действия при нажатии на клавишу SAVE
        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (old_pass_text.getText().toString().isEmpty())
                {
                    Toast.makeText(MainActivity4.this, "Incorrect old password", Toast.LENGTH_LONG).show();
                    return;
                }
                else if (old_pass_text.getText().toString().compareTo(pass) != 0)
                {
                    old_pass_text.setText("");
                    Toast.makeText(MainActivity4.this, "Incorrect old password", Toast.LENGTH_LONG).show();
                    return;
                }
                else if (new_pass_text.getText().toString().isEmpty())
                {
                    Toast.makeText(MainActivity4.this, "Incorrect new password", Toast.LENGTH_LONG).show();
                    return;
                }
                else if (new_pass_text.getText().toString().compareTo(old_pass_text.getText().toString()) == 0)
                {
                    new_pass_text.setText("");
                    Toast.makeText(MainActivity4.this, "Incorrect new password", Toast.LENGTH_LONG).show();
                    return;
                }
                // Прошли через все проверки
                si_list = someInfoDao.getAll();
                it = si_list.iterator();
                SomeInfo si = null;
                // По одному обновляем узлы
                while (it.hasNext()) {
                    si = it.next();
                    try {
                        si.resource = crypto.encrypt(new_pass_text.getText().toString().getBytes("UTF-16LE"),
                                crypto.decrypt(old_pass_text.getText().toString(),
                                        Base64.decode(si.resource.getBytes("UTF-16LE"),
                                                Base64.DEFAULT)).getBytes("UTF-16LE"));
                        si.login = crypto.encrypt(new_pass_text.getText().toString().getBytes("UTF-16LE"),
                                crypto.decrypt(old_pass_text.getText().toString(),
                                        Base64.decode(si.login.getBytes("UTF-16LE"),
                                                Base64.DEFAULT)).getBytes("UTF-16LE"));
                        si.password = crypto.encrypt(new_pass_text.getText().toString().getBytes("UTF-16LE"),
                                crypto.decrypt(old_pass_text.getText().toString(),
                                        Base64.decode(si.password.getBytes("UTF-16LE"),
                                                Base64.DEFAULT)).getBytes("UTF-16LE"));
                        si.notes = crypto.encrypt(new_pass_text.getText().toString().getBytes("UTF-16LE"),
                                crypto.decrypt(old_pass_text.getText().toString(),
                                        Base64.decode(si.notes.getBytes("UTF-16LE"),
                                                Base64.DEFAULT)).getBytes("UTF-16LE"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    someInfoDao.update(si);
                }
                answerIntent.putExtra("new_pass", new_pass_text.getText().toString());
                setResult(RESULT_OK, answerIntent);
                finish();
                return;
            }
        });
        // Действия при нажатии на кнопку CANCEL
        cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED, answerIntent);
                finish();
                return;
            }
        });
    }
}