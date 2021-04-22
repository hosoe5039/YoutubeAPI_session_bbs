<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"%>
<%@ page import = "java.util.*"
import = "youtubeGet.Channel_Info_Bean"%>

<%
	String mode = (String)request.getAttribute("mode");
	String channel_name =  "'"+ (String)request.getAttribute("channel_name") + "'";

	// TODO あいうえお
	List <Channel_Info_Bean> list = (List<Channel_Info_Bean>)request.getAttribute("Channel_info_list");

	Channel_Info_Bean channel_info = list.get(0);
	Channel_Info_Bean channel_info_last = list.get(list.size() - 1);

	String data =""; //Y軸データ
	String chart_title = "";//グラフタイトル
	String day = "";//グラフのデータ日付け
	String period="";//データ抽出期間
	String unit = "";//Y軸単位
	String chart_max ="";
	String chart_min ="";

	int data_value_subscriber = 0;//チャンネル登録者データ
	double data_value_view_count = 0;
	int max_subscriber_count = 0;
	int min_subscriber_count = 0;
	double max_view_count = 0;
	double min_view_count = 0;
	int step_data = 0;

	if(mode.equals("subscriber_count")){
		chart_title = "チャンネル登録者推移";
		unit = "人";
		max_subscriber_count = channel_info.getSubscriber_count();//登録者数の最大値の初期値
		min_subscriber_count = channel_info.getSubscriber_count();//登録者数の最小値の初期値

	}else if(mode.equals("total_view_count")){
		unit = "回数";
		chart_title = "チャンネル動画再生回数";
		max_view_count = channel_info_last.getTotal_view_count();//動画再生回数の最大値
		min_view_count = channel_info.getTotal_view_count();//動画再生回数の最小値
		chart_max = String.valueOf(max_view_count);
		chart_min = String.valueOf(min_view_count);


		//再生回数グラフのStep数の決定
		if(max_view_count == min_view_count){
			max_view_count = max_view_count + 10;
			min_view_count = min_view_count - 10;
			step_data = 10;
		}else if (max_view_count != min_view_count){
			if(max_view_count - min_view_count <= 10){
				step_data = (int)(max_view_count - min_view_count);
			}else{
				step_data = (int)(max_view_count - min_view_count) /10;
			}
		}
	}

	for (int i = 0; i < list.size(); i++){
		Channel_Info_Bean channel = list.get(i);
		day = day + "'"+ channel.getDay() + "'";

		if(mode.equals("subscriber_count")){

			data_value_subscriber = channel.getSubscriber_count();

			// チャンネル登録者の最大値、最小値を求める
			if(max_subscriber_count < data_value_subscriber){
				max_subscriber_count = data_value_subscriber;
			}else if(min_subscriber_count > data_value_subscriber){
				min_subscriber_count = data_value_subscriber;
			}

			data = data +  String.valueOf(data_value_subscriber) + ",";

		}else if(mode.equals("total_view_count")){
			data_value_view_count =  channel.getTotal_view_count();
			data = data +  String.valueOf(data_value_view_count) + ",";
		}



	}

	// 登録者推移グラフのStep数の決定
	if(mode.equals("subscriber_count")){

		chart_max = String.valueOf(max_subscriber_count);
		chart_min = String.valueOf(min_subscriber_count);

		if(max_subscriber_count == min_subscriber_count){
			max_subscriber_count = max_subscriber_count + 10;
			min_subscriber_count = min_subscriber_count - 10;
			step_data = 10;
		}else if (max_subscriber_count != min_subscriber_count){
			if(max_subscriber_count - min_subscriber_count >= 10){
				step_data = max_subscriber_count - min_subscriber_count;
			}else{
				step_data = (max_subscriber_count - min_subscriber_count) /10;
			}
		}
	}

		day = day.replace("''","','");
		String period_day = "'" +  channel_info.getDay() +"～"+ channel_info_last.getDay() + "'";
		data = data + "#";
		data = data.replace(",#","");
		String step_size = String.valueOf(step_data);
%>
<!DOCTYPE html>
<html lang="ja">

<head>
  <meta charset="utf-8">
　<title><%=chart_title%></title>
</head>
<body>
  <h1><%=chart_title%></h1>
  <canvas id="myLineChart"></canvas>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/Chart.js/2.7.2/Chart.bundle.js"></script>

  <script>
  var ctx = document.getElementById("myLineChart");
  var myLineChart = new Chart(ctx, {
    type: 'line',
    data: {
      labels: [<%=day%>],
      datasets: [
        {
          label: <%=channel_name%>,
          data: [<%=data%>],
          borderColor: "rgba(255,0,0,1)",
          backgroundColor: "rgba(0,0,0,0)"
        },
      ],
    },
    options: {
      title: {
        display: true,
        text: <%=period_day%>,
      },
      scales: {
        yAxes: [{
          ticks: {
            suggestedMax: <%=chart_max%>,
            suggestedMin: <%=chart_min%>,
            stepSize: <%=step_size%>,
            callback: function(value, index, values){
              return  value +  '<%=unit%>'
            }
          }
        }]
      },
    }
  });
  </script>
</body>

</html>