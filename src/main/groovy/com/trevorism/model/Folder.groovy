package com.trevorism.model

import groovy.transform.ToString

@ToString
class Folder {

    String id
    String name
    String username
    List<String> imageIds = []
    Date createdDate = new Date()
    int imageCount = 0
    String coverImageId

}
