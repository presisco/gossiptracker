package com.presisco.gossiptracker.bolt

import com.presisco.lazystorm.bolt.neo4j.Item
import com.presisco.lazystorm.bolt.neo4j.Neo4jResourceBolt
import com.presisco.lazystorm.getString
import org.apache.storm.topology.BasicOutputCollector
import org.apache.storm.tuple.Tuple
import org.neo4j.driver.v1.Statement
import org.neo4j.driver.v1.Transaction

class MicroBlogWriteNodeBlot : Neo4jResourceBolt<Map<String, Any?>>() {

    private val operationMap = hashMapOf(
        "blog" to fun(trans: Transaction, blogInfo: Map<String, Any?>) {
            val newBlogNode = Item.fromMap(
                mapOf(
                    "tags" to listOf("mblog"),
                    "props" to mapOf(
                        "id" to blogInfo.getString("id")
                    )
                ), "new"
            ).buildCreationCypher()
            trans.run(
                Statement(
                    "merge ($newBlogNode)" +
                            " on create set new.uid='${blogInfo.getString("uid")}'" +
                            ", new.create_time='${blogInfo.getString("create_time")}'" +
                            " on match set new.uid='${blogInfo.getString("uid")}'" +
                            ", new.create_time='${blogInfo.getString("create_time")}'"
                )
            )
            if (blogInfo.containsKey("repost_id")) {
                val originalBlogNode = Item.fromMap(
                    mapOf(
                        "tags" to listOf("mblog"),
                        "props" to mapOf(
                            "id" to blogInfo.getString("repost_id")
                        )
                    ), "original"
                ).buildCreationCypher()
                val repostRelation = Item.fromMap(
                    mapOf(
                        "tags" to listOf("repost")
                    )
                ).buildCreationCypher()
                trans.run(Statement("merge ($originalBlogNode)"))
                trans.run(
                    Statement(
                        "match ($originalBlogNode),($newBlogNode) " +
                                "merge (original)- [$repostRelation] -> (new)"
                    )
                )
            }
        },
        "comment" to fun(trans: Transaction, commentInfo: Map<String, Any?>) {
            val newCommentNode = Item.fromMap(
                mapOf(
                    "tags" to listOf("comment"),
                    "props" to mapOf(
                        "id" to commentInfo.getString("id"),
                        "uid" to commentInfo.getString("uid")
                    )
                ), "new"
            ).buildCreationCypher()
            val originalBlogNode = Item.fromMap(
                mapOf(
                    "tags" to listOf("mblog"),
                    "props" to mapOf(
                        "id" to commentInfo.getString("blog_id")
                    )
                ), "blog"
            ).buildCreationCypher()
            val commentRelation = Item.fromMap(
                mapOf(
                    "tags" to listOf("comment")
                ), "comment"
            ).buildCreationCypher()
            trans.run(Statement("merge ($newCommentNode)"))
            trans.run(Statement("merge ($originalBlogNode)"))
            trans.run(
                Statement(
                    "match ($originalBlogNode),($newCommentNode) " +
                            "merge (blog)- [$commentRelation] -> (new)"
                )
            )
        }
    )

    override fun execute(tuple: Tuple, collector: BasicOutputCollector) {
        if (!operationMap.containsKey(tuple.sourceStreamId)) {
            return
        }

        val info = getInput(tuple)
        val session = getSession()
        val trans = session.beginTransaction()

        try {
            operationMap[tuple.sourceStreamId]!!.invoke(trans, info)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }

        trans.success()
        session.close()
    }
}