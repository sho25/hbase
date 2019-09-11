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
name|master
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
name|LinkedList
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
name|locks
operator|.
name|ReentrantReadWriteLock
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
name|client
operator|.
name|RegionInfo
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
name|TableState
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
name|HbckRegionInfo
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
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceStability
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

begin_comment
comment|/**  * Used to do the hbck checking job at master side.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|HbckChore
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
name|HbckChore
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|HBCK_CHORE_INTERVAL
init|=
literal|"hbase.master.hbck.chore.interval"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|DEFAULT_HBCK_CHORE_INTERVAL
init|=
literal|60
operator|*
literal|60
operator|*
literal|1000
decl_stmt|;
specifier|private
specifier|final
name|MasterServices
name|master
decl_stmt|;
comment|/**    * This map contains the state of all hbck items.  It maps from encoded region    * name to HbckRegionInfo structure.  The information contained in HbckRegionInfo is used    * to detect and correct consistency (hdfs/meta/deployment) problems.    */
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|HbckRegionInfo
argument_list|>
name|regionInfoMap
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|disabledTableRegions
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|splitParentRegions
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
comment|/**    * The regions only opened on RegionServers, but no region info in meta.    */
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|ServerName
argument_list|>
name|orphanRegionsOnRS
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
comment|/**    * The regions have directory on FileSystem, but no region info in meta.    */
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Path
argument_list|>
name|orphanRegionsOnFS
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
comment|/**    * The inconsistent regions. There are three case:    * case 1. Master thought this region opened, but no regionserver reported it.    * case 2. Master thought this region opened on Server1, but regionserver reported Server2    * case 3. More than one regionservers reported opened this region    */
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Pair
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|ServerName
argument_list|>
argument_list|>
argument_list|>
name|inconsistentRegions
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
comment|/**    * The "snapshot" is used to save the last round's HBCK checking report.    */
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|ServerName
argument_list|>
name|orphanRegionsOnRSSnapshot
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Path
argument_list|>
name|orphanRegionsOnFSSnapshot
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Pair
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|ServerName
argument_list|>
argument_list|>
argument_list|>
name|inconsistentRegionsSnapshot
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
comment|/**    * The "snapshot" may be changed after checking. And this checking report "snapshot" may be    * accessed by web ui. Use this rwLock to synchronize.    */
name|ReentrantReadWriteLock
name|rwLock
init|=
operator|new
name|ReentrantReadWriteLock
argument_list|()
decl_stmt|;
comment|/**    * When running, the "snapshot" may be changed when this round's checking finish.    */
specifier|private
specifier|volatile
name|boolean
name|running
init|=
literal|false
decl_stmt|;
specifier|private
specifier|volatile
name|long
name|checkingStartTimestamp
init|=
literal|0
decl_stmt|;
specifier|private
specifier|volatile
name|long
name|checkingEndTimestamp
init|=
literal|0
decl_stmt|;
specifier|private
name|boolean
name|disabled
init|=
literal|false
decl_stmt|;
specifier|public
name|HbckChore
parameter_list|(
name|MasterServices
name|master
parameter_list|)
block|{
name|super
argument_list|(
literal|"HbckChore-"
argument_list|,
name|master
argument_list|,
name|master
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getInt
argument_list|(
name|HBCK_CHORE_INTERVAL
argument_list|,
name|DEFAULT_HBCK_CHORE_INTERVAL
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|master
operator|=
name|master
expr_stmt|;
name|int
name|interval
init|=
name|master
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getInt
argument_list|(
name|HBCK_CHORE_INTERVAL
argument_list|,
name|DEFAULT_HBCK_CHORE_INTERVAL
argument_list|)
decl_stmt|;
if|if
condition|(
name|interval
operator|<=
literal|0
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
name|HBCK_CHORE_INTERVAL
operator|+
literal|" is<=0 hence disabling hbck chore"
argument_list|)
expr_stmt|;
name|disableChore
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|protected
specifier|synchronized
name|void
name|chore
parameter_list|()
block|{
if|if
condition|(
name|isDisabled
argument_list|()
operator|||
name|isRunning
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"hbckChore is either disabled or is already running. Can't run the chore"
argument_list|)
expr_stmt|;
return|return;
block|}
name|running
operator|=
literal|true
expr_stmt|;
name|regionInfoMap
operator|.
name|clear
argument_list|()
expr_stmt|;
name|disabledTableRegions
operator|.
name|clear
argument_list|()
expr_stmt|;
name|splitParentRegions
operator|.
name|clear
argument_list|()
expr_stmt|;
name|orphanRegionsOnRS
operator|.
name|clear
argument_list|()
expr_stmt|;
name|orphanRegionsOnFS
operator|.
name|clear
argument_list|()
expr_stmt|;
name|inconsistentRegions
operator|.
name|clear
argument_list|()
expr_stmt|;
name|checkingStartTimestamp
operator|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
expr_stmt|;
name|loadRegionsFromInMemoryState
argument_list|()
expr_stmt|;
name|loadRegionsFromRSReport
argument_list|()
expr_stmt|;
try|try
block|{
name|loadRegionsFromFS
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
literal|"Failed to load the regions from filesystem"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|saveCheckResultToSnapshot
argument_list|()
expr_stmt|;
name|running
operator|=
literal|false
expr_stmt|;
block|}
comment|// This function does the sanity checks of making sure the chore is not run when it is
comment|// disabled or when it's already running. It returns whether the chore was actually run or not.
specifier|protected
name|boolean
name|runChore
parameter_list|()
block|{
if|if
condition|(
name|isDisabled
argument_list|()
operator|||
name|isRunning
argument_list|()
condition|)
block|{
if|if
condition|(
name|isDisabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"hbck chore is disabled! Set "
operator|+
name|HBCK_CHORE_INTERVAL
operator|+
literal|"> 0 to enable it."
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"hbck chore already running. Can't run till it finishes."
argument_list|)
expr_stmt|;
block|}
return|return
literal|false
return|;
block|}
name|chore
argument_list|()
expr_stmt|;
return|return
literal|true
return|;
block|}
specifier|private
name|void
name|disableChore
parameter_list|()
block|{
name|this
operator|.
name|disabled
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|boolean
name|isDisabled
parameter_list|()
block|{
return|return
name|this
operator|.
name|disabled
return|;
block|}
specifier|private
name|void
name|saveCheckResultToSnapshot
parameter_list|()
block|{
comment|// Need synchronized here, as this "snapshot" may be access by web ui.
name|rwLock
operator|.
name|writeLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|orphanRegionsOnRSSnapshot
operator|.
name|clear
argument_list|()
expr_stmt|;
name|orphanRegionsOnRS
operator|.
name|entrySet
argument_list|()
operator|.
name|forEach
argument_list|(
name|e
lambda|->
name|orphanRegionsOnRSSnapshot
operator|.
name|put
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
expr_stmt|;
name|orphanRegionsOnFSSnapshot
operator|.
name|clear
argument_list|()
expr_stmt|;
name|orphanRegionsOnFS
operator|.
name|entrySet
argument_list|()
operator|.
name|forEach
argument_list|(
name|e
lambda|->
name|orphanRegionsOnFSSnapshot
operator|.
name|put
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
expr_stmt|;
name|inconsistentRegionsSnapshot
operator|.
name|clear
argument_list|()
expr_stmt|;
name|inconsistentRegions
operator|.
name|entrySet
argument_list|()
operator|.
name|forEach
argument_list|(
name|e
lambda|->
name|inconsistentRegionsSnapshot
operator|.
name|put
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
expr_stmt|;
name|checkingEndTimestamp
operator|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|rwLock
operator|.
name|writeLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|loadRegionsFromInMemoryState
parameter_list|()
block|{
name|List
argument_list|<
name|RegionState
argument_list|>
name|regionStates
init|=
name|master
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getRegionStates
argument_list|()
operator|.
name|getRegionStates
argument_list|()
decl_stmt|;
for|for
control|(
name|RegionState
name|regionState
range|:
name|regionStates
control|)
block|{
name|RegionInfo
name|regionInfo
init|=
name|regionState
operator|.
name|getRegion
argument_list|()
decl_stmt|;
if|if
condition|(
name|master
operator|.
name|getTableStateManager
argument_list|()
operator|.
name|isTableState
argument_list|(
name|regionInfo
operator|.
name|getTable
argument_list|()
argument_list|,
name|TableState
operator|.
name|State
operator|.
name|DISABLED
argument_list|)
condition|)
block|{
name|disabledTableRegions
operator|.
name|add
argument_list|(
name|regionInfo
operator|.
name|getEncodedName
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|regionInfo
operator|.
name|isSplitParent
argument_list|()
condition|)
block|{
name|splitParentRegions
operator|.
name|add
argument_list|(
name|regionInfo
operator|.
name|getEncodedName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|HbckRegionInfo
operator|.
name|MetaEntry
name|metaEntry
init|=
operator|new
name|HbckRegionInfo
operator|.
name|MetaEntry
argument_list|(
name|regionInfo
argument_list|,
name|regionState
operator|.
name|getServerName
argument_list|()
argument_list|,
name|regionState
operator|.
name|getStamp
argument_list|()
argument_list|)
decl_stmt|;
name|regionInfoMap
operator|.
name|put
argument_list|(
name|regionInfo
operator|.
name|getEncodedName
argument_list|()
argument_list|,
operator|new
name|HbckRegionInfo
argument_list|(
name|metaEntry
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Loaded {} regions from in-memory state of AssignmentManager"
argument_list|,
name|regionStates
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|loadRegionsFromRSReport
parameter_list|()
block|{
name|int
name|numRegions
init|=
literal|0
decl_stmt|;
name|Map
argument_list|<
name|ServerName
argument_list|,
name|Set
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|>
name|rsReports
init|=
name|master
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getRSReports
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|ServerName
argument_list|,
name|Set
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|>
name|entry
range|:
name|rsReports
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|ServerName
name|serverName
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|regionName
range|:
name|entry
operator|.
name|getValue
argument_list|()
control|)
block|{
name|String
name|encodedRegionName
init|=
name|RegionInfo
operator|.
name|encodeRegionName
argument_list|(
name|regionName
argument_list|)
decl_stmt|;
name|HbckRegionInfo
name|hri
init|=
name|regionInfoMap
operator|.
name|get
argument_list|(
name|encodedRegionName
argument_list|)
decl_stmt|;
if|if
condition|(
name|hri
operator|==
literal|null
condition|)
block|{
name|orphanRegionsOnRS
operator|.
name|put
argument_list|(
name|encodedRegionName
argument_list|,
name|serverName
argument_list|)
expr_stmt|;
continue|continue;
block|}
name|hri
operator|.
name|addServer
argument_list|(
name|hri
operator|.
name|getMetaEntry
argument_list|()
argument_list|,
name|serverName
argument_list|)
expr_stmt|;
block|}
name|numRegions
operator|+=
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|size
argument_list|()
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Loaded {} regions from {} regionservers' reports and found {} orphan regions"
argument_list|,
name|numRegions
argument_list|,
name|rsReports
operator|.
name|size
argument_list|()
argument_list|,
name|orphanRegionsOnFS
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|HbckRegionInfo
argument_list|>
name|entry
range|:
name|regionInfoMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|encodedRegionName
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|HbckRegionInfo
name|hri
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|ServerName
name|locationInMeta
init|=
name|hri
operator|.
name|getMetaEntry
argument_list|()
operator|.
name|getRegionServer
argument_list|()
decl_stmt|;
if|if
condition|(
name|hri
operator|.
name|getDeployedOn
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
comment|// skip the offline region which belong to disabled table.
if|if
condition|(
name|disabledTableRegions
operator|.
name|contains
argument_list|(
name|encodedRegionName
argument_list|)
condition|)
block|{
continue|continue;
block|}
comment|// skip the split parent regions
if|if
condition|(
name|splitParentRegions
operator|.
name|contains
argument_list|(
name|encodedRegionName
argument_list|)
condition|)
block|{
continue|continue;
block|}
comment|// Master thought this region opened, but no regionserver reported it.
name|inconsistentRegions
operator|.
name|put
argument_list|(
name|encodedRegionName
argument_list|,
operator|new
name|Pair
argument_list|<>
argument_list|(
name|locationInMeta
argument_list|,
operator|new
name|LinkedList
argument_list|<>
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|hri
operator|.
name|getDeployedOn
argument_list|()
operator|.
name|size
argument_list|()
operator|>
literal|1
condition|)
block|{
comment|// More than one regionserver reported opened this region
name|inconsistentRegions
operator|.
name|put
argument_list|(
name|encodedRegionName
argument_list|,
operator|new
name|Pair
argument_list|<>
argument_list|(
name|locationInMeta
argument_list|,
name|hri
operator|.
name|getDeployedOn
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
operator|!
name|hri
operator|.
name|getDeployedOn
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|equals
argument_list|(
name|locationInMeta
argument_list|)
condition|)
block|{
comment|// Master thought this region opened on Server1, but regionserver reported Server2
name|inconsistentRegions
operator|.
name|put
argument_list|(
name|encodedRegionName
argument_list|,
operator|new
name|Pair
argument_list|<>
argument_list|(
name|locationInMeta
argument_list|,
name|hri
operator|.
name|getDeployedOn
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|loadRegionsFromFS
parameter_list|()
throws|throws
name|IOException
block|{
name|Path
name|rootDir
init|=
name|master
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|getRootDir
argument_list|()
decl_stmt|;
name|FileSystem
name|fs
init|=
name|master
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|getFileSystem
argument_list|()
decl_stmt|;
name|int
name|numRegions
init|=
literal|0
decl_stmt|;
name|List
argument_list|<
name|Path
argument_list|>
name|tableDirs
init|=
name|FSUtils
operator|.
name|getTableDirs
argument_list|(
name|fs
argument_list|,
name|rootDir
argument_list|)
decl_stmt|;
for|for
control|(
name|Path
name|tableDir
range|:
name|tableDirs
control|)
block|{
name|List
argument_list|<
name|Path
argument_list|>
name|regionDirs
init|=
name|FSUtils
operator|.
name|getRegionDirs
argument_list|(
name|fs
argument_list|,
name|tableDir
argument_list|)
decl_stmt|;
for|for
control|(
name|Path
name|regionDir
range|:
name|regionDirs
control|)
block|{
name|String
name|encodedRegionName
init|=
name|regionDir
operator|.
name|getName
argument_list|()
decl_stmt|;
name|HbckRegionInfo
name|hri
init|=
name|regionInfoMap
operator|.
name|get
argument_list|(
name|encodedRegionName
argument_list|)
decl_stmt|;
if|if
condition|(
name|hri
operator|==
literal|null
condition|)
block|{
name|orphanRegionsOnFS
operator|.
name|put
argument_list|(
name|encodedRegionName
argument_list|,
name|regionDir
argument_list|)
expr_stmt|;
continue|continue;
block|}
name|HbckRegionInfo
operator|.
name|HdfsEntry
name|hdfsEntry
init|=
operator|new
name|HbckRegionInfo
operator|.
name|HdfsEntry
argument_list|(
name|regionDir
argument_list|)
decl_stmt|;
name|hri
operator|.
name|setHdfsEntry
argument_list|(
name|hdfsEntry
argument_list|)
expr_stmt|;
block|}
name|numRegions
operator|+=
name|regionDirs
operator|.
name|size
argument_list|()
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Loaded {} tables {} regions from filesyetem and found {} orphan regions"
argument_list|,
name|tableDirs
operator|.
name|size
argument_list|()
argument_list|,
name|numRegions
argument_list|,
name|orphanRegionsOnFS
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * When running, the HBCK report may be changed later.    */
specifier|public
name|boolean
name|isRunning
parameter_list|()
block|{
return|return
name|running
return|;
block|}
comment|/**    * @return the regions only opened on RegionServers, but no region info in meta.    */
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|ServerName
argument_list|>
name|getOrphanRegionsOnRS
parameter_list|()
block|{
comment|// Need synchronized here, as this "snapshot" may be changed after checking.
name|rwLock
operator|.
name|readLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
return|return
name|this
operator|.
name|orphanRegionsOnRSSnapshot
return|;
block|}
finally|finally
block|{
name|rwLock
operator|.
name|readLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * @return the regions have directory on FileSystem, but no region info in meta.    */
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Path
argument_list|>
name|getOrphanRegionsOnFS
parameter_list|()
block|{
comment|// Need synchronized here, as this "snapshot" may be changed after checking.
name|rwLock
operator|.
name|readLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
return|return
name|this
operator|.
name|orphanRegionsOnFSSnapshot
return|;
block|}
finally|finally
block|{
name|rwLock
operator|.
name|readLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Found the inconsistent regions. There are three case:    * case 1. Master thought this region opened, but no regionserver reported it.    * case 2. Master thought this region opened on Server1, but regionserver reported Server2    * case 3. More than one regionservers reported opened this region    *    * @return the map of inconsistent regions. Key is the region name. Value is a pair of location in    *         meta and the regionservers which reported opened this region.    */
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Pair
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|ServerName
argument_list|>
argument_list|>
argument_list|>
name|getInconsistentRegions
parameter_list|()
block|{
comment|// Need synchronized here, as this "snapshot" may be changed after checking.
name|rwLock
operator|.
name|readLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
return|return
name|this
operator|.
name|inconsistentRegionsSnapshot
return|;
block|}
finally|finally
block|{
name|rwLock
operator|.
name|readLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Used for web ui to show when the HBCK checking started.    */
specifier|public
name|long
name|getCheckingStartTimestamp
parameter_list|()
block|{
return|return
name|this
operator|.
name|checkingStartTimestamp
return|;
block|}
comment|/**    * Used for web ui to show when the HBCK checking report generated.    */
specifier|public
name|long
name|getCheckingEndTimestamp
parameter_list|()
block|{
return|return
name|this
operator|.
name|checkingEndTimestamp
return|;
block|}
block|}
end_class

end_unit

