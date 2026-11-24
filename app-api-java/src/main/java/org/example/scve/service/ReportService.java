package org.example.scve.service;

import org.example.scve.dto.DailyReportResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class ReportService {

    public DailyReportResponse daily(LocalDate date) {
        return SampleData.dailyReport(date != null ? date : LocalDate.now());
    }
}
