package com.trevorism.model

import groovy.transform.ToString

@ToString
class ImageComment {

    String id
    String imageId
    String author
    String text
    Date createdDate = new Date()

}

