package io.github.iamfacetheflames.rangpur.data

interface Playlist {
    var id: Long
    var name: String?
    var timestampCreated: Long
    var folderId: Long
}