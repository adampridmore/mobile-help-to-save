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

package uk.gov.hmrc.mobilehelptosave.connectors

import java.net.URL
import javax.inject.{Inject, Named, Singleton}

import com.google.inject.ImplementedBy
import play.api.LoggerLike
import play.api.libs.json.JsValue
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.encoding.UriPathEncoding.encodePathSegments

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[NativeAppWidgetConnectorImpl])
trait NativeAppWidgetConnector {

  def answers(campaignId: String, questionKey: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[Seq[String]]]

}

@Singleton
class NativeAppWidgetConnectorImpl @Inject() (
  logger: LoggerLike,
  @Named("native-app-widget-baseUrl") baseUrl: URL,
  http: CoreGet
) extends NativeAppWidgetConnector {

  override def answers(campaignId: String, questionKey: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[Seq[String]]] =
    http.GET[JsValue](answersUrl(campaignId, questionKey).toString).map { jsonBody =>
      Some((jsonBody \\ "content").map(_.as[String]))
    } recover {
      case e@(_: HttpException | _: Upstream4xxResponse | _: Upstream5xxResponse) =>
        logger.warn("Couldn't get answers from native-app-widget service", e)
        None
    }

  private def answersUrl(campaignId: String, questionKey: String) =
    new URL(
      baseUrl,
      encodePathSegments("native-app-widget", "widget-data", campaignId, questionKey)
    )

}