package services;

import model.Track;
import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;

import java.io.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.regex.Pattern;

public class TrackingServes {
    private final Pattern pattern = Pattern.compile("^[0-9HMhm]+");
    private final Pattern onlyDigit = Pattern.compile("^[0-9]+");
    private final String BIZON4IK = "@Bizon4ik";
    private String CREDENTIAL;
    private final String OWNER_REPOSITORY = "vkuchyn";

    public TrackingServes() throws IOException {
        Properties prop = new Properties();
        InputStream input = this.getClass().getClassLoader().getResourceAsStream("config.properties");
        prop.load(input);
        this.CREDENTIAL = prop.getProperty("credential");
    }

    public Track getTrackByIssueNumber(Integer issueNumber, String repoName) throws IOException {
        Issue issue = getIssueByNumber(getProvider(repoName), issueNumber);
        Track track = new Track(issueNumber, issue.getTitle());
        track.addTrackingInIssue(getTrackFromIssue(issue).get(issue.getNumber()));
        track.addTrackingInIssue(getTrackFromIssueComments(issueNumber, repoName));
        track.addTrackingInPullRequest(getTrackFromPullRequest(getProvider(repoName), issue.getCreatedAt(), issueNumber, repoName));
        return track;
    }

    private Map<Integer, Map<String, Integer>> getTrackFromPullRequest(IRepositoryIdProvider provider, Date createdAt, Integer issueNumber, String repoName) throws IOException {
        LocalDateTime ldt = LocalDateTime.ofInstant(createdAt.toInstant(), ZoneId.systemDefault());
        ZonedDateTime zdt = ldt.atZone(ZoneOffset.UTC);
        List<Issue> issuesList = getIssuesSinceDate(provider, zdt.toString());
        return findTrackInPullRequest(issuesList, issueNumber, repoName);
    }

    private Map<Integer, Map<String, Integer>> findTrackInPullRequest(List<Issue> issuesList, Integer issueNumber, String repoName) {
       Map<Integer, Map<String, Integer>> result = new HashMap<>();
       issuesList.forEach(issue -> {
                                     String[] words = issue.getTitle().replaceFirst("[-_]", " ").split(" ");
                                     if (onlyDigit.matcher(words[0]).matches() && words[0].equals(issueNumber.toString())){
                                         Map<Integer, Map<String, Integer>> bodyTrack = getTrackFromIssue(issue);
                                         result.put(issue.getNumber(), bodyTrack.get(issue.getNumber()));
                                         try {
                                             Map<String, Integer> commentsTrack = getTrackFromIssueComments(issue.getNumber(),repoName);
                                             if (result.get(issue.getNumber()) == null){
                                                 result.put(issue.getNumber(), commentsTrack);
                                             }else{
                                                 sumUpTwoTrack(result.get(issue.getNumber()), commentsTrack);
                                             }
                                         } catch (IOException e) {
                                             e.printStackTrace();
                                         }
                                     }
                                });
        return result;
    }

    private void sumUpTwoTrack(Map<String, Integer> currentTrack, Map<String, Integer> newTrack) {
        newTrack.forEach((k,v)->{
            if (currentTrack.get(k) == null){
                currentTrack.put(k, v);
            }else {
                currentTrack.put(k, currentTrack.get(k)+v);
            }
        });
    }

    private Map<String, Integer> getTrackFromIssueComments(Integer issueNumber, String repoName) throws IOException {
        List<Comment> comments = getCommentsByIssueNumber(issueNumber, repoName);
        Map<String, Integer>  commentsTrack = new HashMap<>();
        for(Comment c: comments){
            Map<String, Integer>  commentTime = getTimeFromComments(c);
            if (commentTime.size() > 0){
                for(Map.Entry<String, Integer> entity : commentTime.entrySet()){
                    if (commentsTrack.get(entity.getKey())==null) {
                        commentsTrack.put(entity.getKey(), entity.getValue());
                    }else {
                        commentsTrack.put(entity.getKey(), commentsTrack.get(entity.getKey()) + entity.getValue());
                    }
                }
            }
        }
        return commentsTrack;
    }

    private Map<String, Integer> getTimeFromComments(Comment c) {
        int startIndex = c.getBody().indexOf(BIZON4IK);
        Map<String, Integer>  result = new HashMap<>();
        if (startIndex != -1){
            result = parseCommet(c, startIndex);
        }
        return result;
    }

    private Map<String, Integer> parseCommet(Comment c, int startIndex) {
        Map<String, Integer>  result = new HashMap<>();
        if (startIndex == c.getBody().lastIndexOf(BIZON4IK)){
            String[] st = c.getBody()
                    .substring(startIndex+10, c.getBody().length() >= startIndex + 24 ? startIndex + 24 : c.getBody().length())
                    .toLowerCase()
                    .split(" ");

            for(String s :  st) {
                if(pattern.matcher(s).matches()){
                    int time = parseTime(s);
                    if (time > 0) {
                        if(result.get(c.getUser().getLogin()) == null){
                            result.put(c.getUser().getLogin(), time);
                        } else {
                            result.put(c.getUser().getLogin(), result.get(c.getUser().getLogin()) + time);
                        }

                    }
                }else {
                }

            }
        }
        return result;
    }

    private List<Comment> getCommentsByIssueNumber(Integer issueNumber, String repoName) throws IOException {
        IssueService issueService = new IssueService(getGitHubClient());
        return issueService.getComments(getProvider(repoName), issueNumber);
    }

    private Map<Integer, Map<String, Integer>> getTrackFromIssue(Issue issue) {
        int startIndex = issue.getBody().indexOf(BIZON4IK);
        Map<Integer, Map<String, Integer>>  result = new HashMap<>();
        if (startIndex != -1){
            Map<String, Integer> time = parseBody(issue, startIndex);
            if(time.size()>0){
                result.put(issue.getNumber(), time);
            }
        }
        return result;
    }

    private Map<String,Integer> parseBody(Issue issue, int startIndex) {
        Map<String, Integer>  result = new HashMap<>();
        if (startIndex == issue.getBody().lastIndexOf(BIZON4IK)){
            String[] st = issue.getBody()
                    .substring(startIndex+10, issue.getBody().length() >= startIndex + 24 ? startIndex + 24 : issue.getBody().length())
                    .toLowerCase()
                    .split(" ");

            for(String s :  st) {
                if(pattern.matcher(s).matches()){
                    int time = parseTime(s);
                    if (time > 0) {
                        result.put(issue.getUser().getLogin(), time);
                    }
                }

            }
        }

        return result;
    }

    private int parseTime(String s) {
        int h = 0;
        int m = 0;
        int hourIndex  = s.indexOf("h");
        if(hourIndex != -1){
            h = h + Integer.valueOf(s.substring(0, hourIndex));
            int minutIndex = s.indexOf("m");
            if(minutIndex != -1){
                m = Integer.valueOf(s.substring(hourIndex+1, minutIndex));
            }
        }else {
            int minutIndex = s.indexOf("m");
            if(minutIndex != -1){
                m = Integer.valueOf(s.substring(0, minutIndex));
            }
        }
        return 60*h+m;
    }

    private Issue getIssueByNumber(IRepositoryIdProvider provider, Integer issueNumber) throws IOException {
        IssueService issueService = new IssueService(getGitHubClient());
        return issueService.getIssue(provider, issueNumber);
    }

    private List<Issue> getIssuesSinceDate(IRepositoryIdProvider provider, String since) throws IOException {
        IssueService issueService = new IssueService(getGitHubClient());
        Map<String, String> params = new HashMap<>();
        params.put("since", since);
        params.put("state", "all");
        return issueService.getIssues(provider, params);
    }

    private IRepositoryIdProvider getProvider(String repoName) {
        return new RepositoryId(OWNER_REPOSITORY, repoName);
    }

    private GitHubClient getGitHubClient() {
        GitHubClient client = new GitHubClient();
        client.setOAuth2Token(CREDENTIAL);
        return client;
    }
}
