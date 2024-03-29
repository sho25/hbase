begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements.  See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to you under the Apache License, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|quotas
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
name|Collection
import|;
end_import

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
name|HashSet
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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
operator|.
name|Entry
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
name|TimeUnit
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
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
name|fs
operator|.
name|FileSystem
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
name|ScheduledChore
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
name|Stoppable
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
name|Delete
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
name|master
operator|.
name|HMaster
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
name|master
operator|.
name|MetricsMaster
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
name|HashMultimap
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
name|Multimap
import|;
end_import

begin_comment
comment|/**  * A Master-invoked {@code Chore} that computes the size of each snapshot which was created from  * a table which has a space quota.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|SnapshotQuotaObserverChore
extends|extends
name|ScheduledChore
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
name|SnapshotQuotaObserverChore
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|String
name|SNAPSHOT_QUOTA_CHORE_PERIOD_KEY
init|=
literal|"hbase.master.quotas.snapshot.chore.period"
decl_stmt|;
specifier|static
specifier|final
name|int
name|SNAPSHOT_QUOTA_CHORE_PERIOD_DEFAULT
init|=
literal|1000
operator|*
literal|60
operator|*
literal|5
decl_stmt|;
comment|// 5 minutes in millis
specifier|static
specifier|final
name|String
name|SNAPSHOT_QUOTA_CHORE_DELAY_KEY
init|=
literal|"hbase.master.quotas.snapshot.chore.delay"
decl_stmt|;
specifier|static
specifier|final
name|long
name|SNAPSHOT_QUOTA_CHORE_DELAY_DEFAULT
init|=
literal|1000L
operator|*
literal|60L
decl_stmt|;
comment|// 1 minute in millis
specifier|static
specifier|final
name|String
name|SNAPSHOT_QUOTA_CHORE_TIMEUNIT_KEY
init|=
literal|"hbase.master.quotas.snapshot.chore.timeunit"
decl_stmt|;
specifier|static
specifier|final
name|String
name|SNAPSHOT_QUOTA_CHORE_TIMEUNIT_DEFAULT
init|=
name|TimeUnit
operator|.
name|MILLISECONDS
operator|.
name|name
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Connection
name|conn
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|MetricsMaster
name|metrics
decl_stmt|;
specifier|private
specifier|final
name|FileSystem
name|fs
decl_stmt|;
specifier|public
name|SnapshotQuotaObserverChore
parameter_list|(
name|HMaster
name|master
parameter_list|,
name|MetricsMaster
name|metrics
parameter_list|)
block|{
name|this
argument_list|(
name|master
operator|.
name|getConnection
argument_list|()
argument_list|,
name|master
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|master
operator|.
name|getFileSystem
argument_list|()
argument_list|,
name|master
argument_list|,
name|metrics
argument_list|)
expr_stmt|;
block|}
name|SnapshotQuotaObserverChore
parameter_list|(
name|Connection
name|conn
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Stoppable
name|stopper
parameter_list|,
name|MetricsMaster
name|metrics
parameter_list|)
block|{
name|super
argument_list|(
name|QuotaObserverChore
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|,
name|stopper
argument_list|,
name|getPeriod
argument_list|(
name|conf
argument_list|)
argument_list|,
name|getInitialDelay
argument_list|(
name|conf
argument_list|)
argument_list|,
name|getTimeUnit
argument_list|(
name|conf
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|conn
operator|=
name|conn
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|metrics
operator|=
name|metrics
expr_stmt|;
name|this
operator|.
name|fs
operator|=
name|fs
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|chore
parameter_list|()
block|{
try|try
block|{
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Computing sizes of snapshots for quota management."
argument_list|)
expr_stmt|;
block|}
name|long
name|start
init|=
name|System
operator|.
name|nanoTime
argument_list|()
decl_stmt|;
name|_chore
argument_list|()
expr_stmt|;
if|if
condition|(
literal|null
operator|!=
name|metrics
condition|)
block|{
name|metrics
operator|.
name|incrementSnapshotObserverTime
argument_list|(
operator|(
name|System
operator|.
name|nanoTime
argument_list|()
operator|-
name|start
operator|)
operator|/
literal|1_000_000
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to compute the size of snapshots, will retry"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
name|void
name|_chore
parameter_list|()
throws|throws
name|IOException
block|{
comment|// Gets all tables with quotas that also have snapshots.
comment|// This values are all of the snapshots that we need to compute the size of.
name|long
name|start
init|=
name|System
operator|.
name|nanoTime
argument_list|()
decl_stmt|;
name|Multimap
argument_list|<
name|TableName
argument_list|,
name|String
argument_list|>
name|snapshotsToComputeSize
init|=
name|getSnapshotsToComputeSize
argument_list|()
decl_stmt|;
if|if
condition|(
literal|null
operator|!=
name|metrics
condition|)
block|{
name|metrics
operator|.
name|incrementSnapshotFetchTime
argument_list|(
operator|(
name|System
operator|.
name|nanoTime
argument_list|()
operator|-
name|start
operator|)
operator|/
literal|1_000_000
argument_list|)
expr_stmt|;
block|}
comment|// Remove old table snapshots data
name|pruneTableSnapshots
argument_list|(
name|snapshotsToComputeSize
argument_list|)
expr_stmt|;
comment|// Remove old namespace snapshots data
name|pruneNamespaceSnapshots
argument_list|(
name|snapshotsToComputeSize
argument_list|)
expr_stmt|;
comment|// For each table, compute the size of each snapshot
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|namespaceSnapshotSizes
init|=
name|computeSnapshotSizes
argument_list|(
name|snapshotsToComputeSize
argument_list|)
decl_stmt|;
comment|// Write the size data by namespaces to the quota table.
comment|// We need to do this "globally" since each FileArchiverNotifier is limited to its own Table.
name|persistSnapshotSizesForNamespaces
argument_list|(
name|namespaceSnapshotSizes
argument_list|)
expr_stmt|;
block|}
comment|/**    * Removes the snapshot entries that are present in Quota table but not in snapshotsToComputeSize    *    * @param snapshotsToComputeSize list of snapshots to be persisted    */
name|void
name|pruneTableSnapshots
parameter_list|(
name|Multimap
argument_list|<
name|TableName
argument_list|,
name|String
argument_list|>
name|snapshotsToComputeSize
parameter_list|)
throws|throws
name|IOException
block|{
name|Multimap
argument_list|<
name|TableName
argument_list|,
name|String
argument_list|>
name|existingSnapshotEntries
init|=
name|QuotaTableUtil
operator|.
name|getTableSnapshots
argument_list|(
name|conn
argument_list|)
decl_stmt|;
name|Multimap
argument_list|<
name|TableName
argument_list|,
name|String
argument_list|>
name|snapshotEntriesToRemove
init|=
name|HashMultimap
operator|.
name|create
argument_list|()
decl_stmt|;
for|for
control|(
name|Entry
argument_list|<
name|TableName
argument_list|,
name|Collection
argument_list|<
name|String
argument_list|>
argument_list|>
name|entry
range|:
name|existingSnapshotEntries
operator|.
name|asMap
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|TableName
name|tn
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|setOfSnapshots
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|snapshot
range|:
name|snapshotsToComputeSize
operator|.
name|get
argument_list|(
name|tn
argument_list|)
control|)
block|{
name|setOfSnapshots
operator|.
name|remove
argument_list|(
name|snapshot
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|String
name|snapshot
range|:
name|setOfSnapshots
control|)
block|{
name|snapshotEntriesToRemove
operator|.
name|put
argument_list|(
name|tn
argument_list|,
name|snapshot
argument_list|)
expr_stmt|;
block|}
block|}
name|removeExistingTableSnapshotSizes
argument_list|(
name|snapshotEntriesToRemove
argument_list|)
expr_stmt|;
block|}
comment|/**    * Removes the snapshot entries that are present in Quota table but not in snapshotsToComputeSize    *    * @param snapshotsToComputeSize list of snapshots to be persisted    */
name|void
name|pruneNamespaceSnapshots
parameter_list|(
name|Multimap
argument_list|<
name|TableName
argument_list|,
name|String
argument_list|>
name|snapshotsToComputeSize
parameter_list|)
throws|throws
name|IOException
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|existingSnapshotEntries
init|=
name|QuotaTableUtil
operator|.
name|getNamespaceSnapshots
argument_list|(
name|conn
argument_list|)
decl_stmt|;
for|for
control|(
name|TableName
name|tableName
range|:
name|snapshotsToComputeSize
operator|.
name|keySet
argument_list|()
control|)
block|{
name|existingSnapshotEntries
operator|.
name|remove
argument_list|(
name|tableName
operator|.
name|getNamespaceAsString
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// here existingSnapshotEntries is left with the entries to be removed
name|removeExistingNamespaceSnapshotSizes
argument_list|(
name|existingSnapshotEntries
argument_list|)
expr_stmt|;
block|}
comment|/**    * Fetches each table with a quota (table or namespace quota), and then fetch the name of each    * snapshot which was created from that table.    *    * @return A mapping of table to snapshots created from that table    */
name|Multimap
argument_list|<
name|TableName
argument_list|,
name|String
argument_list|>
name|getSnapshotsToComputeSize
parameter_list|()
throws|throws
name|IOException
block|{
name|Set
argument_list|<
name|TableName
argument_list|>
name|tablesToFetchSnapshotsFrom
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|QuotaFilter
name|filter
init|=
operator|new
name|QuotaFilter
argument_list|()
decl_stmt|;
name|filter
operator|.
name|addTypeFilter
argument_list|(
name|QuotaType
operator|.
name|SPACE
argument_list|)
expr_stmt|;
try|try
init|(
name|Admin
name|admin
init|=
name|conn
operator|.
name|getAdmin
argument_list|()
init|)
block|{
comment|// Pull all of the tables that have quotas (direct, or from namespace)
for|for
control|(
name|QuotaSettings
name|qs
range|:
name|QuotaRetriever
operator|.
name|open
argument_list|(
name|conf
argument_list|,
name|filter
argument_list|)
control|)
block|{
if|if
condition|(
name|qs
operator|.
name|getQuotaType
argument_list|()
operator|==
name|QuotaType
operator|.
name|SPACE
condition|)
block|{
name|String
name|ns
init|=
name|qs
operator|.
name|getNamespace
argument_list|()
decl_stmt|;
name|TableName
name|tn
init|=
name|qs
operator|.
name|getTableName
argument_list|()
decl_stmt|;
if|if
condition|(
operator|(
literal|null
operator|==
name|ns
operator|&&
literal|null
operator|==
name|tn
operator|)
operator|||
operator|(
literal|null
operator|!=
name|ns
operator|&&
literal|null
operator|!=
name|tn
operator|)
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Expected either one of namespace and tablename to be null but not both"
argument_list|)
throw|;
block|}
comment|// Collect either the table name itself, or all of the tables in the namespace
if|if
condition|(
literal|null
operator|!=
name|ns
condition|)
block|{
name|tablesToFetchSnapshotsFrom
operator|.
name|addAll
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|admin
operator|.
name|listTableNamesByNamespace
argument_list|(
name|ns
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|tablesToFetchSnapshotsFrom
operator|.
name|add
argument_list|(
name|tn
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// Fetch all snapshots that were created from these tables
return|return
name|getSnapshotsFromTables
argument_list|(
name|admin
argument_list|,
name|tablesToFetchSnapshotsFrom
argument_list|)
return|;
block|}
block|}
comment|/**    * Computes a mapping of originating {@code TableName} to snapshots, when the {@code TableName}    * exists in the provided {@code Set}.    */
name|Multimap
argument_list|<
name|TableName
argument_list|,
name|String
argument_list|>
name|getSnapshotsFromTables
parameter_list|(
name|Admin
name|admin
parameter_list|,
name|Set
argument_list|<
name|TableName
argument_list|>
name|tablesToFetchSnapshotsFrom
parameter_list|)
throws|throws
name|IOException
block|{
name|Multimap
argument_list|<
name|TableName
argument_list|,
name|String
argument_list|>
name|snapshotsToCompute
init|=
name|HashMultimap
operator|.
name|create
argument_list|()
decl_stmt|;
for|for
control|(
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
name|SnapshotDescription
name|sd
range|:
name|admin
operator|.
name|listSnapshots
argument_list|()
control|)
block|{
name|TableName
name|tn
init|=
name|sd
operator|.
name|getTableName
argument_list|()
decl_stmt|;
if|if
condition|(
name|tablesToFetchSnapshotsFrom
operator|.
name|contains
argument_list|(
name|tn
argument_list|)
condition|)
block|{
name|snapshotsToCompute
operator|.
name|put
argument_list|(
name|tn
argument_list|,
name|sd
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|snapshotsToCompute
return|;
block|}
comment|/**    * Computes the size of each snapshot provided given the current files referenced by the table.    *    * @param snapshotsToComputeSize The snapshots to compute the size of    * @return A mapping of table to snapshot created from that table and the snapshot's size.    */
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|computeSnapshotSizes
parameter_list|(
name|Multimap
argument_list|<
name|TableName
argument_list|,
name|String
argument_list|>
name|snapshotsToComputeSize
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|snapshotSizesByNamespace
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|final
name|long
name|start
init|=
name|System
operator|.
name|nanoTime
argument_list|()
decl_stmt|;
for|for
control|(
name|Entry
argument_list|<
name|TableName
argument_list|,
name|Collection
argument_list|<
name|String
argument_list|>
argument_list|>
name|entry
range|:
name|snapshotsToComputeSize
operator|.
name|asMap
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
specifier|final
name|TableName
name|tn
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
specifier|final
name|Collection
argument_list|<
name|String
argument_list|>
name|snapshotNames
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
comment|// Get our notifier instance, this is tracking archivals that happen out-of-band of this chore
name|FileArchiverNotifier
name|notifier
init|=
name|getNotifierForTable
argument_list|(
name|tn
argument_list|)
decl_stmt|;
comment|// The total size consumed by all snapshots against this table
name|long
name|totalSnapshotSize
init|=
name|notifier
operator|.
name|computeAndStoreSnapshotSizes
argument_list|(
name|snapshotNames
argument_list|)
decl_stmt|;
comment|// Bucket that size into the appropriate namespace
name|snapshotSizesByNamespace
operator|.
name|merge
argument_list|(
name|tn
operator|.
name|getNamespaceAsString
argument_list|()
argument_list|,
name|totalSnapshotSize
argument_list|,
name|Long
operator|::
name|sum
argument_list|)
expr_stmt|;
block|}
comment|// Update the amount of time it took to compute the size of the snapshots for a table
if|if
condition|(
name|metrics
operator|!=
literal|null
condition|)
block|{
name|metrics
operator|.
name|incrementSnapshotSizeComputationTime
argument_list|(
operator|(
name|System
operator|.
name|nanoTime
argument_list|()
operator|-
name|start
operator|)
operator|/
literal|1_000_000
argument_list|)
expr_stmt|;
block|}
return|return
name|snapshotSizesByNamespace
return|;
block|}
comment|/**    * Returns the correct instance of {@link FileArchiverNotifier} for the given table name.    *    * @param tn The table name    * @return A {@link FileArchiverNotifier} instance    */
name|FileArchiverNotifier
name|getNotifierForTable
parameter_list|(
name|TableName
name|tn
parameter_list|)
block|{
return|return
name|FileArchiverNotifierFactoryImpl
operator|.
name|getInstance
argument_list|()
operator|.
name|get
argument_list|(
name|conn
argument_list|,
name|conf
argument_list|,
name|fs
argument_list|,
name|tn
argument_list|)
return|;
block|}
comment|/**    * Writes the size used by snapshots for each namespace to the quota table.    */
name|void
name|persistSnapshotSizesForNamespaces
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|snapshotSizesByNamespace
parameter_list|)
throws|throws
name|IOException
block|{
try|try
init|(
name|Table
name|quotaTable
init|=
name|conn
operator|.
name|getTable
argument_list|(
name|QuotaUtil
operator|.
name|QUOTA_TABLE_NAME
argument_list|)
init|)
block|{
name|quotaTable
operator|.
name|put
argument_list|(
name|snapshotSizesByNamespace
operator|.
name|entrySet
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|e
lambda|->
name|QuotaTableUtil
operator|.
name|createPutForNamespaceSnapshotSize
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|,
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|void
name|removeExistingTableSnapshotSizes
parameter_list|(
name|Multimap
argument_list|<
name|TableName
argument_list|,
name|String
argument_list|>
name|snapshotEntriesToRemove
parameter_list|)
throws|throws
name|IOException
block|{
name|removeExistingSnapshotSizes
argument_list|(
name|QuotaTableUtil
operator|.
name|createDeletesForExistingTableSnapshotSizes
argument_list|(
name|snapshotEntriesToRemove
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|void
name|removeExistingNamespaceSnapshotSizes
parameter_list|(
name|Set
argument_list|<
name|String
argument_list|>
name|snapshotEntriesToRemove
parameter_list|)
throws|throws
name|IOException
block|{
name|removeExistingSnapshotSizes
argument_list|(
name|QuotaTableUtil
operator|.
name|createDeletesForExistingNamespaceSnapshotSizes
argument_list|(
name|snapshotEntriesToRemove
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|void
name|removeExistingSnapshotSizes
parameter_list|(
name|List
argument_list|<
name|Delete
argument_list|>
name|deletes
parameter_list|)
throws|throws
name|IOException
block|{
try|try
init|(
name|Table
name|quotaTable
init|=
name|conn
operator|.
name|getTable
argument_list|(
name|QuotaUtil
operator|.
name|QUOTA_TABLE_NAME
argument_list|)
init|)
block|{
name|quotaTable
operator|.
name|delete
argument_list|(
name|deletes
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Extracts the period for the chore from the configuration.    *    * @param conf The configuration object.    * @return The configured chore period or the default value.    */
specifier|static
name|int
name|getPeriod
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
return|return
name|conf
operator|.
name|getInt
argument_list|(
name|SNAPSHOT_QUOTA_CHORE_PERIOD_KEY
argument_list|,
name|SNAPSHOT_QUOTA_CHORE_PERIOD_DEFAULT
argument_list|)
return|;
block|}
comment|/**    * Extracts the initial delay for the chore from the configuration.    *    * @param conf The configuration object.    * @return The configured chore initial delay or the default value.    */
specifier|static
name|long
name|getInitialDelay
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
return|return
name|conf
operator|.
name|getLong
argument_list|(
name|SNAPSHOT_QUOTA_CHORE_DELAY_KEY
argument_list|,
name|SNAPSHOT_QUOTA_CHORE_DELAY_DEFAULT
argument_list|)
return|;
block|}
comment|/**    * Extracts the time unit for the chore period and initial delay from the configuration. The    * configuration value for {@link #SNAPSHOT_QUOTA_CHORE_TIMEUNIT_KEY} must correspond to    * a {@link TimeUnit} value.    *    * @param conf The configuration object.    * @return The configured time unit for the chore period and initial delay or the default value.    */
specifier|static
name|TimeUnit
name|getTimeUnit
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
return|return
name|TimeUnit
operator|.
name|valueOf
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|SNAPSHOT_QUOTA_CHORE_TIMEUNIT_KEY
argument_list|,
name|SNAPSHOT_QUOTA_CHORE_TIMEUNIT_DEFAULT
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit

