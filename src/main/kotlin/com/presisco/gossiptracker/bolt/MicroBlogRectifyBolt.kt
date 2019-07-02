package com.presisco.gossiptracker.bolt

import com.presisco.gossiptracker.util.MicroBlog
import com.presisco.lazystorm.DATA_FIELD_NAME
import com.presisco.lazystorm.getListOfMap
import com.presisco.lazystorm.getString
import org.apache.storm.topology.BasicOutputCollector
import org.apache.storm.topology.OutputFieldsDeclarer
import org.apache.storm.topology.base.BaseBasicBolt
import org.apache.storm.tuple.Fields
import org.apache.storm.tuple.Tuple
import org.apache.storm.tuple.Values

class MicroBlogRectifyBolt : BaseBasicBolt() {

    override fun execute(tuple: Tuple, collector: BasicOutputCollector) {
        val data = tuple.getValue(0) as Map<String, *>

        val rectifiedBlog = hashMapOf(
            "url" to data["__url"],
            "mid" to MicroBlog.url2codedMid(data.getString("__url")),
            "uid" to MicroBlog.uidFromUserUrl(data.getString("user")),
            "from" to data["from"],
            "content" to data["content"],
            "like" to data.getOrDefault("like", -1),
            "comment" to data.getOrDefault("comment", -1),
            "repost" to data.getOrDefault("repost", -1),
            "create_time" to data["create_time"]
        )

        if (data.containsKey("repost_link")) {
            rectifiedBlog["repost_link"] = data["repost_link"]
            rectifiedBlog["repost_id"] = MicroBlog.url2codedMid(data["repost_link"] as String)
        }

        collector.emit("blog", Values(rectifiedBlog))
        if (data.containsKey("comment_list")) {
            val rectifiedComments = data.getListOfMap("comment_list")
                .filter {
                    it.containsKey("comment_id")
                            && MicroBlog.isValidTime(it.getString("create_time"))
                }
                .map {
                    hashMapOf(
                        "mid" to rectifiedBlog.getString("mid"),
                        "cid" to MicroBlog.encodeMid(it.getString("comment_id")),
                        "uid" to MicroBlog.uidFromUserUrl(it.getString("user_link")),
                        "content" to it["content"],
                        "like" to it.getOrDefault("like", -1),
                        "create_time" to it["create_time"]
                    )
                }
            collector.emit("comment", Values(rectifiedComments))
        }
        if (data.containsKey("tags")) {
            data.getListOfMap("tags")
                .filter {
                    it.containsKey("text") &&
                            it.getString("text") != "O网页链接"
                }.forEach {
                    collector.emit(
                        "tag", Values(
                            rectifiedBlog.getString("mid"),
                            it.getString("text"),
                            it.getString("link")
                        )
                    )
                }
        }
    }

    override fun declareOutputFields(declarer: OutputFieldsDeclarer) {
        declarer.declareStream("blog", Fields(DATA_FIELD_NAME))
        declarer.declareStream("comment", Fields(DATA_FIELD_NAME))
        declarer.declareStream("user", Fields(DATA_FIELD_NAME))
        declarer.declareStream("tag", Fields("mid", "name", "link"))
    }
}