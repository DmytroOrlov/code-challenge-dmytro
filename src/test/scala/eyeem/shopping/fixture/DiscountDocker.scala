package eyeem.shopping.fixture

import izumi.distage.docker.ContainerDef
import izumi.distage.docker.Docker.{ContainerConfig, DockerPort}

object DiscountDocker extends ContainerDef {
  val primaryPort: DockerPort = DockerPort.TCP(9000)

  override def config: Config = {
    ContainerConfig(
      image = "eyeem/discounter:0.0.1",
      ports = Seq(primaryPort),
    )
  }
}

import izumi.distage.docker.Docker.AvailablePort
import izumi.distage.docker.modules.DockerSupportModule
import izumi.distage.model.definition.Id
import izumi.distage.model.definition.StandardAxis.Env
import izumi.distage.plugins.PluginDef
import zio.Task

class DiscountDockerSvc(val es: AvailablePort@Id("discount"))

object ElasticPlugin extends DockerSupportModule[Task] with PluginDef {
  make[DiscountDocker.Container].fromResource {
    DiscountDocker.make[Task]
  }

  make[AvailablePort].named("discount").tagged(Env.Test).from {
    dn: DiscountDocker.Container =>
      dn.availablePorts.first(DiscountDocker.primaryPort)
  }

  make[DiscountDockerSvc]
}
