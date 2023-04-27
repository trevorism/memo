package com.trevorism.gcloud.webapi.service

import com.trevorism.data.Repository
import com.trevorism.gcloud.webapi.model.MemoMessage
import org.junit.jupiter.api.Test

class DefaultMessageServiceTest {

    @Test
    void testSaveMessage() {
        DefaultMessageService dms = new DefaultMessageService()
        dms.repository = [create: {it -> assert it.type == "text"; return it}] as Repository<MemoMessage>
        assert dms.saveMessage(new MemoMessage(type: "text", content: "first one"))

    }

    @Test
    void testReadMessage() {
        DefaultMessageService dms = new DefaultMessageService()
        dms.repository = [get: {it -> return new MemoMessage(id: it)}, update: {id, it -> return new MemoMessage(id: id)}] as Repository<MemoMessage>
        assert dms.readMessage()
    }
}
