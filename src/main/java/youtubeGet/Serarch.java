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

		//(1)サーバから動画情報を取得する

		//(1-1)リクエストパラメータを作成
		YouTube.Search.List search = youtube.search().list("id,snippet");

		//キーワードの取得
		String queryTerm = new String(request.getParameter("keyWord").getBytes("ISO-8859-1"));
		queryTerm = URLDecoder.decode(queryTerm,"UTF-8");

		//APIキーの設定
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

		//ソート条件の設定
		search.setType("video");
		search.setOrder(request.getParameter("Sort"));

		//取得件数の設定
		search.setFields("items(*),nextPageToken");
		search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);
		System.out.println(search);


		SearchListResponse searchResponse = null;

		//(1-2)リクエストパラメータを実行

		for(int i=0;i < 3; i++) {
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

		//(2)動画情報をcsvにする
		//(3)csvをダウンロードできるようにする
		response.setContentType("text/csv");
	    response.setHeader("Content-Disposition", "attachment; filename=\"userDirectory.csv\"");
	    try
	    {
	    	//改行コード
	    	String NEW_LINE = "\r\n";
	    	//文字コード
	    	String charset = "UTF-8";

	        OutputStream outputStream = response.getOutputStream();
	        String Header = "動画タイトル,リンク,再生回数,投稿日,チャンネル名,チャンネル登録者数";
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


	private static void prettyPrint(Iterator<SearchResult> iteratorSearchResults, String query,String apiKey) throws IOException {

		//(1-3)動画の詳細情報を取得する

		YouTube.Videos.List VideoList = youtube.videos().list("statistics,snippet");
		VideoList.setKey(apiKey);

		YouTube.Channels.List ChannelList = youtube.channels().list("statistics");
		ChannelList.setKey(apiKey);

		while (iteratorSearchResults.hasNext()) {

			SearchResult singleVideo = iteratorSearchResults.next();
			ResourceId rId = singleVideo.getId();


			// Double checks the kind is video.
			if (rId.getKind().equals("youtube#video")) {

				//(1-3-1)VideoInformations = サーバから"動画情報を取得する(params)
				String VideoID = rId.getVideoId();
				VideoList.setId(VideoID);
				Video VideoInformations = VideoList.execute().getItems().get(0);

				//(1-3-2)ChannelInformations = サーバからチャンネル情報を取得する(params)
				ChannelList.setId(singleVideo.getSnippet().getChannelId());
				Channel ChannelInformations = ChannelList.execute().getItems().get(0);


				//(1-4)動画情報リストを作成する
				String Title = singleVideo.getSnippet().getTitle(); //動画タイトル
				String viewCount = String.valueOf(VideoInformations.getStatistics().getViewCount()); //再生回数
				String URL = "www.youtube.com/watch?v=" + VideoID; //URL
				SearchResultSnippet snippet = singleVideo.getSnippet();
				DateTime publishedAt = snippet.getPublishedAt();
				String publishedAtString = publishedAt.toString(); //投稿日
				String Channel = singleVideo.getSnippet().getChannelTitle(); //チャンネル名
				String TourokuSya = String.valueOf(ChannelInformations.getStatistics().getSubscriberCount()); //チャンネル登録者数

				//String outputResult = Title + "," + URL + "," + viewConut + "," + Channel + "," + TourokuSya;

				//動画情報をLISTに格納
				String outputData[] = {Title, URL, viewCount,publishedAtString, Channel, TourokuSya};
				String outputResult = String.join(",", outputData);
				Tmplist.add(outputResult);


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