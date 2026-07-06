package com.trevorism.service

import com.trevorism.model.Folder
import com.trevorism.model.Image
import org.junit.jupiter.api.Test

import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

import static org.junit.jupiter.api.Assertions.assertThrows

class DefaultFolderServiceTest {

    @Test
    void testCreateFolderRejectsBlankName() {
        assertThrows(IllegalArgumentException) { buildService().createFolder("  ", "alice") }
    }

    @Test
    void testCreateFolderTrimsAndDefaultsUsername() {
        Folder folder = buildService().createFolder("  Vacation  ", null)
        assert folder.id
        assert folder.name == "Vacation"
        assert folder.username == "Unknown"
        assert folder.imageIds == []
    }

    @Test
    void testGetFolder() {
        def service = buildService([new Folder(id: "f1", name: "A")])
        assert service.getFolder(null) == null
        assert service.getFolder("f1").name == "A"
    }

    @Test
    void testRenameFolder() {
        def service = buildService([new Folder(id: "f1", name: "Old")])
        assertThrows(IllegalArgumentException) { service.renameFolder("f1", " ") }
        assert service.renameFolder("missing", "New") == null
        assert service.renameFolder("f1", "  New  ").name == "New"
    }

    @Test
    void testDeleteFolder() {
        def service = buildService([new Folder(id: "f1", name: "A")])
        assert !service.deleteFolder("missing")
        assert service.deleteFolder("f1")
        assert service.getFolder("f1") == null
    }

    @Test
    void testAddImageToFolder() {
        def service = buildService([new Folder(id: "f1", name: "A", imageIds: [])])
        assert service.addImageToFolder("missing", "img1") == null
        assert service.addImageToFolder("f1", "nonimage") == null
        Folder updated = service.addImageToFolder("f1", "img1")
        assert updated.imageIds == ["img1"]
        // adding the same image again does not duplicate
        assert service.addImageToFolder("f1", "img1").imageIds == ["img1"]
    }

    @Test
    void testRemoveImageFromFolder() {
        def service = buildService([new Folder(id: "f1", name: "A", imageIds: ["img1", "img2"])])
        Folder updated = service.removeImageFromFolder("f1", "img1")
        assert updated.imageIds == ["img2"]
    }

    @Test
    void testListFoldersForImage() {
        def service = buildService([
                new Folder(id: "f1", name: "A", imageIds: ["img1"]),
                new Folder(id: "f2", name: "B", imageIds: ["img2"]),
                new Folder(id: "f3", name: "C", imageIds: ["img1", "img2"])
        ])
        assert service.listFoldersForImage("img1")*.id.toSet() == ["f1", "f3"].toSet()
        assert service.listFoldersForImage(null) == []
    }

    @Test
    void testRemoveImageFromAllFolders() {
        def service = buildService([
                new Folder(id: "f1", name: "A", imageIds: ["img1", "img2"]),
                new Folder(id: "f2", name: "B", imageIds: ["img1"])
        ])
        service.removeImageFromAllFolders("img1")
        assert service.getFolder("f1").imageIds == ["img2"]
        assert service.getFolder("f2").imageIds == []
    }

    @Test
    void testCreateFolderFromZipStoresImagesAndDerivesName() {
        byte[] zip = buildZip([
                "photo1.png"   : "bytes1",
                "notes.txt"    : "ignore me",
                "__MACOSX/x.png": "junk",
                "photo2.jpg"   : "bytes2"
        ])
        def service = buildService()

        Folder folder = service.createFolderFromZip(new ByteArrayInputStream(zip), "My Album.zip", "alice")

        assert folder != null
        assert folder.name == "My Album"
        assert folder.username == "alice"
        assert folder.imageIds.size() == 2
    }

    @Test
    void testCreateFolderFromZipReturnsNullWhenNoImages() {
        byte[] zip = buildZip(["readme.txt": "hello", "data.csv": "1,2,3"])
        def service = buildService()

        assert service.createFolderFromZip(new ByteArrayInputStream(zip), "docs.zip", "alice") == null
    }

    private static byte[] buildZip(Map<String, String> entries) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        new ZipOutputStream(baos).withCloseable { ZipOutputStream zos ->
            entries.each { name, content ->
                zos.putNextEntry(new ZipEntry(name))
                zos.write(content.bytes)
                zos.closeEntry()
            }
        }
        return baos.toByteArray()
    }

    private static final ImageService IMAGE_EXISTS =
            [getImage      : { String id -> id?.startsWith("img") ? new Image() : null },
             createImage   : { InputStream is, String filename, String user, caption -> new Image(id: "stored-" + filename) }] as ImageService

    private static DefaultFolderService buildService(List<Folder> seed = [], ImageService imageService = IMAGE_EXISTS) {
        DefaultFolderService service = new DefaultFolderService(null)
        service.repository = new InMemoryRepository<Folder>(seed)
        service.imageService = imageService
        return service
    }
}
