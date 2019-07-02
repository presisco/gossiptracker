package com.presisco.gossiptracker.bolt

import com.presisco.lazyjdbc.client.MapJdbcClient
import com.presisco.lazystorm.bolt.jdbc.MapJdbcClientBolt
import com.presisco.lazystorm.getInt
import com.presisco.lazystorm.lifecycle.Configurable
import org.apache.storm.task.TopologyContext
import org.apache.storm.topology.BasicOutputCollector
import org.apache.storm.tuple.Tuple

class MicroBlogTagCreationBolt : MapJdbcClientBolt(), Configurable {

    @Transient
    private lateinit var tagIdMap: HashMap<String, Int>
    private var mapCapacity = 100000

    override fun configure(config: Map<String, *>) {
        if (config.containsKey("map_capacity")) {
            mapCapacity = config.getInt("map_capacity")
        }
    }

    override fun prepare(topoConf: MutableMap<String, Any>?, context: TopologyContext?) {
        super.prepare(topoConf, context)
        tagIdMap = HashMap(mapCapacity)
    }

    override fun process(
        boltName: String,
        streamName: String,
        data: List<*>,
        table: String,
        client: MapJdbcClient,
        collector: BasicOutputCollector
    ) = emptyList<Any>()

    override fun execute(tuple: Tuple, outputCollector: BasicOutputCollector) {
        val tagName = tuple.getStringByField("name")
        val tagLink = tuple.getStringByField("link")

        if (tagIdMap.containsKey(tagName)) {
            outputCollector.emitData(
                hashMapOf(
                    "mid" to tuple.getStringByField("mid"),
                    "tid" to tagIdMap[tagName]
                )
            )
        }

        val client = getJdbcClient()
        val existTags = client.buildSelect("tid")
            .from(tableName)
            .where("name", "=", tagName)
            .execute()

        val tagId = if (existTags.isEmpty()) {
            client.insertInto(tableName)
                .columns("name", "link")
                .values(tagName, tagLink)
                .execute()
            val lastId = client.select("SELECT LAST_INSERT_ID() tid").first()
            lastId.getInt("tid")
        } else {
            existTags.first().getInt("tid")
        }
        if (tagIdMap.size == mapCapacity) {
            tagIdMap.remove(tagIdMap.keys.first())
        }
        tagIdMap[tagName] = tagId
        outputCollector.emitData(
            hashMapOf(
                "mid" to tuple.getStringByField("mid"),
                "tid" to tagId
            )
        )

    }
}