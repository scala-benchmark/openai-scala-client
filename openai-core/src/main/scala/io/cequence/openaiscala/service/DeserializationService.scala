package io.cequence.openaiscala.service

import akka.actor.ActorSystem
import akka.serialization.SerializationExtension
import scala.util.Try

object DeserializationService {

  def deserializeConfig[T](system: ActorSystem, bytes: Array[Byte], clazz: Class[T]): Try[T] = {
    //CWE-502
    //SINK
    SerializationExtension(system).deserialize(bytes, clazz)
  }
}
