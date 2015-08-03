package ohnosequences.metapasta.databases

import ohnosequences.metapasta.Factory
import ohnosequences.awstools.s3.LoadingManager


trait DatabaseFactory[+T] extends Factory[LoadingManager, T] {
}
