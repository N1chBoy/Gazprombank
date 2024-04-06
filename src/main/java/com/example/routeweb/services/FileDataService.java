package com.example.routeweb.services;

import com.example.routeweb.models.FileModel;
import com.example.routeweb.repositiries.FileRepo;
import io.jsonwebtoken.io.IOException;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@Service
public class FileDataService {

    private final FileRepo fileRepo;

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

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

    // FileDataService.java

    // Этот метод конвертирует файл в формат PDF и возвращает путь к временному файлу PDF
    public Path convertToPdf(String inputFileName) throws IOException, InterruptedException, java.io.IOException {
        LOGGER.info("Начало конвертации файла {}", inputFileName);
        // Загрузка файла из хранилища
        byte[] fileContent = getFileFromStorage(inputFileName);
        if (fileContent == null) {
            throw new IOException("Файл не найден в облаке: " + inputFileName);
        }

        // Создание временного файла для исходного документа
        String baseName = FilenameUtils.getBaseName(inputFileName);
        File tempInputFile = File.createTempFile(baseName, ".pdf");
        Path tempInputPath = tempInputFile.toPath();
        LOGGER.info("Исходный временный файл создан: {}", tempInputPath);
        Files.write(tempInputPath, fileContent);


        // Запуск процесса конвертации
        ProcessBuilder builder = new ProcessBuilder(
                "soffice",
                "--headless",
                "--convert-to", "pdf",
                "--outdir", tempInputFile.getParent(),
                tempInputPath.toString()
        );

        Process process = builder.start();
        int exitCode = process.waitFor();

        // Проверяем результаты выполнения процесса и логируем ошибки
        if (exitCode != 0) {
            try (BufferedReader output = new BufferedReader(new InputStreamReader(process.getInputStream()));
                 BufferedReader errors = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {

                LOGGER.error("Ошибка конвертации файла. Выходной код: {}", exitCode);
                output.lines().forEach(line -> LOGGER.error("STDOUT: {}", line));
                errors.lines().forEach(line -> LOGGER.error("STDERR: {}", line));
            }
            throw new IOException("Ошибка конвертации файла в PDF, код выхода: " + exitCode);
        }

        // Проверяем существование и содержимое файла
        if (!Files.exists(tempInputPath) || Files.size(tempInputPath) == 0) {
            throw new IOException("Ошибка: сконвертированный файл не найден или пуст.");
        }
        LOGGER.info("Конвертация успешна. Файл: {}", tempInputPath);

        // Возвращаем путь к временному файлу PDF
        return tempInputPath;
    }


}

