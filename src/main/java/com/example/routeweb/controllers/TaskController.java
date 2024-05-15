package com.example.routeweb.controllers;

import com.example.routeweb.models.FileModel;
import com.example.routeweb.models.Task;
import com.example.routeweb.services.CamundaService;
import com.example.routeweb.services.FileDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.IOException;
import java.util.List;

@Controller
public class TaskController {

    @Autowired
    private CamundaService camundaService;

    private final FileDataService fileDataService;

    @Autowired
    public TaskController(FileDataService fileDataService) {
        this.fileDataService = fileDataService;
    }

    @GetMapping("/fileWait")
    public String waitDocumentsPage(Model model, RedirectAttributes redirectAttributes) {
        // Получение только файлов с очередью "Wait"
        List<FileModel> fileDataList = fileDataService.getFilesByQueue("Wait");
        model.addAttribute("fileDataList", fileDataList);
        redirectAttributes.addFlashAttribute("successMessage", "Доступны файлы для архивации.");
        return "fileWait";
    }


    @PostMapping("/complete")
    public ResponseEntity<String> completeTaskByCriteria(@RequestParam String confirmBool) {
        try {
            List<Task> tasks = camundaService.getTasksByCriteria();
            System.out.println("Задача: " + tasks);
            if (tasks.isEmpty()) {
                return ResponseEntity.ok("Нет документов, ожидающих подтверждения.");
            }

            for (Task task : tasks) {
                System.out.println("Ответ: " + confirmBool);
                camundaService.completeTaskWithVariables(task.getId(), confirmBool);
            }
            return ResponseEntity.ok("Статус документа изменён");
        } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка при попытке сохранения: " + e.getMessage());
        }
    }
}
