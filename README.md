# hbase-coprocessor-example

Copied from [HBase 1.2 official wiki](http://hbase.apache.org/1.2/book.html#cp)

Example only shows Observer Coprocessors.

## Types of Observer Coprocessor

- RegionObserver.
A RegionObserver coprocessor allows you to observe events on a region, such as Get and Put operations. See RegionObserver. Consider overriding the convenience class BaseRegionObserver, which implements the RegionObserver interface and will not break if new methods are added.

- RegionServerObserver.
A RegionServerObserver allows you to observe events related to the RegionServer’s operation, such as starting, stopping, or performing merges, commits, or rollbacks. See RegionServerObserver. Consider overriding the convenience class BaseMasterAndRegionObserver which implements both MasterObserver and RegionServerObserver interfaces and will not break if new methods are added.

- MasterOvserver.
A MasterObserver allows you to observe events related to the HBase Master, such as table creation, deletion, or schema modification. See MasterObserver. Consider overriding the convenience class BaseMasterAndRegionObserver, which implements both MasterObserver and RegionServerObserver interfaces and will not break if new methods are added.

- WalObserver.
A WalObserver allows you to observe events related to writes to the Write-Ahead Log (WAL). See WALObserver. Consider overriding the convenience class BaseWALObserver, which implements the WalObserver interface and will not break if new methods are added.

## Loading Coprocessors
###Static Loading
Follow these steps to statically load your coprocessor. Keep in mind that you must restart HBase to unload a coprocessor that has been loaded statically.

Define the Coprocessor in hbase-site.xml, with a <property> element with a <name> and a <value> sub-element. The <name> should be one of the following:

`hbase.coprocessor.region.classes` for RegionObservers and Endpoints.

`hbase.coprocessor.wal.classes` for WALObservers.

`hbase.coprocessor.master.classes` for MasterObservers.

`<value>` must contain the fully-qualified class name of your coprocessor’s implementation class.

For example to load a Coprocessor (implemented in class SumEndPoint.java) you have to create following entry in RegionServer’s 'hbase-site.xml' file (generally located under 'conf' directory):
```
<property>
    <name>hbase.coprocessor.region.classes</name>
    <value>org.myname.hbase.coprocessor.endpoint.SumEndPoint</value>
</property>
```

My example is:
```
<property>
    <name>hbase.coprocessor.region.classes</name>
    <value>com.neoremind.hbase.coprocessor.example.SimplePrintRegionObserver</value>
</property>
```

If multiple classes are specified for loading, the class names must be comma-separated. The framework attempts to load all the configured classes using the default class loader. Therefore, the jar file must reside on the server-side HBase classpath.

### Static Unloading

1. Delete the coprocessor’s <property> element, including sub-elements, from hbase-site.xml.

2. Restart HBase.

3. Optionally, remove the coprocessor’s JAR file from the classpath or HBase’s lib/ directory.

### Dynamic Loading

Using HBase Shell

1. Disable the table using HBase Shell:
```
hbase> disable 'users'
```

2. Load the Coprocessor, using a command like the following:
```
hbase alter 'users', METHOD => 'table_att', 'Coprocessor'=>'hdfs://<namenode>:<port>/
user/<hadoop-user>/coprocessor.jar| org.myname.hbase.Coprocessor.RegionObserverExample|1073741823|
arg1=1,arg2=2'
```

My example is:
First put jar to HDFS by executing:
```
./hdfs dfs -put /Users/xu.zhang/IdeaProjects/hbase-coprocessor-example/target/hbase-coprocessor-example.jar /user/xu.zhang/
```

Then run
```
hbase alter 'mytest', METHOD => 'table_att', 'Coprocessor'=>'hdfs://localhost:9000/user/xu.zhang/hbase-coprocessor-example.jar| com.neoremind.hbase.coprocessor.example.AdminCheckRegionObserver|1073741823|'
```

And through API call you row=admin you should get
```
[INFO]	2017-10-11 15:55:56,408	[main]	mrsample.hbase.HBaseDAO	(HBaseDAO.java:187)	-RowKey=admin, Column=details.Admin_det, TS=1507708556403, Value=You can't see Admin details
```

Note that static loading jars will not appear in TABLE_ATTRIBUTES section.
```
hbase(main):003:0> describe 'mytest'
Table mytest is ENABLED
mytest, {TABLE_ATTRIBUTES => {coprocessor$1 => 'hdfs://localhost:9000/user/xu.zhang/hbase-coprocessor-example.jar| com
.neoremind.hbase.coprocessor.example.AdminCheckRegionObserver|1073741823|'}
COLUMN FAMILIES DESCRIPTION
{NAME => 'cf1', BLOOMFILTER => 'ROW', VERSIONS => '100', IN_MEMORY => 'false', KEEP_DELETED_CELLS => 'FALSE', DATA_BLO
CK_ENCODING => 'NONE', TTL => 'FOREVER', COMPRESSION => 'NONE', MIN_VERSIONS => '0', BLOCKCACHE => 'true', BLOCKSIZE =
> '65536', REPLICATION_SCOPE => '0'}
{NAME => 'cf2', BLOOMFILTER => 'ROW', VERSIONS => '100', IN_MEMORY => 'false', KEEP_DELETED_CELLS => 'FALSE', DATA_BLO
CK_ENCODING => 'NONE', TTL => 'FOREVER', COMPRESSION => 'NONE', MIN_VERSIONS => '0', BLOCKCACHE => 'true', BLOCKSIZE =
> '65536', REPLICATION_SCOPE => '0'}
```

The Coprocessor framework will try to read the class information from the coprocessor table attribute value. The value contains four pieces of information which are separated by the pipe (|) character.

- File path: The jar file containing the Coprocessor implementation must be in a location where all region servers can read it.
You could copy the file onto the local disk on each region server, but it is recommended to store it in HDFS.

- Class name: The full class name of the Coprocessor.

- Priority: An integer. The framework will determine the execution sequence of all configured observers registered at the same hook using priorities. This field can be left blank. In that case the framework will assign a default priority value.

- Arguments (Optional): This field is passed to the Coprocessor implementation. This is optional.

3. Enable the table.
```
hbase(main):003:0> enable 'users'
```

Verify that the coprocessor loaded:

```
hbase(main):04:0> describe 'users'
```

The coprocessor should be listed in the TABLE_ATTRIBUTES.

### Dynamic Unloading

Disable the table.
```
hbase> disable 'users'
```

Alter the table to remove the coprocessor.

```
hbase> alter 'users', METHOD => 'table_att_unset', NAME => 'coprocessor$1'
```

Enable the table.
```
hbase> enable 'users'
```