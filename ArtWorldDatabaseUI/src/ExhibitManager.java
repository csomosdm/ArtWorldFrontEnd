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
				JPanel panel = frame.getResultPanel();
				panel.removeAll();
				JLabel temp = new JLabel("Created Exhibit Named " + exhibitName + " with ID " + iD);
				temp.setFont(UIFrame.ERRORFONT);
				panel.add(temp);
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
		}
		try {
			CallableStatement cs = awc.getConnection().prepareCall("{call StaffSearchExhibit(?,?,?,?,?)}");
			cs.setString(1, exhibitName);
			cs.setString(2, artistName);
			cs.setString(3, artworkName);
			if (exhibitID == null) {
				cs.setNull(4, Types.INTEGER);
			} else {
				cs.setInt(5, Integer.parseInt(exhibitID));
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
						temp[i] = rs.getDate(i + 1).toString();
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

	public void updateExhibit(String exhibitID, String name, String startDate, String endDate, Object museumID) {
		if (exhibitID.length() == 0) {
			frame.createErrorMessage("ExhibitID can not be left empty");
			return;
		}
		if (name.length() == 0) {
			name = null;
		}
		if (startDate.length() == 0) {
			startDate = null;
		} if (endDate.length() == 0) {
			endDate = null;
		}
		System.out.println("exhibitID: " + exhibitID + ", Name: " + name + ", startDate: " + startDate + ", endDate: " + endDate + "museumID: " + museumID);
		try {
			CallableStatement cs = awc.getConnection().prepareCall("");
		} catch (SQLException e) {
			frame.createErrorMessage(e.getMessage());
		}
	}

	public void deleteExhibit(String exhibitID, String name, Object museumID) {
		if (exhibitID.length() == 0 || name.length() == 0) {
			frame.createErrorMessage("ExhibitID and Exhibit Name are required");
			return;
		}
		System.out.println("exhibitID: " + exhibitID + ", Name: " + name + ", museumID: " + museumID);
		try {
			CallableStatement cs = awc.getConnection().prepareCall("");
		} catch (SQLException e) {
			frame.createErrorMessage(e.getMessage());
		}
	}

}
