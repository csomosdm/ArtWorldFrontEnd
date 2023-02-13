import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

public class ArtistManager {
	private ArtWorldConnect awc;
	private UIFrame frame;

	public ArtistManager(ArtWorldConnect awc, UIFrame frame) {
		this.awc = awc;
		this.frame = frame;
	}

	public void basicSearch(String artistName, String artworkName, String museumName) {
		if (artistName.length() == 0) {
			artistName = null;
		}
		if (artworkName.length() == 0) {
			artworkName = null;
		}
		if (museumName.length() == 0) {
			museumName = null;
		}
		try {
			CallableStatement cs = awc.getConnection().prepareCall("{call BasicSearchArtist(?,?,?)}");
			cs.setString(1, artistName);
			cs.setString(2, artworkName);
			cs.setString(3, museumName);
			ResultSet rs = cs.executeQuery();
			this.parseBasicSearch(rs);
		} catch (SQLException e) {
			frame.createErrorMessage(e.getMessage());
		}
	}

	private void parseBasicSearch(ResultSet rs) {
		ArrayList<String[]> artists = null;
		String[] temp = new String[3];
		try {
			artists = new ArrayList<String[]>();
			while(rs.next()) {
				temp[0] = rs.getString("Name");
				temp[1] = Integer.toString(rs.getInt("BirthDate"));
				if (temp[1].equals("0")) {
					temp[1] = "N/A";
				}
				temp[2] = Integer.toString(rs.getInt("DeathDate"));
				if (temp[2].equals("0")) {
					temp[2] = "N/A";
				}
				artists.add(temp);
				temp = new String[3];
			}
		} catch (SQLException e) {
			frame.createErrorMessage(e.getMessage());
		}
		Object[][]artistsData = new Object[artists.size()][3];
		for (int i = 0; i < artists.size(); i++) {
			artistsData[i][0] = artists.get(i)[0];
			artistsData[i][1] = artists.get(i)[1];
			artistsData[i][2] = artists.get(i)[2];
		}
		String[] columnNames = {"Artist Name", "Artist Birth Date", "Artist Death Date"};
		JTable table = new JTable(artistsData, columnNames);
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setPreferredSize(new Dimension(UIFrame.frameWidth - 25, UIFrame.resultPanelHeight));
		JPanel rp = frame.getResultPanel();
		rp.removeAll();
		rp.add(scrollPane);
	}

	public void createArtist(String artistName, String artistBirthDate, String artistDeathDate) {
		if (artistName.length() == 0) {
			frame.createErrorMessage("Artist Name is required");
			return;
		}
		try {
			if (artistBirthDate.length() == 0) {
				artistBirthDate = null;
			} else {
				Integer.parseInt(artistBirthDate);
			}
			if (artistDeathDate.length() == 0) {
				artistDeathDate = null;
			} else {
				Integer.parseInt(artistDeathDate);
			}
		} catch (NumberFormatException e) {
			frame.createErrorMessage("Artist Birth Date and Death Date must be integers or left empty if unknown.");
			return;
		}
		try {
			CallableStatement cs = awc.getConnection().prepareCall("{? = call insert_artist(?,?,?)}");
			cs.registerOutParameter(1, Types.INTEGER);
			cs.setString(2, artistName);
			cs.setString(3, artistBirthDate);
			cs.setString(4, artistDeathDate);
			cs.execute();
			int returnValue = cs.getInt(1);
			if (returnValue == 0) {
				String query = "Select P.ID From Person P Join Artist A On P.ID = A.ID Where P.Name = ?";
				if (artistBirthDate != null) {
					query += " And A.BirthDate = ?";
				}
				if (artistDeathDate != null) {
					query += " And A.DeathDate = ?";
				}
				PreparedStatement stmt = awc.getConnection().prepareStatement(query);
				stmt.setString(1, artistName);
				int count = 2;
				if (artistBirthDate != null) {
					stmt.setString(count, artistBirthDate);
					count++;
				}
				if (artistDeathDate != null) {
					stmt.setString(count, artistDeathDate);
				}
				ResultSet rs = stmt.executeQuery();
				int iD = -1;
				while (rs.next()) {
					iD = rs.getInt(1);
				}
				frame.createSuccessMessage("Created Artist Named " + artistName + " with ID " + iD);
			}
		} catch (SQLException e) {
			frame.createErrorMessage(e.getMessage());
		}
	}

	public void staffSearch(String artistName, String artworkName, String artistID) {
		if (artistName.length() == 0) {
			artistName = null;
		}
		if (artworkName.length() == 0) {
			artworkName = null;
		}
		if (artistID.length() == 0) {
			artistID = null;
		}
		try {
			CallableStatement cs = awc.getConnection().prepareCall("{call StaffSearchArtist(?,?,?)}");
			cs.setString(1, artistName);
			cs.setString(2, artworkName);
			if (artistID == null) {
				cs.setNull(3, Types.INTEGER);
			} else {
				cs.setInt(3, Integer.parseInt(artistID));
			}
			ResultSet rs = cs.executeQuery();
			this.parseStaffSearch(rs);
		} catch (SQLException e) {
			frame.createErrorMessage(e.getMessage());
		}
	}

	private void parseStaffSearch(ResultSet rs) {
		ArrayList<String[]> artists = null;
		String[] temp = new String[4];
		try {
			artists = new ArrayList<String[]>();
			while(rs.next()) {
				for(int i = 0; i < 4; i++) {
					if (i == 1) {
						temp[i] = rs.getString(i + 1);
					} else {
						temp[i] = String.valueOf(rs.getInt(i + 1));
					} 
				}
				artists.add(temp);
				temp = new String[4];
			}
		} catch (SQLException e) {
			frame.createErrorMessage(e.getMessage());
		}
		Object[][]data = new Object[artists.size()][4];
		for (int i = 0; i < artists.size(); i++) {
			for(int j = 0; j < 4; j++) {
				data[i][j] = artists.get(i)[j];
				if (data[i][j] == null) {
					data[i][j] = "N/A";
				} else if (data[i][j].equals("0")) {
					data[i][j] = "N/A";
				}
			}
		}
		String[] columnNames = {"Artist ID", "Artist Name", "Artist Birth Date", "Artist Death Date"};
		JTable table = new JTable(data, columnNames);
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setPreferredSize(new Dimension(UIFrame.frameWidth - 25, UIFrame.resultPanelHeight));
		JPanel rp = frame.getResultPanel();
		rp.removeAll();
		rp.add(scrollPane);
	}

	public void updateArtist(String artistID, String name, String birthDate, String deathDate, String username) {
		try {
			if (artistID.length() == 0) {
				frame.createErrorMessage("ArtistID is required.");
				return;
			} else {
				Integer.parseInt(artistID);
			}
		} catch (NumberFormatException e) {
			frame.createErrorMessage("ArtistID must be a postive integer corresponding to an existing artist.");
			return;
		}
		try {
			if (name.length() == 0) {
				frame.createErrorMessage("ArtistName is required.");
				return;
			}
			if (birthDate.length() == 0) {
				birthDate = null;
			}
			if (deathDate.length() == 0) {
				deathDate = null;
			}
		} catch (NumberFormatException e) {
			frame.createErrorMessage("Artist Birth Date and Death Date must be integers or left empty if unknown.");
			return;
		}
		try {
			CallableStatement cs = awc.getConnection().prepareCall("{? = call update_artist(?,?,?,?,?)}");
			cs.registerOutParameter(1, Types.INTEGER);
			cs.setInt(2, Integer.parseInt(artistID));
			cs.setString(3, name);
			if (birthDate == null) {
				cs.setNull(4, Types.INTEGER);
			} else {
				cs.setInt(4, Integer.parseInt(birthDate));
			}
			if (deathDate == null) {
				cs.setNull(5, Types.INTEGER);
			} else {
				cs.setInt(5, Integer.parseInt(deathDate));
			}
			cs.setString(6, username);
			cs.execute();
			int returnValue = cs.getInt(1);
			if (returnValue == 0) {
				frame.createSuccessMessage("Artist with ID " + artistID + "  and name " + name + " was successfully updated.");
			}
		} catch (SQLException e) {
			frame.createErrorMessage(e.getMessage());
		}
	}

	public void deleteArtist(String artistID, String name, String username) {
		if (artistID.length() == 0 || name.length() == 0) {
			frame.createErrorMessage("ArtistID and Artist Name are required.");
			return;
		}
		try {
			Integer.parseInt(artistID);
		} catch (NumberFormatException e) {
			frame.createErrorMessage("artistID must be a positive integer.");
			return;
		}
		try {
			CallableStatement cs = awc.getConnection().prepareCall("{? = call delete_artist(?,?,?)}");
			cs.registerOutParameter(1, Types.INTEGER);
			cs.setInt(2, Integer.parseInt(artistID));
			cs.setString(3, name);
			cs.setString(4, username);
			cs.execute();
			int returnValue = cs.getInt(1);
			if (returnValue == 0) {
				frame.createSuccessMessage("Artist with the name " + name + " was successfully deleted.");
			}
		} catch (SQLException e) {
			frame.createErrorMessage(e.getMessage());
		}
	}

	public void fillDefaults(String artistID, String username) {
		if (artistID.length() == 0) {
			frame.createErrorMessage("Artist ID is required.");
			return;
		} else {
			try {
				Integer.parseInt(artistID);
			} catch (NumberFormatException e) {
				frame.createErrorMessage("Artist ID must be a possitive integer associated with an existing artist.");
				return;
			}
		}
		try {
			CallableStatement cs = awc.getConnection().prepareCall("{call getArtistValues(?,?)}");
			cs.setInt(1, Integer.parseInt(artistID));
			cs.setString(2, username);
			ResultSet rs = cs.executeQuery();
			this.fillDefaultsHelper(rs);
		} catch (SQLException e) {
			frame.createErrorMessage(e.getMessage());
			return;
		}
	}

	private void fillDefaultsHelper(ResultSet rs) {
		JTextField[] fields = frame.getTextFields();
		try {
			rs.next();
			fields[1].setText(rs.getString(1).trim());
			if (rs.getInt(2) == 0) {
				fields[2].setText("");
			} else {
				fields[2].setText(Integer.toString(rs.getInt(2)));
			}
			if (rs.getInt(3) == 0) {
				fields[3].setText("");
			} else {
				fields[3].setText(Integer.toString(rs.getInt(3)));
			}
		} catch (SQLException e) {
			frame.createErrorMessage(e.getMessage());
		}
	}
}
