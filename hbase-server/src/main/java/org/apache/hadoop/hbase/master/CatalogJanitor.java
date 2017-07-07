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
name|master
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileNotFoundException
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
name|Comparator
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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeMap
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
name|atomic
operator|.
name|AtomicBoolean
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
name|atomic
operator|.
name|AtomicInteger
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
name|fs
operator|.
name|Path
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
name|HColumnDescriptor
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
name|HRegionInfo
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
name|HTableDescriptor
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
name|MetaTableAccessor
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
name|master
operator|.
name|assignment
operator|.
name|AssignmentManager
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
name|assignment
operator|.
name|GCMergedRegionsProcedure
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
name|assignment
operator|.
name|GCRegionProcedure
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
name|procedure
operator|.
name|MasterProcedureEnv
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
name|procedure2
operator|.
name|Procedure
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
name|procedure2
operator|.
name|ProcedureExecutor
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
name|HRegionFileSystem
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
name|FSUtils
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
name|Pair
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
name|PairOfSameType
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
name|Threads
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
name|Triple
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
name|shaded
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
comment|/**  * A janitor for the catalog tables.  Scans the<code>hbase:meta</code> catalog  * table on a period looking for unused regions to garbage collect.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|CatalogJanitor
extends|extends
name|ScheduledChore
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
name|CatalogJanitor
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|AtomicBoolean
name|alreadyRunning
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|AtomicBoolean
name|enabled
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|true
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|MasterServices
name|services
decl_stmt|;
specifier|private
specifier|final
name|Connection
name|connection
decl_stmt|;
comment|// PID of the last Procedure launched herein. Keep around for Tests.
name|CatalogJanitor
parameter_list|(
specifier|final
name|MasterServices
name|services
parameter_list|)
block|{
name|super
argument_list|(
literal|"CatalogJanitor-"
operator|+
name|services
operator|.
name|getServerName
argument_list|()
operator|.
name|toShortString
argument_list|()
argument_list|,
name|services
argument_list|,
name|services
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getInt
argument_list|(
literal|"hbase.catalogjanitor.interval"
argument_list|,
literal|300000
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|services
operator|=
name|services
expr_stmt|;
name|this
operator|.
name|connection
operator|=
name|services
operator|.
name|getConnection
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|initialChore
parameter_list|()
block|{
try|try
block|{
if|if
condition|(
name|this
operator|.
name|enabled
operator|.
name|get
argument_list|()
condition|)
name|scan
argument_list|()
expr_stmt|;
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
literal|"Failed initial scan of catalog table"
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
comment|/**    * @param enabled    */
specifier|public
name|boolean
name|setEnabled
parameter_list|(
specifier|final
name|boolean
name|enabled
parameter_list|)
block|{
name|boolean
name|alreadyEnabled
init|=
name|this
operator|.
name|enabled
operator|.
name|getAndSet
argument_list|(
name|enabled
argument_list|)
decl_stmt|;
comment|// If disabling is requested on an already enabled chore, we could have an active
comment|// scan still going on, callers might not be aware of that and do further action thinkng
comment|// that no action would be from this chore.  In this case, the right action is to wait for
comment|// the active scan to complete before exiting this function.
if|if
condition|(
operator|!
name|enabled
operator|&&
name|alreadyEnabled
condition|)
block|{
while|while
condition|(
name|alreadyRunning
operator|.
name|get
argument_list|()
condition|)
block|{
name|Threads
operator|.
name|sleepWithoutInterrupt
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|alreadyEnabled
return|;
block|}
name|boolean
name|getEnabled
parameter_list|()
block|{
return|return
name|this
operator|.
name|enabled
operator|.
name|get
argument_list|()
return|;
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
name|AssignmentManager
name|am
init|=
name|this
operator|.
name|services
operator|.
name|getAssignmentManager
argument_list|()
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|enabled
operator|.
name|get
argument_list|()
operator|&&
operator|!
name|this
operator|.
name|services
operator|.
name|isInMaintenanceMode
argument_list|()
operator|&&
name|am
operator|!=
literal|null
operator|&&
name|am
operator|.
name|isFailoverCleanupDone
argument_list|()
operator|&&
operator|!
name|am
operator|.
name|hasRegionsInTransition
argument_list|()
condition|)
block|{
name|scan
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"CatalogJanitor is disabled! Enabled="
operator|+
name|this
operator|.
name|enabled
operator|.
name|get
argument_list|()
operator|+
literal|", maintenanceMode="
operator|+
name|this
operator|.
name|services
operator|.
name|isInMaintenanceMode
argument_list|()
operator|+
literal|", am="
operator|+
name|am
operator|+
literal|", failoverCleanupDone="
operator|+
operator|(
name|am
operator|!=
literal|null
operator|&&
name|am
operator|.
name|isFailoverCleanupDone
argument_list|()
operator|)
operator|+
literal|", hasRIT="
operator|+
operator|(
name|am
operator|!=
literal|null
operator|&&
name|am
operator|.
name|hasRegionsInTransition
argument_list|()
operator|)
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
literal|"Failed scan of catalog table"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Scans hbase:meta and returns a number of scanned rows, and a map of merged    * regions, and an ordered map of split parents.    * @return triple of scanned rows, map of merged regions and map of split    *         parent regioninfos    * @throws IOException    */
name|Triple
argument_list|<
name|Integer
argument_list|,
name|Map
argument_list|<
name|HRegionInfo
argument_list|,
name|Result
argument_list|>
argument_list|,
name|Map
argument_list|<
name|HRegionInfo
argument_list|,
name|Result
argument_list|>
argument_list|>
name|getMergedRegionsAndSplitParents
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|getMergedRegionsAndSplitParents
argument_list|(
literal|null
argument_list|)
return|;
block|}
comment|/**    * Scans hbase:meta and returns a number of scanned rows, and a map of merged    * regions, and an ordered map of split parents. if the given table name is    * null, return merged regions and split parents of all tables, else only the    * specified table    * @param tableName null represents all tables    * @return triple of scanned rows, and map of merged regions, and map of split    *         parent regioninfos    * @throws IOException    */
name|Triple
argument_list|<
name|Integer
argument_list|,
name|Map
argument_list|<
name|HRegionInfo
argument_list|,
name|Result
argument_list|>
argument_list|,
name|Map
argument_list|<
name|HRegionInfo
argument_list|,
name|Result
argument_list|>
argument_list|>
name|getMergedRegionsAndSplitParents
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|boolean
name|isTableSpecified
init|=
operator|(
name|tableName
operator|!=
literal|null
operator|)
decl_stmt|;
comment|// TODO: Only works with single hbase:meta region currently.  Fix.
specifier|final
name|AtomicInteger
name|count
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
comment|// Keep Map of found split parents.  There are candidates for cleanup.
comment|// Use a comparator that has split parents come before its daughters.
specifier|final
name|Map
argument_list|<
name|HRegionInfo
argument_list|,
name|Result
argument_list|>
name|splitParents
init|=
operator|new
name|TreeMap
argument_list|<>
argument_list|(
operator|new
name|SplitParentFirstComparator
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|Map
argument_list|<
name|HRegionInfo
argument_list|,
name|Result
argument_list|>
name|mergedRegions
init|=
operator|new
name|TreeMap
argument_list|<>
argument_list|()
decl_stmt|;
comment|// This visitor collects split parents and counts rows in the hbase:meta table
name|MetaTableAccessor
operator|.
name|Visitor
name|visitor
init|=
operator|new
name|MetaTableAccessor
operator|.
name|Visitor
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|visit
parameter_list|(
name|Result
name|r
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|r
operator|==
literal|null
operator|||
name|r
operator|.
name|isEmpty
argument_list|()
condition|)
return|return
literal|true
return|;
name|count
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
name|HRegionInfo
name|info
init|=
name|MetaTableAccessor
operator|.
name|getHRegionInfo
argument_list|(
name|r
argument_list|)
decl_stmt|;
if|if
condition|(
name|info
operator|==
literal|null
condition|)
return|return
literal|true
return|;
comment|// Keep scanning
if|if
condition|(
name|isTableSpecified
operator|&&
name|info
operator|.
name|getTable
argument_list|()
operator|.
name|compareTo
argument_list|(
name|tableName
argument_list|)
operator|>
literal|0
condition|)
block|{
comment|// Another table, stop scanning
return|return
literal|false
return|;
block|}
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
name|LOG
operator|.
name|trace
argument_list|(
literal|""
operator|+
name|info
operator|+
literal|" IS-SPLIT_PARENT="
operator|+
name|info
operator|.
name|isSplitParent
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|info
operator|.
name|isSplitParent
argument_list|()
condition|)
name|splitParents
operator|.
name|put
argument_list|(
name|info
argument_list|,
name|r
argument_list|)
expr_stmt|;
if|if
condition|(
name|r
operator|.
name|getValue
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|HConstants
operator|.
name|MERGEA_QUALIFIER
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|mergedRegions
operator|.
name|put
argument_list|(
name|info
argument_list|,
name|r
argument_list|)
expr_stmt|;
block|}
comment|// Returning true means "keep scanning"
return|return
literal|true
return|;
block|}
block|}
decl_stmt|;
comment|// Run full scan of hbase:meta catalog table passing in our custom visitor with
comment|// the start row
name|MetaTableAccessor
operator|.
name|scanMetaForTableRegions
argument_list|(
name|this
operator|.
name|connection
argument_list|,
name|visitor
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
return|return
operator|new
name|Triple
argument_list|<>
argument_list|(
name|count
operator|.
name|get
argument_list|()
argument_list|,
name|mergedRegions
argument_list|,
name|splitParents
argument_list|)
return|;
block|}
comment|/**    * If merged region no longer holds reference to the merge regions, archive    * merge region on hdfs and perform deleting references in hbase:meta    * @param mergedRegion    * @return true if we delete references in merged region on hbase:meta and archive    *         the files on the file system    * @throws IOException    */
name|boolean
name|cleanMergeRegion
parameter_list|(
specifier|final
name|HRegionInfo
name|mergedRegion
parameter_list|,
specifier|final
name|HRegionInfo
name|regionA
parameter_list|,
specifier|final
name|HRegionInfo
name|regionB
parameter_list|)
throws|throws
name|IOException
block|{
name|FileSystem
name|fs
init|=
name|this
operator|.
name|services
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|getFileSystem
argument_list|()
decl_stmt|;
name|Path
name|rootdir
init|=
name|this
operator|.
name|services
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|getRootDir
argument_list|()
decl_stmt|;
name|Path
name|tabledir
init|=
name|FSUtils
operator|.
name|getTableDir
argument_list|(
name|rootdir
argument_list|,
name|mergedRegion
operator|.
name|getTable
argument_list|()
argument_list|)
decl_stmt|;
name|HTableDescriptor
name|htd
init|=
name|getTableDescriptor
argument_list|(
name|mergedRegion
operator|.
name|getTable
argument_list|()
argument_list|)
decl_stmt|;
name|HRegionFileSystem
name|regionFs
init|=
literal|null
decl_stmt|;
try|try
block|{
name|regionFs
operator|=
name|HRegionFileSystem
operator|.
name|openRegionFromFileSystem
argument_list|(
name|this
operator|.
name|services
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|fs
argument_list|,
name|tabledir
argument_list|,
name|mergedRegion
argument_list|,
literal|true
argument_list|)
expr_stmt|;
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
literal|"Merged region does not exist: "
operator|+
name|mergedRegion
operator|.
name|getEncodedName
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|regionFs
operator|==
literal|null
operator|||
operator|!
name|regionFs
operator|.
name|hasReferences
argument_list|(
name|htd
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Deleting region "
operator|+
name|regionA
operator|.
name|getShortNameToLog
argument_list|()
operator|+
literal|" and "
operator|+
name|regionB
operator|.
name|getShortNameToLog
argument_list|()
operator|+
literal|" from fs because merged region no longer holds references"
argument_list|)
expr_stmt|;
name|ProcedureExecutor
argument_list|<
name|MasterProcedureEnv
argument_list|>
name|pe
init|=
name|this
operator|.
name|services
operator|.
name|getMasterProcedureExecutor
argument_list|()
decl_stmt|;
name|pe
operator|.
name|submitProcedure
argument_list|(
operator|new
name|GCMergedRegionsProcedure
argument_list|(
name|pe
operator|.
name|getEnvironment
argument_list|()
argument_list|,
name|mergedRegion
argument_list|,
name|regionA
argument_list|,
name|regionB
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
comment|/**    * Run janitorial scan of catalog<code>hbase:meta</code> table looking for    * garbage to collect.    * @return number of archiving jobs started.    * @throws IOException    */
name|int
name|scan
parameter_list|()
throws|throws
name|IOException
block|{
name|int
name|result
init|=
literal|0
decl_stmt|;
try|try
block|{
if|if
condition|(
operator|!
name|alreadyRunning
operator|.
name|compareAndSet
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"CatalogJanitor already running"
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
name|Triple
argument_list|<
name|Integer
argument_list|,
name|Map
argument_list|<
name|HRegionInfo
argument_list|,
name|Result
argument_list|>
argument_list|,
name|Map
argument_list|<
name|HRegionInfo
argument_list|,
name|Result
argument_list|>
argument_list|>
name|scanTriple
init|=
name|getMergedRegionsAndSplitParents
argument_list|()
decl_stmt|;
comment|/**        * clean merge regions first        */
name|Map
argument_list|<
name|HRegionInfo
argument_list|,
name|Result
argument_list|>
name|mergedRegions
init|=
name|scanTriple
operator|.
name|getSecond
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|HRegionInfo
argument_list|,
name|Result
argument_list|>
name|e
range|:
name|mergedRegions
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|this
operator|.
name|services
operator|.
name|isInMaintenanceMode
argument_list|()
condition|)
block|{
comment|// Stop cleaning if the master is in maintenance mode
break|break;
block|}
name|PairOfSameType
argument_list|<
name|HRegionInfo
argument_list|>
name|p
init|=
name|MetaTableAccessor
operator|.
name|getMergeRegions
argument_list|(
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
decl_stmt|;
name|HRegionInfo
name|regionA
init|=
name|p
operator|.
name|getFirst
argument_list|()
decl_stmt|;
name|HRegionInfo
name|regionB
init|=
name|p
operator|.
name|getSecond
argument_list|()
decl_stmt|;
if|if
condition|(
name|regionA
operator|==
literal|null
operator|||
name|regionB
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Unexpected references regionA="
operator|+
operator|(
name|regionA
operator|==
literal|null
condition|?
literal|"null"
else|:
name|regionA
operator|.
name|getShortNameToLog
argument_list|()
operator|)
operator|+
literal|",regionB="
operator|+
operator|(
name|regionB
operator|==
literal|null
condition|?
literal|"null"
else|:
name|regionB
operator|.
name|getShortNameToLog
argument_list|()
operator|)
operator|+
literal|" in merged region "
operator|+
name|e
operator|.
name|getKey
argument_list|()
operator|.
name|getShortNameToLog
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|cleanMergeRegion
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|,
name|regionA
argument_list|,
name|regionB
argument_list|)
condition|)
block|{
name|result
operator|++
expr_stmt|;
block|}
block|}
block|}
comment|/**        * clean split parents        */
name|Map
argument_list|<
name|HRegionInfo
argument_list|,
name|Result
argument_list|>
name|splitParents
init|=
name|scanTriple
operator|.
name|getThird
argument_list|()
decl_stmt|;
comment|// Now work on our list of found parents. See if any we can clean up.
comment|// regions whose parents are still around
name|HashSet
argument_list|<
name|String
argument_list|>
name|parentNotCleaned
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|HRegionInfo
argument_list|,
name|Result
argument_list|>
name|e
range|:
name|splitParents
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|this
operator|.
name|services
operator|.
name|isInMaintenanceMode
argument_list|()
condition|)
block|{
comment|// Stop cleaning if the master is in maintenance mode
break|break;
block|}
if|if
condition|(
operator|!
name|parentNotCleaned
operator|.
name|contains
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
operator|.
name|getEncodedName
argument_list|()
argument_list|)
operator|&&
name|cleanParent
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
condition|)
block|{
name|result
operator|++
expr_stmt|;
block|}
else|else
block|{
comment|// We could not clean the parent, so it's daughters should not be
comment|// cleaned either (HBASE-6160)
name|PairOfSameType
argument_list|<
name|HRegionInfo
argument_list|>
name|daughters
init|=
name|MetaTableAccessor
operator|.
name|getDaughterRegions
argument_list|(
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
decl_stmt|;
name|parentNotCleaned
operator|.
name|add
argument_list|(
name|daughters
operator|.
name|getFirst
argument_list|()
operator|.
name|getEncodedName
argument_list|()
argument_list|)
expr_stmt|;
name|parentNotCleaned
operator|.
name|add
argument_list|(
name|daughters
operator|.
name|getSecond
argument_list|()
operator|.
name|getEncodedName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|result
return|;
block|}
finally|finally
block|{
name|alreadyRunning
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Compare HRegionInfos in a way that has split parents sort BEFORE their    * daughters.    */
specifier|static
class|class
name|SplitParentFirstComparator
implements|implements
name|Comparator
argument_list|<
name|HRegionInfo
argument_list|>
block|{
name|Comparator
argument_list|<
name|byte
index|[]
argument_list|>
name|rowEndKeyComparator
init|=
operator|new
name|Bytes
operator|.
name|RowEndKeyComparator
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|HRegionInfo
name|left
parameter_list|,
name|HRegionInfo
name|right
parameter_list|)
block|{
comment|// This comparator differs from the one HRegionInfo in that it sorts
comment|// parent before daughters.
if|if
condition|(
name|left
operator|==
literal|null
condition|)
return|return
operator|-
literal|1
return|;
if|if
condition|(
name|right
operator|==
literal|null
condition|)
return|return
literal|1
return|;
comment|// Same table name.
name|int
name|result
init|=
name|left
operator|.
name|getTable
argument_list|()
operator|.
name|compareTo
argument_list|(
name|right
operator|.
name|getTable
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|!=
literal|0
condition|)
return|return
name|result
return|;
comment|// Compare start keys.
name|result
operator|=
name|Bytes
operator|.
name|compareTo
argument_list|(
name|left
operator|.
name|getStartKey
argument_list|()
argument_list|,
name|right
operator|.
name|getStartKey
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|result
operator|!=
literal|0
condition|)
return|return
name|result
return|;
comment|// Compare end keys, but flip the operands so parent comes first
name|result
operator|=
name|rowEndKeyComparator
operator|.
name|compare
argument_list|(
name|right
operator|.
name|getEndKey
argument_list|()
argument_list|,
name|left
operator|.
name|getEndKey
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
block|}
comment|/**    * If daughters no longer hold reference to the parents, delete the parent.    * @param parent HRegionInfo of split offlined parent    * @param rowContent Content of<code>parent</code> row in    *<code>metaRegionName</code>    * @return True if we removed<code>parent</code> from meta table and from    * the filesystem.    * @throws IOException    */
name|boolean
name|cleanParent
parameter_list|(
specifier|final
name|HRegionInfo
name|parent
parameter_list|,
name|Result
name|rowContent
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Check whether it is a merged region and not clean reference
comment|// No necessary to check MERGEB_QUALIFIER because these two qualifiers will
comment|// be inserted/deleted together
if|if
condition|(
name|rowContent
operator|.
name|getValue
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|HConstants
operator|.
name|MERGEA_QUALIFIER
argument_list|)
operator|!=
literal|null
condition|)
block|{
comment|// wait cleaning merge region first
return|return
literal|false
return|;
block|}
comment|// Run checks on each daughter split.
name|PairOfSameType
argument_list|<
name|HRegionInfo
argument_list|>
name|daughters
init|=
name|MetaTableAccessor
operator|.
name|getDaughterRegions
argument_list|(
name|rowContent
argument_list|)
decl_stmt|;
name|Pair
argument_list|<
name|Boolean
argument_list|,
name|Boolean
argument_list|>
name|a
init|=
name|checkDaughterInFs
argument_list|(
name|parent
argument_list|,
name|daughters
operator|.
name|getFirst
argument_list|()
argument_list|)
decl_stmt|;
name|Pair
argument_list|<
name|Boolean
argument_list|,
name|Boolean
argument_list|>
name|b
init|=
name|checkDaughterInFs
argument_list|(
name|parent
argument_list|,
name|daughters
operator|.
name|getSecond
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|hasNoReferences
argument_list|(
name|a
argument_list|)
operator|&&
name|hasNoReferences
argument_list|(
name|b
argument_list|)
condition|)
block|{
name|String
name|daughterA
init|=
name|daughters
operator|.
name|getFirst
argument_list|()
operator|!=
literal|null
condition|?
name|daughters
operator|.
name|getFirst
argument_list|()
operator|.
name|getShortNameToLog
argument_list|()
else|:
literal|"null"
decl_stmt|;
name|String
name|daughterB
init|=
name|daughters
operator|.
name|getSecond
argument_list|()
operator|!=
literal|null
condition|?
name|daughters
operator|.
name|getSecond
argument_list|()
operator|.
name|getShortNameToLog
argument_list|()
else|:
literal|"null"
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Deleting region "
operator|+
name|parent
operator|.
name|getShortNameToLog
argument_list|()
operator|+
literal|" because daughters -- "
operator|+
name|daughterA
operator|+
literal|", "
operator|+
name|daughterB
operator|+
literal|" -- no longer hold references"
argument_list|)
expr_stmt|;
name|ProcedureExecutor
argument_list|<
name|MasterProcedureEnv
argument_list|>
name|pe
init|=
name|this
operator|.
name|services
operator|.
name|getMasterProcedureExecutor
argument_list|()
decl_stmt|;
name|pe
operator|.
name|submitProcedure
argument_list|(
operator|new
name|GCRegionProcedure
argument_list|(
name|pe
operator|.
name|getEnvironment
argument_list|()
argument_list|,
name|parent
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
comment|/**    * @param p A pair where the first boolean says whether or not the daughter    * region directory exists in the filesystem and then the second boolean says    * whether the daughter has references to the parent.    * @return True the passed<code>p</code> signifies no references.    */
specifier|private
name|boolean
name|hasNoReferences
parameter_list|(
specifier|final
name|Pair
argument_list|<
name|Boolean
argument_list|,
name|Boolean
argument_list|>
name|p
parameter_list|)
block|{
return|return
operator|!
name|p
operator|.
name|getFirst
argument_list|()
operator|||
operator|!
name|p
operator|.
name|getSecond
argument_list|()
return|;
block|}
comment|/**    * Checks if a daughter region -- either splitA or splitB -- still holds    * references to parent.    * @param parent Parent region    * @param daughter Daughter region    * @return A pair where the first boolean says whether or not the daughter    * region directory exists in the filesystem and then the second boolean says    * whether the daughter has references to the parent.    * @throws IOException    */
name|Pair
argument_list|<
name|Boolean
argument_list|,
name|Boolean
argument_list|>
name|checkDaughterInFs
parameter_list|(
specifier|final
name|HRegionInfo
name|parent
parameter_list|,
specifier|final
name|HRegionInfo
name|daughter
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|daughter
operator|==
literal|null
condition|)
block|{
return|return
operator|new
name|Pair
argument_list|<>
argument_list|(
name|Boolean
operator|.
name|FALSE
argument_list|,
name|Boolean
operator|.
name|FALSE
argument_list|)
return|;
block|}
name|FileSystem
name|fs
init|=
name|this
operator|.
name|services
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|getFileSystem
argument_list|()
decl_stmt|;
name|Path
name|rootdir
init|=
name|this
operator|.
name|services
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|getRootDir
argument_list|()
decl_stmt|;
name|Path
name|tabledir
init|=
name|FSUtils
operator|.
name|getTableDir
argument_list|(
name|rootdir
argument_list|,
name|daughter
operator|.
name|getTable
argument_list|()
argument_list|)
decl_stmt|;
name|Path
name|daughterRegionDir
init|=
operator|new
name|Path
argument_list|(
name|tabledir
argument_list|,
name|daughter
operator|.
name|getEncodedName
argument_list|()
argument_list|)
decl_stmt|;
name|HRegionFileSystem
name|regionFs
init|=
literal|null
decl_stmt|;
try|try
block|{
if|if
condition|(
operator|!
name|FSUtils
operator|.
name|isExists
argument_list|(
name|fs
argument_list|,
name|daughterRegionDir
argument_list|)
condition|)
block|{
return|return
operator|new
name|Pair
argument_list|<>
argument_list|(
name|Boolean
operator|.
name|FALSE
argument_list|,
name|Boolean
operator|.
name|FALSE
argument_list|)
return|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error trying to determine if daughter region exists, "
operator|+
literal|"assuming exists and has references"
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
return|return
operator|new
name|Pair
argument_list|<>
argument_list|(
name|Boolean
operator|.
name|TRUE
argument_list|,
name|Boolean
operator|.
name|TRUE
argument_list|)
return|;
block|}
name|boolean
name|references
init|=
literal|false
decl_stmt|;
name|HTableDescriptor
name|parentDescriptor
init|=
name|getTableDescriptor
argument_list|(
name|parent
operator|.
name|getTable
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|regionFs
operator|=
name|HRegionFileSystem
operator|.
name|openRegionFromFileSystem
argument_list|(
name|this
operator|.
name|services
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|fs
argument_list|,
name|tabledir
argument_list|,
name|daughter
argument_list|,
literal|true
argument_list|)
expr_stmt|;
for|for
control|(
name|HColumnDescriptor
name|family
range|:
name|parentDescriptor
operator|.
name|getFamilies
argument_list|()
control|)
block|{
if|if
condition|(
operator|(
name|references
operator|=
name|regionFs
operator|.
name|hasReferences
argument_list|(
name|family
operator|.
name|getNameAsString
argument_list|()
argument_list|)
operator|)
condition|)
block|{
break|break;
block|}
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
name|error
argument_list|(
literal|"Error trying to determine referenced files from : "
operator|+
name|daughter
operator|.
name|getEncodedName
argument_list|()
operator|+
literal|", to: "
operator|+
name|parent
operator|.
name|getEncodedName
argument_list|()
operator|+
literal|" assuming has references"
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
operator|new
name|Pair
argument_list|<>
argument_list|(
name|Boolean
operator|.
name|TRUE
argument_list|,
name|Boolean
operator|.
name|TRUE
argument_list|)
return|;
block|}
return|return
operator|new
name|Pair
argument_list|<>
argument_list|(
name|Boolean
operator|.
name|TRUE
argument_list|,
name|Boolean
operator|.
name|valueOf
argument_list|(
name|references
argument_list|)
argument_list|)
return|;
block|}
specifier|private
name|HTableDescriptor
name|getTableDescriptor
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|)
throws|throws
name|FileNotFoundException
throws|,
name|IOException
block|{
return|return
name|this
operator|.
name|services
operator|.
name|getTableDescriptors
argument_list|()
operator|.
name|get
argument_list|(
name|tableName
argument_list|)
return|;
block|}
comment|/**    * Checks if the specified region has merge qualifiers, if so, try to clean    * them    * @param region    * @return true if the specified region doesn't have merge qualifier now    * @throws IOException    */
specifier|public
name|boolean
name|cleanMergeQualifier
parameter_list|(
specifier|final
name|HRegionInfo
name|region
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Get merge regions if it is a merged region and already has merge
comment|// qualifier
name|Pair
argument_list|<
name|HRegionInfo
argument_list|,
name|HRegionInfo
argument_list|>
name|mergeRegions
init|=
name|MetaTableAccessor
operator|.
name|getRegionsFromMergeQualifier
argument_list|(
name|this
operator|.
name|services
operator|.
name|getConnection
argument_list|()
argument_list|,
name|region
operator|.
name|getRegionName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|mergeRegions
operator|==
literal|null
operator|||
operator|(
name|mergeRegions
operator|.
name|getFirst
argument_list|()
operator|==
literal|null
operator|&&
name|mergeRegions
operator|.
name|getSecond
argument_list|()
operator|==
literal|null
operator|)
condition|)
block|{
comment|// It doesn't have merge qualifier, no need to clean
return|return
literal|true
return|;
block|}
comment|// It shouldn't happen, we must insert/delete these two qualifiers together
if|if
condition|(
name|mergeRegions
operator|.
name|getFirst
argument_list|()
operator|==
literal|null
operator|||
name|mergeRegions
operator|.
name|getSecond
argument_list|()
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Merged region "
operator|+
name|region
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|" has only one merge qualifier in META."
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
return|return
name|cleanMergeRegion
argument_list|(
name|region
argument_list|,
name|mergeRegions
operator|.
name|getFirst
argument_list|()
argument_list|,
name|mergeRegions
operator|.
name|getSecond
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

