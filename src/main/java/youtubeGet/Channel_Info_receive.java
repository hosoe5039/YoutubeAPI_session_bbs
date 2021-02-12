package youtubeGet;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTube.Search;
import com.google.api.services.youtube.model.Channel;

@WebServlet("/ChannelsInfo")

public class Channel_Info_receive extends HttpServlet{

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// 文字コード
		request.setCharacterEncoding("UTF-8");

		//(1)YoutubeAPIを使用する準備をする


		HttpTransport HTTP_TRANSPORT = new NetHttpTransport();//httpトランスポータ
		final JsonFactory JSON_FACTORY = new JacksonFactory();//JSONオブジェクト
		YouTube youtube;//YouTubeオブジェクト

		//プロパティファイルの読み込み
		Properties properties = new Properties();
		try {
			InputStream in = Search.class.getResourceAsStream("/youtube.properties");
			properties.load(in);
		} catch (IOException e) {
			System.err.println("There was an error reading " + "youtube.properties" + ": " + e.getCause()
			+ " : " + e.getMessage());
			System.exit(1);
		}

		//APIキーの設定
		String apiKey = properties.getProperty("youtube.apikey");

		//Youtubeオブジェクト初期化
		youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, new HttpRequestInitializer() {
			public void initialize(HttpRequest request) throws IOException {}
		}).setApplicationName("youtube-cmdline-search-sample").build();

		YouTube.Channels.List ChannelList = youtube.channels().list("statistics");
		ChannelList.setKey(apiKey);


		//(2)SQLテーブルCHANNELに接続をしてyoutube_channel_idを取得する

		// DB関連の初期設定
		Connection conn = Sql_conn.getDbConnection();
		PreparedStatement channel_state =  null;
		ResultSet channel_resultSet = null;

		//読み出し用SQL文
		String channels_read_sql = "select id, youtube_channel_id  from CHANNELS;";

		//実行
		try {
			channel_state = conn.prepareStatement(channels_read_sql);
			channel_resultSet = channel_state.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		//(3)取得したチャンネルIDを使用してリクエストパラメータを作成→実行
		//(4)取得したチャンネル情報をテーブルCHANNELS_INFOに保存

		try {
			while (channel_resultSet.next()) {

				//リクエストパラメータを作成→実行
				ChannelList.setId(channel_resultSet.getString("youtube_channel_id"));
				Channel ChannelInformations = ChannelList.execute().getItems().get(0);

				//詳細情報をもとにSQL文を作成

				//insert文のVALUESを設定
				String id =String.valueOf(channel_resultSet.getInt("id"));
				String subscriber_count =String.valueOf(ChannelInformations.getStatistics().getSubscriberCount()); //チャンネル登録者数
				String insert_values = String.join(",", "'" + id + "'", "'" + subscriber_count + "'", "now()");

				//実行用のSQL文を作成
				String Channel_Info_insert = "insert CHANNEL_INFO(CHANNELS_ID,subscriber_count,get_day)";
				Channel_Info_insert = Channel_Info_insert + "values(" + insert_values + ");";
				System.out.println(Channel_Info_insert);
				//SQLを実行する
				PreparedStatement channels_info_insert_state = conn.prepareStatement(Channel_Info_insert);
				channels_info_insert_state.execute();

			}
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

		request.getRequestDispatcher("/index.jsp").forward(request, response);
	}
}
//以下のコメントは今度裕太さんに質問する時のために残しておく
//		//(4)動画の詳細情報をSQLテーブルCHANNEL_INFOに保存する
//		for(HashMap.Entry<String,String> entry : Channel_Info_list.entrySet()){
//
//			//テーブルCHANNElS_INFOに保存するデータの取得
//		    String id = "'" + entry.getKey() + "'";
//		    String subscriber_count =   "'" + entry.getValue() + "'";
//
//
//		    //insertするSQL文作成
//		    String Channel_Info_insert = "insert CHANNEL_INFO(CHANNELS_ID,subscriber_count,get_day)";
//		    Channel_Info_insert = Channel_Info_insert + "values(" + insert_values + ");";
//
//		    try {
//				PreparedStatement channels_info_insert_state = conn.prepareStatement(Channel_Info_insert);
//				channels_info_insert_state.execute();
//
//			} catch (SQLException e) {
//				e.printStackTrace();
//			}
//
//		}

