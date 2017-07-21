/**
 * Created by zeon on 19.07.2017.
 */
public class Main {
   private static FileManager fileManager;
  private static SwingClass swingClass;
    public static void main(String[] args) {
        fileManager = new FileManager();
        swingClass = new SwingClass(fileManager);
        swingClass.creatNewFrame();


    }
}
