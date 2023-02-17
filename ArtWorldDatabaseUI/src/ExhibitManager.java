import java.sql.CallableStatement;
import java.sql.Date;
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

public class ExhibitManager {
	private ArtWorldConnect awc;
	private UIFrame frame;

	public ExhibitManager(ArtWorldConnect awc, UIFrame frame) {
		this.awc = awc;
		this.frame = frame;
	}

	public void basicSearch(String exhibitName, String museumName, String exhibitCity, String exhibitState, String artistName) {
		if (exhibitName.length() == 0) {
			exhibitName = null;
		}
		if (museumName.length() == 0) {
			museumName = null;
		}
		if (exhibitCity.length() == 0) {
			exhibitCity = null;
		}
		if (exhibitState.length() == 0) {
			exhibitState = null;
		}
		if (artistName.length() == 0) {
			artistName = null;
		}
		try {
			CallableStatement cs = awc.getConnection().prepareCall("{call BasicSearchExhibit(?,?,?,?,?)}");
			cs.setString(1, exhibitName);
			cs.setString(2, museumName);
			cs.setString(3, exhibitCity);
			cs.setString(4, exhibitState);
			cs.setString(5, artistName);
			ResultSet rs = cs.executeQuery();
			this.parseBasicSearch(rs);
		} catch (SQLException e) {
			frame.createErrorMessage(e.getMessage());
		}
	}

	private void parseBasicSearch(ResultSet rs) {
		ArrayList<String[]> exhibits = null;
		String[] temp = new String[6];
		try {
			exhibits = new ArrayList<String[]>();
			while (rs.next()) {
				for(int i = 0; i < 6; i++) {
					if (i == 1 || i == 2) {
						Date tempDate = rs.getDate(i + 1);
						if (tempDate == null) {
							temp[i] = "Ongoing";
						} else {
							temp[i] = tempDate.toString();
						}
					} else {
						temp[i] = rs.getString(i + 1);
					}
					if (temp[i] == null) {
						temp[i] = "N/A";
					}
				}
				exhibits.add(temp);
				temp = new String[6];
			}
		} catch (SQLException e) {
			frame.createErrorMessage(e.getMessage());
		}
		Object[][] data = new Object[exhibits.size()][6];
		for (int i = 0; i < exhibits.size(); i++) {
			for (int j = 0; j < 6; j++) {
				data[i][j] = exhibits.get(i)[j];
			}
		}
		String[] columnNames = {"Exhibit Name", "Exhibit Start Date", "Exhibit End Date", "Museum Name", "City", "State"};
		JTable table = new JTable(data, columnNames);
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setPreferredSize(new Dimension(UIFrame.frameWidth - 25, UIFrame.resultPanelHeight));
		JPanel rp = frame.getResultPanel();
		rp.removeAll();
		rp.add(scrollPane);
	}

	public void createExhibit(String exhibitName, String exhibitStartDate, String exhibitEndDate, Object museumID) {
		if (exhibitName.length() == 0) {
			frame.createErrorMessage("Exhibit Name is required.");
			return;
		}
		if (exhibitStartDate.length() == 0) {
			frame.createErrorMessage("Exhibit Start Date is required.");
			return;
		} else {
			try {
				Date.valueOf(exhibitStartDate);
			} catch (IllegalArgumentException e) {
				frame.createErrorMessage("Exhibit Start Date must follow the yyyy-mm-dd format.");
				return;
			}
		}
		if (exhibitEndDate.length() == 0) {
			exhibitEndDate = null;
		} else {
			try {
				Date.valueOf(exhibitEndDate);
			} catch (IllegalArgumentException e) {
				frame.createErrorMessage("Exhibit End Date must follow the yyyy-mm-dd format.");
				return;
			}
		}
		try {
			CallableStatement cs = awc.getConnection().prepareCall("{? = call insert_exhibit(?,?,?,?)}");
			cs.registerOutParameter(1, Types.INTEGER);
			cs.setString(2, exhibitName);
			cs.setDate(3, Date.valueOf(exhibitStartDate));
			if (exhibitEndDate == null) {
				cs.setNull(4, Types.DATE);
			} else {
				cs.setDate(4, Date.valueOf(exhibitEndDate));
			}
			cs.setInt(5, (int) museumID);
			cs.execute();
			int returnValue = cs.getInt(1);
			if (returnValue == 0) {
				String query = "Select ID From Exhibit Where Name = ? And StartDate = ? And HeldAtMuseumID = ?";
				if (exhibitEndDate != null) {
					query += " And EndDate = ?";
				}
				PreparedStatement stmt = awc.getConnection().prepareStatement(query);
				stmt.setString(1, exhibitName);
				stmt.setDate(2, Date.valueOf(exhibitStartDate));
				stmt.setInt(3, (int) museumID);
				if (exhibitEndDate != null) {
					stmt.setDate(4, Date.valueOf(exhibitEndDate));
				}
				ResultSet rs = stmt.executeQuery();
				int iD = -1;
				while (rs.next()) {
					iD = rs.getInt(1);
				}
				frame.createSuccessMessage("Created Exhibit Named " + exhibitName + " with ID " + iD);
			}
		} catch (SQLException e) {
			frame.createErrorMessage(e.getMessage());
		}
	}

	public void staffSearch(String exhibitName, String artistName, String artworkName, String exhibitID, Object museumID) {
		if (exhibitName.length() == 0) {
			exhibitName = null;
		}
		if (artistName.length() == 0) {
			artistName = null;
		}
		if (artworkName.length() == 0) {
			artworkName = null;
		}
		if (exhibitID.length() == 0) {
			exhibitID = null;
		} else {
			try {
				Integer.parseInt(exhibitID);
			} catch (NumberFormatException e) {
				frame.createErrorMessage("Exhibit ID must be a positive integer which corresponds to an existing exhibit.");
				return;
			}
		}
		System.out.println(exhibitName + ", " + artistName + ", " + artworkName + ", " + exhibitID + ", " + museumID);
		try {
			CallableStatement cs = awc.getConnection().prepareCall("{call StaffSearchExhibit(?,?,?,?,?)}");
			cs.setString(1, exhibitName);
			cs.setString(2, artistName);
			cs.setString(3, artworkName);
			if (exhibitID == null) {
				cs.setNull(4, Types.INTEGER);
			} else {
				cs.setInt(4, Integer.parseInt(exhibitID));
			}
			cs.setInt(5, (int) museumID);
			ResultSet rs = cs.executeQuery();
			this.parseStaffSearch(rs);
		} catch (SQLException e) {
			frame.createErrorMessage(e.getMessage());
		}
	}

	private void parseStaffSearch(ResultSet rs) {
		ArrayList<String[]> exhibits = null;
		String[] temp = new String[4];
		try {
			exhibits = new ArrayList<String[]>();
			while (rs.next()) {
				for(int i = 0; i < 4; i++) {
					if (i >= 2) {
						if (rs.getDate(i + 1) == null) {
							temp[i] = "Ongoing";
						} else {
							temp[i] = rs.getDate(i + 1).toString();
						}
					} else if (i == 0) {
						temp[i] = String.valueOf(rs.getInt(i + 1));
					} else {
						temp[i] = rs.getString(i + 1);
					}
					if (temp[i] == null) {
						temp[i] = "N/A";
					}
				}
				exhibits.add(temp);
				temp = new String[4];
			}
		} catch (SQLException e) {
			frame.createErrorMessage(e.getMessage());
		}
		Object[][] data = new Object[exhibits.size()][4];
		for (int i = 0; i < exhibits.size(); i++) {
			for (int j = 0; j < 4; j++) {
				data[i][j] = exhibits.get(i)[j];
			}
		}
		String[] columnNames = {"Exhibit ID", "Exhibit Name", "Exhibit Start Date", "Exhibit End Date"};
		JTable table = new JTable(data, columnNames);
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setPreferredSize(new Dimension(UIFrame.frameWidth - 25, UIFrame.resultPanelHeight));
		JPanel rp = frame.getResultPanel();
		rp.removeAll();
		rp.add(scrollPane);
	}

	public void updateExhibit(String exhibitID, String name, String startDate, String endDate, String museumID, String username) {
		if (exhibitID.length() == 0) {
			frame.createErrorMessage("Exhibit ID is required.");
			return;
		} else {
			try {
				Integer.parseInt(exhibitID);
			} catch (NumberFormatException e) {
				frame.createErrorMessage("Exhibit ID must be a positive integer which corresponds to an exhisting exhibit.");
				return;
			}
		}
		if (name.length() == 0) {
			frame.createErrorMessage("Exhibit Name is required.");
			return;
		}
		try {
			if (startDate.length() == 0) {
				frame.createErrorMessage("Start Date is required.");
				return;
			} else {
				Date.valueOf(startDate);
			}
			if (endDate.length() == 0) {
				endDate = null;
			} else {
				Date.valueOf(endDate);
			}
		} catch (IllegalArgumentException e){
			frame.createErrorMessage("Exhibit Start and End Date must follow the yyyy-mm-dd format.");
			return;
		}
		try {
			CallableStatement cs = awc.getConnection().prepareCall("{? = call update_exhibit(?,?,?,?,?,?)}");
			cs.registerOutParameter(1, Types.INTEGER);
			cs.setInt(2, Integer.parseInt(exhibitID));
			cs.setString(3, name);
			cs.setDate(4, Date.valueOf(startDate));
			if (endDate == null) {
				cs.setNull(5, Types.DATE);
			} else {
				cs.setDate(5, Date.valueOf(endDate));
			}
			cs.setInt(6, Integer.parseInt(museumID));
			cs.setString(7, username);
			cs.execute();
			int returnValue = cs.getInt(1);
			if (returnValue == 0) {
				frame.createSuccessMessage("Exhibit with ID " + exhibitID + " and Name " + name + " was successfully updated.");
			}
		} catch (SQLException e) {
			frame.createErrorMessage(e.getMessage());
		}
	}

	public void deleteExhibit(String exhibitID, String name, String  username) {
		if (exhibitID.length() == 0 || name.length() == 0) {
			frame.createErrorMessage("ExhibitID and Exhibit Name are required.");
			return;
		}
		try {
			Integer.parseInt(exhibitID);
		} catch (NumberFormatException e) {
			frame.createErrorMessage("exhibitID must be a positive integer.");
			return;
		}
		try {
			CallableStatement cs = awc.getConnection().prepareCall("{? = call delete_exhibit(?,?,?)}");
			cs.registerOutParameter(1, Types.INTEGER);
			cs.setInt(2, Integer.parseInt(exhibitID));
			cs.setString(3, name);
			cs.setString(4, username);
			cs.execute();
			int returnValue = cs.getInt(1);
			if (returnValue == 0) {
				frame.createSuccessMessage("Exhibit with the name " + name + " was successfully deleted.");
			}
		} catch (SQLException e) {
			frame.createErrorMessage(e.getMessage());
		}
	}

	public void fillDefaults(String exhibitID, String username) {
		if (exhibitID.length() == 0) {
			frame.createErrorMessage("Exhibit ID is required.");
			return;
		}
		try {
			Integer.parseInt(exhibitID);
		} catch (NumberFormatException e) {
			frame.createErrorMessage("Exhibit ID must be a positive integer.");
			return;
		}
		try {
			CallableStatement cs = awc.getConnection().prepareCall("{call getExhibitValues(?,?)}");
			cs.setInt(1, Integer.parseInt(exhibitID));
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
			fields[1].setText(rs.getString(1).trim());
			fields[2].setText(rs.getDate(2).toString());
			if (rs.getDate(3) == null) {
				fields[3].setText("");
			} else {
				fields[3].setText(rs.getDate(3).toString());
			}
			fields[4].setText(Integer.toString(rs.getInt(4)));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void addArtworkToExhibit(String exhibitID, String artworkID, String username) {
		if (exhibitID.length() == 0 || artworkID.length() == 0) {
			frame.createErrorMessage("Exhibit ID and Artwork ID are required.");
			return;
		} else {
			try {
				Integer.parseInt(exhibitID);
				Integer.parseInt(artworkID);
			} catch (NumberFormatException e) {
				frame.createErrorMessage("Exhibit ID and Artwork ID must be positive integers which correspond to existing exhibits and artworks.");
				return;
			}
		}
		try {
			CallableStatement cs = awc.getConnection().prepareCall("{? = call insert_showed(?,?,?,?,?)}");
			cs.registerOutParameter(1, Types.INTEGER);
			cs.setInt(2, Integer.parseInt(exhibitID));
			cs.setInt(3, Integer.parseInt(artworkID));
			cs.setString(4, username);
			cs.registerOutParameter(5, Types.VARCHAR);
			cs.registerOutParameter(6, Types.VARCHAR);
			cs.execute();
			int returnValue = cs.getInt(1);
			if (returnValue == 0) {
				frame.createSuccessMessage("Artwork with ID " + artworkID + " and name " + cs.getString(6) + " was successfully added to the exhibit with ID " + exhibitID + " and name " + cs.getString(5));
			}
		} catch (SQLException e) {
			frame.createErrorMessage(e.getMessage());
		}
	}

	public void removeArtworkFromExhibit(String exhibitID, String artworkID, String username) {
		if (exhibitID.length() == 0 || artworkID.length() == 0) {
			frame.createErrorMessage("Exhibit ID and Artwork ID are required.");
			return;
		} else {
			try {
				Integer.parseInt(exhibitID);
				Integer.parseInt(artworkID);
			} catch (NumberFormatException e) {
				frame.createErrorMessage("Exhibit ID and Artwork ID must be positive integers which correspond to existing exhibits and artworks.");
				return;
			}
		}
		try {
			CallableStatement cs = awc.getConnection().prepareCall("{? = call delete_showed(?,?,?,?,?)}");
			cs.registerOutParameter(1, Types.INTEGER);
			cs.setInt(2, Integer.parseInt(exhibitID));
			cs.setInt(3, Integer.parseInt(artworkID));
			cs.setString(4, username);
			cs.registerOutParameter(5, Types.VARCHAR);
			cs.registerOutParameter(6, Types.VARCHAR);
			cs.execute();
			int returnValue = cs.getInt(1);
			if (returnValue == 0) {
				frame.createSuccessMessage("Artwork with ID " + artworkID + " and name " + cs.getString(6) + " was successfully removed from the exhibit with ID " + exhibitID + " and name " + cs.getString(5));
			}
		} catch (SQLException e) {
			frame.createErrorMessage(e.getMessage());
		}
	}

}
