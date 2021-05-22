package io.github.iamfacetheflames.rangpur.data

class RootPlaylistFolder(
    override var id: Long = 0,
    override var name: String? = "All",
    override var timestampCreated: Long = 0,
    override var parentId: Long = 0
) : PlaylistFolder {

    override fun toString(): String {
        return name ?: super.toString()
    }

}