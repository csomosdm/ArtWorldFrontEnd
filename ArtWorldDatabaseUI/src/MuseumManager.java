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

public class MuseumManager {
	private ArtWorldConnect awc;
	private UIFrame frame;
	
	public MuseumManager(ArtWorldConnect awc, UIFrame frame) {
		this.awc = awc;
		this.frame = frame;
	}

	public Object[] getMuseums() {
		try {
			CallableStatement cs = awc.getConnection().prepareCall("{call FindMuseumNames}");
			ResultSet rs = cs.executeQuery();
			return this.parseGetMuseums(rs);
		} catch (SQLException e) {
			frame.createErrorMessage(e.getMessage());
		}
		return null;
	}
	
	public Object[] getMuseumIDs() {
		try {
			CallableStatement cs = awc.getConnection().prepareCall("{call FindMuseumIDs}");
			ResultSet rs = cs.executeQuery();
			return this.parseGetMuseumIDs(rs);
		} catch (SQLException e) {
			frame.createErrorMessage(e.getMessage());
		}
		return null;
	}
private Object[] parseGetMuseumIDs(ResultSet rs) {
		ArrayList<Integer> museumIDs = null;
		try {
			museumIDs = new ArrayList<Integer>();
			int index = 0;
			while(rs.next() && index < 25) {
				museumIDs.add(rs.getInt(1));
				index++;
			}
		} catch (SQLException e) {
			frame.createErrorMessage(e.getMessage());
		}
		return museumIDs.toArray();
	}


	private Object[] parseGetMuseums(ResultSet rs) {
		ArrayList<String> museums = null;
		try {
			museums = new ArrayList<String>();
			int index = 0;
			while(rs.next() && index < 25) {
				museums.add(rs.getString("Name"));
				index++;
			}
		} catch (SQLException e) {
			frame.createErrorMessage(e.getMessage());
		}
		return museums.toArray();
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

	public void createMuseum(String museumName, String cityName, String stateName) {
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
			CallableStatement cs = awc.getConnection().prepareCall("{? = call insert_museum(?,?,?)}");
			cs.registerOutParameter(1, Types.INTEGER);
			cs.setString(2, museumName);
			cs.setString(3, cityName);
			cs.setString(4, stateName);
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
				JPanel panel = frame.getResultPanel();
				panel.removeAll();
				JLabel temp = new JLabel("Created Museum Named " + museumName + " with ID " + iD);
				temp.setFont(UIFrame.ERRORFONT);
				panel.add(temp);
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

	public void updateMuseum(String name, String cityName, String state, Object museumID) {
		if (name.length() == 0) {
			name = null;
		}
		if (cityName.length() == 0) {
			cityName = null;
		}
		if (state.length() == 0) {
			state = null;
		}
		System.out.println("Name: " + name + ", cityname: " + cityName + ", State: " + state + ", museumID: " + museumID);
		try {
			CallableStatement cs = awc.getConnection().prepareCall("");
		} catch (SQLException e) {
			frame.createErrorMessage(e.getMessage());
		}
	}

	public void deleteMuseum(String name, Object museumID) {
		if (name.length() == 0) {
			frame.createErrorMessage("Museum Name is required.");
			return;
		}
		System.out.println("Name: " + name + ", museumID: " + museumID);
		try {
			CallableStatement cs = awc.getConnection().prepareCall("");
		} catch (SQLException e) {
			frame.createErrorMessage(e.getMessage());
		}
	}

}
