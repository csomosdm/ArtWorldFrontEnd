import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
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
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;

public class UIFrame extends JFrame implements ActionListener {
	public static final int frameWidth = 1400, frameHeight = 675, queryPanelHeight = 80, otherPanelHeight = 120, resultPanelHeight = 450;
	public static final Font FONT = new Font("TimesRoman", Font.PLAIN,  14);
	public static final Font ERRORFONT = new Font("TimesRoman", Font.BOLD, 30);
	private ArtWorldConnect awc;
	private ArtworkManager artworkManager;
	private ArtistManager artistManager;
	private MuseumManager museumManager;
	private ExhibitManager exhibitManager;
	private JPanel queryPanel, secondPanel, resultPanel;
	private JComboBox actionBox, attributeBox, museumBox;
	private JTextField[] textFields;
	private JLabel artworkNameLabel, artworkIDLabel, artworkMediumLabel, artworkCategoryLabel, artistNameLabel, artistIDLabel, artistBirthDateLabel, artistDeathDateLabel, museumIDLabel,
	museumNameLabel, museumCityLabel, museumStateLabel, exhibitNameLabel, exhibitIDLabel, otherExhibitNameLabel, exhibitCityLabel, exhibitStateLabel, exhibitStartDateLabel, exhibitEndDateLabel;
	private JLabel museumBoxLabel;
	private JButton executeButton;
	private int actionBoxIndex, overallAction;
	private Object[] museumIDs;
	
	public UIFrame (ArtWorldConnect awc) {
		this.awc = awc;
		this.artworkManager = new ArtworkManager(awc, this);
		this.artistManager = new ArtistManager(awc, this);
		this.museumManager = new MuseumManager(awc, this);
		this.exhibitManager = new ExhibitManager(awc, this);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(UIFrame.frameWidth, UIFrame.frameHeight);
		this.setLocationRelativeTo(null);
		this.setTitle("Art World Front End");
		this.setLayout(null);
		this.initializeTextAndLabels();

		//query panel
		queryPanel = new JPanel();
		queryPanel.setBackground(Color.darkGray);
		queryPanel.setBounds(0,0,UIFrame.frameWidth, UIFrame.queryPanelHeight);
		this.initializeQueryPanel();
		
		//other panels
		secondPanel = new JPanel();
		secondPanel.setBackground(Color.orange);
		secondPanel.setBounds(0, UIFrame.queryPanelHeight, UIFrame.frameWidth, UIFrame.otherPanelHeight);
		this.setSearchArtwork();
		
		resultPanel = new JPanel();
		resultPanel.setBounds(0, UIFrame.queryPanelHeight + UIFrame.otherPanelHeight, UIFrame.frameWidth, UIFrame.frameHeight - (UIFrame.queryPanelHeight + UIFrame.otherPanelHeight));
		
		this.add(queryPanel);
		this.add(secondPanel);
		this.add(resultPanel);
		this.setVisible(true);		
	}

	private void initializeQueryPanel() {
		//Generate the strings for the JComboBoxs
		String[] actions = {"Search For", "Create", "Staff Search For",  "Update", "Delete"};
		String[] attributes = {"Artwork", "Artist", "Museum", "Exhibit"};
		Object[] museums = museumManager.getMuseums();
		museumIDs = museumManager.getMuseumIDs();
		
		//Create Action Box and Label
		JLabel actionBoxLabel = new JLabel("Action:");
		actionBoxLabel.setFont(UIFrame.FONT);
		actionBoxLabel.setForeground(Color.white);
		actionBox = new JComboBox(actions);
		actionBox.setFont(UIFrame.FONT);
		actionBox.addActionListener(this);
		actionBoxIndex = 0;
		
		//Create Attribute Box and Label
		JLabel attributeBoxLabel = new JLabel("   ");
		attributeBoxLabel.setFont(UIFrame.FONT);
		attributeBoxLabel.setForeground(Color.white);
		attributeBox = new JComboBox(attributes);
		attributeBox.setFont(UIFrame.FONT);
		attributeBox.addActionListener(this);
		
		//Create Museum Box and Label
		museumBoxLabel = new JLabel(" At:");
		museumBoxLabel.setFont(UIFrame.FONT);
		museumBoxLabel.setForeground(Color.white);
		museumBox = new JComboBox(museums);
		museumBox.setFont(UIFrame.FONT);
		museumBox.addActionListener(this);
		
		//Create Execute Button
		executeButton = new JButton("Execute");
		executeButton.setFont(UIFrame.FONT);
		executeButton.addActionListener(this);
		
		//Adds all boxes and labels to the panel
		queryPanel.add(actionBoxLabel);
		queryPanel.add(actionBox);
		queryPanel.add(attributeBoxLabel);
		queryPanel.add(attributeBox);
		queryPanel.add(new JLabel("   "));
		queryPanel.add(executeButton);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == actionBox || e.getSource() == attributeBox) {
			if (overallAction != (overallAction = actionBox.getSelectedIndex() * 10 + attributeBox.getSelectedIndex()) ) {
				this.updateSecondPanel();
			}
		}
		if (e.getSource() == actionBox) {
			if (actionBoxIndex == 0 && (actionBoxIndex = actionBox.getSelectedIndex()) > 0) {
				queryPanel.remove(executeButton);
				queryPanel.add(museumBoxLabel);
				queryPanel.add(museumBox);
				queryPanel.add(executeButton);
			} else if (actionBoxIndex != 0 && (actionBoxIndex = actionBox.getSelectedIndex()) == 0) {
				queryPanel.remove(museumBoxLabel);
				queryPanel.remove(museumBox);
			}
		} else if (e.getSource() == executeButton) {
			this.executeAction();
		}
		this.repaint();
		this.setVisible(true);
	}

	private void executeAction() {
		switch (overallAction) {
		case (0):
			artworkManager.basicSearch(textFields[0].getText(), textFields[1].getText(), textFields[2].getText(), textFields[3].getText(), textFields[4].getText(), textFields[5].getText());
			break;
		case (1):
			artistManager.basicSearch(textFields[0].getText(), textFields[1].getText(), textFields[2].getText());
			break;
		case (2):
			museumManager.basicSearch(textFields[0].getText(), textFields[1].getText(), textFields[2].getText(), textFields[3].getText(), textFields[4].getText(), textFields[5].getText());
			break;
		case (3):
			exhibitManager.basicSearch(textFields[0].getText(), textFields[1].getText(), textFields[2].getText(), textFields[3].getText(), textFields[4].getText());
			break;
		case (10):
			artworkManager.createArtwork(textFields[0].getText(), textFields[1].getText(), textFields[2].getText(), textFields[3].getText(), museumIDs[museumBox.getSelectedIndex()]);
			break;
		case (11): 
			artistManager.createArtist(textFields[0].getText(), textFields[1].getText(), textFields[2].getText());
			break;
		case (12):
			museumManager.createMuseum(textFields[0].getText(), textFields[1].getText(), textFields[2].getText());
			//Need to update museums combo box and museumsIDs
			break;
		case (13):
			exhibitManager.createExhibit(textFields[0].getText(), textFields[1].getText(), textFields[2].getText(), museumIDs[museumBox.getSelectedIndex()]);
			break;
		case (20):
			artworkManager.staffSearch(textFields[0].getText(), textFields[1].getText(), textFields[2].getText(), textFields[3].getText(), textFields[4].getText(), textFields[5].getText(), museumIDs[museumBox.getSelectedIndex()]);
			break;
		case (21):
			artistManager.staffSearch(textFields[0].getText(), textFields[1].getText(), textFields[2].getText());
			break;
		case (22):
			museumManager.staffSearch(museumIDs[museumBox.getSelectedIndex()]);
			break;
		case (23):
			exhibitManager.staffSearch(textFields[0].getText(), textFields[1].getText(), textFields[2].getText(), textFields[3].getText(), museumIDs[museumBox.getSelectedIndex()]);
			break;
		case (30):
			artworkManager.updateArtwork(textFields[0].getText(), textFields[1].getText(), textFields[2].getText(), textFields[3].getText(), textFields[4].getText(), museumIDs[museumBox.getSelectedIndex()]);
			break;
		case (31):
			artistManager.updateArtist(textFields[0].getText(), textFields[1].getText(), textFields[2].getText(), textFields[3].getText(), museumIDs[museumBox.getSelectedIndex()]);
			break;
		case (32):
			museumManager.updateMuseum(textFields[0].getText(), textFields[1].getText(), textFields[2].getText(), museumIDs[museumBox.getSelectedIndex()]);
			break;
		case (33):
			exhibitManager.updateExhibit(textFields[0].getText(), textFields[1].getText(), textFields[2].getText(), textFields[3].getText(), museumIDs[museumBox.getSelectedIndex()]);
			break;
		case (40):
			artworkManager.deleteArtwork(textFields[0].getText(), textFields[1].getText(), museumIDs[museumBox.getSelectedIndex()]);
			break;
		case (41):
			artistManager.deleteArtist(textFields[0].getText(), textFields[1].getText(), museumIDs[museumBox.getSelectedIndex()]);
			break;
		case (42):
			museumManager.deleteMuseum(textFields[0].getText(), museumIDs[museumBox.getSelectedIndex()]);
			break;
		case (43):
			exhibitManager.deleteExhibit(textFields[0].getText(), textFields[1].getText(), museumIDs[museumBox.getSelectedIndex()]);
			break;
		default:
			System.out.println("execution not yet implemented");
		}
		this.repaint();
	}

	private void updateSecondPanel() {
		switch (overallAction) {
		case (0):
			this.setSearchArtwork();
			break;
		case (1):
			this.setSearchArtist();
			break;
		case (2):
			this.setSearchMuseum();
			break;
		case (3):
			this.setSearchExhibit();
			break;
		case (10):
			this.setInsertArtwork();
			break;
		case (11):
			this.setInsertArtist();
			break;
		case (12):
			this.setInsertMuseum();
			break;
		case (13):
			this.setInsertExhibit();
			break;
		case (20):
			this.setStaffSearchArtwork();
			break;
		case (21):
			this.setStaffSearchArtist();
			break;
		case (22):
			this.setStaffSearchMuseum();
			break;
		case (23):
			this.setStaffSearchExhibit();
			break;
		case (30):
			this.setArtworkUpdate();
			break;
		case (31):
			this.setArtistUpdate();
			break;
		case (32):
			this.setMuseumUpdate();
			break;
		case (33):
			this.setExhibitUpdate();
			break;
		case (40):
			this.setDeleteArtwork();
			break;
		case (41):
			this.setDeleteArtist();
			break;
		case (42):
			this.setDeleteMuseum();
			break;
		case (43):
			this.setDeleteExhibit();
			break;
		default:
			System.out.println("OverallAction: " + overallAction);
		}
		this.repaint();
	}

	private void setDeleteExhibit() {
		this.resetTextFields(2);
		this.secondPanel.removeAll();
		this.secondPanel.add(exhibitIDLabel);
		this.secondPanel.add(textFields[0]);
		this.secondPanel.add(otherExhibitNameLabel);
		this.secondPanel.add(textFields[1]);		
	}

	private void setDeleteMuseum() {
		this.resetTextFields(1);
		this.secondPanel.removeAll();
		this.secondPanel.add(museumNameLabel);
		this.secondPanel.add(textFields[0]);
	}

	private void setDeleteArtist() {
		this.resetTextFields(2);
		this.secondPanel.removeAll();
		this.secondPanel.add(artistIDLabel);
		this.secondPanel.add(textFields[0]);
		this.secondPanel.add(artistNameLabel);
		this.secondPanel.add(textFields[1]);
	}

	private void setDeleteArtwork() {
		this.resetTextFields(2);
		this.secondPanel.removeAll();
		this.secondPanel.add(artworkIDLabel);
		this.secondPanel.add(textFields[0]);
		this.secondPanel.add(artworkNameLabel);
		this.secondPanel.add(textFields[1]);
	}

	private void setExhibitUpdate() {
		this.resetTextFields(4);
		this.secondPanel.removeAll();
		this.secondPanel.add(exhibitIDLabel);
		this.secondPanel.add(textFields[0]);
		this.secondPanel.add(otherExhibitNameLabel);
		this.secondPanel.add(textFields[1]);
		this.secondPanel.add(exhibitStartDateLabel);
		this.secondPanel.add(textFields[2]);
		this.secondPanel.add(new JLabel("                                                "));
		this.secondPanel.add(exhibitEndDateLabel);
		this.secondPanel.add(textFields[3]);
	}

	private void setMuseumUpdate() {	
		this.resetTextFields(3);
		this.secondPanel.removeAll();
		this.secondPanel.add(museumNameLabel);
		this.secondPanel.add(textFields[0]);
		this.secondPanel.add(museumCityLabel);
		this.secondPanel.add(textFields[1]);
		this.secondPanel.add(museumStateLabel);
		this.secondPanel.add(textFields[2]);
	}

	private void setArtistUpdate() {
		this.resetTextFields(4);
		this.secondPanel.removeAll();
		this.secondPanel.add(artistIDLabel);
		this.secondPanel.add(textFields[0]);
		this.secondPanel.add(artistNameLabel);
		this.secondPanel.add(textFields[1]);
		this.secondPanel.add(artistBirthDateLabel);
		this.secondPanel.add(textFields[2]);
		this.secondPanel.add(artistDeathDateLabel);
		this.secondPanel.add(textFields[3]);
	}

	private void setArtworkUpdate() {
		this.resetTextFields(5);
		this.secondPanel.removeAll();
		this.secondPanel.add(artworkIDLabel);
		this.secondPanel.add(textFields[0]);
		this.secondPanel.add(artworkNameLabel);
		this.secondPanel.add(textFields[1]);
		this.secondPanel.add(artworkMediumLabel);
		this.secondPanel.add(textFields[2]);
		this.secondPanel.add(artworkCategoryLabel);
		this.secondPanel.add(textFields[3]);
		this.secondPanel.add(new JLabel("            "));
		this.secondPanel.add(artistIDLabel);
		this.secondPanel.add(textFields[4]);
	}

	private void setStaffSearchExhibit() {
		this.resetTextFields(4);
		this.secondPanel.removeAll();
		this.secondPanel.add(otherExhibitNameLabel);
		this.secondPanel.add(textFields[0]);
		this.secondPanel.add(artistNameLabel);
		this.secondPanel.add(textFields[1]);
		this.secondPanel.add(artworkNameLabel);
		this.secondPanel.add(textFields[2]);
		this.secondPanel.add(exhibitIDLabel);
		this.secondPanel.add(textFields[3]);
	}

	private void setStaffSearchMuseum() {
		this.secondPanel.removeAll();
	}

	private void setStaffSearchArtist() {
		this.resetTextFields(3);
		this.secondPanel.removeAll();
		this.secondPanel.add(artistNameLabel);
		this.secondPanel.add(textFields[0]);
		this.secondPanel.add(artworkNameLabel);
		this.secondPanel.add(textFields[1]);
		this.secondPanel.add(artistIDLabel);
		this.secondPanel.add(textFields[2]);
	}

	private void setStaffSearchArtwork() {
		this.resetTextFields(6);
		this.secondPanel.removeAll();
		this.secondPanel.add(artworkNameLabel);
		this.secondPanel.add(textFields[0]);
		this.secondPanel.add(artworkMediumLabel);
		this.secondPanel.add(textFields[1]);
		this.secondPanel.add(artworkCategoryLabel);
		this.secondPanel.add(textFields[2]);
		this.secondPanel.add(artistNameLabel);
		this.secondPanel.add(textFields[3]);
		this.secondPanel.add(exhibitNameLabel);
		this.secondPanel.add(textFields[4]);
		this.secondPanel.add(artworkIDLabel);
		this.secondPanel.add(textFields[5]);
		this.setVisible(true);		
	}

	private void setInsertExhibit() {
		this.resetTextFields(3);
		this.secondPanel.removeAll();
		this.secondPanel.add(otherExhibitNameLabel);
		this.secondPanel.add(textFields[0]);
		this.secondPanel.add(exhibitStartDateLabel);
		this.secondPanel.add(textFields[1]);
		this.secondPanel.add(exhibitEndDateLabel);
		this.secondPanel.add(textFields[2]);
	}

	private void setInsertMuseum() {
		this.resetTextFields(3);
		this.secondPanel.removeAll();
		this.secondPanel.add(museumNameLabel);
		this.secondPanel.add(textFields[0]);
		this.secondPanel.add(museumCityLabel);
		this.secondPanel.add(textFields[1]);
		this.secondPanel.add(museumStateLabel);
		this.secondPanel.add(textFields[2]);
	}

	private void setInsertArtist() {
		this.resetTextFields(3);
		this.secondPanel.removeAll();
		this.secondPanel.add(artistNameLabel);
		this.secondPanel.add(textFields[0]);
		this.secondPanel.add(artistBirthDateLabel);
		this.secondPanel.add(textFields[1]);
		this.secondPanel.add(artistDeathDateLabel);
		this.secondPanel.add(textFields[2]);
	}

	private void setInsertArtwork() {
		this.resetTextFields(4);
		this.secondPanel.removeAll();
		this.secondPanel.add(artworkNameLabel);
		this.secondPanel.add(textFields[0]);
		this.secondPanel.add(artworkMediumLabel);
		this.secondPanel.add(textFields[1]);
		this.secondPanel.add(artworkCategoryLabel);
		this.secondPanel.add(textFields[2]);
		this.secondPanel.add(artistIDLabel);
		this.secondPanel.add(textFields[3]);
	}

	private void setSearchExhibit() {
		this.resetTextFields(5);
		this.secondPanel.removeAll();
		this.secondPanel.add(exhibitNameLabel);
		this.secondPanel.add(textFields[0]);
		this.secondPanel.add(museumNameLabel);
		this.secondPanel.add(textFields[1]);
		this.secondPanel.add(exhibitCityLabel);
		this.secondPanel.add(textFields[2]);
		this.secondPanel.add(exhibitStateLabel);
		this.secondPanel.add(textFields[3]);
		this.secondPanel.add(artistNameLabel);
		this.secondPanel.add(textFields[4]);
	}

	private void setSearchMuseum() {
		this.resetTextFields(6);
		this.secondPanel.removeAll();
		this.secondPanel.add(museumNameLabel);
		this.secondPanel.add(textFields[0]);
		this.secondPanel.add(museumCityLabel);
		this.secondPanel.add(textFields[1]);
		this.secondPanel.add(museumStateLabel);
		this.secondPanel.add(textFields[2]);
		this.secondPanel.add(artworkNameLabel);
		this.secondPanel.add(textFields[3]);
		this.secondPanel.add(artistNameLabel);
		this.secondPanel.add(textFields[4]);
		this.secondPanel.add(exhibitNameLabel);
		this.secondPanel.add(textFields[5]);
	}

	private void setSearchArtist() {
		this.resetTextFields(3);
		this.secondPanel.removeAll();
		this.secondPanel.add(artistNameLabel);
		this.secondPanel.add(textFields[0]);
		this.secondPanel.add(artworkNameLabel);
		this.secondPanel.add(textFields[1]);
		this.secondPanel.add(museumNameLabel);
		this.secondPanel.add(textFields[2]);
	}
	
	private void setSearchArtwork() {
		this.resetTextFields(6);
		this.secondPanel.removeAll();
		this.secondPanel.add(artworkNameLabel);
		this.secondPanel.add(textFields[0]);
		this.secondPanel.add(artworkMediumLabel);
		this.secondPanel.add(textFields[1]);
		this.secondPanel.add(artworkCategoryLabel);
		this.secondPanel.add(textFields[2]);
		this.secondPanel.add(artistNameLabel);
		this.secondPanel.add(textFields[3]);
		this.secondPanel.add(museumNameLabel);
		this.secondPanel.add(textFields[4]);
		this.secondPanel.add(exhibitNameLabel);
		this.secondPanel.add(textFields[5]);
	}
	
	public JPanel getResultPanel() {
		return resultPanel;
	}

	private void initializeTextAndLabels() {
		textFields = new JTextField[7];
		for (int i = 0; i < 7; i++) {
			textFields[i] = new JTextField(20);
			textFields[i].setFont(UIFrame.FONT);
		}
		
		artworkNameLabel = new JLabel("Artwork Name:");
		artworkNameLabel.setFont(UIFrame.FONT);
		artworkIDLabel = new JLabel("Artwork ID:");
		artworkIDLabel.setFont(UIFrame.FONT);
		artworkMediumLabel = new JLabel("Artwork Medium:");
		artworkMediumLabel.setFont(UIFrame.FONT);
		artworkCategoryLabel = new JLabel("Artwork Category:");
		artworkCategoryLabel.setFont(UIFrame.FONT);
		artistNameLabel = new JLabel("Artist Name:");
		artistNameLabel.setFont(UIFrame.FONT);
		artistIDLabel = new JLabel("Artist ID:");
		artistIDLabel.setFont(UIFrame.FONT);
		artistBirthDateLabel = new JLabel("Artist Birth Date (year)");
		artistBirthDateLabel.setFont(UIFrame.FONT);
		artistDeathDateLabel = new JLabel("Artist Death Date (year)");
		artistDeathDateLabel.setFont(UIFrame.FONT);
		museumIDLabel = new JLabel("MuseumID: ");
		museumIDLabel.setFont(UIFrame.FONT);
		museumNameLabel = new JLabel("Museum Name:");
		museumNameLabel.setFont(UIFrame.FONT);
		museumCityLabel = new JLabel("Museum City:");
		museumCityLabel.setFont(UIFrame.FONT);
		museumStateLabel = new JLabel("Museum State (AL, AK, ...)");
		museumStateLabel.setFont(UIFrame.FONT);
		exhibitNameLabel = new JLabel("Active Exhibit Name:");
		exhibitNameLabel.setFont(UIFrame.FONT);
		otherExhibitNameLabel = new JLabel("Exhibit Name:");
		otherExhibitNameLabel.setFont(UIFrame.FONT);
		exhibitIDLabel = new JLabel("Exhibit ID:");
		exhibitIDLabel.setFont(UIFrame.FONT);
		exhibitCityLabel = new JLabel("Exhibit City:");
		exhibitCityLabel.setFont(UIFrame.FONT);
		exhibitStateLabel = new JLabel("Exhibit State (AL, AK, ...)");
		exhibitStateLabel.setFont(UIFrame.FONT);
		exhibitStartDateLabel = new JLabel("Exhibit Start Date (YYYY-MM-DD)");
		exhibitStartDateLabel.setFont(UIFrame.FONT);
		exhibitEndDateLabel = new JLabel("Exhibit End Date (YYYY-MM-DD)");
		exhibitEndDateLabel.setFont(UIFrame.FONT);
	}
	
	private void resetTextFields(int index) {
		for (int i = 0; i < index; i++) {
			textFields[i].setText("");
		}
	}

	public void createErrorMessage(String errorMessage) {
		this.resultPanel.removeAll();
		JLabel temp = new JLabel(errorMessage);
		temp.setFont(UIFrame.ERRORFONT);
		temp.setForeground(Color.red);
		this.resultPanel.add(temp);
	}
}
