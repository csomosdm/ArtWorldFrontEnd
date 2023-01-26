import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Button;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;

public class UIFrame {
	private static final int frameWidth = 1200;
	private static final int frameHeight = 500;
	private static final Font FONT = new Font("TimesRoman", Font.PLAIN,  14);
	private JFrame frame;
	private JTextField museumName;
	private JTextField exhibitName;
	private JTextField artistName;
	private JTextField artworkName;
	private Button searchButton;
	private ArtworkByFiltersService awbfs;
	private ArtWorldConnect awc;
	private JPanel panel;
	private JScrollPane scrollPane;
	private JTable table;
	
	public UIFrame (ArtWorldConnect awc, ArtworkByFiltersService awbfs) {
		this.awc = awc;
		this.awbfs = awbfs;
		this.frame = new JFrame("ArtWorld Front End");
		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.frame.setSize(UIFrame.frameWidth, UIFrame.frameHeight);
		this.frame.setLocationRelativeTo(null);
		
		this.initializePanel();
		
		searchButton = new Button ("Search");
		searchButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String museumText = museumName.getText();
				if (museumText.length() == 0) {
					museumText = null;
				}
				String exhibitText = exhibitName.getText();
				if (exhibitText.length() == 0) {
					exhibitText = null;
				}
				String artistText = artistName.getText();
				if (artistText.length() == 0) {
					artistText = null;
				}
				String artworkText = artworkName.getText();
				if (artworkText.length() == 0) {
					artworkText = null;
				}
				Object[][] data = awbfs.FindArtworkByFilters(museumText, exhibitText, artistText, artworkText);
				String[] columnNames = {"ArtworkName", "MuseumName", "ArtistName"};
				frame.remove(scrollPane);
				table = new JTable(data, columnNames);
				scrollPane = new JScrollPane(table);
				frame.add(scrollPane, BorderLayout.CENTER);
				table.setFillsViewportHeight(true);
				frame.repaint();
				frame.setVisible(true);
			}
		});
		
		panel.add(searchButton);
		
		Object[][] data = awbfs.FindArtworkByFilters(null, null, null, null);
		String[] columnNames = {"ArtworkName", "MuseumName", "ArtistName"};
		table = new JTable(data, columnNames);
		scrollPane = new JScrollPane(table);
		frame.add(scrollPane, BorderLayout.CENTER);
		table.setFillsViewportHeight(true);
		
		this.frame.add(panel, BorderLayout.NORTH);
		this.frame.setVisible(true);
	}

	/*
	 * Ensures: Creates the JPanel at the top of the screen which houses the search parameters.
	 */
	private void initializePanel() {
		panel = new JPanel();
		panel.setBackground(Color.DARK_GRAY);
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 6));
		panel.setPreferredSize(new Dimension(UIFrame.frameWidth, 80));
		
		JLabel museumLabel = new JLabel();
		museumLabel.setText("Museum Name: ");
		museumLabel.setFont(UIFrame.FONT);
		museumLabel.setForeground(Color.WHITE);
		panel.add(museumLabel);
		
		museumName = new JTextField(16);
		museumName.setFont(UIFrame.FONT);
		museumName.setToolTipText("Enter a museum's name to filter results");
		panel.add(museumName);
				
		JLabel exhibitLabel = new JLabel();
		exhibitLabel.setText("Exhibit Name: ");
		exhibitLabel.setFont(UIFrame.FONT);
		exhibitLabel.setForeground(Color.WHITE);
		panel.add(exhibitLabel);
		
		exhibitName = new JTextField(16);
		exhibitName.setFont(UIFrame.FONT);
		exhibitName.setToolTipText("Enter an exhibit's name to filter results");
		panel.add(exhibitName);
		
		JLabel artistLabel = new JLabel();
		artistLabel.setText("Artist Name: ");
		artistLabel.setFont(UIFrame.FONT);
		artistLabel.setForeground(Color.WHITE);
		panel.add(artistLabel);
		
		artistName = new JTextField(16);
		artistName.setFont(UIFrame.FONT);
		artistName.setToolTipText("Enter an artist's name to filter results");
		panel.add(artistName);
		
		JLabel artworkLabel = new JLabel();
		artworkLabel.setText("Artwork Name: ");
		artworkLabel.setFont(UIFrame.FONT);
		artworkLabel.setForeground(Color.WHITE);
		panel.add(artworkLabel);

		artworkName = new JTextField(16);
		artworkName.setFont(UIFrame.FONT);
		artworkName.setToolTipText("Enter an artwork's name to filter results");
		panel.add(artworkName);		
	}
}
