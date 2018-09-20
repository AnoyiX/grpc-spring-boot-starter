package com.anoyi.grpc.test

import io.gatling.core.Predef._
import io.gatling.http.Predef._

class V1SofaHessianTest extends Simulation {

  // 测试数据
  val data = "{\"id\":1,\"name\":\"anoyi\",\"age\":20,\"gender\":\"男\",\"scores\":{\"语文\":99,\"数学\":20},\"friend\":{\"id\":2,\"name\":\"乖乖\",\"age\":21,\"gender\":\"女\",\"scores\":{\"语文\":10,\"数学\":98},\"friend\":null,\"pet\":null,\"listValue\":[]},\"pet\":{\"type\":\"龙\",\"name\":\"青尊\",\"owner\":{\"id\":1,\"name\":\"anoyi\",\"age\":20,\"gender\":\"男\",\"scores\":null,\"friend\":null,\"pet\":null,\"listValue\":null}},\"listValue\":[1,\"哇哦\"]}"

  // 请求数
  val maxCount = 100000

  // 测试
  val userCount = 100
  val repeatCount = maxCount / userCount
  val scn = scenario("性能测试[并发" + userCount + "][总次数" + maxCount + "]").repeat(repeatCount) {
    exec(
      http("V1-添加用户/sofa")
        .post("http://localhost:8081/v1/user/add")
        .header("Content-Type", "application/json")
        .body(StringBody(data))
        .check(status.is(200))
    )
  }.repeat(repeatCount) {
    exec(
      http("V1-查询用户/sofa")
        .get("http://localhost:8081/v1/user/list")
        .check(status.is(200))
    )
  }

  setUp(scn.inject(atOnceUsers(userCount)))

}