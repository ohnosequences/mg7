### what is compota?
compota is a Scala library for performing computations in Amazon cloud (AWS)

### how to put computations in compota?
Just present your computation as morphism of monoids

### why monoids?
Because they allow to express parallel execution of computation f with input X and output Y:

$$ x \mapsto x_1, x_2, ... x_n  = split_X(x) \mapsto f(x_1), f(x_2), ... f(x_n) \mapsto merge_Y(f(x_1), f(x_2), ... f(x_n)) = f(x)$$

to have identity here it's enough to assume that $X$ and $Y$ are [monoids](http://en.wikipedia.org/wiki/Monoid), $f$ is monoid morphism, $merge_Y$ monoid law in $Y$, 
and $split_Y$ is map from $X$ to $X^{*}$ ([free monoid](http://en.wikipedia.org/wiki/Free_monoid) on $X$, words on $X$, can be represent as `List[X]`), that satisfies property $merge_X \circ split_X = id$.


### an example?

The typical word count can be expressed as a morphism from `List[String]` monoid to `Map[String, Int]` (with any reasonable split).

Example of computation:

$$ List(t_1, t_3, t_2, t_2, t_1) \mapsto List((t_1, t_3, t_2), List(t_2, t_1)) \mapsto
List(Map(t_1 -> 1, t_3 -> 1, t_2 -> 1), Map(t_2 -> 1, t_1 -> 1)) \mapsto 
Map(t_1 -> 2, t_2 -> 2, t_3 -> 1) $$

### can I compose two computations?

yes it will be just a compositions of monoid morphisms

$$X \xleftarrow{f} Y \to \xleftarrow{g} Z$$

### how to express monoids in compota?

There is a simple trait for it:

```Scala
trait Monoid[M] {
  def unit: M
  def mult(x: M, y: M): M
}
```

see example [here](https://github.com/ohnosequences/compota/blob/master/src/main/scala/ohnosequences/nisperon/Monoid.scala)


### how to represent monoid morphism

```scala
trait InstructionsAux {

  type I //input

  type O //output

  def prepare(): Context // method for preparing context (installing software etc..) that will be called before running computations

  type Context 

  def solve(input: I, logger: S3Logger, context: Context): List[O] // computations

}
```

### how compota will execute it?

For every instructions will create two [auto scaling groups](http://docs.aws.amazon.com/AutoScaling/latest/DeveloperGuide/WorkingWithASG.html):

* **workers group** -- instances that will execute `solve` method
* **manager group** -- groups with only one instance that will perform resource management work

### how input data will be passed to worker instances?

Every input monoid in compota is linked with [SQS queue](http://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/Welcome.html), after reading input (element of input monoid) from the queue worker perform computations and then publish results (element of output monoid) to output queues.



