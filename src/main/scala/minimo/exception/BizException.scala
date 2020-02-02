package minimo.exception

case class BizException(code: String, desc: String) extends Throwable(s"code: $code, desc: $desc") {
  def this(codeEnum: BizCode.Value, desc: String) = {
    this(codeEnum.toString, desc)
  }
}

object BizException {
  def apply(code: BizCode.Value, desc: String): BizException = new BizException(code, desc)
}

object BizCode extends Enumeration {
  //按正常流程，不应该出现的业务逻辑出现了。很可能是代码bug或者用户没有通过正常的方式发起请求
  val SYSTEM_ERROR = Value("BIZ_SYSTEM_ERROR")

  //期望的结果没有匹配，后续步骤无法进行，但是这种情况在正常的操作流程中是可以出现的。比如并发请求。
  val LOGIC_FAIL = Value("BIZ_LOGIC_FAIL")

}