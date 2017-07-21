import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import static javax.swing.JFrame.EXIT_ON_CLOSE;

/**
 * Created by zeon on 19.07.2017.
 */
public class SwingClass {
    private JFrame jframe;
    private MyTree tree;
    private FileManager fileManager;
    private DefaultTreeModel model;
    private JTextArea textArea;
    private DefaultMutableTreeNode defaultMutableTreeNode;
    private JPanel jPanelFilesOper;
    private static JFrame enterName;
    private static TimerForTree timerForTree;
    private DefaultTreeCellRenderer renderer = new MyRenderer();
    private TreePath helperTreePath;


    SwingClass(FileManager fileManager)
    {this.fileManager = fileManager;
    }



    TreeSelectionListener treeSelectionListener = new TreeSelectionListener() {      //Слушатель выбора в tree
        public void valueChanged(TreeSelectionEvent tse){
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)tse.getPath().getLastPathComponent();
            nodeCreator(node,fileManager.getChild(node.toString()));

        }
    };

    public class ButtonNewFolderListener implements ActionListener {                //Слушатель кнопки New Folder

        @Override
        public void actionPerformed(ActionEvent e) {
            EnterName("");
        }
    }
    public class ButtonRenameListener implements ActionListener {                   //Слушатель кнопки Rename

        @Override
        public void actionPerformed(ActionEvent e) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
            File file =(File) node.getUserObject();
            EnterName(file.getName());
        }
    }

    public class ButtonOkForRenameListener implements ActionListener                 //Слушатель кнопки OK доп.окна
    {                                                                               // при Переименовании
        @Override
        public void actionPerformed(ActionEvent e) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();

            TreePath   treePath= tree.getSelectionPath().getParentPath();
            nodeCreator((DefaultMutableTreeNode)node.getParent(),fileManager.renameFile(node.toString(),textArea.getText()));
            model.reload();
            tree.setSelectionPath(treePath);
            tree.fireTreeExpandedRun(treePath);
            enterName.dispose();

        }
    }

    public class ButtonOkForNewFolderListener implements ActionListener            //Слушатель кнопки OK доп.окна
    {                                                                              // при создании новой папки
        @Override
        public void actionPerformed(ActionEvent e) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
            TreePath treePath=tree.getSelectionPath();
            if(!node.getAllowsChildren())
            {node = (DefaultMutableTreeNode)node.getParent();
                treePath= tree.getSelectionPath().getParentPath();
            }

            nodeCreator(node,fileManager.newDir(node.toString(),textArea.getText()));

            model.reload();
            tree.setSelectionPath(treePath);
            tree.fireTreeExpandedRun(treePath);
            enterName.dispose();

        }
    }
    public class ButtonDeleteListener implements ActionListener                 //Слушатель кнопки Delete
    {
        @Override
        public void actionPerformed(ActionEvent e) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
            TreePath   treePath= tree.getSelectionPath().getParentPath();
            node.removeFromParent();
            model.reload();
            tree.setSelectionPath(treePath);
            tree.fireTreeExpandedRun(treePath);


        }
    }



   public void creatNewFrame() {                           //Создаём главное окно программы и все доп компоненты для его работы

 jframe = new JFrame("FileManager");
      jframe.setDefaultCloseOperation(EXIT_ON_CLOSE);
       JPanel panel = new JPanel();
       panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
     jframe.setLayout(null);
       jframe.setSize(400,600);
       jframe.setLayout(new BorderLayout());
       JButton nfbutton = new JButton("New Folder");
       JButton rbutton = new JButton("Rename");
       JButton dbutton = new JButton("Delete");
       nfbutton.addActionListener(new ButtonNewFolderListener());
       rbutton.addActionListener(new ButtonRenameListener());
       dbutton.addActionListener(new ButtonDeleteListener());
       textArea = new JTextArea("newFolder");

       jPanelFilesOper = new JPanel ();                     //Инициализация панели для размещения кнопок
       jPanelFilesOper.setLayout(new BoxLayout(jPanelFilesOper,BoxLayout.X_AXIS));

       jPanelFilesOper.add(nfbutton);
       jPanelFilesOper.add(rbutton);
       jPanelFilesOper.add(dbutton);
       defaultMutableTreeNode = new DefaultMutableTreeNode(new File(""),true);
       for (File file:fileManager.getRoots())
       {defaultMutableTreeNode.add(new DefaultMutableTreeNode(file,true));}

       model= new DefaultTreeModel(defaultMutableTreeNode);

       tree = new MyTree(model);                            //Создаём дерево на основе модели и таймер открытия для него
       timerForTree=new TimerForTree(tree);
       timerForTree.setDaemon(true);
       timerForTree.start();                                //Запускаем вторую нить(поток)


       tree.setCellRenderer(renderer);
       tree.addTreeSelectionListener(treeSelectionListener);
       tree.setName("PC");
       JScrollPane treeScrollPane = new JScrollPane(tree, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);


       panel.add(treeScrollPane);
       jframe.add(jPanelFilesOper,BorderLayout.NORTH);
       jframe.add(panel);
       jframe.setLocationRelativeTo(null);
       jframe.setVisible(true);
    }
                                                            //Метод создания узлов дерева
    private void nodeCreator(DefaultMutableTreeNode node,File... files)
    {node.removeAllChildren();
        if(files!=null)
        for (File file: files) {
            if(file.isDirectory())
            node.add(new DefaultMutableTreeNode(file));
            else
            node.add(new DefaultMutableTreeNode(file));
        }
    }

    class MyTree extends JTree                               //Переопределение класса дерева
    {
        public MyTree(TreeModel newModel) {
            super(newModel);
        }

        @Override
        public void fireTreeExpanded(TreePath path) {        //Переопределение метода для вставки таймаута в его работу
            timerForTree.setTreePath(path);
             synchronized (timerForTree) {timerForTree.notify();}

        }
        public void fireTreeExpandedRun(TreePath path)       //Метод вызываемый классом TimerForTree
        {super.fireTreeExpanded(path);}

    }

    class TimerForTree extends Thread                        //Класс для создания таймаута открытия папок
    {
       private TreePath treePath;
       private MyTree myTree;

      public TimerForTree(MyTree myTree)
        {this.myTree = myTree;}

        public void setTreePath(TreePath treePath) {
            this.treePath = treePath;
        }

        public void run() {
         while (true)
         {synchronized (this){
             try {if(treePath!=null)
                    {   helperTreePath=treePath;              //Установим путь для файла на папки на которой будет заменена иконка
                        sleep(2000);                    //Установка таймаута
                        helperTreePath=null;                  //Очистим путь для файла на папки на которой будет заменена иконка
                    myTree.fireTreeExpandedRun(treePath);
                    treePath=null;}
            else this.wait();
             } catch (Exception e)
             {e.printStackTrace();}
         }}
        }
    }


    class MyRenderer extends DefaultTreeCellRenderer {        // Переопределение класс отрисовки компанента для tree

        private FileSystemView fileSystemView;
        private JLabel label;

        MyRenderer() {
            label = new JLabel();
            label.setOpaque(true);
            fileSystemView = FileSystemView.getFileSystemView();
        }


        @Override
        public Component getTreeCellRendererComponent(
                JTree tree,
                Object value,
                boolean selected,
                boolean expanded,
                boolean leaf,
                int row,
                boolean hasFocus) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
            File file = (File)node.getUserObject();
            if(!file.isDirectory())
                   {label.setIcon(fileSystemView.getSystemIcon(file));}
            else {
                  if(expanded)
                    label.setIcon(new ImageIcon("src/resources/folderOp.png"));   //Установка иконки открытой папки
                  else
                  {
                      String st1=helperTreePath==null?"":helperTreePath.getLastPathComponent().toString();
                      String st2=((DefaultMutableTreeNode) value).toString();

                        if(st1.equals(st2) && helperTreePath!=null)                       //Проверяем совпадает ли путь открытия с путём элемента
                        label.setIcon(new ImageIcon("src/resources/clock.png")); //Установка иконки ожидания
                        else
                        label.setIcon(new ImageIcon("src/resources/folderCl.png"));//Установка иконки закрытой папки
                  }

                 }
            label.setText(fileSystemView.getSystemDisplayName(file));
            label.setToolTipText(file.getPath());
            if (selected)
                label.setBackground(backgroundSelectionColor);
             else
                label.setBackground(backgroundNonSelectionColor);
            return label;
        }
    }

    public void EnterName(String rename)                              //Создание дополнительного окна для ввода имени папки или файла
    {
        if(enterName!=null)
        enterName.dispose();
        enterName = new JFrame("Folder name");
        JLabel pLable;
        JButton button = new JButton("Ok");
        if(rename.equals(""))                                         //Если переданно пустое имя, то создаём шаблон создания папки...
        { textArea = new JTextArea("NewFolder");
            pLable = new JLabel("Enter folder name:");
            button.addActionListener(new ButtonOkForNewFolderListener());}
        else                                                           //...Иначе созаём шаблон переименнования
        {textArea = new JTextArea(rename);
            pLable = new JLabel("Enter new name:");
            button.addActionListener(new ButtonOkForRenameListener());}


            //enterName.setBounds(0,0,150,130);
            pLable.setForeground(Color.white);
            enterName.setSize(150, 130);
            enterName.setDefaultCloseOperation(jframe.DISPOSE_ON_CLOSE);
            enterName.setLocationRelativeTo(null);
            enterName.add(pLable, BorderLayout.NORTH);
            enterName.add(button, BorderLayout.SOUTH);
            enterName.add(textArea, BorderLayout.CENTER);
            enterName.getContentPane().setBackground(new Color(120, 100, 140));
            enterName.setVisible(true);
            enterName.setFocusable(true);
    }



}
