
import java.io.File;

/**
 * Created by zeon on 19.07.2017.
 */
public class FileManager {

    public String[] strings;
    File[] roots;
    FileManager()
    {
        roots =  File.listRoots();
    }

    public File[] getRoots() {
        return roots;
    }
    public File[] getChild(String path)
    {
        File file = new File(path);

if(file.isDirectory()&&file.listFiles()!=null)
{   File[] folderEntries = file.listFiles();

        return folderEntries;}
        else return null;

    }
    public File newDir(String parName,String dirName)
    {

        if(dirName.equals(""))
        {dirName= "newFolder";}
        File file = new File(parName+File.separator+dirName);
        file.mkdir();
        return file;
    }
    public File renameFile(String name,String newName)
    {
        File file = new File(name);
        file.renameTo(new File(file.getParent()+File.separator+newName));
        return file;
    }
    public boolean delete(String name)
    {
        File file = new File(name);
        return file.delete();
    }

}
