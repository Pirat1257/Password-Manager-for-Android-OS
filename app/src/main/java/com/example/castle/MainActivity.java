package com.example.castle;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Button ok_button;
    private EditText pass;
    private AppDatabase db;
    private SomeInfoDao someInfoDao;
    private Crypto crypto = new Crypto();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Инициализируем базу данных
        db = App.getInstance().getDatabase();
        someInfoDao = db.someInfoDao();
        // Привязываем элементы
        ok_button = findViewById(R.id.ok_button);
        pass = findViewById(R.id.pass_EditText);
        // Обработка нажатия кнопки OK
        ok_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Берем из базы данных элемент с id равный 0 (проверочный)
                SomeInfo si_head = someInfoDao.getByResource(0);
                // Если элемента не существует, значит и базы нет и вообще это первый запуск
                if (si_head == null) {
                    // Создаем проверочный элемент
                    si_head = new SomeInfo();
                    si_head.id = 0;
                    // Шифрование
                    try {
                        si_head.resource = crypto.encrypt(pass.getText().toString().getBytes("UTF-16LE"), ("Check_resource").getBytes("UTF-16LE"));
                        si_head.login = crypto.encrypt(pass.getText().toString().getBytes("UTF-16LE"), ("Check_login").getBytes("UTF-16LE"));
                        si_head.password = crypto.encrypt(pass.getText().toString().getBytes("UTF-16LE"), ("Check_password").getBytes("UTF-16LE"));
                        si_head.notes = crypto.encrypt(pass.getText().toString().getBytes("UTF-16LE"), ("Check_notes").getBytes("UTF-16LE"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    // Добавляем в базу проверочный элемент
                    someInfoDao.insert(si_head);
                    // Вызываем новое активити и передаем ему пароль
                    Intent intent = new Intent(MainActivity.this, MainActivity2.class);
                    intent.putExtra("password", pass.getText().toString());
                    startActivity(intent);
                    finish();
                    return;
                }
                // В противном случае база есть, пытаемся дешифровать проверочный элемент полученным ключем
                else {
                    try {
                        // Если все данные совпали, создаем новое активити
                        if (crypto.decrypt(pass.getText().toString(), Base64.decode(si_head.resource.getBytes("UTF-16LE"), Base64.DEFAULT)).compareTo("Check_resource") == 0) {
                            if (crypto.decrypt(pass.getText().toString(), Base64.decode(si_head.login.getBytes("UTF-16LE"), Base64.DEFAULT)).compareTo("Check_login") == 0) {
                                if (crypto.decrypt(pass.getText().toString(), Base64.decode(si_head.password.getBytes("UTF-16LE"), Base64.DEFAULT)).compareTo("Check_password") == 0) {
                                    if (crypto.decrypt(pass.getText().toString(), Base64.decode(si_head.notes.getBytes("UTF-16LE"), Base64.DEFAULT)).compareTo("Check_notes") == 0) {
                                        // Вызываем новое активити и передаем ему пароль
                                        Intent intent = new Intent(MainActivity.this, MainActivity2.class);
                                        intent.putExtra("password", pass.getText().toString());
                                        startActivity(intent);
                                        finish();
                                        return;
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    pass.setText(""); // Стираем неправильный пароль
                    Toast.makeText(MainActivity.this, "Incorrect password", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}