package youtubeGet;

import java.sql.Connection;
import java.sql.DriverManager;



public class Sql_conn {

	static Connection getDbConnection(){

		Connection conn = null;

		try {
			Class.forName("com.mysql.jdbc.Driver");

			// Connectionの作成
			conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/youtubeinfo?serverTimezone=UTC&useSSL=false",
					"root", "searchman");
		}catch (Exception e) {
			e.printStackTrace();
			// TODO ログにエラーメッセージを出力する
		}
		return conn;
	}
}
