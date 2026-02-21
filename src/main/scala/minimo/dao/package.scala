package minimo

import io.getquill._
import minimo.util.ObjectId

package object dao {
  val mockMode: Boolean = sys.env.getOrElse("MINIMO_MOCK", "false") == "true"

  lazy val ctx = new PostgresJdbcContext(
    NamingStrategy(SnakeCase, PostgresEscape),
    MinimoConfig.quill
  )

  val UserDao: UserDaoLike = if (mockMode) UserDaoMock else UserDaoDb

//  extends field type
  implicit val encodeObjectId: MappedEncoding[ObjectId, String] = MappedEncoding[ObjectId, String](_.toString)
  implicit val decodeObjectId: MappedEncoding[String, ObjectId] = MappedEncoding[String, ObjectId](new ObjectId(_))

}
