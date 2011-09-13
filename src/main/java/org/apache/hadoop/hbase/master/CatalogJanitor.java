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
name|TableExistsException
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
name|Store
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
name|StoreFile
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
name|Writables
import|;
end_import

begin_comment
comment|/**  * A janitor for the catalog tables.  Scans the<code>.META.</code> catalog  * table on a period looking for unused regions to garbage collect.  */
end_comment

begin_class
class|class
name|CatalogJanitor
extends|extends
name|Chore
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
name|Server
name|server
decl_stmt|;
specifier|private
specifier|final
name|MasterServices
name|services
decl_stmt|;
specifier|private
name|boolean
name|enabled
init|=
literal|true
decl_stmt|;
name|CatalogJanitor
parameter_list|(
specifier|final
name|Server
name|server
parameter_list|,
specifier|final
name|MasterServices
name|services
parameter_list|)
block|{
name|super
argument_list|(
name|server
operator|.
name|getServerName
argument_list|()
operator|+
literal|"-CatalogJanitor"
argument_list|,
name|server
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
argument_list|,
name|server
argument_list|)
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
name|void
name|setEnabled
parameter_list|(
specifier|final
name|boolean
name|enabled
parameter_list|)
block|{
name|this
operator|.
name|enabled
operator|=
name|enabled
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
literal|"Failed scan of catalog table"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Run janitorial scan of catalog<code>.META.</code> table looking for    * garbage to collect.    * @throws IOException    */
name|void
name|scan
parameter_list|()
throws|throws
name|IOException
block|{
comment|// TODO: Only works with single .META. region currently.  Fix.
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
argument_list|<
name|HRegionInfo
argument_list|,
name|Result
argument_list|>
argument_list|(
operator|new
name|SplitParentFirstComparator
argument_list|()
argument_list|)
decl_stmt|;
comment|// This visitor collects split parents and counts rows in the .META. table
name|MetaReader
operator|.
name|Visitor
name|visitor
init|=
operator|new
name|MetaReader
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
comment|// Returning true means "keep scanning"
return|return
literal|true
return|;
block|}
block|}
decl_stmt|;
comment|// Run full scan of .META. catalog table passing in our custom visitor
name|MetaReader
operator|.
name|fullScan
argument_list|(
name|this
operator|.
name|server
operator|.
name|getCatalogTracker
argument_list|()
argument_list|,
name|visitor
argument_list|)
expr_stmt|;
comment|// Now work on our list of found parents. See if any we can clean up.
name|int
name|cleaned
init|=
literal|0
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
name|cleaned
operator|++
expr_stmt|;
block|}
if|if
condition|(
name|cleaned
operator|!=
literal|0
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Scanned "
operator|+
name|count
operator|.
name|get
argument_list|()
operator|+
literal|" catalog row(s) and gc'd "
operator|+
name|cleaned
operator|+
literal|" unreferenced parent region(s)"
argument_list|)
expr_stmt|;
block|}
elseif|else
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
literal|"Scanned "
operator|+
name|count
operator|.
name|get
argument_list|()
operator|+
literal|" catalog row(s) and gc'd "
operator|+
name|cleaned
operator|+
literal|" unreferenced parent region(s)"
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
name|Bytes
operator|.
name|compareTo
argument_list|(
name|left
operator|.
name|getTableName
argument_list|()
argument_list|,
name|right
operator|.
name|getTableName
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
comment|// Compare end keys.
name|result
operator|=
name|Bytes
operator|.
name|compareTo
argument_list|(
name|left
operator|.
name|getEndKey
argument_list|()
argument_list|,
name|right
operator|.
name|getEndKey
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
operator|-
name|result
return|;
comment|// Flip the result so parent comes first.
return|return
name|result
return|;
block|}
block|}
comment|/**    * Get HRegionInfo from passed Map of row values.    * @param result Map to do lookup in.    * @return Null if not found (and logs fact that expected COL_REGIONINFO    * was missing) else deserialized {@link HRegionInfo}    * @throws IOException    */
specifier|static
name|HRegionInfo
name|getHRegionInfo
parameter_list|(
specifier|final
name|Result
name|result
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
index|[]
name|bytes
init|=
name|result
operator|.
name|getValue
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|HConstants
operator|.
name|REGIONINFO_QUALIFIER
argument_list|)
decl_stmt|;
if|if
condition|(
name|bytes
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"REGIONINFO_QUALIFIER is empty in "
operator|+
name|result
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
return|return
name|Writables
operator|.
name|getHRegionInfo
argument_list|(
name|bytes
argument_list|)
return|;
block|}
comment|/**    * If daughters no longer hold reference to the parents, delete the parent.    * @param server HRegionInterface of meta server to talk to     * @param parent HRegionInfo of split offlined parent    * @param rowContent Content of<code>parent</code> row in    *<code>metaRegionName</code>    * @return True if we removed<code>parent</code> from meta table and from    * the filesystem.    * @throws IOException    */
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
name|boolean
name|result
init|=
literal|false
decl_stmt|;
comment|// Run checks on each daughter split.
name|Pair
argument_list|<
name|Boolean
argument_list|,
name|Boolean
argument_list|>
name|a
init|=
name|checkDaughter
argument_list|(
name|parent
argument_list|,
name|rowContent
argument_list|,
name|HConstants
operator|.
name|SPLITA_QUALIFIER
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
name|checkDaughter
argument_list|(
name|parent
argument_list|,
name|rowContent
argument_list|,
name|HConstants
operator|.
name|SPLITB_QUALIFIER
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
name|LOG
operator|.
name|debug
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
comment|// This latter regionOffline should not be necessary but is done for now
comment|// until we let go of regionserver to master heartbeats.  See HBASE-3368.
if|if
condition|(
name|this
operator|.
name|services
operator|.
name|getAssignmentManager
argument_list|()
operator|!=
literal|null
condition|)
block|{
comment|// The mock used in testing catalogjanitor returns null for getAssignmnetManager.
comment|// Allow for null result out of getAssignmentManager.
name|this
operator|.
name|services
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|regionOffline
argument_list|(
name|parent
argument_list|)
expr_stmt|;
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
name|HRegion
operator|.
name|deleteRegion
argument_list|(
name|fs
argument_list|,
name|rootdir
argument_list|,
name|parent
argument_list|)
expr_stmt|;
name|MetaEditor
operator|.
name|deleteRegion
argument_list|(
name|this
operator|.
name|server
operator|.
name|getCatalogTracker
argument_list|()
argument_list|,
name|parent
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
comment|/**    * See if the passed daughter has references in the filesystem to the parent    * and if not, remove the note of daughter region in the parent row: its    * column info:splitA or info:splitB.    * @param parent    * @param rowContent    * @param qualifier    * @return A pair where the first boolean says whether or not the daughter    * region directory exists in the filesystem and then the second boolean says    * whether the daughter has references to the parent.    * @throws IOException    */
name|Pair
argument_list|<
name|Boolean
argument_list|,
name|Boolean
argument_list|>
name|checkDaughter
parameter_list|(
specifier|final
name|HRegionInfo
name|parent
parameter_list|,
specifier|final
name|Result
name|rowContent
parameter_list|,
specifier|final
name|byte
index|[]
name|qualifier
parameter_list|)
throws|throws
name|IOException
block|{
name|HRegionInfo
name|hri
init|=
name|getDaughterRegionInfo
argument_list|(
name|rowContent
argument_list|,
name|qualifier
argument_list|)
decl_stmt|;
name|Pair
argument_list|<
name|Boolean
argument_list|,
name|Boolean
argument_list|>
name|result
init|=
name|checkDaughterInFs
argument_list|(
name|parent
argument_list|,
name|rowContent
argument_list|,
name|hri
argument_list|,
name|qualifier
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|.
name|getFirst
argument_list|()
operator|&&
operator|!
name|result
operator|.
name|getSecond
argument_list|()
condition|)
block|{
comment|// Remove daughter from the parent IFF the daughter region exists in FS.
comment|// If there is no daughter region in the filesystem, must be because of
comment|// a failed split.  The ServerShutdownHandler will do the fixup.  Don't
comment|// do any deletes in here that could intefere with ServerShutdownHandler
comment|// fixup
name|removeDaughterFromParent
argument_list|(
name|parent
argument_list|,
name|hri
argument_list|,
name|qualifier
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
comment|/**    * Get daughter HRegionInfo out of parent info:splitA/info:splitB columns.    * @param result    * @param which Whether "info:splitA" or "info:splitB" column    * @return Deserialized content of the info:splitA or info:splitB as a    * HRegionInfo    * @throws IOException    */
specifier|private
name|HRegionInfo
name|getDaughterRegionInfo
parameter_list|(
specifier|final
name|Result
name|result
parameter_list|,
specifier|final
name|byte
index|[]
name|which
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
index|[]
name|bytes
init|=
name|result
operator|.
name|getValue
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|which
argument_list|)
decl_stmt|;
return|return
name|Writables
operator|.
name|getHRegionInfoOrNull
argument_list|(
name|bytes
argument_list|)
return|;
block|}
comment|/**    * Remove mention of daughter from parent row.    * parent row.    * @param metaRegionName    * @param srvr    * @param parent    * @param split    * @param qualifier    * @throws IOException    */
specifier|private
name|void
name|removeDaughterFromParent
parameter_list|(
specifier|final
name|HRegionInfo
name|parent
parameter_list|,
specifier|final
name|HRegionInfo
name|split
parameter_list|,
specifier|final
name|byte
index|[]
name|qualifier
parameter_list|)
throws|throws
name|IOException
block|{
name|MetaEditor
operator|.
name|deleteDaughterReferenceInParent
argument_list|(
name|this
operator|.
name|server
operator|.
name|getCatalogTracker
argument_list|()
argument_list|,
name|parent
argument_list|,
name|qualifier
argument_list|,
name|split
argument_list|)
expr_stmt|;
block|}
comment|/**    * Checks if a daughter region -- either splitA or splitB -- still holds    * references to parent.    * @param parent Parent region name.     * @param rowContent Keyed content of the parent row in meta region.    * @param split Which column family.    * @param qualifier Which of the daughters to look at, splitA or splitB.    * @return A pair where the first boolean says whether or not the daughter    * region directory exists in the filesystem and then the second boolean says    * whether the daughter has references to the parent.    * @throws IOException    */
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
name|Result
name|rowContent
parameter_list|,
specifier|final
name|HRegionInfo
name|split
parameter_list|,
specifier|final
name|byte
index|[]
name|qualifier
parameter_list|)
throws|throws
name|IOException
block|{
name|boolean
name|references
init|=
literal|false
decl_stmt|;
name|boolean
name|exists
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|split
operator|==
literal|null
condition|)
block|{
return|return
operator|new
name|Pair
argument_list|<
name|Boolean
argument_list|,
name|Boolean
argument_list|>
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
operator|new
name|Path
argument_list|(
name|rootdir
argument_list|,
name|split
operator|.
name|getTableNameAsString
argument_list|()
argument_list|)
decl_stmt|;
name|Path
name|regiondir
init|=
operator|new
name|Path
argument_list|(
name|tabledir
argument_list|,
name|split
operator|.
name|getEncodedName
argument_list|()
argument_list|)
decl_stmt|;
name|exists
operator|=
name|fs
operator|.
name|exists
argument_list|(
name|regiondir
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|exists
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Daughter regiondir does not exist: "
operator|+
name|regiondir
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
return|return
operator|new
name|Pair
argument_list|<
name|Boolean
argument_list|,
name|Boolean
argument_list|>
argument_list|(
name|exists
argument_list|,
name|Boolean
operator|.
name|FALSE
argument_list|)
return|;
block|}
name|HTableDescriptor
name|parentDescriptor
init|=
name|getTableDescriptor
argument_list|(
name|parent
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
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
name|Path
name|p
init|=
name|Store
operator|.
name|getStoreHomedir
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
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|p
argument_list|)
condition|)
continue|continue;
comment|// Look for reference files.  Call listStatus with anonymous instance of PathFilter.
name|FileStatus
index|[]
name|ps
init|=
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
name|StoreFile
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
name|references
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
return|return
operator|new
name|Pair
argument_list|<
name|Boolean
argument_list|,
name|Boolean
argument_list|>
argument_list|(
name|Boolean
operator|.
name|valueOf
argument_list|(
name|exists
argument_list|)
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
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|TableExistsException
throws|,
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
name|Bytes
operator|.
name|toString
argument_list|(
name|tableName
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit

