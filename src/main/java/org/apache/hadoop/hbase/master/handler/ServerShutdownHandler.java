begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
operator|.
name|handler
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
name|NavigableMap
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
name|Server
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
name|catalog
operator|.
name|CatalogTracker
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
name|catalog
operator|.
name|MetaEditor
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
name|catalog
operator|.
name|MetaReader
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
name|executor
operator|.
name|EventHandler
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
name|AssignmentManager
operator|.
name|RegionState
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
name|DeadServer
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
name|MasterServices
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
name|ServerManager
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
name|Writables
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|KeeperException
import|;
end_import

begin_comment
comment|/**  * Process server shutdown.  * Server-to-handle must be already in the deadservers lists.  See  * {@link ServerManager#expireServer(ServerName)}  */
end_comment

begin_class
specifier|public
class|class
name|ServerShutdownHandler
extends|extends
name|EventHandler
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
name|ServerShutdownHandler
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|ServerName
name|serverName
decl_stmt|;
specifier|private
specifier|final
name|MasterServices
name|services
decl_stmt|;
specifier|private
specifier|final
name|DeadServer
name|deadServers
decl_stmt|;
specifier|public
name|ServerShutdownHandler
parameter_list|(
specifier|final
name|Server
name|server
parameter_list|,
specifier|final
name|MasterServices
name|services
parameter_list|,
specifier|final
name|DeadServer
name|deadServers
parameter_list|,
specifier|final
name|ServerName
name|serverName
parameter_list|)
block|{
name|this
argument_list|(
name|server
argument_list|,
name|services
argument_list|,
name|deadServers
argument_list|,
name|serverName
argument_list|,
name|EventType
operator|.
name|M_SERVER_SHUTDOWN
argument_list|)
expr_stmt|;
block|}
name|ServerShutdownHandler
parameter_list|(
specifier|final
name|Server
name|server
parameter_list|,
specifier|final
name|MasterServices
name|services
parameter_list|,
specifier|final
name|DeadServer
name|deadServers
parameter_list|,
specifier|final
name|ServerName
name|serverName
parameter_list|,
name|EventType
name|type
parameter_list|)
block|{
name|super
argument_list|(
name|server
argument_list|,
name|type
argument_list|)
expr_stmt|;
name|this
operator|.
name|serverName
operator|=
name|serverName
expr_stmt|;
name|this
operator|.
name|server
operator|=
name|server
expr_stmt|;
name|this
operator|.
name|services
operator|=
name|services
expr_stmt|;
name|this
operator|.
name|deadServers
operator|=
name|deadServers
expr_stmt|;
if|if
condition|(
operator|!
name|this
operator|.
name|deadServers
operator|.
name|contains
argument_list|(
name|this
operator|.
name|serverName
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
name|this
operator|.
name|serverName
operator|+
literal|" is NOT in deadservers; it should be!"
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Before assign the ROOT region, ensure it haven't     *  been assigned by other place    *<p>    * Under some scenarios, the ROOT region can be opened twice, so it seemed online    * in two regionserver at the same time.    * If the ROOT region has been assigned, so the operation can be canceled.     * @throws InterruptedException    * @throws IOException    * @throws KeeperException    */
specifier|private
name|void
name|verifyAndAssignRoot
parameter_list|()
throws|throws
name|InterruptedException
throws|,
name|IOException
throws|,
name|KeeperException
block|{
name|long
name|timeout
init|=
name|this
operator|.
name|server
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getLong
argument_list|(
literal|"hbase.catalog.verification.timeout"
argument_list|,
literal|1000
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|this
operator|.
name|server
operator|.
name|getCatalogTracker
argument_list|()
operator|.
name|verifyRootRegionLocation
argument_list|(
name|timeout
argument_list|)
condition|)
block|{
name|this
operator|.
name|services
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|assignRoot
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * @return True if the server we are processing was carrying<code>-ROOT-</code>    */
name|boolean
name|isCarryingRoot
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
comment|/**    * @return True if the server we are processing was carrying<code>.META.</code>    */
name|boolean
name|isCarryingMeta
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|String
name|name
init|=
literal|"UnknownServerName"
decl_stmt|;
if|if
condition|(
name|server
operator|!=
literal|null
operator|&&
name|server
operator|.
name|getServerName
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|name
operator|=
name|server
operator|.
name|getServerName
argument_list|()
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
return|return
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|"-"
operator|+
name|name
operator|+
literal|"-"
operator|+
name|getSeqid
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|process
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|ServerName
name|serverName
init|=
name|this
operator|.
name|serverName
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Splitting logs for "
operator|+
name|serverName
argument_list|)
expr_stmt|;
name|this
operator|.
name|services
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|splitLog
argument_list|(
name|serverName
argument_list|)
expr_stmt|;
comment|// Clean out anything in regions in transition.  Being conservative and
comment|// doing after log splitting.  Could do some states before -- OPENING?
comment|// OFFLINE? -- and then others after like CLOSING that depend on log
comment|// splitting.
name|List
argument_list|<
name|RegionState
argument_list|>
name|regionsInTransition
init|=
name|this
operator|.
name|services
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|processServerShutdown
argument_list|(
name|this
operator|.
name|serverName
argument_list|)
decl_stmt|;
comment|// Assign root and meta if we were carrying them.
if|if
condition|(
name|isCarryingRoot
argument_list|()
condition|)
block|{
comment|// -ROOT-
try|try
block|{
name|verifyAndAssignRoot
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
name|this
operator|.
name|server
operator|.
name|abort
argument_list|(
literal|"In server shutdown processing, assigning root"
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Aborting"
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e1
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Interrupted while verifying root region's location"
argument_list|,
name|e1
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
name|e1
argument_list|)
throw|;
block|}
block|}
comment|// Carrying meta?
if|if
condition|(
name|isCarryingMeta
argument_list|()
condition|)
name|this
operator|.
name|services
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|assignMeta
argument_list|()
expr_stmt|;
comment|// Wait on meta to come online; we need it to progress.
comment|// TODO: Best way to hold strictly here?  We should build this retry logic
comment|// into the MetaReader operations themselves.
comment|// TODO: Is the reading of .META. necessary when the Master has state of
comment|// cluster in its head?  It should be possible to do without reading .META.
comment|// in all but one case. On split, the RS updates the .META.
comment|// table and THEN informs the master of the split via zk nodes in
comment|// 'unassigned' dir.  Currently the RS puts ephemeral nodes into zk so if
comment|// the regionserver dies, these nodes do not stick around and this server
comment|// shutdown processing does fixup (see the fixupDaughters method below).
comment|// If we wanted to skip the .META. scan, we'd have to change at least the
comment|// final SPLIT message to be permanent in zk so in here we'd know a SPLIT
comment|// completed (zk is updated after edits to .META. have gone in).  See
comment|// {@link SplitTransaction}.  We'd also have to be figure another way for
comment|// doing the below .META. daughters fixup.
name|NavigableMap
argument_list|<
name|HRegionInfo
argument_list|,
name|Result
argument_list|>
name|hris
init|=
literal|null
decl_stmt|;
while|while
condition|(
operator|!
name|this
operator|.
name|server
operator|.
name|isStopped
argument_list|()
condition|)
block|{
try|try
block|{
name|this
operator|.
name|server
operator|.
name|getCatalogTracker
argument_list|()
operator|.
name|waitForMeta
argument_list|()
expr_stmt|;
name|hris
operator|=
name|MetaReader
operator|.
name|getServerUserRegions
argument_list|(
name|this
operator|.
name|server
operator|.
name|getCatalogTracker
argument_list|()
argument_list|,
name|this
operator|.
name|serverName
argument_list|)
expr_stmt|;
break|break;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Interrupted"
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Received exception accessing META during server shutdown of "
operator|+
name|serverName
operator|+
literal|", retrying META read"
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Skip regions that were in transition unless CLOSING or PENDING_CLOSE
for|for
control|(
name|RegionState
name|rit
range|:
name|regionsInTransition
control|)
block|{
if|if
condition|(
operator|!
name|rit
operator|.
name|isClosing
argument_list|()
operator|&&
operator|!
name|rit
operator|.
name|isPendingClose
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Removed "
operator|+
name|rit
operator|.
name|getRegion
argument_list|()
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|" from list of regions to assign because in RIT"
argument_list|)
expr_stmt|;
name|hris
operator|.
name|remove
argument_list|(
name|rit
operator|.
name|getRegion
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Reassigning "
operator|+
operator|(
name|hris
operator|==
literal|null
condition|?
literal|0
else|:
name|hris
operator|.
name|size
argument_list|()
operator|)
operator|+
literal|" region(s) that "
operator|+
name|serverName
operator|+
literal|" was carrying (skipping "
operator|+
name|regionsInTransition
operator|.
name|size
argument_list|()
operator|+
literal|" regions(s) that are already in transition)"
argument_list|)
expr_stmt|;
comment|// Iterate regions that were on this server and assign them
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
name|hris
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|processDeadRegion
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
argument_list|,
name|this
operator|.
name|services
operator|.
name|getAssignmentManager
argument_list|()
argument_list|,
name|this
operator|.
name|server
operator|.
name|getCatalogTracker
argument_list|()
argument_list|)
condition|)
block|{
name|this
operator|.
name|services
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|assign
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
name|this
operator|.
name|deadServers
operator|.
name|finish
argument_list|(
name|serverName
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Finished processing of shutdown of "
operator|+
name|serverName
argument_list|)
expr_stmt|;
block|}
comment|/**    * Process a dead region from a dead RS.  Checks if the region is disabled    * or if the region has a partially completed split.    * @param hri    * @param result    * @param assignmentManager    * @param catalogTracker    * @return Returns true if specified region should be assigned, false if not.    * @throws IOException    */
specifier|public
specifier|static
name|boolean
name|processDeadRegion
parameter_list|(
name|HRegionInfo
name|hri
parameter_list|,
name|Result
name|result
parameter_list|,
name|AssignmentManager
name|assignmentManager
parameter_list|,
name|CatalogTracker
name|catalogTracker
parameter_list|)
throws|throws
name|IOException
block|{
comment|// If table is not disabled but the region is offlined,
name|boolean
name|disabled
init|=
name|assignmentManager
operator|.
name|getZKTable
argument_list|()
operator|.
name|isDisabledTable
argument_list|(
name|hri
operator|.
name|getTableNameAsString
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|disabled
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|hri
operator|.
name|isOffline
argument_list|()
operator|&&
name|hri
operator|.
name|isSplit
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Offlined and split region "
operator|+
name|hri
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|"; checking daughter presence"
argument_list|)
expr_stmt|;
name|fixupDaughters
argument_list|(
name|result
argument_list|,
name|assignmentManager
argument_list|,
name|catalogTracker
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
comment|/**    * Check that daughter regions are up in .META. and if not, add them.    * @param hris All regions for this server in meta.    * @param result The contents of the parent row in .META.    * @throws IOException    */
specifier|static
name|void
name|fixupDaughters
parameter_list|(
specifier|final
name|Result
name|result
parameter_list|,
specifier|final
name|AssignmentManager
name|assignmentManager
parameter_list|,
specifier|final
name|CatalogTracker
name|catalogTracker
parameter_list|)
throws|throws
name|IOException
block|{
name|fixupDaughter
argument_list|(
name|result
argument_list|,
name|HConstants
operator|.
name|SPLITA_QUALIFIER
argument_list|,
name|assignmentManager
argument_list|,
name|catalogTracker
argument_list|)
expr_stmt|;
name|fixupDaughter
argument_list|(
name|result
argument_list|,
name|HConstants
operator|.
name|SPLITB_QUALIFIER
argument_list|,
name|assignmentManager
argument_list|,
name|catalogTracker
argument_list|)
expr_stmt|;
block|}
comment|/**    * Check individual daughter is up in .META.; fixup if its not.    * @param result The contents of the parent row in .META.    * @param qualifier Which daughter to check for.    * @throws IOException    */
specifier|static
name|void
name|fixupDaughter
parameter_list|(
specifier|final
name|Result
name|result
parameter_list|,
specifier|final
name|byte
index|[]
name|qualifier
parameter_list|,
specifier|final
name|AssignmentManager
name|assignmentManager
parameter_list|,
specifier|final
name|CatalogTracker
name|catalogTracker
parameter_list|)
throws|throws
name|IOException
block|{
name|HRegionInfo
name|daughter
init|=
name|getHRegionInfo
argument_list|(
name|result
argument_list|,
name|qualifier
argument_list|)
decl_stmt|;
if|if
condition|(
name|daughter
operator|==
literal|null
condition|)
return|return;
if|if
condition|(
name|isDaughterMissing
argument_list|(
name|catalogTracker
argument_list|,
name|daughter
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Fixup; missing daughter "
operator|+
name|daughter
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|)
expr_stmt|;
name|MetaEditor
operator|.
name|addDaughter
argument_list|(
name|catalogTracker
argument_list|,
name|daughter
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|// And assign it.
name|assignmentManager
operator|.
name|assign
argument_list|(
name|daughter
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Daughter "
operator|+
name|daughter
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|" present"
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Interpret the content of the cell at {@link HConstants#CATALOG_FAMILY} and    *<code>qualifier</code> as an HRegionInfo and return it, or null.    * @param r Result instance to pull from.    * @param qualifier Column family qualifier    * @return An HRegionInfo instance or null.    * @throws IOException    */
specifier|private
specifier|static
name|HRegionInfo
name|getHRegionInfo
parameter_list|(
specifier|final
name|Result
name|r
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
index|[]
name|bytes
init|=
name|r
operator|.
name|getValue
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|qualifier
argument_list|)
decl_stmt|;
if|if
condition|(
name|bytes
operator|==
literal|null
operator|||
name|bytes
operator|.
name|length
operator|<=
literal|0
condition|)
return|return
literal|null
return|;
return|return
name|Writables
operator|.
name|getHRegionInfoOrNull
argument_list|(
name|bytes
argument_list|)
return|;
block|}
comment|/**    * Look for presence of the daughter OR of a split of the daughter. Daughter    * could have been split over on regionserver before a run of the    * catalogJanitor had chance to clear reference from parent.    * @param daughter Daughter region to search for.    * @throws IOException     */
specifier|private
specifier|static
name|boolean
name|isDaughterMissing
parameter_list|(
specifier|final
name|CatalogTracker
name|catalogTracker
parameter_list|,
specifier|final
name|HRegionInfo
name|daughter
parameter_list|)
throws|throws
name|IOException
block|{
name|FindDaughterVisitor
name|visitor
init|=
operator|new
name|FindDaughterVisitor
argument_list|(
name|daughter
argument_list|)
decl_stmt|;
comment|// Start the scan at what should be the daughter's row in the .META.
comment|// We will either 1., find the daughter or some derivative split of the
comment|// daughter (will have same table name and start row at least but will sort
comment|// after because has larger regionid -- the regionid is timestamp of region
comment|// creation), OR, we will not find anything with same table name and start
comment|// row.  If the latter, then assume daughter missing and do fixup.
name|byte
index|[]
name|startrow
init|=
name|daughter
operator|.
name|getRegionName
argument_list|()
decl_stmt|;
name|MetaReader
operator|.
name|fullScan
argument_list|(
name|catalogTracker
argument_list|,
name|visitor
argument_list|,
name|startrow
argument_list|)
expr_stmt|;
return|return
operator|!
name|visitor
operator|.
name|foundDaughter
argument_list|()
return|;
block|}
comment|/**    * Looks for daughter.  Sets a flag if daughter or some progeny of daughter    * is found up in<code>.META.</code>.    */
specifier|static
class|class
name|FindDaughterVisitor
implements|implements
name|MetaReader
operator|.
name|Visitor
block|{
specifier|private
specifier|final
name|HRegionInfo
name|daughter
decl_stmt|;
specifier|private
name|boolean
name|found
init|=
literal|false
decl_stmt|;
name|FindDaughterVisitor
parameter_list|(
specifier|final
name|HRegionInfo
name|daughter
parameter_list|)
block|{
name|this
operator|.
name|daughter
operator|=
name|daughter
expr_stmt|;
block|}
comment|/**      * @return True if we found a daughter region during our visiting.      */
name|boolean
name|foundDaughter
parameter_list|()
block|{
return|return
name|this
operator|.
name|found
return|;
block|}
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
name|HRegionInfo
name|hri
init|=
name|getHRegionInfo
argument_list|(
name|r
argument_list|,
name|HConstants
operator|.
name|REGIONINFO_QUALIFIER
argument_list|)
decl_stmt|;
if|if
condition|(
name|hri
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"No serialized HRegionInfo in "
operator|+
name|r
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
name|byte
index|[]
name|value
init|=
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
name|SERVER_QUALIFIER
argument_list|)
decl_stmt|;
comment|// See if daughter is assigned to some server
if|if
condition|(
name|value
operator|==
literal|null
condition|)
return|return
literal|false
return|;
comment|// Now see if we have gone beyond the daughter's startrow.
if|if
condition|(
operator|!
name|Bytes
operator|.
name|equals
argument_list|(
name|daughter
operator|.
name|getTableName
argument_list|()
argument_list|,
name|hri
operator|.
name|getTableName
argument_list|()
argument_list|)
condition|)
block|{
comment|// We fell into another table.  Stop scanning.
return|return
literal|false
return|;
block|}
comment|// If our start rows do not compare, move on.
if|if
condition|(
operator|!
name|Bytes
operator|.
name|equals
argument_list|(
name|daughter
operator|.
name|getStartKey
argument_list|()
argument_list|,
name|hri
operator|.
name|getStartKey
argument_list|()
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
comment|// Else, table name and start rows compare.  It means that the daughter
comment|// or some derivative split of the daughter is up in .META.  Daughter
comment|// exists.
name|this
operator|.
name|found
operator|=
literal|true
expr_stmt|;
return|return
literal|false
return|;
block|}
block|}
block|}
end_class

end_unit

