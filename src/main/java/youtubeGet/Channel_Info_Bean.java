package youtubeGet;

//チャンネル情報インスタンス
public class Channel_Info_Bean {

		private String id; //チャンネルID
		private String channel_name;//チャンネル名
		private int subscriber_count;//チャンネル登録者数
		private String get_day;//情報取得日



		public Channel_Info_Bean(){};

		public String getID() {
		    return id;
		}
		public void setID(String id) {
		    this.id = id;
		}
		public String getChannel_Name() {
		    return channel_name;
		}
		public void setChannel_Name(String channel_name) {
		    this.channel_name = channel_name;
		}

		public int getSubscriber_count() {
		    return subscriber_count;
		}
		public void setSubscriber_count(int subscriber_count) {
		    this.subscriber_count =subscriber_count;
		}

		public String getDay() {
		    return get_day;
		}
		public void setDay(String get_day) {
		    this.get_day =get_day;
		}

	}
