package com.example.routeweb.controllers;

import com.example.routeweb.models.FileModel;
import com.example.routeweb.repositiries.FileRepo;
import com.example.routeweb.services.FileDataService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class StatsController {

    private final FileDataService documentService;
    private final FileRepo fileRepo;

    public StatsController(FileDataService documentService, FileRepo fileRepo) {
        this.documentService = documentService;
        this.fileRepo = fileRepo;
    }

    @GetMapping("/statsFile")
    public String showStats(Model model) {
        String earliestDate = fileRepo.findEarliestDate();
        String latestDate = fileRepo.findLatestDate();

        model.addAttribute("earliestDate", earliestDate);
        model.addAttribute("latestDate", latestDate);

        return "statsFile";
    }

    @GetMapping("/statsData")
    @ResponseBody
    public Map<String, Object> getStatsData() {
        Map<String, Object> data = new HashMap<>();

        int mainQueueCount = fileRepo.countByQueue("Main");
        int deadQueueCount = fileRepo.countByQueue("Dead");
        int waitQueueCount = fileRepo.countByQueue("Wait");
        int importQueueCount = fileRepo.countByQueue("Important");

        List<Date> dates = findAllDates();
        List<Integer> documentsProcessed = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        List<String> formattedDates = new ArrayList<>();

        for (Date date : dates) {
            formattedDates.add(dateFormat.format(date));
            int count = countDocumentsByDate(date);
            documentsProcessed.add(count);
            System.out.println("Дата: " + dateFormat.format(date) + ", Обработано документов: " + count);
        }

        data.put("queueCounts", Arrays.asList(mainQueueCount, deadQueueCount, waitQueueCount, importQueueCount));
        data.put("dates", formattedDates);
        data.put("documentsProcessed", documentsProcessed);

        return data;
    }

    public List<Date> findAllDates() {
        List<FileModel> allFiles = fileRepo.findAll();
        Set<Date> allDates = new TreeSet<>((d1, d2) -> d1.compareTo(d2));
        for (FileModel file : allFiles) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(file.getDateTime());
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            allDates.add(cal.getTime());
        }
        return new ArrayList<>(allDates);
    }

    public int countDocumentsByDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startDate = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date endDate = cal.getTime();

        List<FileModel> filesOnDate = fileRepo.findByDateRange(startDate, endDate);
        return filesOnDate.size();
    }
}