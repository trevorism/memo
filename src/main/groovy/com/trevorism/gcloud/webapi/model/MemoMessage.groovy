package com.trevorism.gcloud.webapi.model

class MemoMessage {

    String id
    String type // text | url
    String content
    boolean read
    Date dateCreated
    Date dateRead

}
