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
name|util
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
name|classification
operator|.
name|InterfaceStability
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
name|conf
operator|.
name|Configured
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
name|HBaseConfiguration
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
name|exceptions
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
name|exceptions
operator|.
name|ZooKeeperConnectionException
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
name|Get
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
name|HBaseAdmin
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
name|wal
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
name|io
operator|.
name|WritableComparator
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
name|util
operator|.
name|GenericOptionsParser
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
name|util
operator|.
name|Tool
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
name|util
operator|.
name|ToolRunner
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
import|;
end_import

begin_comment
comment|/**  * Utility that can merge any two regions in the same table: adjacent,  * overlapping or disjoint.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|Merge
extends|extends
name|Configured
implements|implements
name|Tool
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
name|Merge
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|Path
name|rootdir
decl_stmt|;
specifier|private
specifier|volatile
name|MetaUtils
name|utils
decl_stmt|;
specifier|private
name|byte
index|[]
name|tableName
decl_stmt|;
comment|// Name of table
specifier|private
specifier|volatile
name|byte
index|[]
name|region1
decl_stmt|;
comment|// Name of region 1
specifier|private
specifier|volatile
name|byte
index|[]
name|region2
decl_stmt|;
comment|// Name of region 2
specifier|private
specifier|volatile
name|boolean
name|isMetaTable
decl_stmt|;
specifier|private
specifier|volatile
name|HRegionInfo
name|mergeInfo
decl_stmt|;
comment|/** default constructor */
specifier|public
name|Merge
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
comment|/**    * @param conf configuration    */
specifier|public
name|Merge
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|mergeInfo
operator|=
literal|null
expr_stmt|;
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
specifier|public
name|int
name|run
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|parseArgs
argument_list|(
name|args
argument_list|)
operator|!=
literal|0
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
comment|// Verify file system is up.
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
comment|// get DFS handle
name|LOG
operator|.
name|info
argument_list|(
literal|"Verifying that file system is available..."
argument_list|)
expr_stmt|;
try|try
block|{
name|FSUtils
operator|.
name|checkFileSystemAvailable
argument_list|(
name|fs
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
name|fatal
argument_list|(
literal|"File system is not available"
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
operator|-
literal|1
return|;
block|}
comment|// Verify HBase is down
name|LOG
operator|.
name|info
argument_list|(
literal|"Verifying that HBase is not running..."
argument_list|)
expr_stmt|;
try|try
block|{
name|HBaseAdmin
operator|.
name|checkHBaseAvailable
argument_list|(
name|getConf
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|fatal
argument_list|(
literal|"HBase cluster must be off-line, and is not. Aborting."
argument_list|)
expr_stmt|;
return|return
operator|-
literal|1
return|;
block|}
catch|catch
parameter_list|(
name|ZooKeeperConnectionException
name|zkce
parameter_list|)
block|{
comment|// If no zk, presume no master.
block|}
catch|catch
parameter_list|(
name|MasterNotRunningException
name|e
parameter_list|)
block|{
comment|// Expected. Ignore.
block|}
comment|// Initialize MetaUtils and and get the root of the HBase installation
name|this
operator|.
name|utils
operator|=
operator|new
name|MetaUtils
argument_list|(
name|getConf
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|rootdir
operator|=
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|getConf
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
if|if
condition|(
name|isMetaTable
condition|)
block|{
name|mergeTwoMetaRegions
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|mergeTwoRegions
argument_list|()
expr_stmt|;
block|}
return|return
literal|0
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|fatal
argument_list|(
literal|"Merge failed"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|utils
operator|.
name|scanMetaRegion
argument_list|(
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
argument_list|,
operator|new
name|MetaUtils
operator|.
name|ScannerListener
argument_list|()
block|{
specifier|public
name|boolean
name|processRow
parameter_list|(
name|HRegionInfo
name|info
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
name|info
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
argument_list|)
expr_stmt|;
return|return
operator|-
literal|1
return|;
block|}
finally|finally
block|{
if|if
condition|(
name|this
operator|.
name|utils
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|utils
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
block|}
block|}
comment|/** @return HRegionInfo for merge result */
name|HRegionInfo
name|getMergedHRegionInfo
parameter_list|()
block|{
return|return
name|this
operator|.
name|mergeInfo
return|;
block|}
comment|/*    * Merge two meta regions. This is unlikely to be needed soon as we have only    * seend the meta table split once and that was with 64MB regions. With 256MB    * regions, it will be some time before someone has enough data in HBase to    * split the meta region and even less likely that a merge of two meta    * regions will be needed, but it is included for completeness.    */
specifier|private
name|void
name|mergeTwoMetaRegions
parameter_list|()
throws|throws
name|IOException
block|{
name|HRegion
name|rootRegion
init|=
name|utils
operator|.
name|getRootRegion
argument_list|()
decl_stmt|;
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|region1
argument_list|)
decl_stmt|;
name|get
operator|.
name|addColumn
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|HConstants
operator|.
name|REGIONINFO_QUALIFIER
argument_list|)
expr_stmt|;
name|Result
name|result1
init|=
name|rootRegion
operator|.
name|get
argument_list|(
name|get
argument_list|)
decl_stmt|;
name|Preconditions
operator|.
name|checkState
argument_list|(
operator|!
name|result1
operator|.
name|isEmpty
argument_list|()
argument_list|,
literal|"First region cells can not be null"
argument_list|)
expr_stmt|;
name|HRegionInfo
name|info1
init|=
name|HRegionInfo
operator|.
name|getHRegionInfo
argument_list|(
name|result1
argument_list|)
decl_stmt|;
name|get
operator|=
operator|new
name|Get
argument_list|(
name|region2
argument_list|)
expr_stmt|;
name|get
operator|.
name|addColumn
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|HConstants
operator|.
name|REGIONINFO_QUALIFIER
argument_list|)
expr_stmt|;
name|Result
name|result2
init|=
name|rootRegion
operator|.
name|get
argument_list|(
name|get
argument_list|)
decl_stmt|;
name|Preconditions
operator|.
name|checkState
argument_list|(
operator|!
name|result2
operator|.
name|isEmpty
argument_list|()
argument_list|,
literal|"Second region cells can not be null"
argument_list|)
expr_stmt|;
name|HRegionInfo
name|info2
init|=
name|HRegionInfo
operator|.
name|getHRegionInfo
argument_list|(
name|result2
argument_list|)
decl_stmt|;
name|HRegion
name|merged
init|=
name|merge
argument_list|(
name|HTableDescriptor
operator|.
name|META_TABLEDESC
argument_list|,
name|info1
argument_list|,
name|rootRegion
argument_list|,
name|info2
argument_list|,
name|rootRegion
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Adding "
operator|+
name|merged
operator|.
name|getRegionInfo
argument_list|()
operator|+
literal|" to "
operator|+
name|rootRegion
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
expr_stmt|;
name|HRegion
operator|.
name|addRegionToMETA
argument_list|(
name|rootRegion
argument_list|,
name|merged
argument_list|)
expr_stmt|;
name|merged
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
specifier|private
specifier|static
class|class
name|MetaScannerListener
implements|implements
name|MetaUtils
operator|.
name|ScannerListener
block|{
specifier|private
specifier|final
name|byte
index|[]
name|region1
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|region2
decl_stmt|;
specifier|private
name|HRegionInfo
name|meta1
init|=
literal|null
decl_stmt|;
specifier|private
name|HRegionInfo
name|meta2
init|=
literal|null
decl_stmt|;
name|MetaScannerListener
parameter_list|(
specifier|final
name|byte
index|[]
name|region1
parameter_list|,
specifier|final
name|byte
index|[]
name|region2
parameter_list|)
block|{
name|this
operator|.
name|region1
operator|=
name|region1
expr_stmt|;
name|this
operator|.
name|region2
operator|=
name|region2
expr_stmt|;
block|}
specifier|public
name|boolean
name|processRow
parameter_list|(
name|HRegionInfo
name|info
parameter_list|)
block|{
if|if
condition|(
name|meta1
operator|==
literal|null
operator|&&
name|HRegion
operator|.
name|rowIsInRange
argument_list|(
name|info
argument_list|,
name|region1
argument_list|)
condition|)
block|{
name|meta1
operator|=
name|info
expr_stmt|;
block|}
if|if
condition|(
name|region2
operator|!=
literal|null
operator|&&
name|meta2
operator|==
literal|null
operator|&&
name|HRegion
operator|.
name|rowIsInRange
argument_list|(
name|info
argument_list|,
name|region2
argument_list|)
condition|)
block|{
name|meta2
operator|=
name|info
expr_stmt|;
block|}
return|return
name|meta1
operator|==
literal|null
operator|||
operator|(
name|region2
operator|!=
literal|null
operator|&&
name|meta2
operator|==
literal|null
operator|)
return|;
block|}
name|HRegionInfo
name|getMeta1
parameter_list|()
block|{
return|return
name|meta1
return|;
block|}
name|HRegionInfo
name|getMeta2
parameter_list|()
block|{
return|return
name|meta2
return|;
block|}
block|}
comment|/*    * Merges two regions from a user table.    */
specifier|private
name|void
name|mergeTwoRegions
parameter_list|()
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Merging regions "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|this
operator|.
name|region1
argument_list|)
operator|+
literal|" and "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|this
operator|.
name|region2
argument_list|)
operator|+
literal|" in table "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|this
operator|.
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
comment|// Scan the root region for all the meta regions that contain the regions
comment|// we're merging.
name|MetaScannerListener
name|listener
init|=
operator|new
name|MetaScannerListener
argument_list|(
name|region1
argument_list|,
name|region2
argument_list|)
decl_stmt|;
name|this
operator|.
name|utils
operator|.
name|scanRootRegion
argument_list|(
name|listener
argument_list|)
expr_stmt|;
name|HRegionInfo
name|meta1
init|=
name|listener
operator|.
name|getMeta1
argument_list|()
decl_stmt|;
if|if
condition|(
name|meta1
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Could not find meta region for "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|region1
argument_list|)
argument_list|)
throw|;
block|}
name|HRegionInfo
name|meta2
init|=
name|listener
operator|.
name|getMeta2
argument_list|()
decl_stmt|;
if|if
condition|(
name|meta2
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Could not find meta region for "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|region2
argument_list|)
argument_list|)
throw|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Found meta for region1 "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|meta1
operator|.
name|getRegionName
argument_list|()
argument_list|)
operator|+
literal|", meta for region2 "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|meta2
operator|.
name|getRegionName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|HRegion
name|metaRegion1
init|=
name|this
operator|.
name|utils
operator|.
name|getMetaRegion
argument_list|(
name|meta1
argument_list|)
decl_stmt|;
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|region1
argument_list|)
decl_stmt|;
name|get
operator|.
name|addColumn
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|HConstants
operator|.
name|REGIONINFO_QUALIFIER
argument_list|)
expr_stmt|;
name|Result
name|result1
init|=
name|metaRegion1
operator|.
name|get
argument_list|(
name|get
argument_list|)
decl_stmt|;
name|Preconditions
operator|.
name|checkState
argument_list|(
operator|!
name|result1
operator|.
name|isEmpty
argument_list|()
argument_list|,
literal|"First region cells can not be null"
argument_list|)
expr_stmt|;
name|HRegionInfo
name|info1
init|=
name|HRegionInfo
operator|.
name|getHRegionInfo
argument_list|(
name|result1
argument_list|)
decl_stmt|;
if|if
condition|(
name|info1
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|NullPointerException
argument_list|(
literal|"info1 is null using key "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|region1
argument_list|)
operator|+
literal|" in "
operator|+
name|meta1
argument_list|)
throw|;
block|}
name|HRegion
name|metaRegion2
decl_stmt|;
if|if
condition|(
name|Bytes
operator|.
name|equals
argument_list|(
name|meta1
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|meta2
operator|.
name|getRegionName
argument_list|()
argument_list|)
condition|)
block|{
name|metaRegion2
operator|=
name|metaRegion1
expr_stmt|;
block|}
else|else
block|{
name|metaRegion2
operator|=
name|utils
operator|.
name|getMetaRegion
argument_list|(
name|meta2
argument_list|)
expr_stmt|;
block|}
name|get
operator|=
operator|new
name|Get
argument_list|(
name|region2
argument_list|)
expr_stmt|;
name|get
operator|.
name|addColumn
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|HConstants
operator|.
name|REGIONINFO_QUALIFIER
argument_list|)
expr_stmt|;
name|Result
name|result2
init|=
name|metaRegion2
operator|.
name|get
argument_list|(
name|get
argument_list|)
decl_stmt|;
name|Preconditions
operator|.
name|checkState
argument_list|(
operator|!
name|result2
operator|.
name|isEmpty
argument_list|()
argument_list|,
literal|"Second region cells can not be null"
argument_list|)
expr_stmt|;
name|HRegionInfo
name|info2
init|=
name|HRegionInfo
operator|.
name|getHRegionInfo
argument_list|(
name|result2
argument_list|)
decl_stmt|;
if|if
condition|(
name|info2
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|NullPointerException
argument_list|(
literal|"info2 is null using key "
operator|+
name|meta2
argument_list|)
throw|;
block|}
name|HTableDescriptor
name|htd
init|=
name|FSTableDescriptors
operator|.
name|getTableDescriptor
argument_list|(
name|FileSystem
operator|.
name|get
argument_list|(
name|getConf
argument_list|()
argument_list|)
argument_list|,
name|this
operator|.
name|rootdir
argument_list|,
name|this
operator|.
name|tableName
argument_list|)
decl_stmt|;
name|HRegion
name|merged
init|=
name|merge
argument_list|(
name|htd
argument_list|,
name|info1
argument_list|,
name|metaRegion1
argument_list|,
name|info2
argument_list|,
name|metaRegion2
argument_list|)
decl_stmt|;
comment|// Now find the meta region which will contain the newly merged region
name|listener
operator|=
operator|new
name|MetaScannerListener
argument_list|(
name|merged
operator|.
name|getRegionName
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|utils
operator|.
name|scanRootRegion
argument_list|(
name|listener
argument_list|)
expr_stmt|;
name|HRegionInfo
name|mergedInfo
init|=
name|listener
operator|.
name|getMeta1
argument_list|()
decl_stmt|;
if|if
condition|(
name|mergedInfo
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Could not find meta region for "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|merged
operator|.
name|getRegionName
argument_list|()
argument_list|)
argument_list|)
throw|;
block|}
name|HRegion
name|mergeMeta
decl_stmt|;
if|if
condition|(
name|Bytes
operator|.
name|equals
argument_list|(
name|mergedInfo
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|meta1
operator|.
name|getRegionName
argument_list|()
argument_list|)
condition|)
block|{
name|mergeMeta
operator|=
name|metaRegion1
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|Bytes
operator|.
name|equals
argument_list|(
name|mergedInfo
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|meta2
operator|.
name|getRegionName
argument_list|()
argument_list|)
condition|)
block|{
name|mergeMeta
operator|=
name|metaRegion2
expr_stmt|;
block|}
else|else
block|{
name|mergeMeta
operator|=
name|utils
operator|.
name|getMetaRegion
argument_list|(
name|mergedInfo
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Adding "
operator|+
name|merged
operator|.
name|getRegionInfo
argument_list|()
operator|+
literal|" to "
operator|+
name|mergeMeta
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
expr_stmt|;
name|HRegion
operator|.
name|addRegionToMETA
argument_list|(
name|mergeMeta
argument_list|,
name|merged
argument_list|)
expr_stmt|;
name|merged
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/*    * Actually merge two regions and update their info in the meta region(s)    * If the meta is split, meta1 may be different from meta2. (and we may have    * to scan the meta if the resulting merged region does not go in either)    * Returns HRegion object for newly merged region    */
specifier|private
name|HRegion
name|merge
parameter_list|(
specifier|final
name|HTableDescriptor
name|htd
parameter_list|,
name|HRegionInfo
name|info1
parameter_list|,
name|HRegion
name|meta1
parameter_list|,
name|HRegionInfo
name|info2
parameter_list|,
name|HRegion
name|meta2
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|info1
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Could not find "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|region1
argument_list|)
operator|+
literal|" in "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|meta1
operator|.
name|getRegionName
argument_list|()
argument_list|)
argument_list|)
throw|;
block|}
if|if
condition|(
name|info2
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Cound not find "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|region2
argument_list|)
operator|+
literal|" in "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|meta2
operator|.
name|getRegionName
argument_list|()
argument_list|)
argument_list|)
throw|;
block|}
name|HRegion
name|merged
init|=
literal|null
decl_stmt|;
name|HLog
name|log
init|=
name|utils
operator|.
name|getLog
argument_list|()
decl_stmt|;
name|HRegion
name|r1
init|=
name|HRegion
operator|.
name|openHRegion
argument_list|(
name|info1
argument_list|,
name|htd
argument_list|,
name|log
argument_list|,
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|HRegion
name|r2
init|=
name|HRegion
operator|.
name|openHRegion
argument_list|(
name|info2
argument_list|,
name|htd
argument_list|,
name|log
argument_list|,
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|merged
operator|=
name|HRegion
operator|.
name|merge
argument_list|(
name|r1
argument_list|,
name|r2
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
operator|!
name|r2
operator|.
name|isClosed
argument_list|()
condition|)
block|{
name|r2
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
finally|finally
block|{
if|if
condition|(
operator|!
name|r1
operator|.
name|isClosed
argument_list|()
condition|)
block|{
name|r1
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|// Remove the old regions from meta.
comment|// HRegion.merge has already deleted their files
name|removeRegionFromMeta
argument_list|(
name|meta1
argument_list|,
name|info1
argument_list|)
expr_stmt|;
name|removeRegionFromMeta
argument_list|(
name|meta2
argument_list|,
name|info2
argument_list|)
expr_stmt|;
name|this
operator|.
name|mergeInfo
operator|=
name|merged
operator|.
name|getRegionInfo
argument_list|()
expr_stmt|;
return|return
name|merged
return|;
block|}
comment|/*    * Removes a region's meta information from the passed<code>meta</code>    * region.    *    * @param meta META HRegion to be updated    * @param regioninfo HRegionInfo of region to remove from<code>meta</code>    *    * @throws IOException    */
specifier|private
name|void
name|removeRegionFromMeta
parameter_list|(
name|HRegion
name|meta
parameter_list|,
name|HRegionInfo
name|regioninfo
parameter_list|)
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
literal|"Removing region: "
operator|+
name|regioninfo
operator|+
literal|" from "
operator|+
name|meta
argument_list|)
expr_stmt|;
block|}
name|Delete
name|delete
init|=
operator|new
name|Delete
argument_list|(
name|regioninfo
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
decl_stmt|;
name|meta
operator|.
name|delete
argument_list|(
name|delete
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|/*    * Adds a region's meta information from the passed<code>meta</code>    * region.    *    * @param metainfo META HRegionInfo to be updated    * @param region HRegion to add to<code>meta</code>    *    * @throws IOException    */
specifier|private
name|int
name|parseArgs
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|IOException
block|{
name|GenericOptionsParser
name|parser
init|=
operator|new
name|GenericOptionsParser
argument_list|(
name|getConf
argument_list|()
argument_list|,
name|args
argument_list|)
decl_stmt|;
name|String
index|[]
name|remainingArgs
init|=
name|parser
operator|.
name|getRemainingArgs
argument_list|()
decl_stmt|;
if|if
condition|(
name|remainingArgs
operator|.
name|length
operator|!=
literal|3
condition|)
block|{
name|usage
argument_list|()
expr_stmt|;
return|return
operator|-
literal|1
return|;
block|}
name|tableName
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|remainingArgs
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|isMetaTable
operator|=
name|Bytes
operator|.
name|compareTo
argument_list|(
name|tableName
argument_list|,
name|HConstants
operator|.
name|META_TABLE_NAME
argument_list|)
operator|==
literal|0
expr_stmt|;
name|region1
operator|=
name|Bytes
operator|.
name|toBytesBinary
argument_list|(
name|remainingArgs
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
name|region2
operator|=
name|Bytes
operator|.
name|toBytesBinary
argument_list|(
name|remainingArgs
index|[
literal|2
index|]
argument_list|)
expr_stmt|;
name|int
name|status
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|notInTable
argument_list|(
name|tableName
argument_list|,
name|region1
argument_list|)
operator|||
name|notInTable
argument_list|(
name|tableName
argument_list|,
name|region2
argument_list|)
condition|)
block|{
name|status
operator|=
operator|-
literal|1
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|Bytes
operator|.
name|equals
argument_list|(
name|region1
argument_list|,
name|region2
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Can't merge a region with itself"
argument_list|)
expr_stmt|;
name|status
operator|=
operator|-
literal|1
expr_stmt|;
block|}
return|return
name|status
return|;
block|}
specifier|private
name|boolean
name|notInTable
parameter_list|(
specifier|final
name|byte
index|[]
name|tn
parameter_list|,
specifier|final
name|byte
index|[]
name|rn
parameter_list|)
block|{
if|if
condition|(
name|WritableComparator
operator|.
name|compareBytes
argument_list|(
name|tn
argument_list|,
literal|0
argument_list|,
name|tn
operator|.
name|length
argument_list|,
name|rn
argument_list|,
literal|0
argument_list|,
name|tn
operator|.
name|length
argument_list|)
operator|!=
literal|0
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Region "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|rn
argument_list|)
operator|+
literal|" does not belong to table "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|tn
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
specifier|private
name|void
name|usage
parameter_list|()
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"For hadoop 0.20,  Usage: bin/hbase org.apache.hadoop.hbase.util.Merge "
operator|+
literal|"[-Dfs.default.name=hdfs://nn:port]<table-name><region-1><region-2>\n"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"For hadoop 0.21+, Usage: bin/hbase org.apache.hadoop.hbase.util.Merge "
operator|+
literal|"[-Dfs.defaultFS=hdfs://nn:port]<table-name><region-1><region-2>\n"
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
block|{
name|int
name|status
decl_stmt|;
try|try
block|{
name|status
operator|=
name|ToolRunner
operator|.
name|run
argument_list|(
name|HBaseConfiguration
operator|.
name|create
argument_list|()
argument_list|,
operator|new
name|Merge
argument_list|()
argument_list|,
name|args
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"exiting due to error"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|status
operator|=
operator|-
literal|1
expr_stmt|;
block|}
name|System
operator|.
name|exit
argument_list|(
name|status
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

