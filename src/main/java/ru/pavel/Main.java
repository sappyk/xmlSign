package ru.pavel;

import ru.pavel.config.Constants;
import ru.pavel.signature.AttachmentSignature;
import ru.pavel.zip.ZipMaker;

import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {

        File attachment = new File(Constants.FILE);
        AttachmentSignature attachmentSignature = new AttachmentSignature();
        ZipMaker zipMaker = new ZipMaker();

        if (attachment != null) {
            try {
                attachmentSignature.makeSignature(attachment);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            zipMaker.makeZip(attachment);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
