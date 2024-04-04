package com.example.routeweb.controllers;

import com.example.routeweb.models.FileModel;
import com.example.routeweb.services.DatabaseLogService;
import com.example.routeweb.services.FileDataService;
import com.example.routeweb.services.HashUtils;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.ByteArrayInputStream;
import java.net.URLEncoder;
import java.util.List;

@Controller
public class FileDataFromEmployeeController {

    private static final Logger errorLogger = LoggerFactory.getLogger("errorLogger");
    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private DatabaseLogService databaseLogService;
    private final FileDataService fileDataService;
    private final S3Client s3Client; // инъекция S3Client

    @Autowired
    public FileDataFromEmployeeController(FileDataService fileDataService, S3Client s3Client) {
        this.fileDataService = fileDataService;
        this.s3Client = s3Client; // сохранение ссылки на S3Client
    }

    @GetMapping("/fileDataE")
    public String getAllFileData(Model model) {
        infoLogger.info("Getting all file data");
        List<FileModel> fileDataList = fileDataService.getAllFileData();
        model.addAttribute("fileDataList", fileDataList);
        return "fileDataFromEmployee";
    }

    @GetMapping("/downloadE/{fileName}")
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
}