/*
 * Copyright 2018 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.mobilehelptosave.support

import java.net.URL

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Suite}

case class WireMockBaseUrl(value: URL)

trait WireMockSupport extends BeforeAndAfterAll with BeforeAndAfterEach with AppBuilder {
  me: Suite =>

  lazy val wireMockPort: Int = wireMockServer.port
  val wireMockHost                 = "localhost"
  lazy val wireMockBaseUrlAsString = s"http://$wireMockHost:$wireMockPort"
  lazy val wireMockBaseUrl         = new URL(wireMockBaseUrlAsString)
  protected implicit lazy val implicitWireMockBaseUrl: WireMockBaseUrl = WireMockBaseUrl(wireMockBaseUrl)

  protected def basicWireMockConfig(): WireMockConfiguration = wireMockConfig()

  protected implicit lazy val wireMockServer: WireMockServer = {
    val server = new WireMockServer(basicWireMockConfig().dynamicPort())
    server.start()
    server
  }

  override protected def afterAll(): Unit = {
    wireMockServer.stop()
    super.afterAll()
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    wireMockServer.resetAll()
  }

  override protected def appBuilder: ApplicationBuilder = super.appBuilder.configure(
    "appName"                                      -> "mobile-help-to-save",
    "auditing.enabled"                             -> false,
    "microservice.services.auth.port"              -> wireMockPort,
    "microservice.services.help-to-save.port"      -> wireMockPort,
    "microservice.services.mobile-shuttering.port" -> wireMockPort
  )
}
