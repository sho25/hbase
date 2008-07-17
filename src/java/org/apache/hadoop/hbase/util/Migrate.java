begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|commons
operator|.
name|cli
operator|.
name|Options
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

begin_comment
comment|/**  * Perform a file system upgrade to convert older file layouts.  * HBase keeps a file in hdfs named hbase.version just under the hbase.rootdir.  * This file holds the version of the hbase data in the Filesystem.  When the  * software changes in a manner incompatible with the data in the Filesystem,  * it updates its internal version number,  * {@link HConstants#FILE_SYSTEM_VERSION}.  This wrapper script manages moving  * the filesystem across versions until there's a match with current software's  * version number.  *   *<p>This wrapper script comprises a set of migration steps.  Which steps  * are run depends on the span between the version of the hbase data in the  * Filesystem and the version of the current softare.  *   *<p>A migration script must accompany any patch that changes data formats.  *   *<p>This script has a 'check' and 'execute' mode.  Adding migration steps,  * its important to keep this in mind.  Testing if migration needs to be run,  * be careful not to make presumptions about the current state of the data in  * the filesystem.  It may have a format from many versions previous with  * layout not as expected or keys and values of a format not expected.  Tools  * such as {@link MetaUtils} may not work as expected when running against  * old formats -- or, worse, fail in ways that are hard to figure (One such is  * edits made by previous migration steps not being apparent in later migration  * steps).  The upshot is always verify presumptions migrating.  *   *<p>This script will migrate an hbase 0.1 install to a 0.2 install only.  *   * @see<a href="http://wiki.apache.org/hadoop/Hbase/HowToMigrate">How To Migration</a>  */
end_comment

begin_class
specifier|public
class|class
name|Migrate
extends|extends
name|Configured
implements|implements
name|Tool
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
name|Migrate
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|OLD_PREFIX
init|=
literal|"hregion_"
decl_stmt|;
specifier|private
specifier|final
name|HBaseConfiguration
name|conf
decl_stmt|;
specifier|private
name|FileSystem
name|fs
decl_stmt|;
comment|// Gets set by migration methods if we are in readOnly mode.
specifier|private
name|boolean
name|migrationNeeded
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|readOnly
init|=
literal|false
decl_stmt|;
specifier|private
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|references
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
comment|// Filesystem version of hbase 0.1.x.
specifier|private
specifier|static
specifier|final
name|float
name|HBASE_0_1_VERSION
init|=
literal|0.1f
decl_stmt|;
comment|/** default constructor */
specifier|public
name|Migrate
parameter_list|()
block|{
name|this
argument_list|(
operator|new
name|HBaseConfiguration
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param conf    */
specifier|public
name|Migrate
parameter_list|(
name|HBaseConfiguration
name|conf
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
block|}
comment|/*    * Sets the hbase rootdir as fs.default.name.    * @return True if succeeded.    */
specifier|private
name|boolean
name|setFsDefaultName
parameter_list|()
block|{
comment|// Validate root directory path
name|Path
name|rd
init|=
operator|new
name|Path
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|HBASE_DIR
argument_list|)
argument_list|)
decl_stmt|;
try|try
block|{
comment|// Validate root directory path
name|FSUtils
operator|.
name|validateRootPath
argument_list|(
name|rd
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
literal|"Not starting migration because the root directory path '"
operator|+
name|rd
operator|.
name|toString
argument_list|()
operator|+
literal|"' is not valid. Check the setting of the"
operator|+
literal|" configuration parameter '"
operator|+
name|HConstants
operator|.
name|HBASE_DIR
operator|+
literal|"'"
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
name|this
operator|.
name|conf
operator|.
name|set
argument_list|(
literal|"fs.default.name"
argument_list|,
name|rd
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
comment|/*    * @return True if succeeded verifying filesystem.    */
specifier|private
name|boolean
name|verifyFilesystem
parameter_list|()
block|{
try|try
block|{
comment|// Verify file system is up.
name|fs
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
expr_stmt|;
comment|// get DFS handle
name|LOG
operator|.
name|info
argument_list|(
literal|"Verifying that file system is available..."
argument_list|)
expr_stmt|;
name|FSUtils
operator|.
name|checkFileSystemAvailable
argument_list|(
name|fs
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
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
literal|false
return|;
block|}
block|}
specifier|private
name|boolean
name|notRunning
parameter_list|()
block|{
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
name|conf
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|fatal
argument_list|(
literal|"HBase cluster must be off-line."
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
catch|catch
parameter_list|(
name|MasterNotRunningException
name|e
parameter_list|)
block|{
return|return
literal|true
return|;
block|}
block|}
comment|/** {@inheritDoc} */
specifier|public
name|int
name|run
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
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
if|if
condition|(
operator|!
name|setFsDefaultName
argument_list|()
condition|)
block|{
return|return
operator|-
literal|2
return|;
block|}
if|if
condition|(
operator|!
name|verifyFilesystem
argument_list|()
condition|)
block|{
return|return
operator|-
literal|3
return|;
block|}
if|if
condition|(
operator|!
name|notRunning
argument_list|()
condition|)
block|{
return|return
operator|-
literal|4
return|;
block|}
try|try
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting upgrade"
operator|+
operator|(
name|readOnly
condition|?
literal|" check"
else|:
literal|""
operator|)
argument_list|)
expr_stmt|;
comment|// See if there is a file system version file
name|String
name|versionStr
init|=
name|FSUtils
operator|.
name|getVersion
argument_list|(
name|fs
argument_list|,
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|this
operator|.
name|conf
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|versionStr
operator|!=
literal|null
operator|&&
name|versionStr
operator|.
name|compareTo
argument_list|(
name|HConstants
operator|.
name|FILE_SYSTEM_VERSION
argument_list|)
operator|==
literal|0
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"No upgrade necessary."
argument_list|)
expr_stmt|;
return|return
literal|0
return|;
block|}
if|if
condition|(
name|versionStr
operator|==
literal|null
operator|||
name|Float
operator|.
name|parseFloat
argument_list|(
name|versionStr
argument_list|)
operator|<
literal|0.1
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Install 0.1.x of hbase and run its "
operator|+
literal|"migration first"
argument_list|)
throw|;
block|}
name|float
name|version
init|=
name|Float
operator|.
name|parseFloat
argument_list|(
name|versionStr
argument_list|)
decl_stmt|;
if|if
condition|(
name|version
operator|==
literal|0.1f
condition|)
block|{
name|checkForUnrecoveredLogFiles
argument_list|(
name|getRootDirFiles
argument_list|()
argument_list|)
expr_stmt|;
name|migrate
argument_list|()
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unrecognized or non-migratable version: "
operator|+
name|version
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
name|readOnly
condition|)
block|{
comment|// Set file system version
name|LOG
operator|.
name|info
argument_list|(
literal|"Setting file system version."
argument_list|)
expr_stmt|;
name|FSUtils
operator|.
name|setVersion
argument_list|(
name|fs
argument_list|,
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|this
operator|.
name|conf
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Upgrade successful."
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|this
operator|.
name|migrationNeeded
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Upgrade needed."
argument_list|)
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
literal|"Upgrade"
operator|+
operator|(
name|readOnly
condition|?
literal|" check"
else|:
literal|""
operator|)
operator|+
literal|" failed"
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
operator|-
literal|1
return|;
block|}
block|}
specifier|private
name|void
name|migrate
parameter_list|()
throws|throws
name|IOException
block|{
name|addHistorianFamilyToMeta
argument_list|()
expr_stmt|;
name|updateBloomFilters
argument_list|()
expr_stmt|;
block|}
specifier|private
name|FileStatus
index|[]
name|getRootDirFiles
parameter_list|()
throws|throws
name|IOException
block|{
name|FileStatus
index|[]
name|stats
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|this
operator|.
name|conf
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|stats
operator|==
literal|null
operator|||
name|stats
operator|.
name|length
operator|==
literal|0
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"No files found under root directory "
operator|+
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|this
operator|.
name|conf
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
return|return
name|stats
return|;
block|}
specifier|private
name|void
name|checkForUnrecoveredLogFiles
parameter_list|(
name|FileStatus
index|[]
name|rootFiles
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|String
argument_list|>
name|unrecoveredLogs
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|rootFiles
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|String
name|name
init|=
name|rootFiles
index|[
name|i
index|]
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
if|if
condition|(
name|name
operator|.
name|startsWith
argument_list|(
literal|"log_"
argument_list|)
condition|)
block|{
name|unrecoveredLogs
operator|.
name|add
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|unrecoveredLogs
operator|.
name|size
argument_list|()
operator|!=
literal|0
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"There are "
operator|+
name|unrecoveredLogs
operator|.
name|size
argument_list|()
operator|+
literal|" unrecovered region server logs. Please uninstall this version of "
operator|+
literal|"HBase, re-install the previous version, start your cluster and "
operator|+
literal|"shut it down cleanly, so that all region server logs are recovered"
operator|+
literal|" and deleted.  Or, if you are sure logs are vestige of old "
operator|+
literal|"failures in hbase, remove them and then rerun the migration.  "
operator|+
literal|"Here are the problem log files: "
operator|+
name|unrecoveredLogs
argument_list|)
throw|;
block|}
block|}
name|void
name|migrateRegionDir
parameter_list|(
specifier|final
name|byte
index|[]
name|tableName
parameter_list|,
name|String
name|oldPath
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Create directory where table will live
name|Path
name|rootdir
init|=
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|this
operator|.
name|conf
argument_list|)
decl_stmt|;
name|Path
name|tableDir
init|=
operator|new
name|Path
argument_list|(
name|rootdir
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|tableName
argument_list|)
argument_list|)
decl_stmt|;
name|fs
operator|.
name|mkdirs
argument_list|(
name|tableDir
argument_list|)
expr_stmt|;
comment|// Move the old region directory under the table directory
name|Path
name|newPath
init|=
operator|new
name|Path
argument_list|(
name|tableDir
argument_list|,
name|oldPath
operator|.
name|substring
argument_list|(
name|OLD_PREFIX
operator|.
name|length
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|fs
operator|.
name|rename
argument_list|(
operator|new
name|Path
argument_list|(
name|rootdir
argument_list|,
name|oldPath
argument_list|)
argument_list|,
name|newPath
argument_list|)
expr_stmt|;
name|processRegionSubDirs
argument_list|(
name|fs
argument_list|,
name|newPath
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|processRegionSubDirs
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|newPath
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|newName
init|=
name|newPath
operator|.
name|getName
argument_list|()
decl_stmt|;
name|FileStatus
index|[]
name|children
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|newPath
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|children
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|String
name|child
init|=
name|children
index|[
name|i
index|]
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
if|if
condition|(
name|children
index|[
name|i
index|]
operator|.
name|isDir
argument_list|()
condition|)
block|{
name|processRegionSubDirs
argument_list|(
name|fs
argument_list|,
name|children
index|[
name|i
index|]
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
comment|// Rename old compaction directories
if|if
condition|(
name|child
operator|.
name|startsWith
argument_list|(
name|OLD_PREFIX
argument_list|)
condition|)
block|{
name|fs
operator|.
name|rename
argument_list|(
name|children
index|[
name|i
index|]
operator|.
name|getPath
argument_list|()
argument_list|,
operator|new
name|Path
argument_list|(
name|newPath
argument_list|,
name|child
operator|.
name|substring
argument_list|(
name|OLD_PREFIX
operator|.
name|length
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|newName
operator|.
name|compareTo
argument_list|(
literal|"mapfiles"
argument_list|)
operator|==
literal|0
condition|)
block|{
comment|// Check to see if this mapfile is a reference
if|if
condition|(
name|HStore
operator|.
name|isReference
argument_list|(
name|children
index|[
name|i
index|]
operator|.
name|getPath
argument_list|()
argument_list|)
condition|)
block|{
comment|// Keep track of references in case we come across a region
comment|// that we can't otherwise account for.
name|references
operator|.
name|add
argument_list|(
name|child
operator|.
name|substring
argument_list|(
name|child
operator|.
name|indexOf
argument_list|(
literal|"."
argument_list|)
operator|+
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
specifier|private
name|void
name|scanRootRegion
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|MetaUtils
name|utils
init|=
operator|new
name|MetaUtils
argument_list|(
name|this
operator|.
name|conf
argument_list|)
decl_stmt|;
try|try
block|{
name|utils
operator|.
name|scanRootRegion
argument_list|(
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
throws|throws
name|IOException
block|{
comment|// First move the meta region to where it should be and rename
comment|// subdirectories as necessary
name|migrateRegionDir
argument_list|(
name|HConstants
operator|.
name|META_TABLE_NAME
argument_list|,
name|OLD_PREFIX
operator|+
name|info
operator|.
name|getEncodedName
argument_list|()
argument_list|)
expr_stmt|;
name|utils
operator|.
name|scanMetaRegion
argument_list|(
name|info
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
name|tableInfo
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Move the region to where it should be and rename
comment|// subdirectories as necessary
name|migrateRegionDir
argument_list|(
name|tableInfo
operator|.
name|getTableDesc
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|OLD_PREFIX
operator|+
name|tableInfo
operator|.
name|getEncodedName
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
literal|true
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|utils
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|addHistorianFamilyToMeta
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|migrationNeeded
condition|)
block|{
comment|// Be careful. We cannot use MetAutils if current hbase in the
comment|// Filesystem has not been migrated.
return|return;
block|}
name|boolean
name|needed
init|=
literal|false
decl_stmt|;
name|MetaUtils
name|utils
init|=
operator|new
name|MetaUtils
argument_list|(
name|this
operator|.
name|conf
argument_list|)
decl_stmt|;
try|try
block|{
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|metas
init|=
name|utils
operator|.
name|getMETARows
argument_list|(
name|HConstants
operator|.
name|META_TABLE_NAME
argument_list|)
decl_stmt|;
for|for
control|(
name|HRegionInfo
name|meta
range|:
name|metas
control|)
block|{
if|if
condition|(
name|meta
operator|.
name|getTableDesc
argument_list|()
operator|.
name|getFamily
argument_list|(
name|HConstants
operator|.
name|COLUMN_FAMILY_HISTORIAN
argument_list|)
operator|==
literal|null
condition|)
block|{
name|needed
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
name|needed
operator|&&
name|this
operator|.
name|readOnly
condition|)
block|{
name|this
operator|.
name|migrationNeeded
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|utils
operator|.
name|addColumn
argument_list|(
name|HConstants
operator|.
name|META_TABLE_NAME
argument_list|,
operator|new
name|HColumnDescriptor
argument_list|(
name|HConstants
operator|.
name|COLUMN_FAMILY_HISTORIAN
argument_list|,
name|HConstants
operator|.
name|ALL_VERSIONS
argument_list|,
name|HColumnDescriptor
operator|.
name|CompressionType
operator|.
name|NONE
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|,
name|HConstants
operator|.
name|FOREVER
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Historian family added to .META."
argument_list|)
expr_stmt|;
comment|// Flush out the meta edits.
block|}
block|}
finally|finally
block|{
name|utils
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|updateBloomFilters
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|migrationNeeded
operator|&&
name|this
operator|.
name|readOnly
condition|)
block|{
return|return;
block|}
specifier|final
name|Path
name|rootDir
init|=
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|conf
argument_list|)
decl_stmt|;
specifier|final
name|MetaUtils
name|utils
init|=
operator|new
name|MetaUtils
argument_list|(
name|this
operator|.
name|conf
argument_list|)
decl_stmt|;
try|try
block|{
comment|// Scan the root region
name|utils
operator|.
name|scanRootRegion
argument_list|(
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
throws|throws
name|IOException
block|{
comment|// Scan every meta region
specifier|final
name|HRegion
name|metaRegion
init|=
name|utils
operator|.
name|getMetaRegion
argument_list|(
name|info
argument_list|)
decl_stmt|;
name|utils
operator|.
name|scanMetaRegion
argument_list|(
name|info
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
name|tableInfo
parameter_list|)
throws|throws
name|IOException
block|{
name|HTableDescriptor
name|desc
init|=
name|tableInfo
operator|.
name|getTableDesc
argument_list|()
decl_stmt|;
name|Path
name|tableDir
init|=
name|HTableDescriptor
operator|.
name|getTableDir
argument_list|(
name|rootDir
argument_list|,
name|desc
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|HColumnDescriptor
name|column
range|:
name|desc
operator|.
name|getFamilies
argument_list|()
control|)
block|{
if|if
condition|(
name|column
operator|.
name|isBloomFilterEnabled
argument_list|()
condition|)
block|{
comment|// Column has a bloom filter
name|migrationNeeded
operator|=
literal|true
expr_stmt|;
name|Path
name|filterDir
init|=
name|HStoreFile
operator|.
name|getFilterDir
argument_list|(
name|tableDir
argument_list|,
name|tableInfo
operator|.
name|getEncodedName
argument_list|()
argument_list|,
name|column
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|filterDir
argument_list|)
condition|)
block|{
comment|// Filter dir exists
if|if
condition|(
name|readOnly
condition|)
block|{
comment|// And if we are only checking to see if a migration is
comment|// needed - it is. We're done.
return|return
literal|false
return|;
block|}
comment|// Delete the filter
name|fs
operator|.
name|delete
argument_list|(
name|filterDir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// Update the HRegionInfo in meta
name|utils
operator|.
name|updateMETARegionInfo
argument_list|(
name|metaRegion
argument_list|,
name|tableInfo
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
literal|true
return|;
block|}
block|}
argument_list|)
expr_stmt|;
comment|// Stop scanning if only doing a check and we've determined that a
comment|// migration is needed. Otherwise continue by returning true
return|return
name|readOnly
operator|&&
name|migrationNeeded
condition|?
literal|false
else|:
literal|true
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|utils
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"static-access"
argument_list|)
specifier|private
name|int
name|parseArgs
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
block|{
name|Options
name|opts
init|=
operator|new
name|Options
argument_list|()
decl_stmt|;
name|GenericOptionsParser
name|parser
init|=
operator|new
name|GenericOptionsParser
argument_list|(
name|this
operator|.
name|getConf
argument_list|()
argument_list|,
name|opts
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
literal|1
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
if|if
condition|(
name|remainingArgs
index|[
literal|0
index|]
operator|.
name|compareTo
argument_list|(
literal|"check"
argument_list|)
operator|==
literal|0
condition|)
block|{
name|this
operator|.
name|readOnly
operator|=
literal|true
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|remainingArgs
index|[
literal|0
index|]
operator|.
name|compareTo
argument_list|(
literal|"upgrade"
argument_list|)
operator|!=
literal|0
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
return|return
literal|0
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
literal|"Usage: bin/hbase migrate { check | upgrade } [options]\n"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"  check                            perform upgrade checks only."
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"  upgrade                          perform upgrade checks and modify hbase.\n"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"  Options are:"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"    -conf<configuration file>     specify an application configuration file"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"    -D<property=value>            use value for given property"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"    -fs<local|namenode:port>      specify a namenode"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Main program    *     * @param args command line arguments    */
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
init|=
literal|0
decl_stmt|;
try|try
block|{
name|status
operator|=
name|ToolRunner
operator|.
name|run
argument_list|(
operator|new
name|Migrate
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

