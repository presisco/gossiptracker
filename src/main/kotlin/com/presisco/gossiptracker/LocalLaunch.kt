package com.presisco.gossiptracker

import org.apache.storm.LocalCluster

object LocalLaunch {

    @JvmStatic
    fun main(args: Array<String>) {
        LocalCluster.withLocalModeOverride(
            { Entrance.main(arrayOf("config=sample/config.json", "mode=cluster")) },
            3600
        )
        //        //LocalCluster.main(arrayOf("com.presisco.gossiptracker.Entrance","--local-ttl 3600"))
    }

}