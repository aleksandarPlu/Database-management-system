package actions;

public class ActionManager {

	//File
	private NewFileAction 		newFileAction;
	private OpenDirAction		openDirAction;
	private SaveFileAction  	saveFileAction;
	private SaveAsFileAction	saveAsFileAction;
	private CloseFileAction		closeFileAction;
	private CloseAllFileAction	closeAllFileAction;
	private DeleteAction		deleteAction;
	
	//Database
	private DBLoginAction		dbLoginAction;
	
	//Help
	private HelpAboutAction		helpAboutAction;
	
	public ActionManager(){
		initAction();
	}
	
	void initAction(){
		//File
		newFileAction		= new NewFileAction();
		openDirAction		= new OpenDirAction("");
		saveFileAction		= new SaveFileAction();
		saveFileAction.setEnabled(false);
		saveAsFileAction	= new SaveAsFileAction();
		saveAsFileAction.setEnabled(false);
		closeFileAction		= new CloseFileAction();
		closeFileAction.setEnabled(false);
		closeAllFileAction	= new CloseAllFileAction();
		closeAllFileAction.setEnabled(false);
		deleteAction 		= new DeleteAction();
		deleteAction.setEnabled(false);
		
		//Database
		dbLoginAction 		= new DBLoginAction();
		
		//Help
		helpAboutAction		= new HelpAboutAction();
	}

	
	
	public NewFileAction getNewFileAction() {
		return newFileAction;
	}

	public OpenDirAction getOpenDirAction() {
		return openDirAction;
	}

	public SaveFileAction getSaveFileAction() {
		return saveFileAction;
	}

	public CloseFileAction getCloseFileAction() {
		return closeFileAction;
	}

	public CloseAllFileAction getCloseAllFileAction() {
		return closeAllFileAction;
	}

	public HelpAboutAction getHelpAboutAction() {
		return helpAboutAction;
	}

	public DeleteAction getDeleteAction() {
		return deleteAction;
	}

	public SaveAsFileAction getSaveAsFileAction() {
		return saveAsFileAction;
	}

	public DBLoginAction getDbLoginAction() {
		return dbLoginAction;
	}
	
}
