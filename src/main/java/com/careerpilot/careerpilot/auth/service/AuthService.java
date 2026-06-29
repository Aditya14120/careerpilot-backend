package com.careerpilot.careerpilot.auth.service;

import com.careerpilot.careerpilot.auth.dto.RegisterRequest;
import com.careerpilot.careerpilot.auth.dto.RegisterResponse;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);

}