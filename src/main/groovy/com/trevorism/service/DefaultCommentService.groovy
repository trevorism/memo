package com.trevorism.service

import com.trevorism.data.FastDatastoreRepository
import com.trevorism.data.Repository
import com.trevorism.data.model.filtering.FilterConstants
import com.trevorism.data.model.filtering.SimpleFilter
import com.trevorism.https.SecureHttpClient
import com.trevorism.model.ImageComment
import io.micronaut.runtime.http.scope.RequestScope
import jakarta.inject.Inject
import jakarta.inject.Named
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@RequestScope
class DefaultCommentService implements CommentService {

    private static final Logger log = LoggerFactory.getLogger(DefaultCommentService)

    private Repository<ImageComment> repository
    private static final Closure sortByCreatedDateDesc = { a, b ->
        Date bDate = b?.createdDate ?: new Date(0)
        Date aDate = a?.createdDate ?: new Date(0)
        bDate <=> aDate
    }

    @Inject
    ImageService imageService

    DefaultCommentService(@Named("passThruSecureHttpClient") SecureHttpClient httpClient) {
        repository = new FastDatastoreRepository<>(ImageComment, httpClient)
    }

    @Override
    List<ImageComment> listComments(String imageId) {
        List<ImageComment> imageComments = repository.filter(new SimpleFilter("imageId", FilterConstants.OPERATOR_EQUAL, imageId))
        imageComments.sort sortByCreatedDateDesc
    }

    @Override
    ImageComment addComment(String imageId, ImageComment comment) {
        if (!comment?.text?.trim()) {
            throw new IllegalArgumentException('Comment text is required')
        }
        if (!imageService.getImage(imageId)) {
            return null
        }

        ImageComment imageComment = new ImageComment(
                imageId: imageId,
                author: comment.author?.trim() ?: 'Anonymous',
                text: comment.text.trim(),
                createdDate: new Date()
        )
        ImageComment created = repository.create(imageComment)
        return created
    }

    @Override
    ImageComment getComment(String commentId) {
        if (!commentId) {
            return null
        }
        repository.get(commentId)
    }

    @Override
    boolean deleteComment(String commentId) {
        if (!commentId) {
            return false
        }
        if (!repository.get(commentId)) {
            return false
        }
        repository.delete(commentId)
        return true
    }

    @Override
    int countComments(String imageId) {
        if (!imageId) {
            return 0
        }
        repository.filter(new SimpleFilter("imageId", FilterConstants.OPERATOR_EQUAL, imageId)).size()
    }

    @Override
    Map<String, Integer> countComments(Collection<String> imageIds) {
        if (!imageIds) {
            return [:]
        }

        Set<String> idSet = imageIds.findAll { it } as Set
        Map<String, Integer> counts = [:]
        repository.list().each { ImageComment comment ->
            if (comment?.imageId in idSet) {
                counts[comment.imageId] = (counts[comment.imageId] ?: 0) + 1
            }
        }
        return counts
    }

    @Override
    void deleteCommentsForImage(String imageId) {
        if (!imageId) {
            return
        }
        List<ImageComment> comments = repository.filter(new SimpleFilter("imageId", FilterConstants.OPERATOR_EQUAL, imageId))
        comments.each { ImageComment comment ->
            if (comment?.id) {
                try {
                    repository.delete(comment.id)
                } catch (Exception e) {
                    log.warn("Unable to delete comment {} for image {}", comment.id, imageId, e)
                }
            }
        }
    }

}

