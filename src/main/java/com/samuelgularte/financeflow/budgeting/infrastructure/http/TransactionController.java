package com.samuelgularte.financeflow.budgeting.infrastructure.http;

import com.samuelgularte.financeflow.auth.application.output.MessageResponse;
import com.samuelgularte.financeflow.auth.infrastructure.security.CustomUserDetails;
import com.samuelgularte.financeflow.budgeting.application.output.ExportReportResult;
import com.samuelgularte.financeflow.budgeting.application.output.MonthlyDashboardOutput;
import com.samuelgularte.financeflow.budgeting.application.output.MonthlyInsightOutput;
import com.samuelgularte.financeflow.budgeting.application.usecase.ExportMonthlyReportUseCase;
import com.samuelgularte.financeflow.budgeting.application.usecase.GenerateMonthlyInsightUseCase;
import com.samuelgularte.financeflow.budgeting.application.usecase.GetMonthlyDashboardUseCase;
import com.samuelgularte.financeflow.budgeting.application.usecase.request.TextRequest;
import com.samuelgularte.financeflow.budgeting.application.usecase.request.UpdateTransactionRequest;
import com.samuelgularte.financeflow.budgeting.application.output.TransactionOutput;
import com.samuelgularte.financeflow.budgeting.application.output.TransactionPageMapper;
import com.samuelgularte.financeflow.budgeting.application.usecase.DeleteTransactionUseCase;
import com.samuelgularte.financeflow.budgeting.application.usecase.FetchUserTransactionsUseCase;
import com.samuelgularte.financeflow.budgeting.application.usecase.ProcessAudioUseCase;
import com.samuelgularte.financeflow.budgeting.application.usecase.ProcessImageUseCase;
import com.samuelgularte.financeflow.budgeting.application.usecase.ProcessTextUseCase;
import com.samuelgularte.financeflow.budgeting.application.usecase.UpdateTransactionUseCase;
import com.samuelgularte.financeflow.budgeting.domain.Category;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/transactions")
@Slf4j
public class TransactionController {

    private final ProcessTextUseCase processTextUseCase;
    private final ProcessAudioUseCase processAudioUseCase;
    private final ProcessImageUseCase processImageUseCase;
    private final FetchUserTransactionsUseCase fetchUserTransactionsUseCase;
    private final UpdateTransactionUseCase updateTransactionUseCase;
    private final DeleteTransactionUseCase deleteTransactionUseCase;
    private final GetMonthlyDashboardUseCase getMonthlyDashboardUseCase;
    private final GenerateMonthlyInsightUseCase generateMonthlyInsightUseCase;
    private final ExportMonthlyReportUseCase exportMonthlyReportUseCase;
    private final TransactionPageMapper pageMapper;

    public TransactionController(ProcessTextUseCase processTextUseCase,
                                 ProcessAudioUseCase processAudioUseCase,
                                 ProcessImageUseCase processImageUseCase,
                                 FetchUserTransactionsUseCase fetchUserTransactionsUseCase,
                                 UpdateTransactionUseCase updateTransactionUseCase,
                                 DeleteTransactionUseCase deleteTransactionUseCase,
                                 GetMonthlyDashboardUseCase getMonthlyDashboardUseCase,
                                 GenerateMonthlyInsightUseCase generateMonthlyInsightUseCase,
                                 ExportMonthlyReportUseCase exportMonthlyReportUseCase,
                                 TransactionPageMapper pageMapper) {
        this.processTextUseCase = processTextUseCase;
        this.processAudioUseCase = processAudioUseCase;
        this.processImageUseCase = processImageUseCase;
        this.fetchUserTransactionsUseCase = fetchUserTransactionsUseCase;
        this.updateTransactionUseCase = updateTransactionUseCase;
        this.deleteTransactionUseCase = deleteTransactionUseCase;
        this.getMonthlyDashboardUseCase = getMonthlyDashboardUseCase;
        this.generateMonthlyInsightUseCase = generateMonthlyInsightUseCase;
        this.exportMonthlyReportUseCase = exportMonthlyReportUseCase;
        this.pageMapper = pageMapper;
    }

    @GetMapping
    public ResponseEntity<Page<TransactionOutput>> listTransactions(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Category category) {
        log.info("Listing transactions for userId={}, category={}, page={}, size={}", userDetails.getUserId(), category, page, size);
        var txPage = fetchUserTransactionsUseCase.execute(userDetails.getUserId(), category, page, size);
        return ResponseEntity.ok(pageMapper.toSpringPage(txPage));
    }

    @PostMapping("/text")
    public ResponseEntity<MessageResponse> processText(@Valid @RequestBody TextRequest request,
                                                       @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Processing text for userId={}", userDetails.getUserId());
        String response = processTextUseCase.execute(request.text(), userDetails.getUserId());
        return ResponseEntity.ok(new MessageResponse(response));
    }

    @PostMapping(value = "/audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MessageResponse> processAudio(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails userDetails) throws IOException {
        log.info("Processing audio for userId={}", userDetails.getUserId());
        String response = processAudioUseCase.execute(file, userDetails.getUserId());
        return ResponseEntity.ok(new MessageResponse(response));
    }

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MessageResponse> processImage(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails userDetails) throws IOException {
        log.info("Processing image for userId={}", userDetails.getUserId());
        String response = processImageUseCase.execute(file, userDetails.getUserId());
        return ResponseEntity.ok(new MessageResponse(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionOutput> updateTransaction(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateTransactionRequest input,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Updating transaction id={}, userId={}", id, userDetails.getUserId());
        return ResponseEntity.ok(updateTransactionUseCase.execute(id, userDetails.getUserId(), input));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Deleting transaction id={}, userId={}", id, userDetails.getUserId());
        deleteTransactionUseCase.execute(id, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/insights")
    public ResponseEntity<MonthlyInsightOutput> generateInsight(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        log.info("Generating insight for userId={}, year={}, month={}", userDetails.getUserId(), year, month);
        var insight = generateMonthlyInsightUseCase.execute(userDetails.getUserId(), year, month);
        return ResponseEntity.ok(MonthlyInsightOutput.from(insight));
    }

    @GetMapping("/dashboard/monthly")
    public ResponseEntity<MonthlyDashboardOutput> monthlyDashboard(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        log.info("Fetching monthly dashboard for userId={}, year={}, month={}", userDetails.getUserId(), year, month);
        var dashboard = getMonthlyDashboardUseCase.execute(userDetails.getUserId(), year, month);
        return ResponseEntity.ok(MonthlyDashboardOutput.from(dashboard));
    }

    @GetMapping("/export/monthly")
    public ResponseEntity<Resource> exportMonthlyReport(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        log.info("Exporting monthly report for userId={}, year={}, month={}", userDetails.getUserId(), year, month);
        ExportReportResult result = exportMonthlyReportUseCase.execute(userDetails.getUserId(), year, month);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=relatorio-mensal-" + result.year() + "-" + result.month() + ".csv")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(new ByteArrayResource(result.content()));
    }
}
