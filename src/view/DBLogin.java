package view;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class DBLogin extends JDialog{
	private JLabel lblServerName=new JLabel("Server Name:");
	private JLabel lblDatabaseName=new JLabel("Database Name:");
	private JLabel lblAuth=new JLabel("Authentication:");
	JRadioButton rbtnWindows = new JRadioButton("Windows");
	JRadioButton rbtnSQL= new JRadioButton("SQL Server");

	
	private JLabel lblUserName=new JLabel("Login:");
	private JLabel lblPassword=new JLabel("Password:");
	private JTextField txtServerName=new JTextField(20);
	private JTextField txtDatabaseName=new JTextField(20);
	private JTextField txtUserName=new JTextField(10);
	private JPasswordField txtPassword=new JPasswordField(10);
	private JButton btnOk=new JButton("Connect");
	private JButton btnCancel=new JButton("Cancel");
	
	public boolean connect=false;
	
	
	public DBLogin() {
		super();
		setLayout(new GridLayout(6,1));
		JPanel pnl1=new JPanel(new FlowLayout(FlowLayout.LEFT));
		pnl1.add(lblServerName);
		pnl1.add(txtServerName);
		add(pnl1);
		txtServerName.setText("Aleksandar");
		
		
		JPanel pnl2=new JPanel(new FlowLayout(FlowLayout.LEFT));
		pnl2.add(lblDatabaseName);
		pnl2.add(txtDatabaseName);
		add(pnl2);
		txtDatabaseName.setText("UI");
		
		JPanel pnl3=new JPanel(new FlowLayout(FlowLayout.LEFT));
		pnl3.add(lblAuth);
		ButtonGroup group = new ButtonGroup();
	    group.add(rbtnWindows);
	    group.add(rbtnSQL);
	    pnl3.add(rbtnWindows);
	    pnl3.add(rbtnSQL);
		add(pnl3);
		
		rbtnSQL.setSelected(true);
		rbtnWindows.setEnabled(false);
		rbtnSQL.setEnabled(false);
		
		JPanel pnl4=new JPanel(new FlowLayout(FlowLayout.LEFT));
		pnl4.add(lblUserName);
		pnl4.add(txtUserName);
		add(pnl4);
		txtUserName.setText("sa");
		
		JPanel pnl5=new JPanel(new FlowLayout(FlowLayout.LEFT));
		pnl5.add(lblPassword);
		pnl5.add(txtPassword);
		txtPassword.setEchoChar('*');
		add(pnl5);
		txtPassword.setText("student");
		
		JPanel pnl6=new JPanel(new FlowLayout(FlowLayout.RIGHT));
		btnCancel.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				connect=false;
			}
			
		});
		pnl6.add(btnCancel);
		
		btnOk.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				connect=true;
				setVisible(false);
				
			}
			
		});
		pnl6.add(btnOk);
		
		add(pnl6);
		
		setModal(true);
		
		setSize(400,300);
		setLocationRelativeTo(null);
		setTitle("Connect to Server");
		setVisible(true);
	}

	public JLabel getLblServerName() {
		return lblServerName;
	}

	public void setLblServerName(JLabel lblServerName) {
		this.lblServerName = lblServerName;
	}

	public JLabel getLblAuth() {
		return lblAuth;
	}

	public void setLblAuth(JLabel lblAuth) {
		this.lblAuth = lblAuth;
	}

	public JLabel getLblUserName() {
		return lblUserName;
	}

	public void setLblUserName(JLabel lblUserName) {
		this.lblUserName = lblUserName;
	}

	public JLabel getLblPassword() {
		return lblPassword;
	}

	public void setLblPassword(JLabel lblPassword) {
		this.lblPassword = lblPassword;
	}

	public JTextField getTxtServerName() {
		return txtServerName;
	}

	public void setTxtServerName(JTextField txtServerName) {
		this.txtServerName = txtServerName;
	}

	public JTextField getTxtUserName() {
		return txtUserName;
	}

	public void setTxtUserName(JTextField txtUserName) {
		this.txtUserName = txtUserName;
	}

	public JPasswordField getTxtPassword() {
		return txtPassword;
	}

	public void setTxtPassword(JPasswordField txtPassword) {
		this.txtPassword = txtPassword;
	}

	public JTextField getTxtDatabaseName() {
		return txtDatabaseName;
	}

	public void setTxtDatabaseName(JTextField txtDatabaseName) {
		this.txtDatabaseName = txtDatabaseName;
	}

}
