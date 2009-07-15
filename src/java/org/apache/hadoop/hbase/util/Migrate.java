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
name|client
operator|.
name|Put
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
comment|/**  * Perform a migration.  * HBase keeps a file in hdfs named hbase.version just under the hbase.rootdir.  * This file holds the version of the hbase data in the Filesystem.  When the  * software changes in a manner incompatible with the data in the Filesystem,  * it updates its internal version number,  * {@link HConstants#FILE_SYSTEM_VERSION}.  This wrapper script manages moving  * the filesystem across versions until there's a match with current software's  * version number.  This script will only cross a particular version divide.  You may  * need to install earlier or later hbase to migrate earlier (or older) versions.  *   *<p>This wrapper script comprises a set of migration steps.  Which steps  * are run depends on the span between the version of the hbase data in the  * Filesystem and the version of the current softare.  *   *<p>A migration script must accompany any patch that changes data formats.  *   *<p>This script has a 'check' and 'execute' mode.  Adding migration steps,  * its important to keep this in mind.  Testing if migration needs to be run,  * be careful not to make presumptions about the current state of the data in  * the filesystem.  It may have a format from many versions previous with  * layout not as expected or keys and values of a format not expected.  Tools  * such as {@link MetaUtils} may not work as expected when running against  * old formats -- or, worse, fail in ways that are hard to figure (One such is  * edits made by previous migration steps not being apparent in later migration  * steps).  The upshot is always verify presumptions migrating.  *   *<p>This script will migrate an hbase 0.18.x only.  *   * @see<a href="http://wiki.apache.org/hadoop/Hbase/HowToMigrate">How To Migration</a>  */
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
specifier|final
name|HBaseConfiguration
name|conf
decl_stmt|;
specifier|private
name|FileSystem
name|fs
decl_stmt|;
comment|// Gets set by migration methods if we are in readOnly mode.
name|boolean
name|migrationNeeded
init|=
literal|false
decl_stmt|;
name|boolean
name|readOnly
init|=
literal|false
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
comment|// Filesystem version we can migrate from
specifier|private
specifier|static
specifier|final
name|int
name|PREVIOUS_VERSION
init|=
literal|6
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|MIGRATION_LINK
init|=
literal|" See http://wiki.apache.org/hadoop/Hbase/HowToMigrate for more information."
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
literal|"Verifying that file system is available.."
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
literal|"Verifying that HBase is not running...."
operator|+
literal|"Trys ten times  to connect to running master"
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
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"File system version file "
operator|+
name|HConstants
operator|.
name|VERSION_FILE_NAME
operator|+
literal|" does not exist. No upgrade possible."
operator|+
name|MIGRATION_LINK
argument_list|)
throw|;
block|}
if|if
condition|(
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
name|HBASE_0_1_VERSION
operator|||
name|Integer
operator|.
name|valueOf
argument_list|(
name|versionStr
argument_list|)
operator|.
name|intValue
argument_list|()
operator|<
name|PREVIOUS_VERSION
condition|)
block|{
name|String
name|msg
init|=
literal|"Cannot upgrade from "
operator|+
name|versionStr
operator|+
literal|" to "
operator|+
name|HConstants
operator|.
name|FILE_SYSTEM_VERSION
operator|+
literal|" you must install an earlier hbase, run "
operator|+
literal|"the upgrade tool, reinstall this version and run this utility again."
operator|+
name|MIGRATION_LINK
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|msg
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
name|msg
argument_list|)
throw|;
block|}
name|migrate6to7
argument_list|()
expr_stmt|;
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
comment|// Move the fileystem version from 6 to 7.
specifier|private
name|void
name|migrate6to7
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|readOnly
operator|&&
name|this
operator|.
name|migrationNeeded
condition|)
block|{
return|return;
block|}
comment|// Before we start, make sure all is major compacted.
if|if
condition|(
operator|!
name|allMajorCompacted
argument_list|()
condition|)
block|{
name|String
name|msg
init|=
literal|"All tables must be major compacted before the migration can begin."
operator|+
name|MIGRATION_LINK
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|msg
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
name|msg
argument_list|)
throw|;
block|}
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
comment|// Preperation
comment|// TODO: Fail if not all major compacted first
comment|// TODO: Set the .META. and -ROOT- to flush at 16k?  32k?
comment|// TODO: Enable block cache on all tables
comment|// TODO: Rewrite MEMCACHE_FLUSHSIZE as MEMSTORE_FLUSHSIZE – name has changed.
comment|// TODO: Remove tableindexer 'index' attribute index from TableDescriptor (See HBASE-1586)
comment|// TODO: TODO: Move of in-memory parameter from table to column family (from HTD to HCD).
comment|// TODO: Purge isInMemory, etc., methods from HTD as part of migration.
comment|// TODO: Clean up old region log files (HBASE-698)
name|updateVersions
argument_list|(
name|utils
operator|.
name|getRootRegion
argument_list|()
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
expr_stmt|;
name|enableBlockCache
argument_list|(
name|utils
operator|.
name|getRootRegion
argument_list|()
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
expr_stmt|;
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
if|if
condition|(
name|readOnly
operator|&&
operator|!
name|migrationNeeded
condition|)
block|{
name|migrationNeeded
operator|=
literal|true
expr_stmt|;
return|return
literal|false
return|;
block|}
name|updateVersions
argument_list|(
name|utils
operator|.
name|getRootRegion
argument_list|()
argument_list|,
name|info
argument_list|)
expr_stmt|;
name|enableBlockCache
argument_list|(
name|utils
operator|.
name|getRootRegion
argument_list|()
argument_list|,
name|info
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"TODO: Note on make sure not using old hbase-default.xml"
argument_list|)
expr_stmt|;
comment|/*        * hbase.master / hbase.master.hostname are obsolete, that's replaced by hbase.cluster.distributed. This config must be set to "true" to have a fully-distributed cluster and the server lines in zoo.cfg must not point to "localhost".  The clients must have a valid zoo.cfg in their classpath since we don't provide the master address.  hbase.master.dns.interface and hbase.master.dns.nameserver should be set to control the master's address (not mandatory).        */
name|LOG
operator|.
name|info
argument_list|(
literal|"TODO: Note on zookeeper config. before starting:"
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
comment|/**    * Runs through the hbase rootdir and checks all stores have only    * one file in them -- that is, they've been major compacted.  Looks    * at root and meta tables too.    * @param fs    * @param c    * @return True if this hbase install is major compacted.    * @throws IOException    */
specifier|public
specifier|static
name|boolean
name|isMajorCompacted
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|HBaseConfiguration
name|c
parameter_list|)
throws|throws
name|IOException
block|{
name|FileStatus
index|[]
name|directories
init|=
name|fs
operator|.
name|listStatus
argument_list|(
operator|new
name|Path
argument_list|(
name|c
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|HBASE_DIR
argument_list|)
argument_list|)
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
name|p
parameter_list|)
block|{
name|boolean
name|isdir
init|=
literal|false
decl_stmt|;
try|try
block|{
name|isdir
operator|=
name|fs
operator|.
name|getFileStatus
argument_list|(
name|p
argument_list|)
operator|.
name|isDir
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
return|return
name|isdir
return|;
block|}
block|}
argument_list|)
decl_stmt|;
block|}
comment|/*    * Enable blockcaching on catalog tables.    * @param mr    * @param oldHri    */
name|void
name|enableBlockCache
parameter_list|(
name|HRegion
name|mr
parameter_list|,
name|HRegionInfo
name|oldHri
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|enableBlockCache
argument_list|(
name|oldHri
argument_list|)
condition|)
block|{
return|return;
block|}
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|oldHri
operator|.
name|getRegionName
argument_list|()
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|HConstants
operator|.
name|REGIONINFO_QUALIFIER
argument_list|,
name|Writables
operator|.
name|getBytes
argument_list|(
name|oldHri
argument_list|)
argument_list|)
expr_stmt|;
name|mr
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Enabled blockcache on "
operator|+
name|oldHri
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/*    * @param hri Update versions.    * @param true if we changed value    */
specifier|private
name|boolean
name|enableBlockCache
parameter_list|(
specifier|final
name|HRegionInfo
name|hri
parameter_list|)
block|{
name|boolean
name|result
init|=
literal|false
decl_stmt|;
name|HColumnDescriptor
name|hcd
init|=
name|hri
operator|.
name|getTableDesc
argument_list|()
operator|.
name|getFamily
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
decl_stmt|;
if|if
condition|(
name|hcd
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"No info family in: "
operator|+
name|hri
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
comment|// Set blockcache enabled.
name|hcd
operator|.
name|setBlockCacheEnabled
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
comment|/*    * Update versions kept in historian.    * @param mr    * @param oldHri    */
name|void
name|updateVersions
parameter_list|(
name|HRegion
name|mr
parameter_list|,
name|HRegionInfo
name|oldHri
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|updateVersions
argument_list|(
name|oldHri
argument_list|)
condition|)
block|{
return|return;
block|}
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|oldHri
operator|.
name|getRegionName
argument_list|()
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|HConstants
operator|.
name|REGIONINFO_QUALIFIER
argument_list|,
name|Writables
operator|.
name|getBytes
argument_list|(
name|oldHri
argument_list|)
argument_list|)
expr_stmt|;
name|mr
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Upped versions on "
operator|+
name|oldHri
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/*    * @param hri Update versions.    * @param true if we changed value    */
specifier|private
name|boolean
name|updateVersions
parameter_list|(
specifier|final
name|HRegionInfo
name|hri
parameter_list|)
block|{
name|boolean
name|result
init|=
literal|false
decl_stmt|;
name|HColumnDescriptor
name|hcd
init|=
name|hri
operator|.
name|getTableDesc
argument_list|()
operator|.
name|getFamily
argument_list|(
name|HConstants
operator|.
name|CATALOG_HISTORIAN_FAMILY
argument_list|)
decl_stmt|;
if|if
condition|(
name|hcd
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"No region historian family in: "
operator|+
name|hri
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
comment|// Set historian records so they timeout after a week.
if|if
condition|(
name|hcd
operator|.
name|getTimeToLive
argument_list|()
operator|==
name|HConstants
operator|.
name|FOREVER
condition|)
block|{
name|hcd
operator|.
name|setTimeToLive
argument_list|(
name|HConstants
operator|.
name|WEEK_IN_SECONDS
argument_list|)
expr_stmt|;
name|result
operator|=
literal|true
expr_stmt|;
block|}
comment|// Set the versions up to 10 from old default of 1.
name|hcd
operator|=
name|hri
operator|.
name|getTableDesc
argument_list|()
operator|.
name|getFamily
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
expr_stmt|;
if|if
condition|(
name|hcd
operator|.
name|getMaxVersions
argument_list|()
operator|==
literal|1
condition|)
block|{
comment|// Set it to 10, an arbitrary high number
name|hcd
operator|.
name|setMaxVersions
argument_list|(
literal|10
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
literal|"Usage: bin/hbase migrate {check | upgrade} [options]"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|()
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
literal|"  upgrade                          perform upgrade checks and modify hbase."
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|()
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

