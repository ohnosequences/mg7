### Groups of samples
With fixed set of sample tags (e.g. "wood sample", "1 week sample") groups of samples can be defined.
Indeed every tag defines two groups: samples tagged with with tag and rest samples.

#### How to define tags?

##### In configuration.scala
Every reasonable Scala code should work:

```Scala
//definition of samples:
...

//definition of tags, for example:
sample1 withTags List(tag1, tag2)
sample2 withTags List(tag2, tag3)
sample3 withTags List(tag4)
```

##### Tags description file
it is possible to store this information in JSON for example

```json
{
  "sample1": ["tag1", "tag2"],
  "sample2": ["tag2", "tag3"],
  "sample3": ["tag4"]
}
```

##### Questions

* Do we need really this not Scala representation? Because it would lead only to problems with invalid formats of configuration.
* Do we need only this basic way for defining groups? probably in some cases it would better 
to have opportunity to define more specific groups (e.g. tag1 && !tag2). My idea here is to use predicate like: ```tags(sample) => Boolean```
