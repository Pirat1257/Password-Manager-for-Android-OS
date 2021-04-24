package com.example.castle;
import android.util.Base64;
import java.security.MessageDigest;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/*--------------------Класс для шифрования и дешифрования данных--------------------*/
/*--------------------Шифрование--------------------*/
public class Crypto {
    // Дополнение в целых байтах.
    // Значение каждого байта равно числу добавленных байтов, то есть добавляется N байт со значением N.
    // Число добавленных байтов зависит от границы блока, до которого необходимо расширить сообщение.
    private static final String ENCRYPTION_ALGORITHM = "AES/ECB/PKCS7Padding";
    public static String encrypt(byte[] key, byte[] clear) throws Exception
    {
        // Класс Java MessageDigest представляет криптографическую хеш-функцию, которая может вычислять дайджест
        // сообщения из двоичных данных. Когда вы получаете набор зашифрованных данных, вы не можете быть уверены
        // в том, что он не был изменен во время транспортировки. Дайджест сообщения помогает решить эту проблему.
        // Чтобы определить, были ли зашифрованные данные модифицированы при транспортировке, отправитель должен рассчитать
        // дайджест сообщения из данных и отправить его вместе с данными. Другая сторона получая зашифрованные данные и
        // дайджест сообщения, может пересчитать дайджест сообщения из данных и проверить, соответствует ли вычисленный
        // дайджест сообщения дайджесту сообщения, полученному с данными. Если два дайджеста сообщения совпадают,
        // существует вероятность того, что зашифрованные данные не были изменены во время транспортировки.
        MessageDigest md = MessageDigest.getInstance("md5"); // md5 - 128-битный алгоритм хеширования, один из серии алгоритмов по построению дайджеста сообщения
        byte[] digestOfPassword = md.digest(key); // Для массива создается дайджест сообщения MessageDigest.
        // Экземпляр секретного ключа SecretKeySpec создается для алгоритма "AES"
        SecretKeySpec skeySpec = new SecretKeySpec(digestOfPassword, "AES");
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec); // Инициализация Cipher выполняется вызовом его метода init
        byte[] encrypted = cipher.doFinal(clear); // Для шифрования и дешифрования данных с помощью экземпляра Cipher, используется один из методов update() или doFinal().
        return Base64.encodeToString(encrypted,Base64.DEFAULT);
    }

    /*--------------------Дешифрование--------------------*/
    public static String decrypt(String key, byte[] encrypted) throws Exception
    {
        MessageDigest md = MessageDigest.getInstance("md5");
        byte[] digestOfPassword = md.digest(key.getBytes("UTF-16LE"));

        SecretKeySpec skeySpec = new SecretKeySpec(digestOfPassword, "AES");
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] decrypted = cipher.doFinal(encrypted);
        return new String(decrypted, "UTF-16LE");
    }
}
