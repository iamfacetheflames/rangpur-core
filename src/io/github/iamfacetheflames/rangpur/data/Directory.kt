package io.github.iamfacetheflames.rangpur.data

import io.github.iamfacetheflames.rangpur.module.Configuration
import java.io.File

interface Directory : WithId {
    var parent: Directory?
    var locationInMusicDirectory: String?
    var name: String?
}

fun Directory.getJavaFile(config: Configuration): File {
    val libraryLocation = config.getMusicDirectoryLocation()
    val fullPath = libraryLocation + this.locationInMusicDirectory
    val file = File(fullPath)
    return file
}