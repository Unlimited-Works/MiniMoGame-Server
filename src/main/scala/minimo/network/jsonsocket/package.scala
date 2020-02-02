package minimo.network

import minimo.util.JsonFormat
import org.json4s.{DefaultFormats, Formats}

package object jsonsocket {
  implicit val formats: Formats = JsonFormat.minimoFormats

}
