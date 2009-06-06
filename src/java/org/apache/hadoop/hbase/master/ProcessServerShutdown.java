begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2009 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|ArrayList
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
name|Set
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
name|HServerInfo
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
name|RemoteExceptionHandler
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
name|HServerAddress
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
name|ipc
operator|.
name|HRegionInterface
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
name|HLog
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
name|HRegion
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
name|io
operator|.
name|RowResult
import|;
end_import

begin_comment
comment|/**   * Instantiated when a server's lease has expired, meaning it has crashed.  * The region server's log file needs to be split up for each region it was  * serving, and the regions need to get reassigned.  */
end_comment

begin_class
class|class
name|ProcessServerShutdown
extends|extends
name|RegionServerOperation
block|{
specifier|private
specifier|final
name|String
name|deadServer
decl_stmt|;
specifier|private
name|boolean
name|isRootServer
decl_stmt|;
specifier|private
name|List
argument_list|<
name|MetaRegion
argument_list|>
name|metaRegions
decl_stmt|;
specifier|private
name|Path
name|oldLogDir
decl_stmt|;
specifier|private
name|boolean
name|logSplit
decl_stmt|;
specifier|private
name|boolean
name|rootRescanned
decl_stmt|;
specifier|private
name|HServerAddress
name|deadServerAddress
decl_stmt|;
specifier|private
specifier|static
class|class
name|ToDoEntry
block|{
name|boolean
name|regionOffline
decl_stmt|;
specifier|final
name|byte
index|[]
name|row
decl_stmt|;
specifier|final
name|HRegionInfo
name|info
decl_stmt|;
name|ToDoEntry
parameter_list|(
specifier|final
name|byte
index|[]
name|row
parameter_list|,
specifier|final
name|HRegionInfo
name|info
parameter_list|)
block|{
name|this
operator|.
name|regionOffline
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|row
operator|=
name|row
expr_stmt|;
name|this
operator|.
name|info
operator|=
name|info
expr_stmt|;
block|}
block|}
comment|/**    * @param master    * @param serverInfo    */
specifier|public
name|ProcessServerShutdown
parameter_list|(
name|HMaster
name|master
parameter_list|,
name|HServerInfo
name|serverInfo
parameter_list|)
block|{
name|super
argument_list|(
name|master
argument_list|)
expr_stmt|;
name|this
operator|.
name|deadServer
operator|=
name|HServerInfo
operator|.
name|getServerName
argument_list|(
name|serverInfo
argument_list|)
expr_stmt|;
name|this
operator|.
name|deadServerAddress
operator|=
name|serverInfo
operator|.
name|getServerAddress
argument_list|()
expr_stmt|;
name|this
operator|.
name|logSplit
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|rootRescanned
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|oldLogDir
operator|=
operator|new
name|Path
argument_list|(
name|master
operator|.
name|rootdir
argument_list|,
name|HLog
operator|.
name|getHLogDirectoryName
argument_list|(
name|serverInfo
argument_list|)
argument_list|)
expr_stmt|;
comment|// check to see if I am responsible for either ROOT or any of the META tables.
name|closeMetaRegions
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|closeMetaRegions
parameter_list|()
block|{
name|isRootServer
operator|=
name|master
operator|.
name|regionManager
operator|.
name|isRootServer
argument_list|(
name|deadServerAddress
argument_list|)
expr_stmt|;
if|if
condition|(
name|isRootServer
condition|)
block|{
name|master
operator|.
name|regionManager
operator|.
name|unsetRootRegion
argument_list|()
expr_stmt|;
block|}
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|metaStarts
init|=
name|master
operator|.
name|regionManager
operator|.
name|listMetaRegionsForServer
argument_list|(
name|deadServerAddress
argument_list|)
decl_stmt|;
name|metaRegions
operator|=
operator|new
name|ArrayList
argument_list|<
name|MetaRegion
argument_list|>
argument_list|()
expr_stmt|;
for|for
control|(
name|byte
index|[]
name|region
range|:
name|metaStarts
control|)
block|{
name|MetaRegion
name|r
init|=
name|master
operator|.
name|regionManager
operator|.
name|offlineMetaRegion
argument_list|(
name|region
argument_list|)
decl_stmt|;
name|metaRegions
operator|.
name|add
argument_list|(
name|r
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"ProcessServerShutdown of "
operator|+
name|this
operator|.
name|deadServer
return|;
block|}
comment|/** Finds regions that the dead region server was serving    */
specifier|protected
name|void
name|scanMetaRegion
parameter_list|(
name|HRegionInterface
name|server
parameter_list|,
name|long
name|scannerId
parameter_list|,
name|byte
index|[]
name|regionName
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|ToDoEntry
argument_list|>
name|toDoList
init|=
operator|new
name|ArrayList
argument_list|<
name|ToDoEntry
argument_list|>
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
init|=
operator|new
name|HashSet
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|emptyRows
init|=
operator|new
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
try|try
block|{
while|while
condition|(
literal|true
condition|)
block|{
name|Result
name|values
init|=
literal|null
decl_stmt|;
try|try
block|{
name|values
operator|=
name|server
operator|.
name|next
argument_list|(
name|scannerId
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
name|error
argument_list|(
literal|"Shutdown scanning of meta region"
argument_list|,
name|RemoteExceptionHandler
operator|.
name|checkIOException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
break|break;
block|}
if|if
condition|(
name|values
operator|==
literal|null
operator|||
name|values
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
break|break;
block|}
name|byte
index|[]
name|row
init|=
name|values
operator|.
name|getRow
argument_list|()
decl_stmt|;
comment|// Check server name.  If null, skip (We used to consider it was on
comment|// shutdown server but that would mean that we'd reassign regions that
comment|// were already out being assigned, ones that were product of a split
comment|// that happened while the shutdown was being processed.
name|String
name|serverAddress
init|=
name|Bytes
operator|.
name|toString
argument_list|(
name|values
operator|.
name|getValue
argument_list|(
name|CATALOG_FAMILY
argument_list|,
name|SERVER_QUALIFIER
argument_list|)
argument_list|)
decl_stmt|;
name|long
name|startCode
init|=
name|Bytes
operator|.
name|toLong
argument_list|(
name|values
operator|.
name|getValue
argument_list|(
name|CATALOG_FAMILY
argument_list|,
name|STARTCODE_QUALIFIER
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|serverName
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|serverAddress
operator|!=
literal|null
operator|&&
name|serverAddress
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|serverName
operator|=
name|HServerInfo
operator|.
name|getServerName
argument_list|(
name|serverAddress
argument_list|,
name|startCode
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|serverName
operator|==
literal|null
operator|||
operator|!
name|deadServer
operator|.
name|equals
argument_list|(
name|serverName
argument_list|)
condition|)
block|{
comment|// This isn't the server you're looking for - move along
continue|continue;
block|}
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
operator|&&
name|row
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Shutdown scanner for "
operator|+
name|serverName
operator|+
literal|" processing "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|row
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|//        HRegionInfo info = master.getHRegionInfo(row, values.rowResult());
name|HRegionInfo
name|info
init|=
name|master
operator|.
name|getHRegionInfo
argument_list|(
name|row
argument_list|,
name|values
argument_list|)
decl_stmt|;
if|if
condition|(
name|info
operator|==
literal|null
condition|)
block|{
name|emptyRows
operator|.
name|add
argument_list|(
name|row
argument_list|)
expr_stmt|;
continue|continue;
block|}
synchronized|synchronized
init|(
name|master
operator|.
name|regionManager
init|)
block|{
if|if
condition|(
name|info
operator|.
name|isMetaTable
argument_list|()
condition|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"removing meta region "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|info
operator|.
name|getRegionName
argument_list|()
argument_list|)
operator|+
literal|" from online meta regions"
argument_list|)
expr_stmt|;
block|}
name|master
operator|.
name|regionManager
operator|.
name|offlineMetaRegion
argument_list|(
name|info
operator|.
name|getStartKey
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|ToDoEntry
name|todo
init|=
operator|new
name|ToDoEntry
argument_list|(
name|row
argument_list|,
name|info
argument_list|)
decl_stmt|;
name|toDoList
operator|.
name|add
argument_list|(
name|todo
argument_list|)
expr_stmt|;
if|if
condition|(
name|master
operator|.
name|regionManager
operator|.
name|isOfflined
argument_list|(
name|info
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|)
operator|||
name|info
operator|.
name|isOffline
argument_list|()
condition|)
block|{
name|master
operator|.
name|regionManager
operator|.
name|removeRegion
argument_list|(
name|info
argument_list|)
expr_stmt|;
comment|// Mark region offline
if|if
condition|(
operator|!
name|info
operator|.
name|isOffline
argument_list|()
condition|)
block|{
name|todo
operator|.
name|regionOffline
operator|=
literal|true
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
operator|!
name|info
operator|.
name|isOffline
argument_list|()
operator|&&
operator|!
name|info
operator|.
name|isSplit
argument_list|()
condition|)
block|{
comment|// Get region reassigned
name|regions
operator|.
name|add
argument_list|(
name|info
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
finally|finally
block|{
if|if
condition|(
name|scannerId
operator|!=
operator|-
literal|1L
condition|)
block|{
try|try
block|{
name|server
operator|.
name|close
argument_list|(
name|scannerId
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
name|error
argument_list|(
literal|"Closing scanner"
argument_list|,
name|RemoteExceptionHandler
operator|.
name|checkIOException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// Scan complete. Remove any rows which had empty HRegionInfos
if|if
condition|(
name|emptyRows
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Found "
operator|+
name|emptyRows
operator|.
name|size
argument_list|()
operator|+
literal|" rows with empty HRegionInfo while scanning meta region "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|regionName
argument_list|)
argument_list|)
expr_stmt|;
name|master
operator|.
name|deleteEmptyMetaRows
argument_list|(
name|server
argument_list|,
name|regionName
argument_list|,
name|emptyRows
argument_list|)
expr_stmt|;
block|}
comment|// Update server in root/meta entries
for|for
control|(
name|ToDoEntry
name|e
range|:
name|toDoList
control|)
block|{
if|if
condition|(
name|e
operator|.
name|regionOffline
condition|)
block|{
name|HRegion
operator|.
name|offlineRegionInMETA
argument_list|(
name|server
argument_list|,
name|regionName
argument_list|,
name|e
operator|.
name|info
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Get regions reassigned
for|for
control|(
name|HRegionInfo
name|info
range|:
name|regions
control|)
block|{
name|master
operator|.
name|regionManager
operator|.
name|setUnassigned
argument_list|(
name|info
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
class|class
name|ScanRootRegion
extends|extends
name|RetryableMetaOperation
argument_list|<
name|Boolean
argument_list|>
block|{
name|ScanRootRegion
parameter_list|(
name|MetaRegion
name|m
parameter_list|,
name|HMaster
name|master
parameter_list|)
block|{
name|super
argument_list|(
name|m
argument_list|,
name|master
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Boolean
name|call
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"process server shutdown scanning root region on "
operator|+
name|master
operator|.
name|getRootRegionLocation
argument_list|()
operator|.
name|getBindAddress
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan
operator|.
name|addFamily
argument_list|(
name|CATALOG_FAMILY
argument_list|)
expr_stmt|;
name|long
name|scannerId
init|=
name|server
operator|.
name|openScanner
argument_list|(
name|HRegionInfo
operator|.
name|ROOT_REGIONINFO
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|scan
argument_list|)
decl_stmt|;
name|scanMetaRegion
argument_list|(
name|server
argument_list|,
name|scannerId
argument_list|,
name|HRegionInfo
operator|.
name|ROOT_REGIONINFO
operator|.
name|getRegionName
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
specifier|private
class|class
name|ScanMetaRegions
extends|extends
name|RetryableMetaOperation
argument_list|<
name|Boolean
argument_list|>
block|{
name|ScanMetaRegions
parameter_list|(
name|MetaRegion
name|m
parameter_list|,
name|HMaster
name|master
parameter_list|)
block|{
name|super
argument_list|(
name|m
argument_list|,
name|master
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Boolean
name|call
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"process server shutdown scanning "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|m
operator|.
name|getRegionName
argument_list|()
argument_list|)
operator|+
literal|" on "
operator|+
name|m
operator|.
name|getServer
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan
operator|.
name|addFamily
argument_list|(
name|CATALOG_FAMILY
argument_list|)
expr_stmt|;
name|long
name|scannerId
init|=
name|server
operator|.
name|openScanner
argument_list|(
name|HRegionInfo
operator|.
name|ROOT_REGIONINFO
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|scan
argument_list|)
decl_stmt|;
name|scanMetaRegion
argument_list|(
name|server
argument_list|,
name|scannerId
argument_list|,
name|m
operator|.
name|getRegionName
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|process
parameter_list|()
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"process shutdown of server "
operator|+
name|this
operator|.
name|deadServer
operator|+
literal|": logSplit: "
operator|+
name|logSplit
operator|+
literal|", rootRescanned: "
operator|+
name|rootRescanned
operator|+
literal|", numberOfMetaRegions: "
operator|+
name|master
operator|.
name|regionManager
operator|.
name|numMetaRegions
argument_list|()
operator|+
literal|", onlineMetaRegions.size(): "
operator|+
name|master
operator|.
name|regionManager
operator|.
name|numOnlineMetaRegions
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|logSplit
condition|)
block|{
comment|// Process the old log file
if|if
condition|(
name|master
operator|.
name|fs
operator|.
name|exists
argument_list|(
name|oldLogDir
argument_list|)
condition|)
block|{
if|if
condition|(
operator|!
name|master
operator|.
name|regionManager
operator|.
name|splitLogLock
operator|.
name|tryLock
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
try|try
block|{
name|HLog
operator|.
name|splitLog
argument_list|(
name|master
operator|.
name|rootdir
argument_list|,
name|oldLogDir
argument_list|,
name|master
operator|.
name|fs
argument_list|,
name|master
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|master
operator|.
name|regionManager
operator|.
name|splitLogLock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
name|logSplit
operator|=
literal|true
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Log split complete, meta reassignment and scanning:"
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|isRootServer
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"ProcessServerShutdown reassigning ROOT region"
argument_list|)
expr_stmt|;
name|master
operator|.
name|regionManager
operator|.
name|reassignRootRegion
argument_list|()
expr_stmt|;
name|isRootServer
operator|=
literal|false
expr_stmt|;
comment|// prevent double reassignment... heh.
block|}
for|for
control|(
name|MetaRegion
name|metaRegion
range|:
name|metaRegions
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"ProcessServerShutdown setting to unassigned: "
operator|+
name|metaRegion
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|master
operator|.
name|regionManager
operator|.
name|setUnassigned
argument_list|(
name|metaRegion
operator|.
name|getRegionInfo
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|// one the meta regions are online, "forget" about them.  Since there are explicit
comment|// checks below to make sure meta/root are online, this is likely to occur.
name|metaRegions
operator|.
name|clear
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|rootAvailable
argument_list|()
condition|)
block|{
comment|// Return true so that worker does not put this request back on the
comment|// toDoQueue.
comment|// rootAvailable() has already put it on the delayedToDoQueue
return|return
literal|true
return|;
block|}
if|if
condition|(
operator|!
name|rootRescanned
condition|)
block|{
comment|// Scan the ROOT region
name|Boolean
name|result
init|=
operator|new
name|ScanRootRegion
argument_list|(
operator|new
name|MetaRegion
argument_list|(
name|master
operator|.
name|getRootRegionLocation
argument_list|()
argument_list|,
name|HRegionInfo
operator|.
name|ROOT_REGIONINFO
argument_list|)
argument_list|,
name|this
operator|.
name|master
argument_list|)
operator|.
name|doWithRetries
argument_list|()
decl_stmt|;
if|if
condition|(
name|result
operator|==
literal|null
condition|)
block|{
comment|// Master is closing - give up
return|return
literal|true
return|;
block|}
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"process server shutdown scanning root region on "
operator|+
name|master
operator|.
name|getRootRegionLocation
argument_list|()
operator|.
name|getBindAddress
argument_list|()
operator|+
literal|" finished "
operator|+
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|rootRescanned
operator|=
literal|true
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|metaTableAvailable
argument_list|()
condition|)
block|{
comment|// We can't proceed because not all meta regions are online.
comment|// metaAvailable() has put this request on the delayedToDoQueue
comment|// Return true so that worker does not put this on the toDoQueue
return|return
literal|true
return|;
block|}
name|List
argument_list|<
name|MetaRegion
argument_list|>
name|regions
init|=
name|master
operator|.
name|regionManager
operator|.
name|getListOfOnlineMetaRegions
argument_list|()
decl_stmt|;
for|for
control|(
name|MetaRegion
name|r
range|:
name|regions
control|)
block|{
name|Boolean
name|result
init|=
operator|new
name|ScanMetaRegions
argument_list|(
name|r
argument_list|,
name|this
operator|.
name|master
argument_list|)
operator|.
name|doWithRetries
argument_list|()
decl_stmt|;
if|if
condition|(
name|result
operator|==
literal|null
condition|)
block|{
break|break;
block|}
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"process server shutdown finished scanning "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|r
operator|.
name|getRegionName
argument_list|()
argument_list|)
operator|+
literal|" on "
operator|+
name|r
operator|.
name|getServer
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Remove this server from dead servers list.  Finished splitting logs.
name|this
operator|.
name|master
operator|.
name|serverManager
operator|.
name|removeDeadServer
argument_list|(
name|deadServer
argument_list|)
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Removed "
operator|+
name|deadServer
operator|+
literal|" from deadservers Map"
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|protected
name|int
name|getPriority
parameter_list|()
block|{
return|return
literal|2
return|;
comment|// high but not highest priority
block|}
block|}
end_class

end_unit

