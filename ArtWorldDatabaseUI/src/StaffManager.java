import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLType;
import java.sql.Types;
import java.util.ArrayList;
import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

public class StaffManager {
	private ArtWorldConnect awc;
	private UIFrame frame;
	
	public StaffManager(ArtWorldConnect awc, UIFrame frame) {
		this.awc = awc;
		this.frame = frame;
	}

	public void listStaff(String staffName, boolean filterByRole, int selectedRole, String username, Object museumID, boolean advancedSearch) {
		if (staffName.length() == 0) {
			staffName = null;
		}
		if (filterByRole) {
			if (selectedRole == 0) {
				selectedRole = 2;
			} else if (selectedRole == 1) {
				selectedRole = 3;
			} else {
				selectedRole = 4;
			}
		}
		String query;
		if (advancedSearch) {
			query = "{call advanced_search_staff(?,?,?,?)}";
		} else {
			query = "{call search_staff(?,?,?,?)}";
		}
		try {
			CallableStatement cs = awc.getConnection().prepareCall(query);
			cs.setString(1, staffName);
			if (filterByRole) {
				cs.setInt(2, selectedRole);
			} else {
				cs.setNull(2, Types.INTEGER);
			}
			cs.setInt(3, (int) museumID);
			cs.setString(4, username);
			ResultSet rs = cs.executeQuery();
			if (advancedSearch) {
				System.out.println("advanced parse");
				this.advancedParseStaff(rs);
			} else {
				System.out.println("basic parse");
				this.basicParseStaff(rs);
			}
		} catch (SQLException e) {
			frame.createErrorMessage(e.getMessage());
		}
	}

	private void basicParseStaff(ResultSet rs) {
		ArrayList<String[]> staff = null;
		String[] temp = new String[2];
		try {
			staff = new ArrayList<String[]>();
			while(rs.next()) {
				temp[0] = rs.getString(1);
				int x = rs.getInt(2);
				if (x == 2) {
					temp[1] = "Exhibit Staff";
				} else if (x == 3) {
					temp[1] = "General Staff";
				} else if (x == 4) {
					temp[1] = "Owner";
				} else {
					temp[1] = "N/A";
				}
				staff.add(temp);
				temp = new String[2];
			}
		} catch (SQLException e) {
			frame.createErrorMessage(e.getMessage());
		}
		Object[][] data = new Object[staff.size()][2];
		for (int i = 0; i < staff.size(); i++) {
			data[i][0] = staff.get(i)[0];
			data[i][1] = staff.get(i)[1];
		}
		String[] columnNames = {"Staff Name", "Staff Role"};
		JTable table = new JTable(data, columnNames);
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setPreferredSize(new Dimension(UIFrame.frameWidth - 25, UIFrame.resultPanelHeight));
		JPanel rp = frame.getResultPanel();
		rp.removeAll();
		rp.add(scrollPane);	
	}
	
	private void advancedParseStaff(ResultSet rs) {
		ArrayList<String[]> staff = null;
		String[] temp = new String[3];
		try {
			staff = new ArrayList<String[]>();
			while(rs.next()) {
				temp[0] = rs.getString(1);
				temp[1] = rs.getString(2);
				int x = rs.getInt(3);
				if (x == 2) {
					temp[2] = "Exhibit Staff";
				} else if (x == 3) {
					temp[2] = "General Staff";
				} else if (x == 4) {
					temp[2] = "Owner";
				} else {
					temp[2] = "N/A";
				}
				staff.add(temp);
				temp = new String[3];
			}
		} catch (SQLException e) {
			frame.createErrorMessage(e.getMessage());
		}
		Object[][] data = new Object[staff.size()][3];
		for (int i = 0; i < staff.size(); i++) {
			data[i][0] = staff.get(i)[0];
			data[i][1] = staff.get(i)[1];
			data[i][2] = staff.get(i)[2];
		}
		String[] columnNames = {"Staff Username", "Staff Name", "Staff Role"};
		JTable table = new JTable(data, columnNames);
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setPreferredSize(new Dimension(UIFrame.frameWidth - 25, UIFrame.resultPanelHeight));
		JPanel rp = frame.getResultPanel();
		rp.removeAll();
		rp.add(scrollPane);	
	}

	public void addStaff(String username, String name, String password, int position, boolean newUser, Object museumID) {
		if (username.length() == 0 || name.length() == 0) {
			frame.createErrorMessage("Username and Name are required.");
			return;
		}
		if (newUser && password.length() < 6) {
			frame.createErrorMessage("A password containing at least 6 characters is required for new users.");
			return;
		}
		position += 2;
		System.out.println(username + ", " + name + ", " + password + ", " + newUser + ", " + position + ", " + museumID);
		try {
			CallableStatement cs = awc.getConnection().prepareCall("{? = call insert_staff(?,?,?,?,?,?)}");
			cs.registerOutParameter(1, Types.INTEGER);
			cs.setString(2, username);
			cs.setString(3, name);
			cs.setInt(4, position);
			if (newUser) {
				cs.setInt(5, 1);
				cs.setString(6, password);
			} else {
				cs.setInt(5, 0);
				cs.setNull(6, Types.VARCHAR);
			}
			cs.setInt(7, (int) museumID);
			cs.execute();
			int returnValue = cs.getInt(1);
			if (returnValue == 0) {
				frame.createSuccessMessage("Staff with the username " + username + " and name " + name + " was successfully added.");
			}
		} catch (SQLException e) {
			frame.createErrorMessage(e.getMessage());
		}
	}

	public void updateStaff(String username, String name, String password, int position, Object museumID) {
		if (username.length() == 0 || name.length() == 0) {
			frame.createErrorMessage("Username and Name are required.");
			return;
		}
		if (password.length() < 6) {
			frame.createErrorMessage("A password containing at least 6 characters is required.");
			return;
		}
		position += 2;
		System.out.println(username + ", " + name + ", " + password + ", " + position + ", " + museumID);
		try {
			CallableStatement cs = awc.getConnection().prepareCall("{? = call update_staff(?,?,?,?,?)}");
			cs.registerOutParameter(1, Types.INTEGER);
			cs.setString(2, username);
			cs.setString(3, name);
			cs.setString(4, password);
			cs.setInt(5, position);
			cs.setInt(6, (int) museumID);
			cs.execute();
			if (cs.getInt(1) == 0) {
				frame.createSuccessMessage("Staff with username " + username + " and name " + name + " was successfully updated.");
			}
		} catch (SQLException e) {
			frame.createErrorMessage(e.getMessage());
		}
	}

	public void deleteStaff(String username, String name, Object museumID) {
		if (username.length() == 0 || name.length() == 0) {
			frame.createErrorMessage("Username and Name are required.");
			return;
		}
		try {
			CallableStatement cs = awc.getConnection().prepareCall("{? = call delete_staff(?,?,?)}");
			cs.registerOutParameter(1, Types.INTEGER);
			cs.setString(2, username);
			cs.setString(3, name);
			cs.setInt(4, (int) museumID);
			cs.execute();
			if (cs.getInt(1) == 0) {
				frame.createSuccessMessage("Staff with username " + username + " and name " + name + " was successfully deleted.");
			}
		} catch (SQLException e) {
			frame.createErrorMessage(e.getMessage());
		}
	}

	public void fillDefaults(String username, Object museumID, String username2) {
		if (username.length() == 0) {
			frame.createErrorMessage("Username is required.");
		}
		try {
			CallableStatement cs = awc.getConnection().prepareCall("{call getStaffValues(?,?,?)}");
			cs.setString(1, username);
			cs.setInt(2, (int) museumID);
			cs.setString(3, username2);
			ResultSet rs = cs.executeQuery();
			this.fillDefaultsHelper(rs);
		} catch (SQLException e) {
			frame.createErrorMessage(e.getMessage());
			return;
		}
	}

	private void fillDefaultsHelper(ResultSet rs) {
		JTextField[] fields = frame.getTextFields();
		JComboBox box = frame.getStaffRolesBox();
		try {
			while (rs.next()) {
				fields[1].setText(rs.getString(1));
				System.out.println("Gets name properly");
				fields[2].setText(rs.getString(2));
				System.out.println("Position: " + rs.getInt(3));
				box.setSelectedIndex(rs.getInt(3) - 2);
			}	
		} catch (SQLException e) {
			frame.createErrorMessage(e.getMessage());
			return;
		}
	}
}
