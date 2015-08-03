## Metapasta usage


### Installation

#### Compota CLI

Install [Compota CLI](https://github.com/ohnosequences/compotaCLI), the tool for configuring AWS account and creating Compota based projects from templates. 

##### AWS account preparation

Compota CLI needs AWS account credentials there are several ways to specify them:

###### 1. Property file

```bash
accessKey = <access_key>
#for example: accessKey = AKIAIG23IDH2AEPBEFVE

secretKey = <secret_key>
#for example: secretKey = AZpGhgq6i4+m+TRXJ0W8nYmRJY3eqr5p5DQULTci
```

By default Compota CLI looks for path `~/compota.credentials`, but with path can be replaced and specified as a last argument of Compota CLI.


###### 2. Environment variables

* `AWS_ACCESS_KEY`
* `AWS_SECRET_ACCESS_KEY`

###### 3. Instance profile credentials
You can execute `compota configure` from any EC2 instance with proper IAM role without any credentials configuration.

##### `configure` command

After you configured you credentials, execute following command in order to configure your AWS account:

```compota configure```

#### SBT

Install [SBT](http://www.scala-sbt.org/) with version higher than `0.13.0`.


#### Metapasta AWS credentials

Metapasta needs AWS credentials. They ways to specify them are exactly the same as for Compota CLI except default path for property file - `~/matapasta.credentials`.

### Usage

### Creating template

To download a template with metapasta project type:

```
compota create ohnosequences/metapasta.g8
```

### Configuration

After downloading the template, all metapasta parameters can be configured.

#### `samples`
Reads should be either single or paired ended with intersection. Example for an mock community:

```scala
object mockSamples {
  val testBucket = "metapasta-test"

  val ss1 = "supermock3"
  val s1 = PairedSample(ss1, ObjectAddress(testBucket, "mock/" + ss1 + ".fastq"), ObjectAddress(testBucket, "mock/" + ss1 + ".fastq"))

  val samples = List(s1)
  
  val t1 = SampleTag("t1")
  val tagging = Map(s1 -> List(t1))
}
```

> FASTQ files should have solexa quality format.


#### `mappingWorkers` 
configuration of auto scaling group with mapping workers. It is recommended to use `c1_medium` for BLAST and `m1_large` for LAST. In general BLAST performs mapping quite slow, so for it you will need hundred of mapping workers. Example:

```scala
mappingWorkers = Group(size = 1, max = 100, instanceType = InstanceType.c1_medium, purchaseModel = SpotAuto)
```

LAST requires not so a lot instances but they should have at least 3GB RAM (for nt.16S database).

##### `keyName`
Name of ssh key that can be used for connecting to instances.

##### `timeout`
Global timeout in seconds for metapasta

##### `database`
Index of reference database. Metabasta bundled with nt.16s database, but other databased can be implemented (see https://github.com/ohnosequences/metapasta/blob/master/src/main/scala/ohnosequences/metapasta/Database.scala).


##### other parameters
https://github.com/ohnosequences/metapasta/blob/master/src/main/scala/ohnosequences/metapasta/MetapastaConfiguration.scala



### Metapasta command 

##### Publish
Publish project to S3 to make it accessible form all Metapasta components, should be executed before launching
```
sbt publish
```


##### Launch

Launchs metapasta: creates metamanager.

```
sbt "run run"
```


#### Undeploy

*metapasta* will automatically when all assignments work will finish. 
Although it is possible to do send mannualy undeploy command to metamanager.

```
sbt "run undeploy"
```

or undeploy it form local machine:

```
sbt "run undeploy force"
```

##### Add tasks

```
sbt "run add tasks"
```

##### Change size of the group

```
sbt "run map size <number>"
```
