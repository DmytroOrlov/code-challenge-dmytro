package eyeem.shopping.fixture

import izumi.distage.plugins.PluginDef
import zio.random.Random

object FixturePlugin extends PluginDef {
  make[Random.Service].fromHas(Random.live)
}
