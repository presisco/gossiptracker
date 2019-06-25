package com.presisco.gossiptracker.bolt

import com.presisco.lazystorm.LazyLaunch
import com.presisco.lazystorm.test.LazyBasicBoltTest
import com.presisco.lazystorm.test.SimpleDataTuple
import org.junit.Before
import org.junit.Test

class MicroBlogWriteNodeBoltTest : LazyBasicBoltTest(LazyLaunch, "sample/config.json", "neo4j_create_relation_bolt") {

    @Before
    fun prepare() {
        fakeEmptyPrepare()

    }

    @Test
    fun createBlogNodeTest() {
        val inputTuple = SimpleDataTuple(
            "data" to mapOf(
                "id" to "H9jWbhVkd",
                "user" to "5726844795",
                "create_time" to "2018-12-28 14:54",
                "repost_id" to "H9dJ8cWAX",
                "comment_list" to listOf(
                    mapOf(
                        "id" to "1234",
                        "create_time" to "2018-12-30 14:54",
                        "user_link" to "https://weibo.com/5726844796/"
                    ),
                    mapOf(
                        "id" to "1235",
                        "create_time" to "2018-12-30 14:55",
                        "user_link" to "https://weibo.com/5726844797/"
                    ),
                    mapOf(
                        "id" to "1236",
                        "create_time" to "2018-12-30 14:56",
                        "user_link" to "https://weibo.com/5726844798/"
                    )
                )
            ), stream = "blog"
        )
        val collector = fakeBasicOutputCollector()
        bolt.execute(inputTuple, collector)
    }

    @Test
    fun createCommentNodeTest() {
        val inputTuple = SimpleDataTuple(
            "data" to mapOf(
                "id" to "1234",
                "create_time" to "2018-12-30 14:54",
                "user" to "5726844796",
                "blog_id" to "H9jWbhVkd"
            ), stream = "comment"
        )
        val collector = fakeBasicOutputCollector()
        bolt.execute(inputTuple, collector)
    }
}