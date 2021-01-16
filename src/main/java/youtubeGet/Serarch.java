/*
 * Copyright (c) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package youtubeGet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTube.Search;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.SearchResultSnippet;
import com.google.api.services.youtube.model.Video;



@WebServlet("/Serarch")


/**
 * Prints a list of videos based on a search term.
 *
 * @author Jeremy Walker
 */
public class Serarch extends HttpServlet {

//(1)プログラムで使用する変数、ファイルの定義

	//APIキーを記載したファイル
	private static String PROPERTIES_FILENAME = "youtube.properties";

	/** Global instance of the HTTP transport. */
	private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

	/** Global instance of the JSON factory. */
	private static final JsonFactory JSON_FACTORY = new JacksonFactory();

	//一度に取得する動画情報の件数
	private static final long NUMBER_OF_VIDEOS_RETURNED = 5;

	//結果を一時的に格納するリスト
	static List<String> Tmplist = new ArrayList<String>();

	/** Global instance of Youtube object to make all API requests. */
	private static YouTube youtube;

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {


// (2)プロパティファイルを読み込む
		Properties properties = new Properties();
		try {
			InputStream in = Search.class.getResourceAsStream("/" + PROPERTIES_FILENAME);
			properties.load(in);

		} catch (IOException e) {
			System.err.println("There was an error reading " + PROPERTIES_FILENAME + ": " + e.getCause()
			+ " : " + e.getMessage());
			System.exit(1);
		}


		youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, new HttpRequestInitializer() {
			public void initialize(HttpRequest request) throws IOException {}
		}).setApplicationName("youtube-cmdline-search-sample").build();

//(3)JSP入力フォームから受け取った情報からリクエストパラメータを作成する

		YouTube.Search.List search = youtube.search().list("id,snippet");

		String queryTerm = new String(request.getParameter("keyWord").getBytes("ISO-8859-1"));
		queryTerm = URLDecoder.decode(queryTerm,"UTF-8");

		String apiKey = properties.getProperty("youtube.apikey");
		search.setKey(apiKey);
		search.setQ(queryTerm);

		//指定期間の取得
		String After = request.getParameter("AfterDate");

		if(!After.isEmpty()) {
			After = After.replace("/","-")+"T00:00:00.000Z";
			DateTime AfterTime = DateTime.parseRfc3339(After);
			search.setPublishedAfter(AfterTime);
		}

		String Before = request.getParameter("BeforeDate");
		if(!Before.isEmpty()) {
			Before = Before.replace("/","-")+"T23:59:00.000Z";
			DateTime BeforeTime = DateTime.parseRfc3339(Before);
			search.setPublishedAfter(BeforeTime);

		}

		//動画の長さの指定
		search.setVideoDuration(request.getParameter("Video-long"));

		//ソート条件
		search.setType("video");
		search.setOrder(request.getParameter("Sort"));

		/*
		 * This method reduces the info returned to only the fields we need and makes calls more
		 * efficient.
		 */
		//search.setFields("items(id/kind,id/videoId,snippet/title,snippet/channelTitle,snippet/channelId,snippet/thumbnails/default/url)");
		search.setFields("items(*),nextPageToken");
		search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);
		System.out.println(search);


		SearchListResponse searchResponse = null;

//(4)リクエストパラメータを実行する→LISTに格納する。(リストの格納は他メソッドで処理している

		for(int i=0;i < 1; i++) {
			if (searchResponse != null && searchResponse.getNextPageToken() != null) {
				search.setPageToken(searchResponse.getNextPageToken());
			}
			try {
				searchResponse = search.execute();
				List<SearchResult> searchResultList = searchResponse.getItems();
				prettyPrint(searchResultList.iterator(), queryTerm, apiKey);
			}

			catch (GoogleJsonResponseException e) {
				System.err.println("There was a service error: " + e.getDetails().getCode() + " : "
						+ e.getDetails().getMessage());
			} catch (IOException e) {
				System.err.println("There was an IO error: " + e.getCause() + " : " + e.getMessage());
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}

//(5)LISTに格納した結果をCSVに書き込み→ダウンロード
		response.setContentType("text/csv");
	    response.setHeader("Content-Disposition", "attachment; filename=\"userDirectory.csv\"");
	    try
	    {
	    	//改行コード
	    	String NEW_LINE = "\r\n";
	    	//文字コード
	    	String charset = "UTF-8";

	        OutputStream outputStream = response.getOutputStream();
	        String Header = "動画タイトル,リンク,再生回数,チャンネル名,チャンネル登録者数";
	        outputStream.write(Header.getBytes(charset));
	        outputStream.write(NEW_LINE.getBytes(charset));

	        for (int i = 0; i < Tmplist.size(); i++) {

	        	outputStream.write(Tmplist.get(i).getBytes(charset));
	        	outputStream.write(NEW_LINE.getBytes(charset));
	        }
	        outputStream.flush();
	        outputStream.close();
	    }
	    catch(Exception e)
	    {
	        System.out.println(e.toString());
	    }

	}

//(4)リクエストの結果をLISTに格納するメソッド
	private static void prettyPrint(Iterator<SearchResult> iteratorSearchResults, String query,String apiKey) throws IOException {

		YouTube.Videos.List list = youtube.videos().list("statistics,snippet");
		list.setKey(apiKey);

		YouTube.Channels.List channels_list = youtube.channels().list("statistics");
		channels_list.setKey(apiKey);

		while (iteratorSearchResults.hasNext()) {

			SearchResult singleVideo = iteratorSearchResults.next();
			ResourceId rId = singleVideo.getId();


			// Double checks the kind is video.
			if (rId.getKind().equals("youtube#video")) {

				channels_list.setId(singleVideo.getSnippet().getChannelId());
				String VideoID = rId.getVideoId();
				list.setId(VideoID);
				Video v = list.execute().getItems().get(0);
				Channel c = channels_list.execute().getItems().get(0);

				String Title = singleVideo.getSnippet().getTitle(); //動画タイトル
				String viewCount = String.valueOf(v.getStatistics().getViewCount()); //再生回数
				String URL = "www.youtube.com/watch?v=" + VideoID; //URL
				SearchResultSnippet snippet = singleVideo.getSnippet();
				DateTime publishedAt = snippet.getPublishedAt();
				String publishedAtString = publishedAt.toString(); //投稿日
				String Channel = singleVideo.getSnippet().getChannelTitle(); //チャンネル名
				String TourokuSya = String.valueOf(c.getStatistics().getSubscriberCount()); //チャンネル登録者数

//				String outputResult = Title + "," + URL + "," + viewConut + "," + Channel + "," + TourokuSya;

				String outputData[] = {Title, URL, viewCount, Channel, TourokuSya};
				String outputResult = String.join(",", outputData);
				Tmplist.add(outputResult);

				System.out.println( publishedAtString);

				//取得結果をコンソールに表示させる
				//System.out.println("タイトル: " + Title);
				//System.out.println("動画リンク:"+URL);
				//System.out.println("再生回数: "+viewConut);
				//System.out.println("投稿日:"+ date);
				//System.out.println("チャンネル名:"+Channel);
				//System.out.println("チャンネル登録者数:"+ TourokuSya);
				//System.out.println("\n-------------------------------------------------------------\n");
			}
		}
	}
}