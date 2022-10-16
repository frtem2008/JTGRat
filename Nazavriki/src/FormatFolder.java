//Class for music folder formatting.
//Is used by hand, or by bot when new music is received

import java.io.File;
import java.util.Objects;

public class FormatFolder {
    public static void main(String[] args) {
        File music = new File("E:\\Программы\\Idea Projects\\JTGRat\\Music");
        File[] musicFolders = music.listFiles();
        System.out.println("Formatting music");
        for (int i = 0; i < Objects.requireNonNull(musicFolders).length; i++) {
            //formatting each folder with music
            File toFormat = musicFolders[i];
            System.out.println(toFormat.getAbsolutePath());
            formatFolder(toFormat);
        }
        System.out.println("Formatted successfully");
    }

    //formatting music folder method
    public static void formatFolder(File toFormat) {
        File[] files = toFormat.listFiles();
        //replacing filename.mp3 with filenumber.mp3
        for (int i = 0; i < Objects.requireNonNull(files).length; i++) {
            File tmp = new File(toFormat.getAbsolutePath() + "/" + (i + 1) + ".mp3");
            System.out.println(files[i].renameTo(tmp) + " " + files[i].getName() + " renamed");
        }
    }
}
