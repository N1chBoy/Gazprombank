package com.example.routeweb.controllers;

import com.example.routeweb.services.HashUtils;
import com.example.routeweb.models.FileModel;
import com.example.routeweb.services.DatabaseLogService;
import com.example.routeweb.services.FileDataService;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import software.amazon.awssdk.services.s3.S3Client;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Controller
public class FileDataController {

    private static final Logger errorLogger = LoggerFactory.getLogger("errorLogger");
    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private DatabaseLogService databaseLogService;
    private final FileDataService fileDataService;
    private final S3Client s3Client; // инъекция S3Client

    @Autowired
    public FileDataController(FileDataService fileDataService, S3Client s3Client) {
        this.fileDataService = fileDataService;
        this.s3Client = s3Client; // сохранение ссылки на S3Client
    }

    @GetMapping("/fileData")
    public String getAllFileData(Model model) {
        infoLogger.info("Getting all file data");
        List<FileModel> fileDataList = fileDataService.getAllFileData();
        model.addAttribute("fileDataList", fileDataList);
        return "fileData";
    }

    @GetMapping("/download/{fileName}")
    @ResponseBody
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable String fileName) {
        try {
            // Получение содержимого файла из хранилища
            byte[] fileContent = fileDataService.getFileFromStorage(fileName);

            // Вычисление хэш-суммы полученного файла
            String fileHash = HashUtils.calculateHash(fileContent); // Здесь HashUtils - это ваш утилитарный класс для вычисления хэш-суммы

            // Извлечение сохраненной хэш-суммы из базы данных
            String savedHash = fileDataService.getHashFromDatabase(fileName); // Предположим, что у вас есть метод для извлечения хэша из базы данных

            // Сравнение хэш-сумм
            if (fileHash.equals(savedHash)) {
                // Хэш-суммы совпадают, документ не был изменен
                InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(fileContent));
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

                // Кодирование имени файла
                String encodedFileName = URLEncoder.encode(fileName, "UTF-8");
                headers.setContentDispositionFormData("attachment", encodedFileName);
                infoLogger.info("Downloading file {}", fileName);

                return new ResponseEntity<>(resource, headers, HttpStatus.OK);
            } else {
                // Хэш-суммы не совпадают, документ был изменен
                LOGGER.error("File {} has been modified", fileName);
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            errorLogger.error("Error downloading file {}", fileName, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/print/{fileName:.+}")
    public ResponseEntity<InputStreamResource> printFile(@PathVariable String fileName) {
        try {
            // Определение MIME-типа исходного файла
            String originalContentType = determineContentType(fileName);
            byte[] data;

            if (!"application/pdf".equals(originalContentType)) {
                // Если файл не PDF, попытаться конвертировать его в PDF
                data = fileDataService.convertToPdf(fileName);
                // Изменение имени файла на соответствующее имя PDF
                fileName = FilenameUtils.getBaseName(fileName) + ".pdf";
            } else {
                // Если файл уже в формате PDF, получить его содержимое
                data = fileDataService.getFileFromStorage(fileName);
            }

            if (data == null) {
                // Файл не найден
                String errorMessage = "Файл " + fileName + " не найден.";
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new InputStreamResource(new ByteArrayInputStream(errorMessage.getBytes())));
            }

            // Установка заголовков для ответа
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "inline; filename=\"" + fileName + "\"");
            headers.setContentType(MediaType.APPLICATION_PDF);

            // Отправка PDF-файла клиенту
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new InputStreamResource(new ByteArrayInputStream(data)));
        } catch (Exception e) {
            // Обработка исключений
            String errorMessage = "Ошибка при попытке печати файла: " + fileName;
            errorLogger.error(errorMessage, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new InputStreamResource(new ByteArrayInputStream(errorMessage.getBytes())));
        }
    }


    private String determineContentType(String fileName) {
        String extension = StringUtils.getFilenameExtension(fileName).toLowerCase();
        switch (extension) {
            case "pdf":
                return "application/pdf";
            case "txt":
                return "text/plain";
            case "doc":
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            default:
                return "application/octet-stream";
        }
    }
}