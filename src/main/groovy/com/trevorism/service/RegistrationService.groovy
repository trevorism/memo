package com.trevorism.service

import com.trevorism.model.RegistrationRequest
import com.trevorism.model.User

interface RegistrationService {
    boolean registerUser(RegistrationRequest registrationRequest)
}

