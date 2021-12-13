package ru.pavel.signature;

import ru.pavel.config.Constants;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;

import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public class AttachmentSignature {

    public File makeSignature(File file) throws IOException {

        /** Файл в байтовом массиве */
        byte[] fileByteArray = new byte[(int) file.length()];

        //Переводим файл в массив байтов для передачи в метод подписания
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            fileInputStream.read(fileByteArray);
            fileInputStream.close();
        } catch (FileNotFoundException e) {
            System.out.println("File Not Found.");
            e.printStackTrace();
        }

        // получаем ключевую пару
        KeyPair keyPair = loadPair();
        PrivateKey privateKey = keyPair.getPrivate();
        X509Certificate cert = loadCert();

        try {
            //Подписываем документ
            signData(fileByteArray, cert, privateKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    /**
     * Метод для подписания файла
     *
     * @param data               - файл в массиве байтов
     * @param signingCertificate - сертификат
     * @param privateKey         - приватный ключ
     * @return byte[] signedMessage - подписанный документ
     * @throws Exception
     */
    public static byte[] signData(byte[] data, X509Certificate signingCertificate, PrivateKey privateKey)
            throws Exception {

        /** Подписанное сообщение */
        byte[] signedMessage;
        List<X509Certificate> certList = new ArrayList<X509Certificate>();
        CMSTypedData cmsData = new CMSProcessableByteArray(data);
        certList.add(signingCertificate);
        Store certs = new JcaCertStore(certList);

        //Подписываем файл
        CMSSignedDataGenerator cmsGenerator = new CMSSignedDataGenerator();
        ContentSigner contentSigner = new JcaContentSignerBuilder(Constants.GOST).build(privateKey);
        cmsGenerator.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(
                new JcaDigestCalculatorProviderBuilder().setProvider(Constants.PROVIDER)
                        .build()).build(contentSigner, signingCertificate));
        cmsGenerator.addCertificates(certs);

        CMSSignedData cms = cmsGenerator.generate(cmsData, false);
        signedMessage = cms.getEncoded();

        //Записываем подпись в файл
        FileOutputStream signFile = new FileOutputStream(Constants.SIGNATURE_FILE + ".xml.sig", false);
        signFile.write(signedMessage);
        signFile.close();

        return signedMessage;
    }

    /**
     * Метод открывает хранилище ключей. Нужен для получения ключевой пары
     *
     * @param ksType
     * @return ks - пара ключей
     * @throws Exception
     */
    static KeyStore OpenKeyStore(String ksType) throws Exception {
        KeyStore ks = KeyStore.getInstance("CryptoProCSPKeyStore", "DIGT");
        InputStream store_stream = new ByteArrayInputStream(ksType.getBytes("UTF-8"));
        ks.load(store_stream, null);
        return ks;
    }

    /**
     * Метод для загрузки ключевой пары из хранилища
     *
     * @return keyPair - пара ключей (закрытый, открытый)
     */
    static KeyPair loadPair() {
        KeyPair keyPair = null;
        try {
            char[] password = Constants.PRIVATE_KEY_PASSWORD.toCharArray();
            KeyStore ks = OpenKeyStore("CurrentUser/Containers");
            Key key = ks.getKey(Constants.KEYALIAS, password);

            if (key instanceof PrivateKey) {
                // Get certificate of public key
                X509Certificate cert = (X509Certificate) ks.getCertificate(Constants.KEYALIAS);
                // Get public key
                PublicKey publicKey = cert.getPublicKey();
                // Return a key pair
                keyPair = new KeyPair(publicKey, (PrivateKey) key);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return keyPair;
    }

    /**
     * Загружает сертификат из файла
     *
     * @return cert - сертификат
     */
    static X509Certificate loadCert() {
        X509Certificate cert = null;
        try {
            CertificateFactory fac = CertificateFactory.getInstance("X509");
            FileInputStream is = new FileInputStream(Constants.CERTH_PATH);
            cert = (X509Certificate) fac.generateCertificate(is);
            is.close();
        } catch (CertificateException | IOException e) {
            e.printStackTrace();
        }
        return cert;
    }
}