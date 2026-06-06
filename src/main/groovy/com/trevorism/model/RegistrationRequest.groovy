package com.trevorism.model

import com.trevorism.MemowandConstants

class RegistrationRequest {
    String username
    String password
    String email
    String tenantGuid = MemowandConstants.TENANT_GUID
}

