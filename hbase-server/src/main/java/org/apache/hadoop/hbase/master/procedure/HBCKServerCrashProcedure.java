begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|procedure
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
name|RegionLocations
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
name|assignment
operator|.
name|RegionStateStore
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

begin_comment
comment|/**  * Acts like the super class in all cases except when no Regions found in the  * current Master in-memory context. In this latter case, when the call to  * super#getRegionsOnCrashedServer returns nothing, this SCP will scan  * hbase:meta for references to the passed ServerName. If any found, we'll  * clean them up.  *  *<p>This version of SCP is for external invocation as part of fix-up (e.g. HBCK2's  * scheduleRecoveries); the super class is used during normal recovery operations.  * It is for the case where meta has references to 'Unknown Servers',  * servers that are in hbase:meta but not in live-server or dead-server lists; i.e. Master  * and hbase:meta content have deviated. It should never happen in normal running  * cluster but if we do drop accounting of servers, we need a means of fix-up.  * Eventually, as part of normal CatalogJanitor task, rather than just identify  * these 'Unknown Servers', it would make repair, queuing something like this  * HBCKSCP to do cleanup, reassigning them so Master and hbase:meta are aligned again.  *  *<p>NOTE that this SCP is costly to run; does a full scan of hbase:meta.</p>  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|HBCKServerCrashProcedure
extends|extends
name|ServerCrashProcedure
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
name|HBCKServerCrashProcedure
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * @param serverName Name of the crashed server.    * @param shouldSplitWal True if we should split WALs as part of crashed server processing.    * @param carryingMeta True if carrying hbase:meta table region.    */
specifier|public
name|HBCKServerCrashProcedure
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|ServerName
name|serverName
parameter_list|,
specifier|final
name|boolean
name|shouldSplitWal
parameter_list|,
specifier|final
name|boolean
name|carryingMeta
parameter_list|)
block|{
name|super
argument_list|(
name|env
argument_list|,
name|serverName
argument_list|,
name|shouldSplitWal
argument_list|,
name|carryingMeta
argument_list|)
expr_stmt|;
block|}
comment|/**    * Used when deserializing from a procedure store; we'll construct one of these then call    * #deserializeStateData(InputStream). Do not use directly.    */
specifier|public
name|HBCKServerCrashProcedure
parameter_list|()
block|{}
comment|/**    * If no Regions found in Master context, then we will search hbase:meta for references    * to the passed server. Operator may have passed ServerName because they have found    * references to 'Unknown Servers'. They are using HBCKSCP to clear them out.    */
annotation|@
name|Override
annotation|@
name|edu
operator|.
name|umd
operator|.
name|cs
operator|.
name|findbugs
operator|.
name|annotations
operator|.
name|SuppressWarnings
argument_list|(
name|value
operator|=
literal|"NP_NULL_ON_SOME_PATH_EXCEPTION"
argument_list|,
name|justification
operator|=
literal|"FindBugs seems confused on ps in below."
argument_list|)
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|getRegionsOnCrashedServer
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
comment|// Super will return an immutable list (empty if nothing on this server).
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|ris
init|=
name|super
operator|.
name|getRegionsOnCrashedServer
argument_list|(
name|env
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|ris
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|ris
return|;
block|}
comment|// Nothing in in-master context. Check for Unknown Server! in hbase:meta.
comment|// If super list is empty, then allow that an operator scheduled an SCP because they are trying
comment|// to purge 'Unknown Servers' -- servers that are neither online nor in dead servers
comment|// list but that ARE in hbase:meta and so showing as unknown in places like 'HBCK Report'.
comment|// This mis-accounting does not happen in normal circumstance but may arise in-extremis
comment|// when cluster has been damaged in operation.
name|UnknownServerVisitor
name|visitor
init|=
operator|new
name|UnknownServerVisitor
argument_list|(
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getConnection
argument_list|()
argument_list|,
name|getServerName
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|MetaTableAccessor
operator|.
name|scanMetaForTableRegions
argument_list|(
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getConnection
argument_list|()
argument_list|,
name|visitor
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed scan of hbase:meta for 'Unknown Servers'"
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
return|return
name|ris
return|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Found {} mentions of {} in hbase:meta of OPEN/OPENING Regions: {}"
argument_list|,
name|visitor
operator|.
name|getReassigns
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|getServerName
argument_list|()
argument_list|,
name|visitor
operator|.
name|getReassigns
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|RegionInfo
operator|::
name|getEncodedName
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|joining
argument_list|(
literal|","
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|visitor
operator|.
name|getReassigns
argument_list|()
return|;
block|}
comment|/**    * Visitor for hbase:meta that 'fixes' Unknown Server issues. Collects    * a List of Regions to reassign as 'result'.    */
specifier|private
specifier|static
class|class
name|UnknownServerVisitor
implements|implements
name|MetaTableAccessor
operator|.
name|Visitor
block|{
specifier|private
specifier|final
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|reassigns
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|ServerName
name|unknownServerName
decl_stmt|;
specifier|private
specifier|final
name|Connection
name|connection
decl_stmt|;
specifier|private
name|UnknownServerVisitor
parameter_list|(
name|Connection
name|connection
parameter_list|,
name|ServerName
name|unknownServerName
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
name|unknownServerName
operator|=
name|unknownServerName
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|visit
parameter_list|(
name|Result
name|result
parameter_list|)
throws|throws
name|IOException
block|{
name|RegionLocations
name|rls
init|=
name|MetaTableAccessor
operator|.
name|getRegionLocations
argument_list|(
name|result
argument_list|)
decl_stmt|;
if|if
condition|(
name|rls
operator|==
literal|null
condition|)
block|{
return|return
literal|true
return|;
block|}
for|for
control|(
name|HRegionLocation
name|hrl
range|:
name|rls
operator|.
name|getRegionLocations
argument_list|()
control|)
block|{
if|if
condition|(
name|hrl
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|hrl
operator|.
name|getRegion
argument_list|()
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|hrl
operator|.
name|getServerName
argument_list|()
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
operator|!
name|hrl
operator|.
name|getServerName
argument_list|()
operator|.
name|equals
argument_list|(
name|this
operator|.
name|unknownServerName
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|RegionState
operator|.
name|State
name|state
init|=
name|RegionStateStore
operator|.
name|getRegionState
argument_list|(
name|result
argument_list|,
name|hrl
operator|.
name|getRegion
argument_list|()
argument_list|)
decl_stmt|;
name|RegionState
name|rs
init|=
operator|new
name|RegionState
argument_list|(
name|hrl
operator|.
name|getRegion
argument_list|()
argument_list|,
name|state
argument_list|,
name|hrl
operator|.
name|getServerName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|rs
operator|.
name|isClosing
argument_list|()
condition|)
block|{
comment|// Move region to CLOSED in hbase:meta.
name|LOG
operator|.
name|info
argument_list|(
literal|"Moving {} from CLOSING to CLOSED in hbase:meta"
argument_list|,
name|hrl
operator|.
name|getRegion
argument_list|()
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|MetaTableAccessor
operator|.
name|updateRegionState
argument_list|(
name|this
operator|.
name|connection
argument_list|,
name|hrl
operator|.
name|getRegion
argument_list|()
argument_list|,
name|RegionState
operator|.
name|State
operator|.
name|CLOSED
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed moving {} from CLOSING to CLOSED"
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|rs
operator|.
name|isOpening
argument_list|()
operator|||
name|rs
operator|.
name|isOpened
argument_list|()
condition|)
block|{
name|this
operator|.
name|reassigns
operator|.
name|add
argument_list|(
name|hrl
operator|.
name|getRegion
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Passing {}"
argument_list|,
name|rs
argument_list|)
expr_stmt|;
block|}
block|}
return|return
literal|true
return|;
block|}
specifier|private
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|getReassigns
parameter_list|()
block|{
return|return
name|this
operator|.
name|reassigns
return|;
block|}
block|}
block|}
end_class

end_unit

