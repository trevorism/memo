package com.trevorism.service

import com.trevorism.data.FastDatastoreRepository
import com.trevorism.data.Repository
import com.trevorism.data.model.filtering.FilterConstants
import com.trevorism.data.model.filtering.SimpleFilter
import com.trevorism.https.SecureHttpClient
import com.trevorism.model.Folder
import com.trevorism.model.Image
import io.micronaut.runtime.http.scope.RequestScope
import jakarta.inject.Inject
import jakarta.inject.Named
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

@RequestScope
class DefaultFolderService implements FolderService {

    private static final Logger log = LoggerFactory.getLogger(DefaultFolderService)

    private static final List<String> IMAGE_EXTENSIONS = ['.jpg', '.jpeg', '.png', '.gif', '.webp']

    private Repository<Folder> repository
    private static final Closure sortByCreatedDateDesc = { a, b ->
        Date bDate = b?.createdDate ?: new Date(0)
        Date aDate = a?.createdDate ?: new Date(0)
        bDate <=> aDate
    }

    @Inject
    ImageService imageService

    DefaultFolderService(@Named("passThruSecureHttpClient") SecureHttpClient httpClient) {
        repository = new FastDatastoreRepository<>(Folder, httpClient)
    }

    @Override
    List<Folder> listFolders() {
        repository.list().sort sortByCreatedDateDesc
    }

    @Override
    Folder getFolder(String id) {
        if (!id) {
            return null
        }
        repository.get(id)
    }

    @Override
    List<Image> listFolderImages(String id) {
        Folder folder = getFolder(id)
        if (!folder) {
            return null
        }

        List<Image> images = (folder.imageIds ?: []).collect { String imageId ->
            imageService.getImage(imageId)
        }.findAll { it != null }
        images.sort sortByCreatedDateDesc
    }

    @Override
    Folder createFolder(String name, String username) {
        if (!name?.trim()) {
            throw new IllegalArgumentException('Folder name is required')
        }

        Folder folder = new Folder(
                name: name.trim(),
                username: username?.trim() ?: 'Unknown',
                imageIds: [],
                createdDate: new Date()
        )
        return repository.create(folder)
    }

    @Override
    Folder createFolderFromZip(InputStream zipStream, String zipFilename, String username) {
        String albumName = deriveAlbumName(zipFilename)
        String owner = username?.trim() ?: 'Unknown'
        List<String> imageIds = []
        int imageEntryCount = 0
        Exception firstError = null

        new ZipInputStream(zipStream).withCloseable { ZipInputStream zis ->
            ZipEntry entry
            while ((entry = zis.nextEntry) != null) {
                try {
                    if (entry.directory || !isImageEntry(entry.name)) {
                        continue
                    }
                    imageEntryCount++
                    byte[] bytes = readEntryBytes(zis)
                    if (!bytes) {
                        log.warn("Zip entry {} had no bytes", entry.name)
                        continue
                    }
                    Image image = imageService.createImage(new ByteArrayInputStream(bytes), stripPath(entry.name), owner, null)
                    if (image?.id) {
                        imageIds.add(image.id)
                    }
                } catch (Exception e) {
                    if (!firstError) {
                        firstError = e
                    }
                    log.warn("Skipping zip entry {}", entry?.name, e)
                } finally {
                    zis.closeEntry()
                }
            }
        }

        log.info("Zip '{}' produced {} image entries, stored {}", zipFilename, imageEntryCount, imageIds.size())

        if (!imageIds) {
            // Distinguish "zip had no images" from "images were found but failed to store"
            if (imageEntryCount > 0 && firstError) {
                throw new RuntimeException("Found ${imageEntryCount} image(s) in the zip but none could be stored: ${firstError.message}", firstError)
            }
            return null
        }

        Folder folder = new Folder(
                name: albumName,
                username: owner,
                imageIds: imageIds,
                createdDate: new Date()
        )
        return repository.create(folder)
    }

    private static byte[] readEntryBytes(ZipInputStream zis) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        byte[] buffer = new byte[8192]
        int read
        while ((read = zis.read(buffer)) != -1) {
            baos.write(buffer, 0, read)
        }
        return baos.toByteArray()
    }

    private static boolean isImageEntry(String entryName) {
        if (!entryName) {
            return false
        }
        String lower = entryName.toLowerCase()
        if (lower.contains('__macosx/')) {
            return false
        }
        if (stripPath(entryName).startsWith('.')) {
            return false
        }
        return IMAGE_EXTENSIONS.any { lower.endsWith(it) }
    }

    private static String stripPath(String entryName) {
        int slash = Math.max(entryName.lastIndexOf('/'), entryName.lastIndexOf('\\'))
        return slash >= 0 ? entryName.substring(slash + 1) : entryName
    }

    private static String deriveAlbumName(String zipFilename) {
        String base = stripPath(zipFilename ?: '')
        if (base.toLowerCase().endsWith('.zip')) {
            base = base.substring(0, base.length() - 4)
        }
        base = base.trim()
        return base ?: 'New Album'
    }

    @Override
    Folder renameFolder(String id, String name) {
        if (!name?.trim()) {
            throw new IllegalArgumentException('Folder name is required')
        }
        Folder folder = repository.get(id)
        if (!folder) {
            return null
        }
        folder.name = name.trim()
        return repository.update(id, folder)
    }

    @Override
    boolean deleteFolder(String id) {
        Folder folder = repository.get(id)
        if (!folder) {
            return false
        }
        repository.delete(id)
        return true
    }

    @Override
    Folder addImageToFolder(String folderId, String imageId) {
        Folder folder = repository.get(folderId)
        if (!folder) {
            return null
        }
        if (!imageService.getImage(imageId)) {
            return null
        }

        if (folder.imageIds == null) {
            folder.imageIds = []
        }
        if (!folder.imageIds.contains(imageId)) {
            folder.imageIds.add(imageId)
            return repository.update(folderId, folder)
        }
        return folder
    }

    @Override
    Folder removeImageFromFolder(String folderId, String imageId) {
        Folder folder = repository.get(folderId)
        if (!folder) {
            return null
        }
        if (folder.imageIds?.remove(imageId)) {
            return repository.update(folderId, folder)
        }
        return folder
    }

    @Override
    List<Folder> listFoldersForImage(String imageId) {
        if (!imageId) {
            return []
        }
        repository.list().findAll { it.imageIds?.contains(imageId) }.sort sortByCreatedDateDesc
    }

    @Override
    void removeImageFromAllFolders(String imageId) {
        if (!imageId) {
            return
        }
        repository.list().each { Folder folder ->
            if (folder.imageIds?.contains(imageId)) {
                try {
                    folder.imageIds.remove(imageId)
                    repository.update(folder.id, folder)
                } catch (Exception e) {
                    log.warn("Unable to remove image {} from folder {}", imageId, folder.id, e)
                }
            }
        }
    }

}
