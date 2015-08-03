### Best blast hit and LCA results in the same configuration

The idea is to use two assignment paradigm in the same time. That means that metapasta will produce two sets of results, fate logs etc. It is not so easy to implement and adds complexities, but without this feature, user have to run metapasta twice to get lca and bbh results that means that mapping part will be executed also twice. 


#### Producing two sets of results on queues level
It possible to just do some fork after mapping, and have after two autoscalig groups of assigment workers.

main disadvantage is duplication queues


#### Producing two sets of results on monoid level
Just put pairs of assignments (with best blast hit and lca) into same queue.

requires support from workers, so some instruction should be changed

##### Resusing mapping results across different runs of metapasta
Another way to do it is reuse somehow of mapping results.
