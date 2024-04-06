package com.example.routeweb.controllers;

import com.example.routeweb.models.FileModel;
import com.example.routeweb.services.DatabaseLogService;
import com.example.routeweb.services.FileDataService;
import com.example.routeweb.services.HashUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    public String getAllFileData(Model model, RedirectAttributes redirectAttributes) {
        infoLogger.info("Getting all file data");

        // Очистка папки
        try {
            Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
            Files.list(tempDir)
                    .filter(path -> path.toString().endsWith(".pdf")) // Фильтр для определения файлов, которые нужно удалить
                    .forEach(path -> {
                        try {
                            Files.delete(path); // Удаление файла
                        } catch (IOException e) {
                            errorLogger.error("Не удалось удалить временный файл: " + path, e);
                        }
                    });
        } catch (IOException e) {
            errorLogger.error("Ошибка при попытке очистить временную папку", e);
        }

        List<FileModel> fileDataList = fileDataService.getAllFileData();
        model.addAttribute("fileDataList", fileDataList);
        redirectAttributes.addFlashAttribute("successMessage", "Пользователь успешно добавлен.");
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

    // Этот метод обрабатывает запросы на печать файла
    @GetMapping("/print/{fileName:.+}")
    public ResponseEntity<InputStreamResource> printFile(@PathVariable String fileName) {
        try {
            byte[] fileContent = fileDataService.getFileFromStorage(fileName);

            // Проверка наличия файла в хранилище
            if (fileContent == null) {
                LOGGER.error("Файл не найден: {}", fileName);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new InputStreamResource(new ByteArrayInputStream(("Файл не найден: " + fileName).getBytes())));
            }

            // Вычисление хэш-суммы полученного файла
            String fileHash = HashUtils.calculateHash(fileContent);

            // Извлечение сохраненной хэш-суммы из базы данных
            String savedHash = fileDataService.getHashFromDatabase(fileName);

            if (fileHash.equals(savedHash)) {
                // Хэш-суммы совпадают, документ не был изменен
                LOGGER.info("Запрашиваемый файл: {}", fileName);
                String contentType = determineContentType(fileName);
                Path pdfPath;

                if (!"application/pdf".equals(contentType)) {
                    LOGGER.info("Файл требует конвертации в PDF: {}", fileName);
                    pdfPath = fileDataService.convertToPdf(fileName);
                    LOGGER.info("Файл конвертирован: {}", pdfPath);
                } else {
                    pdfPath = Files.createTempFile("pdf_", ".pdf");
                    Files.write(pdfPath, fileContent);
                    LOGGER.info("Файл уже в формате PDF: {}", pdfPath);
                }

                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + pdfPath.getFileName().toString() + "\"");
                headers.setContentType(MediaType.APPLICATION_PDF);

                Resource resource = new UrlResource(pdfPath.toUri());
                LOGGER.info("Отправляем файл клиенту: {}", pdfPath);
                return ResponseEntity.ok().headers(headers).body(new InputStreamResource(resource.getInputStream()));
            } else {
                // Хэш-суммы не совпадают, документ был изменен
                LOGGER.error("Файл {} был изменен", fileName);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new InputStreamResource(new ByteArrayInputStream(("Файл " + fileName + " был изменен").getBytes())));
            }
        } catch (FileNotFoundException e) {
            LOGGER.error("Файл не найден: {}", fileName);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new InputStreamResource(new ByteArrayInputStream(("Файл не найден: " + fileName).getBytes())));
        } catch (Exception e) {
            LOGGER.error("Ошибка при обработке файла: {}", fileName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new InputStreamResource(new ByteArrayInputStream(("Ошибка при обработке файла: " + e.getMessage()).getBytes())));
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