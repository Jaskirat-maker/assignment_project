package com.finance.service.impl;

import com.finance.entity.FinancialRecord;
import com.finance.entity.User;
import com.finance.exception.ResourceNotFoundException;
import com.finance.repository.FinancialRecordRepository;
import com.finance.repository.UserRepository;
import com.finance.service.CsvExportService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CsvExportServiceImpl implements CsvExportService {

    private final FinancialRecordRepository financialRecordRepository;
    private final UserRepository userRepository;

    @Override
    public byte[] exportUserRecords(String username) {
        User user = userRepository.findByUsernameAndDeletedFalse(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<FinancialRecord> records = financialRecordRepository.findByUserIdAndDeletedFalse(user.getId());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(outputStream);
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                     .withHeader("ID", "Title", "Description", "Amount", "Type", "Category", "Date", "CreatedAt"))) {

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            for (FinancialRecord record : records) {
                csvPrinter.printRecord(
                        record.getId(),
                        record.getTitle(),
                        record.getDescription(),
                        record.getAmount(),
                        record.getType(),
                        record.getCategory(),
                        record.getTransactionDate().format(dateFormatter),
                        record.getCreatedAt().format(dateTimeFormatter)
                );
            }

            csvPrinter.flush();
        } catch (IOException e) {
            throw new RuntimeException("Error generating CSV file", e);
        }

        return outputStream.toByteArray();
    }

}