

/**
 * Created by ashwin on 5/12/16.
 */

//google-api-client-1.22.0.jar
import com.google.api.client.googleapis.json.GoogleJsonResponseException;

//google-http-client-1.22.0.jar
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;

//google-http-client-jackson2-1.22.0.jar
import com.google.api.client.json.jackson2.JacksonFactory;

//import com.google.api.services.samples.youtube.cmdline.Auth;

//google-api-services-youtube-v3-rev180-1.22.0.jar
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Thumbnail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class Search
{
    /**
     * Define a global variable that identifies the name of a file that
     * contains the developer's API key.
     */
    private static final String PROPERTIES_FILENAME = "youtube.properties";

    private static long NUMBER_OF_VIDEOS_RETURNED = 5;

    /**
     * Define a global instance of a Youtube object, which will be used
     * to make YouTube Data API requests.
     */
    private static YouTube youtube;

    /**
     * Initialize a YouTube object to search for videos on YouTube. Then
     * display the name and thumbnail image of each video in the result set.
     *
     * @param args command line args.
     */
    public static void main(String[] args) {

        //list of videos
        ArrayList<Video> videosList = new ArrayList<Video>();

        // Read the developer key from the properties file.
        Properties properties = new Properties();
        /*try {
            InputStream in = Search.class.getResourceAsStream("/home/ashwin/Java/youtube/" + PROPERTIES_FILENAME);
            properties.load(in);

        } catch (IOException e) {
            System.err.println("There was an error reading " + PROPERTIES_FILENAME + ": " + e.getCause()
                    + " : " + e.getMessage());
            System.exit(1);
        }*/

        try {
            // This object is used to make YouTube Data API requests. The last
            // argument is required, but since we don't need anything
            // initialized when the HttpRequest is initialized, we override
            // the interface and provide a no-op function.
            youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, new HttpRequestInitializer() {
                public void initialize(HttpRequest request) throws IOException {
                }
            }).setApplicationName("youtube-cmdline-search-sample").build();

            // Prompt the user to enter a query term.
            String queryTerm = getInputQuery();

            NUMBER_OF_VIDEOS_RETURNED = getInputNumberOfVideos();

            // Define the API request for retrieving search results.
            YouTube.Search.List search = youtube.search().list("id,snippet");

            // Set your developer key from the {{ Google Cloud Console }} for
            // non-authenticated requests. See:
            // {{ https://cloud.google.com/console }}
            //String apiKey = properties.getProperty("youtube.apikey");
            String apiKey = getInputApiKey();
            search.setKey(apiKey);
            search.setQ(queryTerm);

            // Restrict the search results to only include videos. See:
            // https://developers.google.com/youtube/v3/docs/search/list#type
            search.setType("video");

            // To increase efficiency, only retrieve the fields that the
            // application uses.
            search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
            search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);

            // Call the API and print results.
            SearchListResponse searchResponse = search.execute();
            List<SearchResult> searchResultList = searchResponse.getItems();
            if (searchResultList != null)
            {
                prettyPrint(searchResultList.iterator(), queryTerm);

                videosList = getVideosList(searchResultList.iterator(), queryTerm);
            }
        } catch (GoogleJsonResponseException e) {
            System.err.println("There was a service error: " + e.getDetails().getCode() + " : "
                    + e.getDetails().getMessage());
        } catch (IOException e) {
            System.err.println("There was an IO error: " + e.getCause() + " : " + e.getMessage());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /*
     * Prompt the user to enter a query term and return the user-specified term.
     */
    private static String getInputQuery() throws IOException {

        String inputQuery = "";

        System.out.print("Please enter a search term: ");
        BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
        inputQuery = bReader.readLine();

        if (inputQuery.length() < 1) {
            // Use the string "YouTube Developers Live" as a default.
            inputQuery = "YouTube Developers Live";
        }
        return inputQuery;
    }


    /*
     * Prompt the user to enter number of videos to be returned.
     */
    private static long getInputNumberOfVideos() throws IOException {

        long number = 1;

        System.out.print("Please enter number of videos: ");
        BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
        number = Integer.parseInt(bReader.readLine());

        if( number < 1 ) {
            number = 1;
        }

        return number;

    }


    /*
     * Prompt the user to enter google api key.
     */
    private static String getInputApiKey() throws IOException {

        String apiKey = "";

        System.out.print("Please enter your api key: ");
        BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
        apiKey = bReader.readLine();

        return apiKey;

    }


    private static ArrayList<Video> mVideosList = new ArrayList<>();

    /*
     * Prints out all results in the Iterator. For each result, print the
     * title, video ID, and thumbnail.
     *
     * @param iteratorSearchResults Iterator of SearchResults to print
     *
     * @param query Search query (String)
     */
    private static void prettyPrint(Iterator<SearchResult> iteratorSearchResults, String query) {

        System.out.println("\n=============================================================");
        System.out.println(
                "   First " + NUMBER_OF_VIDEOS_RETURNED + " videos for search on \"" + query + "\".");
        System.out.println("=============================================================\n");

        if (!iteratorSearchResults.hasNext())
        {
            System.out.println(" There aren't any results for your query.");
        }

        while (iteratorSearchResults.hasNext())
        {
            SearchResult singleVideo = iteratorSearchResults.next();
            ResourceId rId = singleVideo.getId();

            // Confirm that the result represents a video. Otherwise, the
            // item will not contain a video ID.
            if (rId.getKind().equals("youtube#video"))
            {
                Thumbnail thumbnail = singleVideo.getSnippet().getThumbnails().getDefault();

                System.out.println(" Video Id: " + rId.getVideoId());
                System.out.println(" Title: " + singleVideo.getSnippet().getTitle());
                System.out.println(" Thumbnail: " + thumbnail.getUrl());
                System.out.println("\n-------------------------------------------------------------\n");
            }
        }
    }

    private static ArrayList<Video> getVideosList(Iterator<SearchResult> iteratorSearchResults, String query)
    {
        ArrayList<Video> videosList = new ArrayList<Video>();

        if (!iteratorSearchResults.hasNext())
        {
            System.out.println(" There aren't any results for your query.");
        }

        Video video;
        int position = 1;

        while (iteratorSearchResults.hasNext())
        {
            SearchResult singleVideo = iteratorSearchResults.next();
            ResourceId rId = singleVideo.getId();

            // Confirm that the result represents a video. Otherwise, the
            // item will not contain a video ID.
            if (rId.getKind().equals("youtube#video"))
            {
                Thumbnail thumbnail = singleVideo.getSnippet().getThumbnails().getDefault();

                video = new Video();
                video.setPosition(position++);
                video.setId(rId.getVideoId());
                video.setTitle(singleVideo.getSnippet().getTitle());
                video.setImageurl(thumbnail.getUrl());
                video.setType("video");
                video.setActionurl("");
                video.toString();
                videosList.add(video);
            }
        }

        return videosList;
    }

}