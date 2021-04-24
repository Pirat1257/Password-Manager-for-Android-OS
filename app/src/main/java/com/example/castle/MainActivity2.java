package com.example.castle;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainActivity2 extends AppCompatActivity {

    private Button add_button;
    private Button change_pass_button;
    private Button save_button;
    private Button download_button;
    private ListView sources;
    private AppDatabase db;
    private SomeInfoDao someInfoDao;
    private Crypto crypto = new Crypto();
    private ArrayList<String> array_list; // Для вывода названий
    private ArrayAdapter adapter; // Для строк
    private List<SomeInfo> si_list; // Лист бд
    private Iterator<SomeInfo> it; // Итератор для работы с листом бд
    private String pass; // Пароль
    static final private int CHANGED = 0; // Для обработки ответов других активити
    static final private int NEW_PASS = 1;
    private String DB_NAME; // Полный путь до БД
    private String BACKUP_NAME; // Полный путь до бэкапа БД


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        // Инициализируем базу данных
        db = App.getInstance().getDatabase();
        someInfoDao = db.someInfoDao();
        // Создаем новую папку для хранения бэкапа базы данных
        // С помощью выражения Environment.getExternalStorageDirectory() получаем доступ к папке приложения во внешнем хранилище и устанавливаем объект файла:
        File direct = new File(Environment.getExternalStorageDirectory(), "BackupFolder");
        if (!direct.exists())
        {
            if (direct.mkdirs())
            {
                Toast.makeText(MainActivity2.this, "Directory is created", Toast.LENGTH_LONG).show();
            }
        }
        BACKUP_NAME = direct.getAbsolutePath().toString() + "/database";
        DB_NAME = Environment.getDataDirectory().toString() + "/data/com.example.castle/databases/database";
        // Привязываем элементы
        add_button = findViewById(R.id.add_button);
        change_pass_button = findViewById(R.id.change_pass_button);
        save_button = findViewById(R.id.save_button);
        download_button = findViewById(R.id.download_button);
        sources = findViewById(R.id.sources);
        array_list = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, array_list);
        sources.setAdapter(adapter);
        // Получаем информацию от MainActivity
        Bundle arguments = getIntent().getExtras();
        pass = arguments.get("password").toString();
        update_sources();
        // Действия при нажатии на кнопку ADD
        add_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                si_list = someInfoDao.getAll();
                Intent add_elem = new Intent(MainActivity2.this, MainActivity3.class);
                add_elem.putExtra("password", pass);
                startActivityForResult(add_elem, CHANGED);
            }
        });
        // Действие при нажатии на кнопку CHANGE PASSWORD
        change_pass_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent change_pass = new Intent(MainActivity2.this, MainActivity4.class);
                change_pass.putExtra("old_pass", pass);
                startActivityForResult(change_pass, NEW_PASS);
            }
        });
        // Действие при нажатии на кнопку SAVE
        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    exportDatabase();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        // Действие при нажатии на кнопку DOWNLOAD
        download_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    importDatabase();
                    Intent enter_pass = new Intent(MainActivity2.this, MainActivity.class);
                    db.close();
                    startActivity(enter_pass);
                    finish();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                update_sources();
            }
        });
        // Действие при нажатии на один из ресурсов
        sources.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent selected_source = new Intent(MainActivity2.this, MainActivity5.class);
                // Производим поиск id выбранного ресурса
                si_list = someInfoDao.getAll();
                it = si_list.iterator();
                SomeInfo si = it.next(); // Сразу пропускаем проверочный элемент
                int count = 0;
                // Проходим по всем элементам списка
                while (it.hasNext()) {
                    si = it.next();
                    if (count == position)
                        break;
                    else
                        count++;
                }
                selected_source.putExtra("id", si.id);
                selected_source.putExtra("password", pass);
                startActivityForResult(selected_source, CHANGED);
            }
        });
    }

    // Импорт базы данных
    public void importDatabase() throws IOException {
        // Закрытие базы данных ОБЯЗАТЕЛЬНО!!!, т.к. она не обновляется динамически во время работы и все изменения сохраняются только после заркрытия программы или же операции close()
        db.close();
        File backup_db = new File(BACKUP_NAME); // Файл бэкапа
        File actual_db = new File(DB_NAME); // Файл актульной базы данных
        // Проверяем на существование бэкапа
        if (backup_db.exists()) {
            // Копируем
            copyFile(new FileInputStream(backup_db), new FileOutputStream(actual_db));
            Toast.makeText(MainActivity2.this, "Database imported", Toast.LENGTH_LONG).show();
            return;
        }
        return;
    }

    // Экспорт базы данных
    public void exportDatabase() throws IOException {
        // Закрываем базу
        db.close();
        File actual_db = new File(DB_NAME);
        File backup_db = new File(BACKUP_NAME);
        // Проверяем на существование актуальной БД
        if (actual_db.exists()) {
            // Копируем
            copyFile(new FileInputStream(actual_db), new FileOutputStream(backup_db));
            // Получем ресурсы для работы с базой данных снова
            App.getInstance().update();
            db = App.getInstance().getDatabase();
            someInfoDao = db.someInfoDao();
            // Обновляем список
            update_sources();
            Toast.makeText(MainActivity2.this, "Database exported", Toast.LENGTH_LONG).show();
            return;
        }
        return;
    }

    // Копирование файлов
    public static void copyFile(FileInputStream fromFile, FileOutputStream toFile) throws IOException {
        FileChannel fromChannel = null;
        FileChannel toChannel = null;
        try {
            fromChannel = fromFile.getChannel();
            toChannel = toFile.getChannel();
            fromChannel.transferTo(0, fromChannel.size(), toChannel);
        } finally {
            try {
                if (fromChannel != null) {
                    fromChannel.close();
                }
            } finally {
                if (toChannel != null) {
                    toChannel.close();
                }
            }
        }
    }

    // Обработка результата от активити
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // В случае какого либо изменения бд
        if (requestCode == CHANGED) {
            if (resultCode == RESULT_OK) {
                update_sources();
            }
        }
        // В случае изменения пароля
        else if (requestCode == NEW_PASS) {
            if (resultCode == RESULT_OK) {
                pass = data.getStringExtra("new_pass");
                // update_sources();
                Toast.makeText(MainActivity2.this, "Password updated", Toast.LENGTH_LONG).show();
            }
        }
    }

    // Обновление списка источников
    private void update_sources() {
        if (si_list != null)
            si_list.clear();
        if (array_list != null)
            array_list.clear();
        si_list = someInfoDao.getAll();
        it = si_list.iterator();
        SomeInfo si = null;
        // Проходим по всем элементам списка
        while (it.hasNext()) {
            si = it.next();
            // Проверочный блок не добавляется
            if (si.id != 0) {
                try {
                    array_list.add(crypto.decrypt(pass, Base64.decode(si.resource.getBytes("UTF-16LE"), Base64.DEFAULT)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // Обновляем список
            adapter.notifyDataSetChanged();
        }
    }
}