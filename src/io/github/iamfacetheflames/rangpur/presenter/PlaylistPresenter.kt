package io.github.iamfacetheflames.rangpur.presenter

import io.github.iamfacetheflames.rangpur.data.Audio
import io.github.iamfacetheflames.rangpur.data.AudioInPlaylist
import io.github.iamfacetheflames.rangpur.data.Playlist
import io.github.iamfacetheflames.rangpur.data.PlaylistFolder
import io.github.iamfacetheflames.rangpur.model.Models
import io.github.iamfacetheflames.rangpur.model.PlaylistToFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class PlaylistPresenter(val scope: CoroutineScope, val models: Models, val router: Router) {

    var selectedAudios = mutableListOf<AudioInPlaylist>()
    var currentPlaylist: Playlist? = null
    var currentFolder: PlaylistFolder? = null

    private val flowPlaylists = MutableStateFlow<List<Playlist>>(emptyList())
    private val flowPlaylistFolders = MutableStateFlow<List<PlaylistFolder>>(emptyList())
    private val flowAudioListFromPlaylist = MutableStateFlow<List<AudioInPlaylist>>(emptyList())

    fun observableAudioListFromPlaylist(): StateFlow<List<AudioInPlaylist>> = flowAudioListFromPlaylist
    fun observablePlaylists(): StateFlow<List<Playlist>> = flowPlaylists
    fun observablePlaylistFolders(): StateFlow<List<PlaylistFolder>> = flowPlaylistFolders

    fun requestAudioListForPlaylist(playlist: Playlist?) {
        scope.launch(Dispatchers.IO) {
            if (playlist != null) {
                val audios = models.audioLibrary.getAudios(playlist)
                flowAudioListFromPlaylist.value = audios
            }
        }
    }

    fun requestPlaylists(playlistFolder: PlaylistFolder? = null) {
        scope.launch(Dispatchers.IO) {
            selectedAudios = mutableListOf<AudioInPlaylist>()
            val playlists = models.playlistLibrary.getPlaylists(playlistFolder)
            flowPlaylists.value = playlists
        }
    }

    fun requestPlaylistFolders() {
        scope.launch(Dispatchers.IO) {
            val folders = models.playlistLibrary.getPlaylistFolders()
            flowPlaylistFolders.value = folders
        }
    }

    fun createPlaylistFolder(router: Router) {
        scope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                val name = router.openInputDialog("Название будущей папки:")
                withContext(Dispatchers.IO) {
                    if (name != null) {
                        models.playlistLibrary.createPlaylistFolder(name)
                        requestPlaylistFolders()
                    }
                }
            }
        }
    }

    fun createPlaylist(router: Router) {
        scope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                val name = router.openInputDialog("Название будущего плейлиста:")
                withContext(Dispatchers.IO) {
                    if (name != null) {
                        models.playlistLibrary.createPlaylist(name, currentFolder)
                        requestPlaylists(currentFolder)
                    }
                }
            }
        }
    }

    fun renamePlaylist(playlist: Playlist, router: Router) {
        scope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                val value = playlist.name
                val name = router.openInputDialog("Новое имя плейлиста:", value)
                withContext(Dispatchers.IO) {
                    if (name != null) {
                        models.playlistLibrary.renamePlaylist(name, playlist)
                        requestPlaylists(currentFolder)
                    }
                }
            }
        }
    }

    fun deletePlaylistFolder(folder: PlaylistFolder) {
        scope.launch(Dispatchers.IO) {
            withContext(Dispatchers.IO) {
                models.playlistLibrary.removePlaylistFolder(folder)
                requestPlaylistFolders()
            }
        }
    }

    fun deletePlaylist(playlist: Playlist) {
        scope.launch(Dispatchers.IO) {
            withContext(Dispatchers.IO) {
                models.playlistLibrary.removePlaylist(playlist)
                requestPlaylists(currentFolder)
            }
        }
    }

    fun addAudiosInCurrentPlaylist(audios: List<Audio>) {
        val playlist = currentPlaylist
        if (playlist != null) {
            models.playlistLibrary.addAudiosInPlaylist(audios, playlist)
            flowAudioListFromPlaylist.value = models.audioLibrary.getAudios(playlist)
        }
    }

    fun deleteAudiosFromCurrentPlaylist(audios: List<AudioInPlaylist>) {
        val playlist = currentPlaylist
        if (playlist != null) {
            models.playlistLibrary.deleteAudiosFromPlaylist(audios, playlist)
            val newAudioList = LinkedList<AudioInPlaylist>()
            newAudioList.addAll(flowAudioListFromPlaylist.value)
            newAudioList.removeAll(audios)
            flowAudioListFromPlaylist.value = newAudioList
        }
    }

    fun moveAudiosInPlaylistToNewPosition(selectedList: List<AudioInPlaylist>, movePosition: Int) {
        scope.launch(Dispatchers.IO) {
            val fullList = flowAudioListFromPlaylist.value
            if (
                selectedList.isEmpty() ||
                    selectedList.containsAll(fullList)
            ) {
                return@launch
            }
            val isUp = movePosition < selectedList.first().position
            val nullableResultList = LinkedList<AudioInPlaylist?>()
            nullableResultList.addAll(
                fullList
            )
            selectedList.forEach {
                nullableResultList.set(
                    nullableResultList.indexOf(it),
                    null
                )
            }
            nullableResultList.addAll(
                if (isUp) {
                    movePosition
                } else {
                    movePosition + 1
                },
                selectedList
            )
            val finalList = nullableResultList.filterNotNull()
            models.database.moveAudiosInPlaylistToNewPosition(finalList)
            flowAudioListFromPlaylist.value = finalList
        }
    }

    fun exportPlaylistToTextFile(playlist: Playlist) {
        scope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                val file = router.openSaveFileDialog("Куда сохранить:", "${playlist.name}.txt", "text file","txt") ?: return@withContext
                withContext(Dispatchers.IO) {
                    val audios = models.database.getPlaylistAudios(playlist)
                    PlaylistToFile.exportPlaylistTxt(file.name, file.parentFile.absolutePath, audios)
                }
            }
        }
    }

}