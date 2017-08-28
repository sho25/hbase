begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
package|;
end_package

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
name|Locale
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
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
name|cli
operator|.
name|CommandLine
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
name|lang
operator|.
name|StringUtils
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
name|hbase
operator|.
name|classification
operator|.
name|InterfaceAudience
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
name|Configuration
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
name|regionserver
operator|.
name|DisabledRegionSplitPolicy
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
name|regionserver
operator|.
name|HStore
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
name|regionserver
operator|.
name|StoreEngine
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
name|regionserver
operator|.
name|StripeStoreConfig
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
name|regionserver
operator|.
name|StripeStoreEngine
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
name|AbstractHBaseTool
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
name|hbase
operator|.
name|util
operator|.
name|MultiThreadedAction
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
name|MultiThreadedReader
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
name|MultiThreadedWriter
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
name|RegionSplitter
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
name|test
operator|.
name|LoadTestDataGenerator
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
name|test
operator|.
name|LoadTestKVGenerator
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Assert
import|;
end_import

begin_comment
comment|/**  * A perf test which does large data ingestion using stripe compactions and regular compactions.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|StripeCompactionsPerformanceEvaluation
extends|extends
name|AbstractHBaseTool
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
name|StripeCompactionsPerformanceEvaluation
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|TableName
name|TABLE_NAME
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|StripeCompactionsPerformanceEvaluation
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|COLUMN_FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"CF"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|MIN_NUM_SERVERS
init|=
literal|1
decl_stmt|;
comment|// Option names.
specifier|private
specifier|static
specifier|final
name|String
name|DATAGEN_KEY
init|=
literal|"datagen"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|ITERATIONS_KEY
init|=
literal|"iters"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|PRELOAD_COUNT_KEY
init|=
literal|"pwk"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|WRITE_COUNT_KEY
init|=
literal|"wk"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|WRITE_THREADS_KEY
init|=
literal|"wt"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|READ_THREADS_KEY
init|=
literal|"rt"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|INITIAL_STRIPE_COUNT_KEY
init|=
literal|"initstripes"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|SPLIT_SIZE_KEY
init|=
literal|"splitsize"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|SPLIT_PARTS_KEY
init|=
literal|"splitparts"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|VALUE_SIZE_KEY
init|=
literal|"valsize"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|SEQ_SHARDS_PER_SERVER_KEY
init|=
literal|"seqshards"
decl_stmt|;
comment|// Option values.
specifier|private
name|LoadTestDataGenerator
name|dataGen
decl_stmt|;
specifier|private
name|int
name|iterationCount
decl_stmt|;
specifier|private
name|long
name|preloadKeys
decl_stmt|;
specifier|private
name|long
name|writeKeys
decl_stmt|;
specifier|private
name|int
name|writeThreads
decl_stmt|;
specifier|private
name|int
name|readThreads
decl_stmt|;
specifier|private
name|Long
name|initialStripeCount
decl_stmt|;
specifier|private
name|Long
name|splitSize
decl_stmt|;
specifier|private
name|Long
name|splitParts
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|VALUE_SIZE_DEFAULT
init|=
literal|"512:4096"
decl_stmt|;
specifier|protected
name|IntegrationTestingUtility
name|util
init|=
operator|new
name|IntegrationTestingUtility
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|void
name|addOptions
parameter_list|()
block|{
name|addOptWithArg
argument_list|(
name|DATAGEN_KEY
argument_list|,
literal|"Type of data generator to use (default or sequential)"
argument_list|)
expr_stmt|;
name|addOptWithArg
argument_list|(
name|SEQ_SHARDS_PER_SERVER_KEY
argument_list|,
literal|"Sequential generator will shard the data into many"
operator|+
literal|" sequences. The number of such shards per server is specified (default is 1)."
argument_list|)
expr_stmt|;
name|addOptWithArg
argument_list|(
name|ITERATIONS_KEY
argument_list|,
literal|"Number of iterations to run to compare"
argument_list|)
expr_stmt|;
name|addOptWithArg
argument_list|(
name|PRELOAD_COUNT_KEY
argument_list|,
literal|"Number of keys to preload, per server"
argument_list|)
expr_stmt|;
name|addOptWithArg
argument_list|(
name|WRITE_COUNT_KEY
argument_list|,
literal|"Number of keys to write, per server"
argument_list|)
expr_stmt|;
name|addOptWithArg
argument_list|(
name|WRITE_THREADS_KEY
argument_list|,
literal|"Number of threads to use for writing"
argument_list|)
expr_stmt|;
name|addOptWithArg
argument_list|(
name|READ_THREADS_KEY
argument_list|,
literal|"Number of threads to use for reading"
argument_list|)
expr_stmt|;
name|addOptWithArg
argument_list|(
name|INITIAL_STRIPE_COUNT_KEY
argument_list|,
literal|"Number of stripes to split regions into initially"
argument_list|)
expr_stmt|;
name|addOptWithArg
argument_list|(
name|SPLIT_SIZE_KEY
argument_list|,
literal|"Size at which a stripe will split into more stripes"
argument_list|)
expr_stmt|;
name|addOptWithArg
argument_list|(
name|SPLIT_PARTS_KEY
argument_list|,
literal|"Number of stripes to split a stripe into when it splits"
argument_list|)
expr_stmt|;
name|addOptWithArg
argument_list|(
name|VALUE_SIZE_KEY
argument_list|,
literal|"Value size; either a number, or a colon-separated range;"
operator|+
literal|" default "
operator|+
name|VALUE_SIZE_DEFAULT
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|processOptions
parameter_list|(
name|CommandLine
name|cmd
parameter_list|)
block|{
name|int
name|minValueSize
init|=
literal|0
decl_stmt|,
name|maxValueSize
init|=
literal|0
decl_stmt|;
name|String
name|valueSize
init|=
name|cmd
operator|.
name|getOptionValue
argument_list|(
name|VALUE_SIZE_KEY
argument_list|,
name|VALUE_SIZE_DEFAULT
argument_list|)
decl_stmt|;
if|if
condition|(
name|valueSize
operator|.
name|contains
argument_list|(
literal|":"
argument_list|)
condition|)
block|{
name|String
index|[]
name|valueSizes
init|=
name|valueSize
operator|.
name|split
argument_list|(
literal|":"
argument_list|)
decl_stmt|;
if|if
condition|(
name|valueSize
operator|.
name|length
argument_list|()
operator|!=
literal|2
condition|)
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Invalid value size: "
operator|+
name|valueSize
argument_list|)
throw|;
name|minValueSize
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|valueSizes
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|maxValueSize
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|valueSizes
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|minValueSize
operator|=
name|maxValueSize
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|valueSize
argument_list|)
expr_stmt|;
block|}
name|String
name|datagen
init|=
name|cmd
operator|.
name|getOptionValue
argument_list|(
name|DATAGEN_KEY
argument_list|,
literal|"default"
argument_list|)
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
decl_stmt|;
if|if
condition|(
literal|"default"
operator|.
name|equals
argument_list|(
name|datagen
argument_list|)
condition|)
block|{
name|dataGen
operator|=
operator|new
name|MultiThreadedAction
operator|.
name|DefaultDataGenerator
argument_list|(
name|minValueSize
argument_list|,
name|maxValueSize
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|COLUMN_FAMILY
block|}
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"sequential"
operator|.
name|equals
argument_list|(
name|datagen
argument_list|)
condition|)
block|{
name|int
name|shards
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|cmd
operator|.
name|getOptionValue
argument_list|(
name|SEQ_SHARDS_PER_SERVER_KEY
argument_list|,
literal|"1"
argument_list|)
argument_list|)
decl_stmt|;
name|dataGen
operator|=
operator|new
name|SeqShardedDataGenerator
argument_list|(
name|minValueSize
argument_list|,
name|maxValueSize
argument_list|,
name|shards
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unknown "
operator|+
name|DATAGEN_KEY
operator|+
literal|": "
operator|+
name|datagen
argument_list|)
throw|;
block|}
name|iterationCount
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|cmd
operator|.
name|getOptionValue
argument_list|(
name|ITERATIONS_KEY
argument_list|,
literal|"1"
argument_list|)
argument_list|)
expr_stmt|;
name|preloadKeys
operator|=
name|Long
operator|.
name|parseLong
argument_list|(
name|cmd
operator|.
name|getOptionValue
argument_list|(
name|PRELOAD_COUNT_KEY
argument_list|,
literal|"1000000"
argument_list|)
argument_list|)
expr_stmt|;
name|writeKeys
operator|=
name|Long
operator|.
name|parseLong
argument_list|(
name|cmd
operator|.
name|getOptionValue
argument_list|(
name|WRITE_COUNT_KEY
argument_list|,
literal|"1000000"
argument_list|)
argument_list|)
expr_stmt|;
name|writeThreads
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|cmd
operator|.
name|getOptionValue
argument_list|(
name|WRITE_THREADS_KEY
argument_list|,
literal|"10"
argument_list|)
argument_list|)
expr_stmt|;
name|readThreads
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|cmd
operator|.
name|getOptionValue
argument_list|(
name|READ_THREADS_KEY
argument_list|,
literal|"20"
argument_list|)
argument_list|)
expr_stmt|;
name|initialStripeCount
operator|=
name|getLongOrNull
argument_list|(
name|cmd
argument_list|,
name|INITIAL_STRIPE_COUNT_KEY
argument_list|)
expr_stmt|;
name|splitSize
operator|=
name|getLongOrNull
argument_list|(
name|cmd
argument_list|,
name|SPLIT_SIZE_KEY
argument_list|)
expr_stmt|;
name|splitParts
operator|=
name|getLongOrNull
argument_list|(
name|cmd
argument_list|,
name|SPLIT_PARTS_KEY
argument_list|)
expr_stmt|;
block|}
specifier|private
name|Long
name|getLongOrNull
parameter_list|(
name|CommandLine
name|cmd
parameter_list|,
name|String
name|option
parameter_list|)
block|{
if|if
condition|(
operator|!
name|cmd
operator|.
name|hasOption
argument_list|(
name|option
argument_list|)
condition|)
return|return
literal|null
return|;
return|return
name|Long
operator|.
name|parseLong
argument_list|(
name|cmd
operator|.
name|getOptionValue
argument_list|(
name|option
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Configuration
name|getConf
parameter_list|()
block|{
name|Configuration
name|c
init|=
name|super
operator|.
name|getConf
argument_list|()
decl_stmt|;
if|if
condition|(
name|c
operator|==
literal|null
operator|&&
name|util
operator|!=
literal|null
condition|)
block|{
name|conf
operator|=
name|util
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|c
operator|=
name|conf
expr_stmt|;
block|}
return|return
name|c
return|;
block|}
annotation|@
name|Override
specifier|protected
name|int
name|doWork
parameter_list|()
throws|throws
name|Exception
block|{
name|setUp
argument_list|()
expr_stmt|;
try|try
block|{
name|boolean
name|isStripe
init|=
literal|true
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
name|iterationCount
operator|*
literal|2
condition|;
operator|++
name|i
control|)
block|{
name|createTable
argument_list|(
name|isStripe
argument_list|)
expr_stmt|;
name|runOneTest
argument_list|(
operator|(
name|isStripe
condition|?
literal|"Stripe"
else|:
literal|"Default"
operator|)
operator|+
name|i
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|isStripe
operator|=
operator|!
name|isStripe
expr_stmt|;
block|}
return|return
literal|0
return|;
block|}
finally|finally
block|{
name|tearDown
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|this
operator|.
name|util
operator|=
operator|new
name|IntegrationTestingUtility
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Initializing/checking cluster has "
operator|+
name|MIN_NUM_SERVERS
operator|+
literal|" servers"
argument_list|)
expr_stmt|;
name|util
operator|.
name|initializeCluster
argument_list|(
name|MIN_NUM_SERVERS
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Done initializing/checking cluster"
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|deleteTable
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|util
operator|.
name|getAdmin
argument_list|()
operator|.
name|tableExists
argument_list|(
name|TABLE_NAME
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Deleting table"
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|util
operator|.
name|getAdmin
argument_list|()
operator|.
name|isTableDisabled
argument_list|(
name|TABLE_NAME
argument_list|)
condition|)
block|{
name|util
operator|.
name|getAdmin
argument_list|()
operator|.
name|disableTable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
block|}
name|util
operator|.
name|getAdmin
argument_list|()
operator|.
name|deleteTable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Deleted table"
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|createTable
parameter_list|(
name|boolean
name|isStripe
parameter_list|)
throws|throws
name|Exception
block|{
name|createTable
argument_list|(
name|createHtd
argument_list|(
name|isStripe
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|deleteTable
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Restoring the cluster"
argument_list|)
expr_stmt|;
name|util
operator|.
name|restoreCluster
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Done restoring the cluster"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|runOneTest
parameter_list|(
name|String
name|description
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|Exception
block|{
name|int
name|numServers
init|=
name|util
operator|.
name|getHBaseClusterInterface
argument_list|()
operator|.
name|getClusterStatus
argument_list|()
operator|.
name|getServersSize
argument_list|()
decl_stmt|;
name|long
name|startKey
init|=
operator|(
name|long
operator|)
name|preloadKeys
operator|*
name|numServers
decl_stmt|;
name|long
name|endKey
init|=
name|startKey
operator|+
operator|(
name|long
operator|)
name|writeKeys
operator|*
name|numServers
decl_stmt|;
name|status
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%s test starting on %d servers; preloading 0 to %d and writing to %d"
argument_list|,
name|description
argument_list|,
name|numServers
argument_list|,
name|startKey
argument_list|,
name|endKey
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|preloadKeys
operator|>
literal|0
condition|)
block|{
name|MultiThreadedWriter
name|preloader
init|=
operator|new
name|MultiThreadedWriter
argument_list|(
name|dataGen
argument_list|,
name|conf
argument_list|,
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|long
name|time
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|preloader
operator|.
name|start
argument_list|(
literal|0
argument_list|,
name|startKey
argument_list|,
name|writeThreads
argument_list|)
expr_stmt|;
name|preloader
operator|.
name|waitForFinish
argument_list|()
expr_stmt|;
if|if
condition|(
name|preloader
operator|.
name|getNumWriteFailures
argument_list|()
operator|>
literal|0
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Preload failed"
argument_list|)
throw|;
block|}
name|int
name|waitTime
init|=
operator|(
name|int
operator|)
name|Math
operator|.
name|min
argument_list|(
name|preloadKeys
operator|/
literal|100
argument_list|,
literal|30000
argument_list|)
decl_stmt|;
comment|// arbitrary
name|status
argument_list|(
name|description
operator|+
literal|" preload took "
operator|+
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|time
operator|)
operator|/
literal|1000
operator|+
literal|"sec; sleeping for "
operator|+
name|waitTime
operator|/
literal|1000
operator|+
literal|"sec for store to stabilize"
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|waitTime
argument_list|)
expr_stmt|;
block|}
name|MultiThreadedWriter
name|writer
init|=
operator|new
name|MultiThreadedWriter
argument_list|(
name|dataGen
argument_list|,
name|conf
argument_list|,
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|MultiThreadedReader
name|reader
init|=
operator|new
name|MultiThreadedReader
argument_list|(
name|dataGen
argument_list|,
name|conf
argument_list|,
name|TABLE_NAME
argument_list|,
literal|100
argument_list|)
decl_stmt|;
comment|// reader.getMetrics().enable();
name|reader
operator|.
name|linkToWriter
argument_list|(
name|writer
argument_list|)
expr_stmt|;
name|long
name|testStartTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|writer
operator|.
name|start
argument_list|(
name|startKey
argument_list|,
name|endKey
argument_list|,
name|writeThreads
argument_list|)
expr_stmt|;
name|reader
operator|.
name|start
argument_list|(
name|startKey
argument_list|,
name|endKey
argument_list|,
name|readThreads
argument_list|)
expr_stmt|;
name|writer
operator|.
name|waitForFinish
argument_list|()
expr_stmt|;
name|reader
operator|.
name|waitForFinish
argument_list|()
expr_stmt|;
comment|// reader.waitForVerification(300000);
comment|// reader.abortAndWaitForFinish();
name|status
argument_list|(
literal|"Readers and writers stopped for test "
operator|+
name|description
argument_list|)
expr_stmt|;
name|boolean
name|success
init|=
name|writer
operator|.
name|getNumWriteFailures
argument_list|()
operator|==
literal|0
decl_stmt|;
if|if
condition|(
operator|!
name|success
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Write failed"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|success
operator|=
name|reader
operator|.
name|getNumReadErrors
argument_list|()
operator|==
literal|0
operator|&&
name|reader
operator|.
name|getNumReadFailures
argument_list|()
operator|==
literal|0
expr_stmt|;
if|if
condition|(
operator|!
name|success
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Read failed"
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Dump perf regardless of the result.
comment|/*StringBuilder perfDump = new StringBuilder();     for (Pair<Long, Long> pt : reader.getMetrics().getCombinedCdf()) {       perfDump.append(String.format(           "csvread,%s,%d,%d%n", description, pt.getFirst(), pt.getSecond()));     }     if (dumpTimePerf) {       Iterator<Triple<Long, Double, Long>> timePerf = reader.getMetrics().getCombinedTimeSeries();       while (timePerf.hasNext()) {         Triple<Long, Double, Long> pt = timePerf.next();         perfDump.append(String.format("csvtime,%s,%d,%d,%.4f%n",             description, pt.getFirst(), pt.getThird(), pt.getSecond()));       }     }     LOG.info("Performance data dump for " + description + " test: \n" + perfDump.toString());*/
name|status
argument_list|(
name|description
operator|+
literal|" test took "
operator|+
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|testStartTime
operator|)
operator|/
literal|1000
operator|+
literal|"sec"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|success
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|status
parameter_list|(
name|String
name|s
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"STATUS "
operator|+
name|s
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|s
argument_list|)
expr_stmt|;
block|}
specifier|private
name|HTableDescriptor
name|createHtd
parameter_list|(
name|boolean
name|isStripe
parameter_list|)
throws|throws
name|Exception
block|{
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|COLUMN_FAMILY
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|noSplitsPolicy
init|=
name|DisabledRegionSplitPolicy
operator|.
name|class
operator|.
name|getName
argument_list|()
decl_stmt|;
name|htd
operator|.
name|setConfiguration
argument_list|(
name|HConstants
operator|.
name|HBASE_REGION_SPLIT_POLICY_KEY
argument_list|,
name|noSplitsPolicy
argument_list|)
expr_stmt|;
if|if
condition|(
name|isStripe
condition|)
block|{
name|htd
operator|.
name|setConfiguration
argument_list|(
name|StoreEngine
operator|.
name|STORE_ENGINE_CLASS_KEY
argument_list|,
name|StripeStoreEngine
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|initialStripeCount
operator|!=
literal|null
condition|)
block|{
name|htd
operator|.
name|setConfiguration
argument_list|(
name|StripeStoreConfig
operator|.
name|INITIAL_STRIPE_COUNT_KEY
argument_list|,
name|initialStripeCount
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|htd
operator|.
name|setConfiguration
argument_list|(
name|HStore
operator|.
name|BLOCKING_STOREFILES_KEY
argument_list|,
name|Long
operator|.
name|toString
argument_list|(
literal|10
operator|*
name|initialStripeCount
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|htd
operator|.
name|setConfiguration
argument_list|(
name|HStore
operator|.
name|BLOCKING_STOREFILES_KEY
argument_list|,
literal|"500"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|splitSize
operator|!=
literal|null
condition|)
block|{
name|htd
operator|.
name|setConfiguration
argument_list|(
name|StripeStoreConfig
operator|.
name|SIZE_TO_SPLIT_KEY
argument_list|,
name|splitSize
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|splitParts
operator|!=
literal|null
condition|)
block|{
name|htd
operator|.
name|setConfiguration
argument_list|(
name|StripeStoreConfig
operator|.
name|SPLIT_PARTS_KEY
argument_list|,
name|splitParts
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|htd
operator|.
name|setConfiguration
argument_list|(
name|HStore
operator|.
name|BLOCKING_STOREFILES_KEY
argument_list|,
literal|"10"
argument_list|)
expr_stmt|;
comment|// default
block|}
return|return
name|htd
return|;
block|}
specifier|protected
name|void
name|createTable
parameter_list|(
name|HTableDescriptor
name|htd
parameter_list|)
throws|throws
name|Exception
block|{
name|deleteTable
argument_list|()
expr_stmt|;
if|if
condition|(
name|util
operator|.
name|getHBaseClusterInterface
argument_list|()
operator|instanceof
name|MiniHBaseCluster
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Test does not make a lot of sense for minicluster. Will set flush size low."
argument_list|)
expr_stmt|;
name|htd
operator|.
name|setConfiguration
argument_list|(
name|HConstants
operator|.
name|HREGION_MEMSTORE_FLUSH_SIZE
argument_list|,
literal|"1048576"
argument_list|)
expr_stmt|;
block|}
name|byte
index|[]
index|[]
name|splits
init|=
operator|new
name|RegionSplitter
operator|.
name|HexStringSplit
argument_list|()
operator|.
name|split
argument_list|(
name|util
operator|.
name|getHBaseClusterInterface
argument_list|()
operator|.
name|getClusterStatus
argument_list|()
operator|.
name|getServersSize
argument_list|()
argument_list|)
decl_stmt|;
name|util
operator|.
name|getAdmin
argument_list|()
operator|.
name|createTable
argument_list|(
name|htd
argument_list|,
name|splits
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
class|class
name|SeqShardedDataGenerator
extends|extends
name|LoadTestDataGenerator
block|{
specifier|private
specifier|static
specifier|final
name|byte
index|[]
index|[]
name|COLUMN_NAMES
init|=
operator|new
name|byte
index|[]
index|[]
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col1"
argument_list|)
block|}
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|PAD_TO
init|=
literal|10
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|PREFIX_PAD_TO
init|=
literal|7
decl_stmt|;
specifier|private
specifier|final
name|int
name|numPartitions
decl_stmt|;
specifier|public
name|SeqShardedDataGenerator
parameter_list|(
name|int
name|minValueSize
parameter_list|,
name|int
name|maxValueSize
parameter_list|,
name|int
name|numPartitions
parameter_list|)
block|{
name|super
argument_list|(
name|minValueSize
argument_list|,
name|maxValueSize
argument_list|)
expr_stmt|;
name|this
operator|.
name|numPartitions
operator|=
name|numPartitions
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|getDeterministicUniqueKey
parameter_list|(
name|long
name|keyBase
parameter_list|)
block|{
name|String
name|num
init|=
name|StringUtils
operator|.
name|leftPad
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|keyBase
argument_list|)
argument_list|,
name|PAD_TO
argument_list|,
literal|"0"
argument_list|)
decl_stmt|;
return|return
name|Bytes
operator|.
name|toBytes
argument_list|(
name|getPrefix
argument_list|(
name|keyBase
argument_list|)
operator|+
name|num
argument_list|)
return|;
block|}
specifier|private
name|String
name|getPrefix
parameter_list|(
name|long
name|i
parameter_list|)
block|{
return|return
name|StringUtils
operator|.
name|leftPad
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
call|(
name|int
call|)
argument_list|(
name|i
operator|%
name|numPartitions
argument_list|)
argument_list|)
argument_list|,
name|PREFIX_PAD_TO
argument_list|,
literal|"0"
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
index|[]
name|getColumnFamilies
parameter_list|()
block|{
return|return
operator|new
name|byte
index|[]
index|[]
block|{
name|COLUMN_FAMILY
block|}
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
index|[]
name|generateColumnsForCf
parameter_list|(
name|byte
index|[]
name|rowKey
parameter_list|,
name|byte
index|[]
name|cf
parameter_list|)
block|{
return|return
name|COLUMN_NAMES
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|generateValue
parameter_list|(
name|byte
index|[]
name|rowKey
parameter_list|,
name|byte
index|[]
name|cf
parameter_list|,
name|byte
index|[]
name|column
parameter_list|)
block|{
return|return
name|kvGenerator
operator|.
name|generateRandomSizeValue
argument_list|(
name|rowKey
argument_list|,
name|cf
argument_list|,
name|column
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|verify
parameter_list|(
name|byte
index|[]
name|rowKey
parameter_list|,
name|byte
index|[]
name|cf
parameter_list|,
name|byte
index|[]
name|column
parameter_list|,
name|byte
index|[]
name|value
parameter_list|)
block|{
return|return
name|LoadTestKVGenerator
operator|.
name|verify
argument_list|(
name|value
argument_list|,
name|rowKey
argument_list|,
name|cf
argument_list|,
name|column
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|verify
parameter_list|(
name|byte
index|[]
name|rowKey
parameter_list|,
name|byte
index|[]
name|cf
parameter_list|,
name|Set
argument_list|<
name|byte
index|[]
argument_list|>
name|columnSet
parameter_list|)
block|{
return|return
literal|true
return|;
block|}
block|}
empty_stmt|;
block|}
end_class

end_unit

