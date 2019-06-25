package com.presisco.gossiptracker.bolt

import com.presisco.gossiptracker.util.MicroBlog
import com.presisco.lazystorm.DATA_FIELD_NAME
import com.presisco.lazystorm.bolt.LazyBasicBolt
import com.presisco.lazystorm.getListOfMap
import com.presisco.lazystorm.getString
import org.apache.storm.topology.BasicOutputCollector
import org.apache.storm.topology.OutputFieldsDeclarer
import org.apache.storm.tuple.Fields
import org.apache.storm.tuple.Tuple
import org.apache.storm.tuple.Values

class MicroBlogRectifyBolt : LazyBasicBolt<Map<String, Any>>() {

    override fun execute(tuple: Tuple, collector: BasicOutputCollector) {
        val data = getInput(tuple)

        try {
            if (!data.containsKey("content")) {
                collector.emit("user", Values(data))
                return
            }

            if (!data.containsKey("user")) {
                return
            }

            val rectifiedBlog = hashMapOf(
                "url" to data["__url"],
                "id" to MicroBlog.url2codedMid(data.getString("__url")),
                "uid" to MicroBlog.uidFromUserUrl(data.getString("user")),
                "create_time" to data["create_time"]
            )

            if (data.containsKey("repost_link")) {
                rectifiedBlog["repost_link"] = data["repost_link"]
                rectifiedBlog["repost_id"] = MicroBlog.url2codedMid(data["repost_link"] as String)
            }

            collector.emit("blog", Values(rectifiedBlog))
            if (data.containsKey("comment_list")) {
                data.getListOfMap("comment_list")
                    .filter { it.containsKey("comment_id") }
                    .map {
                        hashMapOf(
                            "blog_id" to rectifiedBlog.getString("id"),
                            "id" to it.getString("comment_id"),
                            "uid" to MicroBlog.uidFromUserUrl(it.getString("user_link")),
                            "create_time" to it.getString("create_time")
                        )
                    }.forEach {
                        collector.emit("comment", Values(it))
                    }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override fun declareOutputFields(declarer: OutputFieldsDeclarer) {
        declarer.declareStream("blog", Fields(DATA_FIELD_NAME))
        declarer.declareStream("comment", Fields(DATA_FIELD_NAME))
        declarer.declareStream("user", Fields(DATA_FIELD_NAME))
    }
}