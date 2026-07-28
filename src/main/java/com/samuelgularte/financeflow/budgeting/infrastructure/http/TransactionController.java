package com.samuelgularte.financeflow.budgeting.infrastructure.http;

import com.samuelgularte.financeflow.auth.application.usecase.response.MessageResponse;
import com.samuelgularte.financeflow.auth.infrastructure.security.CustomUserDetails;
import com.samuelgularte.financeflow.budgeting.application.input.TextRequest;
import com.samuelgularte.financeflow.budgeting.application.input.UpdateTransactionInput;
import com.samuelgularte.financeflow.budgeting.application.output.TransactionOutput;
import com.samuelgularte.financeflow.budgeting.application.output.TransactionPageMapper;
import com.samuelgularte.financeflow.budgeting.application.usecase.DeleteTransactionUseCase;
import com.samuelgularte.financeflow.budgeting.application.usecase.FetchUserTransactionsUseCase;
import com.samuelgularte.financeflow.budgeting.application.usecase.ProcessAudioUseCase;
import com.samuelgularte.financeflow.budgeting.application.usecase.ProcessTextUseCase;
import com.samuelgularte.financeflow.budgeting.application.usecase.UpdateTransactionUseCase;
import com.samuelgularte.financeflow.budgeting.domain.Category;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
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
    private final FetchUserTransactionsUseCase fetchUserTransactionsUseCase;
    private final UpdateTransactionUseCase updateTransactionUseCase;
    private final DeleteTransactionUseCase deleteTransactionUseCase;
    private final TransactionPageMapper pageMapper;

    public TransactionController(ProcessTextUseCase processTextUseCase,
                                 ProcessAudioUseCase processAudioUseCase,
                                 FetchUserTransactionsUseCase fetchUserTransactionsUseCase,
                                 UpdateTransactionUseCase updateTransactionUseCase,
                                 DeleteTransactionUseCase deleteTransactionUseCase,
                                 TransactionPageMapper pageMapper) {
        this.processTextUseCase = processTextUseCase;
        this.processAudioUseCase = processAudioUseCase;
        this.fetchUserTransactionsUseCase = fetchUserTransactionsUseCase;
        this.updateTransactionUseCase = updateTransactionUseCase;
        this.deleteTransactionUseCase = deleteTransactionUseCase;
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

    @PutMapping("/{id}")
    public ResponseEntity<TransactionOutput> updateTransaction(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateTransactionInput input,
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
}
