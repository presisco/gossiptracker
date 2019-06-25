package com.presisco.gossiptracker

import com.presisco.lazystorm.Launch

object Entrance : Launch() {

    override val createCustomSpout =
        { name: String, _: Map<String, Any?> -> throw IllegalStateException("unsupported spout name: $name") }

    override val createCustomBolt =
        { name: String, _: Map<String, Any?> -> throw IllegalStateException("unsupported bolt name: $name") }

    @JvmStatic
    fun main(args: Array<String>) {
        launch(args)
    }
}