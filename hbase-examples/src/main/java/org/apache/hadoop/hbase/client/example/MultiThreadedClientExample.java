begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|client
operator|.
name|example
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ThreadFactoryBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|conf
operator|.
name|Configured
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|TableName
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|client
operator|.
name|Connection
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|client
operator|.
name|ConnectionFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|client
operator|.
name|Put
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|client
operator|.
name|RegionLocator
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|client
operator|.
name|Result
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|client
operator|.
name|ResultScanner
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|client
operator|.
name|Scan
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|client
operator|.
name|Table
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|filter
operator|.
name|KeyOnlyFilter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|Bytes
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|Tool
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|ToolRunner
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Callable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ExecutorService
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Executors
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ForkJoinPool
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Future
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ThreadFactory
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ThreadLocalRandom
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
import|;
end_import

begin_comment
comment|/**  * Example on how to use HBase's {@link Connection} and {@link Table} in a  * multi-threaded environment. Each table is a light weight object  * that is created and thrown away. Connections are heavy weight objects  * that hold on to zookeeper connections, async processes, and other state.  *  *<pre>  * Usage:  * bin/hbase org.apache.hadoop.hbase.client.example.MultiThreadedClientExample testTableName 500000  *</pre>  *  *<p>  * The table should already be created before running the command.  * This example expects one column family named d.  *</p>  *<p>  * This is meant to show different operations that are likely to be  * done in a real world application. These operations are:  *</p>  *  *<ul>  *<li>  *     30% of all operations performed are batch writes.  *     30 puts are created and sent out at a time.  *     The response for all puts is waited on.  *</li>  *<li>  *     20% of all operations are single writes.  *     A single put is sent out and the response is waited for.  *</li>  *<li>  *     50% of all operations are scans.  *     These scans start at a random place and scan up to 100 rows.  *</li>  *</ul>  *  */
end_comment

begin_class
specifier|public
class|class
name|MultiThreadedClientExample
extends|extends
name|Configured
implements|implements
name|Tool
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|MultiThreadedClientExample
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|DEFAULT_NUM_OPERATIONS
init|=
literal|500000
decl_stmt|;
comment|/**    * The name of the column family.    *    * d for default.    */
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"d"
argument_list|)
decl_stmt|;
comment|/**    * For the example we're just using one qualifier.    */
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|QUAL
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|ExecutorService
name|internalPool
decl_stmt|;
specifier|private
specifier|final
name|int
name|threads
decl_stmt|;
specifier|public
name|MultiThreadedClientExample
parameter_list|()
throws|throws
name|IOException
block|{
comment|// Base number of threads.
comment|// This represents the number of threads you application has
comment|// that can be interacting with an hbase client.
name|this
operator|.
name|threads
operator|=
name|Runtime
operator|.
name|getRuntime
argument_list|()
operator|.
name|availableProcessors
argument_list|()
operator|*
literal|4
expr_stmt|;
comment|// Daemon threads are great for things that get shut down.
name|ThreadFactory
name|threadFactory
init|=
operator|new
name|ThreadFactoryBuilder
argument_list|()
operator|.
name|setDaemon
argument_list|(
literal|true
argument_list|)
operator|.
name|setNameFormat
argument_list|(
literal|"internal-pol-%d"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|this
operator|.
name|internalPool
operator|=
name|Executors
operator|.
name|newFixedThreadPool
argument_list|(
name|threads
argument_list|,
name|threadFactory
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|run
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|args
operator|.
name|length
argument_list|<
literal|1
operator|||
name|args
operator|.
name|length
argument_list|>
literal|2
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Usage: "
operator|+
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|" tableName [num_operations]"
argument_list|)
expr_stmt|;
return|return
operator|-
literal|1
return|;
block|}
specifier|final
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|args
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
name|int
name|numOperations
init|=
name|DEFAULT_NUM_OPERATIONS
decl_stmt|;
comment|// the second arg is the number of operations to send.
if|if
condition|(
name|args
operator|.
name|length
operator|==
literal|2
condition|)
block|{
name|numOperations
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|args
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
comment|// Threads for the client only.
comment|//
comment|// We don't want to mix hbase and business logic.
comment|//
name|ExecutorService
name|service
init|=
operator|new
name|ForkJoinPool
argument_list|(
name|threads
operator|*
literal|2
argument_list|)
decl_stmt|;
comment|// Create two different connections showing how it's possible to
comment|// separate different types of requests onto different connections
specifier|final
name|Connection
name|writeConnection
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|getConf
argument_list|()
argument_list|,
name|service
argument_list|)
decl_stmt|;
specifier|final
name|Connection
name|readConnection
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|getConf
argument_list|()
argument_list|,
name|service
argument_list|)
decl_stmt|;
comment|// At this point the entire cache for the region locations is full.
comment|// Only do this if the number of regions in a table is easy to fit into memory.
comment|//
comment|// If you are interacting with more than 25k regions on a client then it's probably not good
comment|// to do this at all.
name|warmUpConnectionCache
argument_list|(
name|readConnection
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
name|warmUpConnectionCache
argument_list|(
name|writeConnection
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Future
argument_list|<
name|Boolean
argument_list|>
argument_list|>
name|futures
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|numOperations
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numOperations
condition|;
name|i
operator|++
control|)
block|{
name|double
name|r
init|=
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextDouble
argument_list|()
decl_stmt|;
name|Future
argument_list|<
name|Boolean
argument_list|>
name|f
decl_stmt|;
comment|// For the sake of generating some synthetic load this queues
comment|// some different callables.
comment|// These callables are meant to represent real work done by your application.
if|if
condition|(
name|r
operator|<
literal|.30
condition|)
block|{
name|f
operator|=
name|internalPool
operator|.
name|submit
argument_list|(
operator|new
name|WriteExampleCallable
argument_list|(
name|writeConnection
argument_list|,
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|r
operator|<
literal|.50
condition|)
block|{
name|f
operator|=
name|internalPool
operator|.
name|submit
argument_list|(
operator|new
name|SingleWriteExampleCallable
argument_list|(
name|writeConnection
argument_list|,
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|f
operator|=
name|internalPool
operator|.
name|submit
argument_list|(
operator|new
name|ReadExampleCallable
argument_list|(
name|writeConnection
argument_list|,
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|futures
operator|.
name|add
argument_list|(
name|f
argument_list|)
expr_stmt|;
block|}
comment|// Wait a long time for all the reads/writes to complete
for|for
control|(
name|Future
argument_list|<
name|Boolean
argument_list|>
name|f
range|:
name|futures
control|)
block|{
name|f
operator|.
name|get
argument_list|(
literal|10
argument_list|,
name|TimeUnit
operator|.
name|MINUTES
argument_list|)
expr_stmt|;
block|}
comment|// Clean up after our selves for cleanliness
name|internalPool
operator|.
name|shutdownNow
argument_list|()
expr_stmt|;
name|service
operator|.
name|shutdownNow
argument_list|()
expr_stmt|;
return|return
literal|0
return|;
block|}
specifier|private
name|void
name|warmUpConnectionCache
parameter_list|(
name|Connection
name|connection
parameter_list|,
name|TableName
name|tn
parameter_list|)
throws|throws
name|IOException
block|{
try|try
init|(
name|RegionLocator
name|locator
init|=
name|connection
operator|.
name|getRegionLocator
argument_list|(
name|tn
argument_list|)
init|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Warmed up region location cache for "
operator|+
name|tn
operator|+
literal|" got "
operator|+
name|locator
operator|.
name|getAllRegionLocations
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Class that will show how to send batches of puts at the same time.    */
specifier|public
specifier|static
class|class
name|WriteExampleCallable
implements|implements
name|Callable
argument_list|<
name|Boolean
argument_list|>
block|{
specifier|private
specifier|final
name|Connection
name|connection
decl_stmt|;
specifier|private
specifier|final
name|TableName
name|tableName
decl_stmt|;
specifier|public
name|WriteExampleCallable
parameter_list|(
name|Connection
name|connection
parameter_list|,
name|TableName
name|tableName
parameter_list|)
block|{
name|this
operator|.
name|connection
operator|=
name|connection
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Boolean
name|call
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Table implements Closable so we use the try with resource structure here.
comment|// https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html
try|try
init|(
name|Table
name|t
init|=
name|connection
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
init|)
block|{
name|byte
index|[]
name|value
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|Double
operator|.
name|toString
argument_list|(
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextDouble
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|int
name|rows
init|=
literal|30
decl_stmt|;
comment|// Array to put the batch
name|ArrayList
argument_list|<
name|Put
argument_list|>
name|puts
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|rows
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|30
condition|;
name|i
operator|++
control|)
block|{
name|byte
index|[]
name|rk
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextLong
argument_list|()
argument_list|)
decl_stmt|;
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|rk
argument_list|)
decl_stmt|;
name|p
operator|.
name|addImmutable
argument_list|(
name|FAMILY
argument_list|,
name|QUAL
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|puts
operator|.
name|add
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
comment|// now that we've assembled the batch it's time to push it to hbase.
name|t
operator|.
name|put
argument_list|(
name|puts
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
block|}
comment|/**    * Class to show how to send a single put.    */
specifier|public
specifier|static
class|class
name|SingleWriteExampleCallable
implements|implements
name|Callable
argument_list|<
name|Boolean
argument_list|>
block|{
specifier|private
specifier|final
name|Connection
name|connection
decl_stmt|;
specifier|private
specifier|final
name|TableName
name|tableName
decl_stmt|;
specifier|public
name|SingleWriteExampleCallable
parameter_list|(
name|Connection
name|connection
parameter_list|,
name|TableName
name|tableName
parameter_list|)
block|{
name|this
operator|.
name|connection
operator|=
name|connection
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Boolean
name|call
parameter_list|()
throws|throws
name|Exception
block|{
try|try
init|(
name|Table
name|t
init|=
name|connection
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
init|)
block|{
name|byte
index|[]
name|value
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|Double
operator|.
name|toString
argument_list|(
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextDouble
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|byte
index|[]
name|rk
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextLong
argument_list|()
argument_list|)
decl_stmt|;
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|rk
argument_list|)
decl_stmt|;
name|p
operator|.
name|addImmutable
argument_list|(
name|FAMILY
argument_list|,
name|QUAL
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|t
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
block|}
comment|/**    * Class to show how to scan some rows starting at a random location.    */
specifier|public
specifier|static
class|class
name|ReadExampleCallable
implements|implements
name|Callable
argument_list|<
name|Boolean
argument_list|>
block|{
specifier|private
specifier|final
name|Connection
name|connection
decl_stmt|;
specifier|private
specifier|final
name|TableName
name|tableName
decl_stmt|;
specifier|public
name|ReadExampleCallable
parameter_list|(
name|Connection
name|connection
parameter_list|,
name|TableName
name|tableName
parameter_list|)
block|{
name|this
operator|.
name|connection
operator|=
name|connection
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Boolean
name|call
parameter_list|()
throws|throws
name|Exception
block|{
comment|// total length in bytes of all read rows.
name|int
name|result
init|=
literal|0
decl_stmt|;
comment|// Number of rows the scan will read before being considered done.
name|int
name|toRead
init|=
literal|100
decl_stmt|;
try|try
init|(
name|Table
name|t
init|=
name|connection
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
init|)
block|{
name|byte
index|[]
name|rk
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextLong
argument_list|()
argument_list|)
decl_stmt|;
name|Scan
name|s
init|=
operator|new
name|Scan
argument_list|(
name|rk
argument_list|)
decl_stmt|;
comment|// This filter will keep the values from being sent accross the wire.
comment|// This is good for counting or other scans that are checking for
comment|// existence and don't rely on the value.
name|s
operator|.
name|setFilter
argument_list|(
operator|new
name|KeyOnlyFilter
argument_list|()
argument_list|)
expr_stmt|;
comment|// Don't go back to the server for every single row.
comment|// We know these rows are small. So ask for 20 at a time.
comment|// This would be application specific.
comment|//
comment|// The goal is to reduce round trips but asking for too
comment|// many rows can lead to GC problems on client and server sides.
name|s
operator|.
name|setCaching
argument_list|(
literal|20
argument_list|)
expr_stmt|;
comment|// Don't use the cache. While this is a silly test program it's still good to be
comment|// explicit that scans normally don't use the block cache.
name|s
operator|.
name|setCacheBlocks
argument_list|(
literal|false
argument_list|)
expr_stmt|;
comment|// Open up the scanner and close it automatically when done.
try|try
init|(
name|ResultScanner
name|rs
init|=
name|t
operator|.
name|getScanner
argument_list|(
name|s
argument_list|)
init|)
block|{
comment|// Now go through rows.
for|for
control|(
name|Result
name|r
range|:
name|rs
control|)
block|{
comment|// Keep track of things size to simulate doing some real work.
name|result
operator|+=
name|r
operator|.
name|getRow
argument_list|()
operator|.
name|length
expr_stmt|;
name|toRead
operator|-=
literal|1
expr_stmt|;
comment|// Most online applications won't be
comment|// reading the entire table so this break
comment|// simulates small to medium size scans,
comment|// without needing to know an end row.
if|if
condition|(
name|toRead
operator|<=
literal|0
condition|)
block|{
break|break;
block|}
block|}
block|}
block|}
return|return
name|result
operator|>
literal|0
return|;
block|}
block|}
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
name|ToolRunner
operator|.
name|run
argument_list|(
operator|new
name|MultiThreadedClientExample
argument_list|()
argument_list|,
name|args
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

