package youtubeGet;

import java.io.IOException;
import java.net.URLDecoder;
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


@WebServlet("/Chart_display")

public class Chart_display extends HttpServlet{
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// 文字コード
		request.setCharacterEncoding("UTF-8");

		String mode = request.getParameter("mode");


		//取得対象のチャンネル名→JSPへ情報を転送する
		String channel_name = URLDecoder.decode(request.getParameter("channel"),"UTF-8");
		request.setAttribute("channel_name", channel_name);

		//(1)SQL文を作成して実行

		//チャンネル情報の読み出し条件の取得
		String id = request.getParameter("id");


		//DB接続
		Connection conn = Sql_conn.getDbConnection();
		PreparedStatement channel_info_state =  null;
		ResultSet channel_info_resultSet = null;

		String sql = "select " + mode + ", get_day from  CHANNEL_INFO where CHANNELS_ID = " + id;
		sql = sql + " AND " + mode + " is not null;";
	System.out.println(sql);
		try {
			channel_info_state = conn.prepareStatement(sql);
			channel_info_resultSet = channel_info_state.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		}


		//(2)読み出し結果をインスタンス化してLISTに保存する

		List<Channel_Info_Bean> Channel_info_list = new ArrayList<>();

		try {
			while (channel_info_resultSet.next()) {

				Channel_Info_Bean channel = new Channel_Info_Bean();

				if(mode.equals("subscriber_count")) {
					channel.setSubscriber_count(channel_info_resultSet.getInt("subscriber_count"));
				}else if(mode.equals("total_view_count")) {
					channel.setTotal_view_count(channel_info_resultSet.getLong("total_view_count"));
				}

				channel.setDay(String.valueOf(channel_info_resultSet.getDate("get_day")));
				Channel_info_list.add(channel);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		//(3)結果を取得して表示用JSPへ転送する
		request.setAttribute("Channel_info_list", Channel_info_list);
		request.setAttribute("mode", mode);

		//(4)JSPに遷移する
		request.getRequestDispatcher("/channel_info_chart.jsp").forward(request, response);

	}
}