/*
 * Copyright (c) 2022 The National Archives
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package uk.gov.nationalarchives.omega.editorial.connectors

import cats.effect.kernel.Sync

import java.util.UUID

private trait RandomClientIdGen[F[_]] {

  /** Generates a ClientId pseudo-random manner.
    * @return
    *   randomly generated ClientId
    */
  def randomClientId: F[String]
}

private object RandomClientIdGen {
  def apply[F[_]](implicit randomClientIdGen: RandomClientIdGen[F]): RandomClientIdGen[F] = randomClientIdGen

  def randomClientId[F[_] : RandomClientIdGen]: F[String] = RandomClientIdGen[F].randomClientId

  implicit def fromSync[F[_]](implicit sync: Sync[F]): RandomClientIdGen[F] = new RandomClientIdGen[F] {
    override final val randomClientId: F[String] =
      sync.map(sync.blocking(UUID.randomUUID()))(uuid => s"jms-rr-client-$uuid")
  }
}
