package servlets;

import model.Track;
import services.TrackingServes;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@WebServlet("/")
public class MainServlet extends HttpServlet {


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("track.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        String issueNumber = request.getParameter("issueNumber");
        String repoName = request.getParameter("repoName");
        try {
            validateIssueNumber(issueNumber);
            Track track  =  new TrackingServes().getTrackByIssueNumber(Integer.valueOf(issueNumber), repoName);
            request.setAttribute("track", track);
        }catch (NumberFormatException e) {
            System.out.println(e.getMessage());
            request.setAttribute("error", "Incorrect Issue Number");
        }catch (IOException e){
            System.out.println(e.getMessage());
            request.setAttribute("error", "Technical exception, maybe incorrect credential");
        }finally {
            request.getRequestDispatcher("track.jsp").forward(request, response);
        }

    }

    private void validateIssueNumber(String issueNumber) {
        if (issueNumber == null) throw new NumberFormatException();
        Integer.valueOf(issueNumber);
    }


}
