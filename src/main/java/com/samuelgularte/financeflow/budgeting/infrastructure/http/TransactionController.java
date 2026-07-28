package com.samuelgularte.financeflow.budgeting.infrastructure.http;

import com.samuelgularte.financeflow.auth.application.usecase.response.MessageResponse;
import com.samuelgularte.financeflow.auth.infrastructure.security.CustomUserDetails;
import com.samuelgularte.financeflow.budgeting.application.input.TextRequest;
import com.samuelgularte.financeflow.budgeting.application.input.UpdateTransactionInput;
import com.samuelgularte.financeflow.budgeting.application.output.TransactionOutput;
import com.samuelgularte.financeflow.budgeting.application.usecase.DeleteTransactionUseCase;
import com.samuelgularte.financeflow.budgeting.application.usecase.FetchUserTransactionsUseCase;
import com.samuelgularte.financeflow.budgeting.application.usecase.ProcessAudioUseCase;
import com.samuelgularte.financeflow.budgeting.application.usecase.ProcessTextUseCase;
import com.samuelgularte.financeflow.budgeting.application.usecase.UpdateTransactionUseCase;
import com.samuelgularte.financeflow.budgeting.domain.Category;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final ProcessTextUseCase processTextUseCase;
    private final ProcessAudioUseCase processAudioUseCase;
    private final FetchUserTransactionsUseCase fetchUserTransactionsUseCase;
    private final UpdateTransactionUseCase updateTransactionUseCase;
    private final DeleteTransactionUseCase deleteTransactionUseCase;

    public TransactionController(ProcessTextUseCase processTextUseCase,
                                 ProcessAudioUseCase processAudioUseCase,
                                 FetchUserTransactionsUseCase fetchUserTransactionsUseCase,
                                 UpdateTransactionUseCase updateTransactionUseCase,
                                 DeleteTransactionUseCase deleteTransactionUseCase) {
        this.processTextUseCase = processTextUseCase;
        this.processAudioUseCase = processAudioUseCase;
        this.fetchUserTransactionsUseCase = fetchUserTransactionsUseCase;
        this.updateTransactionUseCase = updateTransactionUseCase;
        this.deleteTransactionUseCase = deleteTransactionUseCase;
    }

    @GetMapping
    public ResponseEntity<Page<TransactionOutput>> listTransactions(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) Category category) {
        return ResponseEntity.ok(fetchUserTransactionsUseCase.execute(userDetails.getUserId(), category, pageable));
    }

    @PostMapping("/text")
    public ResponseEntity<MessageResponse> processText(@Valid @RequestBody TextRequest request,
                                                       @AuthenticationPrincipal CustomUserDetails userDetails) {
        String response = processTextUseCase.execute(request.text(), userDetails.getUserId());
        return ResponseEntity.ok(new MessageResponse(response));
    }

    @PostMapping(value = "/audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MessageResponse> processAudio(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails userDetails) throws IOException {
        String response = processAudioUseCase.execute(file, userDetails.getUserId());
        return ResponseEntity.ok(new MessageResponse(response));
    }

    @RequestMapping(path = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<TransactionOutput> updateTransaction(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateTransactionInput input,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(updateTransactionUseCase.execute(id, userDetails.getUserId(), input));
    }

    @RequestMapping(path = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteTransaction(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        deleteTransactionUseCase.execute(id, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Void> handleNotFound() {
        return ResponseEntity.notFound().build();
    }
}
