import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLType;
import java.sql.Types;
import java.util.ArrayList;
import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class ArtworkManager {
	private ArtWorldConnect awc;
	private UIFrame frame;

	public ArtworkManager(ArtWorldConnect awc, UIFrame frame) {
		this.awc = awc;
		this.frame = frame;
	}

	public void basicSearch(String artworkTitle, String artworkMedium, String artworkCategory, String artistName, String museumName, String exhibitName) {
		if (artworkTitle.length() == 0) {
			artworkTitle = null;
		}
		if (artworkMedium.length() == 0) {
			artworkMedium = null;
		}
		if (artworkCategory.length() == 0) {
			artworkCategory = null;
		}
		if (artistName.length() == 0) {
			artistName = null;
		}
		if (museumName.length() == 0) {
			museumName = null;
		}
		if (exhibitName.length() == 0) {
			exhibitName = null;
		}
		try {
			CallableStatement cs = awc.getConnection().prepareCall("{call BasicSearchArtwork(?,?,?,?,?,?)}");
			cs.setString(1, artworkTitle);
			cs.setString(2, artworkMedium);
			cs.setString(3, artworkCategory);
			cs.setString(4, artistName);
			cs.setString(5, museumName);
			cs.setString(6, exhibitName);
			ResultSet rs = cs.executeQuery();
			this.parseBasicSearch(rs);
		} catch (SQLException e) {
			frame.createErrorMessage(e.getMessage());
		}
	}

	private void parseBasicSearch(ResultSet rs) {
		ArrayList<String[]> artworks = null;
		String[] temp = new String[7];
		try {
			artworks = new ArrayList<String[]>();
			while(rs.next()) {
				temp[0] = rs.getString("ArtworkName");
				temp[1] = rs.getString("Medium");
				temp[2] = rs.getString("Category");
				temp[3] = rs.getString("ArtistName");
				temp[4] = rs.getString("MuseumName");
				temp[5] = rs.getString("ActiveExhibitName");
				temp[6] = rs.getString("ExhibitMuseumName");
				artworks.add(temp);
				temp = new String[7];
			}
		} catch (SQLException e) {
			frame.createErrorMessage(e.getMessage());
		}
		Object[][] data = new Object[artworks.size()][7];
		for (int i = 0; i < artworks.size(); i++) {
			for(int j = 0; j < 7; j++) {
				data[i][j] = artworks.get(i)[j];
				if (data[i][j] == null) {
					data[i][j] = "N/A";
				}
			}
		}
		String[] columnNames = {"Artwork Title", "Medium", "Category", "Artist", "Museum", "Active Exhibit", "Exhibit Museum"};
		JTable table = new JTable(data, columnNames);
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setPreferredSize(new Dimension(UIFrame.frameWidth - 25, UIFrame.resultPanelHeight));
		JPanel rp = frame.getResultPanel();
		rp.removeAll();
		rp.add(scrollPane);
	}

	public void createArtwork(String artworkName, String artworkMedium, String artworkCategory, String artistID, Object museumID) {
		if (artworkName.length() == 0) {
			frame.createErrorMessage("Artwork Name is required.");
			return;
		}
		try {
			Integer.parseInt(artistID);
		} catch (NumberFormatException e){
			frame.createErrorMessage("ArtistID must be a positive integer.");
			return;
		}
		if (artworkMedium.length() == 0) {
			artworkMedium = null;
		}
		if (artworkCategory.length() == 0) {
			artworkCategory = null;
		}
		try {
			CallableStatement cs = awc.getConnection().prepareCall("{? = call insert_artwork(?,?,?,?,?)}");
			cs.registerOutParameter(1, Types.INTEGER);
			cs.setString(2, artworkName);
			cs.setString(3, artworkMedium);
			if (artistID.length() == 0) {
				cs.setNull(4, Types.INTEGER);
			} else {
				cs.setInt(4, Integer.parseInt(artistID));
			}
			cs.setInt(5, (int) museumID);
			cs.setString(6, artworkCategory);
			cs.execute();
			int returnValue = cs.getInt(1);
			if (returnValue == 0) {
				String query = "Select ID From Artwork Where Title = ? And DisplayedAtID = ?";
				if (artworkMedium != null) {
					query += " And Medium = ?";
				}
				if (artworkCategory != null) {
					query += " And Category = ?";
				}
				if (artistID.length() != 0) {
					query+= " And ArtistID = ?";
				}
				PreparedStatement stmt = awc.getConnection().prepareStatement(query);
				int count = 3;
				stmt.setString(1, artworkName);
				stmt.setInt(2, (int) museumID);
				if (artworkMedium != null) {
					stmt.setString(count, artworkMedium);
					count++;
				}
				if (artworkCategory != null) {
					stmt.setString(count, artworkCategory);
					count++;
				}
				if (artistID.length() != 0) {
					stmt.setInt(count, Integer.parseInt(artistID));
				}
				ResultSet rs = stmt.executeQuery();
				int iD = -1;
				while (rs.next()) {
					iD = rs.getInt(1);
				}
				JPanel panel = frame.getResultPanel();
				panel.removeAll();
				JLabel temp = new JLabel("Created Artwork Titled " + artworkName + " with ID " + iD);
				temp.setFont(UIFrame.ERRORFONT);
				panel.add(temp);
			}
		} catch (SQLException e) {
			frame.createErrorMessage(e.getMessage());
		}
	}

	public void staffSearch(String artworkName, String artworkMedium, String artworkCategory, String artistName, String exhibitName, String artworkID,
			Object museumID) {
		if (artworkName.length() == 0) {
			artworkName = null;
		}
		if (artworkMedium.length() == 0) {
			artworkMedium = null;
		}
		if (artworkCategory.length() == 0) {
			artworkCategory = null;
		}
		if (artistName.length() == 0) {
			artistName = null;
		}
		if (exhibitName.length() == 0) {
			exhibitName = null;
		}
		if (artworkID.length() == 0) {
				artworkID = null;
		}
		try {
			CallableStatement cs = awc.getConnection().prepareCall("{call StaffSearchArtwork(?,?,?,?,?,?,?)}");
			cs.setString(1, artworkName);
			cs.setString(2, artworkMedium);
			cs.setString(3, artworkCategory);
			cs.setString(4, artistName);
			cs.setString(5, exhibitName);
			if (artworkID == null) {
				cs.setNull(6, Types.INTEGER);
			} else {
				cs.setInt(6, Integer.parseInt(artworkID));
			}
			cs.setInt(7, (int) museumID);
			ResultSet rs = cs.executeQuery();
			this.parseStaffSearch(rs);
		} catch (SQLException e) {
			frame.createErrorMessage(e.getMessage());
		}
	}

	private void parseStaffSearch(ResultSet rs) {
		ArrayList<String[]> artworks = null;
		String[] temp = new String[8];
		try {
			artworks = new ArrayList<String[]>();
			while(rs.next()) {
				for(int i = 0; i < 8; i++) {
					if (i == 0) {
						temp[i] = String.valueOf(rs.getInt(i + 1));
					} else {
						temp[i] = rs.getString(i + 1);
					}
				}
				artworks.add(temp);
				temp = new String[8];
			}
		} catch (SQLException e) {
			frame.createErrorMessage(e.getMessage());
		}
		Object[][] data = new Object[artworks.size()][8];
		for (int i = 0; i < artworks.size(); i++) {
			for(int j = 0; j < 8; j++) {
				data[i][j] = artworks.get(i)[j];
				if (data[i][j] == null) {
					data[i][j] = "N/A";
				}
			}
		}
		String[] columnNames = {"Artwork ID", "Artwork Title", "Medium", "Category", "Artist", "Museum", "Active Exhibit", "Exhibit Museum"};
		JTable table = new JTable(data, columnNames);
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setPreferredSize(new Dimension(UIFrame.frameWidth - 25, UIFrame.resultPanelHeight));
		JPanel rp = frame.getResultPanel();
		rp.removeAll();
		rp.add(scrollPane);		
	}

	public void updateArtwork(String artworkID, String artworkName, String artworkMedium, String artworkCategory, String artistID, Object museumID) {
		if (artworkID.length() == 0) {
			frame.createErrorMessage("Artwork ID can not be left empty");
			return;
		}
		if (artworkName.length() == 0) {
			artworkName = null;
		}
		if (artworkMedium.length() == 0) {
			artworkMedium = null;
		}
		if (artworkCategory.length() == 0) {
			artworkCategory = null;
		}
		if (artistID.length() == 0) {
			artistID = null;
		}
		try {
			CallableStatement cs = awc.getConnection().prepareCall("{call update_artwork(?,?,?,?,?)}");
			cs.setInt(1, Integer.parseInt(artworkID));
			cs.setString(2, artworkName);
			cs.setString(3, artworkMedium);
			if (artistID == null) {
				cs.setNull(4, Types.INTEGER);
			} else {
				cs.setInt(4, Integer.parseInt(artistID));
			}
			cs.setInt(5, (int) museumID);
			cs.execute();
		} catch (SQLException e) {
			frame.createErrorMessage(e.getMessage());
		}
	}

	public void deleteArtwork(String artworkID, String name, Object museumID) {
		if (artworkID.length() == 0 || name.length() == 0) {
			frame.createErrorMessage("ArtworkID and Artwork Name are required");
			return;
		}
		System.out.println("artworkID: " + artworkID + ", Name: " + name + ", museumID: " + museumID);
		try {
			CallableStatement cs = awc.getConnection().prepareCall("");
		} catch (SQLException e) {
			frame.createErrorMessage(e.getMessage());
		}
	}

}