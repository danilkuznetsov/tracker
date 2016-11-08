package model;

import javax.swing.text.html.parser.Entity;
import java.util.HashMap;
import java.util.Map;

public class Track {

    private String issueTitle;
    private Integer issueNumber;
    private Map<Integer, Map<String, Integer>> trackingInIssue;
    private Map<Integer, Map<String, Integer>> trackingInPullRequest;

    public Track(Integer issueNumber, String issueTitle){
        this.issueNumber = issueNumber;
        this.issueTitle = issueTitle;
        this.trackingInIssue = new HashMap<>();
        this.trackingInPullRequest = new HashMap<>();
    }

    public Map<String, Integer> getTotalTrack(){
        Map<String, Integer> totalTrack = new HashMap<>();
        if (trackingInIssue != null){
            totalTrack = trackingInIssue.get(issueNumber);
        }

        if (trackingInPullRequest != null){
            for(Map.Entry<Integer, Map<String,Integer>> entry : trackingInPullRequest.entrySet()){
                for(Map.Entry<String, Integer> en : entry.getValue().entrySet()){
                    if (totalTrack.get(en.getKey()) == null){
                        totalTrack.put(en.getKey(), en.getValue());
                    }else {
                        totalTrack.put(en.getKey(), totalTrack.get(en.getKey())+en.getValue());
                    }
                }
            }
        }

        return totalTrack;
    }

    public void addTrackingInIssue(Map<String, Integer> newTrack){
        if (newTrack != null){
            if(trackingInIssue.size() == 0 ){
                trackingInIssue.put(issueNumber, newTrack);
            }else {
                sumupTrack(trackingInIssue.get(issueNumber), newTrack);
            }
        }
    }

    public void addTrackingInPullRequest(Map<Integer, Map<String, Integer>> newTrack){
        if(trackingInPullRequest.size() == 0){
            trackingInPullRequest = newTrack;
        }else{
            newTrack.forEach((k,v)-> {
                if(trackingInPullRequest.get(k) == null){
                    trackingInPullRequest.put(k, v);
                }else{
                    sumupTrack(trackingInPullRequest.get(k), newTrack.get(k));
                }
            });
        }
    }

    private void sumupTrack(Map<String, Integer> currentTrack, Map<String, Integer> newTrack) {
        newTrack.forEach((k, v)-> {
            if(currentTrack.get(k) != null){
                System.out.println("put next value = " + currentTrack.get(k)+v);
                currentTrack.put(k, currentTrack.get(k)+v);
            }else {
                System.out.println("put next value1 = " + currentTrack.get(k));
                currentTrack.put(k, v);
            }
        });
    }


    public String getIssueTitle() {
        return issueTitle;
    }

    public Integer getIssueNumber() {
        return issueNumber;
    }

    public Map<Integer, Map<String, Integer>> getTrackingInIssue() {
        return trackingInIssue;
    }

    public Map<Integer, Map<String, Integer>> getTrackingInPullRequest() {
        return trackingInPullRequest;
    }

    @Override
    public String toString() {
        return "Track{" +
                "issueTitle='" + issueTitle + '\'' +
                ", issueNumber=" + issueNumber +
                ", trackingInIssue=" + trackingInIssue +
                ", trackingInPullRequest=" + trackingInPullRequest +
                '}';
    }
}
