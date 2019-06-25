package com.presisco.gossiptracker.bolt

import com.presisco.gsonhelper.MapHelper
import com.presisco.lazystorm.DATA_FIELD_NAME
import com.presisco.lazystorm.bolt.LazyBasicBolt
import com.presisco.lazystorm.getList
import com.presisco.lazystorm.getString
import com.presisco.lazystorm.lifecycle.Configurable
import org.apache.storm.task.TopologyContext
import org.apache.storm.topology.BasicOutputCollector
import org.apache.storm.topology.OutputFieldsDeclarer
import org.apache.storm.tuple.Fields
import org.apache.storm.tuple.Tuple
import org.apache.storm.tuple.Values

class ContentClassifyBolt : LazyBasicBolt<String>(), Configurable {

    @Transient
    private lateinit var mapHelper: MapHelper

    private lateinit var sources: List<String>

    @Transient
    private lateinit var matchers: List<Regex>

    override fun configure(config: Map<String, *>) {
        sources = config.getList("sources")
    }

    override fun prepare(topoConf: MutableMap<String, Any>?, context: TopologyContext?) {
        mapHelper = MapHelper()
        matchers = sources.map { "https?://[A-Za-z\\d-_\\\\.]*?weibo[A-Za-z\\d-_\\\\.]*?/".toRegex() }
    }

    override fun execute(tuple: Tuple, collector: BasicOutputCollector) {
        val kafkaMessage = tuple.getValueByField("value") as String
        val data = mapHelper.fromJson(kafkaMessage) as HashMap<String, Any?>

        var source: String? = null
        for (index in 0.until(matchers.size)) {
            if (data.getString("__url").contains(matchers[index])) {
                source = sources[index]
            }
        }

        source?.let {
            data["source"] = it
            collector.emit(it, Values(data))
        }
    }

    override fun declareOutputFields(declarer: OutputFieldsDeclarer) {
        sources.forEach { declarer.declareStream(it, Fields(DATA_FIELD_NAME)) }
    }

}