package com.presisco.gossiptracker.util

import org.junit.Test
import kotlin.test.expect

class MicroBlogTest {

    @Test
    fun url2mid() {
        expect("3520617028999724") { MicroBlog.url2mid("https://weibo.com/2480531040/z8ElgBLeQ") }
        expect("4379882098277738") { MicroBlog.url2mid("https://weibo.com/2557129567/Hxw65yJpU") }
        expect("4379882098277738") { MicroBlog.url2mid("https://weibo.com/2557129567/Hxw65yJpU?refer_flag=1001030103_") }
        expect("z8ElgBLeQ") { MicroBlog.url2codedMid("https://weibo.com/2480531040/z8ElgBLeQ") }
        expect("z8ElgBLeQ") { MicroBlog.url2codedMid("https://weibo.com/2480531040/z8ElgBLeQ?refer_flag=1001030103_") }
    }

    @Test
    fun url2uid() {
        expect("2480531040") { MicroBlog.uidFromBlogUrl("https://weibo.com/2480531040/z8ElgBLeQ") }
        expect("32575896") { MicroBlog.uidFromUserUrl("https://weibo.com/32575896") }
        expect("someone") { MicroBlog.uidFromUserUrl("https://weibo.com/someone") }
    }

    @Test
    fun encodeTest() {
        expect("Hxw65yJpU") { MicroBlog.encodeMid("4379882098277738") }
    }

    @Test
    fun timeValidationTest() {
        expect(true) { MicroBlog.isValidTime("2018-01-01 08:00") }
        expect(false) { MicroBlog.isValidTime("6月25日 14:23") }
    }
}