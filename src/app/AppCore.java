package app;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import gui.Menu;
import gui.Toolbar;
import gui.ToolbarDrives;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import model.db.DBMetadata;
import model.db.DBNode;
import model.db.DBTree;
import view.DBLogin;
import view.FileView;
import view.ListaView;
import actions.ActionManager;

public class AppCore extends JFrame {

	private static final long serialVersionUID = 1L;
	
	private static AppCore instance = null;
	private ActionManager actionManager;
	private ListaView lista;
	private Menu menu;
	private Toolbar toolbar;
	private ToolbarDrives toolbarDrives;
	private FileView fileView;
	private JTabbedPane framework;
	
	private String currentPath;
	private JLabel currentPathLabel;
	private JLabel infoSizeLabel;
	private JLabel infoDateLabel;
	
	 //atributi za rad sa bazom podataka
    private Connection conn=null;
    private DBTree dbTree=null;
	
	private AppCore(){
		
	}

	private void initialise(){
		initialiseList();
		initialiseDBTree();
    	initialiseGUI();			
    }
	
	private void initialiseGUI(){
		actionManager = new ActionManager();
		
		setSize(1200,750);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setTitle("UI Project");
		
		//TOP
		menu = new Menu();
		setJMenuBar(menu);
		
		JPanel panTop = new JPanel();
		panTop.setPreferredSize(new Dimension(50,60));
		panTop.setLayout(new BorderLayout());
		
		toolbar = new Toolbar();
		panTop.add(toolbar, BorderLayout.NORTH);
		
		toolbarDrives = new ToolbarDrives();
		panTop.add(toolbarDrives, BorderLayout.CENTER);
		
		//CENTER -- LEFT
		JPanel panLeft = new JPanel();
		panLeft.setPreferredSize(new Dimension(100,100));
		panLeft.setLayout(new BorderLayout());
		
		JScrollPane filenavigator=new JScrollPane(lista);
	    filenavigator.setPreferredSize(new Dimension(100,100));
	    TitledBorder fileBorder = BorderFactory.createTitledBorder("File Explorer");
	    filenavigator.setBorder(fileBorder);
	    
	    JScrollPane dbnavigator=new JScrollPane(dbTree);
	    dbnavigator.setVisible(true);
	    TitledBorder dbBorder = BorderFactory.createTitledBorder("DB Explorer");
	    dbnavigator.setBorder(dbBorder);
	    
	    JSplitPane splitNavigator=new JSplitPane(JSplitPane.VERTICAL_SPLIT,filenavigator,dbnavigator);
	    splitNavigator.setDividerLocation(250);
		
		panLeft.setLayout(new BorderLayout());
		panLeft.add(splitNavigator,BorderLayout.CENTER);
		
		//CENTER -- LEFT -- TOP
		JPanel currentPathPanel = new JPanel();
		currentPathLabel = new JLabel(" ",SwingConstants.LEFT);
		currentPathPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		currentPathPanel.add(currentPathLabel);
		
		panLeft.add(currentPathPanel, BorderLayout.NORTH);

		//CENTER -- LEFT -- BOTTOM
		JPanel infoPanel = new JPanel();
		infoDateLabel = new JLabel(" Date: ", SwingConstants.LEFT);
		infoSizeLabel = new JLabel(" Size: ", SwingConstants.LEFT);
		infoPanel.setLayout(new GridLayout(1,2));
		infoPanel.add(infoDateLabel);
		infoPanel.add(infoSizeLabel);
		
		panLeft.add(infoPanel, BorderLayout.SOUTH);
		
		//CENTER -- RIGHT
		framework = new JTabbedPane();
		framework.setBackground(Color.GRAY);
				
		//CENTER
		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panLeft, framework);
		split.setDividerLocation(300);
		split.setOneTouchExpandable(true);
		
		//BOTTOM
		JPanel panBottom = new JPanel();
		panBottom.setPreferredSize(new Dimension(100,50));
				
		
		getContentPane().setLayout(new BorderLayout());
		
		getContentPane().add(panTop, BorderLayout.NORTH);
		getContentPane().add(split, BorderLayout.CENTER);
		getContentPane().add(panBottom, BorderLayout.SOUTH);
		
		try{
    		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    		SwingUtilities.updateComponentTreeUI(this);
		} catch (Exception e) {
    		e.printStackTrace();
    	}
	}
	
	private void initialiseList(){
		lista = new ListaView();
	}
	
	private void initialiseDBTree(){
		dbTree=new DBTree();
	}
	
	public static AppCore getInstance(){
		if(instance == null){
			instance = new AppCore();
			instance.initialise();
		}
		return instance;
	}

	public ActionManager getActionManager() {
		return actionManager;
	}

	public ListaView getLista() {
		return lista;
	}

	public void setLista(ListaView lista) {
		this.lista = lista;
	}

	public FileView getFileView() {
		return fileView;
	}

	public void setFileView(FileView fileView) {
		this.fileView = fileView;
		Icon icon = new ImageIcon(getClass().getResource("../actions/images/tab.png"));
		framework.addTab(fileView.getUiFile().getFileName(), icon, fileView, null);
		framework.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		framework.setSelectedComponent(fileView);
		

		getActionManager().getCloseFileAction().setEnabled(true);
		getActionManager().getCloseAllFileAction().setEnabled(true);
		getActionManager().getSaveAsFileAction().setEnabled(true);
		SwingUtilities.updateComponentTreeUI(this);
	}

	public JTabbedPane getFramework() {
		return framework;
	}

	public void setFramework(JTabbedPane framework) {
		this.framework = framework;
	}

	public String getCurrentPath() {
		return currentPath;
	}

	public void setCurrentPath(String currentPath) {
		this.currentPath = currentPath;
		currentPathLabel.setText(currentPath);
	}
	
	public void setFileInfo(String infoSize, String infoDate){
		infoDateLabel.setText(" Date: " + infoDate);
		infoSizeLabel.setText(" Size: " + infoSize);
	}
			
	
	public void DBLogin(){
		DBLogin dbConnection=new DBLogin();
		if (dbConnection.connect==true){
		
		String serverName=dbConnection.getTxtServerName().getText();
		String databaseName=dbConnection.getTxtDatabaseName().getText();
		String userName=dbConnection.getTxtUserName().getText();
		char[] pass=dbConnection.getTxtPassword().getPassword();
		
		String password=new String(pass);
		if (openConnection(serverName,databaseName, userName, password)){
			//uspesna konekcija, procitati tabele iz baze podataka
			DBNode dbRootNode=(DBNode) dbTree.getModel().getRoot();
			
			dbRootNode.setName("Server:"+serverName+", Dabase:"+ databaseName);
			SwingUtilities.updateComponentTreeUI(this);
			DBMetadata dbMetaData=new DBMetadata();
			try {
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				dbMetaData.readDatabase();
			} catch (SQLException e) {
				e.printStackTrace();
			}finally{
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
			
		 }
		}
		
		
	}
	public boolean openConnection(String serverName,String databaseName,String userName,String password){
		boolean result=false;
		try {
			Class.forName("net.sourceforge.jtds.jdbc.Driver");
			String url="jdbc:jtds:sqlserver://"+serverName+"/"+databaseName;

			conn=DriverManager.getConnection(url, userName, password);

			result=true;
			
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "UI Project", 1);
		}
		return result;
		
		
	}

	public Connection getConn() {
		return conn;
	}

	public void setConn(Connection conn) {
		this.conn = conn;
	}

	public DBTree getDBTree() {
		return dbTree;
	}

}
