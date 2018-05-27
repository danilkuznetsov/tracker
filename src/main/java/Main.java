import services.TrackingServes;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class Main {
    public static void main(String[] args) throws IOException {
        String apiKey = "Insert your private api key";
        String owner = "vkuchyn";
        String manager = "@JujaD";

        TrackingServes ts = new TrackingServes(apiKey, owner, manager);


        String repoName = "github-issue-stat";
        String issueLabels = ""; // A list of comma separated label names. Example: bug,ui,@high
        String issueState = "open";

        List<Integer> numberIssues = ts.fetchIdIssues(repoName, issueState, issueLabels);

        System.out.println("Found next issue: \n");
        System.out.println(numberIssues);

        Map<String, Integer> allTimeByAuthor = numberIssues
                .stream()
                .map((number) -> {
                    try {
                        return ts.getTrackByIssueNumber(number, repoName);
                    } catch (IOException e) {
                        throw new RuntimeException();
                    }
                })
                .flatMap(track -> {
                    List<TimeReport> reports = track.getTotalTrack()
                            .entrySet()
                            .stream()
                            .map(en -> new TimeReport(en.getKey(), en.getValue()))
                            .collect(Collectors.toList());

                    System.out.println("Issue:  " + track.getIssueNumber());
                    System.out.println("Issue Title:  " + track.getIssueTitle());
                    System.out.println("\nTime by authors:\n");
                    System.out.println(reports);
                    System.out.println("-----------------------------------------------");

                    return reports.stream();
                })
                .collect(
                        Collectors.toMap(TimeReport::getAuthor, TimeReport::getTime, (d1, d2) -> d1 + d2)
                );

        System.out.println("\n Time track by author: \n");
        System.out.println(allTimeByAuthor);

    }

    private static class TimeReport {
        private String author;
        private int time;

        public TimeReport(String author, int time) {
            this.author = author;
            this.time = time;
        }

        public String getAuthor() {
            return author;
        }

        public int getTime() {
            return time;
        }

        @Override
        public String toString() {
            return "TimeReport{" +
                    "author='" + author + '\'' +
                    ", time=" + time +
                    '}';
        }
    }
}
