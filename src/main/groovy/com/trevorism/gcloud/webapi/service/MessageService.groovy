package com.trevorism.gcloud.webapi.service

import com.trevorism.gcloud.webapi.model.MemoMessage

interface MessageService {

    MemoMessage saveMessage(MemoMessage message)
    boolean readMessage(String id)
}