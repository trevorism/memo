package com.trevorism.service

import com.trevorism.model.Folder
import com.trevorism.model.Image

interface FolderService {

    List<Folder> listFolders()

    Folder getFolder(String id)

    List<Image> listFolderImages(String id)

    Folder createFolder(String name, String username)

    Folder renameFolder(String id, String name)

    boolean deleteFolder(String id)

    Folder addImageToFolder(String folderId, String imageId)

    Folder removeImageFromFolder(String folderId, String imageId)

    List<Folder> listFoldersForImage(String imageId)

    void removeImageFromAllFolders(String imageId)
}
