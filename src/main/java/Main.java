import model.Track;
import services.TrackingServes;

import java.io.IOException;


public class Main {
    public static void main(String[] args) throws IOException {
        TrackingServes ts  = new TrackingServes();
        Track track = ts.getTrackByIssueNumber(1099, "socialscore");
        System.out.println(track);
        System.out.println(track.getTotalTrack());
    }
}
