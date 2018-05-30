import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import services.TrackingServes;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


public class Main {
    public static void main(String[] args) throws IOException {
        String apiKey = "<api_key>";
        String owner = "vkuchyn";
        String manager = "@Bizon4ik";

        TrackingServes ts = new TrackingServes(apiKey, owner, manager);

        GitHubClient git = new GitHubClient();
        git.setOAuth2Token(apiKey);

        String repoName = "scorezoo_front";
        String issueLabels = "CLOSE IT"; // A list of comma separated label names. Example: bug,ui,@high
        String issueState = "open";

        IssueService issueService = new IssueService(git);
//        List<Integer> numberIssues = asList(2829);
        List<Integer> numberIssues = ts.fetchIdIssues(repoName, issueState, issueLabels);

        System.out.println("Found next issue: \n");
        System.out.println(numberIssues);

        Map<String, Integer> allTimeByAuthor = numberIssues
            .stream()
            .map(number -> {
                try {
                    return ts.getTrackByIssueNumber(number, repoName);
                } catch (IOException e) {
                    throw new RuntimeException();
                }
            })
            .flatMap(track -> {
                List<Main.TimeReport> reports = track.getTotalTrack()
                    .entrySet()
                    .stream()
                    .map(en -> new Main.TimeReport(en.getKey(), en.getValue()))
                    .collect(Collectors.toList());

                System.out.println("Issue:  " + track.getIssueNumber());
                System.out.println("Issue Title:  " + track.getIssueTitle());
                System.out.println("\nTime by authors:\n");
                System.out.println(issueReportComment(reports));
                System.out.println("-----------------------------------------------");

                try {
                    // TODO wrap with interface
                    issueService.createComment(owner, repoName, track.getIssueNumber().intValue(), issueReportComment(reports));
                    final Issue issue = issueService.getIssue(owner, repoName, track.getIssueNumber().intValue());
                    issue.setState(IssueService.STATE_CLOSED);
                    issueService.editIssue(owner, repoName, issue);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return reports.stream();
            })
            .collect(
                Collectors.toMap(Main.TimeReport::getAuthor, Main.TimeReport::getTime, (d1, d2) -> d1 + d2)
            );

        System.out.println("\n Time track by author: \n");
        System.out.println(allTimeByAuthor);

    }

    private static String issueReportComment(List<Main.TimeReport> reports) {
        return reports.stream()
            .map(TimeComment::new)
            .map(Object::toString)
            .collect(Collectors.joining("\n"));
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

    // TODO(vkuchyn): extract to public class, cover with unit test.
    private static class TimeComment {
        private Main.TimeReport report;

        public TimeComment(Main.TimeReport report) {
            this.report = report;
        }

        @Override
        public String toString() {
            final int hours = this.report.getTime() / 60;
            final int minutes = this.report.getTime() % 60;
            String hoursStr = Optional.of(hours)
                .filter(h -> h > 0)
                .map(h -> h + "h")
                .orElse("");

            return new StringBuilder("@")
                .append(this.report.getAuthor())
                .append(" - ")
                .append(hoursStr)
                .append(minutes).append("m")
                .append(" included.")
                .toString();
        }
    }
}
