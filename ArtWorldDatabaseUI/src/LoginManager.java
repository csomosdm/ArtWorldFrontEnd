import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.awt.Color;

public class LoginManager extends JFrame implements ActionListener {
	private ArtWorldConnect awc;
	private JFrame frame;
	private JButton login, continueAsGuest;
	private JTextField usernameText;
	private JPasswordField passwordText;
	private JLabel failedLogIn;
	private JPanel panel;
	private boolean admin;

	public LoginManager(ArtWorldConnect awc) {
		this.awc = awc;
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(350, 150);
		this.setLocationRelativeTo(null);
		this.setTitle("Login In Page");
		usernameText = new JTextField(20);
		usernameText.setFont(UIFrame.FONT);
		passwordText = new JPasswordField(20);
		passwordText.setFont(UIFrame.FONT);
		login = new JButton("Login");
		login.setFont(UIFrame.FONT);
		login.addActionListener(this);
		continueAsGuest = new JButton("Continue As Guest");
		continueAsGuest.setFont(UIFrame.FONT);
		continueAsGuest.addActionListener(this);
		failedLogIn = new JLabel("Log In Attempts Failed." + '\n' + "Please try again.");
		failedLogIn.setFont(UIFrame.FONT);
		failedLogIn.setForeground(Color.red);
		JLabel usernameLabel = new JLabel("Username:  ");
		usernameLabel.setFont(UIFrame.FONT);
		JLabel passwordLabel = new JLabel("Password:  ");
		passwordLabel.setFont(UIFrame.FONT);
		admin = false;
		panel = new JPanel();
		panel.add(usernameLabel);
		panel.add(usernameText);
		panel.add(passwordLabel);
		panel.add(passwordText);
		panel.add(login);
		panel.add(continueAsGuest);
		this.add(panel);
		this.setVisible(true);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == login) {
			if (this.attemptLogin()) {
				this.dispose();
				new UIFrame(awc, usernameText.getText(), admin);
			} else {
				this.setSize(350, 175);
				panel.add(failedLogIn);
				this.setVisible(true);
			}
		} else if (e.getSource() == continueAsGuest) {
			this.dispose();
			new UIFrame(awc, null, false);
		}
	}
	
	private String getPassword() {
		StringBuilder sb = new StringBuilder();
		for (char c : passwordText.getPassword()) {
			sb.append(c);
		}
		return sb.toString();
	}
	
	private boolean attemptLogin() {
		String username = usernameText.getText();
		if (username.length() == 0) {
			return false;
		}
		String password = getPassword();
		if (password.length() == 0) {
			return false;
		}
		try {
			CallableStatement cs = awc.getConnection().prepareCall("{? = call attempt_login(?,?,?)}");
			cs.registerOutParameter(1, Types.INTEGER);
			cs.setString(2, username);
			cs.setString(3, password);
			cs.registerOutParameter(4, Types.BIT);
			cs.execute();
			int returnValue = cs.getInt(1);
			if (returnValue == 0) {
				if (cs.getBoolean(4)) {
					admin = true;
				}
				return true;
			} else {
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
}
