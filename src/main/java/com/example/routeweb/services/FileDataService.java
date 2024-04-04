package com.example.routeweb.services;

import com.example.routeweb.models.FileModel;
import com.example.routeweb.repositiries.FileRepo;
import io.jsonwebtoken.io.IOException;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import java.io.*;
import java.nio.file.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;
import java.util.Optional;

@Service
public class FileDataService {

    private final FileRepo fileRepo;

    private final S3Client s3Client; // инъекция S3Client

    @Autowired
    public FileDataService(FileRepo fileRepo, S3Client s3Client) {
        this.fileRepo = fileRepo;
        this.s3Client = s3Client; // сохранение ссылки на S3Client
    }

    public List<FileModel> getAllFileData() {
        return fileRepo.findAll();
    }
    public byte[] getFileFromStorage(String filePath) {

        // Получение имени файла из пути
        String fileName = new File(filePath).getName();

        // Указание имени бакета и ключа
        String bucketName = "mainbacket";
        String key = "main/" + fileName;

        // Загрузка содержимого файла из хранилища
        ResponseInputStream<GetObjectResponse> objectResponse = s3Client.getObject(GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build());

        // Преобразование содержимого файла в массив байтов
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = objectResponse.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            return outputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getHashFromDatabase(String fileName) {
        Optional<FileModel> fileDataOptional = fileRepo.findByName(fileName);
        if (fileDataOptional.isPresent()) {
            FileModel fileData = fileDataOptional.get();
            return fileData.getHashFile(); // Предположим, что у вас есть метод getFileHash в сущности FileData
        } else {
            return null; // Если файл не найден в базе данных, возвращаем null
        }
    }

    public byte[] convertToPdf(String inputFilePath) throws IOException, InterruptedException, java.io.IOException {
        // Загрузка файла из облака
        byte[] fileContent = getFileFromStorage(inputFilePath);
        if (fileContent == null) {
            throw new IOException("Файл не найден в облаке: " + inputFilePath);
        }

        // Создание временного файла для исходного документа
        String baseName = FilenameUtils.getBaseName(inputFilePath);
        File tempInputFile = File.createTempFile(baseName, "." + FilenameUtils.getExtension(inputFilePath));
        Path tempInputPath = tempInputFile.toPath();
        Files.write(tempInputPath, fileContent);

        // Создание временного файла для сконвертированного документа
        File tempOutputFile = File.createTempFile(baseName, ".pdf"); // Создается файл с уникальным именем
        Path tempOutputPath = tempOutputFile.toPath();
        tempOutputFile.deleteOnExit(); // Указываем, чтобы файл удалился после завершения программы

        // Запуск процесса конвертации
        ProcessBuilder builder = new ProcessBuilder(
                "C:\\Program Files\\LibreOffice\\program\\soffice.exe",
                "--headless",
                "--convert-to", "pdf",
                "--outdir", tempInputFile.getParent(),
                tempInputPath.toString()
        );

        Process process = builder.start();
        int exitCode = process.waitFor();

        // Удаляем исходный временный файл
        Files.delete(tempInputPath);

        if (exitCode != 0) {
            throw new IOException("Ошибка конвертации файла в PDF, код выхода: " + exitCode);
        }

        // Чтение сконвертированного файла
        byte[] pdfData = Files.readAllBytes(tempOutputPath);

        // Удаление временного PDF-файла после чтения
        Files.delete(tempOutputPath);

        return pdfData;
    }
}

