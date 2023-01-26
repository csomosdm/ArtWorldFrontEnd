import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ArtworkByFiltersService {
	private ArtWorldConnect awc;
	
	public ArtworkByFiltersService(ArtWorldConnect awc) {
		this.awc = awc;
	}
	
	public Object[][] FindArtworkByFilters(String museum, String exhibit, String artist, String artwork) {
		try {
			CallableStatement cs = awc.getConnection().prepareCall("{call SearchArtworkByFilters(?,?,?,?)}");
			cs.setString(1, museum);
			cs.setString(2, exhibit);
			cs.setString(3, artist);
			cs.setString(4, artwork);
			ResultSet rs = cs.executeQuery();
			return this.parseResults(rs);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return null;
	}

	private Object[][] parseResults(ResultSet rs) {
		Object[][] data = new Object[25][3];
		int i = 0;
		int j = 0;
		String artistName;
		try {
			while (rs.next() && i < 25) {
				j = 0;
				data[i][j++] = rs.getString("ArtworkName").trim();
				data[i][j++] = rs.getString("MuseumName");
				if ((artistName = rs.getString("ArtistName")).length() == 0) {
					data[i][j++] = "Unknown";
				} else {
					data[i][j++] = artistName;
				}
				i++;
			}
			return data;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
