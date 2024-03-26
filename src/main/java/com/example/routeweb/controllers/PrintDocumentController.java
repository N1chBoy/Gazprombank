package com.example.routeweb.controllers;

import com.example.routeweb.services.FileDataService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class PrintDocumentController {

    private final FileDataService fileDataService;

    public PrintDocumentController(FileDataService fileDataService) {
        this.fileDataService = fileDataService;
    }

    @PostMapping("/printDocument")
    public ResponseEntity<String> printDocument(@RequestBody String fileName) {
        try {
            // Получение URL документа из хранилища
            String documentUrl = fileDataService.getDocumentUrl(fileName);

            // Возврат URL в ответе
            return ResponseEntity.ok(documentUrl);
        } catch (Exception e) {
            // Обработка ошибок
            e.printStackTrace(); // Замените на логирование ошибки
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
