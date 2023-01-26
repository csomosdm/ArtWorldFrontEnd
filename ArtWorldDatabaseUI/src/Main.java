public class Main {

	public static void main(String[] args) {
		ArtWorldConnect awc = new ArtWorldConnect();
		ArtworkByFiltersService awbfs = new ArtworkByFiltersService(awc);
		awc.connect("csomosdm", "Sbr83(D");
		UIFrame frame = new UIFrame(awc, awbfs);
//		awc.closeConnection();
//		awc.connect("AWDUser", "Awd93>X!5v");
	}

}
