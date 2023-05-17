import com.google.inject.AbstractModule
import uk.gov.nationalarchives.omega.editorial.StubServerBootstrap

class TestModule extends AbstractModule {

  override def configure(): Unit =
    bind(classOf[StubServerBootstrap]).asEagerSingleton()

}
