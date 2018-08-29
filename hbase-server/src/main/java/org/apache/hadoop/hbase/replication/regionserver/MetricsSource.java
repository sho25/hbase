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
operator|.
name|replication
operator|.
name|regionserver
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
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
name|hadoop
operator|.
name|hbase
operator|.
name|CompatibilitySingletonFactory
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
name|metrics
operator|.
name|BaseSource
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
name|EnvironmentEdgeManager
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

begin_comment
comment|/**  * This class is for maintaining the various replication statistics for a source and publishing them  * through the metrics interfaces.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|REPLICATION
argument_list|)
specifier|public
class|class
name|MetricsSource
implements|implements
name|BaseSource
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
name|MetricsSource
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// tracks last shipped timestamp for each wal group
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|lastTimestamps
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|ageOfLastShippedOp
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
name|long
name|lastHFileRefsQueueSize
init|=
literal|0
decl_stmt|;
specifier|private
name|String
name|id
decl_stmt|;
specifier|private
specifier|final
name|MetricsReplicationSourceSource
name|singleSourceSource
decl_stmt|;
specifier|private
specifier|final
name|MetricsReplicationSourceSource
name|globalSourceSource
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|MetricsReplicationSourceSource
argument_list|>
name|singleSourceSourceByTable
decl_stmt|;
comment|/**    * Constructor used to register the metrics    *    * @param id Name of the source this class is monitoring    */
specifier|public
name|MetricsSource
parameter_list|(
name|String
name|id
parameter_list|)
block|{
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
name|singleSourceSource
operator|=
name|CompatibilitySingletonFactory
operator|.
name|getInstance
argument_list|(
name|MetricsReplicationSourceFactory
operator|.
name|class
argument_list|)
operator|.
name|getSource
argument_list|(
name|id
argument_list|)
expr_stmt|;
name|globalSourceSource
operator|=
name|CompatibilitySingletonFactory
operator|.
name|getInstance
argument_list|(
name|MetricsReplicationSourceFactory
operator|.
name|class
argument_list|)
operator|.
name|getGlobalSource
argument_list|()
expr_stmt|;
name|singleSourceSourceByTable
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
block|}
comment|/**    * Constructor for injecting custom (or test) MetricsReplicationSourceSources    * @param id Name of the source this class is monitoring    * @param singleSourceSource Class to monitor id-scoped metrics    * @param globalSourceSource Class to monitor global-scoped metrics    */
specifier|public
name|MetricsSource
parameter_list|(
name|String
name|id
parameter_list|,
name|MetricsReplicationSourceSource
name|singleSourceSource
parameter_list|,
name|MetricsReplicationSourceSource
name|globalSourceSource
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|MetricsReplicationSourceSource
argument_list|>
name|singleSourceSourceByTable
parameter_list|)
block|{
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
name|this
operator|.
name|singleSourceSource
operator|=
name|singleSourceSource
expr_stmt|;
name|this
operator|.
name|globalSourceSource
operator|=
name|globalSourceSource
expr_stmt|;
name|this
operator|.
name|singleSourceSourceByTable
operator|=
name|singleSourceSourceByTable
expr_stmt|;
block|}
comment|/**    * Set the age of the last edit that was shipped    * @param timestamp write time of the edit    * @param walGroup which group we are setting    */
specifier|public
name|void
name|setAgeOfLastShippedOp
parameter_list|(
name|long
name|timestamp
parameter_list|,
name|String
name|walGroup
parameter_list|)
block|{
name|long
name|age
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
operator|-
name|timestamp
decl_stmt|;
name|singleSourceSource
operator|.
name|setLastShippedAge
argument_list|(
name|age
argument_list|)
expr_stmt|;
name|globalSourceSource
operator|.
name|setLastShippedAge
argument_list|(
name|age
argument_list|)
expr_stmt|;
name|this
operator|.
name|ageOfLastShippedOp
operator|.
name|put
argument_list|(
name|walGroup
argument_list|,
name|age
argument_list|)
expr_stmt|;
name|this
operator|.
name|lastTimestamps
operator|.
name|put
argument_list|(
name|walGroup
argument_list|,
name|timestamp
argument_list|)
expr_stmt|;
block|}
comment|/**    * Set the age of the last edit that was shipped group by table    * @param timestamp write time of the edit    * @param tableName String as group and tableName    */
specifier|public
name|void
name|setAgeOfLastShippedOpByTable
parameter_list|(
name|long
name|timestamp
parameter_list|,
name|String
name|tableName
parameter_list|)
block|{
name|long
name|age
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
operator|-
name|timestamp
decl_stmt|;
name|this
operator|.
name|getSingleSourceSourceByTable
argument_list|()
operator|.
name|computeIfAbsent
argument_list|(
name|tableName
argument_list|,
name|t
lambda|->
name|CompatibilitySingletonFactory
operator|.
name|getInstance
argument_list|(
name|MetricsReplicationSourceFactory
operator|.
name|class
argument_list|)
operator|.
name|getSource
argument_list|(
name|t
argument_list|)
argument_list|)
operator|.
name|setLastShippedAge
argument_list|(
name|age
argument_list|)
expr_stmt|;
block|}
comment|/**    * get the last timestamp of given wal group. If the walGroup is null, return 0.    * @param walGroup which group we are getting    * @return timeStamp    */
specifier|public
name|long
name|getLastTimeStampOfWalGroup
parameter_list|(
name|String
name|walGroup
parameter_list|)
block|{
return|return
name|this
operator|.
name|lastTimestamps
operator|.
name|get
argument_list|(
name|walGroup
argument_list|)
operator|==
literal|null
condition|?
literal|0
else|:
name|lastTimestamps
operator|.
name|get
argument_list|(
name|walGroup
argument_list|)
return|;
block|}
comment|/**    * get age of last shipped op of given wal group. If the walGroup is null, return 0    * @param walGroup which group we are getting    * @return age    */
specifier|public
name|long
name|getAgeofLastShippedOp
parameter_list|(
name|String
name|walGroup
parameter_list|)
block|{
return|return
name|this
operator|.
name|ageOfLastShippedOp
operator|.
name|get
argument_list|(
name|walGroup
argument_list|)
operator|==
literal|null
condition|?
literal|0
else|:
name|ageOfLastShippedOp
operator|.
name|get
argument_list|(
name|walGroup
argument_list|)
return|;
block|}
comment|/**    * Convenience method to use the last given timestamp to refresh the age of the last edit. Used    * when replication fails and need to keep that metric accurate.    * @param walGroupId id of the group to update    */
specifier|public
name|void
name|refreshAgeOfLastShippedOp
parameter_list|(
name|String
name|walGroupId
parameter_list|)
block|{
name|Long
name|lastTimestamp
init|=
name|this
operator|.
name|lastTimestamps
operator|.
name|get
argument_list|(
name|walGroupId
argument_list|)
decl_stmt|;
if|if
condition|(
name|lastTimestamp
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|lastTimestamps
operator|.
name|put
argument_list|(
name|walGroupId
argument_list|,
literal|0L
argument_list|)
expr_stmt|;
name|lastTimestamp
operator|=
literal|0L
expr_stmt|;
block|}
if|if
condition|(
name|lastTimestamp
operator|>
literal|0
condition|)
block|{
name|setAgeOfLastShippedOp
argument_list|(
name|lastTimestamp
argument_list|,
name|walGroupId
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Increment size of the log queue.    */
specifier|public
name|void
name|incrSizeOfLogQueue
parameter_list|()
block|{
name|singleSourceSource
operator|.
name|incrSizeOfLogQueue
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|globalSourceSource
operator|.
name|incrSizeOfLogQueue
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|decrSizeOfLogQueue
parameter_list|()
block|{
name|singleSourceSource
operator|.
name|decrSizeOfLogQueue
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|globalSourceSource
operator|.
name|decrSizeOfLogQueue
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
comment|/**    * Add on the the number of log edits read    *    * @param delta the number of log edits read.    */
specifier|private
name|void
name|incrLogEditsRead
parameter_list|(
name|long
name|delta
parameter_list|)
block|{
name|singleSourceSource
operator|.
name|incrLogReadInEdits
argument_list|(
name|delta
argument_list|)
expr_stmt|;
name|globalSourceSource
operator|.
name|incrLogReadInEdits
argument_list|(
name|delta
argument_list|)
expr_stmt|;
block|}
comment|/** Increment the number of log edits read by one. */
specifier|public
name|void
name|incrLogEditsRead
parameter_list|()
block|{
name|incrLogEditsRead
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
comment|/**    * Add on the number of log edits filtered    *    * @param delta the number filtered.    */
specifier|public
name|void
name|incrLogEditsFiltered
parameter_list|(
name|long
name|delta
parameter_list|)
block|{
name|singleSourceSource
operator|.
name|incrLogEditsFiltered
argument_list|(
name|delta
argument_list|)
expr_stmt|;
name|globalSourceSource
operator|.
name|incrLogEditsFiltered
argument_list|(
name|delta
argument_list|)
expr_stmt|;
block|}
comment|/** The number of log edits filtered out. */
specifier|public
name|void
name|incrLogEditsFiltered
parameter_list|()
block|{
name|incrLogEditsFiltered
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
comment|/**    * Convience method to apply changes to metrics do to shipping a batch of logs.    *    * @param batchSize the size of the batch that was shipped to sinks.    */
specifier|public
name|void
name|shipBatch
parameter_list|(
name|long
name|batchSize
parameter_list|,
name|int
name|sizeInBytes
parameter_list|)
block|{
name|singleSourceSource
operator|.
name|incrBatchesShipped
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|globalSourceSource
operator|.
name|incrBatchesShipped
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|singleSourceSource
operator|.
name|incrOpsShipped
argument_list|(
name|batchSize
argument_list|)
expr_stmt|;
name|globalSourceSource
operator|.
name|incrOpsShipped
argument_list|(
name|batchSize
argument_list|)
expr_stmt|;
name|singleSourceSource
operator|.
name|incrShippedBytes
argument_list|(
name|sizeInBytes
argument_list|)
expr_stmt|;
name|globalSourceSource
operator|.
name|incrShippedBytes
argument_list|(
name|sizeInBytes
argument_list|)
expr_stmt|;
block|}
comment|/**    * Convience method to apply changes to metrics do to shipping a batch of logs.    *    * @param batchSize the size of the batch that was shipped to sinks.    * @param hfiles total number of hfiles shipped to sinks.    */
specifier|public
name|void
name|shipBatch
parameter_list|(
name|long
name|batchSize
parameter_list|,
name|int
name|sizeInBytes
parameter_list|,
name|long
name|hfiles
parameter_list|)
block|{
name|shipBatch
argument_list|(
name|batchSize
argument_list|,
name|sizeInBytes
argument_list|)
expr_stmt|;
name|singleSourceSource
operator|.
name|incrHFilesShipped
argument_list|(
name|hfiles
argument_list|)
expr_stmt|;
name|globalSourceSource
operator|.
name|incrHFilesShipped
argument_list|(
name|hfiles
argument_list|)
expr_stmt|;
block|}
comment|/** increase the byte number read by source from log file */
specifier|public
name|void
name|incrLogReadInBytes
parameter_list|(
name|long
name|readInBytes
parameter_list|)
block|{
name|singleSourceSource
operator|.
name|incrLogReadInBytes
argument_list|(
name|readInBytes
argument_list|)
expr_stmt|;
name|globalSourceSource
operator|.
name|incrLogReadInBytes
argument_list|(
name|readInBytes
argument_list|)
expr_stmt|;
block|}
comment|/** Removes all metrics about this Source. */
specifier|public
name|void
name|clear
parameter_list|()
block|{
name|int
name|lastQueueSize
init|=
name|singleSourceSource
operator|.
name|getSizeOfLogQueue
argument_list|()
decl_stmt|;
name|globalSourceSource
operator|.
name|decrSizeOfLogQueue
argument_list|(
name|lastQueueSize
argument_list|)
expr_stmt|;
name|singleSourceSource
operator|.
name|decrSizeOfLogQueue
argument_list|(
name|lastQueueSize
argument_list|)
expr_stmt|;
name|singleSourceSource
operator|.
name|clear
argument_list|()
expr_stmt|;
name|globalSourceSource
operator|.
name|decrSizeOfHFileRefsQueue
argument_list|(
name|lastHFileRefsQueueSize
argument_list|)
expr_stmt|;
name|lastTimestamps
operator|.
name|clear
argument_list|()
expr_stmt|;
name|lastHFileRefsQueueSize
operator|=
literal|0
expr_stmt|;
block|}
comment|/**    * Get AgeOfLastShippedOp    * @return AgeOfLastShippedOp    */
specifier|public
name|Long
name|getAgeOfLastShippedOp
parameter_list|()
block|{
return|return
name|singleSourceSource
operator|.
name|getLastShippedAge
argument_list|()
return|;
block|}
comment|/**    * Get the sizeOfLogQueue    * @return sizeOfLogQueue    */
specifier|public
name|int
name|getSizeOfLogQueue
parameter_list|()
block|{
return|return
name|singleSourceSource
operator|.
name|getSizeOfLogQueue
argument_list|()
return|;
block|}
comment|/**    * Get the timeStampsOfLastShippedOp, if there are multiple groups, return the latest one    * @return lastTimestampForAge    * @deprecated Since 2.0.0. Removed in 3.0.0.    * @see #getTimestampOfLastShippedOp()    */
annotation|@
name|Deprecated
specifier|public
name|long
name|getTimeStampOfLastShippedOp
parameter_list|()
block|{
return|return
name|getTimestampOfLastShippedOp
argument_list|()
return|;
block|}
comment|/**    * Get the timestampsOfLastShippedOp, if there are multiple groups, return the latest one    * @return lastTimestampForAge    */
specifier|public
name|long
name|getTimestampOfLastShippedOp
parameter_list|()
block|{
name|long
name|lastTimestamp
init|=
literal|0L
decl_stmt|;
for|for
control|(
name|long
name|ts
range|:
name|lastTimestamps
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|ts
operator|>
name|lastTimestamp
condition|)
block|{
name|lastTimestamp
operator|=
name|ts
expr_stmt|;
block|}
block|}
return|return
name|lastTimestamp
return|;
block|}
comment|/**    * Get the slave peer ID    * @return peerID    */
specifier|public
name|String
name|getPeerID
parameter_list|()
block|{
return|return
name|id
return|;
block|}
specifier|public
name|void
name|incrSizeOfHFileRefsQueue
parameter_list|(
name|long
name|size
parameter_list|)
block|{
name|singleSourceSource
operator|.
name|incrSizeOfHFileRefsQueue
argument_list|(
name|size
argument_list|)
expr_stmt|;
name|globalSourceSource
operator|.
name|incrSizeOfHFileRefsQueue
argument_list|(
name|size
argument_list|)
expr_stmt|;
name|lastHFileRefsQueueSize
operator|=
name|size
expr_stmt|;
block|}
specifier|public
name|void
name|decrSizeOfHFileRefsQueue
parameter_list|(
name|int
name|size
parameter_list|)
block|{
name|singleSourceSource
operator|.
name|decrSizeOfHFileRefsQueue
argument_list|(
name|size
argument_list|)
expr_stmt|;
name|globalSourceSource
operator|.
name|decrSizeOfHFileRefsQueue
argument_list|(
name|size
argument_list|)
expr_stmt|;
name|lastHFileRefsQueueSize
operator|-=
name|size
expr_stmt|;
if|if
condition|(
name|lastHFileRefsQueueSize
operator|<
literal|0
condition|)
block|{
name|lastHFileRefsQueueSize
operator|=
literal|0
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|incrUnknownFileLengthForClosedWAL
parameter_list|()
block|{
name|singleSourceSource
operator|.
name|incrUnknownFileLengthForClosedWAL
argument_list|()
expr_stmt|;
name|globalSourceSource
operator|.
name|incrUnknownFileLengthForClosedWAL
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|incrUncleanlyClosedWALs
parameter_list|()
block|{
name|singleSourceSource
operator|.
name|incrUncleanlyClosedWALs
argument_list|()
expr_stmt|;
name|globalSourceSource
operator|.
name|incrUncleanlyClosedWALs
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|incrBytesSkippedInUncleanlyClosedWALs
parameter_list|(
specifier|final
name|long
name|bytes
parameter_list|)
block|{
name|singleSourceSource
operator|.
name|incrBytesSkippedInUncleanlyClosedWALs
argument_list|(
name|bytes
argument_list|)
expr_stmt|;
name|globalSourceSource
operator|.
name|incrBytesSkippedInUncleanlyClosedWALs
argument_list|(
name|bytes
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|incrRestartedWALReading
parameter_list|()
block|{
name|singleSourceSource
operator|.
name|incrRestartedWALReading
argument_list|()
expr_stmt|;
name|globalSourceSource
operator|.
name|incrRestartedWALReading
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|incrRepeatedFileBytes
parameter_list|(
specifier|final
name|long
name|bytes
parameter_list|)
block|{
name|singleSourceSource
operator|.
name|incrRepeatedFileBytes
argument_list|(
name|bytes
argument_list|)
expr_stmt|;
name|globalSourceSource
operator|.
name|incrRepeatedFileBytes
argument_list|(
name|bytes
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|incrCompletedWAL
parameter_list|()
block|{
name|singleSourceSource
operator|.
name|incrCompletedWAL
argument_list|()
expr_stmt|;
name|globalSourceSource
operator|.
name|incrCompletedWAL
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|incrCompletedRecoveryQueue
parameter_list|()
block|{
name|singleSourceSource
operator|.
name|incrCompletedRecoveryQueue
argument_list|()
expr_stmt|;
name|globalSourceSource
operator|.
name|incrCompletedRecoveryQueue
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|incrFailedRecoveryQueue
parameter_list|()
block|{
name|globalSourceSource
operator|.
name|incrFailedRecoveryQueue
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|()
block|{
name|singleSourceSource
operator|.
name|init
argument_list|()
expr_stmt|;
name|globalSourceSource
operator|.
name|init
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setGauge
parameter_list|(
name|String
name|gaugeName
parameter_list|,
name|long
name|value
parameter_list|)
block|{
name|singleSourceSource
operator|.
name|setGauge
argument_list|(
name|gaugeName
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|globalSourceSource
operator|.
name|setGauge
argument_list|(
name|gaugeName
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|incGauge
parameter_list|(
name|String
name|gaugeName
parameter_list|,
name|long
name|delta
parameter_list|)
block|{
name|singleSourceSource
operator|.
name|incGauge
argument_list|(
name|gaugeName
argument_list|,
name|delta
argument_list|)
expr_stmt|;
name|globalSourceSource
operator|.
name|incGauge
argument_list|(
name|gaugeName
argument_list|,
name|delta
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|decGauge
parameter_list|(
name|String
name|gaugeName
parameter_list|,
name|long
name|delta
parameter_list|)
block|{
name|singleSourceSource
operator|.
name|decGauge
argument_list|(
name|gaugeName
argument_list|,
name|delta
argument_list|)
expr_stmt|;
name|globalSourceSource
operator|.
name|decGauge
argument_list|(
name|gaugeName
argument_list|,
name|delta
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|removeMetric
parameter_list|(
name|String
name|key
parameter_list|)
block|{
name|singleSourceSource
operator|.
name|removeMetric
argument_list|(
name|key
argument_list|)
expr_stmt|;
name|globalSourceSource
operator|.
name|removeMetric
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|incCounters
parameter_list|(
name|String
name|counterName
parameter_list|,
name|long
name|delta
parameter_list|)
block|{
name|singleSourceSource
operator|.
name|incCounters
argument_list|(
name|counterName
argument_list|,
name|delta
argument_list|)
expr_stmt|;
name|globalSourceSource
operator|.
name|incCounters
argument_list|(
name|counterName
argument_list|,
name|delta
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|updateHistogram
parameter_list|(
name|String
name|name
parameter_list|,
name|long
name|value
parameter_list|)
block|{
name|singleSourceSource
operator|.
name|updateHistogram
argument_list|(
name|name
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|globalSourceSource
operator|.
name|updateHistogram
argument_list|(
name|name
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getMetricsContext
parameter_list|()
block|{
return|return
name|globalSourceSource
operator|.
name|getMetricsContext
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getMetricsDescription
parameter_list|()
block|{
return|return
name|globalSourceSource
operator|.
name|getMetricsDescription
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getMetricsJmxContext
parameter_list|()
block|{
return|return
name|globalSourceSource
operator|.
name|getMetricsJmxContext
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getMetricsName
parameter_list|()
block|{
return|return
name|globalSourceSource
operator|.
name|getMetricsName
argument_list|()
return|;
block|}
annotation|@
name|VisibleForTesting
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|MetricsReplicationSourceSource
argument_list|>
name|getSingleSourceSourceByTable
parameter_list|()
block|{
return|return
name|singleSourceSourceByTable
return|;
block|}
block|}
end_class

end_unit

