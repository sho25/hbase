begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2008 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|io
operator|.
name|UnsupportedEncodingException
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
name|SortedMap
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
name|io
operator|.
name|HbaseMapWritable
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
name|io
operator|.
name|Text
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
name|HServerAddress
name|deadServer
decl_stmt|;
specifier|private
name|String
name|deadServerName
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
class|class
name|ToDoEntry
block|{
name|boolean
name|deleteRegion
decl_stmt|;
name|boolean
name|regionOffline
decl_stmt|;
name|Text
name|row
decl_stmt|;
name|HRegionInfo
name|info
decl_stmt|;
name|ToDoEntry
parameter_list|(
name|Text
name|row
parameter_list|,
name|HRegionInfo
name|info
parameter_list|)
block|{
name|this
operator|.
name|deleteRegion
operator|=
literal|false
expr_stmt|;
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
comment|/**    * @param serverInfo    */
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
name|serverInfo
operator|.
name|getServerAddress
argument_list|()
expr_stmt|;
name|this
operator|.
name|deadServerName
operator|=
name|this
operator|.
name|deadServer
operator|.
name|toString
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
name|StringBuilder
name|dirName
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"log_"
argument_list|)
decl_stmt|;
name|dirName
operator|.
name|append
argument_list|(
name|deadServer
operator|.
name|getBindAddress
argument_list|()
argument_list|)
expr_stmt|;
name|dirName
operator|.
name|append
argument_list|(
literal|"_"
argument_list|)
expr_stmt|;
name|dirName
operator|.
name|append
argument_list|(
name|serverInfo
operator|.
name|getStartCode
argument_list|()
argument_list|)
expr_stmt|;
name|dirName
operator|.
name|append
argument_list|(
literal|"_"
argument_list|)
expr_stmt|;
name|dirName
operator|.
name|append
argument_list|(
name|deadServer
operator|.
name|getPort
argument_list|()
argument_list|)
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
name|dirName
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/** {@inheritDoc} */
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
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/** Finds regions that the dead region server was serving */
specifier|private
name|void
name|scanMetaRegion
parameter_list|(
name|HRegionInterface
name|server
parameter_list|,
name|long
name|scannerId
parameter_list|,
name|Text
name|regionName
parameter_list|)
throws|throws
name|IOException
block|{
name|ArrayList
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
name|HashSet
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
try|try
block|{
while|while
condition|(
literal|true
condition|)
block|{
name|RowResult
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
name|Text
name|row
init|=
name|values
operator|.
name|getRow
argument_list|()
decl_stmt|;
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
literal|"shutdown scanner looking at "
operator|+
name|row
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// Check server name.  If null, be conservative and treat as though
comment|// region had been on shutdown server (could be null because we
comment|// missed edits in hlog because hdfs does not do write-append).
name|String
name|serverName
decl_stmt|;
try|try
block|{
name|serverName
operator|=
name|Writables
operator|.
name|cellToString
argument_list|(
name|values
operator|.
name|get
argument_list|(
name|COL_SERVER
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnsupportedEncodingException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Server name"
argument_list|,
name|e
argument_list|)
expr_stmt|;
break|break;
block|}
if|if
condition|(
name|serverName
operator|.
name|length
argument_list|()
operator|>
literal|0
operator|&&
name|deadServerName
operator|.
name|compareTo
argument_list|(
name|serverName
argument_list|)
operator|!=
literal|0
condition|)
block|{
comment|// This isn't the server you're looking for - move along
continue|continue;
block|}
comment|// Bingo! Found it.
name|HRegionInfo
name|info
init|=
name|master
operator|.
name|getHRegionInfo
argument_list|(
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
continue|continue;
block|}
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
name|info
operator|.
name|getRegionName
argument_list|()
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
name|isMarkedToClose
argument_list|(
name|deadServerName
argument_list|,
name|info
operator|.
name|getRegionName
argument_list|()
argument_list|)
condition|)
block|{
name|master
operator|.
name|regionManager
operator|.
name|noLongerMarkedToClose
argument_list|(
name|deadServerName
argument_list|,
name|info
operator|.
name|getRegionName
argument_list|()
argument_list|)
expr_stmt|;
name|master
operator|.
name|regionManager
operator|.
name|noLongerUnassigned
argument_list|(
name|info
argument_list|)
expr_stmt|;
if|if
condition|(
name|master
operator|.
name|regionManager
operator|.
name|isMarkedForDeletion
argument_list|(
name|info
operator|.
name|getRegionName
argument_list|()
argument_list|)
condition|)
block|{
comment|// Delete this region
name|master
operator|.
name|regionManager
operator|.
name|regionDeleted
argument_list|(
name|info
operator|.
name|getRegionName
argument_list|()
argument_list|)
expr_stmt|;
name|todo
operator|.
name|deleteRegion
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
comment|// Mark region offline
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
comment|// Get region reassigned
name|regions
operator|.
name|add
argument_list|(
name|info
argument_list|)
expr_stmt|;
comment|// If it was pending, remove.
comment|// Otherwise will obstruct its getting reassigned.
name|master
operator|.
name|regionManager
operator|.
name|noLongerPending
argument_list|(
name|info
operator|.
name|getRegionName
argument_list|()
argument_list|)
expr_stmt|;
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
name|deleteRegion
condition|)
block|{
name|HRegion
operator|.
name|removeRegionFromMETA
argument_list|(
name|server
argument_list|,
name|regionName
argument_list|,
name|e
operator|.
name|row
argument_list|)
expr_stmt|;
block|}
elseif|else
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
argument_list|)
expr_stmt|;
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
name|deadServer
operator|+
literal|": logSplit: "
operator|+
name|this
operator|.
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
name|conf
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
name|HRegionInterface
name|server
init|=
literal|null
decl_stmt|;
name|long
name|scannerId
init|=
operator|-
literal|1L
decl_stmt|;
for|for
control|(
name|int
name|tries
init|=
literal|0
init|;
name|tries
operator|<
name|numRetries
condition|;
name|tries
operator|++
control|)
block|{
if|if
condition|(
name|master
operator|.
name|closed
operator|.
name|get
argument_list|()
condition|)
block|{
return|return
literal|true
return|;
block|}
name|server
operator|=
name|master
operator|.
name|connection
operator|.
name|getHRegionConnection
argument_list|(
name|master
operator|.
name|getRootRegionLocation
argument_list|()
argument_list|)
expr_stmt|;
name|scannerId
operator|=
operator|-
literal|1L
expr_stmt|;
try|try
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
name|scannerId
operator|=
name|server
operator|.
name|openScanner
argument_list|(
name|HRegionInfo
operator|.
name|rootRegionInfo
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|COLUMN_FAMILY_ARRAY
argument_list|,
name|EMPTY_START_ROW
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|scanMetaRegion
argument_list|(
name|server
argument_list|,
name|scannerId
argument_list|,
name|HRegionInfo
operator|.
name|rootRegionInfo
operator|.
name|getRegionName
argument_list|()
argument_list|)
expr_stmt|;
break|break;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
if|if
condition|(
name|tries
operator|==
name|numRetries
operator|-
literal|1
condition|)
block|{
throw|throw
name|RemoteExceptionHandler
operator|.
name|checkIOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
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
for|for
control|(
name|int
name|tries
init|=
literal|0
init|;
name|tries
operator|<
name|numRetries
condition|;
name|tries
operator|++
control|)
block|{
try|try
block|{
if|if
condition|(
name|master
operator|.
name|closed
operator|.
name|get
argument_list|()
condition|)
block|{
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
name|HRegionInterface
name|server
init|=
literal|null
decl_stmt|;
name|long
name|scannerId
init|=
operator|-
literal|1L
decl_stmt|;
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
name|r
operator|.
name|getRegionName
argument_list|()
operator|+
literal|" on "
operator|+
name|r
operator|.
name|getServer
argument_list|()
operator|+
literal|" "
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
name|server
operator|=
name|master
operator|.
name|connection
operator|.
name|getHRegionConnection
argument_list|(
name|r
operator|.
name|getServer
argument_list|()
argument_list|)
expr_stmt|;
name|scannerId
operator|=
name|server
operator|.
name|openScanner
argument_list|(
name|r
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|COLUMN_FAMILY_ARRAY
argument_list|,
name|EMPTY_START_ROW
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|scanMetaRegion
argument_list|(
name|server
argument_list|,
name|scannerId
argument_list|,
name|r
operator|.
name|getRegionName
argument_list|()
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
literal|"process server shutdown finished scanning "
operator|+
name|r
operator|.
name|getRegionName
argument_list|()
operator|+
literal|" on "
operator|+
name|r
operator|.
name|getServer
argument_list|()
operator|+
literal|" "
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
block|}
name|master
operator|.
name|serverManager
operator|.
name|removeDeadServer
argument_list|(
name|deadServerName
argument_list|)
expr_stmt|;
break|break;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
if|if
condition|(
name|tries
operator|==
name|numRetries
operator|-
literal|1
condition|)
block|{
throw|throw
name|RemoteExceptionHandler
operator|.
name|checkIOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit

