package com.presisco.gossiptracker

import com.presisco.lazystorm.Launch
import com.presisco.lazystorm.bolt.jdbc.SimpleReplaceBolt

object Entrance : Launch() {

    override val createCustomSpout =
        { name: String, _: Map<String, Any?> -> throw IllegalStateException("unsupported spout name: $name") }

    override val createCustomBolt = { name: String, config: Map<String, Any?> ->
        with(config) {
            val className = getString("class")
            when (className) {
                "WeiboReplaceBolt" -> SimpleReplaceBolt().setTimeFormat("yyyy-MM-dd HH:mm")
                else -> throw IllegalStateException("unsupported bolt class: $className")
            }
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        launch(args)
    }
}