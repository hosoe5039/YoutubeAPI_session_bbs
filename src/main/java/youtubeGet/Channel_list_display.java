package youtubeGet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/Channels_List")

public class Channel_list_display extends HttpServlet{

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		//文字コード設定
		request.setCharacterEncoding("UTF-8");

		//(1)チャンネル一覧、読み出し用のSQL実行

		Connection conn = Sql_conn.getDbConnection();
		PreparedStatement channel_state =  null;
		ResultSet channel_resultSet = null;

		//読み出し用SQL文
		String channels_read_sql =  "select id, channel  from CHANNELS;";

		//実行

		try {
			channel_state = conn.prepareStatement(channels_read_sql);
			channel_resultSet = channel_state.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		//読み出し結果をLISTに格納する
		List<Channel_Info_Bean> Channel_list = new ArrayList<>();

		//(2)読み出し結果をインスタンス化してLISTに保存する
		try {
			while (channel_resultSet.next()) {
				//チャンネルインスタンス初期化
				Channel_Info_Bean channel = new Channel_Info_Bean();
				channel.setID(String.valueOf(channel_resultSet.getInt("id")));//IDセット
				channel.setChannel_Name(channel_resultSet.getString("channel"));//チャンネル名セット
				Channel_list.add(channel);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		//(2)読み出し結果をJSPに転送
		request.setAttribute("Channels_list", Channel_list);

		//(3)JSPに遷移する
		request.getRequestDispatcher("/channel_list.jsp").forward(request, response);

	}
}

