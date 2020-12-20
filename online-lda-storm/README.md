### Storm Trident topology for Online LDA Algorithm

Forked from [siwS/online-lda-storm](https://github.com/siwS/online-lda-storm)

## Original Author README
#### Description 

This project is part of my thesis during my postgraduate studies at National Technical University of Athens.

The purpose of this project was to implement real-time topic detection on Twitter documents using Apache Storm and the Latent Dirichlet Allocation (LDA) algorithm.

#### Technologies

This project uses [Apache Storm](http://storm.apache.org/index.html) to do real-time processing of input data. Input is polled by Twitter public stream API.
Specifically, it uses the [Trident](http://storm.apache.org/releases/current/Trident-tutorial.html) abstraction of Storm that allows to do
elegant stream processing, state manipulation and low latency querying.
Redis Server is used for persisting the state.

#### LDA Algorithm

In order to do real-time processing, I used the Online version of the LDA Algorithm. Typical LDA Algorithm processes the documents iteratively in batches.
The [Online LDA](http://papers.nips.cc/paper/3902-online-learning-for-latent-dirichlet-allocation.pdf) algorithm only processes each document once, making it suitable for real-time processing.

I used the library [JOLDA](https://github.com/miberk/jolda) for executing the algorithm.

#### Run locally

The project is built using Apache Maven so you can just download and execute with maven. 
It depends on the JOLDA library, so you have to check it out and build it with Maven as well.
You have to install redis server locally, i.e. run `sudo apt-get install redis-server` for the topology to run.
Finally you have to configure the necessary properties in config file.

#### Run on a Storm cluster

In order to run on a cluster, you will need to setup a [Storm cluster](http://storm.apache.org/releases/2.0.0-SNAPSHOT/Setting-up-a-Storm-cluster.html).
You also need to set up a Redis server and configure the necessary properties in config file. To submit the topology you should run `storm jar filename.jar mainClass topologyName` in Nimbus machine.

#### Topology

This Storm Trident topology:

- is feeded with data from public Twitter stream using TwitterSampleSpout in batches
- pre-processes the input data (stemming, removing stop words)
- joins the tweets in a big list of tweets 
- applies the Online LDA algorithm in each batch

My whole thesis is [here](http://dspace.lib.ntua.gr/bitstream/handle/123456789/41875/thesis_tzima_sofia_03108052.pdf?sequence=1). Unfortunately it is written in Greek :P
