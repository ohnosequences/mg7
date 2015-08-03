package ohnosequences.nisperon


object Naming {
  def name(nisperon: NisperonConfiguration, nispero: NisperoConfiguration, suffix: String): String = {
    "nispero_" + nisperon.id  + "_" + nispero.name + "_" + suffix
  }

  def name(nisperon: NisperonConfiguration, suffix: String): String = {
    "nispero_" + nisperon.id  + "_" + suffix
  }

  def s3name(nisperon: NisperonConfiguration, suffix: String): String = {
    ("nispero-" + nisperon.id + "-" + suffix).replace("_", "-").toLowerCase
  }

  def s3name(nisperon: NisperonConfiguration): String = {
    "nispero-" + nisperon.id.replace("_", "-").toLowerCase
  }

  def notificationTopic(nisperon: NisperonConfiguration): String = {
    "nispero_" + nisperon.email.hashCode
  }

 }
