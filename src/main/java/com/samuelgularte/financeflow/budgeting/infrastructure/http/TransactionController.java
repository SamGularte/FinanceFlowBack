package com.samuelgularte.financeflow.budgeting.infrastructure.http;

import com.samuelgularte.financeflow.auth.application.usecase.response.MessageResponse;
import com.samuelgularte.financeflow.auth.infrastructure.security.CustomUserDetails;
import com.samuelgularte.financeflow.budgeting.application.input.TextRequest;
import com.samuelgularte.financeflow.budgeting.application.usecase.ProcessTextUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final ProcessTextUseCase processTextUseCase;

    public TransactionController(ProcessTextUseCase processTextUseCase) {
        this.processTextUseCase = processTextUseCase;
    }

    @PostMapping("/text")
    public ResponseEntity<MessageResponse> processText(@Valid @RequestBody TextRequest request,
                                                       @AuthenticationPrincipal CustomUserDetails userDetails) {
        UUID userId = userDetails.getUserId();
        String response = processTextUseCase.execute(request.text(), userId);
        return ResponseEntity.ok(new MessageResponse(response));
    }
}
