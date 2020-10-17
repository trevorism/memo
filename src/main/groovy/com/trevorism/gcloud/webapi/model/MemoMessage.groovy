package com.trevorism.gcloud.webapi.model

class MemoMessage {

    String id
    String type // text | url
    String content
    boolean read
    boolean paid
    Date dateCreated
    Date dateRead

}
