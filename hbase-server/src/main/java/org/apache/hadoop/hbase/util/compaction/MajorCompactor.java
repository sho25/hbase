begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  * http://www.apache.org/licenses/LICENSE-2.0  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|util
operator|.
name|compaction
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
name|Arrays
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
name|Optional
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ConcurrentHashMap
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
name|TimeUnit
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
name|cli
operator|.
name|CommandLineParser
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
name|DefaultParser
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
name|HelpFormatter
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
name|Option
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
name|Options
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
name|ParseException
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
name|HBaseConfiguration
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
name|HBaseInterfaceAudience
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
name|HConstants
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
name|HRegionLocation
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
name|NotServingRegionException
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
name|ServerName
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
name|Admin
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
name|CompactionState
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
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Joiner
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Splitter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Iterables
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Sets
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|TOOLS
argument_list|)
specifier|public
class|class
name|MajorCompactor
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|MajorCompactor
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Set
argument_list|<
name|MajorCompactionRequest
argument_list|>
name|ERRORS
init|=
name|ConcurrentHashMap
operator|.
name|newKeySet
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|ClusterCompactionQueues
name|clusterCompactionQueues
decl_stmt|;
specifier|private
specifier|final
name|long
name|timestamp
decl_stmt|;
specifier|private
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|storesToCompact
decl_stmt|;
specifier|private
specifier|final
name|ExecutorService
name|executor
decl_stmt|;
specifier|private
specifier|final
name|long
name|sleepForMs
decl_stmt|;
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
name|MajorCompactor
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|TableName
name|tableName
parameter_list|,
name|Set
argument_list|<
name|String
argument_list|>
name|storesToCompact
parameter_list|,
name|int
name|concurrency
parameter_list|,
name|long
name|timestamp
parameter_list|,
name|long
name|sleepForMs
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|connection
operator|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|timestamp
operator|=
name|timestamp
expr_stmt|;
name|this
operator|.
name|storesToCompact
operator|=
name|storesToCompact
expr_stmt|;
name|this
operator|.
name|executor
operator|=
name|Executors
operator|.
name|newFixedThreadPool
argument_list|(
name|concurrency
argument_list|)
expr_stmt|;
name|this
operator|.
name|clusterCompactionQueues
operator|=
operator|new
name|ClusterCompactionQueues
argument_list|(
name|concurrency
argument_list|)
expr_stmt|;
name|this
operator|.
name|sleepForMs
operator|=
name|sleepForMs
expr_stmt|;
block|}
specifier|public
name|void
name|compactAllRegions
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|Future
argument_list|<
name|?
argument_list|>
argument_list|>
name|futures
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
while|while
condition|(
name|clusterCompactionQueues
operator|.
name|hasWorkItems
argument_list|()
operator|||
operator|!
name|futuresComplete
argument_list|(
name|futures
argument_list|)
condition|)
block|{
while|while
condition|(
name|clusterCompactionQueues
operator|.
name|atCapacity
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Waiting for servers to complete Compactions"
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|sleepForMs
argument_list|)
expr_stmt|;
block|}
name|Optional
argument_list|<
name|ServerName
argument_list|>
name|serverToProcess
init|=
name|clusterCompactionQueues
operator|.
name|getLargestQueueFromServersNotCompacting
argument_list|()
decl_stmt|;
if|if
condition|(
name|serverToProcess
operator|.
name|isPresent
argument_list|()
operator|&&
name|clusterCompactionQueues
operator|.
name|hasWorkItems
argument_list|()
condition|)
block|{
name|ServerName
name|serverName
init|=
name|serverToProcess
operator|.
name|get
argument_list|()
decl_stmt|;
comment|// check to see if the region has moved... if so we have to enqueue it again with
comment|// the proper serverName
name|MajorCompactionRequest
name|request
init|=
name|clusterCompactionQueues
operator|.
name|reserveForCompaction
argument_list|(
name|serverName
argument_list|)
decl_stmt|;
name|ServerName
name|currentServer
init|=
name|connection
operator|.
name|getRegionLocator
argument_list|(
name|tableName
argument_list|)
operator|.
name|getRegionLocation
argument_list|(
name|request
operator|.
name|getRegion
argument_list|()
operator|.
name|getStartKey
argument_list|()
argument_list|)
operator|.
name|getServerName
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|currentServer
operator|.
name|equals
argument_list|(
name|serverName
argument_list|)
condition|)
block|{
comment|// add it back to the queue with the correct server it should be picked up in the future.
name|LOG
operator|.
name|info
argument_list|(
literal|"Server changed for region: "
operator|+
name|request
operator|.
name|getRegion
argument_list|()
operator|.
name|getEncodedName
argument_list|()
operator|+
literal|" from: "
operator|+
name|serverName
operator|+
literal|" to: "
operator|+
name|currentServer
operator|+
literal|" re-queuing request"
argument_list|)
expr_stmt|;
name|clusterCompactionQueues
operator|.
name|addToCompactionQueue
argument_list|(
name|currentServer
argument_list|,
name|request
argument_list|)
expr_stmt|;
name|clusterCompactionQueues
operator|.
name|releaseCompaction
argument_list|(
name|serverName
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Firing off compaction request for server: "
operator|+
name|serverName
operator|+
literal|", "
operator|+
name|request
operator|+
literal|" total queue size left: "
operator|+
name|clusterCompactionQueues
operator|.
name|getCompactionRequestsLeftToFinish
argument_list|()
argument_list|)
expr_stmt|;
name|futures
operator|.
name|add
argument_list|(
name|executor
operator|.
name|submit
argument_list|(
operator|new
name|Compact
argument_list|(
name|serverName
argument_list|,
name|request
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// haven't assigned anything so we sleep.
name|Thread
operator|.
name|sleep
argument_list|(
name|sleepForMs
argument_list|)
expr_stmt|;
block|}
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"All compactions have completed"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|boolean
name|futuresComplete
parameter_list|(
name|List
argument_list|<
name|Future
argument_list|<
name|?
argument_list|>
argument_list|>
name|futures
parameter_list|)
block|{
name|futures
operator|.
name|removeIf
argument_list|(
name|Future
operator|::
name|isDone
argument_list|)
expr_stmt|;
return|return
name|futures
operator|.
name|isEmpty
argument_list|()
return|;
block|}
specifier|public
name|void
name|shutdown
parameter_list|()
throws|throws
name|Exception
block|{
name|executor
operator|.
name|shutdown
argument_list|()
expr_stmt|;
name|executor
operator|.
name|awaitTermination
argument_list|(
name|Long
operator|.
name|MAX_VALUE
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|ERRORS
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|StringBuilder
name|builder
init|=
operator|new
name|StringBuilder
argument_list|()
operator|.
name|append
argument_list|(
literal|"Major compaction failed, there were: "
argument_list|)
operator|.
name|append
argument_list|(
name|ERRORS
operator|.
name|size
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|" regions / stores that failed compacting\n"
argument_list|)
operator|.
name|append
argument_list|(
literal|"Failed compaction requests\n"
argument_list|)
operator|.
name|append
argument_list|(
literal|"--------------------------\n"
argument_list|)
operator|.
name|append
argument_list|(
name|Joiner
operator|.
name|on
argument_list|(
literal|"\n"
argument_list|)
operator|.
name|join
argument_list|(
name|ERRORS
argument_list|)
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|builder
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|connection
operator|!=
literal|null
condition|)
block|{
name|connection
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"All regions major compacted successfully"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|VisibleForTesting
name|void
name|initializeWorkQueues
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|storesToCompact
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|connection
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
operator|.
name|getDescriptor
argument_list|()
operator|.
name|getColumnFamilyNames
argument_list|()
operator|.
name|forEach
argument_list|(
name|a
lambda|->
name|storesToCompact
operator|.
name|add
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|a
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"No family specified, will execute for all families"
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Initializing compaction queues for table:  "
operator|+
name|tableName
operator|+
literal|" with cf: "
operator|+
name|storesToCompact
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HRegionLocation
argument_list|>
name|regionLocations
init|=
name|connection
operator|.
name|getRegionLocator
argument_list|(
name|tableName
argument_list|)
operator|.
name|getAllRegionLocations
argument_list|()
decl_stmt|;
for|for
control|(
name|HRegionLocation
name|location
range|:
name|regionLocations
control|)
block|{
name|Optional
argument_list|<
name|MajorCompactionRequest
argument_list|>
name|request
init|=
name|MajorCompactionRequest
operator|.
name|newRequest
argument_list|(
name|connection
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|location
operator|.
name|getRegion
argument_list|()
argument_list|,
name|storesToCompact
argument_list|,
name|timestamp
argument_list|)
decl_stmt|;
name|request
operator|.
name|ifPresent
argument_list|(
name|majorCompactionRequest
lambda|->
name|clusterCompactionQueues
operator|.
name|addToCompactionQueue
argument_list|(
name|location
operator|.
name|getServerName
argument_list|()
argument_list|,
name|majorCompactionRequest
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
class|class
name|Compact
implements|implements
name|Runnable
block|{
specifier|private
specifier|final
name|ServerName
name|serverName
decl_stmt|;
specifier|private
specifier|final
name|MajorCompactionRequest
name|request
decl_stmt|;
name|Compact
parameter_list|(
name|ServerName
name|serverName
parameter_list|,
name|MajorCompactionRequest
name|request
parameter_list|)
block|{
name|this
operator|.
name|serverName
operator|=
name|serverName
expr_stmt|;
name|this
operator|.
name|request
operator|=
name|request
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|compactAndWait
argument_list|(
name|request
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NotServingRegionException
name|e
parameter_list|)
block|{
comment|// this region has split or merged
name|LOG
operator|.
name|warn
argument_list|(
literal|"Region is invalid, requesting updated regions"
argument_list|,
name|e
argument_list|)
expr_stmt|;
comment|// lets updated the cluster compaction queues with these newly created regions.
name|addNewRegions
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Error compacting:"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|clusterCompactionQueues
operator|.
name|releaseCompaction
argument_list|(
name|serverName
argument_list|)
expr_stmt|;
block|}
block|}
name|void
name|compactAndWait
parameter_list|(
name|MajorCompactionRequest
name|request
parameter_list|)
throws|throws
name|Exception
block|{
name|Admin
name|admin
init|=
name|connection
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
try|try
block|{
comment|// only make the request if the region is not already major compacting
if|if
condition|(
operator|!
name|isCompacting
argument_list|(
name|request
argument_list|)
condition|)
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|stores
init|=
name|request
operator|.
name|getStoresRequiringCompaction
argument_list|(
name|storesToCompact
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|stores
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|request
operator|.
name|setStores
argument_list|(
name|stores
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|store
range|:
name|request
operator|.
name|getStores
argument_list|()
control|)
block|{
name|admin
operator|.
name|majorCompactRegion
argument_list|(
name|request
operator|.
name|getRegion
argument_list|()
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|store
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
while|while
condition|(
name|isCompacting
argument_list|(
name|request
argument_list|)
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|sleepForMs
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Waiting for compaction to complete for region: "
operator|+
name|request
operator|.
name|getRegion
argument_list|()
operator|.
name|getEncodedName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
comment|// Make sure to wait for the CompactedFileDischarger chore to do its work
name|int
name|waitForArchive
init|=
name|connection
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getInt
argument_list|(
literal|"hbase.hfile.compaction.discharger.interval"
argument_list|,
literal|2
operator|*
literal|60
operator|*
literal|1000
argument_list|)
decl_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|waitForArchive
argument_list|)
expr_stmt|;
comment|// check if compaction completed successfully, otherwise put that request back in the
comment|// proper queue
name|Set
argument_list|<
name|String
argument_list|>
name|storesRequiringCompaction
init|=
name|request
operator|.
name|getStoresRequiringCompaction
argument_list|(
name|storesToCompact
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|storesRequiringCompaction
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// this happens, when a region server is marked as dead, flushes a store file and
comment|// the new regionserver doesn't pick it up because its accounted for in the WAL replay,
comment|// thus you have more store files on the filesystem than the regionserver knows about.
name|boolean
name|regionHasNotMoved
init|=
name|connection
operator|.
name|getRegionLocator
argument_list|(
name|tableName
argument_list|)
operator|.
name|getRegionLocation
argument_list|(
name|request
operator|.
name|getRegion
argument_list|()
operator|.
name|getStartKey
argument_list|()
argument_list|)
operator|.
name|getServerName
argument_list|()
operator|.
name|equals
argument_list|(
name|serverName
argument_list|)
decl_stmt|;
if|if
condition|(
name|regionHasNotMoved
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Not all store files were compacted, this may be due to the regionserver not "
operator|+
literal|"being aware of all store files.  Will not reattempt compacting, "
operator|+
name|request
argument_list|)
expr_stmt|;
name|ERRORS
operator|.
name|add
argument_list|(
name|request
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|request
operator|.
name|setStores
argument_list|(
name|storesRequiringCompaction
argument_list|)
expr_stmt|;
name|clusterCompactionQueues
operator|.
name|addToCompactionQueue
argument_list|(
name|serverName
argument_list|,
name|request
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Compaction failed for the following stores: "
operator|+
name|storesRequiringCompaction
operator|+
literal|" region: "
operator|+
name|request
operator|.
name|getRegion
argument_list|()
operator|.
name|getEncodedName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Compaction complete for region: "
operator|+
name|request
operator|.
name|getRegion
argument_list|()
operator|.
name|getEncodedName
argument_list|()
operator|+
literal|" -> cf(s): "
operator|+
name|request
operator|.
name|getStores
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|private
name|boolean
name|isCompacting
parameter_list|(
name|MajorCompactionRequest
name|request
parameter_list|)
throws|throws
name|Exception
block|{
name|CompactionState
name|compactionState
init|=
name|connection
operator|.
name|getAdmin
argument_list|()
operator|.
name|getCompactionStateForRegion
argument_list|(
name|request
operator|.
name|getRegion
argument_list|()
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|compactionState
operator|.
name|equals
argument_list|(
name|CompactionState
operator|.
name|MAJOR
argument_list|)
operator|||
name|compactionState
operator|.
name|equals
argument_list|(
name|CompactionState
operator|.
name|MAJOR_AND_MINOR
argument_list|)
return|;
block|}
specifier|private
name|void
name|addNewRegions
parameter_list|()
block|{
try|try
block|{
name|List
argument_list|<
name|HRegionLocation
argument_list|>
name|locations
init|=
name|connection
operator|.
name|getRegionLocator
argument_list|(
name|tableName
argument_list|)
operator|.
name|getAllRegionLocations
argument_list|()
decl_stmt|;
for|for
control|(
name|HRegionLocation
name|location
range|:
name|locations
control|)
block|{
if|if
condition|(
name|location
operator|.
name|getRegion
argument_list|()
operator|.
name|getRegionId
argument_list|()
operator|>
name|timestamp
condition|)
block|{
name|Optional
argument_list|<
name|MajorCompactionRequest
argument_list|>
name|compactionRequest
init|=
name|MajorCompactionRequest
operator|.
name|newRequest
argument_list|(
name|connection
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|location
operator|.
name|getRegion
argument_list|()
argument_list|,
name|storesToCompact
argument_list|,
name|timestamp
argument_list|)
decl_stmt|;
name|compactionRequest
operator|.
name|ifPresent
argument_list|(
name|request
lambda|->
name|clusterCompactionQueues
operator|.
name|addToCompactionQueue
argument_list|(
name|location
operator|.
name|getServerName
argument_list|()
argument_list|,
name|request
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
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
name|Options
name|options
init|=
operator|new
name|Options
argument_list|()
decl_stmt|;
name|options
operator|.
name|addOption
argument_list|(
name|Option
operator|.
name|builder
argument_list|(
literal|"table"
argument_list|)
operator|.
name|required
argument_list|()
operator|.
name|desc
argument_list|(
literal|"table name"
argument_list|)
operator|.
name|hasArg
argument_list|()
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
name|Option
operator|.
name|builder
argument_list|(
literal|"cf"
argument_list|)
operator|.
name|optionalArg
argument_list|(
literal|true
argument_list|)
operator|.
name|desc
argument_list|(
literal|"column families: comma separated eg: a,b,c"
argument_list|)
operator|.
name|hasArg
argument_list|()
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
name|Option
operator|.
name|builder
argument_list|(
literal|"servers"
argument_list|)
operator|.
name|required
argument_list|()
operator|.
name|desc
argument_list|(
literal|"Concurrent servers compacting"
argument_list|)
operator|.
name|hasArg
argument_list|()
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
name|Option
operator|.
name|builder
argument_list|(
literal|"minModTime"
argument_list|)
operator|.
name|desc
argument_list|(
literal|"Compact if store files have modification time< minModTime"
argument_list|)
operator|.
name|hasArg
argument_list|()
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
name|Option
operator|.
name|builder
argument_list|(
literal|"zk"
argument_list|)
operator|.
name|optionalArg
argument_list|(
literal|true
argument_list|)
operator|.
name|desc
argument_list|(
literal|"zk quorum"
argument_list|)
operator|.
name|hasArg
argument_list|()
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
name|Option
operator|.
name|builder
argument_list|(
literal|"rootDir"
argument_list|)
operator|.
name|optionalArg
argument_list|(
literal|true
argument_list|)
operator|.
name|desc
argument_list|(
literal|"hbase.rootDir"
argument_list|)
operator|.
name|hasArg
argument_list|()
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
name|Option
operator|.
name|builder
argument_list|(
literal|"sleep"
argument_list|)
operator|.
name|desc
argument_list|(
literal|"Time to sleepForMs (ms) for checking compaction status per region and available "
operator|+
literal|"work queues: default 30s"
argument_list|)
operator|.
name|hasArg
argument_list|()
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
name|Option
operator|.
name|builder
argument_list|(
literal|"retries"
argument_list|)
operator|.
name|desc
argument_list|(
literal|"Max # of retries for a compaction request,"
operator|+
literal|" defaults to 3"
argument_list|)
operator|.
name|hasArg
argument_list|()
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
name|Option
operator|.
name|builder
argument_list|(
literal|"dryRun"
argument_list|)
operator|.
name|desc
argument_list|(
literal|"Dry run, will just output a list of regions that require compaction based on "
operator|+
literal|"parameters passed"
argument_list|)
operator|.
name|hasArg
argument_list|(
literal|false
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|CommandLineParser
name|cmdLineParser
init|=
operator|new
name|DefaultParser
argument_list|()
decl_stmt|;
name|CommandLine
name|commandLine
init|=
literal|null
decl_stmt|;
try|try
block|{
name|commandLine
operator|=
name|cmdLineParser
operator|.
name|parse
argument_list|(
name|options
argument_list|,
name|args
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ParseException
name|parseException
parameter_list|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"ERROR: Unable to parse command-line arguments "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|args
argument_list|)
operator|+
literal|" due to: "
operator|+
name|parseException
argument_list|)
expr_stmt|;
name|printUsage
argument_list|(
name|options
argument_list|)
expr_stmt|;
block|}
name|String
name|tableName
init|=
name|commandLine
operator|.
name|getOptionValue
argument_list|(
literal|"table"
argument_list|)
decl_stmt|;
name|String
name|cf
init|=
name|commandLine
operator|.
name|getOptionValue
argument_list|(
literal|"cf"
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|families
init|=
name|Sets
operator|.
name|newHashSet
argument_list|()
decl_stmt|;
if|if
condition|(
name|cf
operator|!=
literal|null
condition|)
block|{
name|Iterables
operator|.
name|addAll
argument_list|(
name|families
argument_list|,
name|Splitter
operator|.
name|on
argument_list|(
literal|","
argument_list|)
operator|.
name|split
argument_list|(
name|cf
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|Configuration
name|configuration
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|int
name|concurrency
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|commandLine
operator|.
name|getOptionValue
argument_list|(
literal|"servers"
argument_list|)
argument_list|)
decl_stmt|;
name|long
name|minModTime
init|=
name|Long
operator|.
name|parseLong
argument_list|(
name|commandLine
operator|.
name|getOptionValue
argument_list|(
literal|"minModTime"
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|quorum
init|=
name|commandLine
operator|.
name|getOptionValue
argument_list|(
literal|"zk"
argument_list|,
name|configuration
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_QUORUM
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|rootDir
init|=
name|commandLine
operator|.
name|getOptionValue
argument_list|(
literal|"rootDir"
argument_list|,
name|configuration
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|HBASE_DIR
argument_list|)
argument_list|)
decl_stmt|;
name|long
name|sleep
init|=
name|Long
operator|.
name|valueOf
argument_list|(
name|commandLine
operator|.
name|getOptionValue
argument_list|(
literal|"sleep"
argument_list|,
name|Long
operator|.
name|toString
argument_list|(
literal|30000
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|configuration
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|HBASE_DIR
argument_list|,
name|rootDir
argument_list|)
expr_stmt|;
name|configuration
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_QUORUM
argument_list|,
name|quorum
argument_list|)
expr_stmt|;
name|MajorCompactor
name|compactor
init|=
operator|new
name|MajorCompactor
argument_list|(
name|configuration
argument_list|,
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableName
argument_list|)
argument_list|,
name|families
argument_list|,
name|concurrency
argument_list|,
name|minModTime
argument_list|,
name|sleep
argument_list|)
decl_stmt|;
name|compactor
operator|.
name|initializeWorkQueues
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|commandLine
operator|.
name|hasOption
argument_list|(
literal|"dryRun"
argument_list|)
condition|)
block|{
name|compactor
operator|.
name|compactAllRegions
argument_list|()
expr_stmt|;
block|}
name|compactor
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|printUsage
parameter_list|(
specifier|final
name|Options
name|options
parameter_list|)
block|{
name|String
name|header
init|=
literal|"\nUsage instructions\n\n"
decl_stmt|;
name|String
name|footer
init|=
literal|"\n"
decl_stmt|;
name|HelpFormatter
name|formatter
init|=
operator|new
name|HelpFormatter
argument_list|()
decl_stmt|;
name|formatter
operator|.
name|printHelp
argument_list|(
name|MajorCompactor
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|,
name|header
argument_list|,
name|options
argument_list|,
name|footer
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
