<%@ page language="java" contentType="text/html;charset=utf-8"%>
<html>
<body>
	<form action="./Serarch" Method="post">
		<p>キーワード</p>
		<p><input type="text" name="keyWord"/></p>
		<p>指定期間</p>
		<p><input type="date" name="AfterDate">　～　<input type="date" name="BeforeDate"></p>
		<p>動画の長さ</p>
		<p>
		<select name="Video-long" size="1">
		<option value="any">指定しない</option>
		<option value="short">4分未満</option>
		<option value="medium">4 分以上 20 分以下</option>
		<option value="long">20分以上</option>
		</select>
		</p>

		<p>並び替え</p>
		<p>
		<select name="Sort" size="1">
		<option value="relevance">指定しない</option>
		<option value="viewCount">再生回数順</option>
		<option value="date">投稿日時順</option>
		<option value="rate">評価の高い順</option>
		</select>
		</p>
		<input type="submit" value="検索">
	</form>
</body>
</html>