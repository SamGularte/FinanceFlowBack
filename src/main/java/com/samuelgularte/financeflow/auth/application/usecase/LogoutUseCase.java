package com.samuelgularte.financeflow.auth.application.usecase;

import com.samuelgularte.financeflow.auth.application.usecase.request.LogoutRequest;
import com.samuelgularte.financeflow.auth.domain.repository.RefreshTokenRepository;
import com.samuelgularte.financeflow.auth.domain.service.TokenHasher;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class LogoutUseCase {

    private final RefreshTokenRepository refreshTokenRepository;

    public LogoutUseCase(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public String execute(LogoutRequest request) {
        refreshTokenRepository.findByToken(TokenHasher.hash(request.refreshToken()))
                .ifPresent(refreshTokenRepository::delete);
        return "Logged out";
    }
}
