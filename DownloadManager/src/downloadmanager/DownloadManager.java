package downloadmanager;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.ImageIcon;

/**
 *
 * @author Shadman
 */

public class DownloadManager extends JFrame
  implements Observer
{   
  // Adding textfield for download url
  private JTextField addTextField;
  
  private JFileChooser jf;
  private String path = System.getProperty("user.dir");

  // DownloadTable's data model
  private DownloadsTableModel tableModel;
  
  // Table that lists downloads
  private JTable table;
  
  // Add download button
  private JButton addButton;

  // Buttons for managing selected download
  private JButton pauseButton, resumeButton;
  private JButton cancelButton, clearButton;

  // Currently selected download
  private Download selectedDownload;

  // Flag for whether or not table selection is being cleared.
  private boolean clearing;
  
  // Panels to place other components
  
  JPanel addPanel, downloadsPanel, buttonsPanel;
  
  // Icon Image
  
  private ImageIcon icon = new ImageIcon(System.getProperty("user.dir")+"\\handprintOutline.png");

  
  /**********************  END OF VARIABLE DECLARATION  ***************************/
  
  
  // Constructor
  public DownloadManager()
  {
      
    /************************* INITIALIZE GUI ******************************/       
      
    // Set title
    setTitle("Download Manager"); 
    
    // Set Icon
     setIconImage(icon.getImage());
    
    // Set window size
    setSize(640, 480);

    // Handle window closing events.
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        actionExit();
      }
    });
    
    /***** MENU BAR ********/
    
    // File menu
    JMenuBar menuBar = new JMenuBar();
    JMenu fileMenu = new JMenu("File");
    fileMenu.setMnemonic(KeyEvent.VK_F);
    JMenuItem fileExitMenuItem = new JMenuItem("Exit",
      KeyEvent.VK_X);
    fileExitMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        actionExit();
      }
    });
    
    // Edit Menu
    
     JMenu editMenu = new JMenu("Edit");
     JMenuItem changeColorItem = new JMenuItem("Change Interface Color");
     changeColorItem.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e){
            actionChangeColor();
        }
     });
     
     editMenu.add(changeColorItem);
     
     // Create File Menu items
    
    JMenuItem chooseDirectoryItem = new JMenuItem("Choose Directory");
    chooseDirectoryItem.addActionListener(new ActionListener(){
       public void actionPerformed(ActionEvent e){
          actionChooseDirectory();
       }
    });
    
    JMenuItem downloadFolderItem = new JMenuItem("Downloads Folder");
    downloadFolderItem.addActionListener(new ActionListener(){
     public void actionPerformed(ActionEvent e){
         actionOpenDownloadsFolder();
     }
    });
    
    // Add items to file menu
    fileMenu.add(chooseDirectoryItem);
    fileMenu.add(downloadFolderItem);
    fileMenu.add(fileExitMenuItem);
    
    // Add menus to menu bar
    menuBar.add(fileMenu);
    menuBar.add(editMenu);
    setJMenuBar(menuBar);

    /******** ADD PANEL ***********/
    
    addPanel = new JPanel();
    
    // Add textfield to add Panel
    addTextField = new JTextField(30);
    addTextField.addKeyListener(new KeyListener() {
        
     // Enable JButton when text entered into textfield   
        @Override
        public void keyTyped(KeyEvent e) {    
          actionUpdateDownloadButton();
        }         
        @Override
        public void keyPressed(KeyEvent e) {
        }
        @Override
        public void keyReleased(KeyEvent e) {
       }
    }

    );
    addPanel.add(addTextField);
    addButton = new JButton("Add Download");
    addButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        actionAdd();
      }
    });
    addButton.setEnabled(false);
    addPanel.add(addButton);
    
    // SET UP TABLE!! 
    
    tableModel = new DownloadsTableModel();
    table = new JTable(tableModel);
    
    // Listens for change in selection of table row
    table.getSelectionModel().addListSelectionListener(new
      ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        tableSelectionChanged();
      }
    });
    table.addMouseListener(new MouseListener(){
        @Override
        public void mouseClicked(MouseEvent e) {
            if(e.getModifiers() == InputEvent.BUTTON2_MASK)
            {
              JPopupMenu jpop = new JPopupMenu();
              JMenuItem item = new JMenuItem("File Location");
              item.addActionListener(new ActionListener(){
       public void actionPerformed(ActionEvent e){
            Desktop d = Desktop.getDesktop();
            
            File f = new File(selectedDownload.getDirectory(selectedDownload.getName()));
            
           try {
               d.open(f);
           } catch (IOException ex) {
               Logger.getLogger(DownloadManager.class.getName()).log(Level.SEVERE, null, ex);
           }
       }
    });
              jpop.add(item);
              
              
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void mouseExited(MouseEvent e) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    
    });
    
    
    
    // Allow only one row at a time to be selected
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
  
    // SET UP PROGRESS BAR AS RENDERER FOR TABLE PROGRESS COLUMN !! 
    
    ProgressRenderer renderer = new ProgressRenderer(0, 100);
    renderer.setStringPainted(true); // show progress text
    table.setDefaultRenderer(JProgressBar.class, renderer);
         
    // Set table's row height large enough to fit JProgressBar
    table.setRowHeight((int) renderer.getPreferredSize().getHeight());
    
    
    /****** DOWNLOAD PANEL ***********/

    // Set up downloads panel
    downloadsPanel = new JPanel();
    downloadsPanel.setBorder(
      BorderFactory.createTitledBorder("Downloads"));
    downloadsPanel.setLayout(new BorderLayout());
    downloadsPanel.add(new JScrollPane(table),
      BorderLayout.CENTER);
    
    
    /******** BUTTONS PANEL **********/ 
    
    // Set up buttons panel
    buttonsPanel = new JPanel();
    
    pauseButton = new JButton("Pause");
    pauseButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        actionPause();
      }
    });
    pauseButton.setEnabled(false);
    buttonsPanel.add(pauseButton);
    
    resumeButton = new JButton("Resume");
    resumeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        actionResume();
      }
    });
    resumeButton.setEnabled(false);
    buttonsPanel.add(resumeButton);
    
    cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        actionCancel();
      }
    });
    cancelButton.setEnabled(false);
    buttonsPanel.add(cancelButton);
    
    clearButton = new JButton("Clear");
    clearButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        actionClear();
      }
    });
    clearButton.setEnabled(false);
    buttonsPanel.add(clearButton);

    // Add panels to display
    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(addPanel, BorderLayout.NORTH);
    getContentPane().add(downloadsPanel, BorderLayout.CENTER);
    getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
  }
  // Constructor ends
  
  
  /******** ACTIONS/METHODS ***************/
  
  // Exit program
  private void actionExit() {
    System.exit(0);
  }  
 
  // Add a new download.
  private void actionAdd() {
    URL verifiedUrl = verifyUrl(addTextField.getText());
    
    ProcessBuilder p = new ProcessBuilder("C:\\Users\\Shadman\\Documents\\NetBeansProjects\\JavaApplication51\\src\\javaapplication51\\JavaApplication51.java"); 
      try {
          p.start();
      } catch (IOException ex) {
      }
    
    if (verifiedUrl != null) {
      tableModel.addDownload(new Download(verifiedUrl, path));
      addTextField.setText(""); // reset add text field
    } else {
      JOptionPane.showMessageDialog(this,
        "Invalid Download URL", "Error",
        JOptionPane.ERROR_MESSAGE);
    }
  }
  
  // Choose directory of download files
  private void actionChooseDirectory()
  {
      jf = new JFileChooser(path);
      jf.showOpenDialog(null);
         
      // Only allow choosing directory
      jf.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      // Disallow choosing other type of files 
      jf.setAcceptAllFileFilterUsed(false);
         
      // Get current directory
      File f = jf.getCurrentDirectory();
      // Set path 
      path = f.getAbsolutePath();
  }
  
  // Change the color of interface
  private void actionChangeColor()
  {
   java.awt.Color c = javax.swing.JColorChooser.showDialog(this, "Choose color", Color.yellow);
   addPanel.setBackground(c);
   downloadsPanel.setBackground(c);
   buttonsPanel.setBackground(c);
  }
 
  // Open the current downloads folder
  private void actionOpenDownloadsFolder()
  {
     Desktop d = Desktop.getDesktop();
     File f = new File(path);
     
     try{
       d.open(f);
     }
     catch(IOException ex){
    }
  }
  
  // Update download button depending on textfield having content or not 
  private void actionUpdateDownloadButton()
  {
    if(addTextField.getText().equals(""))
        addButton.setEnabled(false);
    else
        addButton.setEnabled(true);
  }
 
  
  // Verify download URL.
  private URL verifyUrl(String url) {
    // Only allow HTTP URLs.
    if (!url.toLowerCase().startsWith("http://") && !url.toLowerCase().startsWith("https://"))
      return null;
       
    // Verify format of URL.
    URL verifiedUrl = null;
    try {
      verifiedUrl = new URL(url);
    } catch (Exception e) {
      return null;
    }

    // Make sure URL specifies a file.
    if (verifiedUrl.getFile().length() < 2)
     return null;

    return verifiedUrl;
  }

  // Called when table row selection changes.
  private void tableSelectionChanged() {
    /* Unregister from receiving notifications
       from the last selected download. */
    if (selectedDownload != null)
      selectedDownload.deleteObserver(DownloadManager.this);

    /* If not in the middle of clearing a download,
       set the selected download and register to
       receive notifications from it. */
    
    // BUG FIX! (NO arrayoutofbounds exception after clearing)
    if (!clearing && table.getSelectedRow()>=0) {
      selectedDownload = 
        tableModel.getDownload(table.getSelectedRow());
      selectedDownload.addObserver(DownloadManager.this);
      updateButtons();
    }
  }

  // Pause selected download
  private void actionPause() {
    selectedDownload.pause();
    updateButtons();
  }

  // Resume selected download
  private void actionResume() {
    selectedDownload.resume();
    updateButtons();
  }

  // Cancel selected download
  private void actionCancel() {
    selectedDownload.cancel();
    updateButtons();
  }

  // Clear selected download
  private void actionClear() {
    clearing = true;
    tableModel.clearDownload(table.getSelectedRow());
    clearing = false;
    selectedDownload = null;
    updateButtons();
  }

  /* Update each button's state based off of the
     currently selected download's status. */
  private void updateButtons() {
    if (selectedDownload != null) {
      int status = selectedDownload.getStatus();
      switch (status) {
        case Download.DOWNLOADING:
          pauseButton.setEnabled(true);
          resumeButton.setEnabled(false);
          cancelButton.setEnabled(true);
          clearButton.setEnabled(false);
          break;
        case Download.PAUSED:
          pauseButton.setEnabled(false);
          resumeButton.setEnabled(true);
          cancelButton.setEnabled(true);
          clearButton.setEnabled(false);
          break;
        case Download.ERROR:
          pauseButton.setEnabled(false);
          resumeButton.setEnabled(true);
          cancelButton.setEnabled(false);
          clearButton.setEnabled(true);
          break;
        default: // means download COMPLETE or CANCELLED
          pauseButton.setEnabled(false);
          resumeButton.setEnabled(false);
          cancelButton.setEnabled(false);
          clearButton.setEnabled(true);
      }
    } else {
      // No download is selected in table.
      pauseButton.setEnabled(false);
      resumeButton.setEnabled(false);
      cancelButton.setEnabled(false);
      clearButton.setEnabled(false);
    }
  }
 
    public void update(Observable o, Object arg) {
        
        // Update buttons if the selected download has changed
        if (selectedDownload != null && selectedDownload.equals(o))
             updateButtons();
    }

    
    // MAIN METHOD
    public static void main(String[] args) 
    {
       DownloadManager manager = new DownloadManager();
       manager.show();
    }
}
