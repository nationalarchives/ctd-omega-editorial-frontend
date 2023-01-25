package uk.gov.nationalarchives.omega.editorial.services

import cats.effect.IO
import cats.effect.std.Queue
import uk.gov.nationalarchives.omega.editorial.EditorialMain
import uk.gov.nationalarchives.omega.jms.JmsRRClient.ReplyMessageHandler
import uk.gov.nationalarchives.omega.jms.RequestMessage

import javax.inject.Inject

class MessageService @Inject()(editorialMain: EditorialMain) {

  def getEditSet(id: String): IO[String] = {
    val requestQueue = "request-general"
    Queue.bounded[IO,String](1).flatMap(editSetQueue => {
      val replyHandler: ReplyMessageHandler[IO] = replyMessage => editSetQueue.offer(replyMessage.body)
      editorialMain.jmsRrClient.request(requestQueue, RequestMessage(s"hello $id"), replyHandler).flatMap { _ =>
        editSetQueue.take.map(item => item)
      }
    })
  }

}
