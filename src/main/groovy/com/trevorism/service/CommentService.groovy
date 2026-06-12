package com.trevorism.service

import com.trevorism.model.ImageComment

interface CommentService {

    List<ImageComment> listComments(String imageId)
    ImageComment addComment(String imageId, ImageComment comment)
    ImageComment getComment(String commentId)
    boolean deleteComment(String commentId)
    int countComments(String imageId)
    Map<String, Integer> countComments(Collection<String> imageIds)
    void deleteCommentsForImage(String imageId)
}

