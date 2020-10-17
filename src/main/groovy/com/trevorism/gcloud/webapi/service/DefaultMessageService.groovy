package com.trevorism.gcloud.webapi.service

import com.trevorism.data.PingingDatastoreRepository
import com.trevorism.data.Repository
import com.trevorism.gcloud.webapi.model.MemoMessage

class DefaultMessageService implements MessageService{

    private Repository<MemoMessage> repository = new PingingDatastoreRepository<>(MemoMessage)

    @Override
    MemoMessage saveMessage(MemoMessage message) {
        message.dateCreated = new Date()
        message.read = false
        message.dateRead = null
        return repository.create(message)
    }

    @Override
    boolean readMessage(String id) {
        MemoMessage message = repository.get(id)
        if(!message || message.read) {
            return false
        }
        message.read = true
        message.dateRead = new Date()
        return repository.update(id, message)
    }
}
