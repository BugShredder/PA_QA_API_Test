package Steps;

import cucumber.api.DataTable;
import cucumber.api.PendingException;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import gherkin.deps.com.google.gson.JsonArray;
import gherkin.deps.com.google.gson.JsonElement;
import gherkin.deps.com.google.gson.JsonObject;
import gherkin.deps.com.google.gson.JsonParser;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicResponseHandler;
import org.junit.Assert;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SongsAPISteps<list> {
    private java.lang.String SongURL = "http://turing.niallbunting.com:3004";
    private HttpResponse SongRes;
    private java.lang.String StatusCode;
    private java.lang.String songResponse;
    private HttpResponse PostSongsRes;
    private String idString;
    private String _VString;
    private String Song;
    private String Artist;
    private String publishDate;
    private String createdDate;
    private String newSongID;
    private String newPlayListID;
    private String desc;
    private String title;
    private String j_object;

    // private SongsAPISteps

    @Given("^I generate restful request for a video using \"([^\"]*)\"$")
    public void iGenerateRestfulRequestForAVideoUsing(java.lang.String id) throws Throwable {
        SongRes = Request.Get(SongURL + "/api/video/" + id)
                //.execute().returnContent().asString();
                // .execute().returnResponse().getStatusLine().toString();
                .connectTimeout(1000)
                .socketTimeout(1000)
                .execute()
                .returnResponse();

        ResponseHandler<java.lang.String> handler = new BasicResponseHandler();
        songResponse = handler.handleResponse(SongRes);

        System.out.println(songResponse);
    }

    @Then("^I receive successful response \"([^\"]*)\"$")
    public void iReceiveSuccessfulResponse(int status) {

        assertEquals(SongRes.getStatusLine().getStatusCode(), status);
        System.out.println("Status code is: " + status);
        System.out.println("Status reason is: " + SongRes.getStatusLine().getReasonPhrase());

    }


    @Then("^get response contains  \"([^\"]*)\" \"([^\"]*)\" \"([^\"]*)\" \"([^\"]*)\" \"([^\"]*)\" \"([^\"]*)\"$")
    public void responseContains(java.lang.String id, java.lang.String artist, java.lang.String song, java.lang.String pub_date, java.lang.String __v, String created_date) throws Throwable {

        Assert.assertTrue(songResponse.contains(artist));
        Assert.assertTrue(songResponse.contains(song));
        Assert.assertTrue(songResponse.contains(pub_date));
        Assert.assertTrue(songResponse.contains(__v));
        GetIDandCreatedDate();

    }


    @Given("^I generate restful request to post a video using  \"([^\"]*)\" \"([^\"]*)\" \"([^\"]*)\"$")
    public void iGenerateRestfulRequestToPostAVideoUsing(String artist, String song, String published_date) throws Throwable {
        SongRes = Request.Post(SongURL + "/api/video/")
                .useExpectContinue()
                .version(HttpVersion.HTTP_1_1)
                .addHeader("content-type", "application/json")
                .addHeader("Accept", "application/json")
                .bodyString("{ \"artist\": \"" + artist + "\", \"song\": \"" + song + "\", \"publishDate\": \"" + published_date + "\" }", ContentType.APPLICATION_JSON)
                .execute()
                .returnResponse();
        ResponseHandler<String> handler = new BasicResponseHandler();
        songResponse = handler.handleResponse(SongRes);
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(songResponse).getAsJsonObject().get("_id");
        newSongID = element.toString();
        newSongID = newSongID.replace("\"", "");
        System.out.println("Song ID = " + newSongID + " was imported into the DB");

    }

    @Then("^post response contains id created date \"([^\"]*)\"  \"([^\"]*)\" \"([^\"]*)\"$")
    public void postResponseContainsIdCreatedDate(java.lang.String artist, java.lang.String song, java.lang.String pub_date) throws Throwable {

        System.out.println(songResponse);
        Assert.assertTrue(songResponse.contains(artist));
        Assert.assertTrue(songResponse.contains(song));
        Assert.assertTrue(songResponse.contains(pub_date));
        GetIDandCreatedDate();
    }

    private void GetIDandCreatedDate() {
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(songResponse);
        JsonObject songs = element.getAsJsonObject();

        JsonElement id = songs.get("_id");
        idString = id.toString();
        idString = idString.replace("\"", "");
        assertTrue(idString != null);
        System.out.println("Song ID = " + idString);

        JsonElement dateCreated = songs.get("date_created");
        createdDate = dateCreated.toString();
        createdDate = createdDate.replace("\"", "");
        assertTrue(createdDate != null);
        System.out.println("date_created = " + createdDate);



    }

    @Given("^I generate a restful request to update a video using id \"([^\"]*)\"$")
    public void iGenerateARestfulRequestToUpdateAVideoUsingId(String id) throws Throwable {
        SongRes = Request.Patch(SongURL+"/api/video/" +id)
                .useExpectContinue()
                .version(HttpVersion.HTTP_1_1)
                .addHeader("content-type", "application/json")
                .addHeader("Accept", "application/json")
                .execute()
                .returnResponse();
    }


    @Then("^I get \"([^\"]*)\" \"([^\"]*)\" response$")
    public void iGetResponse(int status, String message) {
        assertEquals(SongRes.getStatusLine().getStatusCode(), status);
        assertEquals(SongRes.getStatusLine().getReasonPhrase(), message);

        System.out.println("Status code is: " + status);
        System.out.println("Reason is: " +message);
    }

    @When("^I generate a restful request to delete a video using newSongID$")
    public void iGenerateARestfulRequestToDeleteAVideoUsingNewSongID() throws Throwable {
        SongRes = Request.Delete(SongURL+"/api/video/" +newSongID)
                .version(HttpVersion.HTTP_1_1)
                .addHeader("content-type", "application/json")
                .execute()
                .returnResponse();
        if (SongRes.getStatusLine().getStatusCode()==204) {
            System.out.println("The song id: " +newSongID + " was deleted successfully");
        }else
            System.out.println("The song id: " +newSongID + " was NOT deleted!!!");
    }


    @Then("^I get all videos in the list$")
    public void iGetAllVideosInTheList() throws Throwable {
        convertSongs();
    }

    @Given("^I generate restful request to get all videos$")
    public void iGenerateRestfulRequestToGetAllVideos() throws Throwable {
        SongRes = Request.Get(SongURL+"/api/video")
                .connectTimeout(1000)
                .socketTimeout(1000)
                .execute()
                .returnResponse();
        ResponseHandler < String > handler = new BasicResponseHandler();
        songResponse = handler.handleResponse(SongRes);

    }

    private void convertSongs() {
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(songResponse);
        JsonArray songs = element.getAsJsonArray();
        System.out.println(songs);
        for (int i=0; i < songs.size(); i++) {
            JsonObject songsJson = (JsonObject) songs.get(i);
            JsonElement id = songsJson.get("_id");
            idString = id.toString();
            idString = idString.replace("\"", "");
            System.out.println("Song ID " + i + " = " + idString);

            JsonElement artist = songsJson.get("artist");
            Artist = artist.toString();
            Artist = Artist.replace("\"", "");
            System.out.println("Artist " + i + " = " + Artist);

            JsonElement songname = songsJson.get("song");
            Song = songname.toString();
            Song = Song.replace("\"", "");
            System.out.println("Song " + i + " = " + Song);
        }


    }


    @Then("^I get all playlists$")
    public void iGetAllPlaylists() throws Throwable {
       convertPlaylist();
    }

    @Given("^I generate restful request to get all playlists$")
    public void iGenerateRestfulRequestToGetAllPlaylists() throws Throwable {
            SongRes = Request.Get(SongURL+"/api/playlist")
                    .connectTimeout(1000)
                    .socketTimeout(1000)
                    .execute()
                    .returnResponse();
            ResponseHandler < String > handler = new BasicResponseHandler();
            songResponse = handler.handleResponse(SongRes);

    }

    private void convertPlaylist() {
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(songResponse);
        JsonArray PlaylistAll = element.getAsJsonArray();
        System.out.println(PlaylistAll);
        for (int i=0; i < PlaylistAll.size(); i++) {
            JsonObject Playlist = (JsonObject) PlaylistAll.get(i);
            JsonElement id = Playlist.get("_id");
            idString = id.toString();
            idString = idString.replace("\"", "");
            System.out.println("List ID " + i + " = " + idString);

            JsonElement decs = Playlist.get("desc");
            desc = decs.toString();
            desc = desc.replace("\"", "");
            System.out.println("desc " + i + " = " + desc);

            JsonElement Title = Playlist.get("title");
            title = Title.toString();
            title = title.replace("\"", "");
            System.out.println("title " + i + " = " + title);
        }


    }

    @Given("^I generate restful request to get a playlist using \"([^\"]*)\"$")
    public void iGenerateRestfulRequestToGetAPlaylistUsing(String id) throws Throwable {
        SongRes = Request.Get(SongURL+"/api/playlist/" + id)
                .connectTimeout(1000)
                .socketTimeout(1000)
                .execute()
                .returnResponse();
        ResponseHandler < String > handler = new BasicResponseHandler();
        songResponse = handler.handleResponse(SongRes);

    }



    @And("^response contains following list details$")
    public void responseContainsFollowingListDetails(DataTable table) {
        List<List<String>> data = table.raw();
        System.out.println(data.get(1).get(1));
        String data1 = data.get(0).get(1);
        validateSingleJsonObject(data.get(0).get(1),data.get(1).get(1));
        validateSingleJsonObject(data.get(0).get(2),data.get(1).get(2));
        validateSingleJsonObject(data.get(0).get(3),data.get(1).get(3));


    }

    @And("^response contains following videos$")
    public void responseContainsFollowingVideos(DataTable table) {
        List<List<String>> data = table.raw();
       if (data.get(1).get(1).contains("false") ) {
           validateSingleJsonObject(data.get(0).get(1),data.get(1).get(1));
           validateSingleJsonObject(data.get(0).get(2),data.get(1).get(2));
           validateSingleJsonObject(data.get(0).get(3),data.get(1).get(3));

       }
    }

    private void validateSingleJsonObject(String name, String value){
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(songResponse);
        JsonObject list = element.getAsJsonObject();

        JsonElement Name = list.get(name);
        if (Name == null){
            System.out.println("Object not found in response: " +name);
            assertTrue(false);
        }
        j_object = Name.toString();
        j_object = j_object.replace("\"", "");
        assertEquals("The object value " +j_object +" is equal to " +value, j_object, value);
        System.out.println(name + " = " + j_object);

    }

    @Given("^I generate restful request to post playlist$")
    public void iGenerateRestfulRequestToPostPlaylist(DataTable table) throws Throwable {
        List<List<String>> data = table.raw();
            SongRes = Request.Post(SongURL+"/api/playlist")
                    .connectTimeout(1000)
                    .socketTimeout(1000)
                    .addHeader("content-type", "application/json")
                    .addHeader("Accept", "application/json")
                    .bodyString(" {\""+ data.get(0).get(0) +"\": \""+data.get(1).get(0)+"\", \""+data.get(0).get(1)+"\": \""+data.get(1).get(1)+"\"}\n", ContentType.APPLICATION_JSON)
                    .execute()
                    .returnResponse();
            ResponseHandler < String > handler = new BasicResponseHandler();
            songResponse = handler.handleResponse(SongRes);
        System.out.println(songResponse);
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(songResponse).getAsJsonObject().get("_id");
        newPlayListID = element.toString();
        newPlayListID = newPlayListID.replace("\"", "");
        System.out.println("Song ID = " + newPlayListID + " was imported into the DB");

    }

    @And("^response contains id and date_created$")
    public void responseContainsIdAndDate_created() throws Throwable {
        GetIDandCreatedDate();
    }


    @Given("^I generate restful request to update playlist \"([^\"]*)\" \"([^\"]*)\" \"([^\"]*)\"$")
    public void iGenerateRestfulRequestToUpdatePlaylist(String plylist_id, String id, String action) throws Throwable {
        SongRes = Request.Patch(SongURL+"/api/playlist/" + plylist_id)
                .connectTimeout(1000)
                .socketTimeout(1000)
                .addHeader("content-type", "application/json")
                .addHeader("Accept", "application/json")
                .bodyString("{ \"videos\": [ {\""+id+"\": \""+action+"\"} ] }", ContentType.APPLICATION_JSON)
                .execute()
                .returnResponse();
        System.out.println(SongRes);
    }


    @When("^I generate a restful request to delete playlist using newID$")
    public void iGenerateARestfulRequestToDeletePlaylistUsingNewID() throws Throwable {
        SongRes = Request.Delete(SongURL+"/api/playlist/" + newPlayListID)
                .connectTimeout(1000)
                .socketTimeout(1000)
                .addHeader("content-type", "application/json")
                .addHeader("Accept", "application/json")
                .execute()
                .returnResponse();
        System.out.println(SongRes);
        if (SongRes.getStatusLine().getStatusCode()==204) {
            System.out.println("The song id: " +newPlayListID + " was deleted successfully");
        }else
            System.out.println("The song id: " +newPlayListID + " was NOT deleted!!!");
    }
}