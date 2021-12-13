package ru.pavel.zip;

import ru.pavel.config.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipMaker {

    /**
     * Метод для архивации файлов в ru.pavel.zip
     */
    public void makeZip(File file) {

        try (ZipOutputStream zout = new ZipOutputStream(
                new FileOutputStream(Constants.ZIP + ".ru.pavel.zip"));
             FileInputStream fis = new FileInputStream(file);
             FileInputStream fisSig = new FileInputStream(Constants.SIGNATURE_FILE +".xml.sig")) {
            ZipEntry entry1 = new ZipEntry("filename.xml");
            zout.putNextEntry(entry1);
            // считываем содержимое файла в массив byte
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            // добавляем содержимое к архиву
            zout.write(buffer);
            // закрываем текущую запись для новой записи
            zout.closeEntry();
            ZipEntry entry2 = new ZipEntry("filename.xml.sig");
            zout.putNextEntry(entry2);
            // считываем содержимое файла в массив byte
            byte[] buffer2 = new byte[fisSig.available()];
            fisSig.read(buffer2);
            zout.write(buffer2);
            zout.closeEntry();

            fis.close();
            fisSig.close();
            zout.close();

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

    }
}
