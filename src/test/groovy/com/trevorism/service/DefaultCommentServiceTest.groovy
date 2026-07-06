package com.trevorism.service

import com.trevorism.model.Image
import com.trevorism.model.ImageComment
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertThrows

class DefaultCommentServiceTest {

    @Test
    void testAddCommentRejectsBlankText() {
        def service = buildService()
        assertThrows(IllegalArgumentException) { service.addComment("img1", new ImageComment(text: "   ")) }
    }

    @Test
    void testAddCommentReturnsNullWhenImageMissing() {
        def service = buildService()
        assert service.addComment("missing", new ImageComment(text: "hi")) == null
    }

    @Test
    void testAddCommentTrimsAndDefaultsAuthor() {
        def service = buildService()
        ImageComment created = service.addComment("img1", new ImageComment(text: "  hello  ", author: "  "))
        assert created.id
        assert created.imageId == "img1"
        assert created.text == "hello"
        assert created.author == "Unknown"
    }

    @Test
    void testAddCommentKeepsProvidedAuthor() {
        def service = buildService()
        ImageComment created = service.addComment("img1", new ImageComment(text: "hi", author: "  Alice  "))
        assert created.author == "Alice"
    }

    @Test
    void testUpdateCommentNullIdReturnsNull() {
        assert buildService().updateComment(null, new ImageComment(text: "x")) == null
    }

    @Test
    void testUpdateCommentBlankTextThrows() {
        def service = buildService([new ImageComment(id: "c1", imageId: "img1", text: "old")])
        assertThrows(IllegalArgumentException) { service.updateComment("c1", new ImageComment(text: " ")) }
    }

    @Test
    void testUpdateCommentMissingReturnsNull() {
        assert buildService().updateComment("nope", new ImageComment(text: "x")) == null
    }

    @Test
    void testUpdateCommentUpdatesTrimmedText() {
        def service = buildService([new ImageComment(id: "c1", imageId: "img1", text: "old")])
        ImageComment updated = service.updateComment("c1", new ImageComment(text: "  new text  "))
        assert updated.text == "new text"
    }

    @Test
    void testGetComment() {
        def service = buildService([new ImageComment(id: "c1", imageId: "img1", text: "hi")])
        assert service.getComment(null) == null
        assert service.getComment("c1").text == "hi"
    }

    @Test
    void testDeleteComment() {
        def service = buildService([new ImageComment(id: "c1", imageId: "img1", text: "hi")])
        assert !service.deleteComment(null)
        assert !service.deleteComment("nope")
        assert service.deleteComment("c1")
        assert service.getComment("c1") == null
    }

    @Test
    void testCountCommentsForImage() {
        def service = buildService([
                new ImageComment(imageId: "img1", text: "a"),
                new ImageComment(imageId: "img1", text: "b"),
                new ImageComment(imageId: "other", text: "c")
        ])
        assert service.countComments("img1") == 2
        assert service.countComments((String) null) == 0
    }

    @Test
    void testCountCommentsForCollection() {
        def service = buildService([
                new ImageComment(imageId: "img1", text: "a"),
                new ImageComment(imageId: "img1", text: "b"),
                new ImageComment(imageId: "img2", text: "c")
        ])
        Map<String, Integer> counts = service.countComments(["img1", "img2", "img3"])
        assert counts["img1"] == 2
        assert counts["img2"] == 1
        assert counts["img3"] == null
        assert service.countComments([] as Collection) == [:]
    }

    @Test
    void testListCommentsSortedByCreatedDateDesc() {
        def older = new ImageComment(id: "old", imageId: "img1", text: "old", createdDate: new Date(1000))
        def newer = new ImageComment(id: "new", imageId: "img1", text: "new", createdDate: new Date(9999))
        def service = buildService([older, newer])
        def result = service.listComments("img1")
        assert result*.id == ["new", "old"]
    }

    @Test
    void testDeleteCommentsForImage() {
        def service = buildService([
                new ImageComment(id: "c1", imageId: "img1", text: "a"),
                new ImageComment(id: "c2", imageId: "img1", text: "b"),
                new ImageComment(id: "c3", imageId: "other", text: "c")
        ])
        service.deleteCommentsForImage("img1")
        assert service.countComments("img1") == 0
        assert service.getComment("c3") != null
    }

    private static final ImageService IMAGE_EXISTS =
            [getImage: { String id -> id == "img1" ? new Image() : null }] as ImageService

    private static DefaultCommentService buildService(List<ImageComment> seed = [], ImageService imageService = IMAGE_EXISTS) {
        DefaultCommentService service = new DefaultCommentService(null)
        service.repository = new InMemoryRepository<ImageComment>(seed)
        service.imageService = imageService
        return service
    }
}
