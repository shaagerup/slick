package slick.test.cats
import slick.basic.BasicBackend

import slick.jdbc.H2Profile.api._

import slick.cats._
import org.scalatest._

class DBIOEffectSpec extends FlatSpec with Matchers {
    import scala.concurrent.ExecutionContext.Implicits.global
    val dbioEffect = new DBIOEffect(Database.forURL("jdbc:h2:mem:test1;DB_CLOSE_DELAY=-1",driver="org.h2.Driver"))
    
    dbioEffect.suspend{
        assert(false)    
        DBIO.successful(())
    }
}