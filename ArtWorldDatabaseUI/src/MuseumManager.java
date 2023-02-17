import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.awt.Dimension;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

public class MuseumManager {
	private ArtWorldConnect awc;
	private UIFrame frame;
	
	public MuseumManager(ArtWorldConnect awc, UIFrame frame) {
		this.awc = awc;
		this.frame = frame;
	}

	public void basicSearch(String museumName, String museumCity, String museumState, String artworkName, String artistName, String exhibitName) {
		if (museumName.length() == 0) {
			museumName = null;
		}
		if (museumCity.length() == 0) {
			museumCity = null;
		}
		if (museumState.length() == 0) {
			museumState = null;
		}
		if (artworkName.length() == 0) {
			artworkName = null;
		}
		if (artistName.length() == 0) {
			artistName = null;
		}
		if (exhibitName.length() == 0) {
			exhibitName = null;
		}
		try {
			CallableStatement cs = awc.getConnection().prepareCall("{call BasicSearchMuseum(?,?,?,?,?,?)}");
			cs.setString(1, museumName);
			cs.setString(2, museumCity);
			cs.setString(3, museumState);
			cs.setString(4, artworkName);
			cs.setString(5, artistName);
			cs.setString(6, exhibitName);
			ResultSet rs = cs.executeQuery();
			this.parseBasicSearch(rs);
		} catch (SQLException e) {
			frame.createErrorMessage(e.getMessage());
		}
	}

	private void parseBasicSearch(ResultSet rs) {
		ArrayList<String[]> museums = null;
		String[] temp = new String[3];
		try {
			museums = new ArrayList<String[]>();
			while (rs.next()) {
				for (int i = 0; i < 3; i++) {
					temp[i] = rs.getString(i + 1);
				}
				museums.add(temp);
				temp = new String[3];
			}
		} catch (SQLException e) {
			frame.createErrorMessage(e.getMessage());
		}
		Object[][] data = new Object[museums.size()][3];
		for (int i = 0; i < museums.size(); i++) {
			for (int j = 0; j < 3; j++) {
				data[i][j] = museums.get(i)[j];
				if (data[i][j] == null) {
					data[i][j] = "N/A";
				}
			}
		}
		String[] columnNames = {"Museum Name", "City", "State"};
		JTable table = new JTable(data, columnNames);
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setPreferredSize(new Dimension(UIFrame.frameWidth - 25, UIFrame.resultPanelHeight));
		JPanel rp = frame.getResultPanel();
		rp.removeAll();
		rp.add(scrollPane);	
		}

	public void createMuseum(String museumName, String cityName, String stateName, String username) {
		if (museumName.length() == 0) {
			frame.createErrorMessage("Museum Name is required.");
			return;
		}
		if (cityName.length() == 0) {
			frame.createErrorMessage("City Name is required.");
			return;
		}
		if (stateName.length() == 0) {
			frame.createErrorMessage("State Abbreviation is required.");
			return;
		}
		try {
			CallableStatement cs = awc.getConnection().prepareCall("{? = call insert_museum(?,?,?,?)}");
			cs.registerOutParameter(1, Types.INTEGER);
			cs.setString(2, museumName);
			cs.setString(3, cityName);
			cs.setString(4, stateName);
			cs.setString(5, username);
			cs.execute();
			int returnValue = cs.getInt(1);
			if (returnValue == 0) {
				String query = "Select M.ID From Museum M Join City C On M.CityID = C.ID Where M.Name = ? And C.Name = ? And C.State = ?";
				PreparedStatement stmt = awc.getConnection().prepareStatement(query);
				stmt.setString(1, museumName);
				stmt.setString(2, cityName);
				stmt.setString(3, stateName);
				ResultSet rs = stmt.executeQuery();
				int iD = -1;
				while (rs.next()) {
					iD = rs.getInt(1);
				}
				frame.createSuccessMessage("Created Museum Named " + museumName + " with ID " + iD);
			}
		} catch (SQLException e) {
			frame.createErrorMessage(e.getMessage());
		}
	}

	public void staffSearch(Object museumID) {
		try {
			CallableStatement cs = awc.getConnection().prepareCall("{call StaffSearchMuseum(?)}");
			cs.setInt(1, (int) museumID);
			ResultSet rs = cs.executeQuery();
			this.parseStaffSearch(rs);
		} catch (SQLException e) {
			frame.createErrorMessage(e.getMessage());
		}
	}

	private void parseStaffSearch(ResultSet rs) {
		ArrayList<String[]> museums = null;
		String[] temp = new String[4];
		try {
			museums = new ArrayList<String[]>();
			while (rs.next()) {
				for (int i = 0; i < 4; i++) {
					if (i == 0) {
						temp[i] = Integer.toString(rs.getInt(i + 1));
					} else {
						temp[i] = rs.getString(i + 1);
					}
				}
				museums.add(temp);
				temp = new String[4];
			}
		} catch (SQLException e) {
			frame.createErrorMessage(e.getMessage());
		}
		Object[][] data = new Object[museums.size()][4];
		for (int i = 0; i < museums.size(); i++) {
			for (int j = 0; j < 4; j++) {
				data[i][j] = museums.get(i)[j];
				if (data[i][j] == null) {
					data[i][j] = "N/A";
				}
			}
		}
		String[] columnNames = {"Museum ID", "Museum Name", "City", "State"};
		JTable table = new JTable(data, columnNames);
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setPreferredSize(new Dimension(UIFrame.frameWidth - 25, UIFrame.resultPanelHeight));
		JPanel rp = frame.getResultPanel();
		rp.removeAll();
		rp.add(scrollPane);	
	}

	public void updateMuseum(String name, String cityName, String state, Object museumID, String username) {
		if (name.length() == 0) {
			frame.createErrorMessage("Museum Name is required.");
			return;
		}
		if (cityName.length() == 0) {
			frame.createErrorMessage("City Name is required.");
			return;
		}
		if (state.length() == 0) {
			frame.createErrorMessage("State abbreviation is required.");
			return;
		}
		try {
			CallableStatement cs = awc.getConnection().prepareCall("? = call update_museum(?,?,?,?)");
			cs.registerOutParameter(1, Types.INTEGER);
			cs.setInt(2, (int) museumID);
			cs.setString(3, name);
			cs.setString(4, cityName);
			cs.setString(5, state);
			cs.setString(6, username);
			cs.execute();
			int returnValue = cs.getInt(1);
			if (returnValue == 0) {
				frame.createSuccessMessage("Museum with ID " + museumID + " and Name " + name + " was successfully updated.");
			}
		} catch (SQLException e) {
			frame.createErrorMessage(e.getMessage());
		}
	}

	public void deleteMuseum(String name, Object museumID, String username) {
		if (name.length() == 0) {
			frame.createErrorMessage("Museum Name is required.");
			return;
		}
		try {
			CallableStatement cs = awc.getConnection().prepareCall("{? = call delete_museum(?,?,?)}");
			cs.registerOutParameter(1, Types.INTEGER);
			cs.setInt(2, (int) museumID);
			cs.setString(3, name);
			cs.setString(4, username);
			cs.execute();
			int returnValue = cs.getInt(1);
			if (returnValue == 0) {
				frame.createSuccessMessage("Museum with the name " + name + " was successfully deleted.");
			}
		} catch (SQLException e) {
			frame.createErrorMessage(e.getMessage());
		}
	}

	public void fillDefaults(Object museumID, String username) {
		try {
			CallableStatement cs = awc.getConnection().prepareCall("{call getMuseumValues(?,?)}");
			cs.setInt(1, (int) museumID);
			cs.setString(2, username);
			ResultSet rs = cs.executeQuery();
			this.fillDefaultsHelper(rs);
		} catch (SQLException e) {
			frame.createErrorMessage(e.getMessage());
		}
	}

	private void fillDefaultsHelper(ResultSet rs) {
		JTextField[] fields = frame.getTextFields();
		try {
			rs.next();
			fields[0].setText(rs.getString(1).trim());
			fields[1].setText(rs.getString(2).trim());
			fields[2].setText(rs.getString(3).trim());
		} catch (SQLException e) {
			frame.createErrorMessage(e.getMessage());
		}
	}

	public Object[][] getMuseumInformation(String username, boolean guest) {
		if (guest) {
			Object[][] temp = new Object[3][1];
			temp[0][0] = "filler";
			temp[1][0] = 0;
			temp[2][0] = 1;
			return temp;
		}
		try {
			CallableStatement cs = awc.getConnection().prepareCall("{call get_museum_information(?)}");
			cs.setString(1, username);
			ResultSet rs = cs.executeQuery();
			return this.parsegetMuseumInformation(rs);
		} catch (SQLException e) {
			frame.createErrorMessage(e.getMessage());
		}
		return null;
	}
	
	private Object[][] parsegetMuseumInformation(ResultSet rs) {
		ArrayList<String> museumNames = null;
		ArrayList<Integer> museumIDs = null;
		ArrayList<Integer> museumPermissions = null;
		try {
			museumNames = new ArrayList<String>();
			museumIDs = new ArrayList<Integer>();
			museumPermissions = new ArrayList<Integer>();
			while (rs.next()) {
				museumNames.add(rs.getString(1));
				museumIDs.add(rs.getInt(2));
				museumPermissions.add(rs.getInt(3));
			}
		} catch (SQLException e) {
			frame.createErrorMessage(e.getMessage());
		}
		Object[][] temp = new Object[3][museumNames.size()];
		temp[0] = museumNames.toArray();
		temp[1] = museumIDs.toArray();
		temp[2] = museumPermissions.toArray();
		return temp;
	}	
}
