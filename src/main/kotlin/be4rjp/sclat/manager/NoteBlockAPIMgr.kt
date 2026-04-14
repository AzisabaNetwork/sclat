package be4rjp.sclat.manager

import be4rjp.sclat.Sclat
import be4rjp.sclat.data.NoteBlockSong
import com.xxmicloxx.NoteBlockAPI.model.Song
import com.xxmicloxx.NoteBlockAPI.utils.NBSDecoder
import java.io.File
import java.util.Random

/**
 *
 * @author Be4rJP
 */
object NoteBlockAPIMgr {
    private var nbgmC = 0
    private var fbgmC = 0

    // private static byte volume = 22;
    private val nsList: MutableList<Song?> = ArrayList()
    private val nsnList: MutableList<String?> = ArrayList()
    private val fsList: MutableList<Song?> = ArrayList()
    private val fsnList: MutableList<String?> = ArrayList()

    fun loadSongFiles() {
        for (songname in Sclat.conf!!
            .config!!
            .getConfigurationSection("nBGM")!!
            .getKeys(false)) {
            val song =
                NBSDecoder.parse(
                    File(
                        "plugins/Sclat/BGM",
                        Sclat.conf!!
                            .config!!
                            .getString("nBGM.$songname"),
                    ),
                )
            nsList.add(song)
            nsnList.add(songname)
            nbgmC++
        }

        for (songname in Sclat.conf!!
            .config!!
            .getConfigurationSection("fBGM")!!
            .getKeys(false)) {
            val song =
                NBSDecoder.parse(
                    File(
                        "plugins/Sclat/BGM",
                        Sclat.conf!!
                            .config!!
                            .getString("fBGM.$songname"),
                    ),
                )
            fsList.add(song)
            fsnList.add(songname)
            fbgmC++
        }
    }

    val randomNormalSong: NoteBlockSong
        get() {
            val random = Random().nextInt(nbgmC)
            val songname = nsnList[random]
            val song = nsList[random]
            val nbs = NoteBlockSong(songname, song)
            return nbs
        }

    val randomFinalSong: NoteBlockSong
        get() {
            val random = Random().nextInt(fbgmC)
            val songname = fsnList[random]
            val song = fsList[random]
            val nbs = NoteBlockSong(songname, song)
            return nbs
        }
}
