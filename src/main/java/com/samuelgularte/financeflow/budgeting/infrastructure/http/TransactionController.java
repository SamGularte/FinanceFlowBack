package com.samuelgularte.financeflow.budgeting.infrastructure.http;

import com.samuelgularte.financeflow.auth.application.usecase.response.MessageResponse;
import com.samuelgularte.financeflow.auth.infrastructure.security.CustomUserDetails;
import com.samuelgularte.financeflow.budgeting.application.input.TextRequest;
import com.samuelgularte.financeflow.budgeting.application.usecase.ProcessAudioUseCase;
import com.samuelgularte.financeflow.budgeting.application.usecase.ProcessTextUseCase;
import jakarta.validation.Valid;
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

    public TransactionController(ProcessTextUseCase processTextUseCase, ProcessAudioUseCase processAudioUseCase) {
        this.processTextUseCase = processTextUseCase;
        this.processAudioUseCase = processAudioUseCase;
    }

    @PostMapping("/text")
    public ResponseEntity<MessageResponse> processText(@Valid @RequestBody TextRequest request,
                                                       @AuthenticationPrincipal CustomUserDetails userDetails) {
        UUID userId = userDetails.getUserId();
        String response = processTextUseCase.execute(request.text(), userId);
        return ResponseEntity.ok(new MessageResponse(response));
    }

    @PostMapping(value = "/audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MessageResponse> processAudio(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails userDetails) throws IOException {
        UUID userId = userDetails.getUserId();
        String response = processAudioUseCase.execute(file, userId);
        return ResponseEntity.ok(new MessageResponse(response));
    }
}
