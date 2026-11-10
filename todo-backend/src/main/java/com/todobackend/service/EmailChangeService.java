package com.todobackend.service;

import com.todobackend.dto.request.EmailChangeRequest;
import com.todobackend.security.principal.UserPrincipal;

public interface EmailChangeService {

    void requestEmailChange(
            UserPrincipal authenticatedUser,
            EmailChangeRequest request
    );

    void confirmEmailChange(String rawToken);

}
