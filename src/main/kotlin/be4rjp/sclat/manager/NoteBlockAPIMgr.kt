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
    private var nBgm_C = 0
    private var fBgm_C = 0

    // private static byte volume = 22;
    private val nsList: MutableList<Song?> = ArrayList<Song?>()
    private val nsnList: MutableList<String?> = ArrayList<String?>()
    private val fsList: MutableList<Song?> = ArrayList<Song?>()
    private val fsnList: MutableList<String?> = ArrayList<String?>()

    fun LoadSongFiles() {
        for (songname in Sclat.Companion.conf!!.config!!.getConfigurationSection("nBGM")!!.getKeys(false)) {
            val song = NBSDecoder.parse(
                File(
                    "plugins/Sclat/BGM",
                    Sclat.Companion.conf!!.config!!.getString("nBGM." + songname),
                ),
            )
            nsList.add(song)
            nsnList.add(songname)
            nBgm_C++
        }

        for (songname in Sclat.Companion.conf!!.config!!.getConfigurationSection("fBGM")!!.getKeys(false)) {
            val song = NBSDecoder.parse(
                File(
                    "plugins/Sclat/BGM",
                    Sclat.Companion.conf!!.config!!.getString("fBGM." + songname),
                ),
            )
            fsList.add(song)
            fsnList.add(songname)
            fBgm_C++
        }
    }

    val randomNormalSong: NoteBlockSong
        get() {
            val random = Random().nextInt(nBgm_C)
            val songname = nsnList.get(random)
            val song = nsList.get(random)
            val nbs = NoteBlockSong(songname, song)
            return nbs
        }

    val randomFinalSong: NoteBlockSong
        get() {
            val random = Random().nextInt(fBgm_C)
            val songname = fsnList.get(random)
            val song = fsList.get(random)
            val nbs = NoteBlockSong(songname, song)
            return nbs
        }
}
