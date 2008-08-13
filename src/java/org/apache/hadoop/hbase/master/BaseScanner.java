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
name|Map
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|FileStatus
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
name|PathFilter
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
name|ipc
operator|.
name|RemoteException
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
name|Chore
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
name|UnknownScannerException
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
name|io
operator|.
name|BatchUpdate
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
name|regionserver
operator|.
name|HStoreFile
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
name|HStore
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
name|ipc
operator|.
name|HRegionInterface
import|;
end_import

begin_comment
comment|/**  * Base HRegion scanner class. Holds utilty common to<code>ROOT</code> and  *<code>META</code> HRegion scanners.  *   *<p>How do we know if all regions are assigned? After the initial scan of  * the<code>ROOT</code> and<code>META</code> regions, all regions known at  * that time will have been or are in the process of being assigned.</p>  *   *<p>When a region is split the region server notifies the master of the  * split and the new regions are assigned. But suppose the master loses the  * split message? We need to periodically rescan the<code>ROOT</code> and  *<code>META</code> regions.  *<ul>  *<li>If we rescan, any regions that are new but not assigned will have  *    no server info. Any regions that are not being served by the same  *    server will get re-assigned.</li>  *        *<li>Thus a periodic rescan of the root region will find any new  *<code>META</code> regions where we missed the<code>META</code> split  *    message or we failed to detect a server death and consequently need to  *    assign the region to a new server.</li>  *          *<li>if we keep track of all the known<code>META</code> regions, then  *    we can rescan them periodically. If we do this then we can detect any  *    regions for which we missed a region split message.</li>  *</ul>  *      * Thus just keeping track of all the<code>META</code> regions permits  * periodic rescanning which will detect unassigned regions (new or  * otherwise) without the need to keep track of every region.</p>  *   *<p>So the<code>ROOT</code> region scanner needs to wake up:  *<ol>  *<li>when the master receives notification that the<code>ROOT</code>  * region has been opened.</li>  *<li>periodically after the first scan</li>  *</ol>  *   * The<code>META</code>  scanner needs to wake up:  *<ol>  *<li>when a<code>META</code> region comes on line</li>  *</li>periodically to rescan the online<code>META</code> regions</li>  *</ol>  *   *<p>A<code>META</code> region is not 'online' until it has been scanned  * once.  */
end_comment

begin_class
specifier|abstract
class|class
name|BaseScanner
extends|extends
name|Chore
implements|implements
name|HConstants
block|{
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|BaseScanner
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|rootRegion
decl_stmt|;
specifier|protected
specifier|final
name|HMaster
name|master
decl_stmt|;
specifier|protected
specifier|final
name|RegionManager
name|regionManager
decl_stmt|;
specifier|protected
name|boolean
name|initialScanComplete
decl_stmt|;
specifier|protected
specifier|abstract
name|boolean
name|initialScan
parameter_list|()
function_decl|;
specifier|protected
specifier|abstract
name|void
name|maintenanceScan
parameter_list|()
function_decl|;
comment|// will use this variable to synchronize and make sure we aren't interrupted
comment|// mid-scan
specifier|final
name|Integer
name|scannerLock
init|=
operator|new
name|Integer
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|BaseScanner
parameter_list|(
specifier|final
name|HMaster
name|master
parameter_list|,
specifier|final
name|RegionManager
name|regionManager
parameter_list|,
specifier|final
name|boolean
name|rootRegion
parameter_list|,
specifier|final
name|int
name|period
parameter_list|,
specifier|final
name|AtomicBoolean
name|stop
parameter_list|)
block|{
name|super
argument_list|(
name|period
argument_list|,
name|stop
argument_list|)
expr_stmt|;
name|this
operator|.
name|rootRegion
operator|=
name|rootRegion
expr_stmt|;
name|this
operator|.
name|master
operator|=
name|master
expr_stmt|;
name|this
operator|.
name|regionManager
operator|=
name|regionManager
expr_stmt|;
name|this
operator|.
name|initialScanComplete
operator|=
literal|false
expr_stmt|;
block|}
comment|/** @return true if initial scan completed successfully */
specifier|public
name|boolean
name|isInitialScanComplete
parameter_list|()
block|{
return|return
name|initialScanComplete
return|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|initialChore
parameter_list|()
block|{
return|return
name|initialScan
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
name|maintenanceScan
argument_list|()
expr_stmt|;
block|}
comment|/**    * @param region Region to scan    * @throws IOException    */
specifier|protected
name|void
name|scanRegion
parameter_list|(
specifier|final
name|MetaRegion
name|region
parameter_list|)
throws|throws
name|IOException
block|{
name|HRegionInterface
name|regionServer
init|=
literal|null
decl_stmt|;
name|long
name|scannerId
init|=
operator|-
literal|1L
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|" scanning meta region "
operator|+
name|region
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
comment|// Array to hold list of split parents found.  Scan adds to list.  After
comment|// scan we go check if parents can be removed.
name|Map
argument_list|<
name|HRegionInfo
argument_list|,
name|RowResult
argument_list|>
name|splitParents
init|=
operator|new
name|HashMap
argument_list|<
name|HRegionInfo
argument_list|,
name|RowResult
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
name|regionServer
operator|=
name|master
operator|.
name|connection
operator|.
name|getHRegionConnection
argument_list|(
name|region
operator|.
name|getServer
argument_list|()
argument_list|)
expr_stmt|;
name|scannerId
operator|=
name|regionServer
operator|.
name|openScanner
argument_list|(
name|region
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|COLUMN_FAMILY_ARRAY
argument_list|,
name|EMPTY_START_ROW
argument_list|,
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|int
name|numberOfRegionsFound
init|=
literal|0
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|RowResult
name|values
init|=
name|regionServer
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
name|master
operator|.
name|getHRegionInfo
argument_list|(
name|values
operator|.
name|getRow
argument_list|()
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
name|values
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
continue|continue;
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
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|" "
operator|+
name|info
operator|.
name|toString
argument_list|()
operator|+
literal|"}, SERVER => '"
operator|+
name|serverName
operator|+
literal|"', STARTCODE => "
operator|+
name|startCode
argument_list|)
expr_stmt|;
block|}
comment|// Note Region has been assigned.
name|checkAssigned
argument_list|(
name|info
argument_list|,
name|serverName
argument_list|,
name|startCode
argument_list|)
expr_stmt|;
if|if
condition|(
name|isSplitParent
argument_list|(
name|info
argument_list|)
condition|)
block|{
name|splitParents
operator|.
name|put
argument_list|(
name|info
argument_list|,
name|values
argument_list|)
expr_stmt|;
block|}
name|numberOfRegionsFound
operator|+=
literal|1
expr_stmt|;
block|}
if|if
condition|(
name|rootRegion
condition|)
block|{
name|regionManager
operator|.
name|setNumMetaRegions
argument_list|(
name|numberOfRegionsFound
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
if|if
condition|(
name|e
operator|instanceof
name|RemoteException
condition|)
block|{
name|e
operator|=
name|RemoteExceptionHandler
operator|.
name|decodeRemoteException
argument_list|(
operator|(
name|RemoteException
operator|)
name|e
argument_list|)
expr_stmt|;
if|if
condition|(
name|e
operator|instanceof
name|UnknownScannerException
condition|)
block|{
comment|// Reset scannerId so we do not try closing a scanner the other side
comment|// has lost account of: prevents duplicated stack trace out of the
comment|// below close in the finally.
name|scannerId
operator|=
operator|-
literal|1L
expr_stmt|;
block|}
block|}
throw|throw
name|e
throw|;
block|}
finally|finally
block|{
try|try
block|{
if|if
condition|(
name|scannerId
operator|!=
operator|-
literal|1L
operator|&&
name|regionServer
operator|!=
literal|null
condition|)
block|{
name|regionServer
operator|.
name|close
argument_list|(
name|scannerId
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
comment|// Scan is finished.
comment|// First clean up any meta region rows which had null HRegionInfos
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
literal|" rows with empty HRegionInfo "
operator|+
literal|"while scanning meta region "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|region
operator|.
name|getRegionName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|master
operator|.
name|deleteEmptyMetaRows
argument_list|(
name|regionServer
argument_list|,
name|region
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|emptyRows
argument_list|)
expr_stmt|;
block|}
comment|// Take a look at split parents to see if any we can clean up.
if|if
condition|(
name|splitParents
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|HRegionInfo
argument_list|,
name|RowResult
argument_list|>
name|e
range|:
name|splitParents
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|HRegionInfo
name|hri
init|=
name|e
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|cleanupSplits
argument_list|(
name|region
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|regionServer
argument_list|,
name|hri
argument_list|,
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|LOG
operator|.
name|info
argument_list|(
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|" scan of meta region "
operator|+
name|region
operator|.
name|toString
argument_list|()
operator|+
literal|" complete"
argument_list|)
expr_stmt|;
block|}
comment|/*    * @param info Region to check.    * @return True if this is a split parent.    */
specifier|private
name|boolean
name|isSplitParent
parameter_list|(
specifier|final
name|HRegionInfo
name|info
parameter_list|)
block|{
if|if
condition|(
operator|!
name|info
operator|.
name|isSplit
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
operator|!
name|info
operator|.
name|isOffline
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Region is split but not offline: "
operator|+
name|info
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
comment|/*    * If daughters no longer hold reference to the parents, delete the parent.    * @param metaRegionName Meta region name.    * @param server HRegionInterface of meta server to talk to     * @param parent HRegionInfo of split parent    * @param rowContent Content of<code>parent</code> row in    *<code>metaRegionName</code>    * @return True if we removed<code>parent</code> from meta table and from    * the filesystem.    * @throws IOException    */
specifier|private
name|boolean
name|cleanupSplits
parameter_list|(
specifier|final
name|byte
index|[]
name|metaRegionName
parameter_list|,
specifier|final
name|HRegionInterface
name|srvr
parameter_list|,
specifier|final
name|HRegionInfo
name|parent
parameter_list|,
name|RowResult
name|rowContent
parameter_list|)
throws|throws
name|IOException
block|{
name|boolean
name|result
init|=
literal|false
decl_stmt|;
name|boolean
name|hasReferencesA
init|=
name|hasReferences
argument_list|(
name|metaRegionName
argument_list|,
name|srvr
argument_list|,
name|parent
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|rowContent
argument_list|,
name|COL_SPLITA
argument_list|)
decl_stmt|;
name|boolean
name|hasReferencesB
init|=
name|hasReferences
argument_list|(
name|metaRegionName
argument_list|,
name|srvr
argument_list|,
name|parent
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|rowContent
argument_list|,
name|COL_SPLITB
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|hasReferencesA
operator|&&
operator|!
name|hasReferencesB
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Deleting region "
operator|+
name|parent
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|" because daughter splits no longer hold references"
argument_list|)
expr_stmt|;
name|HRegion
operator|.
name|deleteRegion
argument_list|(
name|master
operator|.
name|fs
argument_list|,
name|master
operator|.
name|rootdir
argument_list|,
name|parent
argument_list|)
expr_stmt|;
name|HRegion
operator|.
name|removeRegionFromMETA
argument_list|(
name|srvr
argument_list|,
name|metaRegionName
argument_list|,
name|parent
operator|.
name|getRegionName
argument_list|()
argument_list|)
expr_stmt|;
name|result
operator|=
literal|true
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
comment|/*     * Checks if a daughter region -- either splitA or splitB -- still holds    * references to parent.  If not, removes reference to the split from    * the parent meta region row.    * @param metaRegionName Name of meta region to look in.    * @param srvr Where region resides.    * @param parent Parent region name.     * @param rowContent Keyed content of the parent row in meta region.    * @param splitColumn Column name of daughter split to examine    * @return True if still has references to parent.    * @throws IOException    */
specifier|private
name|boolean
name|hasReferences
parameter_list|(
specifier|final
name|byte
index|[]
name|metaRegionName
parameter_list|,
specifier|final
name|HRegionInterface
name|srvr
parameter_list|,
specifier|final
name|byte
index|[]
name|parent
parameter_list|,
name|RowResult
name|rowContent
parameter_list|,
specifier|final
name|byte
index|[]
name|splitColumn
parameter_list|)
throws|throws
name|IOException
block|{
name|boolean
name|result
init|=
literal|false
decl_stmt|;
name|HRegionInfo
name|split
init|=
name|Writables
operator|.
name|getHRegionInfo
argument_list|(
name|rowContent
operator|.
name|get
argument_list|(
name|splitColumn
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|split
operator|==
literal|null
condition|)
block|{
return|return
name|result
return|;
block|}
name|Path
name|tabledir
init|=
name|HTableDescriptor
operator|.
name|getTableDir
argument_list|(
name|master
operator|.
name|rootdir
argument_list|,
name|split
operator|.
name|getTableDesc
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|HColumnDescriptor
name|family
range|:
name|split
operator|.
name|getTableDesc
argument_list|()
operator|.
name|getFamilies
argument_list|()
control|)
block|{
name|Path
name|p
init|=
name|HStoreFile
operator|.
name|getMapDir
argument_list|(
name|tabledir
argument_list|,
name|split
operator|.
name|getEncodedName
argument_list|()
argument_list|,
name|family
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
comment|// Look for reference files.  Call listStatus with an anonymous
comment|// instance of PathFilter.
name|FileStatus
index|[]
name|ps
init|=
name|master
operator|.
name|fs
operator|.
name|listStatus
argument_list|(
name|p
argument_list|,
operator|new
name|PathFilter
argument_list|()
block|{
specifier|public
name|boolean
name|accept
parameter_list|(
name|Path
name|path
parameter_list|)
block|{
return|return
name|HStore
operator|.
name|isReference
argument_list|(
name|path
argument_list|)
return|;
block|}
block|}
argument_list|)
decl_stmt|;
if|if
condition|(
name|ps
operator|!=
literal|null
operator|&&
name|ps
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|result
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
name|result
condition|)
block|{
return|return
name|result
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
name|split
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|" no longer has references to "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|parent
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|BatchUpdate
name|b
init|=
operator|new
name|BatchUpdate
argument_list|(
name|parent
argument_list|)
decl_stmt|;
name|b
operator|.
name|delete
argument_list|(
name|splitColumn
argument_list|)
expr_stmt|;
name|srvr
operator|.
name|batchUpdate
argument_list|(
name|metaRegionName
argument_list|,
name|b
argument_list|,
operator|-
literal|1L
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
specifier|protected
name|void
name|checkAssigned
parameter_list|(
specifier|final
name|HRegionInfo
name|info
parameter_list|,
specifier|final
name|String
name|serverName
parameter_list|,
specifier|final
name|long
name|startCode
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Skip region - if ...
if|if
condition|(
name|info
operator|.
name|isOffline
argument_list|()
comment|// offline
operator|||
name|regionManager
operator|.
name|isClosing
argument_list|(
name|info
operator|.
name|getRegionName
argument_list|()
argument_list|)
condition|)
block|{
comment|// queued for offline
name|regionManager
operator|.
name|noLongerUnassigned
argument_list|(
name|info
argument_list|)
expr_stmt|;
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
return|return;
block|}
name|HServerInfo
name|storedInfo
init|=
literal|null
decl_stmt|;
name|boolean
name|deadServer
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|serverName
operator|.
name|length
argument_list|()
operator|!=
literal|0
condition|)
block|{
if|if
condition|(
name|regionManager
operator|.
name|isMarkedToClose
argument_list|(
name|serverName
argument_list|,
name|info
operator|.
name|getRegionName
argument_list|()
argument_list|)
condition|)
block|{
comment|// Skip if region is on kill list
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
literal|"not assigning region (on kill list): "
operator|+
name|info
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return;
block|}
name|storedInfo
operator|=
name|master
operator|.
name|serverManager
operator|.
name|getServerInfo
argument_list|(
name|serverName
argument_list|)
expr_stmt|;
name|deadServer
operator|=
name|master
operator|.
name|serverManager
operator|.
name|isDead
argument_list|(
name|serverName
argument_list|)
expr_stmt|;
block|}
comment|/*      * If the server is a dead server or its startcode is off -- either null      * or doesn't match the start code for the address -- then add it to the      * list of unassigned regions IF not already there (or pending open).      */
if|if
condition|(
operator|!
name|deadServer
operator|&&
operator|!
name|regionManager
operator|.
name|isUnassigned
argument_list|(
name|info
argument_list|)
operator|&&
operator|!
name|regionManager
operator|.
name|isPending
argument_list|(
name|info
operator|.
name|getRegionName
argument_list|()
argument_list|)
operator|&&
operator|(
name|storedInfo
operator|==
literal|null
operator|||
name|storedInfo
operator|.
name|getStartCode
argument_list|()
operator|!=
name|startCode
operator|)
condition|)
block|{
comment|// The current assignment is invalid
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
literal|"Current assignment of "
operator|+
name|info
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|" is not valid: serverInfo: "
operator|+
name|storedInfo
operator|+
literal|", passed startCode: "
operator|+
name|startCode
operator|+
literal|", storedInfo.startCode: "
operator|+
operator|(
operator|(
name|storedInfo
operator|!=
literal|null
operator|)
condition|?
name|storedInfo
operator|.
name|getStartCode
argument_list|()
else|:
operator|-
literal|1
operator|)
operator|+
literal|", unassignedRegions: "
operator|+
name|regionManager
operator|.
name|isUnassigned
argument_list|(
name|info
argument_list|)
operator|+
literal|", pendingRegions: "
operator|+
name|regionManager
operator|.
name|isPending
argument_list|(
name|info
operator|.
name|getRegionName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Recover the region server's log if there is one.
comment|// This is only done from here if we are restarting and there is stale
comment|// data in the meta region. Once we are on-line, dead server log
comment|// recovery is handled by lease expiration and ProcessServerShutdown
if|if
condition|(
operator|!
name|regionManager
operator|.
name|isInitialMetaScanComplete
argument_list|()
operator|&&
name|serverName
operator|.
name|length
argument_list|()
operator|!=
literal|0
condition|)
block|{
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
name|serverName
operator|.
name|replace
argument_list|(
literal|":"
argument_list|,
literal|"_"
argument_list|)
argument_list|)
expr_stmt|;
name|Path
name|logDir
init|=
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
decl_stmt|;
try|try
block|{
if|if
condition|(
name|master
operator|.
name|fs
operator|.
name|exists
argument_list|(
name|logDir
argument_list|)
condition|)
block|{
name|regionManager
operator|.
name|splitLogLock
operator|.
name|lock
argument_list|()
expr_stmt|;
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
name|logDir
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
name|regionManager
operator|.
name|splitLogLock
operator|.
name|unlock
argument_list|()
expr_stmt|;
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
literal|"Split "
operator|+
name|logDir
operator|.
name|toString
argument_list|()
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
literal|"unable to split region server log because: "
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
comment|// Now get the region assigned
name|regionManager
operator|.
name|setUnassigned
argument_list|(
name|info
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Notify the thread to die at the end of its next run    */
specifier|public
name|void
name|interruptIfAlive
parameter_list|()
block|{
synchronized|synchronized
init|(
name|scannerLock
init|)
block|{
if|if
condition|(
name|isAlive
argument_list|()
condition|)
block|{
name|super
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

