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
name|Set
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
name|MasterNotRunningException
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
name|Cell
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
comment|/**  * Abstract base class for operations that need to examine all HRegionInfo   * objects that make up a table. (For a table, operate on each of its rows  * in .META.) To gain the   */
end_comment

begin_class
specifier|abstract
class|class
name|TableOperation
implements|implements
name|HConstants
block|{
specifier|static
specifier|final
name|Long
name|ZERO_L
init|=
name|Long
operator|.
name|valueOf
argument_list|(
literal|0L
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TableOperation
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|protected
name|Set
argument_list|<
name|MetaRegion
argument_list|>
name|metaRegions
decl_stmt|;
specifier|protected
name|Text
name|tableName
decl_stmt|;
specifier|protected
name|Set
argument_list|<
name|HRegionInfo
argument_list|>
name|unservedRegions
decl_stmt|;
specifier|protected
name|HMaster
name|master
decl_stmt|;
specifier|protected
specifier|final
name|int
name|numRetries
decl_stmt|;
specifier|protected
name|TableOperation
parameter_list|(
specifier|final
name|HMaster
name|master
parameter_list|,
specifier|final
name|Text
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|numRetries
operator|=
name|master
operator|.
name|numRetries
expr_stmt|;
name|this
operator|.
name|master
operator|=
name|master
expr_stmt|;
if|if
condition|(
operator|!
name|this
operator|.
name|master
operator|.
name|isMasterRunning
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|MasterNotRunningException
argument_list|()
throw|;
block|}
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|unservedRegions
operator|=
operator|new
name|HashSet
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|()
expr_stmt|;
comment|// We can not access any meta region if they have not already been
comment|// assigned and scanned.
if|if
condition|(
name|master
operator|.
name|regionManager
operator|.
name|metaScannerThread
operator|.
name|waitForMetaRegionsOrClose
argument_list|()
condition|)
block|{
comment|// We're shutting down. Forget it.
throw|throw
operator|new
name|MasterNotRunningException
argument_list|()
throw|;
block|}
name|this
operator|.
name|metaRegions
operator|=
name|master
operator|.
name|regionManager
operator|.
name|getMetaRegionsForTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
name|void
name|process
parameter_list|()
throws|throws
name|IOException
block|{
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
name|boolean
name|tableExists
init|=
literal|false
decl_stmt|;
try|try
block|{
comment|// Prevent meta scanner from running
synchronized|synchronized
init|(
name|master
operator|.
name|regionManager
operator|.
name|metaScannerThread
operator|.
name|scannerLock
init|)
block|{
for|for
control|(
name|MetaRegion
name|m
range|:
name|metaRegions
control|)
block|{
comment|// Get a connection to a meta server
name|HRegionInterface
name|server
init|=
name|master
operator|.
name|connection
operator|.
name|getHRegionConnection
argument_list|(
name|m
operator|.
name|getServer
argument_list|()
argument_list|)
decl_stmt|;
comment|// Open a scanner on the meta region
name|long
name|scannerId
init|=
name|server
operator|.
name|openScanner
argument_list|(
name|m
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|COLUMN_FAMILY_ARRAY
argument_list|,
name|tableName
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
literal|null
argument_list|)
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
name|server
operator|.
name|next
argument_list|(
name|scannerId
argument_list|)
decl_stmt|;
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
name|HRegionInfo
name|info
init|=
name|this
operator|.
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
throw|throw
operator|new
name|IOException
argument_list|(
name|COL_REGIONINFO
operator|+
literal|" not found on "
operator|+
name|values
operator|.
name|getRow
argument_list|()
argument_list|)
throw|;
block|}
name|String
name|serverName
init|=
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
decl_stmt|;
name|long
name|startCode
init|=
name|Writables
operator|.
name|cellToLong
argument_list|(
name|values
operator|.
name|get
argument_list|(
name|COL_STARTCODE
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|info
operator|.
name|getTableDesc
argument_list|()
operator|.
name|getName
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
break|break;
comment|// Beyond any more entries for this table
block|}
name|tableExists
operator|=
literal|true
expr_stmt|;
if|if
condition|(
operator|!
name|isBeingServed
argument_list|(
name|serverName
argument_list|,
name|startCode
argument_list|)
condition|)
block|{
name|unservedRegions
operator|.
name|add
argument_list|(
name|info
argument_list|)
expr_stmt|;
block|}
name|processScanItem
argument_list|(
name|serverName
argument_list|,
name|startCode
argument_list|,
name|info
argument_list|)
expr_stmt|;
block|}
comment|// while(true)
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
name|e
operator|=
name|RemoteExceptionHandler
operator|.
name|checkIOException
argument_list|(
name|e
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|error
argument_list|(
literal|"closing scanner"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
name|scannerId
operator|=
operator|-
literal|1L
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|tableExists
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|tableName
operator|+
literal|" does not exist"
argument_list|)
throw|;
block|}
name|postProcessMeta
argument_list|(
name|m
argument_list|,
name|server
argument_list|)
expr_stmt|;
name|unservedRegions
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
comment|// for(MetaRegion m:)
block|}
comment|// synchronized(metaScannerLock)
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
comment|// No retries left
name|this
operator|.
name|master
operator|.
name|checkFileSystem
argument_list|()
expr_stmt|;
throw|throw
name|RemoteExceptionHandler
operator|.
name|checkIOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
continue|continue;
block|}
break|break;
block|}
comment|// for(tries...)
block|}
specifier|protected
name|boolean
name|isBeingServed
parameter_list|(
name|String
name|serverName
parameter_list|,
name|long
name|startCode
parameter_list|)
block|{
name|boolean
name|result
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|serverName
operator|!=
literal|null
operator|&&
name|serverName
operator|.
name|length
argument_list|()
operator|>
literal|0
operator|&&
name|startCode
operator|!=
operator|-
literal|1L
condition|)
block|{
name|HServerInfo
name|s
init|=
name|master
operator|.
name|serverManager
operator|.
name|getServerInfo
argument_list|(
name|serverName
argument_list|)
decl_stmt|;
name|result
operator|=
name|s
operator|!=
literal|null
operator|&&
name|s
operator|.
name|getStartCode
argument_list|()
operator|==
name|startCode
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
specifier|protected
name|boolean
name|isEnabled
parameter_list|(
name|HRegionInfo
name|info
parameter_list|)
block|{
return|return
operator|!
name|info
operator|.
name|isOffline
argument_list|()
return|;
block|}
specifier|protected
specifier|abstract
name|void
name|processScanItem
parameter_list|(
name|String
name|serverName
parameter_list|,
name|long
name|startCode
parameter_list|,
name|HRegionInfo
name|info
parameter_list|)
throws|throws
name|IOException
function_decl|;
specifier|protected
specifier|abstract
name|void
name|postProcessMeta
parameter_list|(
name|MetaRegion
name|m
parameter_list|,
name|HRegionInterface
name|server
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_class

end_unit

